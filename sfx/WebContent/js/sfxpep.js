/**
 * @author: Samuel Kaluvuri
 */
var poll_xhr;
var is_ucon_request = false;
var is_transparency_request = false;
var ucon_first_request = true;
var abort_poll_ucon_session = false;
var ready_to_send_ajax_request = false;
var ucon_session = "";
var ucon_update_row = 0;
var ucon_live_log_content = "";
var transparency_notification_content = "";
var existingPolling = false;

$(document).ready(function(){
	var $demoid=getParameterByName('demoid');
	var $selectpdpcode=init($demoid);

	/*************** MENU *********************/
	$("#mpep").removeClass('menubox').addClass('menuboxactive');
	$("#mhome").click(function(){
		restartUconSession(ucon_session, $selectpdpcode);
		window.location.href = "./main.html";
	});
	
	$("#mpep").click(function(){
		restartUconSession(ucon_session, $selectpdpcode);
		window.location.href = "./evaluate.html";
	});
	$("#msr").click(function(){
		restartUconSession(ucon_session, $selectpdpcode);
		window.location.href = "./registry.html";
	});
	$("#mact").click(function(){
		restartUconSession(ucon_session, $selectpdpcode);
		window.location.href = "./activity.html";
	});
	$("#mtrack").click(function(){
		restartUconSession(ucon_session, $selectpdpcode);
		window.location.href = "./tracker.html";
	});
	$("#mabt").click(function(){
		restartUconSession(ucon_session, $selectpdpcode);
		window.location.href = "./about.html";
	});
	$("#madmin").click(function(){
		restartUconSession(ucon_session, $selectpdpcode);
		window.location.href = "./admin.html";
	});
	$("#msetting").click(function(){
		restartUconSession(ucon_session, $selectpdpcode);
		window.location.href = "./settings.html";
	});
	$("#mlogout").click(function(){
		restartUconSession(ucon_session, $selectpdpcode);
		logoutUser();
	});
	$("#mhelp").click(function(){
		restartUconSession(ucon_session, $selectpdpcode);
		window.location.href = "./help.html";
	});
	$("#uconanalyzetracebutton").click(function(){
		console.log("uconanalyzetracebutton clicked");
		restartUconSession(ucon_session, $selectpdpcode);
		window.location.href = "./activity.html";
	});
	
	/*
	 * Evaluate a request
	 */
	$("#loadavailablerequest").click(function(){
		console.log("before if ucon_first_request");
		var message=sendAjaxRequestXMLResponse("../sfxservice/policy/isucon/request/" + $selectpdpcode, "GET", "text","", "");
		console.log("message from isucon request: " + message.responseText);
		if (!message.responseText) {
			console.log("This is NOT ucon request");
			// Remove previous ucon session
			if (is_ucon_request) {
				/*
				 * Should delete all data belong to this session
				 * And then create new session for ucon evaluation
				 */
				restartUconSession(ucon_session, $selectpdpcode);
			}
			// Set current request -> not ucon
			is_ucon_request = false;
		}
		else {
			console.log("This is ucon request");
			is_ucon_request = true;
		}
		if (is_ucon_request && ucon_first_request) {
			abort_poll_ucon_session = false;
			ucon_live_log_content = "";
   			$("#liveuconresponse").val(ucon_live_log_content);
			var message = sendAjaxRequestXMLResponse("../sfxservice/policy/create/ucon_session", "GET", "text","", "");
			ucon_session = message.responseText;
		
			console.log("Evaluate first UCON Request");
			sendAjaxRequestXMLResponse("../sfxservice/policy/load/request/ucon/first/" + $selectpdpcode + "/" + $('#availablerequests').val() + "/" + ucon_session, "", "");
			$("#uconrestartbutton").show();
   	       	$("#uconlivesection").show();
   	       	$("#uconresponseactionboxmenu").show();
   	       	$("#uconresponseupdateboxmenu").show();
   	       	$("#uconresponsepipboxmenu").show();
   	       	$("#responsemenunoucon").hide();
   	       	$("#responsemenuuconbutton").show();
   	       	
   	  	    /*
   	       	 * Get UCON Request/Action 
   	       	 */
   	       getUconRequestAction($selectpdpcode);
 	       	
   	       /*
   	        * Get UCON UPDATE 
   	        */
   	       getUconUpdate($selectpdpcode);
   	       
   	       /*
   	        * Get UCON Log 
   	        */
   	       getUconLog();
   	       

  	       /*
  	        * Get UCON PIP
  	        */
   	       getUconPIP();
   	        
   	       	/*
   	       	 * ToDo
   	       	 * Long Polling - An Efficient Server-Push Technique
   	       	 * Applications built with Long Polling in mind attempt to offer real-time server interaction, 
   	       	 * using a persistent or long-lasting HTTP connection between the server and the client.
   	       	 */
   	       	
   	       	/*
   	       	 * XMLHttpRequest Long Polling is a web application model in which a long-held HTTP request allows a web server to push data to a browser, 
   	       	 * without the browser explicitly requesting it.
   	       	 */
   	       	
   	       	/*
   	       	 * Long polling addresses the weakness of traditional polling by keeping the connection to your server open. 
   	       	 */
   	       	
   	       	/*
   	       	 * Comet is implemented using the Comet Processor/Comet Event interfaces in Tomcat
   	       	 */
   	       	
   	       	//	sendAjaxRequestXMLResponse("../sfxservice/policy/load/request/ucon/first/" + $selectpdpcode + "/" + $('#availablerequests').val() + "/" + ucon_session, "", "");
   	       	
	   	     /* Long Polling */
	   	    (function poll(){
	   	    	  existingPolling = true;
	   	    	  console.log("ABC Send request to SAFAX server to poll");
	   	    	  poll_xhr = $.ajax({ url: "../sfxservice/policy/poll/ucon_session/" + ucon_session, 
	   	    		  success: function(data){
		   	    		  $.each(data, function (index, value) {
		   	    			  console.log(value.attributevalue);
		   	    			  var id_string = "#pip_" +  value.attributeid;
		   	    			  
		   	    			  console.log("id_string is: " + id_string);
		   	    			  
		   	    			  $(id_string).html(value.attributevalue).fadeIn(1000);
		   	    		      $(id_string).effect( "highlight", {color:"#ffff99"}, 3000 );
		   	    		      
		   	    			 /*
		   	    			  * Get UCON Log 
		   	    			  */	 
		   	    			ucon_live_log_content = ucon_live_log_content + value.attributeid + " value changed" + "\n";
		   	    		    $("#liveuconresponse").val(ucon_live_log_content);
		   	    		  });
	   	    		  }, 
		   	    	   dataType: "json", 
		   	    	   complete: function() {
		   	    		   console.log("complete in poll");
		   	    		   if (abort_poll_ucon_session) {
		   	    			   console.log("abort_poll_ucon_session is TRUE");
		   	    			   poll_xhr.abort();
		   	    			   console.log("send new request to STOP ucon session");
		   	    			   console.log("FINISH sending new request to STOP ucon session");
		   	    			   /*
		   	    			    * Stop, delete this current ucon session
		   	    			    */
		   	    			  // restartUconSession(ucon_session, $selectpdpcode);
		   	    			  getUconLog();
		   	    	     	  ucon_live_log_content = ucon_live_log_content + "Polling stopped" + "\n";
		   	    		      $("#liveuconresponse").val(ucon_live_log_content);
		   	    		      
		   	    		     /*
     		   	      	      * Get UCON Request/Action 
		   	      	       	  */
		   	      	          getUconRequestAction($selectpdpcode);
		   	      	          existingPolling = false;
		   	    		   }
		   	    		   else {
		   	    			   console.log("abort_poll_ucon_session is FALSE -> CONTINUE POLL");
		   	    			   ready_to_send_ajax_request = false;
		   	    			   ucon_live_log_content = ucon_live_log_content + "Polling server for changes..." + "\n";
		   	    			   $("#liveuconresponse").val(ucon_live_log_content);
				   	    	   /*
				   	   	   	    * Get UCON UPDATE 
				   	   	   	    */
		   	    			   console.log("$selectpdpcode is: " + $selectpdpcode);
		   	    			   getUconUpdate($selectpdpcode);
		   	    			   getUconLog();
		   	    			   poll();   
		   	    			   existingPolling = true;
		   	    		   }
		   	    	   },
		   	    	   timeout: 30000 });
	   	     	})();
	   		ucon_first_request = false;
		 }
		 
		else if (is_ucon_request && !ucon_first_request) {
			 console.log("Evaluate second or third ... UCON Request");
			 sendAjaxRequestXMLResponse("../sfxservice/policy/load/request/ucon/" + $selectpdpcode + "/" + $('#availablerequests').val() + "/" + ucon_session, "", "");
			 /*
	   	      * Get UCON Request/Action 
	   	      */
			 getUconRequestAction($selectpdpcode);
			
			 /*
	   	      * Get UCON UPDATE 
	   	      */
			 getUconUpdate($selectpdpcode);
			 
		     /*
     	      * Get UCON Log 
	   	      */
	   	     getUconLog();
	   	     
	   	     if (abort_poll_ucon_session) {
	   	    	 /*
	     	      * Get UCON PIP 
		   	      */
		   	     getUconPIP();
	   	     }
		 }
		
		 else if (!is_ucon_request) {			 
			 var message=sendAjaxRequestXMLResponse("../sfxservice/policy/istransparency/request/" + $selectpdpcode, "GET", "text","", "");
			 console.log("message from istransparency request: " + message.responseText);
			 if (!message.responseText){
				 console.log("This is a normal request");
				 $("#uconrestartbutton").hide();
			     $("#uconlivesection").hide();
			     $("#uconresponseactionboxmenu").hide();
			     $("#uconresponseupdateboxmenu").hide();
			     $("#uconresponsepipboxmenu").hide();
			     $("#responsemenunoucon").show();
			     $("#responsemenuuconbutton").hide();
			     $("#transparencynotificationboxmenu").hide();
			     is_transparency_request = false;
			 }
			 else {
				 console.log("This is a transparency request");
				 $("#uconrestartbutton").hide();
			     $("#uconlivesection").hide();
			     $("#uconresponseactionboxmenu").hide();
			     $("#uconresponseupdateboxmenu").hide();
			     $("#uconresponsepipboxmenu").hide();
			     $("#responsemenunoucon").show();
			     $("#transparencynotificationboxmenu").show();
			     $("#responsemenuuconbutton").hide();
			     is_transparency_request = true;
			 }
			 
			 console.log("Evaluate NON UCON Request");
			 console.log($('#availablerequests').val());
			 sendAjaxRequestXMLResponse("../sfxservice/policy/load/request/"+$selectpdpcode+"/"+$('#availablerequests').val(), "", "");
			 is_ucon_request = false;
		 }
	});

	$('.pdprow').click(function(){
		console.log("click pdp row");
		$selectpdpcode=$(this).data("pdpcode");
		console.log("$selectpdpcode: " + $selectpdpcode);
		$(this).siblings('.activeproject').removeClass('activeproject fg-white').addClass('fg-darker');
		$(this).removeClass('fg-darker').addClass('activeproject fg-white');
		$("#requestuploadform :input").prop("disabled", false);
		loadRequests($(this).data('demoid'));
	});
	
	$("#requestxacml").on('change', function(){
		if(isFileUploadEmpty("#requestxacml")) return false;
		var formData = new FormData(document.forms.namedItem("requestuploadform"));
		var message=sendAjaxRequestFile("../sfxservice/policy/upload/request/"+$selectpdpcode, "xml",formData);
		resetForm("requestuploadform");
		return false;
	});
	
	/*
	 * Restart Ucon Session
	 */
	$('#uconrestartbutton').click(function(){
		console.log("restart ucon session CLICKED");
		restartUconSession(ucon_session, $selectpdpcode);
	});
});

