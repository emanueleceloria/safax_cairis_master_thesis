package nl.tue.sec.safax.sfxbe.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import nl.tue.sec.safax.sfxbe.ds.AttributeRecord;
import nl.tue.sec.safax.sfxbe.util.DataUtil;

import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.Block;
import com.mongodb.client.AggregateIterable;

import org.bson.Document;

import static java.util.Arrays.asList;


public class DBFns {
	public static int log(String transactionID, String logMsg,int level,String component){
		String query="INSERT INTO sfx_log(transactionid,log,logtype,utimestamp,component) VALUES(?,?,?,?,?)";
		long tstamp= System.currentTimeMillis() / 1000L;
		return DBAbstraction.insertStatement(query, DataUtil.convertToList(transactionID,
				logMsg,Integer.toString(level),Long.toString(tstamp),component));
	}
	
	public static int logUcon(String uconSession, String logMsg){
		String query="INSERT INTO sfx_ucon_log(log, ucon_sessionid, utimestamp) VALUES(?,?,?)";
		long tstamp= System.currentTimeMillis() / 1000L;
		return DBAbstraction.insertStatement(query, DataUtil.convertToList(logMsg,
				uconSession,Long.toString(tstamp)));
	}
	
	public static int errorlog(String transactionID, String logHead,String logMsg,int level,String component){
		String query="INSERT INTO sfx_errorlog(transactionid,errorheader,errorlog,logtype,utimestamp,component) VALUES(?,?,?,?,?,?)";
		long tstamp= System.currentTimeMillis() / 1000L;
		return DBAbstraction.insertStatement(query, DataUtil.convertToList(transactionID,logHead,
									logMsg,Integer.toString(level),Long.toString(tstamp),component));
	}

	/********************** USER SPECIFIC **************************/
	public static boolean userNameExists(String username){
		String query="SELECT COUNT(*) FROM sfx_user WHERE uname=?";
		int count=Integer.parseInt(DBAbstraction.selectRecord(query, DataUtil.convertToList(username)));
		return (count>0)?true:false;
	}
	
	public static boolean emailExists(String email){
		String query="SELECT COUNT(*) FROM sfx_user WHERE email=?";
		int count=Integer.parseInt(DBAbstraction.selectRecord(query, DataUtil.convertToList(email)));
		return (count>0)?true:false;
	}
	public static boolean emailExists(String uname, String email){
		String query="SELECT COUNT(*) FROM sfx_user WHERE email=? AND uname!=?";
		int count=Integer.parseInt(DBAbstraction.selectRecord(query, DataUtil.convertToList(email,uname)));
		return (count>0)?true:false;
	}
	
	public static String loginUser(String username, String password){
		String query="SELECT uid FROM sfx_user WHERE uname=? AND  upwd=? AND isactive=1";
		return DBAbstraction.selectRecord(query,DataUtil.convertToList(username,password));
	}
	
	public static int createSession(String uid, String ip, String useragent, String sessionid, long timestamp,boolean isguest){
		String query="INSERT INTO sfx_usersession(uid,ip,useragent,sessionid, sessionstart,isactive,isguest) "
				+ "VALUES (?,?,?,?,?,1,?)";
		int guest = isguest? 1 : 0;
		return DBAbstraction.insertStatement(query, DataUtil.convertToList(uid,ip,useragent,sessionid,Long.toString(timestamp),Integer.toString(guest)));
	}
	public static String  validateSession(String sessionid){
		String query="SELECT uname FROM sfx_user a, sfx_usersession b WHERE a.uid=b.uid AND b.sessionid=? AND b.isactive=1";
		String uname=DBAbstraction.selectRecord(query, DataUtil.convertToList(sessionid));
		long unixTime = System.currentTimeMillis();
		if(uname!=null && uname.length()>0){
			query="UPDATE sfx_usersession SET lasttransaction=? WHERE sessionid=?";
			DBAbstraction.updateStatement(query, DataUtil.convertToList(Long.toString(unixTime),sessionid));
			return uname;
		}
		return null;
	}
	
	public static int createUser(String username, String password,
			String fname, String email) {
			String query="INSERT INTO sfx_user(uname,upwd,fullname,email,isactive) VALUES(?,?,?,?,0)";
			int uid=DBAbstraction.insertStatement(query, DataUtil.convertToList(username,password,fname,email));
			query="SELECT gid FROM sfx_group WHERE groupname=?";
			String gid=DBAbstraction.selectRecord(query, DataUtil.convertToList("registered"));
			query="INSERT INTO sfx_usergroup(gid,uid) VALUES (?,?)";
			DBAbstraction.insertStatement(query, DataUtil.convertToList(gid,Integer.toString(uid)));
			return uid;
	}
	
	public static int createGuestUser(String username, String password, String fname, String email) {
		String query="INSERT INTO sfx_user(uname,upwd,fullname,email,isactive) VALUES(?,?,?,?,0)";
		int uid=DBAbstraction.insertStatement(query, DataUtil.convertToList(username,password,fname,email));
		query="SELECT gid FROM sfx_group WHERE groupname=?";
		String gid=DBAbstraction.selectRecord(query, DataUtil.convertToList("guest"));
		query="INSERT INTO sfx_usergroup(gid,uid) VALUES (?,?)";
		DBAbstraction.insertStatement(query, DataUtil.convertToList(gid,Integer.toString(uid)));
		String prid=DBAbstraction.selectRecord("SELECT prid FROM sfx_project WHERE projectname='GUEST' AND isactive=1", null);
		if(prid==null || prid.length()<1)
			prid=Integer.toString(createProject("GUEST", "", "", "0", "1"));
		assignProjectUsers(prid, Integer.toString(uid));
		return uid;
	}
	
	public static int requestReset(String email){
		String query="INSERT INTO sfx_userrequest (requestheader,requestmessage,status) VALUES (?,?,1)";
		String requestheader="Password reset request";
		String requestmessage="From user with email : "+email;
		return DBAbstraction.insertStatement(query, DataUtil.convertToList(requestheader,requestmessage));
	}
	
	public static JSONArray getUsers(){
		String query="SELECT uid, uname, fullname FROM sfx_user WHERE isactive=1 OR isactive=-1 ORDER BY uname ASC";
		return DBAbstraction.selectRecordsJson(query,null);
	}
	
	/* Registered Users find users
	 * 1) Initially all user are shown (already done)
	 * 2) If a letter is typed, the list of users should be filtered by considering only the users for which the letter occurs in the username/name/email.
	 * 3) If another letter is typed, the list of users should contain only the users for which the string occurs in the username/name/email. (and so on)
	 * 4) If a letter is deleted from the string, the list of users should be updated accordingly. 
	 */
	public static JSONArray findUsers(String q, String uname){
		// Original Query from Samuel String query="SELECT uid, uname FROM sfx_user WHERE isactive=1 AND uname!=? AND (uname LIKE '%"+q+"%' OR fullname LIKE '%"+q+"%'  OR email ='"+q+"') ";
		// Modified Query by Duc Luu
		String query="SELECT uid, uname FROM sfx_user WHERE isactive=1 AND uname!=? AND (uname LIKE '"+q+"%' OR fullname LIKE '"+q+"%'  OR email LIKE '"+q+"%') ";
		return DBAbstraction.selectRecordsJson(query,DataUtil.convertToList(uname));
	}
	
	/* Administrators find users
	 * 1) Initially all user are shown (already done)
	 * 2) If a letter is typed, the list of users should be filtered by considering only the users for which the letter occurs in the username/name/email.
	 * 3) If another letter is typed, the list of users should contain only the users for which the string occurs in the username/name/email. (and so on)
	 * 4) If a letter is deleted from the string, the list of users should be updated accordingly. 
	 */
	public static JSONArray findUsersAdmin(String q, String uname){
		String query="SELECT uid, uname FROM sfx_user WHERE (isactive=1 OR isactive=-1) AND uname!=? AND (uname LIKE '"+q+"%' OR fullname LIKE '"+q+"%'  OR email LIKE '"+q+"%')";
		return DBAbstraction.selectRecordsJson(query,DataUtil.convertToList(uname));
	}
	public static String getUserFromName(String uname){
		String query="SELECT uid FROM sfx_user WHERE uname=?";
		return DBAbstraction.selectRecord(query, DataUtil.convertToList(uname));
	}
	
	public static String getUserIDFromSessionID(String usid){
		String query="SELECT uid FROM sfx_usersession WHERE usid = ?";
		return DBAbstraction.selectRecord(query, DataUtil.convertToList(usid));
	}
	
	public static String getUserEmail(String uid){
		String query="SELECT email FROM sfx_user WHERE uid=?";
		return DBAbstraction.selectRecord(query, DataUtil.convertToList(uid));
	}
	public static String getUserFromEmail(String email){
		String query="SELECT uid FROM sfx_user WHERE email=?";
		return DBAbstraction.selectRecord(query, DataUtil.convertToList(email));
	}
	
	public static int registerRequest(String requestid, String uid){
		long tstamp= System.currentTimeMillis();
		String query="INSERT INTO sfx_pwdreset(reqid,requesttime,isactive,uid) VALUES(?,?,1,?)";
		return DBAbstraction.insertStatement(query, DataUtil.convertToList(requestid,Long.toString(tstamp),uid));
	}
	
	public static boolean updatePwdReset(String requestid){
		String query="UPDATE sfx_pwdreset SET isactive=0 WHERE reqid=?";
		return DBAbstraction.updateStatement(query, DataUtil.convertToList(requestid));
	}
	
	public static boolean resetPassword(String uid,String pwd){
		String query="UPDATE sfx_user SET upwd=? WHERE uid=?";
		return DBAbstraction.updateStatement(query, DataUtil.convertToList(pwd,uid));
	}
	
	public static JSONObject getUserInfo(String userid){
		String query="SELECT uid,uname,email,fullname,isactive FROM sfx_user WHERE uid=?";
		return DBAbstraction.selectRecordJson(query, DataUtil.convertToList(userid));
	}
	
	public static JSONArray getUserGroupInfo(String uid){
		String query="SELECT a.gid,a.groupname FROM sfx_group a, sfx_usergroup b, sfx_user c WHERE a.gid=b.gid AND b.uid=c.uid AND c.uid=?";
		return DBAbstraction.selectRecordsJson(query, DataUtil.convertToList(uid));
	}
	
	public static JSONArray getAllUserGroupInfo(String uid){
		String query="SELECT gid,groupname FROM sfx_group";
		return DBAbstraction.selectRecordsJson(query, null);
	}
	
	public static JSONArray findGroups(String groupname){
		String query="SELECT * from sfx_group WHERE groupname LIKE '"+groupname+"%'";
		return DBAbstraction.selectRecordsJson(query, null);
	}
	
	public static boolean updateUserPassword(String userid, String password){
		String query="UPDATE sfx_user SET upwd=? WHERE uid=?";
		return DBAbstraction.updateStatement(query, DataUtil.convertToList(password,userid));
	}
	public static boolean updateUser(String userid, String username, String fullname, String useremail, String password){
		String query="UPDATE sfx_user SET uname=?, fullname=?, email=?, upwd=? WHERE uid=?";
		return DBAbstraction.updateStatement(query, DataUtil.convertToList(username,fullname,useremail,password,userid));
	}
	
	public static boolean updateUser(String userid, String username, String fullname, String useremail){
		String query="UPDATE sfx_user SET uname=?, fullname=?, email=? WHERE uid=?";
		return DBAbstraction.updateStatement(query, DataUtil.convertToList(username,fullname,useremail,userid));
	}
	public static boolean updateUserActive(boolean deactive,String userid){
		String query="";
		if(deactive)
			query="UPDATE sfx_user SET isactive=-1 WHERE uid=?";
		else
			query="UPDATE sfx_user SET isactive=1 WHERE uid=?";
		return DBAbstraction.updateStatement(query, DataUtil.convertToList(userid));
	}
	
	public static boolean assignUserGroup(String userid, String groupid){
		String query="INSERT INTO sfx_usergroup VALUES(?,?)";
		return DBAbstraction.updateStatement(query, DataUtil.convertToList(groupid,userid));
	}
	
	public static boolean removeUserGroup(String userid){
		String query="DELETE FROM sfx_usergroup WHERE uid=?";
		return DBAbstraction.updateStatement(query, DataUtil.convertToList(userid));
	}
	
	
	/********************** PROJECT SPECIFIC **************************/
	public static JSONArray getAllProjects(){
		String query="SELECT * FROM sfx_project WHERE isactive=1";
		return DBAbstraction.selectRecordsJson(query, null);
	}
	public static JSONArray getPublicProjects(){
		String query="SELECT * FROM sfx_project WHERE isactive=1 AND ispublic=1";
		return DBAbstraction.selectRecordsJson(query, null);
	}
	public static JSONArray getAllPrivateProjects(String username){
		String query="SELECT a.prid, a.projectname, a.projecturl,a.projectdesc,a.ispublic FROM sfx_project a, sfx_user b, sfx_projectuser c WHERE a.prid=c.prid AND b.uid=c.uid AND a.isactive=1 AND a.ispublic=0 AND b.uname=?";
		return DBAbstraction.selectRecordsJson(query, DataUtil.convertToList(username));
	}
	public static JSONArray getAllAssignedProjects(String username){
		String query="SELECT a.prid, a.projectname, a.projecturl,a.projectdesc,a.ispublic FROM sfx_project a, sfx_user b, sfx_projectuser c WHERE a.prid=c.prid AND b.uid=c.uid AND a.isactive=1 AND b.uname=?";
		return DBAbstraction.selectRecordsJson(query, DataUtil.convertToList(username));
	}
	
	public static boolean checkProjectName(String projectname){
		String query="SELECT COUNT(*) FROM sfx_project WHERE projectname=?";
		int count=Integer.parseInt(DBAbstraction.selectRecord(query, DataUtil.convertToList(projectname)));
		return (count>0)?true:false;
	}
	
	public static int createProject(String projectname, String projectdesc, String projecturl, String ispublic, String uid){
		String query="INSERT INTO sfx_project(projectname,projecturl,projectdesc, isactive, ispublic,projectowner) "
				+ "VALUES(?,?,?,1,?,?)";
		return DBAbstraction.insertStatement(query, DataUtil.convertToList(projectname,projecturl,projectdesc,ispublic,uid));
	}
	
	public static boolean updateProject(String prid,String projectname, String projectdesc, String projecturl, String ispublic, String uid){
		String query="UPDATE sfx_project SET projectname=?, projecturl=?, projectdesc=?, ispublic=? WHERE prid=?";
		return DBAbstraction.updateStatement(query, DataUtil.convertToList(projectname,projecturl,projectdesc,ispublic,prid));
	}
	
	public static int assignProjectUsers(String prid,String uid){
		String query="INSERT INTO sfx_projectuser(prid,uid,accesslevel) VALUES (?,?,1)";
		return DBAbstraction.insertStatement(query, DataUtil.convertToList(prid,uid));
	}
	
