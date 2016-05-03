package edu.upenn.cis455.indexer;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;

import edu.upenn.cis455.indexer.item.*;

public class Indexer {
	IndexingPerformer indexingPerformer = new IndexingPerformer();
	
	public List<String> getRelevantUrls(String stemmedWord, int atMost) {
		
		return null;
	}
	
	public List<Hit> getHits(String stemmedPhrase, String url) {
		return null;
	}
	
	public void startIndexing() {
		indexingPerformer.start();
	}
	
	public void stopIndexing() {
		indexingPerformer.stop();
//		for (IndexingPerformer performer : indexingPerformers) {
//			performer.stop();
//		}
	}
	
	public void getIndexingStatus() {
		
	}
}

class DynamodbConnector {
	DynamoDBMapper mapper = new DynamoDBMapper(new AmazonDynamoDBClient(new ProfileCredentialsProvider()));
	
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
}

class IndexingPerformer {
	final int THREAD_NUMBER = 100;
	List<IndexingPerformerThread> indexingPerformerThreads = new ArrayList<>();
	DynamoDBMapper mapper = new DynamoDBMapper(new AmazonDynamoDBClient(new ProfileCredentialsProvider()));
	S3Connector s3Connector = new S3Connector();
	
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
	
	String getUrlContent(String url) throws IOException {
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
				try {
					content = getUrlContent(url);
				} catch (IOException e) {
					System.err.println("can not get the content of url: " + url);
					e.printStackTrace();
					continue;
				}
				// parse
				parseContent(url, content);
				
				// save
			}
		}
				
		void parseContent(String url, String content) {
			Document doc = Jsoup.parse(content);
			HitsVisitor visitor = new HitsVisitor();
	        NodeTraversor traversor = new NodeTraversor(visitor);
	        traversor.traverse(doc);
	        Set<String> words = visitor.getWordToHits().keySet();
	        for (String word : words) {
	        	// save to table RelevanceUrl
	        	RelevanceUrlItem relevanceUrlItem = new RelevanceUrlItem();
	        	Double relevance = visitor.getWordToRelevance().get(word);
	        	relevanceUrlItem.setWord(word);
	        	relevanceUrlItem.setUrl(url, relevance);
	        	mapper.save(relevanceUrlItem);
	        	// save to table Hits
	        	HitsItem hitsItem = new HitsItem();
	        	hitsItem.setWord(word);
	        	hitsItem.setUrl(url);
	        	hitsItem.setHits(visitor.getWordToHits().get(word));
	        	mapper.save(hitsItem);
	        }
        	// save to table PageAttributes
	        // TODO? css query
        	PageAttributesItem pageAttributesItem = new PageAttributesItem();
        	pageAttributesItem.setUrl(url);
        	pageAttributesItem.setTitle(doc.select("head/title").text());
        	pageAttributesItem.setWordCount(visitor.getWordCount());
        	pageAttributesItem.setDescription(doc.select("p[0]").text());
        	pageAttributesItem.setMaxWordFrequency(visitor.getMaxFrequency());
        	mapper.save(pageAttributesItem);
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
	AmazonS3 s3client = new AmazonS3Client();

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
				file.createNewFile();
			    urlQueueIndex = 0;
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
	
	String getUrlContent(String url) throws IOException {
		String bucketName = urlContentS3BucketName;
		String key = url;
		S3Object s3object = s3client.getObject(new GetObjectRequest(bucketName, key));
		return new Scanner(s3object.getObjectContent()).useDelimiter("\\A").next();
	}
}

class HitsVisitor implements NodeVisitor {
	private Map<String, List<Hit>> wordToHits = new HashMap<>();
	private Map<String, List<Hit>> twoWordToHits;
	private Map<String, Double> wordToRelevance;
	private int curPosition = 0;
	private int totalWordCount = 0;
	private Stemmer stemmer = new Stemmer();
//    private Pattern specialChar = Pattern.compile("[^a-zA-Z]");
    private Set<String> textClassification = new HashSet<>(Arrays.asList("title", "h1", "h2", "h3"));
    private final int TEXT_CLASSIFICATION_ITERATE_LENGTH = 2;

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

	// hit when the node is first seen
	@Override
    public void head(Node node, int depth) {
		if (depth == 0) {
			twoWordToHits = new HashMap<>();
		}
        if (node instanceof TextNode) {
        	String[] wordsArray = ((TextNode) node).text().split("\\s+");
        	// remove special letters, calc capitalization
        	List<String> words = new ArrayList<>();
        	List<Integer> capitalization = new ArrayList<>();
        	for (int i = 0; i < wordsArray.length; i++) {
        		if (wordsArray[i].isEmpty()) {
        			continue;
        		}
        		capitalization.add(calculateCapitalization(wordsArray[i]));
        		words.add(stemmer.stem(wordsArray[i]));
        	}
        	// produce hit
        	String textClassification = calculateTextClassification(node);
        	for (int i = 0; i < words.size(); i++) {
    			totalWordCount += 1;
    			String word;
    			// one-word
    			word = words.get(i);
        		List<Hit> hits = wordToHits.get(word);
        		if (hits == null) {
        			hits = new ArrayList<>();
        		}
            	Hit hit = new Hit();
            	hit.setPosition(curPosition++);
            	hit.setCapitalization(capitalization.get(i));
            	hit.setTextClassification(textClassification);
        		hits.add(hit);
        		wordToHits.put(word, hits);
        		// two-word
        		if (i == words.size() - 1) {
        			break;
        		}
        		word = words.get(i) + " " + words.get(i + 1);
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
			Map<String, Double> wordToRelevance = new HashMap<>();
			for (String phrase : wordToHits.keySet()) {
				double relevance = 1.0 * wordToHits.get(phrase).size() / (totalWordCount + 1);
				wordToRelevance.put(phrase, relevance);
			}
		}
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