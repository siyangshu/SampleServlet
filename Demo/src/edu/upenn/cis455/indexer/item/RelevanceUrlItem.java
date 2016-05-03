package edu.upenn.cis455.indexer.item;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="RelevanceUrl")
public class RelevanceUrlItem {
	private String word;
	private String relevanceUrl;
        
    @DynamoDBHashKey(attributeName="WORD")  
    public String getWord() { return word;}
    public void setWord(String word) {this.word = word;}
    
    @DynamoDBRangeKey(attributeName="R_URL")  
    public String getRelevanceUrl() { return relevanceUrl; }
    public void setRelevanceUrl(String relevanceUrl) { this.relevanceUrl = relevanceUrl; }
    
    @DynamoDBIgnore
    public String getUrl() {
    	return relevanceUrl.substring(6);
    }

    @DynamoDBIgnore
    public void setUrl(String url, double relevance) {
    	// 6 digit int
    	relevance *= 1_000_000;
    	int relevanceInt = (int) relevance;
    	relevanceInt = 999_999 - relevanceInt;
    	if (relevanceInt < 0) {
    		relevanceInt = 0;
    		System.out.println("relevance to small: relevanceInt: " + relevanceInt + ", relevance: " + relevance);
    	}
    	if (relevanceInt >= 1_000_000) {
    		relevanceInt = 1_000_000;
    		System.out.println("relevance to large: relevanceInt: " + relevanceInt + ", relevance: " + relevance);
    	}
    	setRelevanceUrl(String.format("%06d", relevanceInt) + url);
    }
}
