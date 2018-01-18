package nl.tue.sec.cairis.engine;

import java.util.ArrayList;

import nl.tue.sec.cairis.db.DBFns;
import nl.tue.sec.cairis.impl.AuthenticationMethods;
import nl.tue.sec.cairis.impl.CAIRISRetrievalMethods;
import nl.tue.sec.cairis.util.LogUtil;

import com.sun.jersey.api.client.ClientResponse;

public class CairisEngine {

	
	public static int coreExecute(String CairisURL, String transID, String authURL, String CAIRISdb){
		ClientResponse response;
		String risk_value;
		int int_risk_value;


		String demoID = DBFns.getDemoIDfromTransactionID(transID);
		if(demoID == null){
			LogUtil.errorlog(transID, "ERROR: No demo associated to this transaction", "", 2);
			LogUtil.writeLog(transID, "ERROR: No demo associated to this transaction", -4);
			System.out.println("\n\nERROR: No demo associated to this transaction\n\n");
			return -1;
		}

		ArrayList<String> cairisCredentials = DBFns.getCAIRISCredentials(demoID);

		if(cairisCredentials == null){
			LogUtil.errorlog(transID, "Attention: No CAIRIS cedentials available", "", 2);
			LogUtil.writeLog(transID, "Attention: No CAIRIS cedentials available", -3);
			System.out.println("\n\nAttention: No CAIRIS cedentials available\n\n");

			return -1;
		}
		String cairisID = cairisCredentials.get(0);
		String cairisUsername = cairisCredentials.get(1);
		String cairisPwd = cairisCredentials.get(2);
		
//		LogUtil.writeLog(transID, "demo: "+demoID, -3);
//		LogUtil.writeLog(transID, "id: "+cairisID, 3);
//		LogUtil.writeLog(transID, "uname: "+cairisUsername, 3);
//		LogUtil.writeLog(transID, "pwd: "+cairisPwd, 3);

		String cairisDB = DBFns.getCAIRISDB(cairisID);
		
		if(cairisDB == null){
			LogUtil.errorlog(transID, "Attention: No active DB associated to the specified demo", "", 2);
			LogUtil.writeLog(transID, "Attention: No active DB associated to the specified demo", -3);
			System.out.println("\n\nAttention: No active DB associated to the specified demo\n\n");
		
			return -1;
		}
		
		//		SIMPLE SESSION_ID AUTHENTICATION
		//		response = AuthenticationMethods.simpleAuth(CairisURL);


		/*
		 * HTTP BASIC AUTHENTICATION
		 * 
		 */

		response = AuthenticationMethods.httpBasicAuth(cairisUsername,cairisPwd, authURL); 

		if (response.getStatus() != 200){
			LogUtil.errorlog(transID, "Authentication to CAIRIS failed : HTTP error code : "
					+ response.getStatus(), "", 3);
			if(response.getStatus() == 401){
				LogUtil.writeLog(transID, "Authentication to CAIRIS failed, wrong credentials for the user: "+cairisUsername, 3);
				System.out.println("Authentication to CAIRIS failed, wrong credentials for the user: "+cairisUsername);
			}else{
				LogUtil.writeLog(transID, "Authentication to CAIRIS failed : HTTP error code : "
						+ response.getStatus(), 3);
				System.out.println("Authentication to CAIRIS failed : HTTP error code : "
						+ response.getStatus());
			}
			return -1;
		}

		System.out.println("CAIRIS session obtained by the user: "+cairisUsername);
		LogUtil.writeLog(transID, "CAIRIS session obtained by the user: "+cairisUsername, 2);


		response=CAIRISRetrievalMethods.getRisk(response, CairisURL, CAIRISdb, cairisDB, transID);

		/////	End HTTP BASIC AUTHENTICATION

		if (response.getStatus() != 200){
			LogUtil.errorlog(transID, "Risk could not be retrieved : HTTP error code : "
					+ response.getStatus(), "", 3);
			// page not found
			if (response.getStatus() == 404) {
				System.out.println("Risk could not be retrieved because the page was not found, check the correct URL (HTTP error code : "
						+ response.getStatus()+")");
				LogUtil.writeLog(transID, "Risk could not be retrieved because the page was not found, check the correct URL (HTTP error code : "
						+ response.getStatus()+")", 3);

			}// bad request
			else if (response.getStatus() == 400) {
				System.out.println("Risk could not be retrieved because of a bad request towards CAIRIS, check parameters (HTTP error code : "
						+ response.getStatus()+")");
				LogUtil.writeLog(transID, "Risk could not be retrieved because of a bad request towards CAIRIS, check parameters (HTTP error code : "
						+ response.getStatus()+")", 3);
			}else{
				LogUtil.writeLog(transID, "Risk could not be retrieved : HTTP error code : "
						+ response.getStatus(), 3);
			}

			return -1;
		}


		risk_value = response.getEntity(String.class);
		int_risk_value = Integer.valueOf(risk_value);
		
		return int_risk_value;
	}
}
