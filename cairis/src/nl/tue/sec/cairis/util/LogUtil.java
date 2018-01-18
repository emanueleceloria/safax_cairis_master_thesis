package nl.tue.sec.cairis.util;

import nl.tue.sec.cairis.db.DBFns;
import nl.tue.sec.cairis.util.CairisUtil;

public class LogUtil {
	 private static String component="nl.tue.sec.cairis";
	 /*
		 * log level
		 * 3 Network Messaging
		 * 2 Internal messages to the component
		 * 1 Interface level
		 * 0 Every other
		 * -1 Info
		 * -2 Warning
		 * -3 Error
		 * -4 Super Critical Errors
		 * 
		 */
//		public static void log(String transactionid, String message,int level){
//			   if(transactionid!=null)
//				   DBFns.log(transactionid, message,level,component);
//			   else
//				  errorlog("null", message,"",-2);
//		}
		/*
			 * error log level
			 * 4 Invalid Errors
			 * 3 Network Errors
			 * 2 DB errors
			 * 1 Parsing errors
			 * 0 Every other
			 * 
		*/
		public static void errorlog(String transactionid,String header, String message, int level){
			   if(transactionid==null || transactionid.length()<1)
				   transactionid="null";
			   DBFns.errorlog(transactionid,header,message,level,component);
		}
		
		   public static void writeLog(String transactionid, String message,int level){
			   CairisUtil.writeLog(transactionid, level, message);
		   }
}
