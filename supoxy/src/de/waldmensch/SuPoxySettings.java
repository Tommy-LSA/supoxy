package de.waldmensch;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;


public class SuPoxySettings {

	/* Default Values Begin*/
	public static Integer requestinterval_default = 5;
	public static Integer httpport_default = 8000;
	public static Integer cachesize_default = 500;
	/* Default Values End*/

	public static Boolean configOK;

	static Properties properties = new Properties();
	public static String sunnyuser;
	public static String sunnypassword;
	public static Integer requestinterval;
	public static Integer httpport;
	public static Integer cachesize;

	public static void LoadConfig(String configpath){

		FileInputStream fis;

		try {
			fis = new FileInputStream(configpath);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			configOK = false;
			return;
		}

		BufferedInputStream stream = new BufferedInputStream(fis);
		try {
			properties.load(stream);
			stream.close();

			sunnyuser = properties.getProperty("sunnyuser");
			sunnypassword = properties.getProperty("sunnypassword");
			requestinterval = SuPoxyUtils.IntParser("requestinterval",properties.getProperty("requestinterval"), requestinterval_default);
			httpport = SuPoxyUtils.IntParser("httpport", properties.getProperty("httpport"), httpport_default);
			cachesize = SuPoxyUtils.IntParser("cachesize", properties.getProperty("cachesize"), cachesize_default);

		} catch (IOException e) {
			e.printStackTrace();
			configOK = false;
			return;
		}

		// no less than 5!
		requestinterval = (requestinterval < 5) ? requestinterval : 5;

		// without user/pw we are not able to get data
		configOK = (sunnyuser.length() > 0 && sunnypassword.length() > 0) ? true : false;

	}

}