/*
 * Stop, delete this current ucon session
 */
function restartUconSession(ucon_session, selectpdpcode){
	if (is_ucon_request) {
		console.log("restartUconSession");
		console.log("ucon_session: " + ucon_session);
		console.log("selectpdpcode: " + selectpdpcode);
		abort_poll_ucon_session = true;
		console.log("send new request to STOP ucon session");			   
		var message = sendAjaxRequestXMLResponse("../sfxservice/policy/delete/ucon_session/" + ucon_session + "/" + selectpdpcode, "GET", "text","", "");
		$("#safaxresponse").text("");
		
		ucon_first_request = true;
		is_ucon_request = false;
		ucon_session = "";
		
		/*
		 * delete all table rows except first
		 */
		$("#table_uconresponserequestboxmenu").find("tr:gt(0)").remove();	  
	    $("#table_uconresponseupdateboxmenu").find("tr:gt(0)").remove();	  
		$("#table_uconresponsepipboxmenu").find("tr:gt(0)").remove();
	}
}

function getUconRequestAction(selectpdpcode){
	/*
   	 * Get UCON Request/Action 
   	 */
     var message = sendAjaxRequest("../sfxservice/policy/poll/initial/ucon/request/" + ucon_session, "GET", "json","", "");
     console.log("poll/initial/ucon/request");
     
	 /*
      * delete all table rows except first
	  */
	 $("#table_uconresponserequestboxmenu").find("tr:gt(0)").remove();
     
     $.each(message, function (index, value) {
		console.log("Action/Request: " + value.requestid); // Bob,1,Call.xml
		console.log("Trigger: " + value.trigger); // Credit
		console.log("request_sessionid: " + value.request_sessionid); 
		
		var table_row = "<tr id = 'row_" + value.request_sessionid +"'> "  
			              + "<td>" + value.requestid + "</td>" 
			              + "<td>" + value.trigger_interval + " seconds" + "</td>" 
			              + "<td>" + "<button class = 'small inverse' id = '" + value.request_sessionid + "'>" + "Stop" + "<i class='icon-console on-right' style='padding-left:10px;'></i></button>" 
			              + "</tr>";
		$('#table_uconresponserequestboxmenu tr:last').after(table_row);	
		var createdElement = document.getElementById(value.request_sessionid);
		createdElement.onclick = function() {
			console.log("Stop/Remove request session id= " + value.request_sessionid); // onclick stuff
			console.log("send new request to STOP ucon REQUEST session");
			var message = sendAjaxRequestXMLResponse("../sfxservice/policy/delete/ucon_request_session/" + ucon_session + "/" + value.request_sessionid + "/" + selectpdpcode, "GET", "text","", "");
			$("#safaxresponse").text("");
			console.log("FINISH sending new request to STOP ucon REQUEST session");
			var row_id = "#row_" + value.request_sessionid;
			$(row_id).remove();
				
				 /*
				  * Get UCON UPDATE 
				  */
				getUconUpdate(selectpdpcode);
		};
    });	
}

