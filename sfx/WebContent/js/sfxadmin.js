/**
 * @author: Samuel Kaluvuri + Duc Luu
 * Last Updated: 11 November 2015
 */

// Making sure the page is ready
// Delay the execution of the Javascript code until the DOM is loaded.


//Global variable starttime in the Average Stats of Statistics tab
var starttime;
var endtime;

//Store selected user id in search filter of Statistics tab
var userid_selected_in_statistics_tab = -1;

$(document).ready(function(){
	var $sfx_usernameedit="";
	$sfx_uidedit="";
	var assignedGroupArrays = [];
	
	/************* INIT SETUP *************************/
	init();
	/*************** MENU *********************/
	$("#madmin").removeClass('menubox').addClass('menuboxactive');
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
	/************* INTERNAL MENU *************************/
	$("#umetile").click(function(){
		$(this).siblings('.selected').removeClass('selected');
		$(this).addClass('selected');
		$('#statisticstab').hide();
		$('#trackertab').hide();
		$('#servicetab').hide();
		$('#usermanagementtab').show();
	});
	
	$("#stattile").click(function(){
		$(this).siblings('.selected').removeClass('selected');
		$(this).addClass('selected');
		$('#usermanagementtab').hide();
		$('#trackertab').hide();
		$('#servicetab').hide();
		
		arrayListOfChosenPEPInstances = []; // a list of checked PEP instances
		arrayListOfChosenPDPInstances = []; // a list of checked PDP instances
		arrayListOfChosenPIPInstances = []; // a list of checked PDP instances
		arrayListOfChosenCHInstances = []; // a list of checked CH instances
		arrayListOfChosenUDFInstances = []; // a list of checked UDF instances
		
		loadStats();
		$('#statisticstab').show();
		
		loadDateTimePicker();
		
		// Load Evaluation Time - Method from sfxadmin_evaluation_stats.js file
		updateAllEvaluationTime();
		
		// Load UCON Evaluation Time - Method from sfxadmin_evaluation_stats.js file
		updateAllUCONEvaluationTime();
	});
	
	$("#issuetile").click(function(){
		$(this).siblings('.selected').removeClass('selected');
		$(this).addClass('selected');
		$('#usermanagementtab').hide();
		$('#statisticstab').hide();
		$('#servicetab').hide();
		loadIssues();
		$('#trackertab').show();
	});
	
	/*
	 * Register Service
	 */
	$("#registertile").click(function(){
		$(this).siblings('.selected').removeClass('selected');
		$(this).addClass('selected');
		$('#usermanagementtab').hide();
		$('#statisticstab').hide();
		$('#trackertab').hide();
		$('#servicetab').show();
	});
	
	loadUserRequests();
	
	/************* USER TASKS *************************/
	$(document).on('click','.actuserreq', function(){
		activateUser($(this).data('newuid'));
	});
	
	$(document).on('click','.removeuserreq', function(){
		removeUser($(this).data('newuid'));
	});
	
	$("#finduser").keyup(function (){
		if($(this).val().length > 0) {
			findUsers($(this).val());
		}
		else {
			loadUsers();
			$("#assignedgroups").text("");
			// Load Available Groups
			loadAllAvailableGroups();	
		}
	});
	
	$("#finduser").change(function (){
		if($(this).val().length>1)
			findUsers($(this).val());
		else
			loadUsers();
	});
		
	$('#findgroups').keyup(function(e) {
		if($(this).val().length > 0) {
			findGroups($(this).val());
		}
			
		else if ($(this).val().length == 0) {
			loadAllAvailableGroups();
		}
	});
		
	$("#allusers").click(function(){
		$sfx_uidedit=$("#allusers").val();
		$sfx_usernameedit=loadUserInfo($sfx_uidedit);
	});

	$("#addusergroup").click(function(){
		return !$('#usergroups option:selected').remove().appendTo('#assignedgroups'); 
	});
	
	$("#removeusergroup").click(function(){
		return !$('#assignedgroups option:selected').remove(); 
	});
	
	$("#addservicedependency").click(function(){
		return !$('#servicedependencylist option:selected').remove().appendTo('#assignedservicedependency'); 
	});
	
	$("#removeservicedependency").click(function(){
		return !$('#assignedservicedependency option:selected').remove().appendTo('#servicedependencylist');
	});

	$("#eusername").focusout(function (e) { //user types username on inputfiled
		   var username = $(this).val(); //get the string typed by user
		   if(username==$sfx_usernameedit){
			   $("#umusername").removeClass('error-state');
			   return false;
		   }
		   username=username.replace(/[^a-z\.\_A-Z0-9]/g, '');
		   var message=sendAjaxRequest("../sfxservice/user/check/username?uname="+username, "GET", "json","", "");
		   if(message.response=='false'){
			   $("#umusername").removeClass('success-state');
			   $("#umusername").addClass('error-state');
		   }
		   else{
			   $("#umusername").removeClass('error-state');
			   $("#umusername").addClass('success-state');
		   }
	});
	
	$('#umesubmit').on('click', function() {
		$('#assignedgroups option').prop('selected', true);
		$('#umuserid').val($sfx_uidedit);
		var message=sendAjaxRequest("../sfxservice/admin/user/update", "POST", "json","", $("#umeform").serialize());
		$sfx_usernameedit="";
		//$sfx_uidedit="";
		//$("#allusers").text("");
		//$("#finduser").val("");
		//resetForm("umeform");
		//$('#assignedgroups').text("");
		loadUsers();
		$sfx_usernameedit=loadUserInfo($sfx_uidedit);
		loadAllAvailableGroups();
		
		//$("#umeform :input").prop("disabled", true);
		return false;
	});
	
	$(document).on('click', '.fixissue', function() {
		var $issid=$(this).parent().data('issueid');
		var message=sendAjaxRequest("../sfxservice/admin/issue/resolve/"+$issid, "GET", "json","", "");
		loadIssues();
	});
	
	// User clicks register new service to create new service
	$('#serviceregistersubmit').on('click', function() {
		if ($('#serviceregistersubmit').text() == "Save") {
			$('#serviceregistersubmit').text("Register New Service");
			var message=sendAjaxRequest("../sfxservice/registry/add/service", "POST", "json","", $("#registerserviceform").serialize());
			
			loadDefaultServiceRegistryInfoForm();
			
			if(message.response == 'true'){
				loadServices();
			}
		}
		else {
			$('#serviceregistersubmit').text("Save");
			$('#serviceregistersubmitcancel').show();
			$('#serviceregisterinterfacesubmit').hide();
			$('#serviceinterfaceactionlabel').hide();
			$('#serviceregistermodifysubmit').hide();
			$('#serviceregistermodifycancel').hide();
			$('#serviceregisterdelete').hide();
			$("#registerserviceform :input").prop("disabled", false);
		}
				
		resetForm("registerserviceform");		
		$("#assignedservicedependency").text("");
		
		findAllServiceDependencyList();
		
		return false;
	});
	
	// User clicks cancel registering new service
	$('#serviceregistersubmitcancel').on('click', function() {
		loadDefaultServiceRegistryInfoForm();
		
		$("#servicedependencylist").text("");
		$("#assignedservicedependency").text("");
		
		resetForm("registerserviceform");
		return false;
	});
	
	// User clicks modify existing services while in the service menu not in the service interface menu
	$('#serviceregistermodifysubmit').on('click', function() {
		if ($('#serviceregistermodifysubmit').text() == "Save") {
			$('#serviceregistermodifysubmit').text("Modify Service");
			
			// send all service dependency in the list into the form
			// If do not declare this only selected rows of dependency will be included in the form
			$('#assignedservicedependency option').prop('selected', true);
			
			var serviceid = $('#registryserviceid').val();
				
			if (serviceid == "" || serviceid == "Service ID") {
				var r = confirm("You need to choose a service.");
			}
			else {
				var message=sendAjaxRequest("../sfxservice/registry/update/service", "POST", "json","", $("#registerserviceform").serialize());
				loadDefaultServiceRegistryInfoForm();
			}
			loadServices();			
		}
		else {
			// Modify Service button when pressed -> Save
			$('#serviceregistermodifysubmit').text("Save");
			$('#serviceregistermodifysubmit').show();
			$('#serviceregistersubmitcancel').hide();
			$('#serviceregistersubmit').hide();
			
			// Show Cancel button when Modify Service button -> Save 
			$('#serviceregistermodifycancel').show();
			$('#serviceregisterdelete').hide();
			$("#registerserviceform :input").prop("disabled", false);
			
			$('#serviceregisterinterfacesubmit').hide();
			$('#serviceinterfaceactionlabel').hide();
		}
		return false;
	});
	
	// User clicks cancel modifying existing services
	$('#serviceregistermodifycancel').on('click', function() {		
		loadDefaultServiceRegistryInfoForm();
		return false;
	});
	
	// User clicks remove existing services
	$('#serviceregisterdelete').on('click', function() {
		 var serviceid = $('#registryserviceid').val();
		
		if (serviceid == "" || serviceid == "Service ID") {
			var r = confirm("You need to choose a service.");
		}
		else {
			var r = confirm("Are you sure you want to delete this web service?");
			if (r == true) {
			    //txt = "You pressed OK!";
				var message=sendAjaxRequest("../sfxservice/registry/remove/service/" + serviceid, "GET", "json","", "");
				resetForm("registerserviceform");
				$("#servicedependencylist").text("");
				$("#assignedservicedependency").text("");
				loadServices();
			} else {
			   // txt = "You pressed Cancel!";
			}
		}
		return false;
	});
	
	// User clicks register new service INTERFACE
	$('#serviceregisterinterfacesubmit').on('click', function() {
		$('#registerserviceinterface').show();
		$('#registerservice').hide();
		
		// Display save button 
		$('#serviceregisterinterfacesavesubmit').show();
		
		// Display cancel button
		$('#serviceregisterinterfacesavesubmitcancel').show();
		
		$('#registerservice').hide();
		$('#serviceregisterinterfacesaveremovesubmit').hide();
		$('#serviceregisterinterfacemodifysubmit').hide();
		$('#serviceregisterinterfacemodifysubmitcancel').hide();
		$('#serviceregisterinterfacecopysubmit').hide();
		
		$("#registerserviceinterfaceform :input").prop("disabled", false);
		resetForm("registerserviceinterfaceform");
		
		return false;
	});
	
	// User clicks save after clicking register new service interface
	$('#serviceregisterinterfacesavesubmit').on('click', function() {
		// Example nl:tue:sec:safax:ch
		// This is ServiceID not srid in the database
		var serviceIDString = $('#registryserviceid').val();
		
		if (serviceIDString == "" || serviceIDString == "Service ID") {
			var r = confirm("You need to choose a service.");
		}
		else {			
			// Insert into safax.sfx_serviceinterface
			var message=sendAjaxRequest("../sfxservice/registry/add/service/interface", "POST", "json","", $("#registerserviceinterfaceform").serialize());
			
			// Set value of srid id attribute of the form into null
			// <input type="hidden" id="srid_creating_service_interface" name="srid"/> from admin.html
			// <form id="registerserviceinterfaceform" name="registerserviceinterfaceform" method="POST">
			$('#srid_creating_service_interface').val("");
			
			// Reload service and service interface information 
			loadServices();
		}
		
		// <div id="registerserviceinterface"> contains <form id="registerserviceinterfaceform>
		$('#registerserviceinterface').hide();
		
		// <div id="registerservice"> contains <form id="registerserviceform">
		$('#registerservice').show();
		
		$("#registerserviceinterfaceform :input").prop("disabled", true);
		resetForm("registerserviceinterfaceform");
		resetForm("registerserviceform");
		return false;
	});
	
	// User clicks cancel after clicking register new service interface
	$('#serviceregisterinterfacesavesubmitcancel').on('click', function() {
		$('#registerserviceinterface').hide();
		$('#registerservice').show();
		
		resetForm("registerserviceinterfaceform");
		return false;
	});
	
	// User clicks remove existing service interfaces
	$('#serviceregisterinterfacesaveremovesubmit').on('click', function() {
		 var siid = $('#siid_service_interface').val();
		
		if (siid == "") {
			var r = confirm("You need to choose a service interface.");
		}
		else {
			var r = confirm("Are you sure you want to delete this web service interface?");
			if (r == true) {

				var message=sendAjaxRequest("../sfxservice/registry/remove/service/interface/" + siid, "GET", "json","", "");
				resetForm("registerserviceinterfaceform");
				$('#siid_service_interface').val("");
				loadServices();
			}
		}
		return false;
	});
	
	// User clicks modify existing service interfaces
	$('#serviceregisterinterfacemodifysubmit').on('click', function() {
		var siid = $('#siid_service_interface').val();
		
		if ($('#serviceregisterinterfacemodifysubmit').text() == "Save") {
			$('#serviceregisterinterfacemodifysubmit').text("Modify Interface");
			$('#serviceregisterinterfacesaveremovesubmit').show();
			$('#serviceregisterinterfacecopysubmit').show();
			$('#serviceregisterinterfacemodifysubmitcancel').hide();
			var message=sendAjaxRequest("../sfxservice/registry/update/service/interface", "POST", "json","", $("#registerserviceinterfaceform").serialize());
			
			loadServices();
			
			// Disable the service interface form
			$("#registerserviceinterfaceform :input").prop("disabled", true);
			
			// Enable the Modify Service Interface button and Remove Service Interface button 
			$("#serviceregisterinterfacemodifysubmit").prop("disabled", false);
			$("#serviceregisterinterfacesaveremovesubmit").prop("disabled", false);
			$("#serviceregisterinterfacecopysubmit").prop("disabled", false);
		}
		else {
			// Change the Modify Service Interface button -> Save button
			$('#serviceregisterinterfacemodifysubmit').text("Save");
			
			// Hide Remove Service Interface button 
			$('#serviceregisterinterfacesaveremovesubmit').hide();
			
			// Hide Copy Service Interface button 
			$('#serviceregisterinterfacecopysubmit').hide();
			
			// Show Cancel button together with Save ( Modify Service Interface button)
			$('#serviceregisterinterfacemodifysubmitcancel').show();
			
			// Enable user to change the service interface form
			$("#registerserviceinterfaceform :input").prop("disabled", false);
		}
		return false;
	});
	
	// User clicks cancel modifying existing service interfaces
	$('#serviceregisterinterfacemodifysubmitcancel').on('click', function() {
		// Not allow user to change the service interface form
		$('#registerserviceinterfaceform :input').prop("disabled", true);
		
		// Enable the Modify Service Interface button, Remove Service Interface button, and Copy Service Interface button 
		$('#serviceregisterinterfacemodifysubmit').prop("disabled", false);
		$('#serviceregisterinterfacesaveremovesubmit').prop("disabled", false);
		$('#serviceregisterinterfacecopysubmit').prop("disabled", false);
		
		// Show the Modify Service Interface button, Remove Service Interface button, and Copy Service Interface button 
		$('#serviceregisterinterfacesaveremovesubmit').show();
		$('#serviceregisterinterfacemodifysubmit').show();
		$('#serviceregisterinterfacecopysubmit').show();
		
		// Hide this cancel button
		$('#serviceregisterinterfacemodifysubmitcancel').hide();
		
		// Show the Modify Service Interface button
		$('#serviceregisterinterfacemodifysubmit').text("Modify Interface");
		return false;
	});
	
	// User clicks copy service interfaces
	$('#serviceregisterinterfacecopysubmit').on('click', function() {
		$("#copytoservice").show();
		loadAssignedServices();
		return false;
	});
	
	// User clicks close copy service interfaces
	$("#copytoserviceclose").click(function(){
		$("#copytoservice").hide();
		$("#copyinterfacetoservice").text("");
	});
	
	// Use clicks copy service interface button in the pop up window
	$("#copyinterfacebutton").click(function(){
		// Insert into safax.sfx_serviceinterface by using srid selected in the combobox not the srid selected by user
		$("#registerserviceinterfaceform :input").prop("disabled", false);
		
		originalSRID = $('#srid_creating_service_interface').val();
				
		$('#srid_creating_service_interface').val($('#copyinterfacetoservice').val());
				
		var interfacesOfAService = sendAjaxRequest("../sfxservice/registry/get/service/interfaces/" + $('#srid_creating_service_interface').val(), "GET", "json","","");
		var boolExistingInterface = false
		
		/*
		 * $.each(obj, function(i, val)
		 * loops over all members of an object
		 */
		$.each(interfacesOfAService, function (index, value) {
			if (value.endpoint == $('#registryserviceinterfaceendpointurl').val()) {
				boolExistingInterface = true;
			}
		});
		
		if (!boolExistingInterface) {
			var message=sendAjaxRequest("../sfxservice/registry/add/service/interface", "POST", "json","", $("#registerserviceinterfaceform").serialize());
		}
		
		// Set value of srid id attribute of the form into null
		// <input type="hidden" id="srid_creating_service_interface" name="srid"/> from admin.html
		// <form id="registerserviceinterfaceform" name="registerserviceinterfaceform" method="POST">
		$('#srid_creating_service_interface').val(originalSRID);
		
		$("#registerserviceinterfaceform :input").prop("disabled", true);
		
		$("#copytoservice").hide();
		$("#copyinterfacetoservice").text("");
		
		loadServices();
		
		return;
	});
	
	// User clicks view statistics
	$('#viewstatisticsbutton').on('click', function() {
		updateAllEvaluationTime();
		updateAllUCONEvaluationTime();
	});
});

