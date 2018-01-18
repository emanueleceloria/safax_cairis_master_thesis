package nl.tue.sec.safax.sfxbe.impl;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import nl.tue.sec.safax.sfxbe.db.DBFns;
import nl.tue.sec.safax.sfxbe.ds.Request;
import nl.tue.sec.safax.sfxbe.parser.AttributeParser;
import nl.tue.sec.safax.sfxbe.util.AccessRequest;
import nl.tue.sec.safax.sfxbe.util.DataUtil;
import nl.tue.sec.safax.sfxbe.util.SafaxUtil;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;

public class DemoHandlerImpl {
	
	public String createDemo(String demoname, String demodesc, String uname, String projectid,String sessionid){
		demoname=DataUtil.sanitizeInput(demoname);
		demodesc=DataUtil.sanitizeInput(demodesc);
		projectid=DataUtil.sanitizeInput(projectid);
		if(demoname.length()<1)
			return DataUtil.generalResponse("false", "Invalid Demoname", "");
		
		String uid=DBFns.getUserFromName(uname);
		String projectname=DBFns.getProjectName(projectid);
		Request r=SafaxUtil.getAccessRequest(projectname, "", "demo", uname, "create");
		if(!AccessRequest.auhorizeUser(r, sessionid))
			return DataUtil.generalResponse("false", "Not authorized to create demos", "");
		
		int demoid=DBFns.createDemo(demoname,demodesc,uid,projectid);
		DBFns.setupDemoConfig("pdp", "nl:tue:sec:safax:pdp", Integer.toString(demoid));
		DBFns.setupDemoConfig("pep", "nl:tue:sec:pep", Integer.toString(demoid));
		DBFns.setupDemoConfig("pip", "nl:tue:sec:pip", Integer.toString(demoid));
		DBFns.setupDemoConfig("pap", "nl:tue:sec:safax:pap", Integer.toString(demoid));
		DBFns.setupDemoConfig("ch", "nl:tue:sec:ch", Integer.toString(demoid));
		DBFns.createPDP(Integer.toString(demoid));
		JSONObject j=new JSONObject();
		DataUtil.asMap(j).put("demoid", Integer.toString(demoid));
		DataUtil.asMap(j).put("response", "true");
		DataUtil.asMap(j).put("message", "Demo Successfully created");
		DataUtil.asMap(j).put("data", "Proceed to configure the demo");
		return j.toJSONString();
	}

	public JSONArray getAllDemos(String uname,String projectid){
		return DBFns.getAllDemos(projectid);
	}
	
	public String fetchDemo(String uname,String demoid){
		return DBFns.fetchDemo(demoid).toJSONString();
	}
	
	public String fetchDemoConfig(String uname, String demoid){
		return DBFns.fetchDemoConfig(demoid).toJSONString();
	}
	
	public String getDemoPDPHost(String uname, String demoid){
		return DBFns.getDemoPDPHost(demoid).toJSONString();
	}
	
	public String updateDemoConfig(String configkey, String configvalue, String demoid, String uname, String sessionid){
		configkey=DataUtil.sanitizeInput(configkey);
		configvalue=DataUtil.sanitizeInput(configvalue);
		demoid=DataUtil.sanitizeInput(demoid);
		if(DBFns.updateDemoConfig(configkey, configvalue, demoid))
			return DataUtil.generalResponse("true", "Demo Config Updated", "");
		return DataUtil.generalResponse("false", "Could not update config", "");
	}
	
	@SuppressWarnings("unchecked")
	public JSONArray getPDPS(String username){
		if(DBFns.isUserAdmin(username))
			return DBFns.getAllPDPS(username);
		JSONArray ja=new JSONArray();
		ja.addAll(DBFns.getPublicPDPS(username));
		ja.addAll(DBFns.getPrivatePDPS(DBFns.getUserFromName(username)));
		return ja;
	}
	
	public String canEditDemo(String demoid, String uname,String sessionid){
		String projectname=DBFns.getProjectNameFromDemo(demoid);
		if(projectname==null || projectname.length()<1)
			return DataUtil.generalResponse("false", "No active project exists with that name", "Please try again. "
					+ "Or report it to our administrators.");
		if(AccessRequest.auhorizeUser(SafaxUtil.getAccessRequest(projectname, demoid, "demo", uname, "edit"),sessionid))
				return DataUtil.generalResponse("true", "Edit Demo", "");
		return DataUtil.generalResponse("false", "You can't edit what you don't own.", "");
	}
	
