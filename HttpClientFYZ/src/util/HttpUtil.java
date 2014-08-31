package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class HttpUtil {

	/**
	 * HTTP Get method
	 */
	public static String getPage(DefaultHttpClient httpClient, String url, String charset) {
		HttpGet get = new HttpGet(url);

		try {

			// get.setHeader("ContentType","application//x-www-form-urlencoded;charset=UTF-8");
			get.removeHeaders("UserAgent");
			get.setHeader("Accept", "text/html, application/xhtml+xml, */*");
			get.setHeader("Accept-Language:", "en-AU");
			get.setHeader("UserAgent", "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");

			HttpResponse response = httpClient.execute(get);
			StringBuffer sb = readResponse(response, charset);

			return sb.toString();

		} catch (IOException e) {
			e.printStackTrace();

		}
		return null;
	}

	public static StringBuffer readResponse(HttpResponse response, String charset) throws UnsupportedEncodingException, IOException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(response
				.getEntity().getContent(), charset));

		StringBuffer sb = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			sb.append(line + "\n");
		}

		return sb;
	}

	
}
