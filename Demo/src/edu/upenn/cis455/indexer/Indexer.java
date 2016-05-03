package edu.upenn.cis455.indexer;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;

import edu.upenn.cis455.indexer.item.*;

class Debug {
    static final boolean debug = false;
    static final boolean dynamodbOpen = false;
    static final boolean printEmptyContentUrl = false;
}

public class Indexer {
	DynamodbConnector dynamodbConnector = new DynamodbConnector();
	IndexingPerformer indexingPerformer = new IndexingPerformer(dynamodbConnector);
	
	public List<String> getRelevantUrls(String stemmedWord, int atMost) {
		return dynamodbConnector.getRelevantUrls(stemmedWord, atMost);
	}
	
	public List<Hit> getHits(String stemmedPhrase, String url) {
		return dynamodbConnector.getHits(stemmedPhrase, url);
	}
	
	public void startIndexing() {
		indexingPerformer.start();
	}
	
	public void stopIndexing() {
		indexingPerformer.stop();
		dynamodbConnector.flush();
//		for (IndexingPerformer performer : indexingPerformers) {
//			performer.stop();
//		}
	}
	
	public void getIndexingStatus() {
		
	}

	public void clearTable() {
		System.out.println("Are you sure you want to clear all dynamodb tables? enter yes to confirm: ");
		Scanner scanner = new Scanner(System.in);
		String input = scanner.nextLine();
		if (input.equals("yes")) {
			System.out.println("clearing...");
			dynamodbConnector.clearTable();
			System.out.println("done");
		} else {
			System.out.println("canceled");
		}
	}
}

class DynamodbConnector {
	DynamoDBMapper mapper = new DynamoDBMapper(new AmazonDynamoDBClient(new ProfileCredentialsProvider()));
	int writtenItemCount = 0;
	
	List<String> getRelevantUrls(String stemmedWord, int atMost) {
		// create item
		RelevanceUrlItem relevanceUrlItem = new RelevanceUrlItem();
		relevanceUrlItem.setWord(stemmedWord);
		// create query expression
		DynamoDBQueryExpression<RelevanceUrlItem> queryExpression = new DynamoDBQueryExpression<>();
		queryExpression.setHashKeyValues(relevanceUrlItem);
		queryExpression.setLimit(atMost);
		List<RelevanceUrlItem> relevanceUrls = mapper.query(RelevanceUrlItem.class, queryExpression);
		// convert to result
		List<String> results = new ArrayList<>();
		for (RelevanceUrlItem item : relevanceUrls) {
			results.add(item.getUrl());
		}
		return results;
	}
	
	List<Hit> getHits(String stemmedPhrase, String url) {
		HitsItem item = mapper.load(HitsItem.class, stemmedPhrase, url);
		return item.getHits();
	}	

	Vector<RelevanceUrlItem> relevanceUrlItems = new Vector<>();
	void save(RelevanceUrlItem item) {
		if (relevanceUrlItems.size() > 100) {
			synchronized (this) {
				if (relevanceUrlItems.size() > 100) {
					batchSave(relevanceUrlItems);
					relevanceUrlItems = new Vector<>();
				}
			}
		}
		relevanceUrlItems.add(item);
	}
	
	Vector<HitsItem> hitsItems = new Vector<>();
	void save(HitsItem item) {
		if (hitsItems.size() > 100) {
			synchronized (this) {
				if (hitsItems.size() > 100) {
					batchSave(hitsItems);
					hitsItems = new Vector<>();
				}
			}
		}
		hitsItems.add(item);
	}
	
	Vector<PageAttributesItem> pageAttributesItems = new Vector<>();
	void save(PageAttributesItem item) {
		if (pageAttributesItems.size() > 100) {
			synchronized (this) {
				if (pageAttributesItems.size() > 10) {
					batchSave(pageAttributesItems);
					pageAttributesItems = new Vector<>();
				}
			}
		}
		pageAttributesItems.add(item);
	}
	
	synchronized void flush() {
		mapper.batchSave(hitsItems);
		hitsItems = new Vector<>();
		mapper.batchSave(pageAttributesItems);
		pageAttributesItems = new Vector<>();
		mapper.batchSave(relevanceUrlItems);
		relevanceUrlItems = new Vector<>();
	}

