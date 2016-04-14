package edu.upenn.cis455.mapreduce.worker;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.Job;

public class WorkerServlet extends HttpServlet {

  static final long serialVersionUID = 455555002;
  
  String m_master_address;
  int m_port;	// listening port
  String m_storage_dir;
  
  String m_job_name;
  Job m_job;
  String m_status = "idle";
  int m_keys_read;
  int m_keys_written;
  
  BufferedReader m_reader;	// use to read key value pair from input directory
  MapContext m_map_context;
  ReduceContext m_reduce_context;
  Map<String, String> m_worker_address;
  
  boolean m_running = true;
  
  StatusSenderThread m_status_sender_thread;
  
  public static void sop(Object o) {
	  System.out.println(o);
  }
  
  @Override
  public void init(ServletConfig config) {
		String workingDirectory = System.getProperty("user.dir");
		System.out.println("Working Directory = " + workingDirectory);

		m_master_address = config.getInitParameter("master");
	  m_port = Integer.valueOf(config.getInitParameter("port"));
	  m_storage_dir = config.getInitParameter("storagedir");
	  // create a thread to send status to master every 10s
	  m_status_sender_thread = new StatusSenderThread();
	  m_status_sender_thread.start();
  }
  
  @Override
  public void destroy() {
	  m_running = false;
  }

	void printMessage(PrintWriter out, String message) {
		out.println("<!DOCTYPE html><html><body><p>" + message + "</p></body></html>");				
	}