function loadUserRequests(){
	$("#activationrequestlist").text("");
	var msg=sendAjaxRequest("../sfxservice/admin/user/activation/request", "GET", "json","","");
	$('<div/>',{
		'class':'subheader demoBox'}
	).html("Activation Requests").appendTo("#activationrequestlist");
	
	$.each(msg, function (index, value) {
		var $div=$('<div/>',{'class':'asettingbox'}).data('userid',value.uid).appendTo("#activationrequestlist");
		$('<div/>',{'class':'fg-darkCyan'}).html(value.email).appendTo($div);
		$('<div/>',{'class':'fg-dark'}).html(value.fullname).appendTo($div);
		$('<div/>',{'class':'fg-dark'}).html(value.uname).appendTo($div);
		var $newdiv=$('<div/>',{'style':'padding-top:10px;padding-bottom:10px;'}).data('newuid',value.uid).appendTo($div);
		$('<button/>',{'class':'actuserreq large bg-dark fg-white', 'style':'margin-right:30px'}).data('newuid',value.uid).html('Approve').appendTo($newdiv);
		$('<button/>',{'class':'removeuserreq large bg-dark fg-white'}).data('newuid',value.uid).html('Delete').appendTo($newdiv);		
	});	
}

function activateUser(userid){
	var message=sendAjaxRequest("../sfxservice/admin/activiate/user/"+userid, "GET", "json","", "");
	loadUserRequests();
}