function getUconUpdate(selectpdpcode){
	 /*
	  * Get UCON UPDATE 
	  */
	 
	 /*
	  * delete all table rows except first
	  */
     $("#table_uconresponseupdateboxmenu").find("tr:gt(0)").remove();
	var message = sendAjaxRequest("../sfxservice/policy/poll/initial/ucon/update/" + ucon_session, "GET", "json","", "");
    console.log("poll/initial/ucon/update");
    ucon_update_row = 0;
    $.each(message, function (index, value) {
		console.log("updatetype: " + value.updatetype); // OnUpdate
		console.log("Action/Request: " + value.requestid); // Bob,1,Call.xml
		console.log("Trigger: " + value.updateinterval); // 10 seconds
		var table_row = "<tr> <td>" + value.updatetype + "</td>" + "<td>" + value.requestid + "</td>" + "<td>" +  value.updateinterval + " seconds" + "</td>" + "</tr>";
		$('#table_uconresponseupdateboxmenu tr:last').after(table_row);
		ucon_update_row = ucon_update_row + 1;
	 }); 
	if (ucon_update_row > 0) {
		abort_poll_ucon_session = false;
		//getUconLog();
		
		if (!existingPolling && !ucon_first_request) {
		    /* Long Polling */
	   	    (function poll(){
	   	    	  existingPolling = true;
	   	    	  console.log("Send request to SAFAX server to poll - This is in getUconUpdate when row > 0");
	   	    	  poll_xhr = $.ajax({ url: "../sfxservice/policy/poll/ucon_session/" + ucon_session, 
	   	    		  success: function(data){
		   	    		  $.each(data, function (index, value) {
		   	    			  console.log(value.attributevalue);
		   	    			  var id_string = "#pip_" +  value.attributeid;
		   	    			  
		   	    			  console.log("id_string is: " + id_string);
		   	    			  
		   	    			  $(id_string).html(value.attributevalue).fadeIn(1000);
		   	    		      $(id_string).effect( "highlight", {color:"#ffff99"}, 3000 );
		   	    		      
		   	    			 /*
		   	    			  * Get UCON Log 
		   	    			  */	 
		   	    			ucon_live_log_content = ucon_live_log_content + value.attributeid + " value changed" + "\n";
		   	    		    $("#liveuconresponse").val(ucon_live_log_content);
		   	    		  });
	   	    		  }, 
		   	    	   dataType: "json", 
		   	    	   complete: function() {
		   	    		   console.log("complete in poll");
		   	    		   if (abort_poll_ucon_session) {
		   	    			   console.log("abort_poll_ucon_session is TRUE");
		   	    			   poll_xhr.abort();
		   	    			   console.log("send new request to STOP ucon session");
		   	    			   console.log("FINISH sending new request to STOP ucon session");
		   	    			   
		   	    			   getUconPIP();
		   	    			   
		   	    			   /*
		   	    			    * Stop, delete this current ucon session
		   	    			    */
		   	    			  // restartUconSession(ucon_session, $selectpdpcode);
		   	    			  getUconLog();
		   	    	     	  ucon_live_log_content = ucon_live_log_content + "Polling stopped" + "\n";
		   	    		      $("#liveuconresponse").val(ucon_live_log_content);
		   	    		      
		   	    		     /*
     		   	      	      * Get UCON Request/Action 
		   	      	       	  */
		   	      	          getUconRequestAction(selectpdpcode);
		   	      	          existingPolling = false;
		   	    		   }
		   	    		   else {
		   	    			   console.log("abort_poll_ucon_session is FALSE -> CONTINUE POLL");
		   	    			   ready_to_send_ajax_request = false;
		   	    			   ucon_live_log_content = ucon_live_log_content + "Polling server for changes..." + "\n";
		   	    			   $("#liveuconresponse").val(ucon_live_log_content);
				   	    	   /*
				   	   	   	    * Get UCON UPDATE 
				   	   	   	    */
		   	    			   getUconUpdate(selectpdpcode);
		   	    			   getUconLog();
		   	    			   poll();   
		   	    			   existingPolling = true;
		   	    		   }
		   	    	   },
		   	    	   timeout: 30000 });
	   	     })();
		}
	}
	else {
		abort_poll_ucon_session = true;
		getUconLog();
		//restartUconSession(ucon_session, selectpdpcode);
	}
}

