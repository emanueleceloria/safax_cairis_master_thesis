/*
	Dropdown with Multiple checkbox select with jQuery - May 27, 2013
	(c) 2013 @ElmahdiMahmoud
	license: http://www.opensource.org/licenses/mit-license.php
*/ 

// Average Stats of Statistics tab
arrayListOfChosenPEPInstances = []; // a list of checked PEP instances
arrayListOfChosenPDPInstances = []; // a list of checked PDP instances
arrayListOfChosenPIPInstances = []; // a list of checked PDP instances
arrayListOfChosenCHInstances = []; // a list of checked CH instances
arrayListOfChosenUDFInstances = []; // a list of checked UDF instances

numOfPDPInstances = 0;
numOfPEPInstances = 0;
numOfPIPInstances = 0;
numOfCHInstances = 0;
numOfUDFInstances = 0;

function updateAllUCONEvaluationTime() {
	var url = "../sfxservice/statistic/resource/ucon/totalevaluation";
	var message = sendAjaxRequest(url, "GET", "json","","");
	if (message.average != null) {
		// Number of UCON Sessions
		$('#numberuconsession').html(parseInt(message.num));
		// UCON Session Time
		$('#uconsessiontime').html(parseFloat(message.average).toFixed(3)  + " seconds");
	}
	else {
		// Number of UCON Sessions
		$('#numberuconsession').html("null ");
		// UCON Session Time
		$('#uconsessiontime').html("null "  + "seconds");
	}
}

function updateAllEvaluationTime() {
	var url = "../sfxservice/statistic/resource/totalevaluation?ref=eval&assignedgroups=1&assignedgroups=2&uid=" + userid_selected_in_statistics_tab + "&starttime=" + starttime + "&" + "endtime=" + endtime;
	// PDP
	for (var i = 0; i < arrayListOfChosenPDPInstances.length; i++) {
		pdp_serviceid= arrayListOfChosenPDPInstances[i];
		url = url.concat("&pdp_serviceid=" + arrayListOfChosenPDPInstances[i]) ;
	}
	
	// PEP
	for (var j = 0; j < arrayListOfChosenPEPInstances.length; j++) {
		pep_serviceid= arrayListOfChosenPDPInstances[j];
		url = url.concat("&pep_serviceid=" + arrayListOfChosenPEPInstances[j]) ;
	}
	
	// PIP
	for (var j = 0; j < arrayListOfChosenPIPInstances.length; j++) {
		pip_serviceid= arrayListOfChosenPIPInstances[j];
		url = url.concat("&pip_serviceid=" + arrayListOfChosenPIPInstances[j]) ;
	}
	
	// CH
	for (var k = 0; k < arrayListOfChosenCHInstances.length; k++) {
		ch_serviceid = arrayListOfChosenCHInstances[k];
		url = url.concat("&ch_serviceid=" + arrayListOfChosenCHInstances[k]) ;
	}
	
	// UDF
	for (var l = 0; l < arrayListOfChosenUDFInstances.length; l++) {
		udf_serviceid = arrayListOfChosenUDFInstances[l];
		url = url.concat("&udf_serviceid=" + arrayListOfChosenUDFInstances[l]) ;
	}
	
	var message = sendAjaxRequest(url, "GET", "json","","");
	
	if (message.average != null) {
		// Total Evaluation
		$('#evaluationtime').html(parseFloat(message.average).toFixed(3)  + " seconds");
		$('#numberevaluationtime').html(parseInt(message.num));
		// PDP Service Evaluation
		$('#evaltimepdp').html(parseFloat(message.average_pdp_service).toFixed(3)  + " seconds");
		// PEP Service Evaluation
		$('#evaltimepep').html(parseFloat(message.average_pep_service).toFixed(3)  + " seconds");
		// CH Service Evaluation
		$('#evaltimech').html(parseFloat(message.average_ch_service).toFixed(3)  + " seconds");
		// PIP Service Evaluation
		$('#evaltimepip').html(parseFloat(message.average_pip_service).toFixed(3)  + " seconds");
		// UDF Service Evaluation
		$('#evaltimeudf').html(parseFloat(message.average_udf_service).toFixed(3)  + " seconds");
	}
	else {
		$('#evaluationtime').html("null "  + "seconds");
		$('#numberevaluationtime').html(parseInt(message.num));
		// PDP Service Evaluation
		$('#evaltimepdp').html("null "  + "seconds");
		// PEP Service Evaluation
		$('#evaltimepep').html("null "  + "seconds");
		// CH Service Evaluation
		$('#evaltimech').html("null "  + "seconds");
		// PIP Service Evaluation
		$('#evaltimepip').html("null "  + "seconds");
		// UDF Service Evaluation
		$('#evaltimeudf').html("null "  + "seconds");
	}
}

