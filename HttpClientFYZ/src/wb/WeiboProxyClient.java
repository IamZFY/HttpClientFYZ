package wb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import util.FileUtil;

/**
 * Test commit from different place
 * Test commit from web
 * Test conflict check in
 * @author Fengyuan
 *
 */
public class WeiboProxyClient extends Thread {


	static ArrayList <ArrayList <String>> files = new ArrayList <ArrayList <String>> () ;
	private SimpleDateFormat sdf = new SimpleDateFormat("HH");
	private DefaultHttpClient client = new DefaultHttpClient();
	private HttpPost post = null;
	private HttpGet get = null;
	private ArrayList <String> file = null;
	private String name = null;
	
	String userName = null;
	String password = null;
	String comment = null;

	
	
	WeiboProxyClient(ArrayList <String> file, String name)
	{
//		HttpParams my_httpParams = new BasicHttpParams();
//		HttpConnectionParams.setConnectionTimeout(my_httpParams, 240000);
//		HttpConnectionParams.setSoTimeout(my_httpParams, 240000);
//		DefaultHttpClient defaultHttpClient = new DefaultHttpClient(my_httpParams);
//		client = defaultHttpClient;
		
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
				oneFile = FileUtil.readFile(listOfFiles[i]);
				files.add(oneFile);
			} 
		}
		
		for (int i = 0; i < files.size(); i++) 
		{
			Thread thread = new WeiboProxyClient(files.get(i), "" + i);
			thread.start();
		}
		
