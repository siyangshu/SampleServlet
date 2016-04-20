package edu.upenn.cis455.indexer;

import java.util.List;
import java.util.Set;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshalling;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.JsonMarshaller;

@DynamoDBTable(tableName="Indexer")
public class IndexerItem {
	private String word;
	private String url;
	private Integer totalWord;
	private List<Hit> hits;
        
    @DynamoDBHashKey(attributeName="WORD")  
    public String getWord() { return word;}
    public void setWord(String word) {this.word = word;}
    
    @DynamoDBRangeKey(attributeName="URL")  
    public String getUrl() {return url; }
    public void setUrl(String url) { this.url = url; }
    
    @DynamoDBAttribute(attributeName="TOTAL_WORD")  
    public Integer getTotalWord() { return totalWord;}
    public void setTotalWord(Integer totalWord) {this.totalWord = totalWord;}

    @DynamoDBAttribute(attributeName = "hits")
    public List<Hit> getHits() { return hits; }    
    public void setHits(List<Hit> hits) { this.hits = hits; }
    
    @Override
    public String toString(){
    	String result = "";
    	result += word + ":" + url + "\tHits: ";
    	for (Hit hit : hits) {
    		result += hit.toString() + "\t";
    	}
    	return result;
    }

}

class HitJSONMarshaller extends JsonMarshaller<Hit> { }