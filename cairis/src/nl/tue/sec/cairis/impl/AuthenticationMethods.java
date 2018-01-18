package nl.tue.sec.cairis.impl;

import javax.xml.bind.DatatypeConverter;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class AuthenticationMethods {

	public static ClientResponse httpBasicAuth(String username, String passwd, String authURL){
		
		Client client = Client.create();
		WebResource webResource;
		ClientResponse response;

		String authString = username + ":" + passwd;
		
//		  This commented rows can only be used with Java 8 - jdk 1.8
//		  which includes the java.util.Base64 library in order to use the
//		  encoding functions
//
//      byte[] authEncodedBytes = Base64.getEncoder().encode(authString.getBytes());
//      String encoded = new String(authEncodedBytes);

        byte[] message = authString.getBytes();
        String encoded = DatatypeConverter.printBase64Binary(message); 
		
		webResource = client.resource(authURL);
		response= webResource.type("application/json").header("Authorization", "Basic " + encoded)
				.post(ClientResponse.class);
		
		return response;
	}
	
	public static ClientResponse simpleAuth(String cairisURL){
		
		Client client = Client.create();
		WebResource webResource;
		ClientResponse response;
		
		String CairisURL = "";
		CairisURL = cairisURL+"?session_id=test";
		webResource = client.resource(CairisURL);
		response= webResource.get(ClientResponse.class);
		System.out.println("Contacting "+CairisURL+" - status: "+response.getStatus()+ " - info: "+response.getStatusInfo());

		return response;
	}
}