function removeUser(userid){
	var message=sendAjaxRequest("../sfxservice/admin/delete/user/"+userid, "GET", "json","", "");
	loadUserRequests();
}

function findUsers(uname){
	$("#allusers").text("");
	var message=sendAjaxRequest("../sfxservice/user/admin/find?uname="+uname, "GET", "json","","");
	$.each(message, function (index, value) {
			$("<option/>",{'value':value.uid}).html(value.uname).appendTo("#allusers");
	});
}

// For example, registered user or administrator
function findGroups(groupname){
	$("#usergroups").text("");
	var message=sendAjaxRequest("../sfxservice/user/find?group="+groupname, "GET", "json","","");
	
	$.each(message, function (index, value) {
		var existingAssignedGroup = false;
				
		var i;
		for( i = 0, l = assignedGroupArrays.length; i < l; i++ ) {
			if (value.gid == assignedGroupArrays[i]) {
				existingAssignedGroup = true;
			}
		}
		if (!existingAssignedGroup) {
			$("<option/>",{'value':value.gid}).html(value.groupname).appendTo("#usergroups");	
		}
	});		
}

function findAvailableServiceDependencyList(){
	var serviceIDString = $('#registryserviceid').val();
	
	if (serviceIDString == "" || serviceIDString == "Service ID") {
		var r = confirm("You need to choose a service.");
	}
	else {			
		$("#servicedependencylist").text("");
		var message=sendAjaxRequest("../sfxservice/registry/find/available/dependency/list?srid=" + $('#srid_creating_service_interface').val(), "GET", "json","","");
		$.each(message, function (index, value) {
				$("<option/>",{'value':value.srid}).html(value.serviceid).appendTo("#servicedependencylist");
		});
	}
}

