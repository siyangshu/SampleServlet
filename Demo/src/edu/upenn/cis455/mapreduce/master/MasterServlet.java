package edu.upenn.cis455.mapreduce.master;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import edu.upenn.cis455.mapreduce.Job;

import sun.net.www.http.HttpClient;

public class MasterServlet extends HttpServlet {
	static final long serialVersionUID = 455555001;

	public static void sop(Object o) {
		System.out.println(o);
	}

	@Override
	public void init(ServletConfig config) {
		String workingDirectory = System.getProperty("user.dir");
		System.out.println("Working Directory = " + workingDirectory);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
		PrintWriter out = response.getWriter();
		printMessage(out, "hello world");
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {

	}

	void printMessage(PrintWriter out, String message) {
		out.println("<!DOCTYPE html><html><body><p>" + message + "</p></body></html>");
	}
}
