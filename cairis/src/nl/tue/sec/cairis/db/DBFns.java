package nl.tue.sec.cairis.db;

import java.util.ArrayList;

import nl.tue.sec.cairis.db.DBAbstraction;
import nl.tue.sec.cairis.util.DataUtil;


public class DBFns {
	
//	public static int log(String transactionID, String logMsg,int level,String component){
//		String query="INSERT INTO ext_log(transactionid,log,logtype,utimestamp,component) VALUES(?,?,?,?,?)";
//		long tstamp= System.currentTimeMillis() / 1000L;
//		return DBAbstraction.insertStatement(query, DataUtil.convertToList(transactionID,
//				logMsg,Integer.toString(level),Long.toString(tstamp),component));
//	}
	public static int errorlog(String transactionID, String logHead,String logMsg,int level,String component){
		String query="INSERT INTO ext_errorlog(transactionid,errorheader,errorlog,logtype,utimestamp,component) "
					+ "VALUES(?,?,?,?,?,?)";
		long tstamp= System.currentTimeMillis() / 1000L;
		return DBAbstraction.insertStatement(query, DataUtil.convertToList(transactionID,logHead,
									logMsg,Integer.toString(level),Long.toString(tstamp),component));
	}

	public static String getDemoIDfromTransactionID(String transactionID){
		String query="SELECT b.demoid FROM sfx_transaction a, sfx_pdp b WHERE a.pdpcode=b.pdpcode AND a.transactionid=?";
		String demoID = DBAbstraction.selectRecord(query, DataUtil.convertToList(transactionID));

		return demoID;
	}
	
	public static ArrayList<String> getCAIRISCredentials(String demoID){
		ArrayList<String> creds= new ArrayList<String>();
		String query = "SELECT ccid, cairisuname, cairispwd FROM sfx_cairis_credentials WHERE demoid=?";
		creds=DBAbstraction.selectColumns(query, DataUtil.convertToList(demoID));
		return creds;
	}
	
	public static String getCAIRISDB(String ccid){
		String query = "SELECT dbname FROM sfx_cairis_db WHERE ccid=? AND isactive=1";
		String db=DBAbstraction.selectRecord(query,DataUtil.convertToList(ccid));
		return db;
	}
	
/*
 * The following are methods that can be used to perform a test for CairisService
 *  module against a local DB called 'sfx_cairis_risks' if for example the Cairis' APIs
 *  are not still ready or CAIRIS service at demo.cairis.org is down.  
 * 
 */
	public static String getHighestRiskValuefromResource(String resource){
		String query="SELECT MAX(riskvalue) FROM safax.sfx_cairis_risks WHERE resourcename=?";
		return DBAbstraction.selectRecord(query, DataUtil.convertToList(resource));

	}
	
	public static String getHighestRiskValuefromResourceGivenThreat(String resource, String threat){
		String query="SELECT MAX(riskvalue) FROM safax.sfx_cairis_risks WHERE resourcename=? and threatname= ?";
		return DBAbstraction.selectRecord(query, DataUtil.convertToList(resource, threat));

	}
	
	public static String getHighestRiskValuefromResourceGivenEnvironment(String resource, String environment){
		String query="SELECT MAX(riskvalue) FROM safax.sfx_cairis_risks WHERE resourcename=? and environmentname=?";
		return DBAbstraction.selectRecord(query, DataUtil.convertToList(resource, environment));

	}
	
	public static String getHighestRiskValuefromResourceGivenThreatandEnvironment(String resource, String threat, String environment){
		String query="SELECT MAX(riskvalue) FROM safax.sfx_cairis_risks WHERE resourcename=? and threatname=? and environmentname=?";
		return DBAbstraction.selectRecord(query, DataUtil.convertToList(resource, threat, environment));

	}
	


}