function getUconLog(){
	 /*
	  * Get UCON Log 
	  */	 
	var message = sendAjaxRequest("../sfxservice/policy/poll/initial/ucon/log/" + ucon_session, "GET", "json","", "");
    console.log("poll/initial/ucon/log");
    ucon_live_log_content = "";
    $.each(message, function (index, value) {
		console.log("log content: " + value.log); // 
		ucon_live_log_content = ucon_live_log_content + value.log + "\n";
	});	
    $("#liveuconresponse").val(ucon_live_log_content);
}

function getUconPIP() {
	/*
   	 * Get UCON PIP
   	 */
    message = sendAjaxRequest("../sfxservice/policy/poll/initial/ucon/pip/" + ucon_session, "GET", "json","", "");
    console.log("poll/initial/ucon/pip");
    console.log(message);
	$.each(message, function (index, value) {
		console.log("categoryvalue: " + value.categoryvalue);
		console.log("attributevalue: " + value.attributevalue);
		console.log("attributeid: " + value.attributeid);
		
		/*
		 * Check if this PIP attribute has been already displayed
		 */
		var pipAlreadyDisplayed = false;
		$('#table_uconresponsepipboxmenu > tbody  > tr').each(function() {
			console.log("Loop each row of table");
			console.log($(this).html());
			console.log($(this).find('td').eq(0).html());
			if ($(this).find('td').eq(0).html() == value.attributeid) {
				pipAlreadyDisplayed = true;
			}
		});
		/*
		 * <td id = "pip_attributeid"> -> used to update PIP GUI with PIP updated value
		 */
		if (!pipAlreadyDisplayed) {
			var table_row = "<tr> <td>" + value.attributeid + "</td>" + "<td>" + value.categoryvalue + "</td>" + "<td id = pip_" + value.attributeid + ">" +  value.attributevalue + "</td>" + "</tr>";
  			$('#table_uconresponsepipboxmenu tr:last').after(table_row);
		}   			
		else {
			var id_string = "#pip_" +  value.attributeid;
			$(id_string).html(value.attributevalue).fadeIn(1000);
   		    $(id_string).effect( "highlight", {color:"#ffff99"}, 3000 );
		}
	});	
}



