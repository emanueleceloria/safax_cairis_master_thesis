/**
 * @author: Samuel Kaluvuri
 */
$(document).ready(function(){	
	var cookietoSet;
	init();
	$("#registerfname").focusout(function (e) {
		   var name = $(this).val(); //get the string typed by user
		   if(name.length<5){
			   $("#registername").removeClass('success-state');
			   $("#registername").addClass('error-state');
			   $("#statusname").text("");
			   $("#statusname").html("Really? You want us to believe that <i>this</i> is your name?");
			   if($("#form-register").find(".success-state").length!=5)
				   $("#registerSubmit").prop('disabled',true);
		   }
		   else{
			   $("#registername").removeClass('error-state');
			   $("#registername").addClass('success-state');
			   $("#statusname").text("");
			   if($("#form-register").find(".success-state").length==5)
				   $("#registerSubmit").prop('disabled',false);
		   }
		
	});
	
	$("#registeremail").focusout(function (e) {
		   var email = $(this).val(); //get the string typed by user
		   var message=sendAjaxRequest("../sfxservice/user/check/email?email="+email, "GET", "json","", "");
		   if(message.response=='false'){
			   $("#registeruseremail").removeClass('success-state');
			   $("#registeruseremail").addClass('error-state');
			   $("#statusemail").text(message.message);
			   if($("#form-register").find(".success-state").length!=5)
				   $("#registerSubmit").prop('disabled',true);
		   }
		   else{
			   $("#registeruseremail").removeClass('error-state');
			   $("#registeruseremail").addClass('success-state');
			   $("#statusemail").text("");
			   
			   if($("#form-register").find(".success-state").length==5)
				   $("#registerSubmit").prop('disabled',false);
			   
		   }
		
	});
	
	$("#registerUserName").focusout(function (e) { //user types username on inputfiled
		   var username = $(this).val(); //get the string typed by user
		   username=username.replace(/[^a-z\.\_A-Z0-9]/g, '');
		   var message=sendAjaxRequest("../sfxservice/user/check/username?uname="+username, "GET", "json","", "");
		   if(message.response=='false'){
			   $("#registerUName").removeClass('success-state');
			   $("#registerUName").addClass('error-state');
			   $("#statusUserName").text(message.message);
			   if($("#form-register").find(".success-state").length!=5)
				   $("#registerSubmit").prop('disabled',true);

		   }
		   else{
			   $("#registerUName").removeClass('error-state');
			   $("#registerUName").addClass('success-state');
			   $("#statusUserName").text("");
			   
			   if($("#form-register").find(".success-state").length==5)
				   $("#registerSubmit").prop('disabled',false);
		   }
	});
	
	$("#registerpwd").focusout(function (e) {
		if($("#registerpwd").val().length<4){
		  	$("#registerpwdbox").removeClass('success-state');
		  	$("#registerconfirmpwdbox").removeClass('success-state'); 
		  	$("#registerpwdbox").addClass('error-state');
		  	$("#registerconfirmpwdbox").addClass('error-state');
		  	$("#statuspwd").text("Mmm..maybe you would like to have a longer password?");
		  	if($("#form-register").find(".success-state").length!=5)
				   $("#registerSubmit").prop('disabled',true);

	  }
	 else if($("#registerconfirmpwd").val()!=$("#registerpwd").val()){
			  	$("#registerpwdbox").removeClass('success-state');
			  	$("#registerconfirmpwdbox").removeClass('success-state'); 
			  	$("#registerpwdbox").addClass('error-state');
			  	$("#registerconfirmpwdbox").addClass('error-state');
			  	$("#statuspwd").text("");
			  	$("#statuspwd").text("Passwords do not match");
			  	if($("#form-register").find(".success-state").length!=5)
					   $("#registerSubmit").prop('disabled',true);

		   }
		   else{
			   $("#registerpwdbox").removeClass('error-state');
			   $("#registerconfirmpwdbox").removeClass('error-state');
			   $("#registerpwdbox").addClass('success-state');
			  	$("#registerconfirmpwdbox").addClass('success-state');
			   $("#statuspwd").text("");
			   
			   if($("#form-register").find(".success-state").length==5)
				   $("#registerSubmit").prop('disabled',false);
		   }
	});
	
	$("#registerconfirmpwd").focusout(function (e) { //user types username on inputfiled
		if($("#registerpwd").val().length<4){
		  	$("#registerpwdbox").removeClass('success-state');
		  	$("#registerconfirmpwdbox").removeClass('success-state'); 
		  	$("#registerpwdbox").addClass('error-state');
		  	$("#registerconfirmpwdbox").addClass('error-state');
		  	$("#statuspwd").text("Mmm..maybe you would like to have a longer password?");
		  	if($("#form-register").find(".success-state").length!=5)
				   $("#registerSubmit").prop('disabled',true);
		}
		else  if($("#registerconfirmpwd").val()!=$("#registerpwd").val()){
			  	$("#registerpwdbox").removeClass('success-state');
			  	$("#registerconfirmpwdbox").removeClass('success-state'); 
			  	$("#registerpwdbox").addClass('error-state');
			  	$("#registerconfirmpwdbox").addClass('error-state');
			  	$("#statuspwd").text("");
			  	$("#statuspwd").text("Passwords do not match");
			  	if($("#form-register").find(".success-state").length!=5)
					   $("#registerSubmit").prop('disabled',true);

		   }
		   else{
			   $("#registerpwdbox").removeClass('error-state');
			   $("#registerconfirmpwdbox").removeClass('error-state');
			   $("#registerpwdbox").addClass('success-state');
			  	$("#registerconfirmpwdbox").addClass('success-state');
			   $("#statuspwd").text("");
			   
			   if($("#form-register").find(".success-state").length==5)
				   $("#registerSubmit").prop('disabled',false);
		   }
	});

	
	$("#loginSubmit").click( function(){
		
		if(isFormElementEmpty("#lusername") || isFormElementEmpty("#lpwd")){
			$("#loginmessage").show();
			$("#loginmessage").html("Seriously?");
			resetForm("form-login");
			return false;
		}
		var message=sendAjaxRequest("../sfxservice/user/login", "POST", "json","", $("#form-login").serialize());
		
		resetForm("form-login");
		loadDisplayBox("message");
		displayMessage(message);
		if(message.response=='true'){
			// Check if web browser is IE 11
			var isIE11 = !!navigator.userAgent.match(/Trident.*rv\:11\./);
			if (isIE11) {
				$.removeCookie("sfxsession");
			}
				
			// IE does not set cookie after receiving response from AJAX 
			if (isIE11) {
				$.cookie("sfxsession", message['cookie'], {
					   expires : 1,           //expires in 1 days

					   path    : '/',          //The value of the path attribute of the cookie 
					                           //(default: path of page that created the cookie).
				});
			}
			
			$("#registerButton").hide();
			$("#loginButton").hide();
			
			// Samuel
			window.location.href = "./main.html"; /* problem with IE */
		}
		return false;
		
	});
	
	$("#guestlogin").click( function(){
		var message=sendAjaxRequest("../sfxservice/user/guest/session", "GET", "json","", "");
		resetForm("form-login");
		loadDisplayBox("message");
		displayMessage(message);
		if(message.response=='true'){
			// Check if web browser is IE 11
			var isIE11 = !!navigator.userAgent.match(/Trident.*rv\:11\./);
			if (isIE11) {
				$.removeCookie("sfxsession");
			}
				
			// IE does not set cookie after receiving response from AJAX 
			if (isIE11) {
				$.cookie("sfxsession", message['cookie'], {
					   expires : 1,           //expires in 1 days

					   path    : '/',          //The value of the path attribute of the cookie 
					                           //(default: path of page that created the cookie).
				});
			}
			
			$("#registerButton").show();
			$("#loginButton").show();
			setTimeout(function () {
				// Samuel
				/* window.location.href = "./main.html"; */
				
				// Duc fix IE
				window.location.assign('./main.html'); // fixed problem with IE
				
			},2000);
		}
		return false;
	});
	
	$("#registerSubmit").click( function(){
		var message=sendAjaxRequest("../sfxservice/user/create", "POST", "json","", $("#form-register").serialize());
		resetForm("form-register");
		loadDisplayBox("message");
		displayMessage(message);
		return false;
	});
	
	$("#requestresetSubmit").click( function(){
		var message=sendAjaxRequest("../sfxservice/user/reset", "POST", "json","", $("#form-reset").serialize());
		resetForm("form-reset");
		loadDisplayBox("message");
		displayMessage(message);
		return false;
	});
	
	$("#registerlink").click(function(){
		loadForm("register");
		return;
	});
	$("#registerButton").click(function(){
		loadForm("register");
	});
	$("#loginButton").click(function(){
		loadForm("login");
	});
	$("#forgotPwdLink").click(function(){
		loadForm("resetPwd");
	});
	
	$("#aboutButton").click(function(){
		loadDisplayBox("about");
	});
	

});



