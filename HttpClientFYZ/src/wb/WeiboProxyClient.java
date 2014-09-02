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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import util.HtmlUtil;
import util.HttpUtil;
import util.StringUtil;

/**
 * Test commit from different place Test commit from web Test conflict check in
 * 
 * @author CarlZ
 * 
 */
public class WeiboProxyClient extends Thread {

	static ArrayList<ArrayList<String>> files = new ArrayList<ArrayList<String>>();
	private DefaultHttpClient client = new DefaultHttpClient();
	private ArrayList<String> file = null;

	String userName = null;
	String password = null;

	WeiboProxyClient(ArrayList<String> file, String name) {
		this.file = file;
	}

	public static void main(String[] args) {

		File folder = new File(".");
		File[] listOfFiles = folder.listFiles();
		ArrayList<String> oneFile = null;

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()
					&& listOfFiles[i].getName().endsWith(".txt")) {
				System.out.println("File " + listOfFiles[i].getName());
				oneFile = FileUtil.readFile(listOfFiles[i]);
				files.add(oneFile);
			}
		}

		for (int i = 0; i < files.size(); i++) {
			Thread thread = new WeiboProxyClient(files.get(i), "" + i);
			thread.start();
		}

	}

	public void run() {

		userName = file.get(0);
		password = file.get(1);

		connectAndChat();

	}

	public void connectAndChat() {
		try {
			HttpPost post = null;
			HttpGet get = null;
			// 1 First page
			String url = "https://login.weibo.cn/login/?ns=1&revalid=2&backURL=http%3A%2F%2Fweibo.cn%2F&backTitle=%CE%A2%B2%A9&vt=";
			post = new HttpPost(url);
			HttpResponse response = client.execute(post);
			StringBuffer result = HttpUtil.readResponse(response, "UTF-8");
			// System.out.println(result);

			// 2 Log in via first page
			List<NameValuePair> nameValuePairs = HtmlUtil.parseHtml(result.toString());

			HtmlUtil.setValues(nameValuePairs, "mobile", userName);
			String passwordfieldname = StringUtil.findPattern(	"<input type=\"password\" name=\"(.*)\" size=\"30\" /><br/><input type=\"checkbox\"", result.toString());
			HtmlUtil.setValues(nameValuePairs, passwordfieldname, password);
			
			System.out.println("-----------------------");
			HtmlUtil.printMap(nameValuePairs);

			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			response = client.execute(post);
			// System.out.println(response);

			// 3 Redirected to a new GET page
			url = response.getFirstHeader("Location").getValue();
			StringBuffer sb = HttpUtil.readResponse(response, "UTF-8");

			get = new HttpGet(url);
			response = client.execute(get);
			HttpUtil.readResponse(response, "UTF-8").toString();

			// 4 One specific post page
			get = new HttpGet("http://weibo.cn/comment/BaCMt3JRa?uid=5031070097&rl=1#cmtfrm");
							
			response = client.execute(get);
			StringBuffer onePost = HttpUtil.readResponse(response, "UTF-8");

			String replyURL = null;
			String question = "";
			while (!"再见".equals(question)) {
				
				// Read from console and post
				System.out.print("我说:");
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "UTF8"));
				question = reader.readLine();
				replyURL = prepareComments(onePost.toString(), nameValuePairs,	"@小冰 " + question);

				post = new HttpPost(replyURL);
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
				response = client.execute(post);
				HttpUtil.readResponse(response, "UTF-8");

				// Get the returned page via redirection again
				try {
					url = response.getFirstHeader("Location").getValue();
				} catch (Exception e) {
					System.out.println("电脑:" + "拒绝回答！");
				}

				boolean answered = false;
				for (int i = 0; i < 5; i++) {

					Thread.sleep(2000);
					get = new HttpGet(url);
					HttpResponse response5 = client.execute(get);
					result = HttpUtil.readResponse(response5, "UTF-8");

					String reply = StringUtil.findPattern("@文明礼貌上网</a>:(.*?)</span>.*举报</a>.*" + question, result.toString());
					if (reply != null && reply.length() > 0) {
						System.out.println("电脑:" + reply);
						answered = true;
						break;
					} else {
						//System.out.print(".");
					}

				}
				if (!answered) {
					System.out.println("电脑:" + "拒绝回答！");
				}
			}

			

		} catch (Exception e) {
			e.printStackTrace();

		}

	}


	private String prepareComments(String form,	List<NameValuePair> nameValuePairs, String commentText) {

		nameValuePairs.addAll(HtmlUtil.parseHtml(form.toString()));
		
		String url = StringUtil.findPattern("<form action=\"(.*?)\" method=\"post\"><div>    评论", form);
		if (url == null) {
			return null;
		}

		url = "http://weibo.cn" + url.replaceAll("amp;", "");

		nameValuePairs.add(new BasicNameValuePair("content", commentText));

		HtmlUtil.setValues(nameValuePairs, "content", commentText);
		HtmlUtil.setValues(nameValuePairs, "submit", "评论");
		HtmlUtil.setValues(nameValuePairs, "rt", null);
		

		return url;
	}

}