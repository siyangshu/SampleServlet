package edu.upenn.cis455.indexer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;

public class DynamodbConnector {
//	DynamoDB dynamoDB = new DynamoDB(new AmazonDynamoDBClient(new ProfileCredentialsProvider()));
	DynamoDBMapper mapper = new DynamoDBMapper(new AmazonDynamoDBClient(new ProfileCredentialsProvider()));
//    String tableName = "Indexer";
    
    public DynamodbConnector() {
    }
    
    public static void sop(Object x) {
    	System.out.println(x);
    }
    
    public void writeItem(IndexerItem item) {
    	mapper.save(item);
    }

	public void createItems() {
		IndexerItem item = new IndexerItem();
		item.setWord("new");
		item.setUrl("www.google.com");
		Hit hit = new Hit();
		hit.setCapitalization("ALL UPPERCASE");
		hit.setFont("14");
		hit.setPosition(100);
		List<Hit> hits = new ArrayList<>();
		hits.add(hit);
		item.setHits(hits);
		mapper.save(item); 
	}

}