function loadForm(formname){
	$("#mainDisplayContainer").hide();
	$("#mainForms").show();
	if(formname=='login'){
		$("#registerForm").hide();
		$("#resetPwdForm").hide();
		$("#loginForm").show();
	}
	else if(formname=='register'){
		$("#registerForm").show();
		$("#loginForm").hide();
		$("#resetPwdForm").hide();
	}
	else if(formname=='resetPwd'){
		$("#registerForm").hide();
		$("#loginForm").hide();
		$("#resetPwdForm").show();
	}
}

function loadDisplayBox(formname){
	$("#mainDisplayContainer").show();
	$("#mainForms").hide();
	if(formname=='about'){
		$("#about").show();
		$("#help").hide();
		$("#message").hide();
	}
	else if(formname=='help'){
		$("#about").hide();
		$("#help").show();
		$("#message").hide();
	}
	else if(formname=='message'){
		$("#about").hide();
		$("#help").hide();
		$("#message").show();
	}
}

function displayMessage(msg){
//	var json = $.parseJSON(message);
	if(msg.response=='true'){
		$('#message').html("<div class='text-success pad  subheader'>" +msg.message+"</div>" +
							"<div class='pad'>"+msg.data+"</div>");
	}
	else{
		$('#message').html("<div class='text-alert pad subheader'>" +msg.message+"</div>" +
				"<div class='pad'>"+msg.data+"</div>");
	}
}


/**************** Init Functions *****************************/

function init(){
	if(checkUserStatus())
			window.location.href = "./main.html";
	loadForm("login");
}

function checkUserStatus(){
	var msg=sendAjaxRequest("../sfxservice/user/auth/session", "GET", "json","","");
	if(msg.response=='true')
		return true;
	return false;
}

function resetForm(name) {
	document.getElementById(name).reset();
}

function isFormElementEmpty(name){
	if(!$(name).val() && $(name).val() == ""){
		return true;
	}
	return false;
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
 	        success: function(msg) {
               return msg;
 	     }
 	}).responseJSON;
}