	public static boolean removeProjectUsers(String prid){
		String query="DELETE a.* FROM sfx_projectuser AS a INNER JOIN sfx_project AS b  on a.prid=b.prid WHERE a.prid=? AND a.uid!=b.projectowner";
		return DBAbstraction.deleteStatement(query, DataUtil.convertToList(prid));
	}
	
	public static boolean deleteProject(String prid){
		String query="UPDATE sfx_project SET isactive=0 WHERE prid=?";
		return DBAbstraction.updateStatement(query, DataUtil.convertToList(prid));
	}
	
	public static String getProjectName(String prid){
		String query="SELECT projectname FROM sfx_project WHERE prid=?";
		return DBAbstraction.selectRecord(query, DataUtil.convertToList(prid));
	}
	
	public static JSONObject getProjectInfo(String prid,String uname){
//		String query="SELECT a.prid, a.projectname, a.projecturl,a.projectdesc, a.ispublic "
//				+ "FROM sfx_project a, sfx_user b, sfx_projectuser c WHERE a.prid=c.prid AND "
//				+ "b.uid=c.uid AND a.isactive=1 AND a.prid=? AND b.uname=?";
		String query="SELECT prid,projectname, projecturl,projectdesc,ispublic FROM sfx_project WHERE prid=?";
		return DBAbstraction.selectRecordJson(query, DataUtil.convertToList(prid));
	}
	
	public static JSONArray getProjectUsers(String prid, String uname){
		String query="SELECT b.uid, b.uname FROM sfx_projectuser a, sfx_user b WHERE"
				+ " a.uid=b.uid AND a.prid=? AND b.uname!=?";
		return DBAbstraction.selectRecordsJson(query, DataUtil.convertToList(prid,uname));
	}
	
	public static ArrayList<String> getProjectUserIDs(String prid){
		String query="SELECT b.uid FROM sfx_projectuser a, sfx_user b WHERE"
				+ " a.uid=b.uid AND a.prid=?";
		return DBAbstraction.selectRecords(query, DataUtil.convertToList(prid));
	}
	
	public static String getProjectNameFromDemo(String demoid){
		String query="SELECT projectname FROM sfx_project a, sfx_demo b WHERE a.prid=b.prid AND b.demoid=?";
		return DBAbstraction.selectRecord(query, DataUtil.convertToList(demoid));
	}
	/********************** DEMO SPECIFIC **************************/
	public static JSONArray getAllDemos(String projectid){
		String query="SELECT * FROM sfx_demo WHERE prid=? AND isactive=1";
		return DBAbstraction.selectRecordsJson(query, DataUtil.convertToList(projectid));
	}
	public static boolean deleteDemo(String demoid){
		/* delete data from sfx_transparency and sfx_democonflicts 8?
		 * 
		 */
		String delete_query_demo_conflicts = "DELETE c.* from sfx_transparency a "
							+ "INNER JOIN sfx_pdp b "
							+ "ON a.pdpid = b.pdpid "
							+ "INNER JOIN sfx_democonflicts c "
							+ "ON a.transparencyid = c.transparencyid "
							+ "WHERE b.demoid = ?";
		
		DBAbstraction.deleteStatement(delete_query_demo_conflicts, DataUtil.convertToList(demoid));
		
		String delete_query = "DELETE a.* from sfx_transparency a "
							+ "INNER JOIN sfx_pdp b "
							+ "ON a.pdpid = b.pdpid "
							+ "WHERE b.demoid = ?";
		
		DBAbstraction.deleteStatement(delete_query, DataUtil.convertToList(demoid));
		
		
		String query="UPDATE sfx_demo SET isactive=0 WHERE demoid=?";
		DBAbstraction.updateStatement(query, DataUtil.convertToList(demoid));
		query="UPDATE sfx_pdp SET isactive=0 WHERE demoid=?";
		return DBAbstraction.updateStatement(query, DataUtil.convertToList(demoid));
	}
	
	public static int createDemo(String demoname,String demodesc, String uid, String projectid){
		String query="INSERT INTO sfx_demo(demoname,demodesc, isactive, prid,demoowner) VALUES (?,?,1,?,?)";
		return  DBAbstraction.insertStatement(query, DataUtil.convertToList(demoname,demodesc,projectid,uid));
	}
	
	public static boolean editDemo(String demoname,String demodesc, String projectid,String demoid){
		String query="UPDATE sfx_demo SET demoname=?, demodesc=?, prid=? WHERE demoid=?";
		return  DBAbstraction.updateStatement(query, DataUtil.convertToList(demoname,demodesc,projectid,demoid));
	}
	
	public static JSONObject fetchDemo(String demoid){
		String query="SELECT * FROM sfx_demo WHERE demoid=?";
		return  DBAbstraction.selectRecordJson(query, DataUtil.convertToList(demoid));
	}
	
	public static int copyDemo(String demoid,String prid, String uid){
		String query="INSERT INTO sfx_demo (demoname, demodesc, isactive, prid, demoowner)  "
				+ "SELECT demoname,demodesc,isactive,"+prid+",demoowner FROM sfx_demo WHERE demoid=?";
		int did=DBAbstraction.insertStatement(query, DataUtil.convertToList(demoid));
		if(did<1)
			return 0;
		
		query="UPDATE sfx_demo SET demoowner=? WHERE demoid=?";
		DBAbstraction.updateStatement(query, DataUtil.convertToList(uid, Integer.toString(did)));
		createPDP(Integer.toString(did));
		query="INSERT INTO sfx_xacmlpolicy (policyid, policy,isactive, demoid) "
				+ "SELECT policyid, policy, isactive, ?  FROM sfx_xacmlpolicy WHERE demoid=?";
		DBAbstraction.insertStatement(query, DataUtil.convertToList(Integer.toString(did), demoid));
		
		query="INSERT INTO ext_trust_policy (policyid, policy,policytype, isactive, demoid) "
				+ "SELECT policyid, policy, policytype, isactive, ?  FROM ext_trust_policy WHERE demoid=?";
		DBAbstraction.insertStatement(query, DataUtil.convertToList(Integer.toString(did), demoid));
		
		query="INSERT INTO sfx_configuration (configkey, configvalue, demoid) "
				+ "SELECT configkey, configvalue, ?  FROM sfx_configuration WHERE demoid=?";
		DBAbstraction.insertStatement(query, DataUtil.convertToList(Integer.toString(did), demoid));
		
		query="INSERT INTO sfx_xacmlrequest (requestid, request,isactive, demoid) "
				+ "SELECT requestid, request, isactive, ?  FROM sfx_xacmlrequest WHERE demoid=?";
		DBAbstraction.insertStatement(query, DataUtil.convertToList(Integer.toString(did), demoid));

		
		// Copy transparency conflicts - 26 November 2015		
		String select_pdpid_query=  
				  " SELECT pdpid FROM sfx_pdp a " 
				+ " WHERE demoid = ?";

		// Integer.toString(did) -> demoid of the copied demo
		ArrayList<String> listOfPDPID = DBAbstraction.selectRecords(select_pdpid_query, DataUtil.convertToList(Integer.toString(did)));
		
		// Copy transparency conflicts - 26 November 2015		
		String select_transparencyid_query=  
				  " SELECT transparencyid FROM sfx_transparency a " 
				+ " INNER JOIN sfx_pdp b"
				+ " ON a.pdpid = b.pdpid"
				+ " WHERE b.demoid = ?";
		
		ArrayList<String> listOfTransparencyID = DBAbstraction.selectRecords(select_transparencyid_query, DataUtil.convertToList(demoid));
			
		for (int i = 0; i < listOfTransparencyID.size(); i++) {
			// 1. table sfx_transparency: transparencyid, pdpid, uid, viewpoint
			query="INSERT INTO sfx_transparency (pdpid, uid, viewpoint) "
					+ " SELECT ?, uid, viewpoint FROM sfx_transparency "
					+ " WHERE transparencyid = ?";
					
			int new_trans_id = DBAbstraction.insertStatement(query, DataUtil.convertToList(listOfPDPID.get(0), listOfTransparencyID.get(i)));
			
			// 2. table sfx_democonflicts.			
			String query_conflicts = " SELECT conflictid FROM sfx_democonflicts "
								   + " WHERE transparencyid = ? ";
			
			ArrayList<String> listOfConflictsID = DBAbstraction.selectRecords(query_conflicts, DataUtil.convertToList( listOfTransparencyID.get(i)));
			
			for (int j = 0; j < listOfConflictsID.size(); j++) {
				String query_insert_demo_conflicts = 
						" INSERT INTO sfx_democonflicts (transparencyid, conflictid) "
					+	" VALUES (?, ?) ";

				DBAbstraction.insertStatement(query_insert_demo_conflicts, DataUtil.convertToList(Integer.toString(new_trans_id), listOfConflictsID.get(j)));
			}
		}
		
		return did;
	}
	
	public static boolean loadAttributes(ArrayList<AttributeRecord> records, String demoid){
		deleteAttributes(demoid);
		for(int i=0;i<records.size();i++){
			if(records.get(i).getRelid().equalsIgnoreCase("null")){
				if(records.get(i).getDatatype().equals("string"))
					records.get(i).setDatatype("http://www.w3.org/2001/XMLSchema#string");
				else if(records.get(i).getDatatype().equals("integer") || records.get(i).getDatatype().equals("int"))
					records.get(i).setDatatype("http://www.w3.org/2001/XMLSchema#integer");
				else if(records.get(i).getDatatype().equals("boolean"))
					records.get(i).setDatatype("http://www.w3.org/2001/XMLSchema#boolean");
				else if(records.get(i).getDatatype().equals("double"))
					records.get(i).setDatatype("http://www.w3.org/2001/XMLSchema#double");
				else if(records.get(i).getDatatype().equals("date"))
					records.get(i).setDatatype("http://www.w3.org/2001/XMLSchema#date");
				else if(records.get(i).getDatatype().equals("dateTime"))
					records.get(i).setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");
				else if(records.get(i).getDatatype().equals("time"))
					records.get(i).setDatatype("http://www.w3.org/2001/XMLSchema#time");
				else if(records.get(i).getDatatype().equals("URI"))
					records.get(i).setDatatype("http://www.w3.org/2001/XMLSchema#anyURI");
				
				String query="INSERT INTO sfx_attribute(attributeid, attributedatatype,attributevalue,demoid) VALUES"
						+ "(?,?,?,?)";
				DBAbstraction.insertStatement(query, DataUtil.convertToList(records.get(i).getAttributeid(),
						records.get(i).getDatatype(),records.get(i).getAttributevalue(),demoid));
			}
		}
		for(int i=0;i<records.size();i++){
			if(!(records.get(i).getRelid().equalsIgnoreCase("null") || records.get(i).getRelvalue().equalsIgnoreCase("null"))){
				String query="SELECT attid FROM sfx_attribute WHERE attributeid=? AND attributevalue=? AND demoid=?";
				String relid=DBAbstraction.selectRecord(query, DataUtil.convertToList(records.get(i).getRelid(),records.get(i).getRelvalue(),demoid));
				if(relid==null || relid.length()<1)
					continue;
				
				if(records.get(i).getDatatype().equals("string"))
					records.get(i).setDatatype("http://www.w3.org/2001/XMLSchema#string");
				else if(records.get(i).getDatatype().equals("integer") || records.get(i).getDatatype().equals("int"))
					records.get(i).setDatatype("http://www.w3.org/2001/XMLSchema#integer");
				else if(records.get(i).getDatatype().equals("boolean"))
					records.get(i).setDatatype("http://www.w3.org/2001/XMLSchema#boolean");
				else if(records.get(i).getDatatype().equals("double"))
					records.get(i).setDatatype("http://www.w3.org/2001/XMLSchema#double");
				else if(records.get(i).getDatatype().equals("date"))
					records.get(i).setDatatype("http://www.w3.org/2001/XMLSchema#date");
				else if(records.get(i).getDatatype().equals("dateTime"))
					records.get(i).setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");
				else if(records.get(i).getDatatype().equals("time"))
					records.get(i).setDatatype("http://www.w3.org/2001/XMLSchema#time");
				else if(records.get(i).getDatatype().equals("URI"))
					records.get(i).setDatatype("http://www.w3.org/2001/XMLSchema#anyURI");
				query="INSERT INTO sfx_attribute(attributeid, attributedatatype,attributevalue,relid,demoid) VALUES"
						+ "(?,?,?,?,?)";
				DBAbstraction.insertStatement(query, DataUtil.convertToList(records.get(i).getAttributeid(),
						records.get(i).getDatatype(),records.get(i).getAttributevalue(),relid,demoid));
			}
		}
		return true;
	}
	
	public static JSONArray getAttributes(String demoid, String refid){
		String query="SELECT * FROM sfx_attribute WHERE relid=? AND relid IS NOT NULL AND demoid=?";
		return DBAbstraction.selectRecordsJson(query, DataUtil.convertToList(refid,demoid));
	}
	
	public static JSONArray getRootAttributes(String demoid){
		String query="SELECT * FROM sfx_attribute WHERE relid IS NULL AND demoid=?";
		return DBAbstraction.selectRecordsJson(query, DataUtil.convertToList(demoid));
	}
	
	public static boolean deleteAttributes(String demoid){
		String query="DELETE FROM sfx_attribute WHERE demoid=?";
		return DBAbstraction.deleteStatement(query, DataUtil.convertToList(demoid));
	}
	
	/********************** PDP SPECIFIC **************************/
	public static int createPDP(String demoid){
		String query="INSERT INTO sfx_pdp(pdpcode,pdpurl,ispersistent,isactive,haspip,demoid) VALUES (?,'na',0,1,1,?)";
		return DBAbstraction.insertStatement(query, DataUtil.convertToList(demoid,demoid));
	}
	public static boolean updatePDP(String demoid, String pdpcode,String pdprcalgo,
			String ispersistent) {
		String query="UPDATE sfx_pdp SET pdpcode=?, rcalgorithm=?, ispersistent=? WHERE demoid=?";
		return DBAbstraction.updateStatement(query,DataUtil.convertToList(pdpcode,pdprcalgo,ispersistent,demoid));
	}
	public static JSONArray getPDPInfo(String demoid){
		String query="SELECT pdpid,pdpcode,pdpurl,rcalgorithm,ispersistent,isactive,haspip,demoid FROM sfx_pdp WHERE demoid=?";
		return DBAbstraction.selectRecordsJson(query, DataUtil.convertToList(demoid));
	}
	
	public static JSONArray getAllPDPS(String username){
		String query="select b.demoname,a.pdpcode,b.demoid from sfx_pdp a, sfx_demo b, sfx_project c "
				+ "WHERE a.demoid=b.demoid AND b.prid=c.prid AND c.isactive=1 AND b.isactive=1 AND c.isactive=1";
		return DBAbstraction.selectRecordsJson(query, null);
	}
	public static JSONArray getPublicPDPS(String username){
		String query="SELECT b.demoname, a.pdpcode, b.demoid FROM sfx_pdp a, sfx_demo b, sfx_project c WHERE a.demoid=b.demoid AND b.prid=c.prid AND b.isactive=1 AND c.isactive=1 AND c.ispublic=1";
		return DBAbstraction.selectRecordsJson(query, null);
	}
	
