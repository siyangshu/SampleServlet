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
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;

import edu.upenn.cis455.indexer.DynamodbConnector;
import edu.upenn.cis455.mapreduce.Job;

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
		PrintWriter out = response.getWriter();
		connector.createItems();
		printMessage(out, "hello world");
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {

	}

	void printMessage(PrintWriter out, String message) {
		out.println("<!DOCTYPE html><html><body><p>" + message + "</p></body></html>");
	}
}
