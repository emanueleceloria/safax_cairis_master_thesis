/**
 * @author: Samuel Kaluvuri
 */

$(document).ready(function(){
	var $sfx_prid=0;
	var $sfx_demoid=0;
	var $project_id;
	
	/************* INIT SETUP *************************/
	init();
	loadProjects('0');
	displayDirectMessage("true","<span class='icon-comments-4 header'></span> <br/><br/>Your Feedback is most appreciated",
			"Registered users can provide us feedback using the " +
			"<a href='./tracker.html'>Issue Tracker</a>. <br/>Request for features or report any bugs." );
	
	/*************** MENU *********************/
	$("#mhome").removeClass('menubox').addClass('menuboxactive');
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
	
	/***************** CREATE PROJECT EVENTS ***********************/
	
	$("#newproject").click(function() {
		showRow("forms");
		displayHeader(true, "Start a new Project");
		resetForm("projectform");
		$("#projectsubmit").text("Create Project");
		$("#createproject").show();
		$("#createdemo").hide();
		$("#newdemo").show();
		$("#actiontype").val("create");
		
		// Enable create Project button
		$("#projectsubmit").prop('disabled', false);
		
		$("#createprojectname").removeClass('error-state');
		
		$("#projectnamemessage").text('');
		
		$("#assignedusers").text('');
		$("#assignedusers").text('');
	});
	
	$("#projectname").focusout(function (e){ 
		   checkProjectName($(this).val());
	});
		
	$('#findprojectusers').keyup(function(e) {
		if($(this).val().length > 0)
			findUsers($(this).val());
		else if ($(this).val().length == 0) {
			//loadUsersFromProject($project_id);
			$("#projectusers").text("");
		}
	});
	
	$("#addprojectuser").click(function(){
		return !$('#projectusers option:selected').remove().appendTo('#assignedusers'); 
	});
	$("#removeprojectuser").click(function(){
		return !$('#assignedusers option:selected').remove(); 
	});
	
	$('#projectform').on('submit', function(e) {
		$('#assignedusers option').prop('selected', true);
		var message=sendAjaxRequest("../sfxservice/project/register", "POST", "json","", $("#projectform").serialize());
		loadProjects('0');
		displayMessage(message);
		resetForm("projectform");
		$("#newdemo").show();
		return false;
	});
	/***************** OPEN PROJECT ***********************/
	
	$(document).on('click', '.openproj', function() {
		displayHeader(true, "Demos configured for this project");
		loadDemos($(this).data('projectid'));
		$(this).parent().siblings().removeClass("activeproject");
		$(this).parent().addClass("activeproject");
		$sfx_prid=$(this).data('projectid');
		$("#newdemo").show();
	});
	
	/***************** EDIT PROJECT ***********************/
	
	$(document).on('click', '.editproj', function() {
		// Hide New demo button
		$("#newdemo").hide();
		$project_id = $(this).data('projectid');
		editProject($(this).data('projectid'));
	});
	
	/***************** DELETE PROJECT ***********************/
	$(document).on('click', '.delproj', function() {
		deleteProject($(this).data('projectid'));
		$sfx_prid=0;
	});

	/***************** CREATE DEMO ***********************/
	
	$("#newdemo").click(function() {
		if($sfx_prid==0){
			displayDirectMessage(false, "First select a project", "");
			return false;
		}
		showRow("forms");
		displayHeader(true, "Create a Demo");
		$("#createproject").hide();
		resetForm("createdemoform");
		$("#createdemo").show();
	});
	
	$("#demosubmit").click(function(){
		if($sfx_prid==0){
			displayDirectMessage(false, "First select a project", "");
			return false;
		}
		var message=sendAjaxRequest("../sfxservice/demo/create/"+$sfx_prid, "POST", "json","", $("#createdemoform").serialize());
		if(message.response=='false'){
			displayMessage(message);
			resetForm("createdemoform");
			return false;
		}
		$sfx_demoid=message.demoid;
		editDemo($sfx_demoid, $sfx_prid);
		return false;
	});
	
	
	/***************** RUN DEMO ***********************/
	$(document).on('click', '.rundemo', function() {
		$sfx_demoid=$(this).data('demoid');
		window.location.href = "./evaluate.html?demoid="+$sfx_demoid;
	});
	
	$("#demoexecute").click(function(){
		if($sfx_demoid==0)
			return;
		window.location.href = "./evaluate.html?demoid="+$sfx_demoid;
	});
	
	/***************** OPEN DEMO ***********************/
	$(document).on('click', '.opendemo', function() {
		$sfx_demoid=$(this).data('demoid');
//		loadDemoInfo($sfx_demoid,$sfx_prid,false);
		editDemo($sfx_demoid, $sfx_prid);
	});

//	/***************** EDIT DEMO ***********************/
//	$(document).on('click', '.editdemo', function() {
//		$sfx_demoid=$(this).data('demoid');
//		editDemo($sfx_demoid, $sfx_prid);
//	});
	
	$("#democonfigsubmit").click(function() {
		var message=sendAjaxRequest("../sfxservice/demo/edit/"+$sfx_demoid, "POST", "json","", $("#democonfigform").serialize());
		displayMessage(message);
		if(message.response=='true'){
			$sfx_prid=message.data;
			loadProjects($sfx_prid);
			editDemo($sfx_demoid, $sfx_prid);
		}
		else
			displayMessage(message);
		return false;
	});
	
	// CAIRIS updates
	$("#cairisconfigsubmit").click(function() {
		var message=sendAjaxRequest("../sfxservice/demo/edit/cairis/"+$sfx_demoid+"/"+$sfx_prid, "POST", "json","", $("#cairisconfigform").serialize());
//		resetForm("cairisconfigform");
//		$('#cairisusr').val('');
//		$('#cairispwd').val('');
//		$('#cairisdb').val('');
		displayMessage(message);
		return false;
	});
	// end CAIRIS updates
	
	$("#xacmlpolicies").on('change', function(){
		var msg=uploadPolicies($sfx_demoid, "#xacmlpolicies", "uploadxacmlpolicies", "xacml", $sfx_prid);
		displayMessage(msg);
		return false;
	});
	
	$("#xacmlrequest").on('change', function(){
		var msg=uploadPolicies($sfx_demoid, "#xacmlrequest", "uploadxacmlrequests","requests", $sfx_prid);
		displayMessage(msg);
		return false;
	});
	
	$("#credpolicies").on('change', function(){
		var msg=uploadPolicies($sfx_demoid, "#credpolicies", "uploadcredpolicies", "credential", $sfx_prid);
		displayMessage(msg);
		return false;
	});
	
	$("#rep_policy").on('change', function(){
		var msg=uploadPolicies($sfx_demoid, "#rep_policy", "uploadrep_policy", "reputation", $sfx_prid);
		displayMessage(msg);
		return false;
	});
	$("#simpolicy").on('change', function(){
		var msg=uploadPolicies($sfx_demoid, "#simpolicy", "uploadsimpolicy", "similarity", $sfx_prid);
		displayMessage(msg);
		return false;
	});
	
	$("#attributefile").on('change', function(){
		var msg=uploadPolicies($sfx_demoid, "#attributefile", "attributeform", "attribute", $sfx_prid);
		displayMessage(msg);
		return false;
	});
	
	$(document).on('click', '.downloadxpolicy', function() {
		window.open('../sfxservice/policy/xacml/'+$(this).data('xpid'));
	});
	$(document).on('click', '.downloadtpolicy', function() {
		window.open('../sfxservice/policy/trust/'+$(this).data('tpid'));
	});
	$(document).on('click', '.downloadxrequest', function() {
		window.open('../sfxservice/policy/request/'+$(this).data('xrid'));
	});
	$(document).on('click', '.editxpolicy', function() {
		window.open('./edit.html?resourcetype=xacml&resourceid='+$(this).data('xpid'));
	});
	$(document).on('click', '.edittpolicy', function() {
		window.open('./edit.html?resourcetype=trust&resourceid='+$(this).data('tpid'));
	});
	$(document).on('click', '.editxrequest', function() {
		window.open('./edit.html?resourcetype=request&resourceid='+$(this).data('xrid'));
	});
	$(document).on('click', '.deletexrequest', function() {
		var msg=deletexrequest($(this).data('xrid'), $sfx_demoid,$sfx_prid);
		displayMessage(msg);
		return;
	});
	$(document).on('click', '.deletexpolicy', function() {
		var msg=deletexpolicies($(this).data('xpid'), $sfx_demoid,$sfx_prid);
		displayMessage(msg);
		return;
	});
	$(document).on('click', '.deletetpolicy', function() {
		var msg=deletetpolicies($(this).data('tpid'), $sfx_demoid,$sfx_prid);
		displayMessage(msg);
		return;
	});
	
	$('#repalphaip').on('focusout',function(){
		if($('#repalphaip').val().length==0)
			$('#repalphaip').val('0');
		var val=parseFloat($('#repalphaip').val().replace(',','.'));
		$(this).val($(this).val().replace(',','.'));
		
		if(val==undefined || val==null)
			val=0;
		if(val>=0 && val<=1){			
			updateDemoConfig($sfx_demoid,'reputation:alpha',val);
			return;
		}
		else
			alert("Alpha value can only be between 0 and 1");
		return;
	});
	
	$(document).on('focusout', '.repsettinginitvector', function(){
		var oldval=$(this).parent().data('repinitvectoriss');
		var iss=$(this).parent().data('repissuer');
		if($(this).val().length==0)
			$(this).val('0');
		var val=parseFloat($(this).val().replace(',','.'));
		$(this).val($(this).val().replace(',','.'));
		
		if(val==undefined || val==null)
			val=0;
		if(val>=0 && val<=1){
			var initvector=$('#repinitvectorip').val();
			initvector=initvector.replace(oldval, iss+":"+val);
			$('#repinitvectorip').val('');
			$('#repinitvectorip').val(initvector);
			updateDemoConfig($sfx_demoid,'reputation:init:vector',initvector);
			return;
		}
		else
			alert("Value can only be between 0 and 1");
		return;
		
	});
	
	$(document).on('click','.repgxfn', function(){
		$(this).parent().siblings().children().removeClass('bg-cyan fg-white').addClass('bg-dark fg-white');
		$(this).removeClass('bg-dark fg-white').addClass('bg-cyan fg-white');
		updateDemoConfig($sfx_demoid, "reputation:gx", $(this).attr('id'));
		return;
	});
	
	$('#simalphaip').on('focusout',function(){
		if($('#simalphaip').val().length==0)
			$('#simalphaip').val('0');
		var val=parseFloat($('#simalphaip').val().replace(',','.'));
		$(this).val($(this).val().replace(',','.'));
		
		if(val==undefined || val==null)
			val=0;
		if(val>=0 && val<=1){			
			updateDemoConfig($sfx_demoid,'similarity:alpha',val);
			return;
		}
		else
			alert("Alpha value can only be between 0 and 1");
		return;
	});
	
	$(document).on('focusout', '.simsettinginitvector', function(){
		var oldval=$(this).parent().data('siminitvectoriss');
		var iss=$(this).parent().data('simissuer');
		if($(this).val().length==0)
			$(this).val('0');
		$(this).val($(this).val().replace(',','.'));
		var val=parseFloat($(this).val().replace(',','.'));
		
		if(val==undefined || val==null)
			val=0;
		if(val>=0 && val<=1){
			var initvector=$('#siminitvectorip').val();
			initvector=initvector.replace(oldval, iss+":"+val);
			$('#siminitvectorip').val('');
			$('#siminitvectorip').val(initvector);
			updateDemoConfig($sfx_demoid,'similarity:init:vector',initvector);
			return;
		}
		else
			alert("Value can only be between 0 and 1");
		
		return;
		
	});
	
	$('#demoeditback').click(function(){
		loadDemos($sfx_prid);
	});

	/***************** COPY DEMO ***********************/
	
	$(document).on('click', '.copydemo', function() {
		$("#copytoproject").show();
		$sfx_demoid=$(this).data('demoid');
		loadAssignedProjects();
	});
	
	$("#copydemobutton").click(function(){
		var prid=$("#copydemotoproject").val();
		copyDemo($sfx_demoid,prid);
		$("#copytoproject").hide();
		$("#copydemotoproject").text("");
		loadProjects(prid);
		loadDemos(prid);
		
		$sfx_prid = prid;
		
		return;
	});
	
	$("#copytoprojecclose").click(function(){
		$("#copytoproject").hide();
		$("#copydemotoproject").text("");
	});
	/***************** DELETE DEMO ***********************/
	$(document).on('click', '.deldemo', function() {
		deleteDemo($(this).data('demoid'),$sfx_prid);
	});
});


