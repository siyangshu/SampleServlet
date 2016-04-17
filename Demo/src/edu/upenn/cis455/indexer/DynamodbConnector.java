package edu.upenn.cis455.indexer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;

public class DynamodbConnector {
    
//	static AmazonDynamoDBClient dynamoDB;
	static DynamoDB dynamoDB = new DynamoDB(new AmazonDynamoDBClient(new ProfileCredentialsProvider()));

    static String tableName = "Indexer";
    
    public DynamodbConnector() {
        try {
//			init();
		} catch (Exception e) {
			sop("can not create dynamo db");
			e.printStackTrace();
		}
    }
    
    public static void sop(Object x) {
    	System.out.println(x);
    }
    
//    private void init() throws Exception {
//        /*
//         * The ProfileCredentialsProvider will return your [default]
//         * credential profile by reading from the credentials file located at
//         * (/Users/siyangshu/.aws/credentials).
//         */
//        AWSCredentials credentials = null;
//        try {
//            credentials = new ProfileCredentialsProvider("default").getCredentials();
//        } catch (Exception e) {
//            throw new AmazonClientException(
//                    "Cannot load the credentials from the credential profiles file. " +
//                    "Please make sure that your credentials file is at the correct " +
//                    "location (/Users/siyangshu/.aws/credentials), and is in valid format.",
//                    e);
//        }
//        dynamoDB = new AmazonDynamoDBClient(credentials);
//        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
//        dynamoDB.setRegion(usWest2);
//    }
    
//    private static Map<String, AttributeValue> newItem(String word, String url, String... tokens) {
//        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
//        
//        item.put("name", new AttributeValue(word));
//        item.put("year", new AttributeValue().withN(Integer.toString(year)));
//        item.put("rating", new AttributeValue(rating));
//        item.put("fans", new AttributeValue().withSS(fans));
//
//        return item;
//    }

	public void createItems() {
		Table table = dynamoDB.getTable(tableName);
        try {

            Item item = new Item()
                .withPrimaryKey("WORD", "apple")
                .withString("URL", "www.apple.com")
                .withString("ISBN", "120-1111111111");
            table.putItem(item);
        } catch (Exception e) {
            System.err.println("Create items failed.");
            System.err.println(e.getMessage());

        }
//        Map<String, AttributeValue> item = newItem("Bill & Ted's Excellent Adventure", 1989, "****", "James", "Sara");
//        PutItemRequest putItemRequest = new PutItemRequest("Indexer", item);
//        PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
//        System.out.println("Result: " + putItemResult);
//        try {
//
//            Item item = new Item()
//                .withPrimaryKey("Id", 120)
//                .withString("Title", "Book 120 Title")
//                .withString("ISBN", "120-1111111111")
//                .withStringSet( "Authors", 
//                    new HashSet<String>(Arrays.asList("Author12", "Author22")))
//                .withNumber("Price", 20)
//                .withString("Dimensions", "8.5x11.0x.75")
//                .withNumber("PageCount", 500)
//                .withBoolean("InPublication", false)
//                .withString("ProductCategory", "Book");
//            table.putItem(item);
//
//            item = new Item()
//                .withPrimaryKey("Id", 121)
//                .withString("Title", "Book 121 Title")
//                .withString("ISBN", "121-1111111111")
//                .withStringSet( "Authors",
//                    new HashSet<String>(Arrays.asList("Author21", "Author 22")))
//                .withNumber("Price", 20)
//                .withString("Dimensions", "8.5x11.0x.75")
//                .withNumber("PageCount", 500)
//                .withBoolean("InPublication", true)
//                .withString("ProductCategory", "Book");
//            table.putItem(item);
//
//        } catch (Exception e) {
//            System.err.println("Create items failed.");
//            System.err.println(e.getMessage());
//        }
	}

}
