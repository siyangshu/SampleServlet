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
	private List<Hit> hits;
        
    @DynamoDBHashKey(attributeName="WORD")  
    public String getWord() { return word;}
    public void setWord(String word) {this.word = word;}
    
    @DynamoDBRangeKey(attributeName="URL")  
    public String getUrl() {return url; }
    public void setUrl(String url) { this.url = url; }
    
    @DynamoDBMarshalling (marshallerClass = HitJSONMarshaller.class)
    public List<Hit> getHits() { return hits; }    
    public void setHits(List<Hit> hits) { this.hits = hits; }

}

class HitJSONMarshaller extends JsonMarshaller<Hit> { }