function loadProjects(prid){
	var message=sendAjaxRequest("../sfxservice/project/all", "GET", "json","", "");
	$("#list-project").text("");
	$.each(message, function (index, value) {
		var $div = $('<div/>', {'class':'row projectBox'}).data("projectid", value.prid);
		if(prid==value.prid)
			$div.addClass('activeproject');
		
		$('<div/>',{'class':'span6 openproj displaycursor'}).data("projectid", value.prid).html(value.projectname).appendTo($div);
		var $status=$('<div/>',{'class':'span1'}).appendTo($div);
		if(value.ispublic=='0')
			$('<span/>',{'class':'icon-key', 'style':'font-size:70%; padding-right:5px;'}).appendTo($status);

		var $menu=$('<div/>',{'class':'span5 text-right'}).data("projectid", value.prid).appendTo($div); 
		$('<span/>',{'class':'icon-pencil editproj projectmenu'}).data("projectid", value.prid).appendTo($menu);
		$('<span/>',{'class':'icon-remove delproj projectmenu'}).data("projectid", value.prid).appendTo($menu);;
		$div.appendTo("#list-project");
	});	
	displayHeader(true, "Select a Project");
	displayDirectMessage("true", "Choose a project to see the associated demos", "Only you can delete your project.");
}

function checkProjectName(projectname){
		projectname=projectname.replace(/[^a-z\.\-\_A-Z0-9]/g, '');
		var message=sendAjaxRequest("../sfxservice/project/check/projectname?projectname="+projectname, "GET", "json","", "");
	   if(message.response=='false'){
		   $("#createprojectname").addClass('error-state');
		   $("#createprojectname").removeClass('success-state');
		   $("#projectnamemessage").text(message.message);
		   
		   // Disable create Project button
		   $("#projectsubmit").prop('disabled', true);
	   }
	   else{
		   $("#createprojectname").removeClass('error-state');
		   $("#createprojectname").addClass('success-state');
		   $("#projectnamemessage").text("");
		   
		   // Enable create Project button
		   $("#projectsubmit").prop('disabled', false);
	   }
}