$(document).ready(function(){
	$(".dropdown dt a").on('click', function () {
		if($(this).attr("id") == "selectPEPDropdownList") {
		     // First click show -> second click hide. Like TOGGLE 
			$("#selectPEPDropdownListUL").slideToggle('fast');
			$("#selectPDPDropdownListUL").hide();
			$("#selectPIPDropdownListUL").hide();
			$("#selectPAPDropdownListUL").hide();
			$("#selectCHDropdownListUL").hide();
			$("#selectUDFDropdownListUL").hide();
			// Check status of the dropdown list
			// update the PEP evaluation time
			if ($("#selectPEPDropdownListUL").is(":hidden")) {
			}
		}
		
		else if($(this).attr("id") == "selectPDPDropdownList") {
			// Check status of the dropdown list
			// update the PEP evaluation time
			if ($("#selectPDPDropdownListUL").is(":hidden")) {
			}
            // First click show -> second click hide. Like TOGGLE 
			$("#selectPDPDropdownListUL").slideToggle('fast');
			$("#selectPEPDropdownListUL").hide();
			$("#selectPIPDropdownListUL").hide();
			$("#selectPAPDropdownListUL").hide();
			$("#selectCHDropdownListUL").hide();
			$("#selectUDFDropdownListUL").hide();
		}
		
		else if($(this).attr("id") == "selectPIPDropdownList") {
		     // First click show -> second click hide. Like TOGGLE 
			$("#selectPIPDropdownListUL").slideToggle('fast');
			$("#selectPDPDropdownListUL").hide();
			$("#selectPEPDropdownListUL").hide();
			$("#selectPAPDropdownListUL").hide();
			$("#selectCHDropdownListUL").hide();
			$("#selectUDFDropdownListUL").hide();
		}
		
		else if($(this).attr("id") == "selectPAPDropdownList") {
		     // First click show -> second click hide. Like TOGGLE 
			$("#selectPAPDropdownListUL").slideToggle('fast');
			$("#selectPDPDropdownListUL").hide();
			$("#selectPEPDropdownListUL").hide()
			$("#selectPIPDropdownListUL").hide()
			$("#selectCHDropdownListUL").hide();
			$("#selectUDFDropdownListUL").hide();
		}
		
		else if($(this).attr("id") == "selectCHDropdownList") {
		     // First click show -> second click hide. Like TOGGLE 
			$("#selectCHDropdownListUL").slideToggle('fast');
			$("#selectPDPDropdownListUL").hide();
			$("#selectPEPDropdownListUL").hide()
			$("#selectPAPDropdownListUL").hide();
			$("#selectPIPDropdownListUL").hide()
			$("#selectUDFDropdownListUL").hide();
			// Check status of the dropdown list
			// update the CH evaluation time
			if ($("#selectCHDropdownListUL").is(":hidden")) {
			}
		}
		
		else if($(this).attr("id") == "selectUDFDropdownList") {
		     // First click show -> second click hide. Like TOGGLE 
			$("#selectUDFDropdownListUL").slideToggle('fast');
			
			$("#selectPDPDropdownListUL").hide();
			$("#selectPEPDropdownListUL").hide()
			$("#selectPAPDropdownListUL").hide();
			$("#selectPIPDropdownListUL").hide()
			$("#selectCHDropdownListUL").hide();
			// Check status of the dropdown list
			// update the CH evaluation time
			if ($("#selectUDFDropdownListUL").is(":hidden")) {
			}
		}
    });

    $(".dropdown dd ul li a").on('click', function () {
        $(".dropdown dd ul").hide();
    });

    function getSelectedValue(id) {
         return $("#" + id).find("dt a span.value").html();
    }
    $(document).bind('click', function (e) {
        var $clicked = $(e.target);
        if (!$clicked.parents().hasClass("dropdown")) $(".dropdown dd ul").hide();
    });
});

