package wb;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class WeiboProxyLogin extends Thread {

	//http://weibo.cn/?gsid=4uHw63051JizpEpp5wIZhl6Oi1z
	static ArrayList <ArrayList <String>> files = new ArrayList <ArrayList <String>> () ;
	private SimpleDateFormat sdf = new SimpleDateFormat("HH");
	private DefaultHttpClient client = new DefaultHttpClient();
	private HttpPost post = null;
	private HttpGet get = null;
	private ArrayList <String> file = null;
	private String name = null;
	
	private static final char[] symbols = new char[62];
	  static {
		    for (int idx = 0; idx < 10; ++idx)
		      symbols[idx] = (char) ('0' + idx);
		    for (int idx = 10; idx < 36; ++idx)
		      symbols[idx] = (char) ('a' + idx - 10);
		    for (int idx = 36; idx < 62; ++idx)
			      symbols[idx] = (char) ('A' + idx - 36);
		  }
	
	  
	WeiboProxyLogin(ArrayList <String> file, String name)
	{
		this.file = file;
		this.name = name;
	}
	
	public static void main(String[] args) 
	{
		File folder = new File(".");
		File[] listOfFiles = folder.listFiles();
		ArrayList <String> oneFile = null;
		
		
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".txt")) {
				System.out.println("File " + listOfFiles[i].getName());
				oneFile = readFile(listOfFiles[i]);
				files.add(oneFile);
			} 
		}
		
		for (int i = 0; i < files.size(); i++) 
		{
			Thread thread = new WeiboProxyLogin(files.get(i), "" + i);
			thread.start();
		}
		

	}
	

	public String getRandomGSID(int length)
	{
		//System.out.println((char) 1 );
		Random random = new Random();
		char[] buf = new char[length];
		for (int idx = 0; idx < length; ++idx)
		{
		      buf[idx] = symbols[random.nextInt(symbols.length)];
		}
		buf[0] = '4';
		buf[1] = 'u';
		buf[4] = '6';
		buf[5] = '3';
		buf[6] = '0';
		buf[7] = '5';
		buf[8] = '1';
		return new String(buf);
		

	}
	
	//   4uUp63051MvNotPNF3sacl5Mq8P
	//   4uVR630516RVoFBvW6Ak0l53Gd1
	//   4umE63051sbMWIq1lyAdNgtyEa0
	//   4uea63051ofBIBIF8IJR8dn0VXS
	public void run() {
		
	
		
		String url = file.get(0);
		int interval = Integer.parseInt(file.get(1) );
		if (interval < 60)
		{
			// interval = 60;
		}
		int focus = Integer.parseInt(file.get(2) );
		if (focus == 0) 
		{
			focus = 1;
		}
		String proxyIP = file.get(3);
		if (proxyIP != null && proxyIP.length() > 6) 
		{
			int proxyPort = Integer.parseInt(file.get(4));
			HttpHost proxy = new HttpHost(proxyIP, proxyPort, "http");
			System.out.println(name +": Apply proxy for security: " + proxyIP + ":" + proxyPort);
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}
		int mode = Integer.parseInt(file.get(5) );
		
		
		// Get the guy's all posts
		for (int i = 0; i < 5000; i++) {
			String s = getRandomGSID(27);
			url = "http://weibo.cn/?gsid=" + s;
			//url = "http://weibo.cn/?vt=4&gsid=4uHw63051JizpEpp5wIZhl6Oi1z";
			String result = getPage(url);
			sleep(10);
			System.out.print(".");
			if ( ( result.length() < 100) || (result.length() > 5300 && result.length() < 5323) || (result.length() > 7000 && result.length() < 8300)  )
			{
				
			}
			else
			{
				System.out.println(result);
				System.out.println(url);
				System.out.println(result.length());
			}
			
		}
		
		System.out.println("Done");
	}

	protected void sleep(int interval) {
		// Sleep between each send
		try 
		{
			Thread.sleep(interval );
			  
		} catch (InterruptedException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * HTTP Get method
	 */
	public String getPage(String url) {
		//System.out.println(name + ":  GET: " + url );
		get = new HttpGet(url);
		
		for (int i = 0; i < 10; i++) 
		{

			try {

				// get.setHeader("ContentType","application//x-www-form-urlencoded;charset=UTF-8");
				get.removeHeaders("UserAgent");
				get.setHeader("Accept", "text/html, application/xhtml+xml, */*");
				get.setHeader("Accept-Language:", "en-AU");

				get.setHeader("UserAgent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");

				HttpResponse response = client.execute(get);
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent(), "UTF8"));

				StringBuffer sb = new StringBuffer();
				String line = "";
				while ((line = rd.readLine()) != null) {
					sb.append(line + "\n");
					// System.out.println(line);
				}
				// Good return;
				return sb.toString();

			} catch (IOException e) {
				
				System.out.println(e.getMessage());
				System.out.println(url);
				// e.printStackTrace();
				// bad try again
				continue;
			}

		}

		return null;
	}
	

	
	/**
	 * File read method
	 * @param fileName
	 * @return
	 */
	public static ArrayList <String> readFile(File fileName) {
		ArrayList<String> result  = new ArrayList<String>();
		try {
			
			// if file doesnt exists, then create it
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileName), "UTF8"));
			String line;
			while ((line = in.readLine()) != null) {
				result.add(line);
				//System.out.println(line);
			}
			//System.out.println(line = in.readLine());
			

		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}


}