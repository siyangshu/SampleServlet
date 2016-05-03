import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

public class AnchorItemExample {

	public static void main(String[] args) {
		DynamoDBMapper mapper = new DynamoDBMapper(new AmazonDynamoDBClient(new ProfileCredentialsProvider()));
		// write to DB
		AnchorItem item = new AnchorItem();
		item.setUrlFrom("abc.com");
		item.setUrlTo("def.com");
		mapper.save(item);
		// read DB
    	item = mapper.load(AnchorItem.class, "abc.com", "def.com");

		// TODO Auto-generated method stub

	}

}
