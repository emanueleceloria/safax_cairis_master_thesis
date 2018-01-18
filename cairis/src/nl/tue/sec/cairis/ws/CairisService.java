package nl.tue.sec.cairis.ws;

import java.util.ArrayList;

import nl.tue.sec.cairis.impl.AuthenticationMethods;
import nl.tue.sec.cairis.impl.CAIRISRetrievalMethods;
import nl.tue.sec.cairis.impl.EvaluationMethods;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.tue.sec.cairis.util.DataUtil;
import nl.tue.sec.cairis.util.LogUtil;
import nl.tue.sec.cairis.db.DBFns;
import nl.tue.sec.cairis.engine.CairisEngine;

import com.sun.jersey.api.client.ClientResponse;

/**
 * @author Emanuele Celoria
 * 
 */

@Path("/")
public class CairisService {
	
	// static parameters which represent the username and password used for http basic authentication
	// plus the cairis demo url and the api to contact in order to obtain a session
	private final static String CAIRISHOME="https://demo.cairis.org";
	private final static String authURL = CAIRISHOME+"/api/session";
	private final static String CAIRISdb = CAIRISHOME+"/api/settings/database";
	
	
	/*
	 * urn:bu:udf:cairis:risk:level:asset
	 */
	@Path("api/risk_level/asset/{asset_name}/{threshold}/{transID}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response asset(
			@PathParam("asset_name") String asset_name,
			@PathParam("threshold") Integer threshold,
			@PathParam("transID") String transID){
		System.out.println("Cairis service - Risk level asset");
		System.out.println("\nasset_name: " + asset_name);
		System.out.println("\nthreshold: " + threshold);

		String CairisURL = "";
		
		int int_risk_value;
		
		if(asset_name.contains("_"))
			asset_name = asset_name.replace("_", "%20");
		
		CairisURL = CAIRISHOME+"/api/risk_level/asset/"+asset_name;
		
		LogUtil.writeLog(transID, "Cairis module in action", 2);
		
		// Execution of the core part of the module
		int_risk_value = CairisEngine.coreExecute(CairisURL, transID, authURL, CAIRISdb);
		
		if(int_risk_value == -1)
			return null;
			
		
			/* TEST
			 * 
			 * just testing the module CairisService, retrieving risk from
			 * a sfx_cairis_risks table that has to be built ad hoc 

		risk_value = DBFns.getHighestRiskValuefromResource(asset_name);
		int_risk_value = Integer.valueOf(risk_value);
			
			  end TEST 
			 */
			
		System.out.println("\nHighest risk for " + asset_name.replace("%20", " ") 
				+ " is: " + int_risk_value + "\n");		
		
		LogUtil.writeLog(transID, "\nAsset: "+asset_name.replace("%20", " ")	
				+ "\nThreshold: "+threshold
				+ "\nHighest risk is: "+int_risk_value, 2);
		
		boolean accepted = EvaluationMethods.evaluateThreshold(int_risk_value, threshold, transID);	
		
		return DataUtil.buildResponse(DataUtil.getJSONFromBool(accepted));
		 
	}
/*

	/*
	 * urn:bu:udf:cairis:risk:level:asset:threat:type
	 */
	@Path("api/risk_level/asset/threat_type/{asset_name}/{threat_name}/{threshold}/{transID}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response asset_threat(
			@PathParam("asset_name") String asset_name,
			@PathParam("threat_name") String threat_name,
			@PathParam("threshold") Integer threshold,
			@PathParam("transID") String transID){
		System.out.println("Cairis service - Risk level asset threat");
		System.out.println("\nasset_name: " + asset_name);
		System.out.println("\nthreat_name: " + threat_name);
		System.out.println("\nthreshold: " + threshold);

		String CairisURL = "";
		
		int int_risk_value;
		
		if(asset_name.contains("_"))
			asset_name = asset_name.replace("_", "%20");
		if(threat_name.contains("_"))
			threat_name = threat_name.replace("_", "%20");
		
		CairisURL = CAIRISHOME+"/api/risk_level/asset/threat_type/" + asset_name + "/" + threat_name;
		
		LogUtil.writeLog(transID, "Cairis module in action", 2);
	
		// Execution of the core part of the module
		int_risk_value = CairisEngine.coreExecute(CairisURL, transID, authURL, CAIRISdb);

		if(int_risk_value == -1)
			return null;
		
		/* TEST

		risk_value = DBFns.getHighestRiskValuefromResourceGivenThreat(asset_name, threat_name);
		int_risk_value = Integer.valueOf(risk_value);
		
		end TEST */
		
		System.out.println("\nHighest risk for " + asset_name.replace("%20", " ")
				+ " given threat " + threat_name.replace("%20", " ")
				+ " is: " + int_risk_value + "\n");

		LogUtil.writeLog(transID, "\nAsset: "+asset_name.replace("%20", " ")
				+ "\nThreat: "+threat_name.replace("%20", " ")
				+ "\nThreshold: "+threshold
				+ "\nHighest risk is: "+int_risk_value, 2);
		
		boolean accepted = EvaluationMethods.evaluateThreshold(int_risk_value, threshold, transID);	

		return DataUtil.buildResponse(DataUtil.getJSONFromBool(accepted));

	}
	
	
	/*
	 * urn:bu:udf:cairis:risk:level:asset:environment
	 */
	@Path("api/risk_level/asset/environment/{asset_name}/{environment}/{threshold}/{transID}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response asset_environment(
			@PathParam("asset_name") String asset_name,
			@PathParam("environment") String environment,
			@PathParam("threshold") Integer threshold,
			@PathParam("transID") String transID){
		System.out.println("Cairis service - Risk level asset with environment");
		System.out.println("\nasset_name: " + asset_name);
		System.out.println("\nenvironment: " + environment);
		System.out.println("\nthreshold: " + threshold);

		String CairisURL = "";

		int int_risk_value;
		
		if(asset_name.contains("_"))
			asset_name = asset_name.replace("_", "%20");
		if(environment.contains("_"))
			environment = environment.replace("_", "%20");
		
		CairisURL = CAIRISHOME+"/api/risk_level/asset/"+asset_name+"/environment/"+environment;
		
		LogUtil.writeLog(transID, "Cairis module in action", 2);
		
		// Execution of the core part of the module
		int_risk_value = CairisEngine.coreExecute(CairisURL, transID, authURL, CAIRISdb);
		
		if(int_risk_value == -1)
			return null;
		
		/* TEST

		risk_value = DBFns.getHighestRiskValuefromResourceGivenEnvironment(asset_name, environment);
		int_risk_value = Integer.valueOf(risk_value);
		
		end TEST */
		
		System.out.println("\nHighest risk for " + asset_name.replace("%20", " ")
				+ " with "+environment.replace("%20", " ")+" as environment is: "
				+ int_risk_value + "\n");
		
		LogUtil.writeLog(transID, "\nAsset: "+asset_name.replace("%20", " ")
				+ "\nEnvironment: "+environment.replace("%20", " ")
				+ "\nThreshold: "+threshold
				+ "\nHighest risk is: "+int_risk_value, 2);

		boolean accepted = EvaluationMethods.evaluateThreshold(int_risk_value, threshold, transID);	

		return DataUtil.buildResponse(DataUtil.getJSONFromBool(accepted));
		 
	}
	
	
	/*
	 * urn:bu:udf:cairis:risk:level:asset:threat:type:environment
	 */
	@Path("api/risk_level/asset/threat_type/environment/{asset_name}/{threat_name}/{environment}/{threshold}/{transID}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response asset_threat_environment(
			@PathParam("asset_name") String asset_name,
			@PathParam("threat_name") String threat_name,
			@PathParam("environment") String environment,
			@PathParam("threshold") Integer threshold,
			@PathParam("transID") String transID){
		System.out.println("Cairis service - Risk level asset threat with environment");
		System.out.println("\nasset_name: " + asset_name);
		System.out.println("\nthreat_name: " + threat_name);
		System.out.println("\nenvironment: " + environment);
		System.out.println("\nthreshold: " + threshold);
		
		String CairisURL = "";
		int int_risk_value;

		
		if(asset_name.contains("_"))
			asset_name = asset_name.replace("_", "%20");
		if(threat_name.contains("_"))
			threat_name = threat_name.replace("_", "%20");
		if(environment.contains("_"))
			environment = environment.replace("_", "%20");
		
		CairisURL = CAIRISHOME+"/api/risk_level/asset/threat_type/"+asset_name+"/"+threat_name+"/environment/"+environment;
		
		LogUtil.writeLog(transID, "Cairis module in action", 2);

		// Execution of the core part of the module
		int_risk_value = CairisEngine.coreExecute(CairisURL, transID, authURL, CAIRISdb);

		if(int_risk_value == -1)
			return null;
		
		/* TEST

		risk_value = DBFns.getHighestRiskValuefromResourceGivenThreatandEnvironment(asset_name, threat_name, environment);
		int_risk_value = Integer.valueOf(risk_value);
		
		end TEST */
		
		System.out.println("\nHighest risk for " + asset_name.replace("%20", " ") + " given threat " 
				+ threat_name.replace("%20", " ") + " with "+environment.replace("%20", " ")
				+" as environment is " + int_risk_value + "\n");
		
		LogUtil.writeLog(transID, "\nAsset: "+asset_name.replace("%20", " ")
				+ "\nThreat: "+threat_name.replace("%20", " ")
				+ "\nEnvironment: "+environment.replace("%20", " ")
				+ "\nThreshold: "+threshold
				+ "\nHighest risk is: "+int_risk_value, 2);
	
		boolean accepted = EvaluationMethods.evaluateThreshold(int_risk_value, threshold, transID);	
	
		return DataUtil.buildResponse(DataUtil.getJSONFromBool(accepted));

	}
	

	
	
	
}
	