// Find all available dependencies of a service
function findAllServiceDependencyList(){	
	var message=sendAjaxRequest("../sfxservice/registry/find/all/dependency/list", "GET", "json","","");
	$.each(message, function (index, value) {
		$("<option/>",{'value':value.srid}).html(value.serviceid).appendTo("#servicedependencylist");
	});
}

// Find assgined dependencies of a servcie
function findServiceAssignedDependencyList(){
	var serviceIDString = $('#registryserviceid').val();
	
	if (serviceIDString == "" || serviceIDString == "Service ID") {
		var r = confirm("You need to choose a service.");
	}
	else {			
		$("#assignedservicedependency").text("");
		var message=sendAjaxRequest("../sfxservice/registry/find/assigned/dependency/list?srid=" + $('#srid_creating_service_interface').val(), "GET", "json","","");
		$.each(message, function (index, value) {
				$("<option/>",{'value':value.srid}).html(value.serviceid).appendTo("#assignedservicedependency");
		});
	}
}

function loadUserInfo(uid){	
	var message=sendAjaxRequest("../sfxservice/user/info?uid="+uid, "GET", "json","","");
	if(jQuery.isEmptyObject(message))
		return "";
	
	$("#umeform :input").prop("disabled", false);
	$('#eusername').val(message.uname);
	$('#userfname').val(message.fullname);
	$('#useremail').val(message.email);
	$("#assignedgroups").text("");
	if(message.isactive=='1')
		$("#isactive").attr("checked", true);
	else
		$("#isactive").attr("checked", false);
	
	// Load Assigned Groups
	message=sendAjaxRequest("../sfxservice/user/groups?uid="+uid, "GET", "json","","");
	
	assignedGroupArrays = [];
	
	$.each(message, function (index, value) {
		$("<option/>",{'value':value.gid}).html(value.groupname).appendTo("#assignedgroups");
		assignedGroupArrays.push(value.gid);   
	});
	
	// Load Available Groups
	loadAllAvailableGroups();
	
	return $('#eusername').val();
}

function loadAllAvailableGroups() {
	$("#usergroups").text("");
	var message=sendAjaxRequest("../sfxservice/user/all/groups", "GET", "json","","");
	
	$.each(message, function (index, value) {
		var existingAssignedGroup = false;
		var i;
		
		if (typeof assignedGroupArrays != 'undefined') {
			for( i = 0, l = assignedGroupArrays.length; i < l; i++ ) {
				if (value.gid == assignedGroupArrays[i]) {
					existingAssignedGroup = true;
				}
			}
			
			if (!existingAssignedGroup) {
				$("<option/>",{'value':value.gid}).html(value.groupname).appendTo("#usergroups");	
			}
		}

		

	});		
}