	public static JSONArray getPrivatePDPS(String uid){
		String query="SELECT b.demoname, a.pdpcode, b.demoid FROM sfx_pdp a, sfx_demo b, sfx_project c, sfx_projectuser d "
				+ "WHERE a.demoid=b.demoid AND b.prid=c.prid AND b.isactive=1 AND c.isactive=1 AND c.ispublic=0 AND c.prid=d.prid AND d.uid=?";
		return DBAbstraction.selectRecordsJson(query, DataUtil.convertToList(uid));
	}
	
	public static int setupDemoConfig(String configkey,String configvalue, String demoid){
		String query="INSERT INTO sfx_configuration(configkey,configvalue,demoid) VALUES(?,?,?)";
		return DBAbstraction.insertStatement(query, DataUtil.convertToList(configkey,configvalue,demoid));
	}
	
	public static JSONArray fetchDemoConfig(String demoid){
		String query="SELECT * FROM sfx_configuration WHERE demoid=?";
		return DBAbstraction.selectRecordsJson(query,DataUtil.convertToList(demoid));
	}
	
	public static JSONObject getDemoPDPHost(String demoid){
		String query="SELECT serviceurl FROM sfx_configuration a, sfx_serviceregistry b WHERE demoid=? and a.configvalue = b.serviceid and b.servicecomponent ='pdp'";
		return DBAbstraction.selectRecordJson(query,DataUtil.convertToList(demoid));
	}
	
	public static boolean updateDemoConfig(String configkey,String configvalue, String demoid){
		String query="UPDATE sfx_configuration SET configvalue=? WHERE configkey=? AND demoid=?";
		return DBAbstraction.updateStatement(query, DataUtil.convertToList(configvalue,configkey,demoid));
	}
	
	public static boolean removeDemoConfig(String configkey,String demoid){
		String query="DELETE FROM sfx_configuration WHERE configkey=? AND demoid=?";
		return DBAbstraction.deleteStatement(query, DataUtil.convertToList(configkey,demoid));
	}
	
		
	/********************** POLICY SPECIFIC **************************/
	public static JSONArray getPolicyIds(String demoid){
		String query="SELECT xpid, policyid FROM sfx_xacmlpolicy WHERE isactive=1 AND demoid=?";
		return DBAbstraction.selectRecordsJson(query, DataUtil.convertToList(demoid));
	}
	
	public static String getDemoFromPolicy(String pid,String type){
		String query;
		if(type.equals("xacml"))
			query="SELECT demoid FROM sfx_xacmlpolicy where xpid=?";
		else
			query="SELECT demoid FROM ext_trust_policy where tpid=?";
		return DBAbstraction.selectRecord(query, DataUtil.convertToList(pid));
	}
	
	public static String getDemoFromRequest(String xrid){
		String query="SELECT demoid FROM sfx_xacmlrequest where xrid=?";
		return DBAbstraction.selectRecord(query, DataUtil.convertToList(xrid));
	}
	
	public static String getProjectNameFromXPolicy(String xpid){
		String query="SELECT projectname FROM sfx_project a, sfx_demo b, sfx_xacmlpolicy c WHERE a.prid=b.prid AND b.demoid=c.demoid AND c.xpid=?";
		return DBAbstraction.selectRecord(query, DataUtil.convertToList(xpid));
	}
	public static String getProjectNameFromTPolicy(String tpid){
		String query="SELECT projectname FROM sfx_project a, sfx_demo b, ext_trust_policy c WHERE a.prid=b.prid AND b.demoid=c.demoid AND c.tpid=?";
		return DBAbstraction.selectRecord(query, DataUtil.convertToList(tpid));
	}
	
	
	public static String getXacmlPolicy(String xpid){
		String query="SELECT policy FROM sfx_xacmlpolicy WHERE isactive=1 AND xpid=?";
		return DBAbstraction.selectRecord(query, DataUtil.convertToList(xpid));
	}
	
	public static String getXacmlRequest(String xrid){
		String query="SELECT request FROM sfx_xacmlrequest WHERE isactive=1 AND xrid=?";
		return DBAbstraction.selectRecord(query, DataUtil.convertToList(xrid));
	}
	
	public static String isUconRequest(String pdpcode){
		String query="SELECT configid FROM sfx_configuration a INNER JOIN sfx_pdp b on a.demoid = b.demoid WHERE configvalue = 'nl:tue:sec:ucon:pep' and pdpcode = ?";
		return DBAbstraction.selectRecord(query, DataUtil.convertToList(pdpcode));
	}
	
	public static String isTransparencyRequest(String pdpcode){
		String query="SELECT configid FROM sfx_configuration a INNER JOIN sfx_pdp b on a.demoid = b.demoid WHERE (configvalue = 'nl:tue:sec:safax:transparency:pep' or configvalue = 'nl:tue:sec:safax:transparency:pdp') and pdpcode = ?";
		return DBAbstraction.selectRecord(query, DataUtil.convertToList(pdpcode));
	}
	
	public static boolean removeUconSession(String ucon_session){
		String query = "DELETE FROM sfx_ucon_update WHERE ucon_sessionid = ?";
		DBAbstraction.deleteStatement(query, DataUtil.convertToList(ucon_session));
		
		query = "DELETE FROM sfx_ucon_request WHERE ucon_sessionid = ?";
		DBAbstraction.deleteStatement(query, DataUtil.convertToList(ucon_session));
		
		query="DELETE FROM sfx_accessedattribute WHERE ucon_sessionid = ?";
		return DBAbstraction.deleteStatement(query, DataUtil.convertToList(ucon_session));
	}
	
	public static boolean removeUconRequestSession(String ucon_request_session){
		/* Already in stopTimer of Obligation.java
		String query = "DELETE FROM sfx_ucon_update WHERE request_sessionid = ?";
		DBAbstraction.deleteStatement(query, DataUtil.convertToList(ucon_request_session));
		*/
		
		String query = "DELETE FROM sfx_ucon_request WHERE request_sessionid = ?";
		return DBAbstraction.deleteStatement(query, DataUtil.convertToList(ucon_request_session));
	}
	
	public static JSONArray getXacmlRequestIds(String demoid){
		String query="SELECT xrid, requestid FROM sfx_xacmlrequest WHERE isactive=1 AND demoid=?";
		return DBAbstraction.selectRecordsJson(query, DataUtil.convertToList(demoid));
	}
	
	public static JSONArray pollInitialUconPIP(String ucon_session){
		String query="SELECT a.ucon_sessionid, a.updatetime, a.id, a.attributeid, a.attributevalue, b.attributevalue as 'categoryvalue' " + 
	                 " FROM sfx_accessedattribute a " + 
				     " INNER JOIN sfx_accessedattribute b ON  a.relid = b.attid  WHERE a.ucon_display = 1 AND a.ucon_sessionid = ? AND b.ucon_sessionid = ? ";
		return DBAbstraction.selectRecordsJson(query, DataUtil.convertToList(ucon_session, ucon_session));
	}
	
	public static JSONArray pollInitialUconUpdate(String ucon_session){
		String query="SELECT * " + 
	                 " FROM sfx_ucon_update a " + 
				     " INNER JOIN sfx_xacmlrequest b ON  a.xrid = b.xrid WHERE a.ucon_sessionid = ? AND a.ucon_display = 1";
		return DBAbstraction.selectRecordsJson(query, DataUtil.convertToList(ucon_session));
	}
	
	public static JSONArray pollInitialUconRequest(String ucon_session){
		String query="SELECT * " + 
	                 " FROM sfx_ucon_request a " + 
				     " INNER JOIN sfx_xacmlrequest b ON  a.xrid = b.xrid WHERE a.ucon_sessionid = ? AND a.ucon_display = 1 and a.trigger_interval is NOT NULL";
		return DBAbstraction.selectRecordsJson(query, DataUtil.convertToList(ucon_session));
	}
	
	public static JSONArray pollInitialUconLog(String ucon_session){
		String query="SELECT * " + 
	                 " FROM sfx_ucon_log" + 
                     " WHERE ucon_sessionid = ?";
		return DBAbstraction.selectRecordsJson(query, DataUtil.convertToList(ucon_session));
	}
	
	public static JSONArray pollUconSession(String ucon_session, long tstamp){
		String query="SELECT a.ucon_sessionid, a.updatetime, a.id, a.attributeid, a.attributevalue, b.attributevalue as 'categoryvalue' " + 
	                 " FROM sfx_accessedattribute a " + 
				     " INNER JOIN sfx_accessedattribute b ON a.relid = b.attid  WHERE a.ucon_sessionid = ? and a.updatetime > ? and b.ucon_sessionid = ? ";
		return DBAbstraction.selectRecordsJson(query, DataUtil.convertToList(ucon_session, Long.toString(tstamp), ucon_session));
	}
	
	public static String getAttributeIDHasValueChanged(String ucon_session, long tstamp){
		String query="SELECT a.attributeid " + 
	                 " FROM sfx_accessedattribute a " + 
				     " INNER JOIN sfx_accessedattribute b ON a.relid = b.attid  WHERE a.ucon_sessionid = ? and a.updatetime > ? and b.ucon_sessionid = ? ";
		return DBAbstraction.selectRecord(query, DataUtil.convertToList(ucon_session, Long.toString(tstamp), ucon_session));
	}
		
	public static String pollUconUpdateTime(String ucon_session){
		String query="SELECT MAX(updatetime) FROM sfx_accessedattribute WHERE ucon_sessionid = ?";
		return DBAbstraction.selectRecord(query, DataUtil.convertToList(ucon_session));
	}
	
	public static int isDemoOwner(String demoid, String username){
		String query="SELECT  COUNT(*) FROM sfx_demo a, sfx_user b WHERE a.demoowner=b.uid AND a.demoid=? AND b.uname=?";
		return Integer.parseInt(DBAbstraction.selectRecord(query, DataUtil.convertToList(demoid,username)));
	}
	public static int uploadPolicy(String demoid, String policyid, String policy){
		String query="DELETE FROM sfx_xacmlpolicy WHERE policyid=? AND demoid=?";
		DBAbstraction.deleteStatement(query, DataUtil.convertToList(policyid,demoid));
		query="INSERT INTO sfx_xacmlpolicy (policyid, policy, isactive, demoid) VALUES (?,?,1,?)";
		return DBAbstraction.insertStatement(query, DataUtil.convertToList(policyid, policy,demoid));
	}
	
	public static int uploadRequest(String demoid, String requestid, String request){
		String query="DELETE FROM sfx_xacmlrequest WHERE requestid=? AND demoid=?";
		DBAbstraction.deleteStatement(query, DataUtil.convertToList(requestid,demoid));
		query="INSERT INTO sfx_xacmlrequest (requestid, request, isactive,demoid) VALUES (?,?,1,?)";
		return DBAbstraction.insertStatement(query, DataUtil.convertToList(requestid, request,demoid));
	}
	
	public static int storeResponse(String responsetype, String requestid, String response){
		String query="INSERT INTO sfx_xacmlresponse (responsetype, response, xrid) VALUES (?,?,?)";
		return DBAbstraction.insertStatement(query, DataUtil.convertToList(responsetype,response,requestid));
	}
	
	public static int uploadTrustPolicy(String demoid, String policyid, String policy,String policyType){
		String query="DELETE FROM ext_trust_policy WHERE policyid=? AND policytype=? AND demoid=?";
		DBAbstraction.deleteStatement(query, DataUtil.convertToList(policyid,policyType,demoid));
		if(policyType.equals("reputation") || policyType.equals("similarity")){
			query="DELETE FROM ext_trust_policy WHERE policytype=? AND demoid=?";
			DBAbstraction.deleteStatement(query, DataUtil.convertToList(policyType,demoid));
		}
		query="INSERT INTO ext_trust_policy(policyid, policy, isactive,policytype, demoid) VALUES (?,?,1,?,?)";
		return DBAbstraction.insertStatement(query, DataUtil.convertToList(policyid, policy,policyType,demoid));
	}
	
	public static boolean deleteTrustPolicyBasedOnType(String demoid, String policytype){
		String query="DELETE FROM ext_trust_policy WHERE policytype=? AND demoid=?";
		return DBAbstraction.deleteStatement(query, DataUtil.convertToList(policytype,demoid));
	}
	public static String getTrustPolicyType(String tpid){
		String query="SELECT policytype FROM ext_trust_policy WHERE isactive=1 AND tpid=?";
		return DBAbstraction.selectRecord(query, DataUtil.convertToList(tpid));
	}
	public static String getTrustPolicy(String tpid){
		String query="SELECT policy FROM ext_trust_policy WHERE isactive=1 AND tpid=?";
		return DBAbstraction.selectRecord(query, DataUtil.convertToList(tpid));
	}
	public static String getTrustPolicy(String demoid,String policytype){
		String query="SELECT policy FROM ext_trust_policy WHERE isactive=1 AND demoid=? AND policytype=?";
		return DBAbstraction.selectRecord(query, DataUtil.convertToList(demoid,policytype));
	}
	public static JSONArray getTrustPolicyIds(String demoid,String policytype){
		String query="SELECT tpid, policyid FROM ext_trust_policy WHERE isactive=1 AND demoid=? AND policytype=?";
		return DBAbstraction.selectRecordsJson(query, DataUtil.convertToList(demoid,policytype));
	}
	
	public static boolean deleteXacmlPolicy(String xpid){
		String query="DELETE FROM sfx_xacmlpolicy WHERE xpid=?";
		return DBAbstraction.deleteStatement(query,DataUtil.convertToList(xpid));
	}
	
	public static boolean deleteTrustPolicy(String tpid,String demoid){
		String query="SELECT policytype FROM ext_trust_policy WHERE tpid=?";
		String policytype=DBAbstraction.selectRecord(query, DataUtil.convertToList(tpid));
		query="";
		
		if(policytype.equals("similarity"))
			query="DELETE FROM sfx_configuration WHERE demoid=? AND (configkey='similarity:alpha' OR configkey='similarity:init:vector')";
		else if(policytype.equals("reputation"))
			query="DELETE FROM sfx_configuration WHERE demoid=? "
					+ "AND (configkey='reputation:alpha' OR configkey='reputation:init:vector' OR configkey='reputation:gx' OR configkey='reputation:system:policy')";
		if(query.length()>1)
			DBAbstraction.deleteStatement(query, DataUtil.convertToList(demoid));
		
		query="DELETE FROM ext_trust_policy WHERE tpid=?";
		return DBAbstraction.deleteStatement(query,DataUtil.convertToList(tpid));
	}
	
	public static boolean deleteXacmlRequest(String xrid){
		String query="DELETE FROM sfx_xacmlrequest WHERE xrid=?";
		return DBAbstraction.deleteStatement(query, DataUtil.convertToList(xrid));
	}
	
	public static boolean updateXacmlRequest(String requestid,String request){
		String query="UPDATE sfx_xacmlrequest SET request=? WHERE xrid=?";
		return DBAbstraction.updateStatement(query,DataUtil.convertToList(request, requestid));
	}
	
