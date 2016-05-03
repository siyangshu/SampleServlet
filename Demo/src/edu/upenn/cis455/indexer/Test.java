package edu.upenn.cis455.indexer;

import javax.servlet.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class Test {
	public static void sop(Object x) {
		System.out.println(x);
	}

	public static void main(String[] args) {
//		Stemmer stemmer = new Stemmer();
//		sop(stemmer.stem("only"));
		sop("A!N@N#M.$".toUpperCase().equals("A!N@N#M.$"));
		
//		IndexerServlet servlet = new IndexerServlet();
////		ServletConfig config = new GenericServlet();
//		servlet.init(null);
//		String content = "<!doctype html><html><head>    <title>SIYANG Shu siyang Shu</title></head><body>    <h1>        Head line.    </h1>    <p>        Paragraph        <a href=\"http://www.google.com/\">Google</a>     </p>    <img src=\"demo.jpeg\"></body></html>";
//		servlet.parseContent("example.com", content);
	}

}
