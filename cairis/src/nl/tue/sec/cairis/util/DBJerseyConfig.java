package nl.tue.sec.cairis.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
 

public class DBJerseyConfig {
	String result = "";
	InputStream inputStream;
	
	String DBHOST = "";
	String DBPORT = "";
	String DB = "";
	String DBUSER = "";
	String DBPWD = "";

	
	public String getDBHost () {
		return DBHOST;
	}
	
	public String getDBPort () {
		return DBPORT;
	}
	
	public String getDB () {
		return DB;
	}
	
	public String getDBUser () {
		return DBUSER;
	}
	
	public String getDBPassword () {
		return DBPWD;
	}
 
	public String getPropValues() throws IOException {
 
		try {
			Properties prop = new Properties();
			String propFileName = "config.db.properties";
 
			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
 
			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
 
			Date time = new Date(System.currentTimeMillis());
 
			// get the property value and print it out
			DBHOST = prop.getProperty("db_host");
			DBPORT = prop.getProperty("db_port");
			DB = prop.getProperty("db");
			DBUSER = prop.getProperty("db_user");
			DBPWD = prop.getProperty("db_password");

			result="DBHOST: "+ DBHOST+"\nDBPORT: "+DBPORT+"\nDB: "+DB+"\nDBUSER: "
					+ DBUSER+"\nDBPWD: "+DBPWD;
			
			System.out.println(result);
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
			inputStream.close();
		}
		return result;
	}
}