/************* STATISTICS TASKS *************************/
function loadStats(){
	// Total all registered users not including guests
	var message=sendAjaxRequest("../sfxservice/statistic/resource/all/registered/user", "GET", "json","","");
	$('#totalusers').html(message.num);	
	
	var totalUsers = message.num;
	
	// Total all registered users who are administrators
	message=sendAjaxRequest("../sfxservice/statistic/resource?type=group&ref=admin", "GET", "json","","");
	$('#totaladminusers').html(message.num);	
	
	// Total all registered users who are staff
	message=sendAjaxRequest("../sfxservice/statistic/resource?type=group&ref=staff", "GET", "json","","");
	$('#staffusers').html(message.num);	
	
	// Total all registered users who are students
	message=sendAjaxRequest("../sfxservice/statistic/resource?type=group&ref=student", "GET", "json","","");
	$('#studentusers').html(message.num);	
	
	// Total all registered users who are partners
	message=sendAjaxRequest("../sfxservice/statistic/resource?type=group&ref=partner", "GET", "json","","");
	$('#partnerusers').html(message.num);
	
	// Total guests not registered users 
	message=sendAjaxRequest("../sfxservice/statistic/resource?type=group&ref=guest", "GET", "json","","");
	$('#guestusers').html(message.num);
	
	// Total active guests not registered users 
	message=sendAjaxRequest("../sfxservice/statistic/resource/all/active/guest", "GET", "json","","");
	$('#activeguestusers').html(message.num);
	
	// Total projects
	message=sendAjaxRequest("../sfxservice/statistic/resource?type=project&ref=all", "GET", "json","","");
	$('#totalprojects').html(message.num);
	
	var totalProjects = message.num;
	
	// Total demos
	message=sendAjaxRequest("../sfxservice/statistic/resource?type=demo&ref=all", "GET", "json","","");
	$('#totaldemo').html(message.num);
	
	var totalDemos = message.num;
	
	// Total demos per project
	$('#totaldemoperproject').html((totalDemos/totalProjects).toFixed(2));
	
	// Total demos per user
	$('#totaldemoperuser').html((totalDemos/totalUsers).toFixed(2));
	
	message=sendAjaxRequest("../sfxservice/statistic/resource?type=demo&ref=trust", "GET", "json","","");
	$('#trustpolicydemo').html(message.num);	

	message=sendAjaxRequest("../sfxservice/statistic/resource?type=demo&ref=request", "GET", "json","","");
	$('#demorequest').html(message.num);
	
	// Load dropdown checkbox for all service instance
	loadServiceInstanceDropdownMultipleCheckBox();
	
	// Load all users in the search input
	loadUserFilterSearch();
}

function loadUserFilterSearch() {
	$('#allusers').text("");
	var message=sendAjaxRequest("../sfxservice/user/get/all", "GET", "json","","");
	
	var user_data_list = [];
	
	var user = {id: "-1", value: "All users"};
	user_data_list.push(user);
	
	$.each(message, function (index, message_value) {		
		var user = {id: message_value.uid, value: message_value.uname + " - " + message_value.fullname};
		user_data_list.push(user);
	});
	
    $( "#selected_user_in_statistics_tab" ).autocomplete({
        source: user_data_list,
        minLength: 0,
        select: function( event, ui ) {
        	if (ui.item) {
        		userid_selected_in_statistics_tab = ui.item.id;
        	}
        	else {
        	}
        }
    });
}