function editProject(projectid){
	showRow("forms");
	$("#actiontype").val("edit");
	$("#updateformprojectid").val(projectid);
	$("#projectsubmit").text("Update Project");
	var message=sendAjaxRequest("../sfxservice/project/fetch/"+projectid, "GET", "json","", "");
	if(message.response=='false'){
		displayHeader(false, "Authorization Denied");
		displayDirectMessage("false", "You cannot modify the project", "Only project owners can modify the projects");
		return;
	}
	displayHeader(true, "Edit Project Settings");
	$("#projectname").val(message.projectname);
	$("#projectdesc").val(message.projectdesc);
	$("#projecturl").val(message.projecturl);
	if(message.ispublic=='1')
		$("#isprivate").attr("checked", false);
	else
		$("#isprivate").attr("checked", true);
	
	// For privacy reasons, all users are not displayed
	//loadUsersFromProject(projectid);
	
	$("#createproject").show();
	$("#createdemo").hide();	
	
	// Hide New demo button
	$("#newdemo").hide();
	
	$("#assignedusers").text("");
	
	var message=sendAjaxRequest("../sfxservice/project/users/"+projectid, "GET", "json","","");
	$.each(message, function (index, value) {
		$("<option/>",{'value':value.uid}).html(value.uname).appendTo("#assignedusers");
	});
}

function loadUsersFromProject(projectid) {
	$("#assignedusers").text("");
	var message=sendAjaxRequest("../sfxservice/project/users/"+projectid, "GET", "json","","");
	$.each(message, function (index, value) {
		$("<option/>",{'value':value.uid}).html(value.uname).appendTo("#assignedusers");
	});
	
	$("#projectusers").text("");
	var message_all_user=sendAjaxRequest("../sfxservice/user/get/all", "GET", "json","","");
	$.each(message_all_user, function (index, value_all_user) {
		var existingAssignUser = false;
		$.each(message, function (index, value_assigned_user) {
			if (value_all_user.uid == value_assigned_user.uid) {
				existingAssignUser = true;
			}
		});
		if (!existingAssignUser) {
			$("<option/>",{'value':value_all_user.uid}).html(value_all_user.uname).appendTo("#projectusers");
		}
	});
}


function deleteProject(projectid){
	var message=sendAjaxRequest("../sfxservice/project/delete/"+projectid, "GET", "json","", "");
	if(message.response=='true')
		loadProjects('0');
	else{
		displayHeader(false, "Authorization Denied");
		displayDirectMessage(message.response,message.message,"Projects can only be deleted by project owners");
	}
}



