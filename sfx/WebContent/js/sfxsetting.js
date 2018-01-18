/**
 * @author: Samuel Kaluvuri
 */
$(document).ready(function(){
	init();
	/*************** MENU *********************/
	$("#msetting").removeClass('menubox').addClass('menuboxactive');
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
	
	
	loadUserInfo();
	$("#fullname").focusout(function (e) {
		   var name = $(this).val(); //get the string typed by user
		   if(name.length<5){
			   $("#userfname").removeClass('success-state');
			   $("#userfname").addClass('error-state');
			   $("#statusfname").text("Should be more than 5 characters");
		   }
		   else{
			   $("#userfname").removeClass('error-state');
			   $("#userfname").addClass('success-state');
			   $("#statusfname").text("");

		   }
	});
	
	$("#email").focusout(function (e) {
		   var email = $(this).val(); //get the string typed by user
		   var message=sendAjaxRequest("../sfxservice/user/check/new/email?email="+email, "GET", "json","", "");
		   if(message.response=='false'){
			   $("#useremail").removeClass('success-state');
			   $("#useremail").addClass('error-state');
			   $("#statusemail").text(message.message);
		   }
		   else{
			   $("#useremail").removeClass('error-state');
			   $("#useremail").addClass('success-state');
			   $("#statusemail").text("");
		   }
		
	});
	
	$("#pwd").focusout(function (e) { //user types username on inputfiled
		if($("#pwd").val().length>0 && $("#pwd").val().length<4){
		  	$("#registerpwd").removeClass('success-state');
		  	$("#registerconfirmpwd").removeClass('success-state'); 
		  	$("#registerpwd").addClass('error-state');
		  	$("#registerconfirmpwd").addClass('error-state');
		  	$("#statuspwd").text("Mmm..maybe you would like to have a longer password?");
		}
		else  if($("#pwd").val()!=$("#confirmpwd").val()){
			  	$("#registerpwd").removeClass('success-state');
			  	$("#registerconfirmpwd").removeClass('success-state'); 
			  	$("#registerpwd").addClass('error-state');
			  	$("#registerconfirmpwd").addClass('error-state');
			  	$("#statuspwd").text("");
			  	$("#statuspwd").text("Passwords do not match");
		   }
		   else{
			   $("#registerpwd").removeClass('error-state');
			   $("#registerconfirmpwd").removeClass('error-state');
			   $("#registerpwd").addClass('success-state');
			  	$("#registerconfirmpwd").addClass('success-state');
			   $("#statuspwd").text("");
		   }
	});
	
	$("#confirmpwd").focusout(function (e) {
		if($("#confirmpwd").val().length>0 && $("#confirmpwd").val().length<4){
		  	$("#registerpwd").removeClass('success-state');
		  	$("#registerconfirmpwd").removeClass('success-state'); 
		  	$("#registerpwd").addClass('error-state');
		  	$("#registerconfirmpwd").addClass('error-state');
		  	$("#statuspwd").text("Mmm..maybe you would like to have a longer password?");

	  }
	 else if($("#confirmpwd").val()!=$("#pwd").val()){
			  	$("#registerpwd").removeClass('success-state');
			  	$("#registerconfirmpwd").removeClass('success-state'); 
			  	$("#registerpwd").addClass('error-state');
			  	$("#registerconfirmpwd").addClass('error-state');
			  	$("#statuspwd").text("");
			  	$("#statuspwd").text("Passwords do not match");
		   }
		   else{
			   $("#registerpwd").removeClass('error-state');
			   $("#registerconfirmpwd").removeClass('error-state');
			   $("#registerpwd").addClass('success-state');
			   $("#registerconfirmpwd").addClass('success-state');
			   $("#statuspwd").text("");
		   }
	});
	
	$("#updateuser").click( function(){
		var message=sendAjaxRequest("../sfxservice/user/update", "POST", "json","", $("#form-update").serialize());
		console.log(message);
		resetForm("form-update");
		loadUserInfo();
		return false;
		
	});
	
	
	
});


function loadUserInfo(){
	var message=sendAjaxRequest("../sfxservice/user/setting", "GET", "json","","");
	if(jQuery.isEmptyObject(message))
		return;
	$('#usernamedisplay').text("");
	$('#usernamedisplay').html(message.uname);
	$('#fullname').val(message.fullname);
	$('#email').val(message.email);
}

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

function resetForm(name) {
	document.getElementById(name).reset();
}