'use strict';

angular.module('capApp')
.controller('ProductionReportsByDatesCtrl', ['prodReportsServiceAjax', '$scope', '$log', '$location', '$localStorage', '$window', '$route','$rootScope','$translate',
    function (prodReportsServiceAjax, $scope, $log, $location, $localStorage, $window, $route, $rootScope, $translate){
	  

	  $scope.formats = ['MM/dd/yyyy', 'MM-dd-yyyy', 'shortDate'];
	  $scope.format = $scope.formats[0];
	  
	  $scope.dayObject = {day : null, dayPart : "all"};	  
	  $scope.rangeObject = {startDate : null, endDate : null};
	  
	  
	  $scope.report;
	  
	  
      $scope.generateDayReport = function() {  		
     	  		
    	  		var postData = {day : $scope.dayObject.day.getTime(), dayPart : $scope.dayObject.dayPart};	
    	  		
    	  		prodReportsServiceAjax.getReportByDay(postData).then(function(data){    				
    	  			$scope.report = data;
    	 		});    			
    			
      };
 
      
      $scope.generateRangeReport = function() {			
	  		
  		
	  		var postData = {startDate : $scope.rangeObject.startDate.toDate().getTime(), endDate : $scope.rangeObject.endDate.toDate().getTime()};
	  		
	  		prodReportsServiceAjax.getReportByRange(postData).then(function(data){
	  			$scope.report = data;
	 		}); 
	  		
      };

      
      
      $scope.generateCsv = function() {			
    	  		
    	  		var data = "";
    	  
    	  		for(var i=0;i<$scope.report.length;i++){
    	  			var line = $scope.report[i];
    	  			
    	  			if(line.stationOrMachineName == 'PRESS' || line.stationOrMachineName == 'PLOWFOLDER' || line.stationOrMachineName == 'FLYFOLDER' || line.stationOrMachineName == 'POPLINE'){
    	  				data = data + line.stationOrMachineName + ",";
    	  				data = data + line.rollsNumber + ",";
    	  				data = data + line.usedLength + ",";
    	  				data = data + line.hours;
    	  				data = data +"\r\n";
    	  			}
    	  			
    	  		}
 
    	  		for(var i=0;i<$scope.report.length;i++){
    	  			var line = $scope.report[i];
    	  			
    	  			if(line.stationOrMachineName != 'PRESS' && line.stationOrMachineName != 'PLOWFOLDER' && line.stationOrMachineName != 'FLYFOLDER' && line.stationOrMachineName != 'POPLINE'){
    	  				data = data + line.stationOrMachineName + ",";
    	  				data = data + line.rollsNumber + ",";
    	  				data = data + line.usedLength + ",";
    	  				data = data + line.hours;
    	  				data = data +"\r\n";
    	  			}
    	  			
    	  		}
    	  		
    	  		
    	  		var blob = new Blob([data], { type: "text/csv" });
			var objectUrl = URL.createObjectURL(blob);
			var a = document.createElement('a');
		
			a.setAttribute("href", objectUrl);
            a.setAttribute("download", "ProdReport" + '.csv');
            a.style.visibility = 'hidden';
            
			document.body.appendChild(a);
			a.click();
			window.open(objectUrl);	
	  		
      };
      
      
}]);