function loadDemos(projectid){
	var message=sendAjaxRequest("../sfxservice/demo/get/all/"+projectid, "GET", "json","", "");
	if (jQuery.isEmptyObject(message)){
		displayHeader(false, "No Demos");
		displayDirectMessage("false","Empty Project", "You can add demos to this project");
		return;
	}
	showRow("demos");
	$.each(message, function (index, value) {
		var $div = $('<div/>', {'class':'row demoBox'}).data("demoid", value.demoid);
		var $display=$('<div/>',{'class':'span1 text-left'}).data("demoid", value.demoid).appendTo($div); 
		$('<span/>',{'class':'icon-bookmark-2 reducedicon'}).data("demoid", value.demoid).appendTo($display);
		$('<div/>',{'class':'span6 text-left opendemo displaycursor'}).data("demoid", value.demoid).html(value.demoname).appendTo($div);
		
		var $menu=$('<div/>',{'class':'span5 text-right'}).data("demoid", value.demoid).appendTo($div); 
		$('<span/>',{'class':'tertiary-text rundemo demomenu'}).data("demoid", value.demoid).html("<i class='icon-share on-right'></i> Run").appendTo($menu);
		$('<span/>',{'class':'tertiary-text copydemo demomenu'}).data("demoid", value.demoid).html("<i class='icon-copy on-right'></i> Copy").appendTo($menu);
//		$('<span/>',{'class':'tertiary-text editdemo demomenu'}).data("demoid", value.demoid).html("<i class='icon-pencil  on-right'></i> Edit").appendTo($menu);
		$('<span/>',{'class':'tertiary-text deldemo demomenu'}).data("demoid", value.demoid).html("<i class='icon-remove  on-right'></i> Delete").appendTo($menu);
		$div.appendTo("#demolist");
	});
}


function editDemo(demoid,projectid){
	var message=sendAjaxRequest("../sfxservice/demo/can/edit/"+demoid, "GET", "json", "", "");
	displayMessage(message);
	if(message.response=='true')
		loadDemoInfo(demoid,projectid,true);
	else
		loadDemoInfo(demoid,projectid,false);
}

