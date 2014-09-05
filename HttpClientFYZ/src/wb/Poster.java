package wb;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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

import util.HtmlUtil;
import util.HttpUtil;
import util.StringUtil;

public class Poster extends Thread {

	private static final int HeaderLength = 8;
	static ArrayList <ArrayList <String>> files = new ArrayList <ArrayList <String>> () ;
	private SimpleDateFormat sdf = new SimpleDateFormat("HH");
	private DefaultHttpClient client = new DefaultHttpClient();
	private HttpPost post = null;
	private HttpGet get = null;
	private ArrayList <String> file = null;
	private String name = null;
	Poster(ArrayList <String> file, String name)
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
			Thread thread = new Poster(files.get(i), "" + i);
			thread.start();
		}
		

	}

	public void run()  {
		
	
		try {
		
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
		
		//Login 
		String userName = file.get(6);
		String password = file.get(7);
		
		HttpPost post = null;
		HttpGet get = null;
		// 1 First page
		String loginurl = "https://login.weibo.cn/login/?ns=1&revalid=2&backURL=http%3A%2F%2Fweibo.cn%2F&backTitle=%CE%A2%B2%A9&vt=";
		post = new HttpPost(loginurl);
		HttpResponse response = client.execute(post);
		StringBuffer result = HttpUtil.readResponse(response, "UTF-8");
		// System.out.println(result);

		// 2 Log in via first page
		List<NameValuePair> nameValuePairs = HtmlUtil.parseHtml(result.toString());

		HtmlUtil.setValues(nameValuePairs, "mobile", userName);
		String passwordfieldname = StringUtil.findPattern(	"<input type=\"password\" name=\"(.*)\" size=\"30\" /><br/><input type=\"checkbox\"", result.toString());
		HtmlUtil.setValues(nameValuePairs, passwordfieldname, password);
		
		HtmlUtil.printMap(nameValuePairs);

		post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		response = client.execute(post);
		// System.out.println(response);

		// 3 Redirected to a new GET page
		loginurl = response.getFirstHeader("Location").getValue();
		StringBuffer sb = HttpUtil.readResponse(response, "UTF-8");

		get = new HttpGet(loginurl);
		response = client.execute(get);
		HttpUtil.readResponse(response, "UTF-8").toString();

		// End login
		
		if (mode == 4) // deletion
			{
				String urlOfFirstComment = "";
				while (urlOfFirstComment != null) {
					String allcomments = getPage(url);

					System.out.println("BBB" + allcomments);
					urlOfFirstComment = findPattern(
							"<a href=\"(/comments/del/.*?)\">删除", allcomments);
					if (urlOfFirstComment == null) {
						break;
					}
					System.out.println("AAA" + urlOfFirstComment);
					urlOfFirstComment = urlOfFirstComment.replace("amp;", "");
					System.out.println("Deletion confirmation:"
							+ "http://weibo.cn" + urlOfFirstComment);

					get = new HttpGet("http://weibo.cn" + urlOfFirstComment);
					response = client.execute(get);
					String delConfirmation = HttpUtil.readResponse(response,
							"UTF-8").toString();
					System.out.println("Content" + delConfirmation);
					String urlOfDel = findPattern(
							"<a href=\"(/comments/del/.*?)\">确定",
							delConfirmation);
					urlOfDel = urlOfDel.replace("amp;", "");
					System.out.println("Deletion action:" + "http://weibo.cn"
							+ urlOfDel);
					get = new HttpGet("http://weibo.cn" + urlOfDel);
					response = client.execute(get);
					System.out.println("Del???" + response);
					HttpUtil.readResponse(response, "UTF-8");
					sleep(interval);

				}
				return;
			}
		
		
		// Get the guy's all posts from his homepage
		String specificPost = getPage(url);
		
		String totalPage = findPattern("value=\"跳页\" />&nbsp;1/([0-9]*?)页", specificPost);
		int totalPages = Integer.parseInt(totalPage );
		System.out.println(name + ": Total page" + totalPage);
		
		
		
		// Parsing all links of comment
		ArrayList <String> urlsInOnePage = null; // getAllPosts(result, "<a href=\"(http://weibo.cn/comment/.*)\" class=\"cc\">评论");
		
		
		// Get more pages

		
		for (int i = 0; i<148 ; i++)
		{
			
			// Pick up a random page
			totalPages = totalPages > focus?focus: totalPages;
			int page =   (int) Math.floor (Math.random() * (totalPages-1)) + 1;
			//page = 14;
			
			String newUrl = url.replaceAll("\\?vt=", "\\?page=" + page + "vt");
			System.out.println("A Page in a User: " + newUrl);
			specificPost = getPage(newUrl);
			//String gsid = newUrl.substring(newUrl.indexOf("gsid=")); 
			if (mode == 1 || mode == 3)
			{
				urlsInOnePage = findAllPosts(specificPost, "<a href=\"(http://weibo.cn/comment/.*)\" class=\"cc\">评论");
			}
			else
			{
				urlsInOnePage = findAllReposts(specificPost, "<a href=\"(http://weibo.cn/repost/.*)\">转发", "");
			}
			// System.out.println(name + ": Total posts: " + urlsInOnePage.size() );
			
			
			int size = file.size() - HeaderLength;
			int myindex = (int) Math.floor((Math.random() * size)) + 8;
			
			// Pick up a random post
			int yourindex = (int) Math.floor((Math.random() * (urlsInOnePage.size() ))) ;
			if (yourindex  > urlsInOnePage.size() - 1)
			{
				System.out.println("error" + yourindex + "/" +  urlsInOnePage.size() );
				continue;
			}
			
			// Get the post url
			String urlInOnePage = urlsInOnePage.get(yourindex).replace("amp;", "");
			System.out.println("A URL in a Page: " + urlInOnePage);
			
			specificPost = getPage(urlInOnePage);
			// Avoid posting to other user
			if (mode == 2 && specificPost.contains("转发了"))
			{
				System.out.println("Thread:" + name + ": skip:" + "page [" + page + "]" + ", post [" + yourindex + "]");
				continue;
			}
			
			if (i != 0)
			{
				// Send the first immediatly for quick check result
				sleep(interval);
			}

			
			// Parsing all value pairs
			// System.out.println("Prepare the comment...");
			//List<NameValuePair> 
			nameValuePairs = new ArrayList<NameValuePair>(1);
			String replyURL = null;
			if (mode == 1 || mode == 3)
			{
				replyURL = prepareComments(specificPost, nameValuePairs, file.get(myindex), mode);
			}
			else
			{
				replyURL = prepareRepost(specificPost, nameValuePairs, file.get(myindex));
			}
			
			String timeStamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
			// Post the comment
			if (replyURL != null)
			{
				System.out.println(timeStamp +": Thread:" + name + ": Send comment[" + myindex + "]" + file.get(myindex) + " to page [" + page + "]" + ", post [" + yourindex + "]");
				specificPost = postPage(replyURL, nameValuePairs);
			}
			else
			{
				System.out.println(timeStamp +": Thread:" + name + ": Skip comment[" + myindex + "]" + file.get(myindex) + " to page [" + page + "]" + ", post [" + yourindex + "], original post deleted");
			}
			
		}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		System.out.println("Done");
	}

	protected void sleep(int interval) {
		// Sleep between each send
		try 
		{
			
			String str = sdf.format(new Date());
			int hour = Integer.parseInt(str);
			
			if (hour < 9)
			{
				System.out.println(name + ": sleep mode ~~~"+ str);
				Thread.sleep(1000 * 60 * 60);
			}
			
			long delay =  (long) ( (Math.random() - 0.5) * interval / 5 * 1000); // random 10 %
			//System.out.print(interval* 1000 + delay + ":\t");
			Thread.sleep(interval* 1000 + delay );

			  
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
		

		try {

			//get.setHeader("ContentType","application//x-www-form-urlencoded;charset=UTF-8");
			get.removeHeaders("UserAgent");
			get.setHeader("Accept","text/html, application/xhtml+xml, */*");
			get.setHeader("Accept-Language:","en-AU");
			  
			get.setHeader("UserAgent","Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
 
			
			HttpResponse response = client.execute(get);
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF8"));

			StringBuffer sb = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				sb.append(line + "\n");
				//System.out.println(line);
			}

			return sb.toString();

		} catch (IOException e) {
			e.printStackTrace();

		}
		return null;
	}
	

	/**
	 * HTTP Post method
	 */
	public String postPage(String url, List<NameValuePair> nameValuePairs) {

		//System.out.println(name + ": POST: " + url );
		post = new HttpPost(url);

		try {

			post.setHeader("ContentType","application//x-www-form-urlencoded;charset=UTF-8"); 
			UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nameValuePairs,"UTF-8" );
			
			post.setEntity(urlEncodedFormEntity);
			
			
			HttpResponse response = client.execute(post);
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF8"));

			StringBuffer sb = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) 
			{
				sb.append(line + "\n");
				//System.out.println(line);
			}

			return sb.toString();

		} catch (IOException e) {
			e.printStackTrace();

		}
		return null;
	}
	
	
	/**
	 * Parsing, get the comments
	 * @param summary
	 * @param pattern
	 * @return
	 */
	private ArrayList <String> findAllPosts(String summary, String pattern ) {
		
		ArrayList <String> comments = new ArrayList<String>();
		//System.out.println("---- "+ summary);
		
		String test = summary;
		int indexofcomment = 0;
		while ( ( indexofcomment = test.indexOf("<a href=\"http://weibo.cn/comment/")) > 0)
		{
			String oneComment = test.substring(indexofcomment, indexofcomment + 200);
			
			// System.out.println(oneComment);
			String url =  findPattern(pattern, oneComment);
			if (url != null)
			{
				url = url.replace("amp;", "");
				// System.out.println("..." + url);
				comments.add(url);
			}
			
			
			test = test.substring(indexofcomment + 200);
		}
		return comments;
	}

	
	/**
	 * Parsing, get the comments
	 * @param summary
	 * @param pattern
	 * @return
	 */
	private ArrayList <String> findAllReposts(String summary, String pattern, String gsid ) {
		
		ArrayList <String> comments = new ArrayList<String>();
		//System.out.println("---- "+ summary);
		
		String test = summary;
		int indexofcomment = 0;
		while ( ( indexofcomment = test.indexOf("<a href=\"http://weibo.cn/repost/")) > 0)
		{
			String oneComment = test.substring(indexofcomment, indexofcomment + 200);
			
			//System.out.println(oneComment);
			String url =  findPattern(pattern, oneComment);
			if (url != null)
			{
				url = url.replace("amp;", "");
				//System.out.println("..." + url);
				comments.add(url+"&" + gsid);
				comments.add(url);
			}
			
			
			test = test.substring(indexofcomment + 200);
		}
		return comments;
	}
	
	
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
	
	
	/**
	 * Parsing
	 */
	private String prepareComments(String form, List<NameValuePair> nameValuePairs, String commentText, int mode) {

		String url = findPattern("<form action=\"(.*?)\" method=\"post\"><div>    评论只显示",	form);
		//.out.println("######" + url);
		String srcuid = findPattern("<input type=\"hidden\" name=\"srcuid\" value=\"(.*?)\" />", form);
		//System.out.println("######" + srcuid);
		String id = findPattern("<input type=\"hidden\" name=\"id\" value=\"(.*?)\" />", form);
		//System.out.println("######" + id);
		String rl = findPattern("<input type=\"hidden\" name=\"rl\" value=\"(.*?)\" />", form);
		//System.out.println("######" + rl);

		System.out.println(name + ": " + commentText);
		if (url == null)
		{
			return null;
		}
			
		url = "http://weibo.cn" + url.replaceAll("amp;", "");

		nameValuePairs.add(new BasicNameValuePair("srcuid", srcuid));
		nameValuePairs.add(new BasicNameValuePair("id", id));
		nameValuePairs.add(new BasicNameValuePair("rl", rl));
		nameValuePairs.add(new BasicNameValuePair("content", commentText));

		if (mode == 1)
		{
			nameValuePairs.add(new BasicNameValuePair("rt", "评论并转发"));
		}
		else if (mode == 3)
		{
			HtmlUtil.setValues(nameValuePairs, "submit", "评论");
		}
		
		
		System.out.println("the post sending" + ": " + url);
		
		return url;
	}
	
	/**
	 * Parsing
	 */
	private String prepareRepost(String form, List<NameValuePair> nameValuePairs, String commentText) {

		String url = findPattern("<form action=\"(.*?)\" method=\"post\" id=\"mblogform\">",	form);
		//.out.println("######" + url);
		String act = findPattern("<input type=\"hidden\" name=\"act\" value=\"(.*?)\" />", form);
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

		nameValuePairs.add(new BasicNameValuePair("act", act));
		nameValuePairs.add(new BasicNameValuePair("id", id));
		nameValuePairs.add(new BasicNameValuePair("rl", rl));
		nameValuePairs.add(new BasicNameValuePair("content", commentText));
		
		nameValuePairs.add(new BasicNameValuePair("rt", "评论并转发"));

		return url;
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
