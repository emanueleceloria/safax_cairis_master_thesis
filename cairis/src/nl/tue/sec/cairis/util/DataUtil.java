package nl.tue.sec.cairis.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DataUtil {
	
	public final static Map<Object,Object> asMap(JSONObject j)	{
		  return j;
		}

	@SuppressWarnings("unchecked")
	public static JSONObject MapToJSON(LinkedHashMap<String, String> lmap){
		JSONObject response=new JSONObject();
		Iterator<Entry<String, String>> it = lmap.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String,String> pairs = (Map.Entry<String,String>)it.next();
			response.put(pairs.getKey(), pairs.getValue());
		}
		return response;
	}
	
	@SuppressWarnings("unchecked")
	public static JSONArray MapToJSON(ArrayList<LinkedHashMap<String, String>> lmap){
		JSONArray response=new JSONArray();
		
		for(LinkedHashMap<String,String> map:lmap){
			response.add(MapToJSON(map));
		}
		
		return response;
	}
	
	public static ArrayList<LinkedHashMap<String,String>> JSONArrayToMap(JSONArray jArray){
		ArrayList<LinkedHashMap<String,String>> lmap=new ArrayList<LinkedHashMap<String,String>>();
		for(int i=0;i<jArray.size();i++)
			lmap.add(JSONToMap((JSONObject) jArray.get(i)));
		
	return null;
	}
	
	@SuppressWarnings("unchecked")
	public static LinkedHashMap<String,String> JSONToMap(JSONObject json){
		LinkedHashMap<String,String> lhmap=new LinkedHashMap<String,String>();
		
		Set<String> keyset = json.keySet();
		Iterator<String> it=keyset.iterator();
		while(it.hasNext()){
			it.next();
			lhmap.put(it.next(), (String) json.get(it.next()));
		}
	return lhmap;
	}
	
	public static JSONArray StringTOJSONArray(String jsonString){
		JSONParser jp=new JSONParser();
		JSONArray jarray=new JSONArray();
		try {
				jarray=(JSONArray) jp.parse(jsonString);

			} catch (ParseException e) {e.printStackTrace();}
		
		return jarray;
	}
	
	public static ArrayList<String> convertToList(String...args){
		ArrayList<String> al=new ArrayList<String>();
		for(String a:args)
			al.add(a);
		return al;
		
	}

	@SuppressWarnings("unchecked")
	public static JSONObject getJSONFromInt(int response) {
		JSONObject j=new JSONObject();
		j.put("response", Integer.toString(response));
		return j;
	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject getJSONFromDouble(double response) {
		JSONObject j=new JSONObject();
		j.put("response", Double.toString(response));
		return j;
	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject getJSONFromString(String response) {
		JSONObject j=new JSONObject();
		j.put("response", response);
		return j;
	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject getJSONFromBool(boolean response) {
		JSONObject j=new JSONObject();
		j.put("response", Boolean.toString(response));
		return j;
	}
	
	public static String generalResponseWithCookieInfo(String status, String message, String data, String cookie){
		JSONObject json=new JSONObject();
		
		asMap(json).put("response", status);
		asMap(json).put("message", message);
		asMap(json).put("data", data);
		asMap(json).put("cookie", cookie);
		return json.toString();
	}
	
	public static Response buildResponse(JSONObject json){
		return Response.status(200).entity(json.toJSONString()).build();
	}

	public static Response buildResponse(JSONArray json){
		return Response.status(200).entity(json.toJSONString()).build();
	}
	
}
