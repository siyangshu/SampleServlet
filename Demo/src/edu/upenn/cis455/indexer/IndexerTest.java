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
//public class IndexerTest {
//	Stemmer stemmer = new Stemmer();
//
//	@Before
//	public void setUp() throws Exception {
//    }
//
//	public static void sop(Object x) {
//		System.out.println(x);
//	}
//	
//	@Test
//	public void test_HitsVisitor() {
//		String content = "<!DOCTYPE html><html><body><p><a href=\"\">This   is a test paragraph. this is not REAL. <h1>Only test</h1></a></p> </body></html>";
//		Document doc = Jsoup.parse(content);
//		HitsVisitor visitor = new HitsVisitor();
//	    NodeTraversor traversor = new NodeTraversor(visitor);
//	    traversor.traverse(doc);
//	    Map<String, List<Hit>> wordToHits = visitor.getWordToHits();
//	    Map<String, Double> wordToRelevance = visitor.getWordToRelevance();
//	    
//	    for (String str : wordToHits.keySet()) {
////	    	sop(str);
//	    }
//	    
//	    assertEquals(2, wordToHits.get(stemmer.stem("test")).size());
//	    
//	    int pos = wordToHits.get(stemmer.stem("paragraph")).get(0).getPosition();
//	    assertEquals(4, pos);
//	    
//	    int cap = wordToHits.get(stemmer.stem("real")).get(0).getCapitalization();
//	    assertEquals(2, cap);
//	    
//	    cap = wordToHits.get(stemmer.stem("ReaL")).get(0).getCapitalization();
//	    assertEquals(2, cap);
//	    
//	    assertEquals(1, wordToHits.get(stemmer.stem("only")).size());
//	    
//	    cap = wordToHits.get(stemmer.stem("Only")).get(0).getCapitalization();
//	    assertEquals(1, cap);
//
//	    assertEquals("h1", wordToHits.get(stemmer.stem("Only")).get(0).getTextClassification());
//	    
//	    assertEquals(null, wordToHits.get(stemmer.stem("this")).get(0).getTextClassification());
//	    
//	    assertEquals(11, visitor.getWordCount());
//
//	    assertEquals(2, wordToHits.get(stemmer.stem("this") + " " + stemmer.stem("is")).size());
//	}
//	
//
//}
