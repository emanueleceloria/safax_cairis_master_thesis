/**
 * @author: Samuel Kaluvuri
 */
$(document).ready(function(){
	document.querySelector("#editarea").spellcheck = false;
	var $elementid=getParameterByName('resourceid');
	var $elementtype=getParameterByName('resourcetype');
	init($elementid,$elementtype);
	
	/*************** MENU *********************/
	$("#mhome").click(function(){
		window.location.href = "./main.html";
	});
	
	$("#mpep").click(function(){
		window.location.href = "./evaluate.html";
	});
	$("#msr").click(function(){
		window.location.href = "./registry.html";
	});
	$("#mact").click(function(){
		window.location.href = "./activity.html";
	});
	$("#mtrack").click(function(){
		window.location.href = "./tracker.html";
	});
	$("#mabt").click(function(){
		window.location.href = "./about.html";
	});
	$("#madmin").click(function(){
		window.location.href = "./admin.html";
	});
	$("#msetting").click(function(){
		window.location.href = "./settings.html";
	});
	$("#mlogout").click(function(){
		logoutUser();
	});
	$("#mhelp").click(function(){
		window.location.href = "./help.html";
	});
	
	$("#updateresource").click(function(){
		var message=sendAjaxRequest("../sfxservice/policy/update","POST", "json","",  $("#editform").serialize());
		window.top.close();
		return false;
	});
	
});


function init(resourceid,resourcetype){
	if(resourceid==undefined || resourcetype==undefined)
		return;
	if(resourceid==null || resourcetype==null)
		return;
	if(resourceid.length<1 || resourcetype.length<1)
		return;
	$("#spinner").show();
	if(!checkUserStatus())
			window.location.href = "./index.html";
	if(checkAdminStatus()){
		$("#madmin").show();
	}
	
	var url="../sfxservice/policy/"+resourcetype+"/"+resourceid;
	
	var message=sendAjaxRequestXMLResponse("../sfxservice/policy/"+resourcetype+"/"+resourceid,  "GET", "json","", "");
	$("#resourcetype").val(resourcetype);
	$("#resourceid").val(resourceid);
	$("#spinner").hide();
	return;
}

function checkAdminStatus(){
	var msg=sendAjaxRequest("../sfxservice/user/auth/admin/session", "GET", "json","","");
	
	if(msg.response=='true')
		return true;
	return false;
}

function checkUserStatus(){
	var msg=sendAjaxRequest("../sfxservice/user/auth/session", "GET", "json","","");
	
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
   	          $("#spinner").show();
   	        },
 	        success: function(msg) {
 	           
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
	       	        async:false,
	       	        mimeType:"",
	       	    	contentType: false,
	       	        cache: false,
	       	        processData:false,
	       	        dataType:datatype,
	       	        beforeSend:function(){
	     	          $("#spinner").show();
	     	        },
	       	        complete: function(xhr,status) {
//	       	         
	       	         $("#spinner").hide();
	       	         $("#editarea").text("");
	       	         $("#editarea").text(xhr.responseText);
	       	         return text;
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