	public static String updatePolicy(String resourceid, String resource, String resourcetype){
		if(resourcetype.equals("xacml")){
			String query="UPDATE sfx_xacmlpolicy SET policy=? WHERE xpid=?";
			DBAbstraction.updateStatement(query,DataUtil.convertToList(resource, resourceid));
			return resourcetype;
		}
		else{
			String query="UPDATE ext_trust_policy SET policy=? WHERE tpid=?";
			DBAbstraction.updateStatement(query,DataUtil.convertToList(resource, resourceid));
			query="SELECT policytype FROM ext_trust_policy where tpid=?";
			return DBAbstraction.selectRecord(query,DataUtil.convertToList(resourceid));
		}
	}
	
	public static String updatePolicyIDs(String resourceid, String policyid, String resourcetype){
		if(resourcetype.equals("xacml")){
			String query="UPDATE sfx_xacmlpolicy SET policyid=? WHERE xpid=?";
			DBAbstraction.updateStatement(query,DataUtil.convertToList(policyid, resourceid));
			return resourcetype;
		}
		else{
			String query="UPDATE ext_trust_policy SET policyid=? WHERE tpid=?";
			DBAbstraction.updateStatement(query,DataUtil.convertToList(policyid, resourceid));
			query="SELECT policytype FROM ext_trust_policy where tpid=?";
			return DBAbstraction.selectRecord(query,DataUtil.convertToList(resourceid));
		}
	}
	
	/********************** SERVICE SPECIFIC **************************/
	
	public static JSONArray findAllDependencyList(){		
		String query = "SELECT * from sfx_serviceregistry";
		return DBAbstraction.selectRecordsJson(query, null);
	}
	
	public static JSONArray findDependencyList(String srid){		
		String query = "SELECT * from sfx_serviceregistry WHERE srid != ? and srid NOT IN (select sdid from sfx_servicedependency where srid = ?)";
		return DBAbstraction.selectRecordsJson(query, DataUtil.convertToList(srid, srid));
	}
	
	public static JSONArray findAssignedDependencyList(String srid){		
		String query = "SELECT * from sfx_servicedependency a, sfx_serviceregistry b WHERE a.sdid = b.srid AND a.srid = ?";
		return DBAbstraction.selectRecordsJson(query, DataUtil.convertToList(srid));
	}
	
	/*
	 * - In the registration of services, symbols ":" 
	 */
	public static JSONArray getAllServices(){
		String query="SELECT a.servicename, b.siid, CONCAT(a.serviceurl,b.endpoint) as serviceurl, CONCAT(a.serviceid, ':' ,b.siname) as serviceid, a.servicecomponent, a.serviceprovider "
				+ " FROM sfx_serviceregistry a, sfx_serviceinterface b WHERE a.srid=b.srid ORDER BY a.servicecomponent ASC, serviceid ASC";
		return DBAbstraction.selectRecordsJson(query, null);
	}
	
	public static JSONArray getInterfacesOfAService(String srid){
		String query="SELECT b.siid, b.siname, CONCAT(a.serviceurl,b.endpoint) as serviceurl, b.endpoint "
				+ " FROM sfx_serviceregistry a, sfx_serviceinterface b WHERE a.srid=b.srid AND a.srid =? ORDER BY a.servicecomponent ASC, serviceid ASC";
		return DBAbstraction.selectRecordsJson(query, DataUtil.convertToList(srid));
	}
	
	public static JSONObject getOneService(String serviceid){
		String query="SELECT * "
				+ " FROM sfx_serviceregistry WHERE serviceid = ?";
		return DBAbstraction.selectRecordJson(query, DataUtil.convertToList(serviceid));
	}
	
	public static JSONObject getOneServiceInterface(String ssid){
		String query="SELECT * "
				+ " FROM sfx_serviceinterface WHERE siid = ?";
		return DBAbstraction.selectRecordJson(query, DataUtil.convertToList(ssid));
	}
		
	public static JSONArray getAllServiceRegistryInfo(){
		String query="SELECT srid, serviceid, servicename, servicecomponent, serviceprovider, serviceurl, servicedesc, isactive, isroot "
				+ " FROM sfx_serviceregistry ORDER BY servicecomponent ASC, serviceid ASC";
		return DBAbstraction.selectRecordsJson(query, null);
	}
	
	public static JSONArray fetchServiceComponents(String element){
		String query="SELECT * FROM sfx_serviceregistry WHERE isactive=1 AND issafax = 0 AND  servicecomponent=?";
		return DBAbstraction.selectRecordsJson(query, DataUtil.convertToList(element));
	}
	
	// Only get service components used in SAFAX GUI
	public static JSONArray fetchSAFAXServiceComponents(String element){
		String query="SELECT * FROM sfx_serviceregistry WHERE isactive=1 AND issafax = 1 AND  servicecomponent=?";
		return DBAbstraction.selectRecordsJson(query, DataUtil.convertToList(element));
	}
	
	public static int registryService(String serviceid, String servicename, String servicecomponent, String serviceprovider,
			String serviceurl, String servicedesc) {
		String query="INSERT INTO sfx_serviceregistry (serviceid, servicename, servicecomponent, serviceprovider, serviceurl, servicedesc, isactive) "
				+ "VALUES (?,?,?,?,?,?,1)";
		return DBAbstraction.insertStatement(query, DataUtil.convertToList(serviceid, servicename, servicecomponent, serviceprovider,
				serviceurl,servicedesc));
	}
	
	// Add new service interface into sfx_serviceinterface
	public static int registryServiceInterface(String srid, String siname, String endpoint, String interfacedesc,
			String httpmethod, String serviceparams, String returntype) {
		String query="INSERT INTO sfx_serviceinterface (srid, siname, endpoint, interfacedesc, httpmethod, serviceparams, returntype, isactive) "
				+ "VALUES (?,?,?,?,?,?,?,1)";
		return DBAbstraction.insertStatement(query, DataUtil.convertToList(srid, siname, endpoint, interfacedesc, httpmethod, serviceparams, returntype));
	}
	
	public static boolean removeAssignedServiceDependency(String srid){
		String query="DELETE FROM sfx_servicedependency WHERE srid=?";
		return DBAbstraction.updateStatement(query, DataUtil.convertToList(srid));
	}
	
	public static boolean assignServiceDependency(String srid, String siid){
		String query="INSERT INTO sfx_servicedependency VALUES(?,?)";
		return DBAbstraction.updateStatement(query, DataUtil.convertToList(srid, siid));
	}
		
	// Update existing service 
	public static boolean updateRegistryService(String srid, String serviceid, String servicename, String servicecomponent, String serviceprovider, String serviceurl, String servicedesc) {
		String query="UPDATE sfx_serviceregistry SET serviceid = ?, servicename = ?, servicecomponent = ?, serviceprovider = ?, serviceurl = ?, servicedesc = ? WHERE srid=?";
		return DBAbstraction.updateStatement(query, DataUtil.convertToList(serviceid, servicename, servicecomponent, serviceprovider, serviceurl, serviceurl, srid));
	}
	
	// Update existing service interface
	public static boolean updateRegistryServiceInterface(String siid, String srid, String siname, String endpoint, String interfacedesc,
			String httpmethod, String serviceparams, String returntype) {
		String query="UPDATE sfx_serviceinterface SET siname = ?, endpoint = ?, interfacedesc = ?, httpmethod = ?, serviceparams = ?, returntype = ? WHERE siid=?";
		return DBAbstraction.updateStatement(query, DataUtil.convertToList(siname, endpoint, interfacedesc, httpmethod, serviceparams, returntype, siid));
	}
	
	public static boolean removeExistingRegistryService(String serviceid) {
		String query="DELETE FROM sfx_serviceregistry WHERE serviceid=?";
		return DBAbstraction.deleteStatement(query,DataUtil.convertToList(serviceid));
	}
	
	public static boolean removeExistingRegistryServiceInterface(String siid) {
		String query="DELETE FROM sfx_serviceinterface WHERE siid=?";
		return DBAbstraction.deleteStatement(query,DataUtil.convertToList(siid));
	}
	
	public static boolean removeExistingRegistryServiceDependence(String serviceid) {
		String query="DELETE FROM sfx_servicedependency WHERE srid=?";
		return DBAbstraction.deleteStatement(query,DataUtil.convertToList(serviceid));
	}
		
	public static String getServiceInfo(String serviceid,String element){
		String query="SELECT CONCAT(a.serviceurl,b.endpoint) FROM sfx_serviceregistry a, sfx_serviceinterface b WHERE a.srid=b.srid AND a.serviceid=? AND b.siname=?";
		return DBAbstraction.selectRecord(query,DataUtil.convertToList(serviceid,element));
	}
	
	public static String getTransactionIDOfUconSession(String ucon_session){
		String query = "SELECT transactionid FROM sfx_ucon_request WHERE ucon_sessionid = ?";
		return DBAbstraction.selectRecord(query,DataUtil.convertToList(ucon_session));
	}
	
	/* This version is not correct as it returns more than once service id */
	/* SAMUEL error 
	public static String getServiceIDForCode(String pdpcode, String component){
		String query="SELECT serviceid FROM sfx_pdp a, sfx_configuration b, sfx_serviceregistry c  "
				+ "WHERE a.demoid=b.demoid AND b.configkey=c.servicecomponent AND a.pdpcode=? AND b.configkey=?";
		return DBAbstraction.selectRecord(query, DataUtil.convertToList(pdpcode,component));
	}
	*/
	
	/*
	 * Fixed on 27 October 2015 by Duc Luu
	 */
	public static String getServiceIDForCode(String pdpcode, String component){
		String query="SELECT serviceid FROM sfx_pdp a, sfx_configuration b, sfx_serviceregistry c  "
				+ "WHERE a.demoid=b.demoid AND b.configkey=c.servicecomponent AND b.configvalue = c.serviceid AND a.pdpcode=? AND b.configkey=?";
		return DBAbstraction.selectRecord(query, DataUtil.convertToList(pdpcode,component));
	}

	/********************** SESSION SPECIFIC **************************/
	public static JSONArray getTransactionLog(String uaid){
		String query="SELECT b.transactionid,b.log,b.logType,b.component FROM sfx_action a, sfx_log b WHERE a.actionid=b.transactionid AND a.uaid=?";
		return DBAbstraction.selectRecordsJson(query, DataUtil.convertToList(uaid));
	}
	
	
	public static JSONArray getSessionActions(String sessionid,String action){
		String query="SELECT a.uaid, a.actionid FROM sfx_action a, sfx_usersession b WHERE a.usid=b.usid AND a.action=? AND b.sessionid=? ORDER BY a.uaid DESC";
		return DBAbstraction.selectRecordsJson(query, DataUtil.convertToList(action,sessionid));
	}
	
	public static void deleteResources(){
		String query="DELETE FROM sfx_demo WHERE isactive=0 AND demoid IN (SELECT a.actionresource FROM sfx_action a, sfx_usersession b WHERE a.usid=b.usid AND a.action='delete' AND a.actionresourcetype='demo'  AND b.isactive=0)";
		DBAbstraction.deleteStatement(query, null);
		query="DELETE FROM sfx_project WHERE isactive=0 AND prid IN (SELECT a.actionresource FROM sfx_action a, sfx_usersession b WHERE a.usid=b.usid AND a.action='delete' AND a.actionresourcetype='project' AND b.isactive=0)";
		DBAbstraction.deleteStatement(query, null);
	}
	
	
	public static JSONArray getPastActions(String username, String sessionid,String action){
		String query="SELECT a.uaid, a.actionid FROM sfx_action a, sfx_usersession b, sfx_user c WHERE a.usid=b.usid AND b.uid=c.uid AND a.action=? AND c.uname=? AND b.sessionid!=? ORDER BY a.uaid DESC LIMIT 0,20";
		return DBAbstraction.selectRecordsJson(query,DataUtil.convertToList(action,username,sessionid));
	}
	
	public static JSONObject getDeletedActions(String actionid){
		String query="SELECT actionresourcetype FROM sfx_action a WHERE uaid=?";
		String type=DBAbstraction.selectRecord(query, DataUtil.convertToList(actionid));
		
		if(type.equals("demo"))
			query="SELECT a.uaid, a.actionid, a.actionresourcetype, a.starttime, b.demoname as resourcename "
					+ " FROM sfx_action a, sfx_demo b WHERE a.actionresource=b.demoid AND a.uaid=?";
		else
			query="SELECT a.uaid, a.actionid, a.actionresourcetype, a.starttime, b.projectname as resourcename"
					+ " FROM sfx_action a, sfx_project b WHERE a.actionresource=b.prid AND a.uaid=?";
		return DBAbstraction.selectRecordJson(query,DataUtil.convertToList(actionid));
	}
	
	public static String undoDelete(String actionid){
		String query="SELECT actionresourcetype FROM sfx_action a WHERE uaid=?";
		String type=DBAbstraction.selectRecord(query, DataUtil.convertToList(actionid));
		
		if(type.equals("demo"))
			query="UPDATE sfx_demo a, sfx_action b SET a.isactive=1, a.demoname=CONCAT('Restored - ',a.demoname)  "
					+ "WHERE b.actionresource=a.demoid AND b.uaid=?";
		else
			query="UPDATE sfx_project a, sfx_action b SET a.isactive=1, a.projectname=CONCAT('Restored - ',a.projectname)  "
					+ "WHERE b.actionresource=a.prid AND b.uaid=?";
		if(DBAbstraction.updateStatement(query,DataUtil.convertToList(actionid))){
			query="DELETE FROM sfx_action WHERE uaid=?";
			DBAbstraction.deleteStatement(query,DataUtil.convertToList(actionid));
			return DataUtil.generalResponse("true","Resource Restored Successfully", "");
		}
		else
			return DataUtil.generalResponse("false","Error in restoring resource", "");
	}

	public static String recordAction(String sessID,String action,String resource, String resourcetype){
		String uuid =action+"_"+UUID.randomUUID().toString().replaceAll("-", "");
		long tstamp= System.currentTimeMillis();
		String query="INSERT INTO sfx_action (actionid,action,actionresource,actionresourcetype,starttime,isactive,usid) "
				+ "VALUES (?,?,?,?,?,1,?)";
		int sid=getSessionID(sessID);
		if(sid<1)
			return null;
		if(DBAbstraction.insertStatement(query, DataUtil.convertToList(
										uuid,action,resource,resourcetype,Long.toString(tstamp), Integer.toString(sid)))>0)
			return uuid;
		else
			return null;
	}
	
	/*
	 * safax.sfx_ucon_action
	 * A UCON session starts when users first evaluate a UCON request
	 * During a UCON session there can be many request
	 * A UCON session ends when users click restart UCON session
	 */
	public static String recordUCONAction(String sessID, String ucon_session){
		long tstamp= System.currentTimeMillis();
		String query="INSERT INTO sfx_ucon_action (ucon_sessionid, starttime) "
				+ "VALUES (?,?)";
		int sid=getSessionID(sessID);
		if(sid<1)
			return null;
		if(DBAbstraction.insertStatement(query, DataUtil.convertToList(ucon_session, Long.toString(tstamp))) > 0)
			return null;
		else
			return null;
	}
	
