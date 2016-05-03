/* SimpleApp.java */
import org.apache.spark.api.java.*;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;

import scala.Tuple2;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
/* SimpleApp.java */
import org.apache.spark.api.java.*;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function;

public final class SimpleApp {
  private static final Pattern SPACE = Pattern.compile(" ");

  public static void main(String[] args) throws Exception {
	  System.out.println("Hello World");
//	    String logFile = "input"; // Should be some file on your system
//	    SparkConf conf = new SparkConf().setAppName("Simple Application");
//	    JavaSparkContext sc = new JavaSparkContext(conf);
//	    JavaRDD<String> logData = sc.textFile(logFile).cache();
//
//	    long numAs = logData.filter(new Function<String, Boolean>() {
//	      public Boolean call(String s) { return s.contains("a"); }
//	    }).count();
//
//	    long numBs = logData.filter(new Function<String, Boolean>() {
//	      public Boolean call(String s) { return s.contains("b"); }
//	    }).count();
//
//	    System.out.println("Lines with a: " + numAs + ", lines with b: " + numBs);
	  
	  
//
//    SparkConf sparkConf = new SparkConf().setAppName("JavaWordCount");
//    JavaSparkContext ctx = new JavaSparkContext(sparkConf);
//    JavaRDD<String> lines = ctx.textFile("https://s3.amazonaws.com/spark-cis455/input", 1);
//
//    JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
//      @Override
//      public Iterable<String> call(String s) {
//        return Arrays.asList(SPACE.split(s));
//      }
//    });
//
//    JavaPairRDD<String, Integer> ones = words.mapToPair(
//      new PairFunction<String, String, Integer>() {
//        @Override
//        public Tuple2<String, Integer> call(String s) {
//          return new Tuple2<>(s, 1);
//        }
//      });
//
//    JavaPairRDD<String, Integer> counts = ones.reduceByKey(
//      new Function2<Integer, Integer, Integer>() {
//        @Override
//        public Integer call(Integer i1, Integer i2) {
//          return i1 + i2;
//        }
//      });
//
//    List<Tuple2<String, Integer>> output = counts.collect();
//    for (Tuple2<?,?> tuple : output) {
//      System.out.println(tuple._1() + ": " + tuple._2());
//    }
//    ctx.stop();
  }
}