//			Thread thread = new WeiboProxyClient();
//			thread.start();

	}

	public void run() 
	{
		
		
		 userName = file.get(0);
		 password = file.get(1);
		 comment = file.get(2);

		
		getPage();

	}


	
	public String getPage() 
	{
		try {

			
			String url1="http://login.weibo.cn/login/?ns=1&revalid=2&backURL=http%3A%2F%2Fweibo.cn%2F&backTitle=%CE%A2%B2%A9&vt=";
			post = new HttpPost(url1);

			
			HttpResponse response = client.execute(post);
			StringBuffer result = printResponse(response);
			
			String vk= findPattern("name=\"vk\" value=\"(.*)\" /><input type=\"submit\"", result.toString());
			String passwordfieldname= findPattern("<input type=\"password\" name=\"(.*)\" size=\"30\" /><br/><input type=\"checkbox\"",result.toString());
			
			
			System.out.println("aa" + vk + "pd" + passwordfieldname);
			
			
			//post.setURI(new URI(url));
			

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("mobile",
					userName));
			nameValuePairs.add(new BasicNameValuePair(passwordfieldname,
					password));
			nameValuePairs.add(new BasicNameValuePair("remember",
					"on"));

//			nameValuePairs.add(new BasicNameValuePair("backURL",
//					"http%253A%252F%252Fweibo.cn%252F"));
			
			nameValuePairs.add(new BasicNameValuePair("backURL",
					"http%3A%2F%2Fweibo.cn%2F"));
//			nameValuePairs.add(new BasicNameValuePair("backTitle",
//					"%E5%BE%AE%E5%8D%9A"));
			nameValuePairs.add(new BasicNameValuePair("backTitle",
					"微博"));
			
			nameValuePairs.add(new BasicNameValuePair("tryCount",
					""));

			nameValuePairs.add(new BasicNameValuePair("vk",
					vk));

//			nameValuePairs.add(new BasicNameValuePair("submit",
//					"%E7%99%BB%E5%BD%95"));
			nameValuePairs.add(new BasicNameValuePair("submit",
					"登录"));
			



			
			
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			response = client.execute(post);
			String neurl = response.getFirstHeader("Location").getValue();
			System.out.println("new url:" + neurl);
			StringBuffer sb = printResponse(response);
			
			HttpGet request2 = new HttpGet(neurl);
			  HttpResponse response2 = client.execute(request2);
			  printResponse(response2);
			
			  HttpGet request3 = new HttpGet("http://weibo.cn/comment/Bj9xmb8Ea?uid=5031070097&rl=0&gid=10001#cmtfrm");
			  HttpResponse response3 = client.execute(request3);
			  StringBuffer onePost  = printResponse(response3);
			  
			  
				//List<NameValuePair> newnameValuePairs = new ArrayList<NameValuePair>(1);
				String replyURL = null;

			
			// System.out.println(comment);
			String question = "";
			while (!"再见".equals(question) )
			{
			System.out.print("我说:");
			Scanner input = new Scanner(System.in);
			
			// System.out.println(input.nextLine());
//			comment.
			question = input.nextLine();
			replyURL = prepareComments(onePost.toString(), nameValuePairs, "@小冰 " +question);
			  
			//System.out.println(replyURL);
			post = new HttpPost(replyURL);  
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs,"UTF-8"));
			
			  //HttpGet request4 = new HttpGet(replyURL);
			  HttpResponse response4 = client.execute(post); 
			  printResponse(response4);
			
			  try{
				neurl = response4.getFirstHeader("Location").getValue();
			  }
			  catch (Exception e)
			  {
				  System.out.println("电脑:" + "拒绝回答！");
			  }
			  
				//System.out.println("new url:" + neurl);
				//StringBuffer sb = printResponse(response);
				boolean answered = false;
				for (int i = 0; i< 5; i++)
				{
					
				
				Thread.sleep(2000);
				
				  HttpGet request5 = new HttpGet(neurl);
				  HttpResponse response5 = client.execute(request5);
				  result = printResponse(response5);
				  
				  String reply= findPattern("@文明礼貌上网</a>:(.*?)</span>.*举报</a>.*"+question, result.toString());
				if (reply != null && reply.length() > 0)
				{
					System.out.println("电脑:" + reply);
					answered = true;
					break;
				}
				else
				{
					System.out.print(".");
				}
				
				}
				if (!answered)
				{
					System.out.println("电脑:" + "拒绝回答！");
				}
			}
				
			return sb.toString();

		} catch (Exception e) {
			e.printStackTrace();

		}
		return null;
	}

	/**
	 * Parsing
	 */
	private String prepareComments(String form, List<NameValuePair> nameValuePairs, String commentText) {

		//String url = findPattern("<form action=\"(.*?)\" method=\"post\"><div>评论",	form);
		String url = findPattern("<form action=\"(.*?)\" method=\"post\"><div>    评论",	form);

		//System.out.println("######" + url);
		String srcuid = findPattern("<input type=\"hidden\" name=\"srcuid\" value=\"(.*?)\" />", form);
		//System.out.println("######" + srcuid);
		String id = findPattern("<input type=\"hidden\" name=\"id\" value=\"(.*?)\" />", form);
		//System.out.println("######" + id);
		String rl = findPattern("<input type=\"hidden\" name=\"rl\" value=\"(.*?)\" />", form);
		//System.out.println("######" + rl);

		// System.out.println(name + ": " + commentText);
		if (url == null)
		{
			return null;
		}
			
		url = "http://weibo.cn" + url.replaceAll("amp;", "");

		nameValuePairs.add(new BasicNameValuePair("srcuid", srcuid));
		nameValuePairs.add(new BasicNameValuePair("id", id));
		nameValuePairs.add(new BasicNameValuePair("rl", rl));
		nameValuePairs.add(new BasicNameValuePair("content", commentText));
		nameValuePairs.add(new BasicNameValuePair("rt", "评论并转发"));

		return url;
	}
	
	
	
	protected StringBuffer printResponse(HttpResponse response)
			throws UnsupportedEncodingException, IOException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				response.getEntity().getContent(), "UTF8"));

		StringBuffer sb = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			sb.append(line + "\n");
		}
		
		//System.out.println("---------------1-----" + sb);
		
		return sb;
	}
	
//	/**
//	 * HTTP Get method
//	 */
//	public String getPage(String url) {
//		//System.out.println(name + ":  GET: " + url );
//		get = new HttpGet(url);
//		
//
//		try {
//
//			//get.setHeader("ContentType","application//x-www-form-urlencoded;charset=UTF-8");
//			get.removeHeaders("UserAgent");
//			get.setHeader("Accept","text/html, application/xhtml+xml, */*");
//			get.setHeader("Accept-Language:","en-AU");
//			  
//			get.setHeader("UserAgent","Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
// 
//			
//			HttpResponse response = client.execute(get);
//			StringBuffer sb = printResponse(response);
//
//			return sb.toString();
//
//		} catch (IOException e) {
//			e.printStackTrace();
//
//		}
//		return null;
//	}
	
	/**
	 * Parsing
	 */
	private String findPattern(String pattern, String oneComment) {
		Pattern p;
		p = Pattern.compile(pattern, Pattern.DOTALL | Pattern.MULTILINE);
		Matcher m = p.matcher(oneComment);
		boolean found = false;
		
		while (m.find()) {
			oneComment = m.group(1);
			found = true;
		}
		
		if (found) 
		{
			return oneComment;
		} else 
		{
			return null;
		}
	}
	
	

}