	public static String recordDetailedAction(String transactionid, String sessID, String action, String actiondesc, String serviceid, String resource, String resourcetype){
		String query="INSERT INTO sfx_action_details (actionid, action, actiondesc, serviceid, actionresource, actionresourcetype, isactive, usid) "
				+ "VALUES (?,?,?,?,?,?,1,?)";
		int sid=getSessionID(sessID);
		if(sid<1)
			return null;
		if(DBAbstraction.insertStatement(query, DataUtil.convertToList(
				transactionid, action, actiondesc, serviceid, resource, resourcetype, Integer.toString(sid)))>0)
			return transactionid;
		else
			return null;
	}
	
	/*
	 * Future USE
	public static String recordDetailedUCONAction(String transactionid, String sessID, String action, String actiondesc, String serviceid, String resource, String resourcetype, String ucon_session){
		String query="INSERT INTO sfx_action_details (actionid, action, actiondesc, serviceid, actionresource, actionresourcetype, isactive, usid, ucon_sessionid) "
				+ "VALUES (?,?,?,?,?,?,1,?, ?)";
		int sid=getSessionID(sessID);
		if(sid<1)
			return null;
		if(DBAbstraction.insertStatement(query, DataUtil.convertToList(
				transactionid, action, actiondesc, serviceid, resource, resourcetype, Integer.toString(sid), ucon_session))>0)
			return transactionid;
		else
			return null;
	}
	*/
	
	// Start recording an instance of service in sfx_action_details table - update starttime
	public static boolean startDetailedAction(String actionid, String pdpcode, String component){
		long tstamp= System.currentTimeMillis();
		String serviceid = DBFns.getServiceIDForCode(pdpcode, component);
		String query="UPDATE sfx_action_details SET starttime=?, isactive=0 WHERE actionid=? and serviceid = ?";
		return DBAbstraction.updateStatement(query, DataUtil.convertToList(Long.toString(tstamp),actionid, serviceid));
	}
	
	// Stop recording an instance of service in sfx_action_details table - update endtime
	public static boolean stopDetailedAction(String actionid, String pdpcode, String component){
		long tstamp= System.currentTimeMillis();	
		String serviceid = DBFns.getServiceIDForCode(pdpcode, component);	
		String query="UPDATE sfx_action_details SET stoptime=?, isactive=0 WHERE actionid=? and serviceid = ?";				
		return DBAbstraction.updateStatement(query, DataUtil.convertToList(Long.toString(tstamp),actionid, serviceid));
	}
	
	// Stop recording a UCON session - update endtime
	public static boolean stopUCONAction(String ucon_session){
		long tstamp= System.currentTimeMillis();	
		String query="UPDATE sfx_ucon_action SET endtime = ?, isdown = 0 WHERE ucon_sessionid = ? and endtime is NULL";				
		return DBAbstraction.updateStatement(query, DataUtil.convertToList(Long.toString(tstamp), ucon_session));
	}
	
	public static boolean stopAction(String actionid){
		long tstamp= System.currentTimeMillis();
		String query="UPDATE sfx_action SET endtime=?, isactive=0 WHERE actionid=?";
		return DBAbstraction.updateStatement(query, DataUtil.convertToList(Long.toString(tstamp),actionid));
	}
	
	public static int getSessionID(String sessID){
		String query="SELECT usid FROM sfx_usersession WHERE sessionid=?";
		String sessionid=DBAbstraction.selectRecord(query,DataUtil.convertToList(sessID));
		if(sessionid!=null)
			return Integer.parseInt(sessionid);
		else
			return 0;
	}
	public static boolean closeSession(String sessionid){
		long unixTime = System.currentTimeMillis();
		String query="UPDATE sfx_usersession SET isactive=0,sessionend=? WHERE sessionid=?";
		return DBAbstraction.updateStatement(query, DataUtil.convertToList(Long.toString(unixTime),sessionid));
	}
	
	public static boolean closeTransaction(String transactionid){
		String query="UPDATE sfx_transaction SET isactive=0 WHERE transactionid=?";
		if(DBAbstraction.updateStatement(query, DataUtil.convertToList(transactionid)))
			return true;
		else
			return false;
	}
	
	public static boolean closeInactiveSessions(){
		long tstamp= System.currentTimeMillis();
		String query="UPDATE sfx_usersession SET sessionend="+tstamp+", isactive=0 WHERE ("+tstamp+"-lasttransaction)>4000000";
		return DBAbstraction.updateStatement(query, null);
	}
	
	public static void closeGuestSessions(){
		String query="SELECT a.uid FROM sfx_user a, sfx_usergroup b, sfx_group c, sfx_usersession d WHERE a.uid=b.uid AND b.gid=c.gid AND c.groupname=? AND a.uid=d.uid AND d.isactive=0";
		for(String uid: DBAbstraction.selectRecords(query, DataUtil.convertToList("guest"))){
			query="DELETE from sfx_user WHERE uid=?";
			DBAbstraction.deleteStatement(query, DataUtil.convertToList(uid));
			
			query="DELETE FROM sfx_projectuser WHERE uid=?";
			DBAbstraction.deleteStatement(query, DataUtil.convertToList(uid));
			
			query="DELETE from sfx_demo WHERE demoowner=?";
			DBAbstraction.deleteStatement(query, DataUtil.convertToList(uid));
		}
	}
	
	/********************** ADMIN SPECIFIC **************************/	
	public static boolean isUserAdmin(String username){
		String query="SELECT a.uid FROM sfx_user a, sfx_usergroup b, sfx_group c WHERE a.uid=b.uid AND b.gid=c.gid AND a.uname=? AND c.groupname=?";
		String uid=DBAbstraction.selectRecord(query, DataUtil.convertToList(username,"admin"));
		if(uid!=null && uid.length()>0) return true;
		return false;
	}
	public static ArrayList<String> getAdminEmails(){
		String query="SELECT a.email FROM sfx_user a, sfx_usergroup b, sfx_group c WHERE a.uid=b.uid AND b.gid=c.gid AND c.groupname=?";
		return DBAbstraction.selectRecords(query, DataUtil.convertToList("admin"));
	}
	
	public static JSONArray getUserActivationRequests(){
		String query="SELECT * FROM sfx_user WHERE isactive=0";
		return DBAbstraction.selectRecordsJson(query, null);
	}
	public static boolean activateUser(String newuserid){
		String query="UPDATE sfx_user SET isactive=1 WHERE uid=?";
		return DBAbstraction.updateStatement(query, DataUtil.convertToList(newuserid));
	}
	
	public static boolean removeUser(String userid){
		String query="DELETE FROM sfx_user WHERE uid=?";
		return DBAbstraction.updateStatement(query, DataUtil.convertToList(userid));
	}
	
	/********************** STATISTICS SPECIFIC **************************/		
	public static JSONObject countUserProjects(String userid){
		String query="SELECT COUNT(*) FROM sfx_project WHERE projectowner=? AND isactive=1";
		return DBAbstraction.selectRecordJson(query, DataUtil.convertToList(userid));
	}
	public static JSONObject countUserInactiveProjects(String userid){
		String query="SELECT COUNT(*) FROM sfx_project WHERE projectowner=? AND isactive=0";
		return DBAbstraction.selectRecordJson(query, DataUtil.convertToList(userid));
	}
	
	public static JSONObject countUserDemos(String userid){
		String query="SELECT COUNT(*) FROM sfx_demo a, sfx_project b WHERE a.prid=b.prid AND b.isactive=1 AND demoowner=? AND isactive=1";
		return DBAbstraction.selectRecordJson(query, DataUtil.convertToList(userid));
	}
	
	public static JSONObject countUserInactiveDemos(String userid){
		String query="SELECT COUNT(*) FROM sfx_demo a, sfx_project b WHERE a.prid=b.prid AND b.isactive=0 AND demoowner=? AND isactive=1";
		return DBAbstraction.selectRecordJson(query, DataUtil.convertToList(userid));
	}
	
	public static JSONObject countUsers(){
		String query="SELECT COUNT(*) FROM sfx_user WHERE isactive=1";
		return DBAbstraction.selectRecordJson(query, null);
	}
	
	public static JSONObject countGroupUsers(String group){
		String query="";
		
		if(group.equals("guest")){
			query="SELECT COUNT(*) as num FROM sfx_usersession WHERE isguest=1";
			return DBAbstraction.selectRecordJson(query,null);
		}
		
		query="SELECT COUNT(*) as num FROM sfx_user a, sfx_group b, sfx_usergroup c WHERE a.isactive=1 AND a.uid=c.uid AND b.gid=c.gid AND b.groupname=?";
		return DBAbstraction.selectRecordJson(query,DataUtil.convertToList(group));
	}
	
	// Not including guest
	public static JSONObject countAllRegisteredUsers(){
		// groupdid 0000000006 is guest
		String query="SELECT count(distinct(uid)) as num FROM sfx_usergroup WHERE gid != 0000000006";
		return DBAbstraction.selectRecordJson(query, null);
	}
	
	public static JSONObject countAllActiveGuestUsers(){
		// groupdid 0000000006 is guest
		String query="SELECT count(distinct(uid)) as num FROM sfx_usergroup WHERE gid = 0000000006";
		return DBAbstraction.selectRecordJson(query, null);
	}
	
	
	public static JSONObject countDemos(){
		String query="SELECT COUNT(*) as num FROM sfx_demo WHERE isactive=1";
		return DBAbstraction.selectRecordJson(query, null);
	}
		
	public static JSONObject countProjects(){
		String query="SELECT COUNT(*) as num FROM sfx_project WHERE isactive=1";
		return DBAbstraction.selectRecordJson(query, null);
	}
	
	public static JSONObject countTrustDemos(){
		String query="SELECT COUNT( DISTINCT a.demoid) as num FROM ext_trust_policy a, sfx_demo b WHERE a.demoid=b.demoid AND b.isactive=1";
		return DBAbstraction.selectRecordJson(query, null);
	}
	public static JSONObject countRequestDemos(){
		String query="SELECT COUNT( DISTINCT a.demoid) as num FROM sfx_xacmlrequest a, sfx_demo b WHERE a.demoid=b.demoid AND b.isactive=1";
		return DBAbstraction.selectRecordJson(query, null);
	}
	public static JSONObject averageSfxEval(){
		String query="SELECT (AVG(endtime-starttime)/1000) as average FROM sfx_action WHERE action='sfxeval' AND isactive=0";
		return DBAbstraction.selectRecordJson(query, null);
	}
	
	// All users, from beginning to today
	public static JSONObject averageServiceComponentEval(String servicecomponent, String serviceid){
		String query="SELECT COUNT(endtime) as num, (AVG(endtime-starttime)/1000) as average FROM sfx_action_details WHERE action='evaluate' AND isactive=0 AND actiondesc = ? AND serviceid = ?";
		return DBAbstraction.selectRecordJson(query, DataUtil.convertToList(servicecomponent, serviceid));
	}
	
	// All users and chosen services instances, from beginning to today
	public static JSONObject averageServiceComponentEval(String servicecomponent, String starttime, String endtime, String serviceid){
		String query="SELECT COUNT(endtime) as num, (AVG(endtime-starttime)/1000) as average FROM sfx_action_details as a INNER JOIN sfx_usersession as b ON a.usid = b.usid WHERE action='evaluate' AND a.isactive=0 AND starttime >=? AND endtime <= ? AND actiondesc = ? AND serviceid = ?";
		return DBAbstraction.selectRecordJson(query, DataUtil.convertToList(starttime, endtime, servicecomponent, serviceid));
	}
	
	// All users and chosen services instances, from beginning to today
	public static JSONObject averageTotalUCONEvaluation(){				
		String query="SELECT COUNT(endtime) as num, (AVG(endtime-starttime)/1000) as average FROM sfx_ucon_action WHERE endtime is NOT NULL AND isdown = 0";
		return DBAbstraction.selectRecordJson(query, null);
	}
	