function loadRequests(demoid){
	var message=sendAjaxRequest("../sfxservice/policy/all/requests/"+demoid, "GET", "json","", "");
	if (jQuery.isEmptyObject(message)){
		$('#availablerequests').text("");
		$('#availablerequests').html("<option>None Available</option>");
		$('#availablerequests').prop("disabled", 'disabled');
		$('#loadavailablerequest').prop("disabled", true);
		return;
	}
	$('#availablerequests').text("");
	$.each(message, function (index, value) {
		console.log("value.requestid: " + value.requestid);
		$('<option/>',{'value':value.xrid}).html(value.requestid).appendTo('#availablerequests');
	});
	$('#availablerequests').prop("disabled", false);
	$('#loadavailablerequest').prop("disabled", false);
}

function loadPDPS(demoid){
	var message=sendAjaxRequest("../sfxservice/demo/get/pdps", "GET", "json","", "");
	var pdpcode="";
	$("#demopdps").text("");
	$.each(message, function (index, value) {
		var $row=$("<tr/>");
		if(parseInt(demoid)==parseInt(value.demoid)){
			$("<div/>",{'class':'pdprow activeproject fg-white displaycursor'}).data('pdpcode',value.pdpcode).data('demoid',value.demoid).html(value.demoname).appendTo('#demopdps');
			pdpcode=value.pdpcode;
			$("#requestuploadform :input").prop("disabled", false);
			loadRequests(value.demoid);
		}
		else
			$("<div/>",{'class':'pdprow displaycursor'}).data('pdpcode',value.pdpcode).data('demoid',value.demoid).html(value.demoname).appendTo('#demopdps');
		
	});
	return pdpcode;
}

