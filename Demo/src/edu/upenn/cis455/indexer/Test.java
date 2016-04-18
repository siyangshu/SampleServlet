package edu.upenn.cis455.indexer;

import javax.servlet.*;

public class Test {

	public static void main(String[] args) {
		IndexerServlet servlet = new IndexerServlet();
//		ServletConfig config = new GenericServlet();
		servlet.init(null);
		String content = "<!doctype html><html><head>    <title>SIYANG Shu siyang Shu</title></head><body>    <h1>        Head line.    </h1>    <p>        Paragraph        <a href=\"http://www.google.com/\">Google</a>     </p>    <img src=\"demo.jpeg\"></body></html>";
		servlet.parseContent("example.com", content);
	}

}
