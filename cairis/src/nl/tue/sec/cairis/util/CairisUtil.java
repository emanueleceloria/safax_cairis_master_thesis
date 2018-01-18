package nl.tue.sec.cairis.util;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;

public class CairisUtil {
private final static String COMPKEY="abc01def-g456-78h2-45u5-0821l09332m1";
	
	public static String writeLog(String transactionid, int level,String message){
		String papurl="http://localhost/sr/store/log";
		Form form=new Form();
		form.add("clientcode",COMPKEY);
		form.add("transactionid",transactionid);
		form.add("level",level);
		form.add("message",message);
		form.add("serviceid","nl:tue:sec:cairis");
		WebResource webResource = Client.create().resource(papurl);
		ClientResponse response= webResource.type(MediaType.APPLICATION_FORM_URLENCODED)
	               										.post(ClientResponse.class,form);
		String resp=response.getEntity(String.class);
		if(response.getStatus()==200)
			return resp;
		return "";
	} 

}