function init(demoid){
	$("#spinner").show();
	if(!checkUserStatus())
			window.location.href = "./index.html";
	if(checkAdminStatus()){
		$("#madmin").show();
	}
	$('#requestuploadform').find('input').prop('disabled','disabled');
	$('#availablerequests').prop("disabled", 'disabled');
	$('#loadavailablerequest').prop("disabled", true);
	var pdpcode=loadPDPS(demoid);
	$("#spinner").hide();
	return pdpcode;
}

function checkAdminStatus(){
	console.log("checkAdminStatus");
	var msg=sendAjaxRequest("../sfxservice/user/auth/admin/session", "GET", "json","","");
	console.log("print content message user/auth/admin/session");
	console.log(msg);
	if(msg.response=='true')
		return true;
	return false;
}

function checkUserStatus(){
	var msg=sendAjaxRequest("../sfxservice/user/auth/session", "GET", "json","","");
	console.log(msg);
	if(msg.response=='true')
		return true;
	return false;
}

function logoutUser(){
	var msg=sendAjaxRequest("../sfxservice/user/logout", "GET", "json","","");
	init();
}

/**************** AJAX Requests *****************************/
function sendAjaxRequest(url,type,datatype,mimetype,data){
	return $.ajax({
 		 	url: url,
 		    type: type,
 		    cache: false,
 		    data: data,
        	mimeType:mimetype,
        	dataType:datatype,
        	async:false,
        	beforeSend:function(){
        		console.log("Before send in sendAjaxRequest");
   	            $("#spinner").show();
   	        },
 	        success: function(msg) {
 	           console.log("inside sendAjaxRequest success");
 	           console.log(msg);
 	           $("#spinner").hide();
               return msg;
 	     }
 	}).responseJSON;
}