  public void doGet(HttpServletRequest request, HttpServletResponse response) 
       throws java.io.IOException
  {
	  String path = request.getPathInfo();
	  PrintWriter out = response.getWriter();
	  printMessage(out, "hello!");
//      if (path.equals("/runmap")) {
//    	  
//      }
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) 
	       throws java.io.IOException
	  {
		  String path = request.getPathInfo();
		  PrintWriter out = response.getWriter();
	      if (path.equals("/runmap")) {
	    	  sop("worker: runmap");
	    	  // change status
	    	  m_status = "mapping";
	    	  // create the job
	    	  String jobName = request.getParameter("job");
	    	  Class jobClass = null;
	    	  Job job;
	    	  try {
	    		  jobClass = Class.forName(jobName);
	    		  m_job = (Job)jobClass.newInstance();
	    	  } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
	    		  e.printStackTrace();
	    	  }
	    	  m_job_name = jobName;
	    	  // save other workers' address
	    	  m_worker_address = new HashMap<>();
	    	  int workerNumber = Integer.valueOf(request.getParameter("numWorkers"));
	    	  for (int i = 1; i <= workerNumber; i++) {
	    		  String workerName = "worker" + i;
	    		  m_worker_address.put(workerName, request.getParameter(workerName));
	    	  }
	    	  // open input file, context
	    	  try {
	    	      InputStream fis = new FileInputStream(request.getParameter("input"));
	    	      InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
	    		  m_reader = new BufferedReader(isr);
	    	  } catch (IOException e) {  
	    	  }
	    	  m_map_context = new MapContext(workerNumber);
	    	  // create threads to run map
	    	  List<MapThread> mapThreads = new ArrayList<>();
	    	  for (int i = 0; i < Integer.valueOf(request.getParameter("numThreads")); i++) {
	    		  MapThread thread = new MapThread();
	    		  thread.start();
	    		  mapThreads.add(thread);
	    	  }
	    	  // wait till map finish
	    	  for (Thread thread : mapThreads) {
	    		  try {
					thread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    	  }
	    	  // post files to other workers
	    	  m_map_context.finish();
	    	  // status
	    	  m_status = "waiting";
	    	  m_status_sender_thread.sendStatus();
	      } else if (path.equals("/pushdata")) {
	    	  sop("worker: pushdata");
	    	  Files.copy(request.getInputStream(), new File(m_storage_dir + "spool-in/reduce_data").toPath(), StandardCopyOption.REPLACE_EXISTING);
	      } else if (path.equals("/runreduce")) {
	    	  sop("worker: runreduce");
	    	  // change status
	    	  m_status = "reducing";
	    	  // create the job
	    	  String jobName = request.getParameter("job");
	    	  Class jobClass = null;
	    	  Job job;
	    	  try {
	    		  jobClass = Class.forName(jobName);
	    		  m_job = (Job)jobClass.newInstance();
	    	  } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
	    		  e.printStackTrace();
	    	  }
	    	  m_job_name = jobName;
	    	  // sort file
	    	  try {
				Runtime.getRuntime().exec("sort " + m_storage_dir + "spool-in/reduce_data -o " + m_storage_dir + "spool-in/reduce_data").waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    	  // open reduce_data file, and create context
	    	  try {
	    	      InputStream fis = new FileInputStream(m_storage_dir + "spool-in/reduce_data");
	    	      InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
	    		  m_reader = new BufferedReader(isr);
	    	  } catch (IOException e) {  
	    		  e.printStackTrace();
	    	  }
	    	  m_reduce_context = new ReduceContext(request.getParameter("output"));
	    	  // create threads to run reduce
	    	  List<ReduceThread> reduceThreads = new ArrayList<>();
	    	  for (int i = 0; i < Integer.valueOf(request.getParameter("numThreads")); i++) {
	    		  ReduceThread thread = new ReduceThread();
	    		  thread.start();
	    		  reduceThreads.add(thread);
	    	  }
	    	  // wait till map finish
	    	  for (Thread thread : reduceThreads) {
	    		  try {
					thread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    	  }
	    	  // post files to other workers
	    	  m_reduce_context.finish();
	    	  // status
	    	  m_status = "idle";
	    	  m_status_sender_thread.sendStatus();
	      } else if (path.equals("/reset_output")) {
	    	  sop("worker: reset output");
			  Files.deleteIfExists(Paths.get(request.getParameter("output")));
	      }
	  }
    

  
  class StatusSenderThread extends Thread {
	    public void run() {
	    	while (m_running) {
		    	try {
					sendStatus();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		    	try {
					Thread.sleep(10_000);
				} catch (InterruptedException e) {
				}
	    	}
	    }
	    
	    void sendStatus() throws IOException {
	    	String query = "port=" + m_port;
	    	query += "&status=" + m_status;
	    	query += "&job=" + (m_job_name == null ? "" : m_job_name);
	    	query += "&keysRead=" + (m_status.equals("idle") ? 0 : m_keys_read);
	    	query += "&keysWritten=" + (m_status.equals("idle") ? 0 : m_keys_written);
//	    	URL url = new URL("http://requestb.in/1g258dx1?" + query);
	    	  URL url = new URL("http://" + m_master_address + "/workerstatus?" + query);
	      	  HttpURLConnection httpCon = (HttpURLConnection)url.openConnection();
	      	  httpCon.setRequestMethod("GET");
			  httpCon.setRequestProperty("User-Agent", "cis555");
			  httpCon.getResponseCode();
			  httpCon.getResponseMessage();
	    }
	}
  
  class MapContext implements Context {
	  List<PrintWriter> m_writers = new ArrayList<>();
	  BigInteger SHA1_NUMBER = new BigInteger("10000000000000000000000000000000000000000", 16);
	  int m_worker_number;
	  
	  MapContext(int workerNumber) {
		  new File(m_storage_dir + "spool-in").mkdirs();
		  new File(m_storage_dir + "spool-out").mkdirs();
		  m_worker_number = workerNumber;
		  
		  for (int i = 1; i <= workerNumber; i++) {
			  try {
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(m_storage_dir + "spool-out/worker" + i)));
				m_writers.add(out);
			  } catch (IOException e) {
				System.err.println("error: can not create file:" + "worker" + i);
				e.printStackTrace();
			}
		  }
	  }
	  
	  BigInteger sha1(String input) {
		  MessageDigest mDigest;
		  try {
		        mDigest = MessageDigest.getInstance("SHA1");			  			  
		  } catch (NoSuchAlgorithmException e) {
			  System.err.println("error!");
			  e.printStackTrace();
			  return null;
		  }
	        byte[] result = mDigest.digest(input.getBytes());
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < result.length; i++) {
	            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
	        }
	        return new BigInteger(sb.toString(), 16);
	    }
	  