function loadDemoInfo(demoid, projectid, editmode){
	resetForm("democonfigform");
	
	if(editmode){
		displayHeader(true, "Edit Demo");
		$('#allframes').find('input, textarea, button, select').prop('disabled',false);
	}
	else{
		displayHeader(true, "View Demo");
		$('#allframes').find('input, textarea, button, select').prop('disabled',true);
	}
	
	var message=sendAjaxRequest("../sfxservice/policy/all/xacml/"+demoid, "GET", "json", "", "");
	showRow("demoedit");
	$("#displayXACMLPolicies").text("");
	if(jQuery.isEmptyObject(message)){
		$("#displayXACMLPolicies").html("No policies have been deployed.");
	}
	$.each(message, function (index, value) {
		var $div=$("<div/>",{'class':'policyrow'}).data('xpid',value.xpid);
		$('<span/>',{'class':'icon-download-2 pad rightpad downloadxpolicy displaycursor'}).data("xpid", value.xpid).appendTo($div);
		if(editmode){
			$('<span/>',{'class':'icon-pencil pad rightpad editxpolicy displaycursor'}).data("xpid", value.xpid).appendTo($div);
			$('<span/>',{'class':'icon-remove pad rightpad deletexpolicy displaycursor'}).data("xpid", value.xpid).appendTo($div);
		}
		$('<span/>',{'class':'pad rightpad'}).html(value.policyid).appendTo($div);
		$div.appendTo("#displayXACMLPolicies");
	});
	
	
	message=sendAjaxRequest("../sfxservice/policy/all/requests/"+demoid, "GET", "json", "", "");
	showRow("demoedit");
	$("#displayXACMLRequests").text("");
	if(jQuery.isEmptyObject(message)){
		$("#displayXACMLRequests").html("No requests have been uploaded.");
	}
	$.each(message, function (index, value) {
		var $div=$("<div/>",{'class':'policyrow'}).data('xrid',value.xrid);
		$('<span/>',{'class':'icon-download-2 pad rightpad downloadxrequest displaycursor'}).data("xrid", value.xrid).appendTo($div);
		if(editmode){
			$('<span/>',{'class':'icon-pencil pad rightpad editxrequest displaycursor'}).data("xrid", value.xrid).appendTo($div);
			$('<span/>',{'class':'icon-remove pad rightpad deletexrequest displaycursor'}).data("xrid", value.xrid).appendTo($div);
		}
		$('<span/>',{'class':'pad rightpad'}).html(value.requestid).appendTo($div);
		$div.appendTo("#displayXACMLRequests");
	});
	
	
	message=sendAjaxRequest("../sfxservice/policy/all/credential/"+demoid, "GET", "json", "", "");
	$("#displaycredpolicies").text("");
	if(jQuery.isEmptyObject(message)){
		$("#displaycredpolicies").html("No credential policies uploaded");
	}
	$.each(message, function (index, value) {
		var $div=$("<div/>",{'class':'policyrow'}).data('tpid',value.tpid);
		$('<span/>',{'class':'icon-download-2 pad rightpad downloadtpolicy displaycursor'}).data("tpid", value.tpid).appendTo($div);
		if(editmode){
			$('<span/>',{'class':'icon-pencil pad rightpad edittpolicy displaycursor'}).data("tpid", value.tpid).appendTo($div);
			$('<span/>',{'class':'icon-remove pad rightpad deletetpolicy displaycursor'}).data("tpid", value.tpid).appendTo($div);
		}
		$('<span/>',{'class':'pad rightpad'}).html(value.policyid).appendTo($div);
		$div.appendTo("#displaycredpolicies");
	});
	
	message=sendAjaxRequest("../sfxservice/policy/all/reputation/"+demoid, "GET", "json", "", "");
	$("#displayrep_policies").text("");
	if(jQuery.isEmptyObject(message)){
		$("#displayrep_policies").html("No Reputation policies uploaded");
		$('#repconfig').hide();
	}
	$.each(message, function (index, value) {
		var $div=$("<div/>",{'class':'policyrow'}).data('tpid',value.tpid);
		$('<span/>',{'class':'icon-download-2 pad rightpad downloadtpolicy displaycursor'}).data("tpid", value.tpid).appendTo($div);
		if(editmode){
			$('<span/>',{'class':'icon-pencil pad rightpad edittpolicy displaycursor'}).data("tpid", value.tpid).appendTo($div);
			$('<span/>',{'class':'icon-remove pad rightpad deletetpolicy displaycursor'}).data("tpid", value.tpid).appendTo($div);
		}
		$('<span/>',{'class':'pad rightpad'}).html(value.policyid).appendTo($div);
		$div.appendTo("#displayrep_policies");
	});
	
	
	message=sendAjaxRequest("../sfxservice/policy/all/similarity/"+demoid, "GET", "json", "", "");
	$("#displaysimpolicies").text("");
	if(jQuery.isEmptyObject(message)){
		$("#displaysimpolicies").html("No Similarity policies uploaded");
		$('#simconfig').hide();
	}
	$.each(message, function (index, value) {
		var $div=$("<div/>",{'class':'policyrow'}).data('tpid',value.tpid);
		$('<span/>',{'class':'icon-download-2 pad rightpad downloadtpolicy displaycursor'}).data("tpid", value.tpid).appendTo($div);
		if(editmode){
			$('<span/>',{'class':'icon-pencil pad rightpad edittpolicy displaycursor'}).data("tpid", value.tpid).appendTo($div);
			$('<span/>',{'class':'icon-remove pad rightpad deletetpolicy displaycursor'}).data("tpid", value.tpid).appendTo($div);
		}
		$('<span/>',{'class':'pad rightpad'}).html(value.policyid).appendTo($div);
		$div.appendTo("#displaysimpolicies");
	});
	
	message=sendAjaxRequest("../sfxservice/policy/all/attribute/"+demoid, "GET", "json", "", "");
	$("#displayattributes").text("");
	if(jQuery.isEmptyObject(message)){
		$("#displayattributes").html("No Attributes uploaded");
		$('#attributelist').hide();
	}
	$.each(message, function (index, value) {
		var $div=$("<div/>",{'class':'policyrow'}).data('tpid',value.tpid);
		$('<span/>',{'class':'icon-download-2 pad rightpad downloadtpolicy displaycursor'}).data("tpid", value.tpid).appendTo($div);
		if(editmode){
			$('<span/>',{'class':'icon-pencil pad rightpad edittpolicy displaycursor'}).data("tpid", value.tpid).appendTo($div);
			$('<span/>',{'class':'icon-remove pad rightpad deletetpolicy displaycursor'}).data("tpid", value.tpid).appendTo($div);
		}
		$('<span/>',{'class':'pad rightpad'}).html(value.policyid).appendTo($div);
		$div.appendTo("#displayattributes");
	});
	
	
	// START OF THE CHANGE - Transparency from Master student
	//
	//
	//
	
	function updateconflicts(viewpoint){
		$("#viewpoint").text("q");
		var msg=sendAjaxRequest("../sfxservice/demo/transparency/viewpoint/"+demoid+"/"+viewpoint, "GET", "json","","");
		$("#viewpoint").val(msg.viewpoint);
		
		$("#selectedconflicts").text("");
		var message=sendAjaxRequest("../sfxservice/demo/transparency/"+demoid+"/"+viewpoint, "GET", "json","","");
		$.each(message, function (index, value) {
			$("<option/>",{'value':value.conflictid}).html(value.conflictname).appendTo("#selectedconflicts");
		});
	

		$("#conflicts").text("");
		var message=sendAjaxRequest("../sfxservice/demo/all/transparency", "GET", "json","","");
		$.each(message, function (index, value) {
			var exists = false;
			$('#selectedconflicts option').each(function(){
				if (this.value == value.conflictid) {
					exists = true;
					return false;
				}
			});
			if(!exists)
			$("<option/>",{'value':value.conflictid}).html(value.conflictname).appendTo("#conflicts");
		});
	}
	
	$("#addnotification").click(function(){
		return !$('#conflicts option:selected').remove().appendTo('#selectedconflicts'); 
	});
	$("#removenotification").click(function(){
		return !$('#selectedconflicts option:selected').remove().appendTo('#conflicts'); 
	});
	
	
	$("#adduser").click(function(){
		var usr = $("#addtransparencyusers").val();
		var message=sendAjaxRequest("../sfxservice/demo/transparency/add/viewpoint/"+demoid+"/"+usr, "GET", "json","","");
		$("<option/>",{'value':$("#addtransparencyusers").val()}).html($("#addtransparencyusers").val()).appendTo("#transparencyusers");
		displayMessage(message);
		return !$("#addtransparencyusers").val("");
	});
	$("#removeuser").click(function(){
		var usr = $('#transparencyusers option:selected').val();
		var message=sendAjaxRequest("../sfxservice/demo/transparency/remove/viewpoint/"+demoid+"/"+usr, "GET", "json","","");
		displayMessage(message);
		return !$('#transparencyusers option:selected').remove(); 
	});
	
	$("#transparencyusers").text("");
	var message=sendAjaxRequest("../sfxservice/demo/get/transparency/users/"+demoid, "GET", "json","","");
	$.each(message, function (index, value) {
		$("<option/>",{'value':value.viewpoint}).html(value.viewpoint).appendTo("#transparencyusers");
	});
	
	$("#preferenceform").on('submit', function(e) {
		$('#selectedconflicts option').prop('selected', true);
		$('#viewpoint option').prop('selected', true);
		//$('#transparencyusers option:selected').prop('selected',true);
		var message=sendAjaxRequest("../sfxservice/demo/save/transparency/"+demoid, "POST", "json","", $("#preferenceform").serialize());
		displayMessage(message);
		return false;
	});
	
//	$("#findtransparencyusers").keypress(function (){
//		if($(this).val().length>2)
//			findTUsers($(this).val());
//		else
//			$("#transparencyusers").text("");
//	});
//	
//	function findTUsers(uname){
//		$("#transparencyusers").text("");
//		var message=sendAjaxRequest("../sfxservice/user/findall?uname="+uname, "GET", "json","","");
//		$.each(message, function (index, value) {
//			$("<option/>",{'value':value.uid}).html(value.uname).appendTo("#transparencyusers");
//		});
//	}
	
	$("#transparencyusers").change(function(){
		updateconflicts($(this).val());
	});

	
	
	
	
	
	
	
	//
	//
	//
	//END OF THE CHANGE
	
	
	
	
	
	
	message = sendAjaxRequest("../sfxservice/demo/pdpcode/"+demoid, "GET", "json", "", "");
	$("#editpdpcode").val(message.pdpcode);
	var host = window.location.host;
	var pdpCode = message.pdpcode; 
	
	$('#editpdpcodestate').text("");
	$('#editpdpcodestate').html("http://"+host+"/pdp/" + pdpCode);
	if(message.ispersistent=='1')
		$("#pdppersistent").prop('checked', true);
	
	$('#pdprcalgo option[value="' + message.rcalgorithm + '"]').prop('selected',true);
	message=sendAjaxRequest("../sfxservice/project/all/assigned", "GET", "json", "", "");
	
	
	/* After a discussion on 28 October, it's not clear whether the PDP URL should be from the host or the actual server 
	message = sendAjaxRequest("../sfxservice/demo/pdp/host/"+demoid, "GET", "json","");
	
	console.log("URL of PDP host is: " + message.serviceurl);
	
	var pdpFullURLHost = getLocation(message.serviceurl);
	
	console.log("PDP host name is: " + pdpFullURLHost.hostname);
	
	if (pdpFullURLHost.hostname == "localhost") {
		
	}
	else {
		host = pdpFullURLHost.hostname;
		$('#editpdpcodestate').text("");
		$('#editpdpcodestate').html("http://"+host+"/pdp/" + pdpCode);
	}
	*/
	
	$("#editdemoprojectlist").text("");
	if(jQuery.isEmptyObject(message)){
		$("#editdemoprojectlist").html("<option>No Assigned Projects</option>");
	}
	$.each(message, function (index, value) {
		if(projectid==value.prid)
			$("<option/>",{'value':value.prid}).html(value.projectname).attr("selected","selected").appendTo("#editdemoprojectlist");
		else
			$("<option/>",{'value':value.prid}).html(value.projectname).appendTo("#editdemoprojectlist");
	});
	
	// This is SAFAX authorization service used for SAFAX GUI
	// sfx_demo table
	if (demoid == '0000000001') {
		message=sendAjaxRequest("../sfxservice/registry/safax/service/components?component=pep", "GET", "json", "", "");
	}
	// These are other demos 
	else {
		message=sendAjaxRequest("../sfxservice/registry/service/components?component=pep", "GET", "json", "", "");
	}
	
	$("#pepservice").text("");
	if(jQuery.isEmptyObject(message)){
		$("#pepservice").html("<option>No Services Available</option>");
	}
	$.each(message, function (index, value) {
		$("<option/>",{'value':value.serviceid}).html(value.servicename).appendTo("#pepservice");
	});
	
	message=sendAjaxRequest("../sfxservice/registry/service/components?component=pdp", "GET", "json", "", "");
	$("#pdpservice").text("");
	if(jQuery.isEmptyObject(message)){
		$("#pdpservice").html("<option>No Services Available</option>");
	}
	$.each(message, function (index, value) {
		$("<option/>",{'value':value.serviceid}).html(value.servicename).appendTo("#pdpservice");
	});
	
	message=sendAjaxRequest("../sfxservice/registry/service/components?component=pap", "GET", "json", "", "");
	$("#papservice").text("");
	if(jQuery.isEmptyObject(message)){
		$("#papservice").html("<option>No Services Available</option>");
	}
	$.each(message, function (index, value) {
		$("<option/>",{'value':value.serviceid}).html(value.servicename).appendTo("#papservice");
	});
	
	message=sendAjaxRequest("../sfxservice/registry/service/components?component=pip", "GET", "json", "", "");
	$("#pipservice").text("");
	if(jQuery.isEmptyObject(message)){
		$("#pipservice").html("<option>No Services Available</option>");
	}
	$.each(message, function (index, value) {
		$("<option/>",{'value':value.serviceid}).html(value.servicename).appendTo("#pipservice");
	});
	
	// This is SAFAX authorization service used for SAFAX GUI
	// sfx_demo table
	if (demoid == '0000000001') {
		message=sendAjaxRequest("../sfxservice/registry/safax/service/components?component=pip", "GET", "json", "", "");
	}
	// These are other demos 
	else {
		message=sendAjaxRequest("../sfxservice/registry/service/components?component=pip", "GET", "json", "", "");
	}
	
	$("#pipservice").text("");
	if(jQuery.isEmptyObject(message)){
		$("#pipservice").html("<option>No Services Available</option>");
	}
	$.each(message, function (index, value) {
		$("<option/>",{'value':value.serviceid}).html(value.servicename).appendTo("#pipservice");
	});
	
	
	// This is SAFAX authorization service used for SAFAX GUI
	// sfx_demo table
	if (demoid == '0000000001') {
		message=sendAjaxRequest("../sfxservice/registry/safax/service/components?component=ch", "GET", "json", "", "");
	}
	// These are other demos 
	else {
		message=sendAjaxRequest("../sfxservice/registry/service/components?component=ch", "GET", "json", "", "");
	}
	
	$("#chservice").text("");
	if(jQuery.isEmptyObject(message)){
		$("#chservice").html("<option>No Services Available</option>");
	}
	$.each(message, function (index, value) {
		$("<option/>",{'value':value.serviceid}).html(value.servicename).appendTo("#chservice");
	});
	
	var msg=sendAjaxRequest("../sfxservice/demo/config/"+demoid, "GET", "json","");
	$.each(msg, function(index, value) {
		if(value.configkey=='reputation:init:vector'){
			$("#repconfig").show();
			$("#repinitvectorip").val(value.configvalue);
			$('#repinitvector').text('');
			var ip=value.configvalue;
			var res=ip.split(",");
			for(i=0;i<res.length;i++){
				var newRes=res[i].split(":");
				var $repsettingdiv=$('<div/>', {
					'style':'padding:15px;'
					}).data('repinitvectoriss', res[i]).data('repissuer',newRes[0]).appendTo("#repinitvector");
				if(editmode)
					$('<input/>',{'class':'repsettinginitvector','style':'padding:10px;margin-right:10px;'}).prop('size','2').val(newRes[1]).appendTo($repsettingdiv);
				else
					$('<input/>',{'class':'repsettinginitvector','style':'padding:10px;margin-right:10px;'}).prop('size','2').prop('disabled',true).val(newRes[1]).appendTo($repsettingdiv);
				$('<input/>',{'style':'background-color:#35383B;color:#ffffff;padding:10px;border:0px;'}).prop('readonly',true).val(newRes[0]).appendTo($repsettingdiv);
			}
		}
		else if(value.configkey=='reputation:alpha'){
			$("#repconfig").show();
			$('#repalphaip').val(value.configvalue);
		}
//		else if(value.configkey=='reputation:system:policy'){
//			$("#repconfig").show();
//			$("#repsyspolicyip").val(value.configvalue);
//			$('#repsyspolicy').text('');
//			var ip=value.configvalue;
//			var res=ip.split("|");
//			for(i=0;i<res.length;i++){
//				var newRes=res[i].split(",");
//				$('<div/>', {
//					'html': newRes[0]+" = "+newRes[1]+","+newRes[2],
//					'class':'repsetting displaycursor'
//					}).data('EBSLID', newRes[0]).appendTo("#repsyspolicy");
//				console.log(newRes[0]+" = "+newRes[1]);
//			}
//		}
		else if(value.configkey=='reputation:gx'){
			$("#repconfig").show();
			if(editmode){
				$('#'+value.configvalue).parent().siblings().removeClass('underlinedbutton');
				$('#'+value.configvalue).parent().siblings().children().removeClass('bg-cyan fg-white').addClass('bg-dark fg-white');
				$('#'+value.configvalue).removeClass('bg-dark fg-white').addClass('bg-cyan fg-white');
			}
			else{
				$('#'+value.configvalue).parent().siblings().removeClass('underlinedbutton');
				$('#'+value.configvalue).parent().addClass('underlinedbutton');
			}
		}
		else if(value.configkey=='similarity:init:vector'){
			$("#simconfig").show();
			$("#siminitvectorip").val(value.configvalue);
			$('#siminitvector').text('');
			var ip=value.configvalue;
			var res=ip.split(",");
			for(var i=0;i<res.length;i++){
				var newRes=res[i].split(":");
				var $simsettingdiv=$('<div/>', {
					'style':'padding:15px;'
					}).data('siminitvectoriss', res[i]).data('simissuer',newRes[0]).appendTo("#siminitvector");
				if(editmode)
					$('<input/>',{'class':'simsettinginitvector','style':'padding:10px;margin-right:10px;'}).prop('size','2').val(newRes[1]).appendTo($simsettingdiv);
				else
					$('<input/>',{'class':'simsettinginitvector','style':'padding:10px;margin-right:10px;'}).prop('size','2').prop('disabled',true).val(newRes[1]).appendTo($simsettingdiv);
				$('<input/>',{'style':'background-color:#35383B;color:#ffffff;padding:10px;border:0px;'}).prop('readonly',true).val(newRes[0]).appendTo($simsettingdiv);
			}
		}
		else if(value.configkey=='similarity:alpha'){
			$("#simconfig").show();
			$('#simalphaip').val(value.configvalue);
		}
		else if(value.configkey=='pep'){
			// SAMUEL original error $('#pepservice option[value="' + message.configvalue + '"]').prop('selected',true);
			// Duc Fix 27 October 2015
			$('#pepservice option[value="' + value.configvalue + '"]').prop('selected',true);
		}
		else if(value.configkey=='pdp'){
			// SAMUEL original error $('#pepservice option[value="' + message.configvalue + '"]').prop('selected',true);
			// Duc Fix 27 October 2015
			$('#pdpservice option[value="' + value.configvalue + '"]').prop('selected',true);
		}
		else if(value.configkey=='ch'){
			// Duc Fix 28 October 2015
			$('#chservice option[value="' + value.configvalue + '"]').prop('selected',true);
		}
		else if(value.configkey=='pip'){
			// Duc Fix 28 October 2015
			$('#pipservice option[value="' + value.configvalue + '"]').prop('selected',true);
		}
		else if(value.configkey=='pap'){
			// Duc Fix 28 October 2015
			$('#papservice option[value="' + value.configvalue + '"]').prop('selected',true);
		}
	});
	
	message=sendAjaxRequest("../sfxservice/demo/get/attributes/"+demoid+"?root=true", "GET", "json", "", "");
	if(jQuery.isEmptyObject(message)){
		$("#attributelist").hide();
	}
	else{
	$("#attributelist").text("");
	$("#attributelist").show();
	$.each(message, function (index, value) {
		var $accordion=$("<div/>",{'class':'accordion-frame'}).appendTo("#attributelist");
		$('<a/>',{'class':'heading'}).attr('href',"#").html("<span class='icon-move-vertical'></span> ID : <span class='fg-crimson'>" +value.attributeid +"</span> Value: <span class='fg-darkCyan'> "+value.attributevalue+"</span>").appendTo($accordion);
		var $content=$('<div/>',{'class':'content'}).appendTo($accordion);
		var newmessage=sendAjaxRequest("../sfxservice/demo/get/attributes/"+demoid+"?root=false&refid="+value.attid, "GET", "json", "", "");
		if(jQuery.isEmptyObject(newmessage)){
			$content.html("No Associated Attributes found");
		}
		else{
			$.each(newmessage, function (index1, value1) {
				$("<div/>",{'class':'configrow'}).html("ID : <span class='fg-crimson'>" +value1.attributeid +"</span> Value: <span class='fg-darkCyan'> "+value1.attributevalue+"</span>").appendTo($content);
			});
		}
	});
	}
	
	message=sendAjaxRequest("../sfxservice/demo/fetch/"+demoid, "GET", "json", "", "");
	if(!jQuery.isEmptyObject(message)){
		$("#editdemoname").val(message.demoname);
		$("#editdemodesc").val(message.demodesc);
	}
}


