package nl.tue.sec.safax.sfxbe.ws;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;

import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

import nl.tue.sec.safax.sfxbe.impl.DemoHandlerImpl;
import nl.tue.sec.safax.sfxbe.util.DataUtil;
import nl.tue.sec.safax.sfxbe.util.SafaxUtil;

@Path("/demo")
public class DemoHandler {
	
	@Path("/can/edit/{demoid}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response canEditDemo(@CookieParam("sfxsession") String sessionid, @PathParam("demoid") String demoid){
		
		String uname=SafaxUtil.isValidSession(sessionid);
		if(uname==null || uname.length()<1)
			return Response.status(400).entity(DataUtil.generalResponse("false", "Invalid Session", "")).build();
		DemoHandlerImpl demoImpl=new DemoHandlerImpl();
		String response=demoImpl.canEditDemo(demoid,uname,sessionid);
		return Response.status(200).entity(response).build();
	}
	
	@Path("/fetch/{demoid}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetchDemo(@CookieParam("sfxsession") String sessionid, @PathParam("demoid") String demoid){
		
		String uname=SafaxUtil.isValidSession(sessionid);
		if(uname==null || uname.length()<1)
			return Response.status(400).entity(DataUtil.generalResponse("false", "Invalid Session", "")).build();
		DemoHandlerImpl demoImpl=new DemoHandlerImpl();
		String response=demoImpl.fetchDemo(uname, demoid);
		return Response.status(200).entity(response).build();
	}
	
	@Path("/update/config/{demoid}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateDemoConfig(@CookieParam("sfxsession") String sessionid, @PathParam("demoid") String demoid,
									 @QueryParam("configkey") String configkey, @QueryParam("configvalue") String configvalue){
		String uname=SafaxUtil.isValidSession(sessionid);
		if(uname==null || uname.length()<1)
			return Response.status(400).entity(DataUtil.generalResponse("false", "Invalid Session", "")).build();
		DemoHandlerImpl demoImpl=new DemoHandlerImpl();
		return Response.status(200).entity(demoImpl.updateDemoConfig(configkey, configvalue, demoid, uname, sessionid)).build();
	}
	
	@Path("/config/{demoid}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetchDemoConfig(@CookieParam("sfxsession") String sessionid, @PathParam("demoid") String demoid){
		
		String uname=SafaxUtil.isValidSession(sessionid);
		if(uname==null || uname.length()<1)
			return Response.status(400).entity(DataUtil.generalResponse("false", "Invalid Session", "")).build();
		DemoHandlerImpl demoImpl=new DemoHandlerImpl();
		String response=demoImpl.fetchDemoConfig(uname, demoid);
		return Response.status(200).entity(response).build();
	}
	
	@Path("/pdp/host/{demoid}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDemoPDPHost(@CookieParam("sfxsession") String sessionid, @PathParam("demoid") String demoid){
		
		String uname=SafaxUtil.isValidSession(sessionid);
		if(uname==null || uname.length()<1)
			return Response.status(400).entity(DataUtil.generalResponse("false", "Invalid Session", "")).build();
		DemoHandlerImpl demoImpl=new DemoHandlerImpl();
		String response=demoImpl.getDemoPDPHost(uname, demoid);
		return Response.status(200).entity(response).build();
	}
	
	@Path("/edit/config/{demoid}")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response editDemoConfig(@CookieParam("sfxsession") String sessionid, 
								   @PathParam("demoid") String demoid,
								   @FormParam("configkey") String configkey,
								   @FormParam("configvalue") String configvalue){
		
		String uname=SafaxUtil.isValidSession(sessionid);
		if(uname==null || uname.length()<1)
			return Response.status(400).entity(DataUtil.generalResponse("false", "Invalid Session", "")).build();
		DemoHandlerImpl demoImpl=new DemoHandlerImpl();
		String response=demoImpl.editDemoConfig(uname, demoid,configkey,configvalue,sessionid);
		return Response.status(200).entity(response).build();
	}
	