	// All users and chosen services instances, from beginning to today
	public static JSONObject averageTotalEvaluationMongoDB(List<String> pdp_serviceid, List<String> pep_serviceid, List<String> ch_serviceid, List<String> pip_serviceid, List<String> udf_serviceid, String starttime, String endtime, String uid){
		/*
		 * ToDo
		 */
		JSONObject jo=new JSONObject();
		
		if (pep_serviceid.size() == 0 || pdp_serviceid.size() == 0 || ch_serviceid.size() == 0) {
			jo.put("num", 0);
			jo.put("average", null);
			jo.put("average_pdp_service", null);
			jo.put("average_pep_service", null);
			jo.put("average_ch_service", null);
			jo.put("average_pip_service", null);
			jo.put("average_udf_service", null);
			
			return jo;
		}

		Document[] pep_serviceid_documents_list = new Document[pep_serviceid.size()];
		for (int i = 0; i < pep_serviceid.size(); i++) {
			Document pepfilter = new Document("pepserviceid", pep_serviceid.get(i));
			pep_serviceid_documents_list[i] = pepfilter;
		}
		
		Document[] pdp_serviceid_documents_list = new Document[pdp_serviceid.size()];
		for (int i = 0; i < pdp_serviceid.size(); i++) {
			Document pdpfilter = new Document("pdpserviceid", pdp_serviceid.get(i));
			pdp_serviceid_documents_list[i] = pdpfilter;
		}
		
		Document[] ch_serviceid_documents_list = new Document[ch_serviceid.size()];
		for (int i = 0; i < ch_serviceid.size(); i++) {
			Document chfilter = new Document("chserviceid", ch_serviceid.get(i));
			ch_serviceid_documents_list[i] = chfilter;
		}
		
		Boolean pipMandatory = true;
		
		Document[] pip_serviceid_documents_list = new Document[pip_serviceid.size()];
		
		if (pip_serviceid.size() != 0) {
			for (int i = 0; i < pip_serviceid.size(); i++) {
				if (pip_serviceid.get(i).equalsIgnoreCase("pip_serviceid_none")) {
					pipMandatory = false;
				}
				Document pipfilter = new Document("pipserviceid", pip_serviceid.get(i));
				pip_serviceid_documents_list[i] = pipfilter;
			}			
		}
		else {
			pipMandatory = false;	
		}

		Boolean udfMandatory = true;
		
		Document[] udf_serviceid_documents_list = new Document[udf_serviceid.size()];
		
		if (udf_serviceid.size() != 0) {
			for (int i = 0; i < udf_serviceid.size(); i++) {
				if (udf_serviceid.get(i).equalsIgnoreCase("udf_serviceid_none")) {
					udfMandatory = false;
				}
				Document udffilter = new Document("udfs.udfserviceid", udf_serviceid.get(i));
				udf_serviceid_documents_list[i] = udffilter;
			}
		}
		else {
			udfMandatory = false;
		}

		Document[] user_id_documents_list = new Document[1];
		if (uid.equalsIgnoreCase("-1")) {
			Document userfilter = new Document("userid", new Document("$exists", true));
			user_id_documents_list[0] = userfilter;
		}
		else {
			Document userfilter = new Document("userid", uid);
			user_id_documents_list[0] = userfilter;
		}
		
		Document filter = null;
		
		/*
		 * None in PIP and UDF checkbox is unchecked
		 */
		if (pipMandatory && udfMandatory) {
			 filter = new Document ("$match", new Document("$and", asList(new Document("evalstarttime", new Document("$gte", new Date(Long.valueOf(starttime)))),
					 													  new Document("evalendtime", new Document("$lte", new Date(Long.valueOf(endtime)))),
					 													  new Document("$or", asList(pep_serviceid_documents_list)), 
                                                                          new Document("$or", asList(pdp_serviceid_documents_list)),
                                                                          new Document("$or", asList(ch_serviceid_documents_list)),
                                                                          new Document("$or", asList(pip_serviceid_documents_list)),
                                                                          new Document("$or", asList(udf_serviceid_documents_list)),
                                                                          new Document("$or", asList(user_id_documents_list)))));
		}
		else if (pipMandatory && !udfMandatory &&  udf_serviceid.size() == 0) {
			 filter = new Document ("$match", new Document("$and", asList(new Document("evalstarttime", new Document("$gte", new Date(Long.valueOf(starttime)))),
					 													  new Document("evalendtime", new Document("$lte", new Date(Long.valueOf(endtime)))),
					 													  new Document("$or", asList(pep_serviceid_documents_list)), 
					 									                  new Document("$or", asList(pdp_serviceid_documents_list)),
                                                                          new Document("$or", asList(ch_serviceid_documents_list)),
                                                                          new Document("$or", asList(pip_serviceid_documents_list)),
		 															      new Document ("udfs.udfserviceid", new Document ("$exists", false))))); // evaluation does not contain any udf
		}
		else if (pipMandatory && !udfMandatory &&  udf_serviceid.size() != 0) {
			 filter = new Document ("$match", new Document("$and", asList(new Document("evalstarttime", new Document("$gte", new Date(Long.valueOf(starttime)))),
					 													  new Document("evalendtime", new Document("$lte", new Date(Long.valueOf(endtime)))),
					 													  new Document("$or", asList(pep_serviceid_documents_list)), 
					 									                  new Document("$or", asList(pdp_serviceid_documents_list)),
                                                                          new Document("$or", asList(ch_serviceid_documents_list)),
                                                                          new Document("$or", asList(pip_serviceid_documents_list)),
                                                                          new Document("$or", asList ( new Document ("udfs.udfserviceid", new Document ("$exists", false)), // evaluation does not contain any udf
         				 					  		                              			           new Document("$or", asList(udf_serviceid_documents_list)))),
         				 					  		                      new Document("$or", asList(user_id_documents_list))))); 
		}
		else if (!pipMandatory && udfMandatory && pip_serviceid.size() == 0) {
			 filter = new Document ("$match", new Document("$and", asList(new Document("evalstarttime", new Document("$gte", new Date(Long.valueOf(starttime)))),
					 													  new Document("evalendtime", new Document("$lte", new Date(Long.valueOf(endtime)))),
					 													  new Document("$or", asList(pep_serviceid_documents_list)), 
					 									                  new Document("$or", asList(pdp_serviceid_documents_list)),
                                                                          new Document("$or", asList(ch_serviceid_documents_list)),
		 																  new Document ("pipserviceid", new Document ("$exists", false)),
                                                                          new Document("$or", asList(udf_serviceid_documents_list)),
                                                                          new Document("$or", asList(user_id_documents_list))))); 
		}
		else if (!pipMandatory && udfMandatory && pip_serviceid.size() != 0) {
			 filter = new Document ("$match", new Document("$and", asList(new Document("evalstarttime", new Document("$gte", new Date(Long.valueOf(starttime)))),
					 										              new Document("evalendtime", new Document("$lte", new Date(Long.valueOf(endtime)))),
					 										              new Document("$or", asList(pep_serviceid_documents_list)), 
					 									                  new Document("$or", asList(pdp_serviceid_documents_list)),
                                                                          new Document("$or", asList(ch_serviceid_documents_list)),
                                                                          new Document("$or", asList ( 
		 																	   			    new Document ("pipserviceid", new Document ("$exists", false)),
                                                               		                        new Document("$or", asList(pip_serviceid_documents_list)))),
                                                                          new Document("$or", asList(udf_serviceid_documents_list)))));
		}
		else if (!pipMandatory && !udfMandatory && pip_serviceid.size() != 0 && udf_serviceid.size() != 0) {			
			filter = new Document ("$match", new Document("$and", asList(new Document("evalstarttime", new Document("$gte", new Date(Long.valueOf(starttime)))),
																		 new Document("evalendtime", new Document("$lte", new Date(Long.valueOf(endtime)))),
																		 new Document("$or", asList(pep_serviceid_documents_list)), 
																		 new Document("$or", asList(pdp_serviceid_documents_list)),
													                     new Document("$or", asList(ch_serviceid_documents_list)),
													            		 new Document("$or", asList ( new Document ("pipserviceid", new Document ("$exists", false)),
													            				 					  new Document("$or", asList(pip_serviceid_documents_list)))), // evaluation does not contain any pip
 				            				 					  		 new Document("$or", asList ( new Document ("udfs.udfserviceid", new Document ("$exists", false)),
 				            				 					  		                              new Document("$or", asList(udf_serviceid_documents_list)))),
 				            				 					  		 new Document("$or", asList(user_id_documents_list))))); // evaluation does not contain any udf
		}
		else if (!pipMandatory && !udfMandatory && pip_serviceid.size() == 0 && udf_serviceid.size() != 0) {
			filter = new Document ("$match", new Document("$and", asList(new Document("evalstarttime", new Document("$gte", new Date(Long.valueOf(starttime)))),
					 													 new Document("evalendtime", new Document("$lte", new Date(Long.valueOf(endtime)))),
					 													 new Document("$or", asList(pep_serviceid_documents_list)), 
					 													 new Document("$or", asList(pdp_serviceid_documents_list)),
					 													 new Document("$or", asList(ch_serviceid_documents_list)),
					 													 new Document("$or", asList ( new Document ("pipserviceid", new Document ("$exists", false)))), // evaluation does not contain any pip
					 													 new Document("$or", asList ( 
					 																	   			    new Document ("udfs.udfserviceid", new Document ("$exists", false)),
                                                                               		                    new Document("$or", asList(udf_serviceid_documents_list))))))); // evaluation does not contain any udf
		}
		else if (!pipMandatory && !udfMandatory && pip_serviceid.size() != 0 && udf_serviceid.size() == 0) {
			filter = new Document ("$match", new Document("$and", asList(new Document("evalstarttime", new Document("$gte", new Date(Long.valueOf(starttime)))),
																		 new Document("evalendtime", new Document("$lte", new Date(Long.valueOf(endtime)))),
																		 new Document("$or", asList(pep_serviceid_documents_list)), 
					 													 new Document("$or", asList(pdp_serviceid_documents_list)),
					 													 new Document("$or", asList(ch_serviceid_documents_list)),
					 													 new Document("$or", asList ( 
		 																	   			    new Document ("pipserviceid", new Document ("$exists", false)),
                                                               		                        new Document("$or", asList(pip_serviceid_documents_list)))), // evaluation does not contain any pip
                                                               		     new Document("$or", asList (new Document ("udfs.udfserviceid", new Document ("$exists", false))))))); // evaluation does not contain any udf
		}
		else if (!pipMandatory && !udfMandatory && pip_serviceid.size() == 0 && udf_serviceid.size() == 0) {
			filter = new Document ("$match", new Document("$and", asList(new Document("evalstarttime", new Document("$gte", new Date(Long.valueOf(starttime)))),
					 												     new Document("evalendtime", new Document("$lte", new Date(Long.valueOf(endtime)))),
					 												     new Document("$or", asList(pep_serviceid_documents_list)), 
					 													 new Document("$or", asList(pdp_serviceid_documents_list)),
					 													 new Document("$or", asList(ch_serviceid_documents_list)),
					 													 new Document("$or", asList (new Document ("pipserviceid", new Document ("$exists", false)))), // evaluation does not contain any pip
					 													 new Document("$or", asList (new Document ("udfs.udfserviceid", new Document ("$exists", false))))))); // evaluation does not contain any udf
		}
 
		/*
		 * Except UDF
		 */
        Document average = null;

        if (pip_serviceid.size() != 0) {
        	 average = new Document ("$group", new Document("_id", null)
				.append("num", new Document("$sum", 1))
				.append("average", new Document("$avg","$evalduration"))								
				.append("average_pep_service", new Document("$avg","$pepduration"))
				.append("average_pdp_service", new Document("$avg","$pdpduration"))
				.append("average_ch_service", new Document("$avg","$chduration"))
				.append("average_pip_service", new Document("$avg","$pipduration")));
        }
        else {
        	 average = new Document ("$group", new Document("_id", null)
				.append("num", new Document("$sum", 1))
				.append("average", new Document("$avg","$evalduration"))								
				.append("average_pep_service", new Document("$avg","$pepduration"))
				.append("average_pdp_service", new Document("$avg","$pdpduration"))
				.append("average_ch_service", new Document("$avg","$chduration")));
        }
        
        /*
         * As a udfs fields contains a collection of UDF instances (an evaluation can use zero or many external services
         */
        Document unwind_average = new Document ("$unwind", "$udfs");
        Document group_after_unwind = new Document ("$group", new Document("_id", "$actionid")
										.append("num", new Document("$sum", 1))
										.append("NEW_udfduration", new Document("$sum","$udfs.udfduration")));
        
        /* should return udf duration zero for non udf
        Document udf_average_group_after_unwind = new Document ("$group", new Document("_id", null)
										.append("num", new Document("$sum", 1))
										.append("average_udf_service", new Document("$avg","$NEW_udfduration"))); */
										
										
		Document udf_average_group_after_unwind = new Document ("$group", new Document("_id", null)
										.append("num", new Document("$sum", 1))
										.append("total_sum", new Document("$sum","$NEW_udfduration")));

        MongoCollection collection = null;
        /*
         * For Debug
         */
        try {
        	collection =  MongoDBConnection.connectToMongo();
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
        
		AggregateIterable<Document> aggregationDebugResult = collection.aggregate(asList(filter));
		for (Document cur : aggregationDebugResult) {
     		System.out.println("Debug aggregation MongoDB");
     		System.out.println(cur.toJson()); 
    	}
        
		AggregateIterable<Document> aggregationResult = collection.aggregate(asList(filter, average));
        
        JSONObject jsonObject = null;
     	for (Document cur : aggregationResult) {
     		System.out.println(cur.toJson()); 
     		JSONParser parser = new JSONParser();
     		try {
     			Object obj = parser.parse(cur.toJson());
     			jsonObject = (JSONObject) obj;
     			System.out.println("jsonObject inside aggregationResult");
     			System.out.println(jsonObject);
     		} catch (ParseException e) {
     			// TODO Auto-generated catch block
     			e.printStackTrace();
     		}
    	}
        
        /*
         * For UDF average
         */
        AggregateIterable<Document> aggregationUnWindResult = collection.aggregate(asList(filter, unwind_average));
        AggregateIterable<Document> aggregationUnWindAndGroupResult = collection.aggregate(asList(filter, unwind_average, group_after_unwind));	
        AggregateIterable<Document> aggregationUDFAverageUnWindAndGroupResult = collection.aggregate(asList(filter, unwind_average, group_after_unwind, udf_average_group_after_unwind));

		System.out.println("UNWIND");
		for (Document cur : aggregationUnWindResult) {
			System.out.println(" " + cur.toJson()); 
		}
		System.out.println("UNWIND and GROUP");
		for (Document cur : aggregationUnWindAndGroupResult) {
		
			System.out.println(" " + cur.toJson()); 
		}
		System.out.println("AVERAGE UDF -- UNWIND and GROUP");
		for (Document cur : aggregationUDFAverageUnWindAndGroupResult) {
			System.out.println(" " + cur.toJson()); 
			JSONParser parser = new JSONParser();
			try {
				Object obj = parser.parse(cur.toJson());
				JSONObject udfJSONObject = (JSONObject) obj;
				if (jsonObject != null) {
					//jsonObject.put("average_udf_service", udfJSONObject.get("average_udf_service"));
					Double averageUDFService = Double.valueOf(udfJSONObject.get("total_sum").toString()) / Double.valueOf(jsonObject.get("num").toString());
					jsonObject.put("average_udf_service", averageUDFService);
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (pip_serviceid.size() == 0 && jsonObject != null) {
			jsonObject.put("average_pip_service", null);
	    }
				
		/*
		 * Return null result
		 */
		if (jsonObject == null) {
			jsonObject = new JSONObject();
			jsonObject.put("num", 0);
			jsonObject.put("average", null);
			jsonObject.put("average_pdp_service", null);
			jsonObject.put("average_pep_service", null);
			jsonObject.put("average_ch_service", null);
			jsonObject.put("average_pip_service", null);
			jsonObject.put("average_udf_service", null);
		}
		/*
		 * 	" SELECT count(a.actionid) as num, (AVG(endtime - starttime)/1000) as average, " + 
														  "(AVG(pdp_service_endtime - pdp_service_starttime)/1000) as average_pdp_service, " + 
														  "(AVG(pep_service_endtime - pep_service_starttime)/1000) as average_pep_service, " + 
														  "(AVG(ch_service_endtime - ch_service_starttime)/1000) as average_ch_service, " + 
														  "(AVG(pip_service_endtime - pip_service_starttime)/1000) as average_pip_service, " +
														  "(AVG(udf_endtime - udf_starttime)/1000) as average_udf_service " + 
		 */
		return jsonObject;		
	}
	
	// All users and chosen services instances, from beginning to today
	public static JSONObject averageTotalEvaluation(List<String> pdp_serviceid, List<String> pep_serviceid, List<String> ch_serviceid, List<String> pip_serviceid, List<String> udf_serviceid, String starttime, String endtime, String uid){				
			List<String> totalList = new ArrayList<String>();
				/*
				 *  SAMPLE QUERY
				 * 	SELECT pdp_serviceid, pep_serviceid, pip_serviceid
					FROM (SELECT usid, actionid, serviceid as pdp_serviceid FROM safax.sfx_action_details WHERE actiondesc = 'pdp'  and action = 'evaluate' AND isactive=0 and serviceid = 'nl:tue:sec:safax:pdp') a 
					INNER JOIN (SELECT action, actionid, serviceid as pep_serviceid FROM safax.sfx_action_details WHERE actiondesc = 'pep' and action = 'evaluate' AND isactive=0 and serviceid = 'nl:tue:sec:pep' or serviceid = 'nl:tue:sec:safax:pep') b 
					ON a.actionid = b.actionid 
					INNER JOIN (SELECT action, actionid, serviceid as pip_serviceid FROM safax.sfx_action_details WHERE actiondesc = 'pip' and action = 'evaluate' AND isactive=0 and serviceid = 'nl:tue:sec:pip_1' or serviceid = 'nl:tue:sec:pip_2' or serviceid = 'nl:tue:sec:safax:pip_2') c
					ON b.actionid = c.actionid 
					INNER JOIN (SELECT actionid, starttime, endtime FROM safax.sfx_action WHERE action = 'evaluate' or action = 'sfxeval' AND isactive=0) e 
					ON c.actionid = e.actionid 
				 */
				
				String query=
						//" SELECT count(endtime) as num, (AVG(endtime-starttime)/1000) as average " +
						// For Debug
						//" SELECT a.actionid, starttime, endtime, pdp_service_starttime, pdp_service_endtime, pep_service_starttime, pep_service_endtime, ch_service_starttime, ch_service_endtime, pip_service_starttime, pip_service_endtime, udf_starttime, udf_endtime " +
						
						// For getting average and count
						// Any evaluation must contain pdp service -> a.actionid as num = the total number of evaluation
						" SELECT count(a.actionid) as num, (AVG(endtime - starttime)/1000) as average, " + 
														  "(AVG(pdp_service_endtime - pdp_service_starttime)/1000) as average_pdp_service, " + 
														  "(AVG(pep_service_endtime - pep_service_starttime)/1000) as average_pep_service, " + 
														  "(AVG(ch_service_endtime - ch_service_starttime)/1000) as average_ch_service, " + 
														  "(AVG(pip_service_endtime - pip_service_starttime)/1000) as average_pip_service, " +
														  "(AVG(udf_endtime - udf_starttime)/1000) as average_udf_service " + 
	                    " FROM " + 
						"  (SELECT starttime as pdp_service_starttime, endtime as pdp_service_endtime, usid, actionid, serviceid as pdp_serviceid FROM safax.sfx_action_details WHERE (actiondesc = 'pdp'  and action = 'evaluate' and isdown = 0) ";
	 
				// PDP - a
				// AND isactive=0 and serviceid = ?) a "
				for (int i = 0; i < pdp_serviceid.size(); i++) {
					totalList.add(pdp_serviceid.get(i));
					String temp = "";
					if ( (i == 0) && pdp_serviceid.size() == 1) {
						temp = " and (serviceid = ?)";
					}
					else if ((i == 0)  && (pdp_serviceid.size() > 1)){
						temp = " and (serviceid = ?";
					}
					else if (i == pdp_serviceid.size() -1) {
						temp = " or serviceid = ?)";
					}
					else {
						temp = " or serviceid = ?";
					}
					query = query + temp;
				}
				
				if (pdp_serviceid.size() == 0) {
					String temp = "";
					temp = " and (serviceid is NULL or serviceid = '')";
					query = query + temp;
				}
				
				query = query + ") a";
	            
				// PEP - b
				// AND isactive=0 and serviceid = ?) b "
				// and serviceid = 'nl:tue:sec:pep' or serviceid = 'nl:tue:sec:safax:pep') b
				//  starttime is NOT NULL -> because PEP is compulsory. It always exists in an evaluation
				query = query + " INNER JOIN " + 
								" (SELECT IFNULL(starttime, 0) as pep_service_starttime, IFNULL(endtime, 0) as pep_service_endtime, actionid, serviceid as pep_serviceid " + 
								" FROM safax.sfx_action_details WHERE (actiondesc = 'pep' and action = 'evaluate' and isdown = 0 and  starttime is NOT NULL)  ";
				
				for (int i = 0; i < pep_serviceid.size(); i++) {
					totalList.add(pep_serviceid.get(i));
					String temp = "";
					if ( (i == 0) && pep_serviceid.size() == 1) {
						temp = " and (serviceid = ?)";
					}
					else if ((i == 0)  && (pep_serviceid.size() > 1)){
						temp = " and (serviceid = ?";
					}
					else if (i == pep_serviceid.size() -1) {
						temp = " or serviceid = ?)";
					}
					else {
						temp = " or serviceid = ?";
					}
					query = query + temp;
				}
				
				if (pep_serviceid.size() == 0) {
					String temp = "";
					temp = " and (serviceid is NULL or serviceid = '')";
					query = query + temp;
				}
				
				query = query + ") b";
				query = query + " ON a.actionid = b.actionid ";
							
				// PIP - c
				System.out.println("pip service id size is: " + pip_serviceid.size());
				
				for (int i = 0; i < pip_serviceid.size(); i++) {
					System.out.println("FOR LOOP pip service id size is: " + pip_serviceid.size());
					if ((i == 0) && !pip_serviceid.get(i).equals("pip_serviceid_none")  && pip_serviceid.size() == 1) {
						query = query + " INNER JOIN (SELECT action, actionid, serviceid as pip_serviceid, starttime as pip_service_starttime, endtime as pip_service_endtime" + 
										" FROM safax.sfx_action_details WHERE ((actiondesc = 'pip' and action = 'evaluate' and starttime is NOT NULL and isdown = 0) and (serviceid = ?  ))";
						
						totalList.add(pip_serviceid.get(i));
					}
					else if ((pip_serviceid.get(i).equals("pip_serviceid_none")) && (i == 0) && pip_serviceid.size() == 1) {
						query = query + " INNER JOIN " +
										" (SELECT IFNULL(starttime, 0) as pip_service_starttime, IFNULL(endtime, 0) as pip_service_endtime, action, actionid, serviceid as pip_serviceid " + 
										" FROM safax.sfx_action_details WHERE (actiondesc = 'pip') and (starttime is NULL or starttime ='') ";
					}
					else if ((i == 0) && !pip_serviceid.get(i).equals("pip_serviceid_none") && pip_serviceid.size() > 1) {
						query = query + " INNER JOIN (SELECT IFNULL(starttime, 0) as pip_service_starttime, IFNULL(endtime, 0) as pip_service_endtime, action, actionid, serviceid as pip_serviceid " + 
										" FROM safax.sfx_action_details WHERE ((actiondesc = 'pip' and action = 'evaluate' and starttime is NOT NULL and isdown = 0) and (serviceid = ?  ";
						
						totalList.add(pip_serviceid.get(i));
					}
					else if ((i != 0) && (!pip_serviceid.get(i).equals("pip_serviceid_none"))  && (i != pip_serviceid.size() - 1)) {
						totalList.add(pip_serviceid.get(i));
						
						String temp = "";
						temp = " or serviceid = ?";
						
						query = query + temp;
					}
					else if ((i != 0) && (!pip_serviceid.get(i).equals("pip_serviceid_none")) && (i == pip_serviceid.size() - 1)) {
						totalList.add(pip_serviceid.get(i));
						
						String temp = "";
						temp = " or serviceid = ?))";

						query = query + temp;
					}
					else if ((i != 0) && (pip_serviceid.get(i).equals("pip_serviceid_none")) && (i == pip_serviceid.size() - 1)) {
						query = query + " )) OR (actiondesc = 'pip' and starttime is NULL) ";
					}
				}
				
				if (pip_serviceid.size() != 0) {				
					query = query + ") c";
					query = query + " ON b.actionid = c.actionid ";
				}
				else if (pip_serviceid.size() == 0) {
					query = query + " INNER JOIN " +
									" (SELECT IFNULL(starttime, 0) as pip_service_starttime, IFNULL(endtime, 0) as pip_service_endtime, action, actionid, serviceid as pip_serviceid  " + 
									" FROM safax.sfx_action_details" + 
									" WHERE (actiondesc = 'pip' and starttime is NULL)  ";
					query = query + ") c";
					query = query + " ON b.actionid = c.actionid ";
				}
			
				// CH
				// and serviceid = ?) d "	
						
				query = query + " INNER JOIN (SELECT starttime as ch_service_starttime, endtime as ch_service_endtime, action, actionid, serviceid as ch_serviceid " + 
				 			 " FROM safax.sfx_action_details WHERE (actiondesc = 'ch' and action = 'evaluate' and isdown = 0)  ";
					
				for (int i = 0; i < ch_serviceid.size(); i++) {
					totalList.add(ch_serviceid.get(i));
					String temp = "";
					if ((i == 0) && (ch_serviceid.size() == 1)) {
						temp = " and (serviceid = ? )";
					}
					else if ((i == 0) && (ch_serviceid.size() > 1)) {
						temp = " and (serviceid = ? ";
					}
					else if (i == ch_serviceid.size() - 1) {
						temp = " or serviceid = ?)";
					}
					else {
						temp = " or serviceid = ?";
					}
					query = query + temp;
				}
				
				if (ch_serviceid.size() == 0) {
					String temp = "";
					temp = " and (serviceid is NULL or serviceid = '')";
					query = query + temp;
				}
				
				query = query + ") d";
				query = query + " ON c.actionid = d.actionid ";
				
				// UDF
				for (int i = 0; i < udf_serviceid.size(); i++) {
					if ((udf_serviceid.get(i).equals("udf_serviceid_none")) && (i == 0) && udf_serviceid.size() == 1) {
						query = query + " INNER JOIN (SELECT action, actionid, serviceid as udf_serviceid, starttime = 0 as udf_starttime, endtime = 0 as udf_endtime " 
								      + " FROM safax.sfx_action_details " 
								      + " WHERE actionid NOT IN (Select actionid FROM safax.sfx_action_details WHERE actiondesc = 'udf') and actiondesc = 'pdp' ";
					}
					else if ((i == 0) && !udf_serviceid.get(i).equals("udf_serviceid_none")  && udf_serviceid.size() != 1) {
						query = query + " INNER JOIN (SELECT action, actionid, serviceid as udf_serviceid, starttime as udf_starttime, endtime as udf_endtime" + 
										" FROM safax.sfx_action_details WHERE ((actiondesc = 'udf' and action = 'evaluate' and isdown = 0) and (serviceid = ?  ";
						totalList.add(udf_serviceid.get(i));
					}
					else if ((i == 0) && !udf_serviceid.get(i).equals("udf_serviceid_none")  && udf_serviceid.size() == 1) {
						query = query + " INNER JOIN (SELECT action, actionid, serviceid as udf_serviceid, starttime as udf_starttime, endtime as udf_endtime" + 
										" FROM safax.sfx_action_details WHERE ((actiondesc = 'udf' and action = 'evaluate' and isdown = 0) and (serviceid = ?  ))";
						totalList.add(udf_serviceid.get(i));
					}
					else if ((i != 0) && (!udf_serviceid.get(i).equals("udf_serviceid_none"))  && (i != udf_serviceid.size() - 1)) {
						totalList.add(udf_serviceid.get(i));
						
						String temp = "";
						temp = " or serviceid = ?";
						
						query = query + temp;
					}
					else if ((i != 0) && (!udf_serviceid.get(i).equals("udf_serviceid_none")) && (i == udf_serviceid.size() - 1)) {
						totalList.add(udf_serviceid.get(i));
						String temp = "";
						temp = " or serviceid = ?))";
						query = query + temp;
					}
					else if ((i != 0) && (udf_serviceid.get(i).equals("udf_serviceid_none")) && (i == udf_serviceid.size() - 1)) {
						query = query + " ))  UNION SELECT action, actionid, serviceid as udf_serviceid, starttime = 0 as udf_starttime, endtime = 0 as udf_endtime " 
			                          + " FROM safax.sfx_action_details " 
	                                  + " WHERE actionid NOT IN " 
			                          + " (SELECT actionid FROM safax.sfx_action_details WHERE actiondesc = 'udf') and actiondesc = 'pdp'";
					}
				}
				
				if (udf_serviceid.size() != 0) {
					query = query + ") e";
					query = query + " ON d.actionid = e.actionid ";	
				}
				else if (udf_serviceid.size() == 0) {
					query = query + 	" INNER JOIN "
									  + " (SELECT action, actionid, serviceid as udf_serviceid, starttime = 0 as udf_starttime, endtime = 0 as udf_endtime " 
			                          + " FROM safax.sfx_action_details " 
	                                  + " WHERE actionid NOT IN " 
			                          + " (SELECT actionid FROM safax.sfx_action_details WHERE actiondesc = 'udf') and actiondesc = 'pdp'" ;
					query = query + ") e";
					query = query + " ON d.actionid = e.actionid ";	
				}
						
				if (!uid.equals("-1")) {
					query = query +  " INNER JOIN (SELECT usid, uid FROM sfx_usersession WHERE uid = ?) as f ON a.usid = f.usid";
					totalList.add(uid);
				}
				query = query + " INNER JOIN (SELECT actionid, starttime, endtime FROM safax.sfx_action WHERE (action = 'evaluate' or action = 'sfxeval') AND (starttime >=? AND endtime <= ?)) g  ";				
				if (udf_serviceid.size() != 0) {
					query = query + "   ON e.actionid = g.actionid ";
				}
				else {
					query = query + "   ON d.actionid = g.actionid ";
				}
				
				totalList.add(starttime);
				totalList.add(endtime);
			return DBAbstraction.selectRecordJson(query, totalList);
		}
		
	// All users and all service instances, from starttime to endtime
	public static JSONObject averageServiceComponentEval(String servicecomponent, String starttime, String endtime){
		String query="SELECT COUNT(endtime) as num, (AVG(endtime-starttime)/1000) as average FROM sfx_action_details as a INNER JOIN sfx_usersession as b ON a.usid = b.usid WHERE action='evaluate' AND starttime >=? AND endtime <= ? AND actiondesc = ?";
		return DBAbstraction.selectRecordJson(query, DataUtil.convertToList(starttime, endtime, servicecomponent));
	}
	
	// All users and all service instances, from beginning to today
	public static JSONObject averageServiceComponentEval(String servicecomponent){
		String query="SELECT COUNT(endtime) as num, (AVG(endtime-starttime)/1000) as average FROM sfx_action_details as a INNER JOIN sfx_usersession as b ON a.usid = b.usid WHERE action='evaluate' AND actiondesc = ?";
		return DBAbstraction.selectRecordJson(query, DataUtil.convertToList(servicecomponent));
	}
	
	public static JSONObject averageServiceComponentEval(String servicecomponent, String uid, String starttime, String endtime, String serviceid){
		if (serviceid == null) {
			String query="SELECT COUNT(endtime) as num, (AVG(endtime-starttime)/1000) as average FROM sfx_action_details as a INNER JOIN sfx_usersession as b ON a.usid = b.usid WHERE action='evaluate'  AND b.uid=? AND starttime >=? AND endtime <= ? AND actiondesc = ?";
			return DBAbstraction.selectRecordJson(query, DataUtil.convertToList(uid, starttime, endtime, servicecomponent));
		}
		else {
			String query="SELECT COUNT(endtime) as num, (AVG(endtime-starttime)/1000) as average FROM sfx_action_details as a INNER JOIN sfx_usersession as b ON a.usid = b.usid WHERE action='evaluate'  AND b.uid=? AND starttime >=? AND endtime <= ? AND actiondesc = ? AND serviceid = ?";
			return DBAbstraction.selectRecordJson(query, DataUtil.convertToList(uid, starttime, endtime, servicecomponent, serviceid));
		}
	}
	
	public static JSONObject getStartDate(){
		String query="SELECT min(starttime) as starttime FROM sfx_action WHERE action='sfxeval' AND isactive=0";
		return DBAbstraction.selectRecordJson(query, null);
	}
	
	public static JSONObject averageEval(){
		String query="SELECT (AVG(endtime-starttime)/1000) as average FROM sfx_action WHERE action='evaluate' AND isactive=0";
		return DBAbstraction.selectRecordJson(query, null);
	}
	
	public static JSONObject averageEval(String uid){		
		String query="SELECT (AVG(endtime-starttime)/1000) as average FROM sfx_action as a INNER JOIN sfx_usersession as b ON a.usid = b.usid WHERE action='evaluate' AND b.uid=?";
		return DBAbstraction.selectRecordJson(query, DataUtil.convertToList(uid));
	}
	
	public static JSONObject averageEval(String starttime, String endtime){
		String query="SELECT (AVG(endtime-starttime)/1000) as average FROM sfx_action WHERE action='evaluate' AND isactive=0 AND starttime >=? AND endtime <= ?";
		return DBAbstraction.selectRecordJson(query, DataUtil.convertToList(starttime, endtime));
	}
	
	public static JSONObject averageEval(String uid, String starttime, String endtime){
		String query="SELECT (AVG(endtime-starttime)/1000) as average FROM sfx_action  as a INNER JOIN sfx_usersession as b ON a.usid = b.usid WHERE action='evaluate'  AND b.uid=? AND starttime >=? AND endtime <= ?";
		return DBAbstraction.selectRecordJson(query, DataUtil.convertToList(uid, starttime, endtime));
	}
	
	public static JSONObject getRequestTime(String transactionid){
		String query="SELECT (endtime-starttime)/1000 as reqtime FROM sfx_action WHERE uaid=? AND starttime IS NOT NULL AND endtime IS NOT NULL";
		return DBAbstraction.selectRecordJson(query, DataUtil.convertToList(transactionid));
	}
	
	/********************** ISSUE SPECIFIC **************************/
	public static int reportIssue(String uid,String issue,String issuedesc, String isfeature){
		String query="INSERT INTO sfx_issue(issueheader,issuedesc,isfeature,isactive,uid) VALUES(?,?,?,1,?)";
		return DBAbstraction.insertStatement(query, DataUtil.convertToList(issue,issuedesc,isfeature,uid));
	}
	
	public static JSONArray getIssues(boolean resolved){
		String flag=resolved?"0":"1";
		
		String query="SELECT a.issid, a.issueheader, a.issuedesc, a.isfeature,b.uname FROM sfx_issue a, sfx_user b "
					+ "WHERE a.uid=b.uid AND a.isactive=? ORDER BY a.issid DESC";
		return DBAbstraction.selectRecordsJson(query, DataUtil.convertToList(flag));
	}
	public static boolean resolveIssue(String issueid){
		String query="UPDATE sfx_issue SET isactive=0 WHERE issid=?";
		return DBAbstraction.updateStatement(query, DataUtil.convertToList(issueid));
	}
	
    /********************* TRANSPARENCY SPECIFIC ********************/
	/********************* Integrate work from Master student *******/
	
	public static JSONArray getViewpoint(String demoid,String viewpoint){
		String query = "SELECT a.viewpoint FROM sfx_transparency a, sfx_pdp b WHERE "
				+ "b.pdpid=a.pdpid AND a.viewpoint=? AND b.demoid=? ";
		return DBAbstraction.selectRecordsJson(query,DataUtil.convertToList(viewpoint, demoid));
	}
	
	public static JSONArray getTransparency(String demoid,String viewpoint){
		String query = "SELECT a.conflictid,a.conflictname FROM sfx_conflict a, "
				+ "sfx_democonflicts c, sfx_transparency d, sfx_pdp f WHERE "
				+ "f.pdpid=d.pdpid AND c.transparencyid=d.transparencyid "
				+ "AND c.conflictid = a.conflictid AND f.demoid=? AND d.viewpoint=?";
		return DBAbstraction.selectRecordsJson(query,DataUtil.convertToList(demoid,viewpoint));
	}
	
	public static JSONArray getTransparencyAll(){
		String query = "SELECT a.conflictid,a.conflictname FROM sfx_conflict a";
		return DBAbstraction.selectRecordsJson(query,null);
	}
	
	public static JSONArray getTransparencyUsers(String demoid){
		String query = "SELECT a.viewpoint FROM sfx_transparency a, sfx_pdp b WHERE a.pdpid=b.pdpid AND b.demoid=?";
		return DBAbstraction.selectRecordsJson(query,DataUtil.convertToList(demoid));
	}
	
	public static void removePreferences(String demoid,String viewpoint){
		String query = "DELETE from sfx_democonflicts where transparencyid in (SELECT "
				+ "a.transparencyid FROM sfx_transparency a, sfx_pdp b WHERE a.pdpid=b.pdpid AND b.demoid=? AND a.viewpoint=?)";
		DBAbstraction.deleteStatement(query, DataUtil.convertToList(demoid,viewpoint));	
	}
	
	public static String checkInitialization(String demoid,String uid){
		String query = "SELECT count(*) FROM sfx_transparency a, sfx_pdp b WHERE a.pdpid=b.pdpid AND b.demoid=? AND a.uid=?";
		return DBAbstraction.selectRecord(query, DataUtil.convertToList(demoid,uid));	
	}
	
	public static void initTransparency(String demoid,String uid){
		String query = "INSERT INTO sfx_transparency(pdpid,uid) SELECT a.pdpid,? FROM sfx_pdp a WHERE a.demoid=?";
		DBAbstraction.insertStatement(query, DataUtil.convertToList(uid,demoid));	
	}
	
	public static void addPreferences(String demoid,String viewpoint,String conflictid){
		String query = "INSERT INTO sfx_democonflicts(transparencyid,conflictid) SELECT a.transparencyid,? FROM sfx_transparency a, sfx_pdp b WHERE a.pdpid=b.pdpid AND b.demoid=? AND a.viewpoint=?";
		DBAbstraction.insertStatement(query, DataUtil.convertToList(conflictid,demoid,viewpoint));	
	}
	
	public static void addViewpoint(String demoid,String uname,String viewpoint){
		String query = "INSERT INTO sfx_transparency(pdpid,uid,viewpoint) SELECT a.pdpid, b.uid,? FROM sfx_pdp a, sfx_user b WHERE a.demoid=? AND b.uname=?";
		DBAbstraction.insertStatement(query, DataUtil.convertToList(viewpoint,demoid,uname));	
	}
	
	public static void removeViewpoint(String demoid,String viewpoint){
		String query = "DELETE FROM sfx_transparency where pdpid in (SELECT a.pdpid from sfx_pdp a "
					 + "WHERE a.demoid=?) AND viewpoint=?";
		DBAbstraction.deleteStatement(query, DataUtil.convertToList(demoid,viewpoint));	
	}
	/*
	 * Example Query
	 * SELECT a.actionid, pepserviceid, pepduration, chserviceid, chduration, pdpserviceid, pdpduration, pipserviceid, pipduration, papserviceid, papduration

		FROM 
		(SELECT (endtime - starttime)/1000 as pepduration, serviceid as pepserviceid, actionid 
		FROM safax.sfx_action_details where actionid = 'sfxeval_39e1919f48764c77965e74c19bce5ac6' 
		and actiondesc = 'pep' and starttime is not null and endtime is not null and isdown = 0) a
		
		INNER JOIN 
		
		(SELECT (endtime - starttime)/1000 as chduration, serviceid as chserviceid, actionid 
		FROM safax.sfx_action_details where actionid = 'sfxeval_39e1919f48764c77965e74c19bce5ac6' 
		and actiondesc = 'ch' and starttime is not null and endtime is not null and isdown = 0) b
		
		ON a.actionid = b.actionid
		
		INNER JOIN 
		
		(SELECT (endtime - starttime)/1000 as pdpduration, serviceid as pdpserviceid, actionid 
		FROM safax.sfx_action_details where actionid = 'sfxeval_39e1919f48764c77965e74c19bce5ac6' 
		and actiondesc = 'pdp' and starttime is not null and endtime is not null and isdown = 0) c
		
		ON b.actionid = c.actionid
		
		INNER JOIN 
		
		(SELECT (endtime - starttime)/1000 as pipduration, serviceid as pipserviceid, actionid 
		FROM safax.sfx_action_details where actionid = 'sfxeval_39e1919f48764c77965e74c19bce5ac6' 
		and actiondesc = 'pip' and starttime is not null and endtime is not null and isdown = 0) d
		
		ON c.actionid = d.actionid
		
		INNER JOIN 
		
		(SELECT (endtime - starttime)/1000 as papduration, serviceid as papserviceid, actionid 
		FROM safax.sfx_action_details where actionid = 'sfxeval_39e1919f48764c77965e74c19bce5ac6' 
		and actiondesc = 'pap' and starttime is not null and endtime is not null and isdown = 0) e
		
		ON d.actionid = e.actionid
		;
	 */
	public static JSONObject returnLastLog(String actionid){
		String query=" SELECT uid, a.actionid, f.evalstarttime, f.evalendtime, f.evalduration, pepserviceid, pepduration, chserviceid, chduration, pdpserviceid, pdpduration, pipserviceid, pipduration, papserviceid, papduration "
				+ ""
				+ " FROM "
				+   "(SELECT (endtime - starttime)/1000 as pepduration, serviceid as pepserviceid, actionid, usid "
				+   " FROM safax.sfx_action_details "
				+   " WHERE actionid = ? and actiondesc = 'pep' and starttime is not null and endtime is not null and isdown = 0) a "
				+   "INNER JOIN "
				+   " (SELECT (endtime - starttime)/1000 as chduration, serviceid as chserviceid, actionid "
				+   "  FROM safax.sfx_action_details "
				+   "  WHERE actionid = ? and actiondesc = 'ch' and starttime is not null and endtime is not null and isdown = 0) b ON a.actionid = b.actionid "
				+   "INNER JOIN "
				+   " (SELECT (endtime - starttime)/1000 as pdpduration, serviceid as pdpserviceid, actionid "
				+   "  FROM safax.sfx_action_details "
				+   "  WHERE actionid = ? and actiondesc = 'pdp' and starttime is not null and endtime is not null and isdown = 0) c ON b.actionid = c.actionid "
				+   "INNER JOIN "
				+   " (SELECT (endtime - starttime)/1000 as pipduration, serviceid as pipserviceid, actionid "
				+   " FROM safax.sfx_action_details "
				+   " WHERE actionid = ? and actiondesc = 'pip') d ON c.actionid = d.actionid "
				+   "INNER JOIN "
				+   " (SELECT (endtime - starttime)/1000 as papduration, serviceid as papserviceid, actionid "
				+   " FROM safax.sfx_action_details where actionid = ? and actiondesc = 'pap') e ON d.actionid = e.actionid " 
				+   "INNER JOIN "
				+   " (SELECT (endtime - starttime)/1000 as evalduration, starttime as evalstarttime, endtime as evalendtime, actionid " 
                +   " FROM safax.sfx_action WHERE actionid = ? ) f ON e.actionid = f.actionid "
                +   "INNER JOIN "
                +   " (SELECT uid, usid " 
                +   " FROM safax.sfx_usersession ) g ON g.usid = a.usid";			
		return DBAbstraction.selectRecordJson(query,DataUtil.convertToList(actionid, actionid, actionid, actionid, actionid, actionid));
	}
		
	public static JSONArray returnLastUDFLog(String actionid){
		String query=" SELECT serviceid as udfserviceid, (endtime - starttime)/1000 as udfduration "
				+ " FROM safax.sfx_action_details WHERE actionid = ? AND actiondesc = 'udf' ";
		
		return DBAbstraction.selectRecordsJson(query,DataUtil.convertToList(actionid));
		
	}
	
	// CAIRIS updates
	
	public static String setCairisCredentials(String cairisuname, String cairispwd, String demoid){
		String query = "SELECT COUNT(*) FROM sfx_cairis_credentials WHERE demoid=?";
		String count = DBAbstraction.selectRecord(query, DataUtil.convertToList(demoid));
		if(Integer.valueOf(count) > 0){
			query="UPDATE sfx_cairis_credentials SET cairisuname=?, cairispwd=? WHERE demoid=?";
			boolean update= DBAbstraction.updateStatement(query,DataUtil.convertToList(cairisuname,cairispwd,demoid));		
			if(update==true){
				query = "SELECT ccid FROM sfx_cairis_credentials WHERE demoid=?";
				String ccid = DBAbstraction.selectRecord(query, DataUtil.convertToList(demoid));
				return ccid;
			}else
				return null;
		}
			query="INSERT INTO sfx_cairis_credentials(cairisuname,cairispwd,demoid) VALUES(?,?,?)";
			int cairisID=DBAbstraction.insertStatement(query, DataUtil.convertToList(cairisuname,cairispwd,demoid));
			if(cairisID > 0){
				query = "SELECT ccid FROM sfx_cairis_credentials WHERE cairisuname=? AND cairispwd=? AND demoid=?";
				return DBAbstraction.selectRecord(query, DataUtil.convertToList(cairisuname,cairispwd,demoid));
			}
				
			
			return null;
		
	}
	
	public static int setCairisDB(String dbname, String cairisid){
		String query = "SELECT cdid FROM sfx_cairis_db WHERE dbname=? AND ccid=?";
		String cdid = DBAbstraction.selectRecord(query, DataUtil.convertToList(dbname, cairisid));
		if(cdid != null){

			query = "SELECT cdid FROM sfx_cairis_db WHERE ccid=?";
			ArrayList<String> cdids = DBAbstraction.selectRecords(query, DataUtil.convertToList(cairisid));

			for(int i=0;i<cdids.size();i++){
				query="UPDATE sfx_cairis_db SET isactive=0 WHERE cdid=?";
				boolean update= DBAbstraction.updateStatement(query,DataUtil.convertToList(cdids.get(i)));		
				if(update==false)
					return 0;
			}

			query="UPDATE sfx_cairis_db SET isactive=1 WHERE cdid=?";
			boolean update= DBAbstraction.updateStatement(query,DataUtil.convertToList(cdid));		
			if(update==true)
				return Integer.valueOf(cdid);			
			else
				return 0;

		}
		
		query = "SELECT cdid FROM sfx_cairis_db WHERE ccid=?";
		ArrayList<String> cdids = DBAbstraction.selectRecords(query, DataUtil.convertToList(cairisid));

		for(int i=0;i<cdids.size();i++){
			query="UPDATE sfx_cairis_db SET isactive=0 WHERE cdid=?";
			boolean update= DBAbstraction.updateStatement(query,DataUtil.convertToList(cdids.get(i)));		
			if(update==false)
				return 0;
		}


		query="INSERT INTO sfx_cairis_db(dbname,ccid,isactive) VALUES(?,?,1)";
		return DBAbstraction.insertStatement(query, DataUtil.convertToList(dbname,cairisid));

	}
	
	// End CAIRIS updates
	
	
}