function loadServiceInstanceDropdownMultipleCheckBox() {
	// Load PDP service instances - sfx_serviceregistry
	message=sendAjaxRequest("../sfxservice/registry/service/components?component=pdp", "GET", "json", "", "");

	if(jQuery.isEmptyObject(message)){
		
	}
	else {
		numOfPDPInstances = 0;
		
		$('#selectPDPDropdownListUL').html('');
		
		$.each(message, function (index, value) {
			var litag = $("<li/>").appendTo("#selectPDPDropdownListUL");
			
			// jQuery provides a nice way to set multiple properties at the same time, using an OBJECT LITERAL
			// The object literal is wrapped in curly braces, with each key separated from its corresponding value by a colon,
			// each key/value pair separated by a comma
			$("<input/>",
					{'value': value.serviceid, 
					 'id': value.serviceid, 
					 'type': 'checkbox',
					 'checked': ''
				    }
			).appendTo(litag);
			
			arrayListOfChosenPDPInstances.push(value.serviceid);
			
			numOfPDPInstances++;
			
			litag.append(value.servicename);
		});
	}
	
	// Load pep service instances
	message=sendAjaxRequest("../sfxservice/registry/service/components?component=pep", "GET", "json", "", "");

	if(jQuery.isEmptyObject(message)){
		
	}
	else {
		numOfPEPInstances = 0;
		$('#selectPEPDropdownListUL').html('');
		$.each(message, function (index, value) {
			if (value.serviceid == "nl:tue:sec:ucon:pep") {
			}
			else {
				var litag = $("<li/>").appendTo("#selectPEPDropdownListUL");
				$("<input/>",
						{'value':value.serviceid,
						 'type': 'checkbox',
						 'checked': ''
						}
				).appendTo(litag);
				arrayListOfChosenPEPInstances.push(value.serviceid);
				numOfPEPInstances++;
				litag.append(value.servicename);
			}
		});
	}
	
	// Load PEP Safax service instances - sfx_serviceregistry
	message=sendAjaxRequest("../sfxservice/registry/safax/service/components?component=pep", "GET", "json", "", "");

	if(jQuery.isEmptyObject(message)){
	}
	else {
		$.each(message, function (index, value) {
			var litag = $("<li/>").appendTo("#selectPEPDropdownListUL");
			$("<input/>",
					{'value':value.serviceid, 
					 'type': 'checkbox',
					 'checked': ''
					 }
			).appendTo(litag);
			
			arrayListOfChosenPEPInstances.push(value.serviceid);
			
			numOfPEPInstances++;
			
			litag.append(value.servicename);
		});
	}
	
	// Load PIP service instances
	message=sendAjaxRequest("../sfxservice/registry/service/components?component=pip", "GET", "json", "", "");

	if(jQuery.isEmptyObject(message)){
		
	}
	else {
		numOfPIPInstances = 0;

		
		$('#selectPIPDropdownListUL').html('');
		$.each(message, function (index, value) {
			var litag = $("<li/>").appendTo("#selectPIPDropdownListUL");
			$("<input/>",
					{'value':value.serviceid,
					 'type': 'checkbox',
					 'checked': ''
					}
			).appendTo(litag);
			
			arrayListOfChosenPIPInstances.push(value.serviceid);
			
			numOfPIPInstances++;	
			
			litag.append(value.servicename);
		});
	}
	
	// Load PIP Safax service instances - sfx_serviceregistry
	message=sendAjaxRequest("../sfxservice/registry/safax/service/components?component=pip", "GET", "json", "", "");

	if(jQuery.isEmptyObject(message)){
	}
	else {
		$.each(message, function (index, value) {
			var litag = $("<li/>").appendTo("#selectPIPDropdownListUL");
			$("<input/>",
					{'value':value.serviceid,
					 'type': 'checkbox',
					 'checked': ''
					}
			).appendTo(litag);
			
			arrayListOfChosenPIPInstances.push(value.serviceid);
			
			numOfPIPInstances++;	
			
			litag.append(value.servicename);
		});
	}
	
	var litag = $("<li/>").appendTo("#selectPIPDropdownListUL");
	$("<input/>",
			{'value': 'None',
			 'type': 'checkbox',
			 'id': 'pip_serviceid_none',
			 'checked': ''
			}
	).appendTo(litag);
	
	litag.append('None');
	
	arrayListOfChosenPIPInstances.push('pip_serviceid_none');
	
	numOfPIPInstances++;	
	
	message=sendAjaxRequest("../sfxservice/registry/service/components?component=pap", "GET", "json", "", "");

	if(jQuery.isEmptyObject(message)){
		
	}
	else {
		$('#selectPAPDropdownListUL').html('');
		$.each(message, function (index, value) {
			var litag = $("<li/>").appendTo("#selectPAPDropdownListUL");
			$("<input/>",
					{'value':value.serviceid,
					 'type': 'checkbox',
					 'checked': ''
					}
			).appendTo(litag);
			litag.append(value.servicename);
		});
	}
	
	message=sendAjaxRequest("../sfxservice/registry/service/components?component=ch", "GET", "json", "", "");

	if(jQuery.isEmptyObject(message)){
		
	}
	else {
		$('#selectCHDropdownListUL').html('');
		$.each(message, function (index, value) {
			var litag = $("<li/>").appendTo("#selectCHDropdownListUL");
			$("<input/>",
					{'value':value.serviceid,
					 'type': 'checkbox',
					 'checked': ''
					}
			).appendTo(litag);
			litag.append(value.servicename);
			
			arrayListOfChosenCHInstances.push(value.serviceid);
			
			numOfCHInstances++;
		});
	}
	
	// Load CH Safax service instances - sfx_serviceregistry
	message=sendAjaxRequest("../sfxservice/registry/safax/service/components?component=ch", "GET", "json", "", "");

	if(jQuery.isEmptyObject(message)){
	}
	else {
		$.each(message, function (index, value) {
			var litag = $("<li/>").appendTo("#selectCHDropdownListUL");
			$("<input/>",{
						'value':value.serviceid,
						'type': 'checkbox',
						'checked': ''
						}
			).appendTo(litag);
			litag.append(value.servicename);
			
			arrayListOfChosenCHInstances.push(value.serviceid);
			
			numOfCHInstances++;
		});
	}
	
	// Load UDF service instances - sfx service registry
	message=sendAjaxRequest("../sfxservice/registry/service/components?component=udf", "GET", "json", "", "");

	if(jQuery.isEmptyObject(message)){
		
	}
	else {
		numOfUDFInstances = 0;
		
		$('#selectUDFDropdownListUL').html('');
		
		$.each(message, function (index, value) {
			var litag = $("<li/>").appendTo("#selectUDFDropdownListUL");
			var input_checkbox = $("<input/>",{
												'value':value.serviceid,
												'id': value.serviceid,
												'checked': '',
												'type': 'checkbox'
												}
			).appendTo(litag);
			litag.append(value.servicename);
			
			arrayListOfChosenUDFInstances.push(value.serviceid);
			
			numOfUDFInstances++;
		});
		
		var litag = $("<li/>").appendTo("#selectUDFDropdownListUL");
		var input_checkbox = $("<input/>",{
											'value':'None',
											'id': 'udf_serviceid_none',
											'checked': '',
											'type': 'checkbox'
											}
		).appendTo(litag);
		
		arrayListOfChosenUDFInstances.push('udf_serviceid_none');
		
		numOfUDFInstances++;
		
		litag.append('None');
	}
	addEventsToDropdownCheckBox();
}

function loadIssues(){
	var message=sendAjaxRequest("../sfxservice/issue/view", "GET", "json","", "");
	$("#reportedissues").text("");
	if (jQuery.isEmptyObject(message)){
		$("#reportedissues").html("<span class='fg-green subheader-secondary'>No reported Issues. We are that good! </span>");
		return;
	}
	$.each(message, function (index, value) {
		$div=$('<div/>',{'class':'issuebox'}).data("issueid", value.issid).appendTo('#reportedissues');
		if(value.isfeature=='0')
			$('<div/>',{'class':'issueboxheaderbug'}).html("Bug: "+value.issueheader).appendTo($div);
		else
			$('<div/>',{'class':'issueboxheaderissue'}).html("Feature: " +value.issueheader).appendTo($div);
		$('<div/>',{'class':'issueboxdesc'}).html(value.issuedesc).appendTo($div);
		$('<div/>',{'class':'issueboxdesc'}).html("<small> Reported By ~"+value.uname+"</small>").appendTo($div);
		$('<div/>',{'class':'text-right fixissue displaycursor issueboxdesc'}).html("<i class='icon-checkmark'></i> Mark as Resolved").appendTo($div);
	});
}