	void clearTable() {
		if (!Debug.dynamodbOpen) {
			return;
		}
		int count = 0;
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
		{
			PaginatedScanList<RelevanceUrlItem> result = mapper.scan(RelevanceUrlItem.class,  scanExpression);
			for (RelevanceUrlItem data : result) {
				sop("deleting " + count++);
			    mapper.delete(data);
			}
//			List<DynamoDBMapper.FailedBatch> fail = mapper.batchDelete(result);
//			sop("deleted " + fail.size() + "/" + result.size());
		}
		{
			PaginatedScanList<PageAttributesItem> result = mapper.scan(PageAttributesItem.class,  scanExpression);
			for (PageAttributesItem data : result) {
				sop("deleting " + count++);
			    mapper.delete(data);
			}
		}
		{
			PaginatedScanList<HitsItem> result = mapper.scan(HitsItem.class,  scanExpression);
			for (HitsItem data : result) {
				sop("deleting " + count++);
			    mapper.delete(data);
			}
		}
	}
	
	static void sop(Object x) {
		System.out.println(x);
	}

	public List<DynamoDBMapper.FailedBatch> batchSave(List<? extends Object> objectsToSave) {
		writtenItemCount += objectsToSave.size();
		if (Debug.dynamodbOpen) {
			List<DynamoDBMapper.FailedBatch> results = mapper.batchSave(objectsToSave);
			if (results.size() != 0) {
				System.err.println("fail to save " + results.size() + " item.");
			}
			return results;			
		} else {
			return null;			
		}
	}
	
	<T> void save(T object) {
		writtenItemCount += 1;
//		sop("here1");
		assert(false);
		mapper.save(object);
	}
}

class IndexingPerformer {
	final int THREAD_NUMBER = 10;
	List<IndexingPerformerThread> indexingPerformerThreads = new ArrayList<>();
	DynamodbConnector dynamodbConnector;
	S3Connector s3Connector = new S3Connector();
	int indexingPageCount = 0;
	
	public IndexingPerformer(DynamodbConnector dynamodbConnector) {
		this.dynamodbConnector = dynamodbConnector;
	}
	
	static void sop(Object x) {
		System.out.println(x);
	}
	
	void start() {
		s3Connector.init();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			System.err.println("IndexingPerformer init error.");
//			e.printStackTrace();
//			return;
//		}
		while (indexingPerformerThreads.size() < THREAD_NUMBER) {
			indexingPerformerThreads.add(new IndexingPerformerThread());
		}
		
		for (IndexingPerformerThread thread : indexingPerformerThreads) {
			thread.isRunning = true;
			thread.start();
		}
	}
	