function uploadPolicies(demoid,uploadbox,formname,policytype,projectid){
	if(demoid==0) return false;
	if(isFileUploadEmpty(uploadbox)) return false;
	var formData = new FormData(document.forms.namedItem(formname));
	var message=sendAjaxRequestFile("../sfxservice/policy/upload/"+policytype+"/"+demoid, "json",formData);
	resetForm(formname);
	showRow("demoedit");
	editDemo(demoid, projectid);
	return message.responseJSON;
}

//function uploadAttributes(demoid, projectid){
//	if(demoid==0) return false;
//	if(isFileUploadEmpty("uploadattribites")) return false;
//	var formData = new FormData(document.forms.namedItem("uploadattribites"));
//	var message=sendAjaxRequestFile("../sfxservice/demo/load/attributes/"+demoid, "json",formData);
//	resetForm("uploadattributes");
//	showRow("demoedit");
//	editDemo(demoid, projectid);
//	return message.responseJSON;
//}


function copyDemo(demoid, projectid){
	var message=sendAjaxRequest("../sfxservice/demo/copy/"+demoid+"/"+projectid, "GET", "json","", "");
	displayMessage(message);
	return;
}

function loadAssignedProjects(){
	$("#copydemotoproject").text("");
	$("#copydemobutton").prop('disabled',true);
	var message=sendAjaxRequest("../sfxservice/project/all/assigned", "GET", "json", "", "");
	$("#copydemotoproject").text("");
	if(jQuery.isEmptyObject(message)){
		$("#copydemotoproject").html("<option>No Assigned Projects</option>");
		return;
	}
	$.each(message, function (index, value) {
		$("<option/>",{'value':value.prid}).html(value.projectname).appendTo("#copydemotoproject");
	});
	$("#copydemobutton").prop('disabled',false);
}

