'use strict';

/**
 * @ngdoc function
 * @name capApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the capApp
 */
angular.module('capApp')
  .controller('MainCtrl', ['$rootScope','$scope', '$filter', 'rollServiceAjax', 'jobServiceAjax', 'lookupServiceAjax', 'userServiceAjax', 'screenSize', '$translatePartialLoader', '$translate', 
	  	'$localStorage','authenticationServiceAjax', '$window',
		function ($rootScope, $scope, $filter, rollServiceAjax, jobServiceAjax, lookupServiceAjax, userServiceAjax, screenSize, $translatePartialLoader, $translate, 
				$localStorage, authenticationServiceAjax, $window) {
	  
		   $translatePartialLoader.addPart('main');
		   $translate.refresh();
		   
	  		$rootScope.hasAnyRole = function(rolesList){
	  			userServiceAjax.getLoggedInUserId().then(function(data){
	  				var result = false;
		  			for(var i = 0; i < rolesList.length; i++){
		  				if(data.roles.indexOf(rolesList[i]) > -1){
		  					result = true;
		  					break;
		  				}
		  			}
		  			return result;
	  			});
	  		};
	  		
	        $scope.logout = function() {			
				console.log("user is requesting logout : revoke token"); 
				authenticationServiceAjax.revokeToken();
				
				console.log("user is requesting logout : remove user data"); 				
				$rootScope.loggedInUserName = null;
 				$rootScope.loggedInUserId = null;
 				$rootScope.loggedInUser = null;	
 				
 				console.log("user is requesting logout : redirect to login"); 
				$window.location.href = '/#!/login';
	        };
  
	  		$rootScope.clearCache = function(){
	  			 lookupServiceAjax.clearCache().then(function(){
	  				 
	  			 });
	  		 };
	  		 
	  		 screenSize.rules = {
				    bigScreen: '(min-width: 2500px)',
				    mediumScreen: '(min-width: 1601px) and (max-width: 2499px)',
				    smallScreen: '(min-width: 500px)  and (max-width: 1600px)',
				    moblieScreen: '(min-width: 320px) and (max-width: 484px) and (-webkit-min-device-pixel-ratio: 2)',
	  		 };
	  		 if(screenSize.is('bigScreen')){
	  			$rootScope.screenType = '4';
		  	 }else if(screenSize.is('mediumScreen')){
		  		$rootScope.screenType = '3';
		  	 }else if(screenSize.is('smallScreen')){
		  		$rootScope.screenType = '2';
		  	 }else{
		  		$rootScope.screenType = '2';// 1 is too small
		  	 }
	  		 $rootScope.screenCheck = screenSize.when('bigScreen', function(isMatch){
	  			$rootScope.screenType = '4';
		  	 });
	  		$rootScope.screenCheck = screenSize.when('mediumScreen', function(isMatch){
	  			$rootScope.screenType = '3';
		  	 });
	  		 $rootScope.screenCheck = screenSize.when('smallScreen', function(isMatch){
	  			$rootScope.screenType = '2';
		  	 });
	  		$rootScope.screenCheck = screenSize.when('moblieScreen', function(isMatch){
	  			$rootScope.screenType = '2';// 1 is too small
		  	 });
	  		
	  		$rootScope.openRollTag = function (rollId) {
				rollServiceAjax.getRollById(rollId).then(function(data){
					$scope.roll = data;
					lookupServiceAjax.readAll('MachineType').then(function(mtData){
						for(var i = 0; i < mtData.length; i++){
							if($scope.roll.machineTypeId == mtData[i].id){
								if(mtData[i].name.indexOf(' Folder') !== -1){
									$scope.roll.machineTypeId = mtData[i].name.substring(0, mtData[i].name.indexOf(' Folder'));
								}
								if(mtData[i].name.indexOf(' Line') !== -1){
									$scope.roll.machineTypeId = mtData[i].name.substring(0, mtData[i].name.indexOf(' Line'));
								}
								break;
							}
						}
					
					      //jobServiceAjax.findNextStationJob(loadTag.jobId).then(function(data2){
						  // $scope.nextJob = data2;
						   var canvasRollId = document.createElement("canvas");
						   JsBarcode(canvasRollId, angular.isDefined($scope.roll.rollId) ? $scope.roll.rollId : 0, {
							   format: "CODE128",
							   background:'transparent',
							   width: 3,
							   height: 35,
							   displayValue: false});
						   var rollIdDataURL = canvasRollId.toDataURL("image/svg");
						   
					      var docDefinition = { 
					    		  //pageSize: 'A8',
					    		  pageSize: { width: 300, height: 180 },
					    		  //pageOrientation: 'portrait',
					    		  pageOrientation: 'landscape', 
					    		  pageMargins: [1, -1, 1, 0],
					   	       content: [
					   	    	   	{text: $scope.roll.rollId, style: 'headerStyle', margin: [1, -10, 1, -8]},
					   	    	   	//{image: rollIdDataURL, style: 'contentStyle'},
					   	    	   	//{text: ($filter('number')(angular.isDefined($scope.roll.length) ? $scope.roll.length : 0) ) + ' Feet', style: 'contentStyle'},
					   	    	    //{text: (angular.isDefined($scope.roll.jobs) ? $scope.roll.jobs.length : 0) + ' Job(s)', style: 'contentStyle'},
					   	    	   	//{text: $scope.roll.paperType.name, style: 'contentStyle'},
					   	    	   	{
						   	    		image: rollIdDataURL, fontSize:6, style: 'centerContent', margin: [1, -15, 1, -2]
						   	    	},
						   	    	{
				   						text: ($scope.roll.machineTypeId) +  
				   						($scope.roll.impositionTypeId != null ? ' ' + $scope.roll.impositionTypeId : '' ) + ' / '  +
				   						($scope.roll.rollType.id == 'PRODUCED' ? $scope.roll.jobs.length + ' Job(s) / ' : '')  +
				   						($filter('number')(angular.isDefined($scope.roll.length) ? $scope.roll.length : 0) ) + ($rootScope.unitSystem == "US" ? ' ft' : ' m')
				   						, style: 'contentStyle'
						   	    	},
						   	    	{text: $scope.roll.paperType.name, style: 'contentStyle'},
					   			],
					   		styles: {
					   		     headerStyle: {
					   		       fontSize: 100,
					   		       bold: true,
					   		       alignment: 'center',
					   		     },
					   		     contentStyle: {
					   		        fontSize: 12,
					   		        bold: true,
					   		        alignment: 'center'
					   		     },
					   		     centerContent: {
					   		    	alignment: 'center'
					   		     }
					   		}
					   	};
					   	 pdfMake.createPdf(docDefinition).print();
					 });
				});
			};
			
			$rootScope.openLoadTag = function (loadTag) {
				jobServiceAjax.getJobByIdWithLam(loadTag.jobId).then(function(data){
					   $scope.job = data;
					   jobServiceAjax.findNextStationJob(loadTag.jobId).then(function(data2){
						   $scope.nextJob = data2;
						   var canvasLoadTagId = document.createElement("canvas");
						   JsBarcode(canvasLoadTagId, angular.isDefined(loadTag.loadTagId) ? loadTag.loadTagId : 0, {
							   format: "CODE128",
							   background:'transparent',
							   width: 3,
							   height: 35,
							   displayValue: false});
						   var loadTagIdDataURL = canvasLoadTagId.toDataURL("image/svg");
						   
					      var docDefinition = { 
					    		  //pageSize: 'A8',
					    		  pageSize: { width: 300, height: 180 },
					    		  //pageOrientation: 'portrait',
					    		  pageOrientation: 'landscape', 
					    		  pageMargins: [1, -1, 1, 0],
					   	       content: [
					   	    	   	{text: loadTag.loadTagId, style: 'headerStyle', margin: [1, -10, 1, -8]},
					   	    	   	
					   	    	   	{
						   	    		image: loadTagIdDataURL, fontSize:6, style: 'centerContent', margin: [1, -15, 1, -2]
						   	    	},
						   	    	{
				   					  text: loadTag.quantity + ($scope.job.stationId != 'COVERPRESS' ? (' (' + $scope.job.quantityNeeded + ')') : '') +
				   							(($scope.job.stationId == 'COVERPRESS' && angular.isDefined($scope.job.partLamination)) ? (' / ' + $scope.job.partLamination.id) :  '') +  
					   						 ' / Cart ' + loadTag.cartNum +
					   						 (angular.isDefined($scope.nextJob.stationId) ? (' / ' + $scope.nextJob.stationId) : '') +
					   						' / ID ' + $scope.job.orderId 
					   						, style: 'contentStyle'
						   	    	},
						   	    	{text: $scope.job.partTitle, style: 'smallerContentStyle'},
					   			],
					   		styles: {
					   		     headerStyle: {
					   		       fontSize: 100,
					   		       bold: true,
					   		       alignment: 'center',
					   		     },
					   		     contentStyle: {
					   		        fontSize: 12,
					   		        bold: true,
					   		        alignment: 'center'
					   		     },
					   		     smallerContentStyle: {
					   		        fontSize: 9,
					   		        bold: true,
					   		        alignment: 'center'
					   		     },
					   		     centerContent: {
					   		    	alignment: 'center'
					   		     }
					   		}
					   	};
					   	 pdfMake.createPdf(docDefinition).print();
					});
				});
			};
			
		    /*$rootScope.openLoadTag = function (loadTag) {
			    jobServiceAjax.getJobById(loadTag.jobId).then(function(data){
				   $scope.job = data;
				   jobServiceAjax.findNextStationJob(loadTag.jobId).then(function(data2){
					   $scope.nextJob = data2;
					   
					   var canvasLoadTagId = document.createElement("canvas");
					   JsBarcode(canvasLoadTagId, angular.isDefined(loadTag.loadTagId) ? loadTag.loadTagId : 0, {
						   format: "CODE128",
						   width: 1,
						   height: 20,
						   displayValue: false});
					   var loadTagIdDataURL = canvasLoadTagId.toDataURL("image/png");
					   
					   
					   var loadInfoTableValues = [];
					   var loadInfoBodyRow = [];
					   loadInfoBodyRow.push({ text: 'ORDER ID', style: 'tableHeaderStyle' });
					   loadInfoBodyRow.push({ text: angular.isDefined($scope.job.order.orderId) ? $scope.job.order.orderId : 0, style: 'tableContentStyle'});
					   loadInfoTableValues.push(loadInfoBodyRow);
					   loadInfoBodyRow = [];
					   loadInfoBodyRow.push({ text: 'QTY', style: 'tableHeaderStyle' });
					   loadInfoBodyRow.push({ text: loadTag.quantity, style: 'tableContentStyle'});
					   loadInfoTableValues.push(loadInfoBodyRow);
					   loadInfoBodyRow = [];
					   loadInfoBodyRow.push({ text: 'Cart#', style: 'tableHeaderStyle' });
					   loadInfoBodyRow.push({ text: angular.isDefined(loadTag.cartNum) ? loadTag.cartNum : '', style: 'tableContentStyle'});
					   loadInfoTableValues.push(loadInfoBodyRow);
					   
					   var partInfoTableValues = [];
					   var partInfoBodyRow = [];
					   partInfoBodyRow.push({ text: 'TITLE', style: 'tableHeaderStyle' });
					   partInfoBodyRow.push({ text: (angular.isDefined($scope.job.part) && angular.isDefined($scope.job.part.title)) ? $scope.job.part.title : '', style: 'tableContentStyle'});
					   partInfoTableValues.push(partInfoBodyRow);
					   partInfoBodyRow = [];
					   partInfoBodyRow.push({ text: 'Next Station', style: 'tableHeaderStyle' });
					   partInfoBodyRow.push({ text: angular.isDefined($scope.nextJob.stationId) ? $scope.nextJob.stationId : '', style: 'tableContentStyle'});
					   partInfoTableValues.push(partInfoBodyRow);
					   
					   /*var partInfoTableValues = [];
					   var partInfoBodyRow = [];
					   partInfoBodyRow.push({ text: 'TITLE', style: 'tableHeaderStyle' });
					   partInfoBodyRow.push({ text: (angular.isDefined($scope.job.part) && angular.isDefined($scope.job.part.title)) ? $scope.job.part.title : '', style: 'tableContentStyle'});
					   partInfoBodyRow.push({ text: 'JOB ID', style: 'tableHeaderStyle' });
					   partInfoBodyRow.push({ text: angular.isDefined($scope.job.jobId) ? $scope.job.jobId : 0, style: 'tableContentStyle'});
					   partInfoTableValues.push(partInfoBodyRow);
					   partInfoBodyRow = [];
					   partInfoBodyRow.push({ text: 'ISBN', style: 'tableHeaderStyle' });
					   partInfoBodyRow.push({ text: (angular.isDefined($scope.job.part) && angular.isDefined($scope.job.part.isbn)) ? $scope.job.part.isbn : '', style: 'tableContentStyle'});
					   partInfoBodyRow.push({ text: 'Due Date', style: 'tableHeaderStyle' });
					   partInfoBodyRow.push({ text: angular.isDefined($scope.job.order.dueDate) ? $filter('date')($scope.job.order.dueDate,'MM/dd/yyyy') : '', style: 'tableContentStyle'});
					   partInfoTableValues.push(partInfoBodyRow);

					   var stationInfoTableValues = [];
					   var stationInfoBodyRow = [];
					   stationInfoBodyRow.push({ text: 'Current Station', style: 'tableHeaderStyle' });
					   stationInfoBodyRow.push({ text: angular.isDefined($scope.job.stationId) ? $scope.job.stationId : '', style: 'tableContentStyle'});
					   stationInfoTableValues.push(stationInfoBodyRow);
					   stationInfoBodyRow = [];
					   stationInfoBodyRow.push({ text: 'Next Station', style: 'tableHeaderStyle' });
					   stationInfoBodyRow.push({ text: angular.isDefined($scope.nextJob.stationId) ? $scope.nextJob.stationId : '', style: 'tableContentStyle'});
					   stationInfoTableValues.push(stationInfoBodyRow);
					   stationInfoBodyRow = [];
					   stationInfoBodyRow.push({ text: 'Cart Number', style: 'tableHeaderStyle' });
					   stationInfoBodyRow.push({ text: angular.isDefined(loadTag.cartNum) ? loadTag.cartNum : '', style: 'tableContentStyle'});
					   stationInfoTableValues.push(stationInfoBodyRow);
				   
					   var docDefinition2 = { 
							   pageSize: 'A8',
							   //pageSize: { width: 75, height: 50 },
							 //pageOrientation: 'portrait',
					    		  pageOrientation: 'landscape', 
					    		  pageMargins: [5, 0, 0, 0],
					   	   content: [
					   			/*{   columnGap: 40,
					   				 columns: [
					   				           {width: 'auto',
					   				        	text: (angular.isDefined(loadTag.loadTagId) ? loadTag.loadTagId : 0), style: 'headerStyle'
					   				           },
					   				           {width: 'auto',
					   				        	   stack: [
					   				        		{image: loadTagIdDataURL, style: 'headerStyle'},
					   				        		{
										   				 style: 'tableStyle',
										   				 table: {
																	headerRows: 1,
																	widths: [15,35,40],
																	body: loadInfoTableValues
															 },
															 layout: 'noBorders'
										   			 }
					   				           		]
					   				           }
					   				  ]
					   			 },
					   			{
					   				 style: 'tableStyle',
					   				 table: {
												headerRows: 1,
												widths: [17,90,30,33],
												body: partInfoTableValues
										 },
										 layout: 'noBorders'
					   			 },
					   			{
					   				 style: 'tableStyle',
					   				 table: {
												headerRows: 1,
												widths: [50,110],
												body: stationInfoTableValues
										 },
										 layout: 'noBorders'
					   			 }*/
					   		   /* {text: (angular.isDefined(loadTag.loadTagId) ? loadTag.loadTagId : 0), style: 'headerStyle'},   
						   		{   columnGap: 10,
					   				 columns: [
					   					{
							   				 style: 'tableStyle',
							   				 table: {
														headerRows: 1,
														widths: [35,35],
														body: loadInfoTableValues
												 },
												 layout: 'noBorders'
							   			 },
							   			{image: loadTagIdDataURL, style: 'contentStyle'}
				   				     ]
				   	    	   	},
				   	    	    {
					   				 style: 'tableStyle',
					   				 table: {
												headerRows: 1,
												widths: [50,110],
												body: partInfoTableValues
										 },
										 layout: 'noBorders'
					   			 }
					   		],
					   
						   styles: {
					   		     headerStyle: {
					   		       fontSize: 40,
					   		       bold: true,
					   		       alignment: 'center'
					   		     },
					   		     contentStyle: {
					   		        fontSize: 15
					   		     },
					   		     tableStyle: {
					   					margin: [0, 0, 0, 0]
					   				},
					   			 tableHeaderStyle: {
					   					bold: true,
					   					fontSize: 7,
					   					color: 'black'
					   			 },
					   			tableContentStyle: {
				   					bold: false,
				   					fontSize: 10,
				   					color: 'black'
				   			 }
					   		}
					   	};
					   	pdfMake.createPdf(docDefinition2).print();
				   });
				 
			    });
		    }*/
	  		
	  		
	  		  
	  		 /*$rootScope.openRollTag = function (rollId) {
				rollServiceAjax.getRollById(rollId).then(function(data){
				   $scope.roll = data;
				   var canvasRollId = document.createElement("canvas");
				   JsBarcode(canvasRollId, angular.isDefined($scope.roll.rollId) ? $scope.roll.rollId : 0, {
					   format: "CODE128",
					   width: 2,
					   height: 40,
					   displayValue: false});
				   var rollIdDataURL = canvasRollId.toDataURL("image/png");
				   
				   var canvasRollLength = document.createElement("canvas");
				   JsBarcode(canvasRollLength, angular.isDefined($scope.roll.length) ? $scope.roll.length : 0, {
					   format: "CODE128",
					   width:1,
					   height:50,
					   displayValue: false});
				   var rollLengthDataURL = canvasRollLength.toDataURL("image/png");
				   
				   var canvasPaperType = document.createElement("canvas");
				   JsBarcode(canvasPaperType, $scope.roll.paperType.name, {
					   format: "CODE128",
					   width:1,
					   height:50,
					   displayValue: false});
				   var paperTypeDataURL = canvasPaperType.toDataURL("image/png");
				   
				   var rollInfoTableValues = [];
				   var rollInfoBodyRow = [];
				   rollInfoBodyRow.push({ text: 'Length', style: 'tableHeaderStyle' });
				   rollInfoBodyRow.push({ text: (angular.isDefined($scope.roll.length) ? $scope.roll.length : 0) + ' (' + (angular.isDefined($scope.roll.rollType.name) ? $scope.roll.rollType.name : '') + ')', style: 'tableContentStyle'});
				   rollInfoTableValues.push(rollInfoBodyRow);
				   rollInfoBodyRow = [];
				   rollInfoBodyRow.push({ text: 'Paper Type', style: 'tableHeaderStyle' });
				   rollInfoBodyRow.push({ text: $scope.roll.paperType.name, style: 'tableContentStyle'});
				   rollInfoTableValues.push(rollInfoBodyRow);
				   
				   var jobsTableValues = [];
				   var jobsBodyRow = [];
				   jobsBodyRow.push({ text: 'ORDER', style: 'tableHeaderStyle' });
				   jobsBodyRow.push({ text: 'ISBN', style: 'tableHeaderStyle' });
				   jobsBodyRow.push({ text: 'QTY', style: 'tableHeaderStyle'});
				   jobsTableValues.push(jobsBodyRow);
				   for(var i = 0; i < $scope.roll.jobs.length; i++){
					   jobsBodyRow = [];
					   if(angular.isDefined($scope.roll.jobs[i].order)){
						   jobsBodyRow.push({ text: $scope.roll.jobs[i].order.orderId, style: 'tableContentStyle' });
					   }else{
						   jobsBodyRow.push({ text: 'n/a', style: 'tableContentStyle' });
					   }
					   if(angular.isDefined($scope.roll.jobs[i].part) && angular.isDefined($scope.roll.jobs[i].part.isbn)){
						   jobsBodyRow.push({ text: $scope.roll.jobs[i].part.isbn, style: 'tableContentStyle' });
					   }else{
						   jobsBodyRow.push({ text: 'n/a', style: 'tableContentStyle' });
					   }
					   if(angular.isDefined($scope.roll.jobs[i].quantityProduced) && $scope.roll.jobs[i].quantityProduced > 0){
						   jobsBodyRow.push({ text: $scope.roll.jobs[i].quantityProduced + ' / ' + $scope.roll.jobs[i].quantityNeeded, style: 'tableContentStyle' });
					   }else{
						   jobsBodyRow.push({ text:  '0 / ' + $scope.roll.jobs[i].quantityNeeded, style: 'tableContentStyle' });
					   }
					   if(angular.isDefined($scope.roll.jobs[i].quantityNeeded)){
						   jobsBodyRow.push({ text: $scope.roll.jobs[i].quantityNeeded, style: 'tableContentStyle' });
					   }else{
						   jobsBodyRow.push({ text:  '', style: 'tableContentStyle' });
					   }
					   jobsTableValues.push(jobsBodyRow);
				   }
				   
			      var docDefinition = { 
			    		  pageSize: 'A5',
			    		  pageOrientation: 'landscape',
			    		  pageMargins: [ 30, 30, 10, 10 ],
			   	       content: [
			   	             // $scope.roll.rollType.name
			   			 {text: 'ROLL TAG # ' + $scope.roll.rollId + '\n', style: 'headerStyle'},
			   			 {image: rollIdDataURL, style: 'headerStyle'},
			   			 {   columnGap: 30,
			   				 columns: [
			   				           {width: 'auto',
			   				        	   stack: [
			   				        	       {text: 'Roll#: ' + (angular.isDefined($scope.roll.rollId) ? $scope.roll.rollId : 0), style: 'contentStyle'},
			   				        	       {image: rollIdDataURL}
			   				           		]
			   				           },
			   				           {width: 'auto',
			   				        	   stack: [
				    				        	   {text: 'Length: ' + (angular.isDefined($scope.roll.length) ? $scope.roll.length : 0), style: 'contentStyle'},
				    				        	   {image: rollLengthDataURL}
			   				           		]
			   				           },
			   				           {width: 'auto',
			  			   				 stack: [
			  					        	       {text: 'Paper Type: ' + $scope.roll.paperType.name, style: 'contentStyle'},
			  					        	       {image: paperTypeDataURL}
			  					        	 ]
			  			   			    }
			   				 ]
			   			 },
			   			 {text: '\n'},
			   			 {
			   				 style: 'tableStyle',
			   				 table: {
										headerRows: 1,
										widths: [130,380],
										body: rollInfoTableValues
								 }
			   			 },
			   			 {
			   				 style: 'tableStyle',
			   				 table: {
										headerRows: 1,
										widths: [150, 200, 150],
										body: jobsTableValues
								 }
			   			 }
			   		],
			   		styles: {
			   		     headerStyle: {
			   		       fontSize: 35,
			   		       bold: true,
			   		       alignment: 'center'
			   		     },
			   		     contentStyle: {
			   		        fontSize: 10
			   		     },
			   		     tableStyle: {
			   					margin: [0, 5, 0, 5]
			   				},
			   			 tableHeaderStyle: {
			   					bold: true,
			   					fontSize: 18,
			   					color: 'black'
			   			 },
			   			tableContentStyle: {
		   					bold: false,
		   					fontSize: 18,
		   					color: 'black'
		   			 }
			   		}
			   	};
			   	 pdfMake.createPdf(docDefinition).open();
			  });
			};
			
		    $rootScope.openLoadTag = function (loadTag) {
			    jobServiceAjax.getJobById(loadTag.jobId).then(function(data){
				   $scope.job = data;
				   
				   jobServiceAjax.findNextStationJob(loadTag.jobId).then(function(data2){
					   $scope.nextJob = data2;
					   
					   var canvasLoadTagId = document.createElement("canvas");
					   JsBarcode(canvasLoadTagId, angular.isDefined(loadTag.loadTagId) ? loadTag.loadTagId : 0, {
						   format: "CODE128",
						   width: 2,
						   height: 40,
						   displayValue: false});
					   var loadTagIdDataURL = canvasLoadTagId.toDataURL("image/png");
					   
					   var canvasJobId = document.createElement("canvas");
					   JsBarcode(canvasJobId, angular.isDefined($scope.job.jobId) ? $scope.job.jobId : 0, {
						   format: "CODE128",
						   width:1,
						   height:50,
						   displayValue: false});
					   var jobIdDataURL = canvasJobId.toDataURL("image/png");
					   
					   var canvasOrderId = document.createElement("canvas");
					   JsBarcode(canvasOrderId, angular.isDefined($scope.job.order.orderId) ? $scope.job.order.orderId : 0, {
						   format: "CODE128",
						   width:1,
						   height:50,
						   displayValue: false});
					   var orderIdDataURL = canvasOrderId.toDataURL("image/png");
					   
					   var loadInfoTableValues = [];
					   var loadInfoBodyRow = [];
					   loadInfoBodyRow.push({ text: 'JOB ID', style: 'tableHeaderStyle' });
					   loadInfoBodyRow.push({ text: angular.isDefined($scope.job.jobId) ? $scope.job.jobId : 0, style: 'tableContentStyle'});
					   loadInfoBodyRow.push({ text: 'ORDER ID', style: 'tableHeaderStyle' });
					   loadInfoBodyRow.push({ text: angular.isDefined($scope.job.order.orderId) ? $scope.job.order.orderId : 0, style: 'tableContentStyle'});
					   loadInfoTableValues.push(loadInfoBodyRow);
					   
					   var partInfoTableValues = [];
					   var partInfoBodyRow = [];
					   partInfoBodyRow.push({ text: 'TITLE', style: 'tableHeaderStyle' });
					   partInfoBodyRow.push({ text: angular.isDefined($scope.job.part.title) ? $scope.job.part.title : '', style: 'tableContentStyle'});
					   partInfoBodyRow.push({ text: 'QTY', style: 'tableHeaderStyle' });
					   partInfoBodyRow.push({ text: loadTag.quantity + '/' + $scope.job.quantityNeeded, style: 'tableContentStyle'});
					   partInfoTableValues.push(partInfoBodyRow);
					   partInfoBodyRow = [];
					   partInfoBodyRow.push({ text: 'ISBN', style: 'tableHeaderStyle' });
					   partInfoBodyRow.push({ text: angular.isDefined($scope.job.part.isbn) ? $scope.job.part.isbn : '', style: 'tableContentStyle'});
					   partInfoBodyRow.push({ text: 'Due Date', style: 'tableHeaderStyle' });
					   partInfoBodyRow.push({ text: angular.isDefined($scope.job.order.dueDate) ? $filter('date')($scope.job.order.dueDate,'MM/dd/yyyy') : '', style: 'tableContentStyle'});
					   partInfoTableValues.push(partInfoBodyRow);

					   var stationInfoTableValues = [];
					   var stationInfoBodyRow = [];
					   stationInfoBodyRow.push({ text: 'Current Station', style: 'tableHeaderStyle' });
					   stationInfoBodyRow.push({ text: angular.isDefined($scope.job.stationId) ? $scope.job.stationId : '', style: 'tableContentStyle'});
					   stationInfoTableValues.push(stationInfoBodyRow);
					   stationInfoBodyRow = [];
					   stationInfoBodyRow.push({ text: 'Next Station', style: 'tableHeaderStyle' });
					   stationInfoBodyRow.push({ text: angular.isDefined($scope.nextJob.stationId) ? $scope.nextJob.stationId : '', style: 'tableContentStyle'});
					   stationInfoTableValues.push(stationInfoBodyRow);
					   stationInfoBodyRow = [];
					   stationInfoBodyRow.push({ text: 'Cart Number', style: 'tableHeaderStyle' });
					   stationInfoBodyRow.push({ text: angular.isDefined(loadTag.cartNum) ? loadTag.cartNum : '', style: 'tableContentStyle'});
					   stationInfoTableValues.push(stationInfoBodyRow);
					   
					   var docDefinition2 = { 
					    		  pageSize: 'A5',
					    		  pageOrientation: 'landscape',
					    		  pageMargins: [ 30, 30, 10, 10 ],
					   	   content: [
					   			 {text: 'LOAD TAG # ' + (angular.isDefined(loadTag.loadTagId) ? loadTag.loadTagId : 0) + '\n', style: 'headerStyle'},
					   			 {   columnGap: 30,
					   				 columns: [
					   				           {width: '33%',
					   				        	   stack: [
					   				        	       {text: 'LoadTag#: ' + (angular.isDefined(loadTag.loadTagId) ? loadTag.loadTagId : 0), style: 'contentStyle'},
					   				        	       {image: loadTagIdDataURL}
					   				           		]
					   				           },
					   				           {width: '33%',
					   				        	   stack: [
						    				        	   {text: 'Job#: ' + (angular.isDefined($scope.job.jobId) ? $scope.job.jobId : 0), style: 'contentStyle'},
						    				        	   {image: jobIdDataURL}
					   				           		]
					   				           },
					   				           {width: '33%',
					  			   				 stack: [
					  					        	       {text: 'Order#: ' + (angular.isDefined($scope.job.order.orderId) ? $scope.job.order.orderId : 0), style: 'contentStyle'},
					  					        	       {image: orderIdDataURL}
					  					        	 ]
					  			   			    },
					   				 ]
					   			 },
					   			{image: loadTagIdDataURL, style: 'headerStyle'},
					   			{text: '\n'},
					   			 {   columnGap: 1,
					   				 columns: [
					   				           {   width: '20%',
					   				        	   text: 'Qty: ' + loadTag.quantity + '/' + $scope.job.quantityNeeded, 
					   				        	   style: 'contentStyle'
					   				           },
					   				           {   width: '20%',
					   				        	   text: 'Due: ' + $filter('date')($scope.job.order.dueDate,'MM/dd/yyyy'),
					   				        	   style: 'contentStyle'
					   				           },
					   				           {   width: '60%',
					  			   				   text: 'Title: ' + $scope.job.part.title, 
					  			   				   style: 'contentStyle'
					  			   			   },
					   				  ]
					   			  },
					   			  {text: '\n'},{text: '\n'},
					   			  {   columnGap: 1,
						   				 columns: [
						   				           {   width: '35%',
						   				        	   text: 'Current Station: ' + $scope.job.stationId, 
						   				        	   style: 'contentStyle'
						   				           },
						   				           {   width: '35%',
						   				        	   text: 'Next Station: ' + $scope.nextJob.stationId,
						   				        	   style: 'contentStyle'
						   				           },
						   				           {   width: '30%',
						  			   				   text: 'Cart#: ' + loadTag.cartNum, 
						  			   				   style: 'contentStyle'
						  			   			   },
						   				  ]
						   		   }
					   			{
					   				 style: 'tableStyle',
					   				 table: {
												headerRows: 1,
												widths: [100,150,100,150],
												body: loadInfoTableValues
										 }
					   			 },
					   			{text: '\n'},
					   			{
					   				 style: 'tableStyle',
					   				 table: {
												headerRows: 1,
												widths: [70,230,90,110],
												body: partInfoTableValues
										 }
					   			 },
					   			{text: '\n'},
					   			{
					   				 style: 'tableStyle',
					   				 table: {
												headerRows: 1,
												widths: [150,350],
												body: stationInfoTableValues
										 }
					   			 }
					   		],
					   
						   styles: {
					   		     headerStyle: {
					   		       fontSize: 35,
					   		       bold: true,
					   		       alignment: 'center'
					   		     },
					   		     contentStyle: {
					   		        fontSize: 10
					   		     },
					   		     tableStyle: {
					   					margin: [0, 5, 0, 5]
					   				},
					   			 tableHeaderStyle: {
					   					bold: true,
					   					fontSize: 18,
					   					color: 'black'
					   			 },
					   			tableContentStyle: {
				   					bold: false,
				   					fontSize: 18,
				   					color: 'black'
				   			 }
					   		}
					   	};
					   	pdfMake.createPdf(docDefinition2).open();
				   });
				 
			    });
		    }
*/	    }
  ]);
