/**
 * @author: Samuel Kaluvuri
 */

$(document).ready(function(){
	init();
	/*************** MENU *********************/
	$("#mtrack").removeClass('menubox').addClass('menuboxactive');
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
	
	
	
	
	loadIssues();
	loadResolvedIssues();
	
	$("#issuesubmit").click( function(){
		console.log("Submit issue button clicked!");
		var message=sendAjaxRequest("../sfxservice/issue/report", "POST", "json","", $("#form-issue").serialize());
		console.log(message);
		resetForm("form-issue");
		loadIssues();
		return false;
	});
});


function loadIssues(){

	var message=sendAjaxRequest("../sfxservice/issue/view", "GET", "json","", "");
	$("#reportedissues").text("");
	if (jQuery.isEmptyObject(message)){
		$("#reportedissues").html("<span class='fg-green subheader-secondary'>No reported Issues. We are that good! </span>");
		return;
	}
	$.each(message, function (index, value) {
		$div=$('<div/>',{'class':'issuebox'}).appendTo('#reportedissues');
		if(value.isfeature=='0')
			$('<div/>',{'class':'issueboxheaderbug'}).html("Bug: "+value.issueheader).appendTo($div);
		else
			$('<div/>',{'class':'issueboxheaderissue'}).html("Feature: " +value.issueheader).appendTo($div);
		$('<div/>',{'class':'issueboxdesc'}).html(value.issuedesc).appendTo($div);
		$('<div/>',{'class':'issueboxdesc '}).html("<small> Reported By ~"+value.uname+"</small>").appendTo($div);
	}); 
}

function loadResolvedIssues(){
	
	var message=sendAjaxRequest("../sfxservice/issue/resolved/view", "GET", "json","", "");
	$("#resolvedissues").text("");
	if (jQuery.isEmptyObject(message)){
		$("#resolvedissues").html("");
		return;
	}
	$.each(message, function (index, value) {
		$div=$('<div/>',{'class':'issuebox'}).appendTo('#resolvedissues');
		if(value.isfeature=='0')
			$('<div/>',{'class':'issueboxheaderbug'}).html("Bug: "+value.issueheader).appendTo($div);
		else
			$('<div/>',{'class':'issueboxheaderissue'}).html("Feature: " +value.issueheader).appendTo($div);
		$('<div/>',{'class':'issueboxdesc'}).html(value.issuedesc).appendTo($div);
		$('<div/>',{'class':'issueboxdesc '}).html("<small> Reported By ~"+value.uname+"</small>").appendTo($div);
	});
	
}










/**************** Init Functions *****************************/

function init(){
	console.log("init function in sfxtracker.js");
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
	console.log("Check user status in Init function in sfxtracker.js");
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

function resetForm(name) {
	document.getElementById(name).reset();
}
