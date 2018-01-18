/**
 * @author: Samuel Kaluvuri
 */
$(document).ready(function(){
	init();
	/*************** MENU *********************/
	$("#mabt").removeClass('menubox').addClass('menuboxactive');
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
});

/**************** Init Functions *****************************/

function init(){
	if(!checkUserStatus())
			window.location.href = "./index.html";
	if(checkAdminStatus()){
		$("#madmin").show();
	}
}

function checkAdminStatus(){
	var msg=sendAjaxRequest("../sfxservice/user/auth/admin/session", "GET", "json","","");
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
   	          $("#spinner").show();
   	        },
 	        success: function(msg) {
 	           console.log(msg);
 	           $("#spinner").hide();
               return msg;
 	     }
 	}).responseJSON;
}