	  public void write(String key, String value) {
		  // calculate sha-1 and get worker number
		  BigInteger hash = sha1(key);
		  int workerIndex = hash.multiply(new BigInteger("" + m_worker_number)).divide(SHA1_NUMBER).intValue();
		  PrintWriter writer = m_writers.get(workerIndex);
		  synchronized(writer) {
			  writer.write(key + "\t" + value + "\n");
		  }
	  }
	  
	  void finish() throws IOException {
		  // post file to other workers
		  for (PrintWriter writer : m_writers) {
			  writer.close();
		  }
		  for (int i = 1; i <= m_worker_number; i++) {
			  String workerName = "worker" + i;
			  String fileName = m_storage_dir + "spool-out/" + workerName;
			  String address = m_worker_address.get(workerName);
			    URL url = new URL("http://" + address + "/pushdata");
			      HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
			      httpCon.setDoOutput(true);
			      httpCon.setRequestMethod("POST");
			      httpCon.setRequestProperty("User-Agent", "cis555");
//			      OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
			      Files.copy(new File(fileName).toPath(), httpCon.getOutputStream());
	        	  httpCon.getResponseCode();
	        	  httpCon.getResponseMessage();

//			      out.close();
//			      System.out.println(httpCon.getResponseCode());
//			      System.out.println(httpCon.getResponseMessage());
		  }
	  }
  }
  
  class MapThread extends Thread {
	  @Override
	  public void run() {
		  String line;
		  while (true) {
			  // read one line.
			  synchronized(m_reader) {
				  try {
					line = m_reader.readLine();
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			  }
			  if (line == null || line.equals("")) {
				  break;
			  }
			  // handle
			  String[] pair = line.split("\t");
			  m_job.map(pair[0], pair[1], m_map_context);
		  }
	  }
  }
  
  class ReduceContext implements Context {
//	  List<PrintWriter> m_writers = new ArrayList<>();
//	  BigInteger SHA1_NUMBER = new BigInteger("10000000000000000000000000000000000000000", 16);
//	  int m_worker_number;
	  PrintWriter m_writer;
	  
	  ReduceContext(String outputDirectory) {
			try {
				m_writer = new PrintWriter(new BufferedWriter(new FileWriter(outputDirectory, true)));
			} catch (IOException e) {
				System.err.println("error in ReduceContext: can not create file:" + outputDirectory);
				e.printStackTrace();
			}
	  }
	  
	  public void write(String key, String value) {
		  synchronized(m_writer) {
			  m_writer.write(key + "\t" + value + "\n");
		  }
	  }
	  
	  void finish() throws IOException {
		  m_writer.close();
	  }
  }
  
  class ReduceThread extends Thread {
	  @Override
	  public void run() {
		  String line = null;
		  String key;
		  List<String> values;
		  String[] pair;
		  while (true) {
			  // read lines with same key
			  synchronized(m_reader) {
				  try {
					line = m_reader.readLine();
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
				  if (line == null) {
					  break;
				  }
				  pair = line.split("\t");
				  key = pair[0];
				  values = new ArrayList<>();
				  values.add(pair[1]);
				  // try to read the lines with same key
				  try {
					  while (true) {
							m_reader.mark(1024);
							line = m_reader.readLine();
							if (line == null) {
								// end of file
								break;
							} else {
								pair = line.split("\t");
								if (pair[0].equals(key)) {
									values.add(pair[1]);
									continue;
								} else {
									m_reader.reset();
									break;
								}
							}						  
					  }
					} catch (IOException e) {
						e.printStackTrace();
					}
			  }
			  // reduce
			  m_job.reduce(key, values.toArray(new String[values.size()]), m_reduce_context);
		  }
	  }

  }
}
  