	@Path("/edit/{demoid}")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response editDemo(@CookieParam("sfxsession") String sessionid, @PathParam("demoid") String demoid,
							@FormParam("demoname") String demoname, @FormParam("moveproject") String projectid,
							@FormParam("editdemodesc") String demodesc,
							@FormParam("pdpcode") String pdpcode, @FormParam("ispersistent") String ispersistent,
							@FormParam("pdpservice") String pdpservice, @FormParam("pipservice") String pipservice,
							@FormParam("pepservice") String pepservice, @FormParam("papservice") String papservice,
							@FormParam("chservice") String chservice, @FormParam("pdprcalgo") String pdprcalgo){
		
		String uname=SafaxUtil.isValidSession(sessionid);
		if(uname==null || uname.length()<1)
			return Response.status(400).entity(DataUtil.generalResponse("false", "Invalid Session", "")).build();
		DemoHandlerImpl demoImpl=new DemoHandlerImpl();
		String response=demoImpl.editDemo(demoid,demoname, uname, demodesc, projectid, pepservice, chservice, pipservice, pdpservice, papservice, pdpcode,pdprcalgo,ispersistent,sessionid);
		return Response.status(200).entity(response).build();
	}
	
	// CAIRIS updates
	@Path("/edit/cairis/{demoid}/{projectid}")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response editCairis(@CookieParam("sfxsession") String sessionid, @PathParam("demoid") String demoid,
							@PathParam("projectid") String projectid,
							@FormParam("cairisusr") String cairisusr, @FormParam("cairispwd") String cairispwd,
							@FormParam("cairisdb") String cairisdb){
		
		String uname=SafaxUtil.isValidSession(sessionid);
		if(uname==null || uname.length()<1)
			return Response.status(400).entity(DataUtil.generalResponse("false", "Invalid Session", "")).build();
		DemoHandlerImpl demoImpl=new DemoHandlerImpl();
		String response=demoImpl.editCairis(demoid, uname,cairisusr,cairispwd,cairisdb,projectid,sessionid);
		return Response.status(200).entity(response).build();
	}
	// end CAIRIS updates
	
	@Path("/get/all/{projectid}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDemos(@CookieParam("sfxsession") String sessionid, @PathParam("projectid") String projectid){
		
		String uname=SafaxUtil.isValidSession(sessionid);
		if(uname==null || uname.length()<1)
			return Response.status(400).entity(DataUtil.generalResponse("false", "Invalid Session", "")).build();
		DemoHandlerImpl demoImpl=new DemoHandlerImpl();
		String response=demoImpl.getAllDemos(uname,projectid).toJSONString();
		return Response.status(200).entity(response).build();
	}
	
	@Path("/create/{projectid}")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response createDemo(@CookieParam("sfxsession") String sessionid, @PathParam("projectid") String projectid,
								@FormParam("demoname") String demoname, @FormParam("demodesc") String demodesc){
		
		String uname=SafaxUtil.isValidSession(sessionid);
		if(uname==null || uname.length()<1)
			return Response.status(400).entity(DataUtil.generalResponse("false", "Invalid Session", "")).build();
		DemoHandlerImpl demoImpl=new DemoHandlerImpl();
		String response=demoImpl.createDemo(demoname,demodesc,uname,projectid,sessionid);
		return Response.status(200).entity(response).build();
	}
	
	
	
	@Path("/delete/{demoid}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteDemo(@CookieParam("sfxsession") String sessionid, @PathParam("demoid") String demoid){
		
		String uname=SafaxUtil.isValidSession(sessionid);
		if(uname==null || uname.length()<1)
			return Response.status(400).entity(DataUtil.generalResponse("false", "Invalid Session", "")).build();
		DemoHandlerImpl demoImpl=new DemoHandlerImpl();
		String response=demoImpl.deleteDemo(demoid,uname,sessionid);
		return Response.status(200).entity(response).build();
	}
	