function addEventsToDropdownCheckBox() {
	$('.mutliSelect #selectPDPDropdownListUL input[type="checkbox"]').on('click', function () {
	        var title = $(this).val();
	        if ($(this).is(':checked')) {
	        	arrayListOfChosenPDPInstances.push(title);
	        } 
	        else {
	        	var index = arrayListOfChosenPDPInstances.indexOf(title);
	        	if (index > -1) {
	        		arrayListOfChosenPDPInstances.splice(index, 1);
	        	}	            
	        }
	    });
	$('.mutliSelect #selectPEPDropdownListUL input[type="checkbox"]').on('click', function () {
		var title = $(this).val();
	      
	    if ($(this).is(':checked')) {
	    	arrayListOfChosenPEPInstances.push(title);
	    } 
	    else {
	    	var index = arrayListOfChosenPEPInstances.indexOf(title);
	        if (index > -1) {
	        	arrayListOfChosenPEPInstances.splice(index, 1);
	        }	            
	    }
    });
	  $('.mutliSelect #selectPIPDropdownListUL input[type="checkbox"]').on('click', function () {
		  var title = $(this).val();
	         
	      if ($(this).is(':checked')) {
	    	 if (title === 'None') {
	    	   arrayListOfChosenPIPInstances.push('pip_serviceid_none');   
	    	 }
	        else {
	    	   arrayListOfChosenPIPInstances.push(title);   
	        }
	      } 
	      else {
	       	if (title === 'None') {
	       		var index = arrayListOfChosenPIPInstances.indexOf('pip_serviceid_none');
		       	
		       	if (index > -1) {
		       		arrayListOfChosenPIPInstances.splice(index, 1);
		       	}	  	
	       	}
	       	else {
	       		var index = arrayListOfChosenPIPInstances.indexOf(title);
		        	
		       	if (index > -1) {
		       		arrayListOfChosenPIPInstances.splice(index, 1);
		       	}	  	
	       	}          
	     }
	     // always move udf_serviceid_none to the end of the array
	     var index = arrayListOfChosenPIPInstances.indexOf('pip_serviceid_none');
	     if (index > -1) {
      		arrayListOfChosenPIPInstances.splice(index, 1);
      	   	arrayListOfChosenPIPInstances.push('pip_serviceid_none');
	     }	 
	 });
	  
	  $('.mutliSelect #selectCHDropdownListUL input[type="checkbox"]').on('click', function () {
	       var title = $(this).val();
	       if ($(this).is(':checked')) {
	        	arrayListOfChosenCHInstances.push(title);
	        } 
	        else {
	        	var index = arrayListOfChosenCHInstances.indexOf(title);
	        	if (index > -1) {
	        		arrayListOfChosenCHInstances.splice(index, 1);
	        	}	            
	        }
	    });
	  
	  $('.mutliSelect #selectUDFDropdownListUL input[type="checkbox"]').on('click', function () {
	       var title = $(this).val();
	         
	       if ($(this).is(':checked')) {
	    	   if (title === 'None') {
	    		   arrayListOfChosenUDFInstances.push('udf_serviceid_none');   
	    	   }
	    	   else {
	    		   arrayListOfChosenUDFInstances.push(title);   
	    	   }
	        } 
	        else {
	        	if (title === 'None') {
	        		var index = arrayListOfChosenUDFInstances.indexOf('udf_serviceid_none');
		        	if (index > -1) {
		        		arrayListOfChosenUDFInstances.splice(index, 1);
		        	}	  	
	        	}
	        	else {
	        		var index = arrayListOfChosenUDFInstances.indexOf(title);
		        	
		        	if (index > -1) {
		        		arrayListOfChosenUDFInstances.splice(index, 1);
		        	}	  	
	        	}          
	        }
	       // always move udf_serviceid_none to the end of the array
	       var index = arrayListOfChosenUDFInstances.indexOf('udf_serviceid_none');
	       if (index > -1) {
       		arrayListOfChosenUDFInstances.splice(index, 1);
       	   	arrayListOfChosenUDFInstances.push('udf_serviceid_none');
	       }	 
	    });
}