	public String deleteDemo(String demoid, String uname,String sessionid){
		
		String projectname=DBFns.getProjectNameFromDemo(demoid);
		if(projectname==null || projectname.length()<1){
			return DataUtil.generalResponse("false", "No active project exists with that name", "Please try again. "
					+ "Or report it to our administrators.");
		}
		Request r=SafaxUtil.getAccessRequest(projectname, demoid, "demo", uname, "delete");
		if(AccessRequest.auhorizeUser(r,sessionid)){
				String actionid=DBFns.recordAction(sessionid, "delete", demoid, "demo");
				DBFns.deleteDemo(demoid);
				DBFns.stopAction(actionid);
				SafaxUtil.removePDP(demoid, sessionid);
				return DataUtil.generalResponse("true", "Demo Deleted.", "");
		}
		return DataUtil.generalResponse("false", "You can't delete what you don't own.", "This demo"
				+ " is owned by someone else.");
	}
	
//	public String updatePDPCode(String demoid, String pdpcode, String ispersistent, String uname){
//		demoid=DataUtil.sanitizeInput(demoid);
//		pdpcode=DataUtil.sanitizeInput(pdpcode);
//		ispersistent=ispersistent.equals("on")?"1":"0";
//		if(DBFns.updatePDP(demoid,pdpcode,ispersistent))
//			return DataUtil.generalResponse("true", "PDP updated successfully", "");
//		return DataUtil.generalResponse("false", "PDP could not be updated", "");
//			
//		
//	}
	
	public String getPDPInfo(String demoid, String uname){
		JSONArray ja=new JSONArray();
		ja=DBFns.getPDPInfo(demoid);
		if(ja.size()>0){
			JSONObject j=(JSONObject)ja.get(0);
			return j.toJSONString();
		}
		return new JSONObject().toJSONString();
			
	}
	
	public String copyDemo(String demoid,String prid, String uname,String sessionid){
		
		System.out.println("copy demo in DEMO Handler Imple.java");
		
		prid=DataUtil.sanitizeInput(prid);
		
		String uid=DBFns.getUserFromName(uname);
		String projectname=DBFns.getProjectName(prid);
		Request r=SafaxUtil.getAccessRequest(projectname, "", "demo", uname, "create");
		if(!AccessRequest.auhorizeUser(r, sessionid))
			return DataUtil.generalResponse("false", "Not authorized to create demos", "");
		
		int newdemo=DBFns.copyDemo(demoid,prid,uid);
		PolicyHandlerImpl phandler=new PolicyHandlerImpl();
		String attributes=phandler.getTrustPolicy(Integer.toString(newdemo), "attribute");
		if(attributes!=null && attributes.length()>0)
			loadAttributes(attributes, Integer.toString(newdemo), sessionid);
		return DataUtil.generalResponse("true", "Demo Successfully Copied", "");
	}

	public String editDemo(String demoid,String demoname,  String uname, String demodesc,
			String projectid, String pepservice, String chservice, String pipservice, String pdpservice, String papservice, String pdpcode,
			String pdprcalgo,
			String ispersistent, String sessionid) {
		demoid=DataUtil.sanitizeInput(demoid);
		demoname=DataUtil.sanitizeInput(demoname);
		uname=DataUtil.sanitizeInput(uname);
		demodesc=DataUtil.sanitizeInput(demodesc);
		projectid=DataUtil.sanitizeInput(projectid);
		pepservice=DataUtil.sanitizeInput(pepservice);
		chservice=DataUtil.sanitizeInput(chservice);
		pipservice=DataUtil.sanitizeInput(pipservice);
		pdpservice=DataUtil.sanitizeInput(pdpservice);
		papservice=DataUtil.sanitizeInput(papservice);
		pdpcode=DataUtil.sanitizeInput(pdpcode);
		pdprcalgo=DataUtil.sanitizeInput(pdprcalgo);
		
		if(!AccessRequest.auhorizeUser(SafaxUtil.getAccessRequest(projectid, demoid, "demo", uname, "edit"),sessionid))
			return DataUtil.generalResponse("false", "You cannot edit the demo", "");
		
		ispersistent=(ispersistent!=null && ispersistent.equals("on"))?"1":"0";
		DBFns.editDemo(demoname, demodesc, projectid, demoid);
		if(ispersistent.equals("1") && AccessRequest.auhorizeUser(SafaxUtil.getAccessRequest(projectid, demoid, "ppdp", uname, "enable"),sessionid))
			DBFns.updatePDP(demoid,pdpcode,pdprcalgo,ispersistent);
		else
			{
				DBFns.updatePDP(demoid,pdpcode,pdprcalgo,"0");
				SafaxUtil.refreshPDP(demoid, sessionid);
			}
				
		DBFns.updateDemoConfig("pep", pepservice,demoid);
		DBFns.updateDemoConfig("ch", chservice,demoid);
		DBFns.updateDemoConfig("pip", pipservice,demoid);
		DBFns.updateDemoConfig("pdp", pdpservice,demoid);
		DBFns.updateDemoConfig("pap", papservice,demoid);
		
		return DataUtil.generalResponse("true", "Demo Settings Successfully updated", projectid);
	}
	
