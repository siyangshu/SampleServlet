package edu.upenn.cis455.indexer.item;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="PageAttributes")
public class PageAttributesItem {
	private String url;
	private String title;
	private String description;
	private Integer wordCount;
	private Integer maxWordFrequency;
	
	@DynamoDBHashKey(attributeName="URL")
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

    @DynamoDBAttribute(attributeName="TITLE")  
    public String getTitle() {
		return title;
	}
    public void setTitle(String title) {
		this.title = title;
	}
	
    @DynamoDBAttribute(attributeName="DES")  
    public String getDescription() {
		return description;
	}
    public void setDescription(String description) {
		this.description = description;
	}
	
	@DynamoDBAttribute(attributeName="COUNT")
	public Integer getWordCount() {
		return wordCount;
	}
	public void setWordCount(Integer wordCount) {
		this.wordCount = wordCount;
	}

	@DynamoDBAttribute(attributeName="MAX_WORD_FREQUENCY")
	public Integer getMaxWordFrequency() {
		return maxWordFrequency;
	}
	public void setMaxWordFrequency(Integer maxWordFrequency) {
		this.maxWordFrequency = maxWordFrequency;
	}
}