function sendAjaxRequestFile(url,datatype,data){
	return $.ajax({
	       		 	url: url,
	       		    type: "POST",
	       		    data:data,
	       	        async:false,
	       	        mimeType:"multipart/form-data",
	       	    	contentType: false,
	       	        cache: false,
	       	        processData:false,
	       	        dataType:datatype,
	       	        beforeSend:function(){
	     	          $("#spinner").show();
	     	        },
	       	        complete: function(xhr,status) {
	       	        	console.log("sendAjaxRequestFile");
	       	        	console.log(xhr.responseText);
	       	        	$("#spinner").hide();
	       	        	$("#safaxresponse").text("");
	       	        	$("#safaxresponse").text(xhr.responseText);
	       	        	return text;
	       	        }
	     	    });
}

function sendAjaxRequestXMLResponse(url,datatype,data){
	return $.ajax({
	       		 	url: url,
	       		    type: "GET",
	       		    data:data,
	       	        async:false, //Setting async to false means that the statement you are calling has to complete before the next statement in your function can be called.
	       	        mimeType:"",
	       	    	contentType: false,
	       	        cache: false,
	       	        processData:false,
	       	        dataType:datatype,
	       	        beforeSend:function(){
	     	          $("#spinner").show();
	     	        },
	       	        complete: function(xhr,status) {
	       	        	if (is_transparency_request) {
	       	        		var index_number = xhr.responseText.indexOf("NOTIFICATIONS");
	       	        		if (index_number !=-1) {
	    	       	        	//substring(from, to)
	    	       	        	var output_text = xhr.responseText.substring(0, index_number);
	    	       	        	$("#spinner").hide();
			       	        	$("#safaxresponse").text("");
			       	        	$("#safaxresponse").text(output_text);
			       	        	
			       	        	/*
			       	        	 * delete all table rows except first
			       	        	 */
				       	         $("#transparencynotificationboxmenu").find("tr:gt(0)").remove();
			       	        	
			       	        	// Get the content after the NOTIFCATIONS
			       	        	transparency_notification_content = xhr.responseText.substring(index_number + 13);
			       	        	
			       	        	lines = transparency_notification_content.split(/\r\n|\r|\n/g);
			       	        	
			       	        	var arrayLength = lines.length;
			       	        	for (var i = 1; i < arrayLength; i++) {
			       	        		var table_row = "<tr> <td>" + lines[i] + "</td>" + "</tr>";
			       	        		$('#transparencynotificationboxmenu tr:last').after(table_row);
			       	        	}
	       	        		}else {
	       	        			transparency_notification_content = "";
	       	        			/*
			       	        	 * delete all table rows except first
			       	        	 */
				       	        $("#transparencynotificationboxmenu").find("tr:gt(0)").remove();
	       	        		}
	       	        	}
	       	        	else {
	       	        		transparency_notification_content = "";
	       	        		/*
		       	        	 * delete all table rows except first
		       	        	 */
			       	        $("#transparencynotificationboxmenu").find("tr:gt(0)").remove();
		       	        	console.log(xhr.responseText);
		       	        	$("#spinner").hide();
		       	        	$("#safaxresponse").text("");
		       	        	$("#safaxresponse").text(xhr.responseText);
		       	        }
	       	        	return xhr.responseText;
	       	        }
	       	    });
}

function resetForm(name) {
	document.getElementById(name).reset();
}

function isFileUploadEmpty(name){
	if($(name)[0].files.length<1){
		return true;
	}
	return false;
}
function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}