	@Path("/pdpcode/{demoid}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPDPInfo(@CookieParam("sfxsession") String sessionid, @PathParam("demoid") String demoid){
		
		String uname=SafaxUtil.isValidSession(sessionid);
		if(uname==null || uname.length()<1)
			return Response.status(400).entity(DataUtil.generalResponse("false", "Invalid Session", "")).build();
		DemoHandlerImpl demoImpl=new DemoHandlerImpl();
		String response=demoImpl.getPDPInfo(demoid,uname);
		return Response.status(200).entity(response).build();
	}
	
	@Path("/get/pdps")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPDPs(@CookieParam("sfxsession") String sessionid){
		
		String uname=SafaxUtil.isValidSession(sessionid);
		if(uname==null || uname.length()<1)
			return Response.status(400).entity(DataUtil.generalResponse("false", "Invalid Session", "")).build();
		DemoHandlerImpl demoImpl=new DemoHandlerImpl();
		String response=demoImpl.getPDPS(uname).toJSONString();
		return Response.status(200).entity(response).build();
	}
	
	@Path("/copy/{demoid}/{prid}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response copyDemo(@CookieParam("sfxsession") String sessionid, @PathParam("demoid") String demoid,@PathParam("prid") String prid){
		System.out.println("Copy a demo HERE");
		String uname=SafaxUtil.isValidSession(sessionid);
		if(uname==null || uname.length()<1)
			return Response.status(400).entity(DataUtil.generalResponse("false", "Invalid Session", "")).build();
		
		DemoHandlerImpl demoImpl=new DemoHandlerImpl();
		String response=demoImpl.copyDemo(demoid,prid,uname,sessionid);
		return Response.status(200).entity(response).build();
	}
	
	
	@Path("/load/attributes/{demoid}")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response loadAttributes(@CookieParam("sfxsession") String sessionid, 
								   @PathParam("demoid") String demoid,
								   FormDataMultiPart formParams){
		
		String uname=SafaxUtil.isValidSession(sessionid);
		if(uname==null || uname.length()<1)
			return Response.status(400).entity(DataUtil.generalResponse("false", "Invalid Session", "")).build();
		  Map<String, List<FormDataBodyPart>> fieldsByName = formParams.getFields();
		    ArrayList<String> requests=new ArrayList<String>();
		    for (List<FormDataBodyPart> fields : fieldsByName.values()){
				for (FormDataBodyPart field : fields)
		        {
					if(field.getName().equalsIgnoreCase("attributes") && field.getContentDisposition().getFileName().length()>0){
		        		InputStream is = field.getValueAs(InputStream.class);
		        		System.out.println(field.getContentDisposition().getFileName());
		        		try {
		        			requests.add(IOUtils.toString(is, "UTF-8"));
							} catch (IOException e) {
							}
		        }
		    }
		   }
		    
		DemoHandlerImpl impl=new DemoHandlerImpl();
		String response="";
		if(requests.size()>0)
			response=impl.loadAttributes(requests.get(0),demoid,sessionid);
		else
			response=DataUtil.generalResponse("false", "Invalid file", ""); 
		return Response.status(200).entity(response).build();
	}
	@Path("/get/attributes/{demoid}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAttributes(@CookieParam("sfxsession") String sessionid,
								  @PathParam("demoid") String demoid, 
								  @QueryParam("refid") String refid, 
								  @QueryParam("root") String isroot){
		
		String uname=SafaxUtil.isValidSession(sessionid);
		if(uname==null || uname.length()<1)
			return Response.status(400).entity(DataUtil.generalResponse("false", "Invalid Session", "")).build();
		DemoHandlerImpl demoImpl=new DemoHandlerImpl();
		String response=demoImpl.getAttributes(demoid,isroot,refid);
		return Response.status(200).entity(response).build();
	}
	
	
	/*
	 * Added interface services for transparency
	 */

	@Path("/transparency/viewpoint/{demoid}/{viewpoint}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getViewpoint(@CookieParam("sfxsession") String sessionid,@PathParam("viewpoint") String viewpoint, @PathParam("demoid") String demoid){
		String uname=SafaxUtil.isValidSession(sessionid);
		if(uname==null || uname.length()<1)
			return Response.status(400).entity(DataUtil.generalResponse("false", "Invalid Session", "")).build();
		
		DemoHandlerImpl impl=new DemoHandlerImpl();
		String response = impl.getViewpoint(demoid,viewpoint);
		System.out.println("RESPONSE "+ response+" END");
		return Response.status(200).entity(response).build();
	}
	
