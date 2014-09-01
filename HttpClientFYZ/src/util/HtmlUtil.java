package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class HtmlUtil {

private static String testHtml = "<html>  <body>    <h2>      Enter Following Details.    </h2>    <form id=\"myform\" name=\"myform\">      <table>      <tr>          <td>            UserName :          </td>          <td>            <input name=\"username\" type=\"text\" value=\"somevalue\">          </td>        </tr>      </table>    </form>  </body></html>";

public static void main(String[] args) {
    parseHtml(testHtml);
}
public static   Map<String, String> parseHtml(String html) {
    Matcher matcher;
    Map<String, String> parameters;
    // pull out all parameters in the form
    parameters = new HashMap<String, String>();
    matcher = inputPattern.matcher(html);
    while (matcher.find()) {
        Map<String, String> attributes = parseAttributes(matcher.group(1));
        // ignore buttons
        String type = attributes.get("type");
        if (type != null
                && (type.equalsIgnoreCase("submit1") || type
                        .equalsIgnoreCase("button"))) {
            continue;
        }
        String name = attributes.get("name");
        if (name != null) {
            String value = attributes.get("value");
            if (value == null) {
                value = "";
            }
            parameters.put(name, value);
            System.out.println("Key  : "+name+"  , value : "+value);
        }
    }
    return parameters;
}

private static Map<String, String> parseAttributes(String attributesStr) {
    Map<String, String> attributes = new HashMap<String, String>();
    Matcher matcher = attributePattern.matcher(attributesStr);
    while (matcher.find()) {
        String key = matcher.group(1);
        String value = "";
        String g = matcher.group(2).trim();
        if (g != null) {
            value = g;
        }
        //System.out.println(key + " ,  " + value);
        attributes.put(key, value.trim());
    }
    return attributes;
}


public static List<NameValuePair> convertMap(Map<String,String> map)
{
	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
	
	
	Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
    while (it.hasNext()) {
        Map.Entry<String, String> pairs = it.next();
        System.out.println(pairs.getKey() + " = " + pairs.getValue());
    	nameValuePairs.add(new BasicNameValuePair(pairs.getKey(),
    			pairs.getValue()));
        it.remove(); // avoids a ConcurrentModificationException
    }
	

	return nameValuePairs;
}

	public static void setValues(List<NameValuePair> pairs,	Map<String, String> map) {

		for (int i = 0; i < pairs.size(); i++) {
			String name = pairs.get(i).getName();
			if (map.containsKey(name)) {
				pairs.remove(i);
				pairs.add(new BasicNameValuePair(name, map.get(name)));
			}

		}

	}

/**
 * The regex pattern used to find a form element in HTML.
 */
private static final Pattern formPattern = Pattern.compile("<form(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
/**
 * The regex pattern to find a form input parameter in HTML.
 */
private static final Pattern inputPattern = Pattern.compile("<input(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

private static final Pattern attributePattern = Pattern.compile("(\\w+)=\"(.*?)\"");

}