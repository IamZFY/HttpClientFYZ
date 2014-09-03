package wb;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class Crawler extends Thread {

	static ArrayList <ArrayList <String>> files = new ArrayList <ArrayList <String>> () ;
	private SimpleDateFormat sdf = new SimpleDateFormat("HH");
	private AbstractHttpClient client = new DefaultHttpClient();
	private HttpPost post = null;
	private ArrayList <String> file = null;
	private String name = null;
	
	Crawler(ArrayList <String> file, String name)
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
			if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".start")) {
				System.out.println("File " + listOfFiles[i].getName());
				oneFile = readFile(listOfFiles[i]);
				files.add(oneFile);
			} else if (listOfFiles[i].isDirectory()) {
				// System.out.println("Directory " + listOfFiles[i].getName());
			}
		}
		
		for (int i = 0; i < files.size(); i++) 
		{
			Thread thread = new Crawler(files.get(i), "" + i);
			thread.start();
		}
		

	}

	public void run() {
		
		String url = file.get(0);

		
		// Get the guy's all posts
		//System.out.println("Get the first URL...");
		String fansPage = getPage(url);
		
		String fansURL = "http://weibo.cn" + findComment("关注\\[.*<a href=\"(.*?)\">粉丝\\[", fansPage);
		
		// Parsing all links of comment
		LinkedHashSet<String> commentUrls = new LinkedHashSet <String>(); // getAllPosts(result, "<a href=\"(http://weibo.cn/comment/.*)\" class=\"cc\">评论");
		
		
		for (int i = 1; i<=20 ; i++)
		{
			//System.out.println("Get the random page URL...");
			String newFansURL = fansURL + "&page=" + i; 
			fansPage = getPage(newFansURL);
			commentUrls.addAll(getAllFanURLs(fansPage, "<a href=\"(.*)\"><img src") );
			//System.out.println(name + ": posts: " + commentUrls.size() );
			
		}
		
		System.out.println("Done");
	}
	
	private ArrayList <String> getAllFanURLs(String input, String pattern ) {
		
		ArrayList <String> comments = new ArrayList<String>();
		String test = input;
		int indexofcomment = 0;
		while ( ( indexofcomment = test.indexOf("<a href=\"http://weibo.cn/")) > 0)
		{
			String oneComment = test.substring(indexofcomment, indexofcomment + 150);
			
			
			String url =  findComment(pattern, oneComment);
			if (url != null)
			{
				url=url.replace("amp;", "");
				System.out.println(url);
			}
			
			test = test.substring(indexofcomment + 150);
		}
		return comments;
	}


	protected String findComment(String pattern, String oneComment) {
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
			return oneComment.replace("amp;", "");
		} else 
		{
			return null;
		}
	}
	
	
	/**
	 * Get the url
	 * @param url
	 * @return
	 */
	public String getPage(String url) {
		System.out.println(name + ":  GET: " + url );
		post = new HttpPost(url);

		try {

			post.setHeader("ContentType","application//x-www-form-urlencoded;charset=UTF-8"); 
			
			HttpResponse response = client.execute(post);
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF8"));

			StringBuffer sb = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				sb.append(line + "\n");
				// System.out.println(line);
			}

			return sb.toString();

		} catch (IOException e) {
			e.printStackTrace();

		}
		return null;
	}
	

	

	
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