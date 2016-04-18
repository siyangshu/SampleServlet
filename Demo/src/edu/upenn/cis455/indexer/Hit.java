package edu.upenn.cis455.indexer;

import com.amazonaws.services.dynamodbv2.datamodeling.*;

@DynamoDBDocument
public class Hit {
	private Integer position;
	private String font;
	private String capitalization;

	@DynamoDBAttribute(attributeName = "position")
	public Integer getPosition() {
		return position;
	}
	public void setPosition(Integer position) {
		this.position = position;
	}

	@DynamoDBAttribute(attributeName = "font")
	public String getFont() {
		return font;
	}
	public void setFont(String font) {
		this.font = font;
	}

	@DynamoDBAttribute(attributeName = "capitalization")
	public String getCapitalization() {
		return capitalization;
	}
	public void setCapitalization(String capitalization) {
		this.capitalization = capitalization;
	}	
	
	@Override
	public String toString() {
		return "position: " + position + ", font: " + font + ", capitalization: " + capitalization;
	}
}
