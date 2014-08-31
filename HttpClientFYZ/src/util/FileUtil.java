package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class FileUtil {
	
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