	@Path("/transparency/{demoid}/{viewpoint}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTransparency(@CookieParam("sfxsession") String sessionid,@PathParam("viewpoint") String viewpoint, @PathParam("demoid") String demoid){
		String uname=SafaxUtil.isValidSession(sessionid);
		if(uname==null || uname.length()<1)
			return Response.status(400).entity(DataUtil.generalResponse("false", "Invalid Session", "")).build();
		
		DemoHandlerImpl impl=new DemoHandlerImpl();
		return Response.status(200).entity(impl.getTransparency(demoid,viewpoint)).build();
	}

	@Path("/get/transparency/users/{demoid}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTransparencyUsers(@CookieParam("sfxsession") String sessionid,@PathParam("demoid") String demoid){
		String uname=SafaxUtil.isValidSession(sessionid);
		if(uname==null || uname.length()<1)
			return Response.status(400).entity(DataUtil.generalResponse("false", "Invalid Session", "")).build();
		
		DemoHandlerImpl impl=new DemoHandlerImpl();
		return Response.status(200).entity(impl.getTransparencyUsers(demoid)).build();
	}
	
	@Path("/all/transparency")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTransparencyAll(@CookieParam("sfxsession") String sessionid){
		String uname=SafaxUtil.isValidSession(sessionid);
		if(uname==null || uname.length()<1)
			return Response.status(400).entity(DataUtil.generalResponse("false", "Invalid Session", "")).build();
		
		DemoHandlerImpl impl=new DemoHandlerImpl();
		return Response.status(200).entity(impl.getTransparencyAll()).build();
	}
	
	@Path("/save/transparency/{demoid}")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response savePreferences(@CookieParam("sfxsession") String sessionid,
									@PathParam("demoid") String demoid,
									@FormParam("selectedconflicts") List<String> selectedconflicts,
									@FormParam("viewpoint") String viewpoint
								  ){
		System.out.println("INDEMOHANDLER"+selectedconflicts.size());
			String uname=SafaxUtil.isValidSession(sessionid);
			if(uname==null || uname.length()<1)
				return Response.status(400).entity(DataUtil.generalResponse("false", "Invalid Session", "")).build();
			DemoHandlerImpl projImpl=new DemoHandlerImpl();
			String response=projImpl.savePreferences(demoid,selectedconflicts,viewpoint);
			return Response.status(200).entity(response).build();
	}
	
	@Path("/transparency/add/viewpoint/{demoid}/{viewpoint}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response addViewpoint(@CookieParam("sfxsession") String sessionid,
									@PathParam("demoid") String demoid,
									@PathParam("viewpoint") String viewpoint
								  ){
		System.out.println("AddUSER:  "+viewpoint);
			String uname=SafaxUtil.isValidSession(sessionid);
			if(uname==null || uname.length()<1)
				return Response.status(400).entity(DataUtil.generalResponse("false", "Invalid Session", "")).build();
			DemoHandlerImpl projImpl=new DemoHandlerImpl();
			String response=projImpl.addViewpoint(demoid,uname,viewpoint);
			return Response.status(200).entity(response).build();
	}
	
	@Path("/transparency/remove/viewpoint/{demoid}/{viewpoint}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeViewpoint(@CookieParam("sfxsession") String sessionid,
									@PathParam("demoid") String demoid,
									@PathParam("viewpoint") String viewpoint
								  ){
		System.out.println("RemoveUSER:  "+viewpoint);
			String uname=SafaxUtil.isValidSession(sessionid);
			if(uname==null || uname.length()<1)
				return Response.status(400).entity(DataUtil.generalResponse("false", "Invalid Session", "")).build();
			DemoHandlerImpl projImpl=new DemoHandlerImpl();
			String response=projImpl.removeViewpoint(demoid,viewpoint);
			return Response.status(200).entity(response).build();
	}
	/* 
	 * END OF THE CHANGE
	 */

}