/************* REGISTER SERVICE TASKS *************************/
// Load services into combobox for copy interface into a service
function loadAssignedServices(){
	$("#copyinterfacetoservice").text("");
	$("#copyinterfacebutton").prop('disabled',true);
	
	var message = sendAjaxRequest("../sfxservice/registry/get/registry/services", "GET", "json","","");

	$("#copyinterfacetoservice").text("");
	if(jQuery.isEmptyObject(message)){
		$("#copyinterfacetoservice").html("<option>No Service</option>");
		return;
	}
	$.each(message, function (index, value) {
		if (value.srid != $('#srid_creating_service_interface').val()) {
			$("<option/>",{'value':value.srid}).html(value.serviceid).appendTo("#copyinterfacetoservice");	
		}
	});
	$("#copyinterfacebutton").prop('disabled',false);
}

function loadDefaultServiceRegistryInfoForm(){
	$("#registerserviceform :input").prop("disabled", true);
	
	// Enable Register New Service button
	$("#serviceregistersubmit").prop("disabled", false);
	
	// Enable Modify Service button
	$("#serviceregistermodifysubmit").prop("disabled", false);
	
	// Enable Remove Service button
	$("#serviceregisterdelete").prop("disabled", false);
	
	// Hide cancel buttons
	$("#serviceregistersubmitcancel").hide();
	$('#serviceregistermodifycancel').hide();
	
	$('#serviceregistersubmit').text("Register New Service");
	$('#serviceregistersubmit').show();
	
	$('#serviceregistermodifysubmit').text("Modify Service");
	$('#serviceregistermodifysubmit').show();
	
	$('#serviceregisterinterfacesubmit').show();
	
	// Example nl:tue:sec:safax:ch
	// This is ServiceID not srid in the database
	var serviceIDString = $('#registryserviceid').val();
	
	if (serviceIDString == "" || serviceIDString == "Service ID") {
		$("#serviceregisterinterfacesubmit").prop("disabled", false);
	}
	else {
	}
	
	$('#serviceinterfaceactionlabel').show();
	
	// Remove existing service interfaces
	$('#serviceregisterdelete').show();
	
	$("#serviceregisterinterfacesubmit").prop("disabled", false);
}


function loadServiceRegistryInfo(serviceid){
	var message = sendAjaxRequest("../sfxservice/registry/get/one/service/" + serviceid, "GET", "json","","");
		
	if(jQuery.isEmptyObject(message))
		return "";
	
	loadDefaultServiceRegistryInfoForm()
	
	// Set value of service id attribute of the form
	$('#srid_creating_service_interface').val(message.srid);
	$('#srid_registry_service').val(message.srid);
	$('#registryserviceid').val(message.serviceid);
	$('#registryservicename').val(message.servicename);
	$('#registryservicecomponent').val(message.servicecomponent);
	$('#registryservicecomponentcombobox').val(message.servicecomponent);
	$('#registryserviceprovider').val(message.serviceprovider);
	$('#registryserviceurl').val(message.serviceurl);
	$('#registryservicedesc').val(message.servicedesc);
		
	// Load a list of services except the selected service and its dependencies
	findAvailableServiceDependencyList();
	
	//Load dependencies of the selected service
	findServiceAssignedDependencyList()

	return $('#registryservicename').val();
}

// Load information of service interface when user chooses a node child of a service node
function loadServiceRegistryInterfaceInfo(ssid){
	var message = sendAjaxRequest("../sfxservice/registry/get/one/service/interface/" + ssid, "GET", "json","","");
	if(jQuery.isEmptyObject(message))
		return "";
	
	// User clicks a service interface -> disabled the form but enable two buttons
	// Modify Service Interface + Remove Service Interface
	$("#registerserviceinterfaceform :input").prop("disabled", true);
	$("#serviceregisterinterfacemodifysubmit").prop("disabled", false);
	$("#serviceregisterinterfacesaveremovesubmit").prop("disabled", false);
	$("#serviceregisterinterfacecopysubmit").prop("disabled", false);
	$('#serviceregisterinterfacemodifysubmit').show();
	$('#serviceregisterinterfacesaveremovesubmit').show();
	$('#serviceregisterinterfacecopysubmit').show();
	
	$('#serviceregisterinterfacemodifysubmit').text("Modify Interface");
	
	// Hide Save and Cancel button when Register New Service Interface button is pressed
	$('#serviceregisterinterfacesavesubmit').hide();
	$('#serviceregisterinterfacesavesubmitcancel').hide();
	
	// Hide Cancel button of Modify Service Interface button
	$('#serviceregisterinterfacemodifysubmitcancel').hide();
		
	$('#registerservice').show();
	
	$('#siid_service_interface').val(message.siid);
	$('#registryserviceinterfacename').val(message.siname);
	$('#registryserviceinterfaceendpointurl').val(message.endpoint);
	$('#registryserviceinterfaceparams').val(message.serviceparams);
	$('#registryserviceinterfacereturntype').val(message.returntype);
	
	if (message.httpmethod == "get") {
		$('#http_get_registry_service_interface').prop("checked", true);
		$('#http_post_registry_service_interface').prop("checked", false);
	}
	else {
		$('#http_get_registry_service_interface').prop("checked", false);
		$('#http_post_registry_service_interface').prop("checked", true);
	}

	$('#registryserviceinterfacedesc').val(message.interfacedesc);
	return $('#registryservicename').val();
}


/**************** Init Functions *****************************/
function init(){
	if(!checkUserStatus())
			window.location.href = "./index.html";
	$("#umeform :input").prop("disabled", true);
	$("#registerserviceform :input").prop("disabled", true);
	$("#serviceregistersubmit").prop("disabled", false);
	$("#serviceregistermodifysubmit").prop("disabled", false);
	$("#serviceregisterdelete").prop("disabled", false);
	$("#serviceregisterinterfacesubmit").prop("disabled", true);
	$("#madmin").show();
	loadUsers();
	loadServices();
	$('#registerserviceinterface').hide();
	$('#registerservice').show();
	$('#serviceregistersubmitcancel').hide();
	$('#serviceregistermodifycancel').hide();
	

}

