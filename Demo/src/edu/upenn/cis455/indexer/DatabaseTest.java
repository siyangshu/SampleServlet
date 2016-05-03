//package edu.upenn.cis455.indexer;
//
//import static org.junit.Assert.*;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.select.NodeTraversor;
//import org.junit.Before;
//import org.junit.Test;
//
//import edu.upenn.cis455.indexer.DynamodbConnector;
//import edu.upenn.cis455.indexer.item.*;
//
//public class DatabaseTest {
//	DynamodbConnector dynamodbConnector = new DynamodbConnector();
//	Stemmer stemmer = new Stemmer();
//
//	@Before
//	public void setUp() throws Exception {
//		// relevance url
//    	RelevanceUrlItem relevanceUrlItem;
//		relevanceUrlItem = new RelevanceUrlItem();
//    	relevanceUrlItem.setWord("word1");
//    	relevanceUrlItem.setUrl("url1", 0.1);
//    	dynamodbConnector.mapper.save(relevanceUrlItem);
//
//    	relevanceUrlItem = new RelevanceUrlItem();
//    	relevanceUrlItem.setWord("word1");
//    	relevanceUrlItem.setUrl("url2", 0.2);
//    	dynamodbConnector.mapper.save(relevanceUrlItem);
//
//    	relevanceUrlItem = new RelevanceUrlItem();
//    	relevanceUrlItem.setWord("word1");
//    	relevanceUrlItem.setUrl("url3", 0.3);
//    	dynamodbConnector.mapper.save(relevanceUrlItem);
//    	
//    	// hits
//    	HitsItem hitsItem = new HitsItem();
//    	hitsItem.setWord("word1");
//    	hitsItem.setUrl("url1");
//    	List<Hit> list = new ArrayList<>();
//    	Hit hit;
//    	hit = new Hit();
//    	hit.setFont("font1");
//    	hit.setPosition(10);
//    	hit.setCapitalization(2);
//    	list.add(hit);
//    	hitsItem.setHits(list);
//    	dynamodbConnector.mapper.save(hitsItem);
//    }
//
//	@Test
//	public void test_getRelevantUrls() {
//    	// save to table RelevanceUrl
//    	List<String> results = dynamodbConnector.getRelevantUrls("word1", 2);
//    	assertEquals("url3", results.get(0));
//    	assertEquals("url2", results.get(1));		
//	}
//
//	@Test
//	public void test_getHits() {
//    	List<Hit> list = dynamodbConnector.getHits("word1", "url1");
//    	assertEquals("font1", list.get(0).getFont());
//	}	
//}
//
