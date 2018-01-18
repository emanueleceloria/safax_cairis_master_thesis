/**
 * @author: Samuel Kaluvuri
 */
$(document).ready(function(){
	init();
	/*************** MENU *********************/
	$("#mact").removeClass('menubox').addClass('menuboxactive');
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
	
	
	
	
	
	
	
	loadTransactions();
	$(document).on('click','.transactionlink', function() {
		$('#activityitems').show();
		$('#deleteDetails').hide();
		$('#displayStatusMessage').hide();
		displayTransaction($(this).data('actionid'));
		displayStatistics($(this).data('actionid'));
	});
	
	$(document).on('click','.deletelink', function() {
		$('#activityitems').hide();
		$('#displayStatusMessage').hide();
		$('#deleteDetails').show();
		displayDeletedItem($(this).data('actionid'));
	});
	
	$("#undoDelete").click(function(){
		var actionid=$(this).data('actionid');
		var msg=sendAjaxRequest("../sfxservice/log/undo/delete/"+actionid, "GET", "json","","");
		
		loadTransactions();
		$('#activityitems').hide();
		$('#deleteDetails').hide();
		$('#displayStatusMessage').show();
		$('#displayStatusMessage').html(msg.message);
		
	});
});


function displayStatistics(actionid){
	var msg=sendAjaxRequest("../sfxservice/statistic/request?type=time&transactionid="+actionid, "GET", "json","","");
	if(jQuery.isEmptyObject(msg)){
		$("#statisticDisplay").text("");
	}
	else{
		$("#statisticDisplay").html("Request Evaluation Time: "+msg.reqtime+ " Seconds");
		return;
	}
	return;
}
function displayTransaction(actionid){
	var msg=sendAjaxRequest("../sfxservice/log/transaction/"+actionid, "GET", "json","","");
	var message="";
	$.each(msg, function (index, value) {
		message+=value.component+":";
		message+=value.log+"\n";
	});
	
	$("#logdisplay").text("");
	$("#logdisplay").text(message);
}

function displayDeletedItem(actionid){
	var msg=sendAjaxRequest("../sfxservice/log/deleted/item/"+actionid, "GET", "json","","");
	var resourcename="";
	if(msg.demoname!=undefined)
		resourcename=msg.demoname;
	else
		resourcename=msg.projectname;
	
	$("#undoDelete").data('actionid',msg.uaid);
	$("#itemtype").html(msg.actionresourcetype);
	$("#resourcename").html(resourcename);
	$("#deletedtime").html(timeConverter(msg.starttime*1));
}

function loadTransactions(){
	var msg=sendAjaxRequest("../sfxservice/log/session/transaction/", "GET", "json","","");
	$("#currenttransactionlist").html("");
	if(jQuery.isEmptyObject(msg)){
		var $li = $("<li/>").appendTo("#currenttransactionlist");
		$("<a/>").html("No Transactions").appendTo($li);
	}
	$.each(msg, function (index, value) {
		var $li = $("<li/>").data('actionid', value.uaid).appendTo("#currenttransactionlist");
		$("<a/>",{'href':'#', 'class':'transactionlink'}).data('actionid', value.uaid).html(value.actionid).appendTo($li);
	});
	
	msg=sendAjaxRequest("../sfxservice/log/past/transaction", "GET", "json","","");
	$("#pasttransactionlist").html("");
	if(jQuery.isEmptyObject(msg)){
		var $li = $("<li/>").appendTo("#pasttransactionlist");
		$("<a/>").html("No Transactions").appendTo($li);
	}
	$.each(msg, function (index, value) {
		var $li = $("<li/>").data('actionid', value.uaid).appendTo("#pasttransactionlist");
		$("<a/>",{'href':'#', 'class':'transactionlink'}).data('actionid', value.uaid).html(value.actionid).appendTo($li);
	});
	
	msg=sendAjaxRequest("../sfxservice/log/delete/transaction", "GET", "json","","");
	$("#deleteditems").html("");
	if(jQuery.isEmptyObject(msg)){
		var $li = $("<li/>").appendTo("#deleteditems");
		$("<a/>").html("Empty").appendTo($li);
	}
	$.each(msg, function (index, value) {
		var $li = $("<li/>").data('actionid', value.uaid).appendTo("#deleteditems");
		$("<a/>",{'href':'#', 'class':'deletelink'}).data('actionid', value.uaid).html(value.actionid).appendTo($li);
	});
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


function timeConverter(UNIX_timestamp){
  var a = new Date(UNIX_timestamp);
  var months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
  var year = a.getFullYear();
  var month = months[a.getMonth()];
  var date = a.getDate();
  var hour = a.getHours();
  var min = a.getMinutes();
  var sec = a.getSeconds();
  var time = date + ',' + month + ' ' + year + ' ' + hour + ':' + min + ':' + sec ;
  return time;
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