package edu.upenn.cis455.mapreduce.job;

import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.Job;

public class WordCount implements Job {

  public void map(String key, String value, Context context)
  {
	  // key: line number
	  // value: line
	  String[] words = value.split(" +");
	  for (String word : words) {
		  context.write(word, "" + 1);
	  }
  }
  
  public void reduce(String key, String[] values, Context context)
  {
	  int sum = 0;
	  for (String value : values) {
		  sum += Integer.valueOf(value);
	  }
	  context.write(key, "" + sum);
  }
  
}
