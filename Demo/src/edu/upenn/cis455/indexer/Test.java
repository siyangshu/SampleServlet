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
        String workingDirectory = System.getProperty("user.dir");
        System.out.println("Working Directory = " + workingDirectory);
		Indexer indexer = new Indexer();
		indexer.clearTable();
		indexer.startIndexing();
		try {
			Thread.sleep(300_000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