function loadDateTimePicker () {
	var message=sendAjaxRequest("../sfxservice/statistic/resource/start/date", "GET", "json","","");
	// Convert Milliseconds to a number using parseInt, or a unary +
	var start_date = new Date(+message.starttime);
	//alert(start_date); 
	
	var today = new Date();
	
	starttime = start_date.getTime();
	
	// plus one day
	endtime = today.getTime() + (1000 * 60 * 60 * 24);
	
	// Start time in Statistics
	$('#startdatetimepicker').datetimepicker({
		timepicker:false,
		format:'d/m/Y',
		value: start_date,
		onChangeDateTime:function(dp,$input){
		    //alert($input.val());
		    // convert datetime -> miliseconds

			var date_format = $input.val().split("/");
			
			// mm/dd/yyyy
			// date_format[0] -> day
			// date_format[1] -> month. Month counts from zero to 11. That's why date_format[1] - 1
			// date_format[2] -> year
			
			// new Date (year, month, day)			
			starttime = new Date(date_format[2], date_format[1] - 1, date_format[0]).getTime();			
		}
	});
	
	// Diable mousewheel
    $("#startdatetimepicker").bind("mousewheel", function() {
    	$("startdatetimepicker").prop('disabled', true);
    	
        return false;
    });
    
 // Diable mousewheel
    $("#enddatetimepicker").bind("mousewheel", function() {
    	$("enddatetimepicker").prop('disabled', true);
    	
        return false;
    });
	
	$('#enddatetimepicker').datetimepicker({
		timepicker:false,
		format:'d/m/Y',
		startDate: today,
		value: today,
		onChangeDateTime:function(dp,$input){
			//alert($input.val());
		    // convert datetime -> miliseconds
			
			end_date_format = $input.val().split("/");
			
			endtime = addDays(new Date(end_date_format[2], end_date_format[1] - 1, end_date_format[0]), +1).getTime();
		}
	});
	
	$('#enddatetimepicker').html(start_date);
}

function loadUsers(){
	$('#allusers').text("");
	//var message=sendAjaxRequest("../sfxservice/user/get/all", "GET", "json","","");
	//$.each(message, function (index, value) {
		//$("<option/>",{'value':value.uid}).html(value.uname).appendTo("#allusers");
	//});
}

function loadServices(){		
	$('#jstree_services_div').jstree("destroy").empty();
	var message = sendAjaxRequest("../sfxservice/registry/get/registry/services", "GET", "json","","");
	var $ul_root = $("<ul/>").appendTo("#jstree_services_div");
	
	$.each(message, function (index, value) {
		var $li = $("<li/>",{'id':value.serviceid}).html(value.serviceid).appendTo($ul_root);
		var interfacesOfAService = sendAjaxRequest("../sfxservice/registry/get/service/interfaces/" + value.srid, "GET", "json","","");
		var $ul;
		
		if(jQuery.isEmptyObject(interfacesOfAService)){
		}
		else {
			$ul = $("<ul/>").appendTo($li);
		}
		
		$.each(interfacesOfAService, function (index, value) {
			var $li = $("<li/>",{'id':value.siid}).html(value.siname).appendTo($ul);
		});
	});
	
	$('#jstree_services_div').jstree({
		"core": {
	        "themes":{
	            "icons":false
	        }
	    }
	});

	$('#jstree_services_div').on("changed.jstree", function (e, data) {
		// This is service
		if (data.node.parents.length == 1) {
			loadServiceRegistryInfo(data.node.id);
			$('#registerserviceinterface').hide();
			$('#registerservice').show();
		}
		// This is service interface
		else if (data.node.parents.length == 2) {
			loadServiceRegistryInterfaceInfo(data.node.id);
			$('#registerserviceinterface').show();
			$('#registerservice').hide();
		}
	});
	
}

function checkUserStatus(){
	var msg=sendAjaxRequest("../sfxservice/user/auth/admin/session", "GET", "json","","");
	if(msg.response=='true')
		return true;
	return false;
}

function logoutUser(){
	var msg=sendAjaxRequest("../sfxservice/user/logout", "GET", "json","","");
	window.location.href = "./index.html";
	init();
}

/**************** AJAX Requests *****************************/
function sendAjaxRequest(url,type,datatype,mimetype,data){
	/*
	 * AJAX is an acronym standing for Asynchronous JavaScript and XML 
	 * and this technology help us to load data from the server without a browser page refresh.
	 */
	
	/*
	 * Load a remote page using an HTTP request.
	 * $.ajax() returns the XMLHttpRequest that it creates.
	 */
	
	/*
	 * $.ajax( options )
	 * options − A set of key/value pairs that configure the Ajax request. All options are optional.
	 */
	return $.ajax({
 		 	url: url,
 		    type: type, // A string defining the HTTP method to use for the request (GET or POST). The default value is GET.
 		    cache: false,
 		    data: data, // A map or string that is sent to the server with the request.
        	mimeType:mimetype,
        	dataType:datatype, // A string defining the type of data expected back from the server (xml, html, json, or script).
        	async:false, // A Boolean indicating whether to perform the request asynchronously. The default value is true.
        	beforeSend:function(){ // A callback function that is executed before the request is sent.
   	          $("#spinner").show();
   	        },
 	        success: function(msg) { // A callback function that is executed if the request succeeds.
 	           $("#spinner").hide();
               return msg;
            // contentType A string containing a MIME content type to set for the request. 
            // The default value is application/x-www-form-urlencoded.
 	     }
 	}).responseJSON;
}

function resetForm(name) {
	document.getElementById(name).reset();
}

function addDays(date, amount) {
	  var tzOff = date.getTimezoneOffset() * 60 * 1000,
	      t = date.getTime(),
	      d = new Date(),
	      tzOff2;

	  t += (1000 * 60 * 60 * 24) * amount;
	  d.setTime(t);

	  tzOff2 = d.getTimezoneOffset() * 60 * 1000;
	  if (tzOff != tzOff2) {
	    var diff = tzOff2 - tzOff;
	    t += diff;
	    d.setTime(t);
	  }

	  return d;
	}