function deleteDemo(demoid,projectid){
	var message=sendAjaxRequest("../sfxservice/demo/delete/"+demoid, "GET", "json","", "");
	displayMessage(message);
	loadDemos(projectid);
}

function deletexpolicies(policyid,demoid,projectid){
	var message=sendAjaxRequest("../sfxservice/policy/xacml/delete/"+policyid, "GET", "json","","");
	editDemo(demoid,projectid);
	return message;
}

function deletexrequest(requestid,demoid,projectid){
	var message=sendAjaxRequest("../sfxservice/policy/request/delete/"+requestid, "GET", "json","","");
	editDemo(demoid,projectid);
	return message;
}

function deletetpolicies(policyid,demoid,projectid){
	var message=sendAjaxRequest("../sfxservice/policy/trust/delete/"+policyid, "GET", "json","","");
	editDemo(demoid,projectid);
	return message;
}

function updateDemoConfig(demoid,configkey,configvalue){
	var message=sendAjaxRequest("../sfxservice/demo/update/config/"+demoid+"?configkey="+configkey+"&configvalue="+configvalue, "GET", "json","","");
	displayMessage(message);
	return;
}

function findUsers(uname){
	$("#projectusers").text("");
	var message=sendAjaxRequest("../sfxservice/user/find?uname="+uname, "GET", "json","","");
	$.each(message, function (index, value) {
		var exists = false;
		$('#assignedusers option').each(function(){
		    if (this.value == value.uid) {
		        exists = true;
		        return false;
		    }
		});
		if(!exists)
			$("<option/>",{'value':value.uid}).html(value.uname).appendTo("#projectusers");
	});
}