	void stop() {
		for (IndexingPerformerThread thread : indexingPerformerThreads) {
			thread.isRunning = false;
		}
		for (IndexingPerformerThread thread : indexingPerformerThreads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
			}
		}
		s3Connector.stop();
	}
		
	String getUrl() {
		return s3Connector.getUrl();
	}
	
	String getUrlContent(String url) {
		return s3Connector.getUrlContent(url);
	}
	
	class IndexingPerformerThread extends Thread {
		public boolean isRunning;
		
		@Override
		public void run() {
			while(isRunning) {
				// get a url
				String url = getUrl();
				if (url == null) {
					isRunning = false;
					sop("indexing done");
					return;
				}
				// get the content
				String content;
				content = getUrlContent(url);
				if (content == null) {
					if (Debug.printEmptyContentUrl) {
						System.out.println("empty inputstream from s3. url: " + url);
					}
					continue;
				}
				// parse
				parseContent(url, content);
				
				// save
			}
		}
				
		void parseContent(String url, String content) {
//			if (indexingPageCount % 10 == 0) {
				sop("current time: " + System.currentTimeMillis());
				sop("have saved " + dynamodbConnector.writtenItemCount + " items to dynamodb");
				sop("parsing " + indexingPageCount++);
//			}
			Document doc = Jsoup.parse(content);
			HitsVisitor visitor = new HitsVisitor();
	        NodeTraversor traversor = new NodeTraversor(visitor);
	        traversor.traverse(doc);
	        Set<String> words = visitor.getWordToHits().keySet();
	        if (words.isEmpty()) {
	        	if (Debug.printEmptyContentUrl) {
		        	sop("empty file, no word to save. skip. " + url);	        		
	        	}
	        	return;
	        }
	        for (String word : words) {
	        	if (Debug.debug) {
		        	if (word.length() <= 1) {
		        		for (String s : visitor.wordToStemmedWord_debug.keySet()) {
		        			sop(s + "=>" + visitor.wordToStemmedWord_debug.get(s));
		        		}
			        	assert(false);
		        	}
	        	}
	        	// save to table RelevanceUrl
	        	RelevanceUrlItem relevanceUrlItem = new RelevanceUrlItem();
	        	Double relevance = null;
	        	relevance = visitor.getWordToRelevance().get(word);
	        	relevanceUrlItem.setWord(word);
	        	relevanceUrlItem.setUrl(url, relevance);
	        	dynamodbConnector.save(relevanceUrlItem);
	        	// save to table Hits
	        	HitsItem hitsItem = new HitsItem();
	        	hitsItem.setWord(word);
	        	hitsItem.setUrl(url);
	        	hitsItem.setHits(visitor.getWordToHits().get(word));
	        	dynamodbConnector.save(hitsItem);
	        }
        	// save to table PageAttributes
	        // TODO? css query
        	PageAttributesItem pageAttributesItem = new PageAttributesItem();
        	pageAttributesItem.setUrl(url);
        	pageAttributesItem.setTitle(doc.select("head").select("title").text());
        	pageAttributesItem.setWordCount(visitor.getWordCount());
        	pageAttributesItem.setDescription(doc.select("p[0]").text());
        	pageAttributesItem.setMaxWordFrequency(visitor.getMaxFrequency());
        	dynamodbConnector.save(pageAttributesItem);
		}	
	}
}

class S3Connector {
	String urlQueueFile = "data/to_be_indexed_urls";
	BufferedReader urlQueueFileReader;
	int urlQueueIndex = 0;
	String urlQueueIndexFile = "data/url_queue_index";
	String urlContentS3BucketName = "newcrawler";
	String toBeIndexedUrlS3BucketName = "urlqueue";
	String toBeIndexedUrlS3Key = "";
	AmazonS3 s3client;
	
	S3Connector() {
		AWSCredentials credentials = new ProfileCredentialsProvider("profile2").getCredentials();
		s3client = new AmazonS3Client(credentials);
	}

	private void retrieveUrlQueueFromS3() {
	}
	
