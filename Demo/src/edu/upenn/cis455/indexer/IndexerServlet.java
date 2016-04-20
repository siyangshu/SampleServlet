package edu.upenn.cis455.indexer;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.io.IOException;
import java.util.*;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.identitymanagement.model.Position;

import edu.upenn.cis455.indexer.DynamodbConnector;
import sun.net.www.http.HttpClient;

public class IndexerServlet extends HttpServlet {
	static final long serialVersionUID = 455555001;
	private DynamodbConnector connector;

	public static void sop(Object o) {
		System.out.println(o);
	}

	@Override
	public void init(ServletConfig config) {
		String workingDirectory = System.getProperty("user.dir");
		System.out.println("Working Directory = " + workingDirectory);
		connector = new DynamodbConnector();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
		String path = request.getPathInfo();
		PrintWriter out = response.getWriter();
		if (path.equals("/login")) {
			out.write("<!DOCTYPE html><html><body><form action=\"login\" method=\"post\">  name:<br>  <input type=\"text\" name=\"name\">  <br>  password:<br>  <input type=\"text\" name=\"password\">  <br><br>  <input type=\"submit\" value=\"submit\"></form> </body></html>");
			return;
		} else if (path.equals("/logout")) {
			HttpSession session=request.getSession();  
            session.invalidate();
			printMessage(out, "You are successfully logged out!");
            return;
		}
		
		HttpSession session = request.getSession(false);  
        if (session == null) {  
        	printMessage(out, "Please login first");
        	return;
        }  
        
	    if (path.equals("/parse_content_form")) {
			out.write("<!DOCTYPE html><html><body><form action=\"parse_content\" method=\"post\">  url:<br>  <input type=\"text\" name=\"url\">  <br>  content:<br>  <input type=\"text\" name=\"content\">  <br><br>  <input type=\"submit\" value=\"click to submit\"></form> </body></html>");
		} else if (path.equals("/get_hits")) {
			String word = request.getParameter("word");
			String url = request.getParameter("url");
			IndexerItem item = connector.readItem(word, url);
			printMessage(out, item.toString());
		} else {
			printMessage(out, "Invalid get path: " + path);
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
		String path = request.getPathInfo();
		PrintWriter out = response.getWriter();
		if (path.equals("/login")) {
			String name = request.getParameter("name");  
	        String password = request.getParameter("password");
	        if (validUser(name, password)) {
	        	request.getSession().setAttribute("name", name);
				printMessage(out, "Welcome!");
	        } else {
				printMessage(out, "Wrong username or password!");	        	
	        }
	        return;
		} 
		
		HttpSession session = request.getSession(false);  
        if (session == null) {  
        	printMessage(out, "Please login first");
        	return;
        }  

        else if (path.equals("/parse_content")) {
			String url = request.getParameter("url");
			String content = request.getParameter("content");
			parseContent(url, content);
		} else {
			printMessage(out, "Invalid post path: " + path);
		}
	}

	void printMessage(PrintWriter out, String message) {
		out.println("<!DOCTYPE html><html><body><p>" + message + "</p></body></html>");
	}
	
	boolean validUser(String name, String password) {
		return password.equals("123qweasdzxc");
	}
	
	public void parseContent(String url, String content) {
		Document doc = Jsoup.parse(content);
		HitsVisitor visitor = new HitsVisitor();
        NodeTraversor traversor = new NodeTraversor(visitor);
        traversor.traverse(doc);
        for (String word : visitor.wordToHits.keySet()) {
        	IndexerItem item = new IndexerItem();
        	item.setWord(word);
        	item.setUrl(url);
        	item.setTotalWord(visitor.totalWord);
        	item.setHits(visitor.wordToHits.get(word));
        	// TODO save or print
//        	sop(item);
        	connector.writeItem(item);
        }
	}
	
    private class HitsVisitor implements NodeVisitor {
		Map<String, List<Hit>> wordToHits = new HashMap<>();
		int position = 0;
		int totalWord = 0;

        // hit when the node is first seen
		@Override
        public void head(Node node, int depth) {
//            String name = node.nodeName();
            if (node instanceof TextNode) {
            	String words = ((TextNode) node).text();
            	for (String word : words.split("\\s+")) {
            		if (word.isEmpty()) {
            			continue;
            		}
        			totalWord += 1;
            		String stemmedWord = stem(word);
            		List<Hit> hits = wordToHits.get(stemmedWord);
            		if (hits == null) {
            			hits = new ArrayList<>();
            		}
                	Hit hit = new Hit();
                	hit.setFont(node.attr("style"));
                	hit.setPosition(position++);
                	hit.setCapitalization(getCapitalization(word));
            		hits.add(hit);
            		wordToHits.put(stemmedWord, hits);
            	}
            }
		}

		@Override
		public void tail(Node node, int depth) {
			// TODO Auto-generated method stub
		}
		
		public String stem(String word) {
			// TODO
			return word.toLowerCase();
		}
		
		public String getCapitalization(String word) {
			// TODO
			return word;
		}
    } 
}