function showRow(msg){
	if(msg=='message'){
		$("#demolist").hide();
		$("#formslist").hide();
		$("#message").text("");
		$("#message").show();
		$("#demoedit").hide();
	}
	else if(msg=='forms'){
		$("#demolist").hide();
		$("#formslist").show();
		$("#message").hide();
		$("#demoedit").hide();
	}
	else if(msg=='demos'){
		$("#demolist").show();
		$("#formslist").hide();
		$("#message").hide();
		$("#demoedit").hide();
		$("#demolist").text("");
	}
	else if(msg=='demoedit'){
		$("#demolist").hide();
		$("#formslist").hide();
		$("#message").hide();
		$("#demoedit").show();
	}	
}
function displayHeader(flag,msg){
	if(flag){
		$('#displayHeader').removeClass('text-alert');
		$('#displayHeader').addClass('text-success').html(msg);
	}
	else{
		$('#displayHeader').removeClass('text-success');
		$('#displayHeader').addClass('text-alert').html(msg);
	}
}

function displayMessage(msg){
	if(msg.response=='true'){
		$('#displayHeader').removeClass('text-alert');
		$('#displayHeader').addClass('text-success').html(msg.message);
	}
	else{
		$('#displayHeader').removeClass('text-success');
		$('#displayHeader').addClass('text-alert').html(msg.message);
	}
}

function hideNewDemo(flag){
	if(flag)
		$("#newdemo").hide();
	else
		$("#newdemo").show();
		
}

function displayDirectMessage(status,message,data){
	showRow("message");
	if(status=='true'){
		$('#message').html("<div class='text-success text-center pad  subheader'>" +message+"</div>" +
							"<div class='pad text-center'>"+data+"</div>");
	}
	else {
		$('#message').html("<div class='text-alert text-center pad subheader'>" +message+"</div>" +
							"<div class='pad text-center'>"+data+"</div>");
	}
	return false;
}

function isFileUploadEmpty(name){
	if($(name)[0].files.length<1){
		return true;
	}
	return false;
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
	       	        complete: function(msg) {
	       	         $("#spinner").hide();
	       	         return msg;
	       	     }
	       	 });
}

function resetForm(name) {
	document.getElementById(name).reset();
}

var getLocation = function(href) {
    var l = document.createElement("a");
    l.href = href;
    return l;
}

