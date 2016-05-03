import java.util.List;
import java.util.Set;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshalling;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.JsonMarshaller;

@DynamoDBTable(tableName="Anchor")
public class AnchorItem {
	private String urlFrom;
	private String urlTo;
//	private String anchorText;
	
	@DynamoDBHashKey(attributeName="URL_FROM")  
	public String getUrlFrom() {
		return urlFrom;
	}
	public void setUrlFrom(String urlFrom) {
		this.urlFrom = urlFrom;
	}
	
	@DynamoDBRangeKey(attributeName="URL_TO")  
	public String getUrlTo() {
		return urlTo;
	}
	public void setUrlTo(String urlTo) {
		this.urlTo = urlTo;
	}
}

