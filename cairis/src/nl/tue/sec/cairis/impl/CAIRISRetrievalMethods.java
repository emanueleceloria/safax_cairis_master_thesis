package nl.tue.sec.cairis.impl;

import nl.tue.sec.cairis.util.LogUtil;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class CAIRISRetrievalMethods {

	public static ClientResponse getRisk(ClientResponse resp, String CairisURL, String CAIRISdbURL, String dbname, String transID){
		Client client = Client.create();
		WebResource webResource;
		ClientResponse response;
		
		//parsing the response in order to extract the session_id obtained
		String rep="\"";
		String sessionCairis = resp.getEntity(String.class).replace(rep,"" ).toString().trim();		 		
		String []respArray = sessionCairis.split(":");
		sessionCairis = respArray[2].replace("}", "").trim();

        //LogUtil.writeLog(transID,"CAIRIS session_id: "+sessionCairis, 2);
		//LogUtil.writeLog(transID,"Final url: "+CairisURL+"?session_id="+sessionCairis, 2);
		response = openCAIRISdb(sessionCairis, CAIRISdbURL, dbname, transID);
		
		if (response.getStatus() != 200){
			LogUtil.errorlog(transID, "Problems opening CAIRIS db : HTTP error code : "
					+ response.getStatus(), "", 3);
			LogUtil.writeLog(transID, "Problems opening CAIRIS db : HTTP error code : "
					+ response.getStatus(), 3);
			System.out.println("Problems opening CAIRIS db : HTTP error code : "
					+ response.getStatus());					


			return null;
		}
				
		webResource = client.resource(CairisURL+"?session_id="+sessionCairis);
		response= webResource.get(ClientResponse.class);

		return response;
	}
	
	private static ClientResponse openCAIRISdb(String sessionCairis, String CAIRISdbURL, String dbname, String transID){
		Client client = Client.create();
		WebResource webResource;
		ClientResponse response;
		
		String DBOpenURL = CAIRISdbURL+"/"+dbname+"/open";
		System.out.println("Contacting CAIRIS database at "+DBOpenURL);
		LogUtil.writeLog(transID,"Opening CAIRIS database: "+dbname, 2);
		
		String DBOpenURLComplete = DBOpenURL+"?session_id="+sessionCairis;

		webResource = client.resource(DBOpenURLComplete);
		response= webResource.post(ClientResponse.class);
		
		return response;
	}
}