	// CAIRIS updates
	public String editCairis(String demoid, String uname, String cairisusr, String cairispwd, String cairisdb, String projectid, String sessionid) {
		demoid=DataUtil.sanitizeInput(demoid);
		uname=DataUtil.sanitizeInput(uname);
		cairisusr=DataUtil.sanitizeInput(cairisusr);
		cairispwd=DataUtil.sanitizeInput(cairispwd);
		cairisdb=DataUtil.sanitizeInput(cairisdb);
		projectid=DataUtil.sanitizeInput(projectid);
		
		if(!AccessRequest.auhorizeUser(SafaxUtil.getAccessRequest(projectid, demoid, "demo", uname, "edit"),sessionid))
			return DataUtil.generalResponse("false", "You cannot edit the demo", "");
		
		if(cairisusr.equals("") || cairispwd.equals("") || cairisdb.equals(""))
			return DataUtil.generalResponse("false", "Please insert a valid username/password/database name", projectid);
			
		
		String ccid = DBFns.setCairisCredentials(cairisusr, cairispwd, demoid);
		
		if(ccid == null)
			return DataUtil.generalResponse("false", "Problems occured while updating CAIRIS Settings", projectid);
			
		int dbid = DBFns.setCairisDB(cairisdb, ccid);
		
		if(dbid <= 0)
			return DataUtil.generalResponse("false", "Problems occured while updating CAIRIS Settings", projectid);

			
		return DataUtil.generalResponse("true", "CAIRIS Settings successfully updated", projectid);
	}
	// end CAIRIS updates
	
	
	
	public String editDemoConfig(String uname, String demoid, String configkey, String configvalue,String sessionid){
		configkey=DataUtil.sanitizeInput(configkey);
		configvalue=DataUtil.sanitizeInput(configvalue);
		demoid=DataUtil.sanitizeInput(demoid);
		
		String projectid=DBFns.getProjectNameFromDemo(demoid);
		if(!AccessRequest.auhorizeUser(SafaxUtil.getAccessRequest(projectid, demoid, "demo", uname, "edit"),sessionid))
			return DataUtil.generalResponse("false", "You cannot edit the demo", "");
		if(DBFns.updateDemoConfig(configkey, configvalue, demoid))
			return DataUtil.generalResponse("true", "Demo Settings Successfully updated", "");
		return DataUtil.generalResponse("false", "Demo Settings could not be updated", "");
	}
	
	public String loadAttributes(String attributes, String demoid, String sessionid){
		String transactionid=DBFns.recordAction(sessionid, "loadatt", demoid, "demo");
		AttributeParser parser=new AttributeParser();
		DBFns.loadAttributes(parser.parseCSV(attributes, transactionid),demoid);
		DBFns.stopAction(transactionid);
		return DataUtil.generalResponse("true", "Uploaded Attribute Values", "");
	}
	
	public String getAttributes(String demoid, String isroot, String refid){
		if(isroot.equals("true"))
			return DBFns.getRootAttributes(demoid).toJSONString();
		else
			return DBFns.getAttributes(demoid,refid).toJSONString();
	}
	
	/* Integrate transparency from Master student 
	 * 
	 * 
	 */
	
	public String getViewpoint(String demoid,String viewpoint){
		
		JSONArray ja=new JSONArray();
		ja=DBFns.getViewpoint(demoid,viewpoint);
		if(ja.size()>0){
			JSONObject j=(JSONObject)ja.get(0);
			return j.toJSONString();
		}
		return new JSONObject().toJSONString();
	}
	
	public String getTransparency(String demoid,String viewpoint){
		//System.out.println("JSONSTRING "+DBFns.getTransparency(demoid,uname).toJSONString());
		return DBFns.getTransparency(demoid,viewpoint).toJSONString();
	}
	
	public String getTransparencyAll(){
		System.out.println("JSONSTRINGALL "+DBFns.getTransparencyAll().toJSONString());
		return DBFns.getTransparencyAll().toJSONString();
	}
	
	public String getTransparencyUsers(String demoid){
		//System.out.println("JSONSTRINGALL "+DBFns.getTransparencyAll().toJSONString());
		return DBFns.getTransparencyUsers(demoid).toJSONString();
	}
	
	
	public String savePreferences(String demoid, List<String> selectedconflicts,String viewpoint){
		//System.out.println("SELECTEDCONFLICTS "+selectedconflicts);
		DBFns.removePreferences(demoid,viewpoint);

		for(String au:selectedconflicts)
			DBFns.addPreferences(demoid,viewpoint,au);
		return DataUtil.generalResponse("true", "Project successfully Updated!", "");
	}
	
	public String addViewpoint(String demoid,String uname,String viewpoint){
		DBFns.addViewpoint(demoid,uname,viewpoint);
		return DataUtil.generalResponse("true", "User successfully Added!", "");
	}
	
	public String removeViewpoint(String demoid,String viewpoint) {
		DBFns.removeViewpoint(demoid,viewpoint);
		return DataUtil.generalResponse("true", "User successfully Removed!", "");
	}
	
	/* End of changs of transparency integration
	 *
	 * 
	 */

}
