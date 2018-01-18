/*
	Dropdown with Multiple checkbox select with jQuery - May 27, 2013
	(c) 2013 @ElmahdiMahmoud
	license: http://www.opensource.org/licenses/mit-license.php
*/ 

$(document).ready(function(){
	$(".dropdown dt a").on('click', function () {
		
		if($(this).attr("id") == "selectPEPDropdownList") {
			console.log("PEP dropdown click");
		     // First click show -> second click hide. Like TOGGLE 
			$("#selectPEPDropdownListUL").slideToggle('fast');
			
			$("#selectPDPDropdownListUL").hide();
			$("#selectPIPDropdownListUL").hide();
			$("#selectPAPDropdownListUL").hide();
			$("#selectCHDropdownListUL").hide();
			$("#selectUDFDropdownListUL").hide();
		}
		
		else if($(this).attr("id") == "selectPDPDropdownList") {
			console.log("PDP dropdown click");
			
		     // First click show -> second click hide. Like TOGGLE 
			$("#selectPDPDropdownListUL").slideToggle('fast');
			
			$("#selectPEPDropdownListUL").hide();
			$("#selectPIPDropdownListUL").hide();
			$("#selectPAPDropdownListUL").hide();
			$("#selectCHDropdownListUL").hide();
			$("#selectUDFDropdownListUL").hide();
		}
		
		else if($(this).attr("id") == "selectPIPDropdownList") {
			console.log("PDP dropdown click");
			
		     // First click show -> second click hide. Like TOGGLE 
			$("#selectPIPDropdownListUL").slideToggle('fast');
			
			$("#selectPDPDropdownListUL").hide();
			$("#selectPEPDropdownListUL").hide();
			$("#selectPAPDropdownListUL").hide();
			$("#selectCHDropdownListUL").hide();
			$("#selectUDFDropdownListUL").hide();
		}
		
		else if($(this).attr("id") == "selectPAPDropdownList") {
			console.log("PDP dropdown click");
			
		     // First click show -> second click hide. Like TOGGLE 
			$("#selectPAPDropdownListUL").slideToggle('fast');
			
			$("#selectPDPDropdownListUL").hide();
			$("#selectPEPDropdownListUL").hide()
			$("#selectPIPDropdownListUL").hide()
			$("#selectCHDropdownListUL").hide();
			$("#selectUDFDropdownListUL").hide();
		}
		
		else if($(this).attr("id") == "selectCHDropdownList") {
			console.log("PDP dropdown click");
			
		     // First click show -> second click hide. Like TOGGLE 
			$("#selectCHDropdownListUL").slideToggle('fast');
			
			$("#selectPDPDropdownListUL").hide();
			$("#selectPEPDropdownListUL").hide()
			$("#selectPAPDropdownListUL").hide();
			$("#selectPIPDropdownListUL").hide()
			$("#selectUDFDropdownListUL").hide();
		}
		
		else if($(this).attr("id") == "selectUDFDropdownList") {
			console.log("PDP dropdown click");
			
		     // First click show -> second click hide. Like TOGGLE 
			$("#selectUDFDropdownListUL").slideToggle('fast');
			
			$("#selectPDPDropdownListUL").hide();
			$("#selectPEPDropdownListUL").hide()
			$("#selectPAPDropdownListUL").hide();
			$("#selectPIPDropdownListUL").hide()
			$("#selectCHDropdownListUL").hide();
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


    $('.mutliSelect input[type="checkbox"]').on('click', function () {
        var title = $(this).closest('.mutliSelect').find('input[type="checkbox"]').val(),
            title = $(this).val() + ",";
      
        if ($(this).is(':checked')) {
            var html = '<span title="' + title + '">' + title + '</span>';
            $('.multiSel').append(html);
            $(".hida").hide();
        } 
        else {
            $('span[title="' + title + '"]').remove();
            var ret = $(".hida");
            $('.dropdown dt a').append(ret);
            
        }
    });
});