	private void retrieveUrlQueueFromS3_other() {
    	// empty local url queue file
		try {
			PrintWriter writer = new PrintWriter(urlQueueFile);
        	writer.print("");
        	writer.close();
		} catch (FileNotFoundException e) {
			System.err.println("can not empty toBeIndexedUrlFile: " + urlQueueFile);
			e.printStackTrace();
		}
		// get S3 new urls
		String bucketName = toBeIndexedUrlS3BucketName;
		String key = toBeIndexedUrlS3Key;
		S3Object s3object;
		try {
            System.out.println("Downloading an object");
    		s3object = s3client.getObject(new GetObjectRequest(bucketName, key));
            System.out.println("Content-Type: " + s3object.getObjectMetadata().getContentType());
        } catch (AmazonServiceException ase) {
        	String errorCode = ase.getErrorCode();
            if (!errorCode.equals("NoSuchKey")) {
                // no more urls, quit
            	System.err.println("receive NoSuchKey error. perhaps no more urls");
            	return;
            }
            System.out.println("Caught an AmazonServiceException, which" +
            		" means your request made it " +
                    "to Amazon S3, but was rejected with an error response" +
                    " for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
            return;
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means"+
            		" the client encountered " +
                    "an internal error while trying to " +
                    "communicate with S3, " +
                    "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
            return;
        }
		// save it to local disk
		try {
			Files.copy(s3object.getObjectContent(), new File(urlQueueFile).toPath());
		} catch (IOException e) {
			System.err.println("fail to write to be indexed urls to disk!");
			e.printStackTrace();
			return;
		}
		// remove the file from s3
        try {
    		s3client.deleteObject(new DeleteObjectRequest(bucketName, key));
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException.");
            System.out.println("Error Message: " + ace.getMessage());
        }
		// init current line number
        writeUrlQueueIndex(0);
	}
	
	void init() {
		// read url queue index from disk
		readUrlQueueIndex();
	    // if toBeIndexedUrlFile not exist, download some
	    File file = new File(urlQueueFile);
	    if (!file.exists()) {
		    System.out.println("no to_be_indexed_urls on local disk. prepare to download some on S3");
		    retrieveUrlQueueFromS3();
	    }
	    // seek to line number
		try {
			urlQueueFileReader = new BufferedReader(new FileReader(urlQueueFile));
			String line;
			for (int i = 0; i < urlQueueIndex; i++) {
				line = urlQueueFileReader.readLine();
				if (line == null) {
					// no more line. retrive more.
					urlQueueFileReader.close();
					retrieveUrlQueueFromS3();
					urlQueueFileReader = new BufferedReader(new FileReader(urlQueueFile));
					break;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("fail to seek to line number: " + urlQueueIndex + " in file: " + urlQueueFile);
			e.printStackTrace();
		}
	}
	
	void stop() {
		// save current line number
        writeUrlQueueIndex(urlQueueIndex);		
	}
	
	private void writeUrlQueueIndex(int urlQueueIndex) {
		this.urlQueueIndex = urlQueueIndex;
		FileWriter writer;
		try {
			writer = new FileWriter(urlQueueIndexFile);
			writer.write(String.valueOf(urlQueueIndex));
			writer.close();
		} catch (IOException e) {
			System.err.println("fail to write url queue index to disk!");
			e.printStackTrace();
			return;
		}	
	}

	private void readUrlQueueIndex() {
	    try {
			File file = new File(urlQueueIndexFile);
			if (!file.exists()) {
				writeUrlQueueIndex(0);
//				file.createNewFile();
//			    urlQueueIndex = 0;
			}  else {
				Scanner sc = new Scanner(file);
				urlQueueIndex = sc.nextInt();
				sc.close();			
			}
		} catch (IOException e) {
			System.err.println("fail to open/create url queue index file");
			e.printStackTrace();
			return;
		}
	}

	synchronized String getUrl() {
		String line;
	    try {
			line = urlQueueFileReader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		urlQueueIndex++;
		return line;
	}
	
	String getUrlContent(String url) {
		String bucketName = urlContentS3BucketName;
		String key = url;
		S3Object s3object = s3client.getObject(new GetObjectRequest(bucketName, key));
		try {
			String result = new Scanner(s3object.getObjectContent()).useDelimiter("\\A").next();
			return result;			
		} catch (NoSuchElementException e) {
			return null;
		}
	}
}

class HitsVisitor implements NodeVisitor {
	private Map<String, List<Hit>> wordToHits = new HashMap<>();
	private Map<String, List<Hit>> twoWordToHits;
	private Map<String, Double> wordToRelevance;
	Map<String, String> wordToStemmedWord_debug = new HashMap<>();
	private int curPosition = 0;
	private int totalWordCount = 0;
	private Stemmer stemmer = new Stemmer();
//    private Pattern specialChar = Pattern.compile("[^a-zA-Z]");
    private Set<String> textClassification = new HashSet<>(Arrays.asList("title", "h1", "h2", "h3"));
    private final int TEXT_CLASSIFICATION_ITERATE_LENGTH = 2;
    private final int MIN_WORD_LENGTH = 2;
    private final int MAX_WORD_LENGTH = 20;

	Map<String, List<Hit>> getWordToHits() {
		return wordToHits;
	}
	
	
	Map<String, Double> getWordToRelevance() {
		return wordToRelevance;
	}
	

	int getWordCount() {
		return totalWordCount;
	}

	int getMaxFrequency() {
		int maxFrequency = 0;
		for (String word : wordToHits.keySet()) {
			maxFrequency = Math.max(wordToHits.get(word).size(), maxFrequency);
		}
		return maxFrequency;
	}
	
	boolean couldIndexing(String lowerWord) {
		return !stemmer.isStopWord(lowerWord) && MIN_WORD_LENGTH <= lowerWord.length() && lowerWord.length() <= MAX_WORD_LENGTH;
	}

	// hit when the node is first seen
	@Override
    public void head(Node node, int depth) {
		if (depth == 0) {
			twoWordToHits = new HashMap<>();
		}
        if (node instanceof TextNode) {
        	String[] wordsArray = ((TextNode) node).text().split("\\s+");
        	// remove special letters, calc capitalization
        	List<String> lowerWords = new ArrayList<>();
        	List<Integer> capitalization = new ArrayList<>();
        	for (int i = 0; i < wordsArray.length; i++) {
        		String word = stemmer.removeSpecialCharacter(wordsArray[i]);
        		if (word.isEmpty()) {
        			continue;
        		}
        		capitalization.add(calculateCapitalization(word));
        		lowerWords.add(word.toLowerCase());
        		if (Debug.debug) {
            		wordToStemmedWord_debug.put(wordsArray[i], stemmer.stem(word.toLowerCase()));        			
        		}
        	}
        	// produce hit
        	String textClassification = calculateTextClassification(node);
        	for (int i = 0; i < lowerWords.size(); i++) {
    			String word = null;
    			List<Hit> hits = null;
    			Hit hit = null;
    			// one-word
    			word = lowerWords.get(i);
    			if (couldIndexing(word)) {
    				word = stemmer.stem(word);
            		hits = wordToHits.get(word);
            		if (hits == null) {
            			hits = new ArrayList<>();
            		}
                	hit = new Hit();
                	hit.setPosition(curPosition);
                	hit.setCapitalization(capitalization.get(i));
                	hit.setTextClassification(textClassification);
            		hits.add(hit);
            		wordToHits.put(word, hits);    				
    			}
        		// two-word
        		if (i < lowerWords.size() - 1 && couldIndexing(lowerWords.get(i)) && couldIndexing(lowerWords.get(i + 1))) {
            		word = stemmer.stem(lowerWords.get(i)) + " " + stemmer.stem(lowerWords.get(i + 1));
            		hits = twoWordToHits.get(word);
            		if (hits == null) {
            			hits = new ArrayList<>();
            		}
                	hit = new Hit();
                	hit.setPosition(curPosition);
                	hit.setCapitalization(Math.max(capitalization.get(i), capitalization.get(i)));
                	hit.setTextClassification(textClassification);
            		hits.add(hit);
            		twoWordToHits.put(word, hits); 
        		}
    			totalWordCount++;
        		curPosition++;
        	}
        }
	}

	@Override
	public void tail(Node node, int depth) {
		if (depth == 0) {
			// when the work done
			// process two word
			for( Iterator<Map.Entry<String, List<Hit>>> it = twoWordToHits.entrySet().iterator(); it.hasNext(); ) {
				Map.Entry<String, List<Hit>> entry = it.next();
				if (entry.getValue().size() < 2) {
					it.remove();
				}
			}
			// add it to wordToHits
			for (String twoWord : twoWordToHits.keySet()) {
				wordToHits.put(twoWord, twoWordToHits.get(twoWord));
			}
			twoWordToHits = null;
			// init word relevance
			wordToRelevance = new HashMap<>();
			for (String phrase : wordToHits.keySet()) {
				double relevance = 1.0 * wordToHits.get(phrase).size() / (totalWordCount + 1);
				wordToRelevance.put(phrase, relevance);
			}
		}
	}
	
	public static void sop(Object x) {
		System.out.println(x);		
	}


	private int calculateCapitalization(String word) {
		word = stemmer.removeSpecialCharacter(word);
		int cap = 0;
		if (word.length() > 0 && 'A' <= word.charAt(0) && word.charAt(0) <= 'Z') {
			cap = 1;
		}
		if (word.length() > 0 && word.equals(word.toUpperCase())) {
			cap = 2;
		}
		return cap;
	}
	
	private String calculateTextClassification(Node node) {	// node should be a text node here
		for (int i = 0; i < TEXT_CLASSIFICATION_ITERATE_LENGTH; i++) {
			node = node.parentNode();
			if (node == null) {
				return null;
			} else {
				if (textClassification.contains(node.nodeName())) {
					return node.nodeName();
				}
			}
		}
		return null;
	}
	
	public String getCapitalization(String word) {
		// TODO
		return word;
	}
} 
