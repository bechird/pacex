	'use strict';

/**
 * @ngdoc function
 * @name capApp.controller:ProductionDashboardCtrl
 * @description
 * # ProductionDashboardCtrl
 * Controller of the capApp
 */
angular.module('capApp')
  .controller('ProductionDashboardCtrl', ['DTOptionsBuilder','DTColumnBuilder', 'DTColumnDefBuilder', '$rootScope', '$scope', '$q', '$routeParams', 'logServiceAjax', 'stationServiceAjax', 
	  		  'rollServiceAjax', 'machineServiceAjax', 'jobServiceAjax','batchServiceAjax', 'orderServiceAjax', 'partServiceAjax', 'lookupServiceAjax', 'loadTagServiceAjax', '$uibModal', '$confirm','$filter',
	  		  '$window', 'toasty','$translatePartialLoader', 'SweetAlert', '$translate', '$localStorage', 'notificationService','SSE_CONSTANTS', '$log', '$controller',
   function (DTOptionsBuilder, DTColumnBuilder, DTColumnDefBuilder, $rootScope, $scope, $q, $routeParams, logServiceAjax, stationServiceAjax, rollServiceAjax, machineServiceAjax, jobServiceAjax, batchServiceAjax,
		   orderServiceAjax, partServiceAjax, lookupServiceAjax, loadTagServiceAjax, $uibModal, $confirm, $filter, $window,toasty ,$translatePartialLoader, 
		   SweetAlert, $translate, $localStorage, notificationService, SSE_CONSTANTS, $log, $controller) {
     
	  $controller('ListOrderParentInstanceCtrl', {$scope: $scope}); 
	  
	  $translatePartialLoader.addPart('productionDashboard');
	  $translate.refresh();
	  
	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;

	  var vm = this;
	  var monthNames = ["Jan ", $translate.instant('productionDashboard_js.FEB'), "Mar ",  $translate.instant('productionDashboard_js.APR'),  $translate.instant('productionDashboard_js.MAY'),  $translate.instant('productionDashboard_js.JUN'), $translate.instant('productionDashboard_js.JUL'), $translate.instant('productionDashboard_js.AUG'), "Sep ", "Oct ", "Nov ", "Dec "];
	  $scope.successMsgArray = []; 
	  $scope.errorMsgArray = [];
	  
	  $scope.fromProd = true;
	  
	  $scope.successMsgDown = false;
	  $scope.errorMsgDown = false;
	  var alertMessage = "";
	  vm.dtInstance = {};
	  vm.dtInstanceLog = {};
	  vm.dtInstanceForJobs = {};
	  vm.dtInstanceForBatches = {};
	  
	  $scope.stationId = $routeParams.stationId;
	  vm.selectedMachineType = 'ALL';
	  vm.stationId = $scope.stationId;
	  
	  var defaultStation = {
			    "name":"",
			    "stationCategoryId":"",
			    "parentStationId":"",
			    "inputType":"",
			    "productionOrdering":"",
			    "description":""
	  };
	  $scope.station = jQuery.extend({}, defaultStation);
	  
		//sse management
	    notificationService.subscribeToApp();
		$scope.$on('$destroy', function() {
			$log.log("leaving ProductionDashboardCtrl controller, unsubscribe from sse");
			notificationService.getPubSub().unsubscribe(SSE_CONSTANTS.machinesEventsTopic);
			notificationService.unsubscribeFromMachines();
			notificationService.getPubSub().unsubscribe(SSE_CONSTANTS.appEventsTopic);
			notificationService.unsubscribeFromApp();
		});
		//*****************************************************************************
	  
	  $scope.listOfPrinters = [];
	  $scope.runningJobId = null;
	  $scope.printerSse = [];
	  $scope.pushEpacModeInfoSse = function(){
  			notificationService.subscribeToMachines($scope.station);
  			notificationService.getPubSub().subscribe(SSE_CONSTANTS.machinesEventsTopic, function (event) {
				var data = JSON.parse(event.data);						
				for(var m = 0; m < $scope.nbMachineCount; m++){
					if($scope.station.machines[m].ipAddress != null && $scope.station.machines[m].ipAddress != '' && event.target.url.indexOf($scope.station.machines[m].ipAddress) !== -1){
						if(data.type == 'PRINTER'){
							var printerStatus = data.object.recorderStatus;
							$scope.station.machines[m].isEpacModePrintingActive = printerStatus != 'OFFLINE';
							$scope.station.machines[m].epacModePrinterStatus = printerStatus;
							if(printerStatus == 'OFFLINE' &&  ($scope.station.machines[m].status.id == 'RUNNING' || $scope.station.machines[m].status.id == 'ON')){
								$scope.station.machines[m].isEpacModePrinterStatusChanged = true;
								$scope.station.machines[m].epacModePrinterStatus = 'OFFLINE';
							}else if(printerStatus == 'READY'){
								$scope.station.machines[m].isEpacModePrinterStatusChanged = false;
								$scope.station.machines[m].epacModePrinterStatus = 'READY';
							}else if(printerStatus == 'ERROR' && ($scope.station.machines[m].status.id == 'RUNNING' || $scope.station.machines[m].status.id == 'ON')){
								$scope.station.machines[m].isEpacModePrinterStatusChanged = true;
								$scope.station.machines[m].epacModePrinterStatus = 'ERROR';
							}else if(printerStatus == 'RUNNING' && $scope.station.machines[m].status.id != 'RUNNING'){
								$scope.station.machines[m].isEpacModePrinterStatusChanged = true;
								$scope.station.machines[m].epacModePrinterStatus = 'RUNNING';
							}
						}else if(data.type == 'PRINTING'){
							if($scope.station.machines[m].rollOnProd != null){
								// in printing event; object holds the list of ongoing jobs
								var printingJobs = data.object;
								for(var j = 0; j < $scope.station.machines[m].rollOnProd.jobs.length; j++){
									var job = $scope.station.machines[m].rollOnProd.jobs[j];
									for(var k = 0; k < printingJobs.length; k++){
										var printingJob = printingJobs[k];
										if(job.rasterNames != null){
											for(var l = 0;  l < job.rasterNames.length; l++){
												if((job.jobId + job.rasterNames[l]) == printingJob.job.id){
													$scope.station.machines[m].rollOnProd.jobs[j].quantityProduced = Math.floor(printingJob.printedSheetCount/printingJob.job.numSheets) ;
													//jobServiceAjax.updateJob(job).then(function(){});
													break;
												}
											}
										}
									}
								}
							}
						}
					}
					//$scope.$apply();
				}
				$scope.$apply();
			});
	  };

	  //$scope.pushCopyFilesInfoSse = function () {
	  notificationService.getPubSub().subscribe(SSE_CONSTANTS.appEventsTopic, function (event) {
			var eventData = JSON.parse(event.data);
			if(eventData.target == 'MachineCopyFiles' && eventData.error == false && $scope.station.inputType != 'Batch'){
				for(var i = 0; i < $scope.nbMachineCount; i++){
	               if(eventData.object != null){
	            	   for(var j = 0; j < $scope.station.machines[i].assignedRolls.length; j++){
	            		   if($scope.station.machines[i].rollOnProd != null && $scope.station.machines[i].rollOnProd.rollId == eventData.object.rollId){
	            			   $scope.station.machines[i].rollOnProd.copyStatus = eventData.object.copyStatus;
							   for(var y = 0; y < $scope.station.machines[i].rollOnProd.jobs.length ; y++){
								   if(eventData.object.jobs[y].jobName != null){
									   $scope.station.machines[i].rollOnProd.jobs[y].jobName = eventData.object.jobs[y].jobName;
									   $scope.station.machines[i].rollOnProd.alljobs[y].jobName = eventData.object.jobs[y].jobName;
								   }
							   }
							  // $scope.$apply();
						   }
		                  if($scope.station.machines[i].assignedRolls[j].rollId == eventData.object.rollId){
		                	  $scope.station.machines[i].assignedRolls[j].copyStatus = eventData.object.copyStatus;
							   for(var y = 0; y < $scope.station.machines[i].assignedRolls[j].jobs.length ; y++){
								   if(eventData.object.jobs[y].jobName != null){
									   $scope.station.machines[i].assignedRolls[j].jobs[y].jobName = eventData.object.jobs[y].jobName;
									   $scope.station.machines[i].assignedRolls[j].alljobs[y].jobName = eventData.object.jobs[y].jobName;
								   }
							   }
							   //$scope.$apply();
						   }
	            	   }
	               }
				}
			}
			if(eventData.target == 'MachineCopyFiles' && eventData.error == true && $scope.station.inputType != 'Batch'){
				for(var i = 0; i < $scope.nbMachineCount; i++){
	               if(eventData.object != null){ 
	            	   for(var j = 0; j < $scope.station.machines[i].assignedRolls.length; j++){  
	            	   	  if($scope.station.machines[i].rollOnProd != null && $scope.station.machines[i].rollOnProd.rollId == eventData.object.rollId){
	            	   		$scope.station.machines[i].rollOnProd.copyStatus = eventData.object.copyStatus;
							    $scope.errorMsgArray[i] = true;
							    $scope.alertMsg = eventData.message;
							    //$scope.$apply();
						   }
	            	   	   if($scope.station.machines[i].assignedRolls[j].rollId == eventData.object.rollId){
	            	   		   $scope.station.machines[i].rollOnProd.copyStatus = eventData.object.copyStatus;
							    //$scope.$apply();
						   }else if($scope.station.machines[i].assignedRolls[j].rollId == eventData.object){
							   $scope.station.machines[i].assignedRolls[j].copyStatus = 'ERROR';
							    //$scope.$apply();
							    return;
						   }else if($scope.station.machines[i].rollOnProd != null && $scope.station.machines[i].rollOnProd.rollId == eventData.object){
							   $scope.station.machines[i].rollOnProd.copyStatus = 'ERROR';
							    $scope.errorMsgArray[i] = true;
							    $scope.alertMsg = eventData.message;
							   // $scope.$apply();
							    return;
						   }else if(eventData.object == machine.machineId){
							   $scope.station.machines[i].rollOnProd.copyStatus = 'ERROR';
							    $scope.errorMsgArray[i] = true;
							    $scope.alertMsg = eventData.message;
							   // $scope.$apply();
							    return;
						   }
	               	   }
	               }
				}
			}
			if(eventData.target == 'MachineCopyFiles' && eventData.error == false && $scope.station.inputType == 'Batch'){
				for(var i = 0; i < $scope.nbMachineCount; i++){
	               if(eventData.object != null){
					   if(($scope.station.machines[i].coverSectionOnProd != null && $scope.station.machines[i].coverSectionOnProd.coverSectionId == eventData.object.coverSectionId)){
						   if($scope.station.machines[i].coverSectionOnProd != null){
							   $scope.station.machines[i].coverSectionOnProd.copyStatus = eventData.object.copyStatus;
							   //$scope.$apply();
							   console.log("$scope.station.machines[i].coverSectionOnProd.copyStatus  ="+$scope.station.machines[i].coverSectionOnProd.copyStatus);
							   console.log("eventData.object.copyStatus  ="+eventData.object.copyStatus);   
							   break;
						   }
					   }
	               }
				}
			}
			if(eventData.target == 'MachineCopyFiles' && eventData.error == true && $scope.station.inputType == 'Batch'){
				for(var i = 0; i < $scope.nbMachineCount; i++){
	               if(eventData.object != null){
					if(($scope.station.machines[i].coverSectionOnProd != null && $scope.station.machines[i].coverSectionOnProd.coverSectionId == eventData.object.coverSectionId)){
						if($scope.station.machines[i].coverSectionOnProd != null){
							$scope.station.machines[i].coverSectionOnProd.copyStatus = eventData.object.copyStatus;
							$scope.errorMsgArray[i] = true;
							$scope.alertMsg = eventData.message;
							//$scope.$apply();
							console.log("$scope.station.machines[i].coverSectionOnProd.copyStatus  ="+$scope.station.machines[i].coverSectionOnProd.copyStatus);
							console.log("eventData.object.copyStatus  ="+eventData.object.copyStatus);  
						    break;
						   }
					   }
	               }
				}
			}
			$scope.$apply();
		});


		//};
	 // $scope.pushCopyFilesInfoSse();
	  

	  
	  /*
	  $scope.pushEpacModeInfoSse = function () {
			//console.log("yes info Roll");
			$scope.EpacModeInfoSse.onmessage = function (event) {
				var tmpData = JSON.parse(event.data);
				for(var i = 0; i < $scope.nbMachineCount; i++){
					if($scope.station.machines[i].fullIpAddress == (tmpData.job.printer.ipaddress + ':' + tmpData.job.printer.port)){
						$scope.station.machines[i].isEpacModePrintingActive = tmpData.job.result == 200 ? true : false;
						break;
					}
				}
				$scope.$apply();
			}
	  };
	  $scope.pushPrinterInfoSse = function () {
			//console.log("yes info Roll");
			$scope.PrinterInfoSse.onmessage = function (event) {
				var tmpData = JSON.parse(event.data);
				for(var i = 0; i < $scope.nbMachineCount; i++){
					if($scope.station.machines[i].fullIpAddress == (tmpData.job.printer.ipaddress + ':' + tmpData.job.printer.port)){
						$scope.station.machines[i].epacModePrinterStatus = tmpData.job.printer.status;
						$scope.station.machines[i].isEpacModePrinterStatusChanged = false;
						if(tmpData.job.printer.status == 'OFFLINE' && 
								($scope.station.machines[i].status.id == 'RUNNING' || $scope.station.machines[i].status.id == 'ON')){
							$scope.station.machines[i].isEpacModePrinterStatusChanged = true;
							$scope.station.machines[i].epacModePrinterStatus = 'OFFLINE';
						}
						if(tmpData.job.printer.status == 'READY' && $scope.station.machines[i].status.id != 'ON'){
							$scope.station.machines[i].isEpacModePrinterStatusChanged = true;
							$scope.station.machines[i].epacModePrinterStatus = 'READY';
						}
						if(tmpData.job.printer.status == 'RUNNING' && $scope.station.machines[i].status.id != 'RUNNING'){
							$scope.station.machines[i].isEpacModePrinterStatusChanged = true;
							$scope.station.machines[i].epacModePrinterStatus = 'RUNNING';
						}
						break;
					}
				}
				$scope.$apply();
			}
	  };
	  $scope.pushOngoingPrintingSse = function () {
			//console.log("yes info Roll");
			$scope.OngoingPrintingSse.onmessage = function (event) {
				var tmpData = JSON.parse(event.data);
				var updateJob = 0;
				if(tmpData.job.jobs != undefined){
					for(var i = 0; i < $scope.nbMachineCount; i++){
						if($scope.station.machines[i].fullIpAddress == (tmpData.job.printer.ipaddress + ':' + tmpData.job.printer.port) &&
								$scope.station.machines[i].rollOnProd != null){
							for(var j = 0; j < $scope.station.machines[i].rollOnProd.jobs.length; j++){
								for(var k = 0; k < tmpData.job.jobs.length; k++){
									for(var l = 0;  l < $scope.station.machines[i].rollOnProd.jobs[j].part.dataSupportsOnProd.length; l++){
										if($scope.station.machines[i].rollOnProd.jobs[j].part.dataSupportsOnProd[l].locations[0].fileName == tmpData.job.jobs[k][0]){
											$scope.runningJobId = $scope.station.machines[i].rollOnProd.jobs[j].jobId;
											if($scope.station.machines[i].rollOnProd.jobs[j].quantityNeeded - $scope.station.machines[i].rollOnProd.jobs[j].quantityProduced > tmpData.job.jobs[k][1]){
												updateJob = 1;
											}
											$scope.station.machines[i].rollOnProd.jobs[j].quantityProduced = $scope.station.machines[i].rollOnProd.jobs[j].quantityNeeded - tmpData.job.jobs[k][1];
											if(updateJob == 1){
												jobServiceAjax.updateJob($scope.station.machines[i].rollOnProd.jobs[j]).then(function(){
												});
											}
											break;
										}
									}
								}
							}
							break;
						}
					}
				}
				$scope.$apply();
			}
	  };
	  */
		
		
	  $scope.loadStation = function(stationId){
    	  stationServiceAjax.loadStationById(stationId).then(function(data){
	            $scope.station = data;
	      	    $scope.nbTabs = [];
	      	    $scope.machinesLoop = [];
	      	    $scope.prevJobData = [];

	      	    /*$scope.nbTabCount = $scope.station != null ? 
	      			      ($scope.station.machines.length % $scope.screenType == 0 ? ($scope.station.machines.length / $scope.screenType) : 
	      				  (($scope.station.machines.length + ($scope.screenType - 1)) / $scope.screenType)) : 0;*/
	      	    $scope.nbTabCount = $scope.station != null ? $window.Math.ceil($scope.station.machines.length / $rootScope.screenType): 0;
	      	    $scope.nbMachineCount = $scope.station != null ? $scope.station.machines.length : 0;
	      	    for(var i = 0; i < $scope.nbMachineCount; i++){
	      	    	$scope.errorMsgArray[i] = false;
	      	    	if(i % $rootScope.screenType  == 0){
	            		$scope.nbTabs.push(i);
	            	}
	      	    	$scope.machinesLoop.push(i);
	      	    	/*if($scope.successMsgArray.length < $scope.nbMachineCount){
	      	    		$scope.successMsgArray.push(false);
		      	    	$scope.errorMsgArray.push(false);
	      	    	}*/
	      	    	if(stationId != 'PRESS' && stationId != 'PLOWFOLDER' && stationId != 'COVERPRESS'){
	      	    		$scope.prevJobData.push({});
	      	    		if(angular.isDefined($scope.station.machines[i].jobOnProd) && $scope.station.machines[i].jobOnProd != null){
	      	    			jobServiceAjax.findPrevJobData($scope.station.machines[i].jobOnProd.jobId).then(function(dataRoot){
	      	    				for(var j = 0; j < $scope.nbMachineCount; j++){
	      	    					if($scope.station.machines[j].machineId == dataRoot.machineId){
	      	    						//update the binder qty needed in case changed when calling findPrevJobData
	    	      	    				$scope.station.machines[j].jobOnProd.quantityNeeded = dataRoot.currentJobQtyNeeded;
	      	    						$scope.prevJobData[j] = dataRoot;
	      	    					}
	      	    				}
		      	    		});
	      	    		}
		      	    }
	      	    	
	      	    }
	      	    
	      	    /*if(stationId == 'PRESS'){
	      	    	for(var i = 0; i < $scope.nbMachineCount; i++){
	      	    		var pointer = []; pointer.push($scope.station.machines[i].machineId); pointer.push(null);
	      	    		$scope.listOfPrinters.push(pointer);
	      	    		if($scope.station.machines[i].fullIpAddress.length > 0){
	      	    			printingServiceAjax.isPrinterActive($scope.station.machines[i].machineId, $scope.station.machines[i].fullIpAddress).then(function(data){
	      	    				for(var j = 0; j < $scope.nbMachineCount; j++){
	      	    					if($scope.listOfPrinters[j][0] == data[0]){
	      	    						$scope.listOfPrinters[j][1] = data[1];
	      	    						break;
	      	    					}
	      	    				}
	      	    			});
	      	    		}else{// the printer has no address so it works with Normal Mode
	      	    			
	      	    		}
	      	    	}
	      	    }*/
	      	    
	      	    if(stationId == 'PRESS'){
	      	    	//$scope.pushPrintingInfoSse();
	      	    	$scope.pushEpacModeInfoSse();
	      	    }
	      	    
	      	    
	      	    /*if(stationId == 'PLOWFOLDER' || stationId == 'COVERPRESS'){
		      	    $scope.tableRowExpanded = [];
				    $scope.tableRowIndexCurrExpanded = [];
				    $scope.tableRowIndexPrevExpanded = [];
				    $scope.jobIdExpanded = [];
				    $scope.tagDataCollapse = [];
				    
				    for(i in $scope.machinesLoop){
				    	$scope.tableRowExpanded.push(false);
				    	$scope.tableRowIndexCurrExpanded.push("");
				    	$scope.tableRowIndexPrevExpanded.push("");
				    	$scope.jobIdExpanded.push("");
				    	var jobsCount = 0;
				    	if(stationId == 'PLOWFOLDER'){
				    		jobsCount = $scope.station.machines[i].rollOnProd != null ? $scope.station.machines[i].rollOnProd.jobs.length : 0;
				    	}else{
				    		jobsCount = $scope.station.machines[i].jobs.length;
				    	}
				    	$scope.tagDataCollapse[i] = new Array(jobsCount);
				    	for(var j = 0; j < jobsCount; j++){
				    		var ji = 0;
				    		if($scope.station.machines[i].currentJob != null){
				    			ji = $scope.station.machines[i].currentJob.jobId;
				    		}
				    		if(stationId == 'PLOWFOLDER'){
				    			if($scope.station.machines[i].rollOnProd != null && ji == $scope.station.machines[i].rollOnProd.jobs[j].jobId){
					    			$scope.tagDataCollapse[i][j] = false;
					    			//$('#expendCollapseTr'+ji).addClass('running');
					    		}else{
					    			$scope.tagDataCollapse[i][j] = true;
					    		}
					    	}else{
					    		if(ji == $scope.station.machines[i].jobs[j].jobId){
					    			$scope.tagDataCollapse[i][j] = false;
					    			//$('#expendCollapseTr'+ji).addClass('running');
					    		}else{
					    			$scope.tagDataCollapse[i][j] = true;
					    		}
					    	}
				    	}
				    }
	      	    }*/
	         });
	    };
	    
	  $scope.loadStation($scope.stationId);
	  
	  $scope.collapseRows = true;
	  $scope.dataTables = {};
	  
	  if($translate.use() == 'fr')
	    {
		  $scope.dataTables = {
	  		          "sProcessing":     "Traitement en cours...",
	  				    "sSearch":         "Rechercher&nbsp;:",
	  				    "sLengthMenu":     "Afficher _MENU_ &eacute;l&eacute;ments",
	  				    "sInfo":           "Affichage de l'&eacute;l&eacute;ment _START_ &agrave; _END_ sur _TOTAL_ &eacute;l&eacute;ments",
	  				    "sInfoEmpty":      "Affichage de l'&eacute;l&eacute;ment 0 &agrave; 0 sur 0 &eacute;l&eacute;ment",
	  				    "sInfoFiltered":   "(filtr&eacute; de _MAX_ &eacute;l&eacute;ments au total)",
	  				    "sInfoPostFix":    "",
	  				    "sLoadingRecords": "Chargement en cours...",
	  				    "sZeroRecords":    "Aucun &eacute;l&eacute;ment &agrave; afficher",
	  				    "sEmptyTable":     "Aucune donn&eacute;e disponible dans le tableau",
	  				    "oPaginate": {
	  				        "sFirst":      "|<",
	  				        "sPrevious":   "<",
	  				        "sNext":       ">",
	  				        "sLast":       ">|"
	  				    },
	  				    "oAria": {
	  				        "sSortAscending":  ": activer pour trier la colonne par ordre croissant",
	  				        "sSortDescending": ": activer pour trier la colonne par ordre d&eacute;croissant"
	  				    }

	  		}
	   }
	  else if($translate.use() == 'es') 
      	{
		  $scope.dataTables = {
			 "sProcessing":     "Procesando...",
			    "sLengthMenu":     "Mostrar _MENU_ registros",
			    "sZeroRecords":    "No se encontraron resultados",
			    "sEmptyTable":     "Ningún dato disponible en esta tabla",
			    "sInfo":           "Mostrando registros del _START_ al _END_ de un total de _TOTAL_ registros",
			    "sInfoEmpty":      "Mostrando registros del 0 al 0 de un total de 0 registros",
			    "sInfoFiltered":   "(filtrado de un total de _MAX_ registros)",
			    "sInfoPostFix":    "",
			    "sSearch":         "Buscar:",
			    "sUrl":            "",
			    "sInfoThousands":  ",",
			    "sLoadingRecords": "Cargando...",
			    "oPaginate": {
			         	"sFirst":   "|<",
				        "sPrevious":   "<",
				        "sNext":       ">",
				        "sLast":       ">|"
			    },
			    "oAria": {
			        "sSortAscending":  ": Activar para ordenar la columna de manera ascendente",
			        "sSortDescending": ": Activar para ordenar la columna de manera descendente"
			    }
  	   }
 	  };
 	
      vm.dtOptions = DTOptionsBuilder.fromSource($rootScope.API_BASE+'/rolls/scheduled/'+$scope.stationId+'/'+vm.selectedMachineType + '?access_token=' + token)
	         .withDOM('frti')
	         .withScroller()
	         .withOption('order', [])
	         .withOption('responsive', true)
	         //.withOption('scrollX', 110)
	         .withOption('scrollY', 650)
	         .withOption('select', {style: 'multi', selector : 'tr input[type="checkbox"]'})
	         .withButtons(['csv'])
	         .withDisplayLength(1000)
	         .withColumnFilter({
	        	 aoColumns: [
						{
						    type: 'number'        //rollId            
						},
						{
							type: 'text',
	    	                  bRegex: true,		
	    	                  bSmart: true        //length           
      	                },
      	                {
      	              	  type: 'date',
      	                  bRegex: true,		//due date
      	                  bSmart: true
      	                },
      	                {
      	              	  type: 'text',
      	                  bRegex: true,		//priority
      	                  bSmart: true
      	                },
      	                {
    	              	  type: 'text',
    	                  bRegex: true,		//paper
    	                  bSmart: true
    	                },
    	                {
      	                    type: 'number'   //hours           
      	                },
      	                {
      	              	  type: 'text',
      	                  bRegex: true,		//colors
      	                  bSmart: true
      	                 },
      	                 {
     	              	  type: 'text',
     	                  bRegex: true,		//imposition
     	                  bSmart: true
     	                 },
     	                 {
        	              	  type: 'text',
        	                  bRegex: true,		//hunkeler type
        	                  bSmart: true
        	             },
      	                 {
       	              	  type: 'text',
       	                  bRegex: true,		//mode
       	                  bSmart: true
       	                 }]
	        	   })
	        	   .withLanguage($scope.dataTables);

	         vm.dtColumns = [
	            DTColumnBuilder.newColumn('rollId').withTitle("<span tooltip='"+$translate.instant('productionDashboard_js.Roll')+"'>R</span>").notSortable().renderWith(function (data) {
	          	  return "<button title='"+$translate.instant('productionDashboard_js.View_Roll_Details')+"'   type='button' class='btn btn-info waves-effect fixed-table-button' uib-tooltip='View Roll Details' onclick=\"var $scope = angular.element(event.target).scope(); $scope.openViewRollModal('"+data+"'); $scope.$apply()\">"+data+"</button>";
	            }),
	            DTColumnBuilder.newColumn(null).withTitle("<span tooltip='"+$translate.instant('productionDashboard_js.Length')+"'>L</span>").notSortable().renderWith(function (data) {
	            	if(data.producedLength > 0){
	            		return data.length + "/<br/>" + data.producedLength;
	            	}else{
	            		return data.length;
	            	}
  				}),
	            DTColumnBuilder.newColumn('dueDate').notSortable().withTitle("<span tooltip='"+$translate.instant('productionDashboard_js.Date')+"'>D</span>").
	            //renderWith(function (data) {
//	                var res = "_";
//	                if(data > 0){
//	                    var date = new Date(data);
//	                    var month = date.getMonth() + 1;
//	                    res = (month.length > 1 ? month : "0" + month) + "/" + date.getDate() + "/" + date.getFullYear() + ", " + date.getHours() + ":" + date.getMinutes();
//	                }
//	                return res;
	            	//renderWith(function(data, type) {
	            	//	return $filter('date')(data, 'MM/dd');
	           // }),
	            renderWith(function (data) {
	            	var res = "_";
	                if(data > 0){
	                  var date = new Date(data);
	                  res = monthNames[date.getMonth()] + date.getDate();
	                }
	                return res;
	            }),  
	            DTColumnBuilder.newColumn('priority').withTitle("<span tooltip='Priority'>Pr</span>").notSortable().
	            renderWith(function (data) {
	            	if(data == 'HIGH**'){
	            		return '<span class="badge badge-pr-hi">Hi**</span>';
	            	}else if(data == 'HIGH*'){
	            		return '<span class="badge badge-pr-hi">Hi*</span>';
	            	}else if(data == 'HIGH'){
	            		return '<span class="badge badge-pr-hi">Hi</span>';
	            	}else{
	            		return '<span class="badge badge-pr-nr">Nr</span>';
	            	}
	            }),
	            DTColumnBuilder.newColumn(null).withTitle($translate.instant('productionDashboard_js.Paper')).notSortable().renderWith(function (data) {
		          	  return data.width + "<br/>" + data.paperType.shortName;
		        }),
	            DTColumnBuilder.newColumn('hours').withTitle($translate.instant('productionDashboard_js.Hrs')).notSortable().withOption('defaultContent', ' ').
	            renderWith(function (data) {
	            	return $filter('number')(data, 2);
	            }), 
	            DTColumnBuilder.newColumn('colors').withTitle($translate.instant('productionDashboard_js.Colors')).notSortable().withOption('defaultContent', ' '),
	            DTColumnBuilder.newColumn('impositionTypeId').withTitle($translate.instant('productionDashboard_js.imp')).notSortable().withOption('defaultContent', ' '),
	            DTColumnBuilder.newColumn('machineTypeId').withTitle($translate.instant('productionDashboard_js.machineType')).notSortable().
	            renderWith(function (data) {
	            	if(data == 'PLOWFOLDER'){
	            		return '<span class="badge badge-plw">PF</span>';
	            	}else if(data == 'FLYFOLDER'){
	            		return '<span class="badge badge-fly">FF</span>';
	            	}else if(data == 'POPLINE'){
	            		return '<span class="badge badge-pop">POP</span>';
	            	}else return data;
	            }),
	            DTColumnBuilder.newColumn('productionMode').withTitle($translate.instant('productionDashboard_js.Mode')).notSortable().
	            renderWith(function (data) {
	            	if(data == 'stanlyOnly'){
	            		return 'SO';
	            	}else if(data == 'stanly'){
	            		return 'S';
	            	}else if(data == 'palettOnly'){
	            		return 'PO';
	            	}
	            }),
	            DTColumnBuilder.newColumn(null).notSortable().withTitle("<label class='checkbox checkbox-inline m-r-20'><input  id='checkboxAll' name='checkboxAll' type='checkbox' onclick='var $scope = angular.element(event.target).scope(); $scope.toggleSelectAll(this.checked); $scope.$apply()'/><i class='input-helper'></i></label>").notSortable().renderWith(function (data) {
	          	  var ckbname = 'checkboxForAssignment'+data.rollId;
	          	  var result = "<label class='checkbox checkbox-inline'><input id='"+ckbname+"' name='"+ckbname+"' type='checkbox' onclick=\"var $scope = angular.element(event.target).scope(); $scope.toggleAssignButton(); $scope.$apply()\" '><i class='input-helper'></i></label>";
		          if(data.rollType.id == 'NEW'){
		        	  result += "&nbsp;<span class='badge-new'>NEW</span>";
		          }
	          	  return result;
	          	})
	        ];
	         
	         vm.dtColumnDefs = [];
	         if($rootScope.includeStanly == 'false'){
				vm.dtColumnDefs= [DTColumnDefBuilder.newColumnDef(9).notVisible()];
			 }
	         
	  //SJI_Start: Added Cover batch
	  vm.dtOptionsForBatches = DTOptionsBuilder.fromSource($rootScope.API_BASE+'/sections/allNew'+ '?access_token=' + token)
	  .withDOM('rt')
	  .withScroller()
	  .withOption('order', [])
	  .withOption('responsive', true)
	  //.withOption('scrollX', 110)
	  .withOption('scrollY', 710)
	  .withOption('select', {style: 'multi', selector : 'tr input[type="checkbox"]'})
	  .withDisplayLength(1000)
	  .withButtons(['csv'])
	  .withColumnFilter({
		  aoColumns: [
				 {
					 type: 'number'        //batchId            
				 },
				 {
					   type: 'number'        //quantity           
				   },
				   {
					type: 'text',
					bRegex: true,		//Lamination
					bSmart: true
				   },
				   {
					   type: 'text',
					 bRegex: true,		//priority
					 bSmart: true
				   }]
			})
			.withLanguage($scope.dataTables);

	  vm.dtColumnsForBatches = [
		 DTColumnBuilder.newColumn('coverSectionName').withTitle($translate.instant('productionDashboard_js.BatchSection')).notSortable().renderWith(function (data) {
			 return "<button title='"+$translate.instant('productionDashboard_js.View_Batch_Details')+"'   type='button' class='btn btn-info waves-effect fixed-table-button' uib-tooltip='View Batch Details' onclick=\"var $scope = angular.element(event.target).scope(); $scope.openViewSectionModal('"+data+"'); $scope.$apply()\">"+data+"</button>";
		 }),
		 DTColumnBuilder.newColumn('quantity').withTitle( $translate.instant('productionDashboard_js.Quantity')).notSortable().withOption('defaultContent', ' '),
		 DTColumnBuilder.newColumn('laminationType.id').withTitle($translate.instant('productionDashboard_js.Lamination')).notSortable().withOption('defaultContent', ' '), 
		 DTColumnBuilder.newColumn('priority').withTitle($translate.instant('productionDashboard_js.Priority')).notSortable().withOption('defaultContent', ' '),
		 DTColumnBuilder.newColumn(null).notSortable().withTitle("<label class='checkbox checkbox-inline m-r-20'><input  id='checkboxAll' name='checkboxAll' type='checkbox' onclick='var $scope = angular.element(event.target).scope(); $scope.toggleSelectAll(this.checked); $scope.$apply()'/><i class='input-helper'></i></label>").notSortable().renderWith(function (data) {
			var ckbname = 'checkboxForAssignment'+data.coverSectionId;
			var result = "<label class='checkbox checkbox-inline m-r-20'><input id='"+ckbname+"' name='"+ckbname+"' type='checkbox' onclick=\"var $scope = angular.element(event.target).scope(); $scope.toggleAssignButton(); $scope.$apply()\" '><i class='input-helper'></i></label>";
		  if(status.id == 'NEW'){
			  result += "&nbsp;<span class='badge-new'>NEW</span>";
		  }
			return result;
		  })
	 ];
	  //SJI_End: Added Cover batch

	      vm.dtOptionsForJobs = DTOptionsBuilder.fromSource($rootScope.API_BASE+'/jobs/scheduled/'+$scope.stationId + '?access_token=' + token)
	      .withDOM('frti')
	     // .withOption('responsive', true)
	      .withScroller()
	      .withOption('order', [])
	      .withOption('scrollY', 300)
	      .withOption('select', {style: 'multi', selector : 'tr input[type="checkbox"]'})
	      .withButtons(['csv'])
	      .withDisplayLength(1000)
	      .withColumnFilter({
	     	 aoColumns: [
					{
					    type: 'number'        //jobId            
					},
					{
					    type: 'number'        //orderId            
					},
					{
						type: 'text',
		                bRegex: true,		//priority
		                bSmart: true     
	                },
	                {
	              	  type: 'date',
	                  bRegex: true,		//due date
	                  bSmart: true
	                },
	                {
	                	type: 'number'  //Hours
	                },
	                {
	                    type: 'number'   //Quantity           
	                }]
	     	   })
	     	   .withLanguage($scope.dataTables);
	
		      vm.dtColumnsForJobs = [
		         DTColumnBuilder.newColumn(null).withTitle($translate.instant('productionDashboard_js.Loads')).notSortable().renderWith(function (data) {
		       	  	var res = "<button uib-tooltip='View Job Details' title='"+$translate.instant('productionDashboard_js.View_Job_Details')+data.jobId+"' type='button' class='btn bgm-teal waves-effect fixed-table-button' onclick=\"var $scope = angular.element(event.target).scope(); $scope.openViewJobModal('"+data.jobId+"'); $scope.$apply()\">"+(data.prevJobData != null ? data.prevJobData.loadTags : data.jobId)+"</button>";
			        return res;
		         }),
		         DTColumnBuilder.newColumn('orderId').withTitle($translate.instant('productionDashboard_js.Order')).notSortable().withOption('defaultContent', ' '),
		         DTColumnBuilder.newColumn('jobPriority.name').withTitle($translate.instant('productionDashboard_js.Priority')).notSortable().withOption('defaultContent', ' '),
		         DTColumnBuilder.newColumn('dueDate').notSortable().withTitle($translate.instant('productionDashboard_js.DueDate')).
		         /* renderWith(function (data) {
		             var res = "_";
		             if(data > 0){
		                 var date = new Date(data);
		                 var month = date.getMonth() + 1;
		                 res = (month.length > 1 ? month : "0" + month) + "/" + date.getDate() + "/" + date.getFullYear() + ", " + date.getHours() + ":" + date.getMinutes();
		             }
		             return res;
		         })*/
		         renderWith(function (data) {
	            	var res = "_";
	                if(data > 0){
	                  var date = new Date(data);
	                  res = monthNames[date.getMonth()] + date.getDate();
	                }
	                return res;
	            }),
		         DTColumnBuilder.newColumn('hours').withTitle($translate.instant('productionDashboard_js.Hours')).notSortable().withOption('defaultContent', ' ').
		         renderWith(function (data) {
		            	return $filter('number')(data, 2);
		            }), 
		         DTColumnBuilder.newColumn(null).withTitle($translate.instant('productionDashboard_js.Quantity')).notSortable().
		         renderWith(function (data) {
		        	 var res = "";
		        	 if(data.quantityProduced > 0){
		        		 res = data.quantityNeeded + "/" + data.quantityProduced;
		             }else{
		            	 res = data.quantityNeeded;
		             }
		        	 if(data.stationId == 'COVERPRESS' || data.stationId == 'LAMINATION'){
		        		 	if(data.partNum.endsWith('J')){
		        		 		res = res + " (Dust Jacket)";
		        		 	}else if(data.partNum.endsWith('E')){
		        		 		res = res + " (End Sheet)";
		        		 	}else if(data.partLamination != null){
					        	res = res + " (" + data.partLamination.id + ")" ;
					        }else{
					        	res = res + " (No Lam)";
					        }
			       	 }
		        	 return res;
		         }),
		         DTColumnBuilder.newColumn('jobId').notSortable().withTitle("<label class='checkbox checkbox-inline m-r-20'><input  id='checkboxAll' name='checkboxAll' type='checkbox' onclick='var $scope = angular.element(event.target).scope(); $scope.toggleSelectAll(this.checked); $scope.$apply()'/><i class='input-helper'></i></label>").notSortable().renderWith(function (data) {
		       	  var ckbname = 'checkboxForAssignment'+data;
		       	  return "<label class='checkbox checkbox-inline m-r-20'><input id='"+ckbname+"' name='"+ckbname+"' type='checkbox' onclick=\"var $scope = angular.element(event.target).scope(); $scope.toggleAssignButton(); $scope.$apply()\" '> <i class='input-helper'></i></label>";
		         }),
		         DTColumnBuilder.newColumn(null).withTitle(' ').notSortable().renderWith(function (data) {
		        	 return "<button title='"+$translate.instant('productionDashboard_js.Split_Job')+"' " + data.jobId+" for Order "+data.orderId+"' uib-tooltip='Split' class='btn bgm-lightgreen waves-effect' type='button' onclick=\"var $scope = angular.element(event.target).scope(); $scope.openSplitJobsModal("+data.jobId+","+data.orderId+",'O',"+(data.splitLevel)+","+data.hours+","+data.quantityNeeded+"); $scope.$apply()\"><i class='zmdi zmdi-arrow-split'></i></button></button>";
		         })
		     ];
	      
	      vm.dtOptionsForMachineLogs = DTOptionsBuilder.fromSource().withDOM('frtip').withOption('order', []).withOption('paging', 'first_last_numbers').withDisplayLength(5).withLanguage($scope.dataTables);
	      vm.dtOptionsForMachineJobs = DTOptionsBuilder.fromSource().withDOM('rtip').withOption('order', []).withOption('scrollX', 220).withOption('paging', 'first_last_numbers').withDisplayLength(6).withLanguage($scope.dataTables);
	      vm.dtOptionsForCollapsable = DTOptionsBuilder.fromSource().withDOM('rt').withOption('order', []).withOption('scrollY', 220).withLanguage($scope.dataTables);
	      vm.dtOptionsForDataSupports = DTOptionsBuilder.fromSource().withDOM('rtip').withOption('order', []).withOption('paging', 'first_last_numbers').withDisplayLength(5).withLanguage($scope.dataTables);
		  vm.dtOptionsForMachineSection = DTOptionsBuilder.fromSource().withDOM('rtip').withOption('order', []).withOption('paging', 'first_last_numbers').withDisplayLength(5).withLanguage($scope.dataTables);

		  vm.dtColumnDefsForMachineSection = [
			DTColumnDefBuilder.newColumnDef(0).notSortable(),
			DTColumnDefBuilder.newColumnDef(1).notSortable(),
			DTColumnDefBuilder.newColumnDef(2).notSortable(),
			DTColumnDefBuilder.newColumnDef(3).notSortable(),
			DTColumnDefBuilder.newColumnDef(4).notSortable()
   			];
	      vm.dtColumnDefsForMachineJobs = [
   	                DTColumnDefBuilder.newColumnDef(0).notSortable(),
   	                DTColumnDefBuilder.newColumnDef(1).notSortable(),
   	                DTColumnDefBuilder.newColumnDef(2).notSortable(),
   	                DTColumnDefBuilder.newColumnDef(3).notSortable(),
   	                DTColumnDefBuilder.newColumnDef(4).notSortable(),
   	                DTColumnDefBuilder.newColumnDef(5).notSortable()
   	       ];
	      vm.dtColumnDefsForMachineLoads = [
			   DTColumnDefBuilder.newColumnDef(0).notSortable(),
			   DTColumnDefBuilder.newColumnDef(1).notSortable(),
			   DTColumnDefBuilder.newColumnDef(2).notSortable(),
			   DTColumnDefBuilder.newColumnDef(3).notSortable(),
			   DTColumnDefBuilder.newColumnDef(4).notSortable(),
			   DTColumnDefBuilder.newColumnDef(5).notSortable(),
			   DTColumnDefBuilder.newColumnDef(6).notSortable()
		  ];
	      vm.dtColumnDefsForCollapsable = [
	                DTColumnDefBuilder.newColumnDef(0).notSortable(),
	                DTColumnDefBuilder.newColumnDef(1).notSortable(),
	                DTColumnDefBuilder.newColumnDef(2).notSortable(),
	                DTColumnDefBuilder.newColumnDef(3).notSortable(),
	                DTColumnDefBuilder.newColumnDef(4).notSortable(),
	                DTColumnDefBuilder.newColumnDef(5).notSortable(),
	                DTColumnDefBuilder.newColumnDef(6).notSortable(),
	                DTColumnDefBuilder.newColumnDef(7).notSortable(),
	                DTColumnDefBuilder.newColumnDef(8).notSortable(),
	                DTColumnDefBuilder.newColumnDef(9).notSortable()
	       ];
		     $scope.sortableOptions = {
				    handle: '.myHandle',
		  		    stop: function(e, ui) {
		  		    	var model = ui.item.sortable.model;
		  		    	var machineId = model.machineId;
		  		    	for(var i = 0; i < $scope.station.machines.length; i++){
	  		    			if($scope.station.machines[i].machineId == machineId){
	  		    				if(angular.isDefined(model.jobId)){
	  			  		    		for (var index in $scope.station.machines[i].assignedJobs) {
	  			  		    			$scope.station.machines[i].assignedJobs[index].machineOrdering = index+1;
	  			  		    			//txt += theMachine.jobs[index].jobId;
	  				  		        }
	  			  		    		$scope.alertMsg = $translate.instant('productionDashboard_js.Job2') + model.jobId +  $translate.instant('productionDashboard_js.reordered_on_machine')+ $scope.station.machines[i].name;
	  			  		    	}else{
	  			  		    		for (var index in $scope.station.machines[i].assignedRolls) {
	  			  		    			$scope.station.machines[i].assignedRolls[index].machineOrdering = index+1;
	  			  		    			//txt += theMachine.rolls[index].rollId;
	  			  		    			// if this is a left over roll with no jobs, send it back for scheduling
	  			  		    			if($scope.stationId == 'PRESS' && index > 0 && $scope.station.machines[i].assignedRolls[index].status.id == 'ASSIGNED' && $scope.station.machines[i].assignedRolls[index].jobs.length == 0){
	  			  		    				$scope.station.machines[i].assignedRolls[index].status.id = 'AVAILABLE';
	  			  		    				$scope.station.machines[i].assignedRolls[index].machineId = null;
	  			  		    			}
	  				  		        }
	  			  		    		$scope.alertMsg =  $translate.instant('productionDashboard_js.Roll')+ model.rollId + $translate.instant('productionDashboard_js.moved_in_rank_on_machine') + $scope.station.machines[i].name;
	  			  		    	}
	  		    				//save to the db
	  			  		    	machineServiceAjax.updateMachine($scope.station.machines[i]).then(function(){
	  			  		    		  $scope.errorMsgDown = false;
	  					              toasty.success({
	  								          title: $translate.instant('productionDashboard_js.Machine_Assignment'),
	  								          msg: $scope.alertMsg,
	  								          showClose: true,
	  						                  clickToClose: true,
	  						                  timeout: 10000,
	  						                  sound: false,
	  						                  html: false,
	  						                  shake: false,
	  						                  theme: "bootstrap"
	  								      });
	  			  		    		$scope.loadStation($scope.stationId);
	  				   			}, function(data, status, headers, config){
	  					            $scope.errorMsgDown = true;
	  					            $scope.successMsgDown = false;
	  					            $scope.errors = data.errors;
	  					        });
	  			  		    	//confirm('position: ' + ui.item.sortable.index + '; model: ' + ui.item.sortable.model);
	  		    			}
	  		    		}
		  		    }
		  	  };
	         
	          $scope.open2 = function() {
		        $scope.popup2.opened = true;
		      };
		      $scope.popup2 = {
		    		    opened: false
		      };
		      $scope.open3 = function() {
			        $scope.popup3.opened = true;
			  };
			  $scope.popup3 = {
			    	 opened: false
			  };
			  $scope.formats = ['dd-MMMM-yyyy', 'yyyy/MM/dd', 'dd.MM.yyyy', 'shortDate'];
			  $scope.format = $scope.formats[0];
		   
		   $scope.openViewJobModal = function (id) {
			   var modalInstance = $uibModal.open({
		        	backdrop  : 'static',
		        	keyboard  : false,
		        	animation: true,
		        	templateUrl: './views/viewJobModalContent.html',
		        	controller: 'EditJobModalInstanceCtrl',
		        	scope: $scope,
		        	size:'lg',
		        	resolve: {
		        		jobId: function () {
		        		return id;
		            }
		          }
		        });
		        modalInstance.result.then(function(selectedItem) {
		        }, function () {});
		   };
		   
		   $scope.dtColumnDefsForDataSupports = [
	             DTColumnDefBuilder.newColumnDef(0).notSortable(),
	             DTColumnDefBuilder.newColumnDef(1).notSortable(),
	             DTColumnDefBuilder.newColumnDef(2).notSortable(),
	             DTColumnDefBuilder.newColumnDef(3).notSortable(),
	             DTColumnDefBuilder.newColumnDef(4).notSortable()
	             ];
	      $scope.dtOptionsForDataSupports = DTOptionsBuilder.fromSource().withDOM('rtip').withOption('order', []).withOption('paging', 'first_last_numbers').withDisplayLength(5).withLanguage($scope.dataTables);
	      $scope.progress =  'PENDING';
		   
	      $scope.loadOrderStatusOptions = function(){
 	    	  lookupServiceAjax.readAll('OrderStatus').then(function(data){
 		            $scope.orderStatusOptions = data;
 		         });
 		};
 		$scope.loadOrderStatusOptions();
 		
		   $scope.openOverviewOrderModal = function (id) {
		        var modalInstance = $uibModal.open({
		        	backdrop  : 'static',
		        	keyboard  : false,
		        	animation: true,
		        	templateUrl: './views/overviewOrder.html',
		        	controller: 'OverviewditOrderModalInstanceCtrl',
		        	scope: $scope,
		        	size:'xl',
		        	resolve: {
		        		orderId: function () {
		        			return id;
		        		},
		        		actionType: function () {
		        			return "overview";
		        		}
		          }
		        });  modalInstance.result.then(function () {
		         	vm.dtInstance.reloadData();
		           }, function () {});
		     };
		   
		   $scope.openSplitJobsModal = function (jobId, orderId, cascadeFlag, splitLevel, hours, originalQuantity) {
		         var modalInstance = $uibModal.open({
		           animation: true,
		           backdrop  : 'static',
		       	   keyboard  : false,
		           templateUrl: './views/splitJobsModalContent.html',
		           controller: 'SplitJobModalInstanceCtrl',
		           scope: $scope,
		           size:'m',
		           resolve: {
		        	   jobId: function () {
		        			return jobId;
		        		},
		        		orderId: function () {
			     			return orderId;
			     		},
			     		cascadeFlag: function () {
			     			return cascadeFlag;
			     		},
			     		splitLevel: function () {
			     			return splitLevel;
			     		},
			     		hours: function () {
			     			return hours;
			     		},
			     		originalQuantity: function () {
			     			return originalQuantity;
			     		}
		           }
		         });
		         modalInstance.result.then(function () {
		              vm.dtInstanceForJobs.reloadData();
		           }, function () {});
		     };
		   
		   $scope.openEditPartModal = function (id) {
			   var modalInstance = $uibModal.open({
		        	backdrop  : 'static',
		        	keyboard  : false,
		        	animation: true,
		        	templateUrl: './views/editPartModalContent.html',
		        	controller: 'EditPartModalInstanceCtrl',
		        	scope: $scope,
		        	size: 'lg',
		        	resolve: {
		        		partNum: function () {
		        			return id;
		        		},
		        		disableFields: function () {
		        			return true;
		        		}
		          }
		        });
		        modalInstance.result.then(function(selectedItem) {
		        }, function () {});
		   };
      
	      $scope.highlightedMachine = null;
	      $scope.toggleHighlightedMachine = function(machineId){
	     	 var table = $('#assignmentTable').DataTable();
	     	 $scope.highlightedMachine = machineId;
	     	 var rowId = "#machineRow" + machineId;
	     	 //first remove the clicked class from the row/machine that has it, then add the class to the new clicked machineRow
	     	 $("div[id^='machineRow']").each(function() {
	     		 $(this).removeClass('clicked');
	     	 });
	     	 //also disable all Assign buttons, and un-check all checkboxes
	     	 $(":input[id^='checkboxForAssignment']").each(function(){
	     		 $(this).prop('checked', false);
	       	 });
	     	 table.rows().deselect();
	     	 $(":input[id^='assignButton']").each(function(){
	     		 $(this).attr('disabled', 'disabled');
	       	  });
	     	 $(rowId).addClass('clicked');
	     	 if($scope.station.inputType == 'Roll'){
		     	 for(var t = 0; t < $scope.station.machines.length ; t++){
		     		if($scope.station.machines[t].machineId == machineId){
		     			vm.selectedMachineType = $scope.station.machines[t].machineType.id;
		     			//vm.dtInstance.reloadData(function callback(json) {});
		     			vm.dtInstance.changeData($rootScope.API_BASE+'/rolls/scheduled/'+$scope.stationId+'/'+vm.selectedMachineType + '?access_token=' + token);
		     			//TODO if any roll/job is already assigned to a machine, pre-check it
		     			
		     			break;
		     		}
		     	 }
	     	 }
	      }
	      
	      $scope.toggleSelectAll = function(checked){
	       	  var table = $('#assignmentTable').DataTable();
	       	  if(checked){
	       		  table.rows().select();
	       		$(":input[id^='checkboxForAssignment']").prop('checked', true);
	       	  }else{
	       		  table.rows().deselect();
	       		$(":input[id^='checkboxForAssignment']").prop('checked', false);
	       	  }
	       	  $scope.toggleAssignButton();
	      };
	         
	      $scope.toggleAssignButton = function(){
	       	  var inputs = [];
	       	  var makeItDisabled = true;
	       	  var btnId = "#assignButton" + $scope.highlightedMachine;
	       	  
	       	  $(":input[id^='checkboxForAssignment']").each(function(){
	       		  inputs.push('#'+$(this).attr('id'));
	       	  });
	       	  for(var i = 0; i < inputs.length ; i++){
	       		  if($(inputs[i]).is(':checked') && $scope.highlightedMachine != null){
	       			  $(btnId).removeAttr('disabled');
	       			  makeItDisabled = false;
	       		  }
	       	  }
	       	  if(makeItDisabled){
	       		  $(btnId).attr('disabled', 'disabled');
	       	  }
	       };  
	         
	       $scope.assignToMachine = function (machineId, machineName) {
		       	  var table = $('#assignmentTable').DataTable();
		       	  var dt = table.rows({selected:true}).data();
		       	  var selectedRollsForAssignment = [];
		       	  //the first value to push to the collection is the machineId
		       	  selectedRollsForAssignment.push(machineId);
		       	  for(var i=0;i<table.rows({selected:true}).count();i++){
		       		selectedRollsForAssignment.push(dt[i].rollId);
		       	  }
		       	  machineServiceAjax.assignToMachine(selectedRollsForAssignment).then(function(rollsNotAssigned){
		       		 // $scope.successMsgDown = true;
		              $scope.errorMsgDown = false;
		              selectedRollsForAssignment[0] = "";
		              if($scope.stationId == 'PRESS'){
		            	  $scope.alertMsg = $translate.instant('productionDashboard_js.rolls_not_assigned_to_machine_press') + rollsNotAssigned
		              }
		       	  	  if($scope.stationId == 'PLOWFOLDER'){
		       	  		  $scope.alertMsg = $translate.instant('productionDashboard_js.rolls_not_assigned_to_machine_pf') + rollsNotAssigned
		       	  	  }
		              if(rollsNotAssigned.length > 0){
		            	  /*toasty.info({
					          title: $translate.instant('productionDashboard_js.Machine_Assignment'),
					          msg:  $scope.alertMsg,
					          showClose: true,
			                  clickToClose: true,
			                  timeout: 10000,
			                  sound: false,
			                  html: false,
			                  shake: false,
			                  theme: "bootstrap"
					      });*/
		            	  SweetAlert.swal($translate.instant('productionDashboard_js.Machine_Assignment'), $scope.alertMsg, "error");
		              }else{
		            	  toasty.success({
					          title: $translate.instant('productionDashboard_js.Machine_Assignment'),
					          msg:  $translate.instant('productionDashboard_js.rolls_assigned_to_machine') + machineName +  $translate.instant('productionDashboard_js.for_production') + selectedRollsForAssignment,
					          showClose: true,
			                  clickToClose: true,
			                  timeout: 10000,
			                  sound: false,
			                  html: false,
			                  shake: false,
			                  theme: "bootstrap"
					      });
		              }
		       		  vm.dtInstance.reloadData();
		       		  $('#checkboxAll').prop('checked', false);
		       		  $scope.loadStation($scope.stationId);
		   	      });
		   	};
		   	
		   	$scope.unassignRoll = function (rollId) {
		       	  rollServiceAjax.unassignRoll(rollId).then(function(){
		       		 // $scope.successMsgDown = true;
		              $scope.errorMsgDown = false;
		              $scope.alertMsg = $translate.instant('productionDashboard_js.Roll2') + rollId + $translate.instant('productionDashboard_js.un_assigned_from_machine');
		              toasty.success({
				          title: $translate.instant('productionDashboard_js.Machine_Assignment'),
				          msg: $translate.instant('productionDashboard_js.Roll2') + rollId + $translate.instant('productionDashboard_js.un_assigned_from_machine'),
				          showClose: true,
		                  clickToClose: true,
		                  timeout: 10000,
		                  sound: false,
		                  html: false,
		                  shake: false,
		                  theme: "bootstrap"
				      });
		       		  vm.dtInstance.reloadData();
		       		  $scope.loadStation($scope.stationId);
		   	      });
		   	};
		   	
		   	$scope.unassignJob = function (jobId) {
		       	  jobServiceAjax.unassignJob(jobId).then(function(){
		       		  //$scope.successMsgDown = true;
		              $scope.errorMsgDown = false;
		              $scope.alertMsg = $translate.instant('productionDashboard_js.Job') + jobId + $translate.instant('productionDashboard_js.un_assigned_from_machine');
		              toasty.success({
				          title: $translate.instant('productionDashboard_js.Machine_Assignment'),
				          msg: $translate.instant('productionDashboard_js.Job') + jobId + $translate.instant('productionDashboard_js.un_assigned_from_machine'),
				          showClose: true,
		                  clickToClose: true,
		                  timeout: 10000,
		                  sound: false,
		                  html: false,
		                  shake: false,
		                  theme: "bootstrap"
				      });
		       		  vm.dtInstanceForJobs.reloadData();
		       		  $scope.loadStation($scope.stationId);
		   	      });
			   };
			   
			   $scope.unassignBatch = function (sectionId) {
				batchServiceAjax.unassignSection(sectionId).then(function(){
					//$scope.successMsgDown = true;
				 $scope.errorMsgDown = false;
				 $scope.alertMsg = $translate.instant('productionDashboard_js.Batch') + sectionId + $translate.instant('productionDashboard_js.un_assigned_from_machine');
				 toasty.success({
					 title: $translate.instant('productionDashboard_js.Machine_Assignment'),
					 msg: $translate.instant('productionDashboard_js.Batch') + sectionId + $translate.instant('productionDashboard_js.un_assigned_from_machine'),
					 showClose: true,
					 clickToClose: true,
					 timeout: 10000,
					 sound: false,
					 html: false,
					 shake: false,
					 theme: "bootstrap"
				 });
					vm.dtInstanceForBatches.reloadData();
					$scope.loadStation($scope.stationId);
				});
		  };   
		  $scope.assignBatchesToMachine = function (machineId) {
				var table = $('#assignmentTable').DataTable();
				var dt = table.rows({selected:true}).data();
				var selectedBatchesForAssignment = [];
			
				//the first value to push to the collection is the machineId
				selectedBatchesForAssignment.push(machineId);
				for(var i=0;i<table.rows({selected:true}).count();i++){
					selectedBatchesForAssignment.push(dt[i].coverSectionId);
				}
				machineServiceAjax.assignSectionsToMachine(selectedBatchesForAssignment).then(function(notAssignedSections){
				 $scope.errorMsgDown = false;
				 //selectedBatchesForAssignment[0] = "";
				 $scope.alertMsg = $translate.instant('productionDashboard_js.batches_not_assigned_to_machine') + notAssignedSections;
				 if(notAssignedSections.length > 0){

				SweetAlert.swal($translate.instant('productionDashboard_js.Machine_Assignment'), $scope.alertMsg, "error");
				}else
				{toasty.success({
					 title: $translate.instant('productionDashboard_js.Machine_Assignment'),
					 msg: $translate.instant('productionDashboard_js.batches_assigned_to_machine') + machineId + $translate.instant('productionDashboard_js.for_production') + selectedBatchesForAssignment,
					 showClose: true,
					 clickToClose: true,
					 timeout: 10000,
					 sound: false,
					 html: false,
					 shake: false,
					 theme: "bootstrap"
				 });}
					vm.dtInstanceForBatches.reloadData();
					$scope.loadStation($scope.stationId);
					$('#checkboxAll').prop('checked', false);
				}, function(data, status, headers, config){
				   $scope.errorMsgDown = true;
				   $scope.successMsgDown = false;
				   $scope.errors = data.errors;
				 });
				 /*stationServiceAjax.loadStationById($scope.stationId).then(function(data){
					$scope.station = data;
					 machineServiceAjax.createSection(selectedBatchesForAssignment);
				 });*/
		  };
		   	$scope.assignJobsToMachine = function (machineId) {
		       	  var table = $('#assignmentTable').DataTable();
		       	  var dt = table.rows({selected:true}).data();
		       	  var selectedJobsForAssignment = [];
		       	  //the first value to push to the collection is the machineId
		       	  selectedJobsForAssignment.push(machineId);
		       	  for(var i=0;i<table.rows({selected:true}).count();i++){
		       		selectedJobsForAssignment.push(dt[i].jobId);
		       	  }
		       	  machineServiceAjax.assignJobsToMachine(selectedJobsForAssignment).then(function(){
		       		  //$scope.successMsgDown = true;
		              $scope.errorMsgDown = false;
		              selectedJobsForAssignment[0] = "";
		              $scope.alertMsg = $translate.instant('productionDashboard_js.jobs_assigned_to_machine') + machineId + $translate.instant('productionDashboard_js.for_production') + selectedJobsForAssignment;
		              toasty.success({
				          title: $translate.instant('productionDashboard_js.Machine_Assignment'),
				          msg: $translate.instant('productionDashboard_js.jobs_assigned_to_machine') + machineId + $translate.instant('productionDashboard_js.for_production') + selectedJobsForAssignment,
				          showClose: true,
		                  clickToClose: true,
		                  timeout: 10000,
		                  sound: false,
		                  html: false,
		                  shake: false,
		                  theme: "bootstrap"
				      });
		       		  vm.dtInstanceForJobs.reloadData();
		       		  $('#checkboxAll').prop('checked', false);
		       		  $scope.loadStation($scope.stationId);
		   	      }, function(data, status, headers, config){
			            $scope.errorMsgDown = true;
			            $scope.successMsgDown = false;
			            $scope.errors = data.errors;
			          });
		   	};
		   	
		   	$(document).ready(function(){
		   		 var btnId = "#assignButton" + $scope.highlightedMachine;
		   		 $(":input[id^='checkboxForAssignment']").click(function() {
			        if ($(this).is(':checked')) {
			        	 $(btnId).removeAttr('disabled');
			        } else {
			        	$(btnId).attr('disabled', 'disabled');
			        }
				  });
		   		//window.resizeTo('200', '200');
		   		//$(window).trigger('resize');
		   		/*$(":input[id^='status']").each(function(){
		   			$(this).change(function(){
			   			var selectedValue = $(this).val();
			   			var machineId = $(this).attr('id').substring('status'.length);
			   			machineServiceAjax.getMachineById(machineId).then(function(data){
			   				data.status.id = selectedValue;
			   				machineServiceAjax.updateMachine(data).then(function(){
			   				 $scope.loadStation($scope.stationId);  //refresh the Dashboard data
			   				});
			   			});
		   			});
		   		  });*/
		   		//var table = $('#jobsLoadsTable').DataTable();
		   		// Add event listener for opening and closing details
		   	   // $('body').on('click', '.details-control', function () {
		   	   //     var tr = $(this).closest('tr');
		   	        //var row = table.row( tr );
		   	       //   var row = vm.dtInstanceForJobsLoads.DataTable.row( tr );
		   	       //row.child('tst');
		   	 	//alert(row.child('tst').data());
		   	        //if ( row.child.isShown() ) {
		   	            // This row is already open - close it
		   	            //row.child.hide();
		   	           // tr.removeClass('shown');
		   	       // }
		   	       // else {
		   	            // Open this row
		   	            //row.child( format(row.data()));
		   	           // row.child.show();
		   	        // row.child( format(row.data()) ).show();
		   	        //row.child( 'OK' ).show();
		   	        //row.child.show();
		   	         //  tr.addClass('shown');
		   	        //}
		   	    // alert(row.child().data());  
		   	    // alert(row.child( 'tst2' ).data());
		   	    //} );
			   	// var table = $('#example').DataTable( {
			      //   "ajax": "data/objects.txt",
			        // "columns": [
			          //   {
			            //     "className":      'details-control',
			              //   "orderable":      false,
			                // "data":           null,
			               //  "defaultContent": ''
			             //},
			             //{ "data": "name" },
			             //{ "data": "position" },
			             //{ "data": "office" },
			             //{ "data": "salary" }
			         //],
			         //"order": [[1, 'asc']]
			     //} );
		     
		   	});
		   	
		   	/* Formatting function for row details 
		   	function format1 ( d ) {
		   	    // `d` is the original data object for the row
		   	   /*return '<table>'+ 
				   	        '<tr ng-repeat="load in '+d.loadTags+'>'+
				   	            '<td></td>'+
				   	            '<td></td>'+
				   	            '<td>'+load.loadTagId+'</td>'+
				   	        '</tr>'+
				   	   '</table>';
		   		
		   		return '<table>'+ 
				   	        '<tr>'+
				   	            '<td>a</td>'+
				   	            '<td>b</td>'+
				   	            '<td>c</td>'+
				   	        '</tr>'+
				   	   '</table>';*/
		   		
		   	// return   'test';
		   	//}*/
		   	
		    /*$scope.tagDataCollapseFn = function (i) {
		    	var jobsCount = 0;
		    	if(stationId == 'PLOWFOLDER'){
		    		jobsCount = $scope.station.machines[i].rollOnProd != null ? $scope.station.machines[i].rollOnProd.jobs.length : 0;
		    	}else{
		    		jobsCount = $scope.station.machines[i].jobs.length;
		    	}
		    	$scope.tagDataCollapse[i] = new Array(jobsCount);
		    	for(var j = 0; j < jobsCount; j++){
		    		if(stationId == 'PLOWFOLDER'){
		    			if($scope.station.machines[i].rollOnProd != null && $scope.station.machines[i].currentJob.jobId == $scope.station.machines[i].rollOnProd.jobs[j].jobId){
			    			$scope.tagDataCollapse[i][j] = false;
			    		}else{
			    			$scope.tagDataCollapse[i][j] = true;
			    		}
		    		}else{
		    			if($scope.station.machines[i].currentJob.jobId == $scope.station.machines[i].jobs[j].jobId){
			    			$scope.tagDataCollapse[i][j] = false;
			    		}else{
			    			$scope.tagDataCollapse[i][j] = true;
			    		}
		    		}
		    	}
		    };*/
		    
		    /*$scope.selectTableRow = function (i, index, jobId) {
		    	//$('#pfJobsTable').DataTable().columns.adjust().draw(true);
		    	//angular.element($window).resize(angular.element($window).width() -1, angular.element($window).height()-1);
		    	//window.resizeTo('200', '200');
		    	//$(window).trigger('resize');
		        if ($scope.tagDataCollapse[i] === 'undefined') {
		            $scope.tagDataCollapse[i] = $scope.tagDataCollapseFn(i);
		        } else {
		            if ($scope.tableRowExpanded[i] === false && $scope.tableRowIndexCurrExpanded[i] === "" && $scope.jobIdExpanded[i] === "") {
		                $scope.tableRowIndexPrevExpanded[i] = "";
		                $scope.tableRowExpanded[i] = true;
		                $scope.tableRowIndexCurrExpanded[i] = index;
		                $scope.jobIdExpanded[i] = jobId;
		                $scope.tagDataCollapse[i][index] = false;
		            } else if ($scope.tableRowExpanded[i] === true) {
		                if ($scope.tableRowIndexCurrExpanded[i] === index && $scope.jobIdExpanded[i] === jobId) {
		                    $scope.tableRowExpanded[i] = false;
		                    $scope.tableRowIndexCurrExpanded[i] = "";
		                    $scope.jobIdExpanded[i] = "";
		                    $scope.tagDataCollapse[i][index] = true;
		                } else {
		                    $scope.tableRowIndexPrevExpanded[i] = $scope.tableRowIndexCurrExpanded[i];
		                    $scope.tableRowIndexCurrExpanded[i] = index;
		                    $scope.jobIdExpanded[i] = jobId;
		                    $scope.tagDataCollapse[i][$scope.tableRowIndexPrevExpanded[i]] = true;
		                    $scope.tagDataCollapse[i][$scope.tableRowIndexCurrExpanded[i]] = false;
		                }
		            }
		        }
		    };*/
		   	
		   	$scope.updateMachineStatus = function(machine, s){
		   		$scope.successMsgDown = false;
	      	    $scope.errorMsgDown = false;
		   		machineServiceAjax.getMachineById(machine.machineId).then(function(data){
		   			var alog = {};
		   			$scope.defaultLog2 = {
		   		    		"machineId":machine.machineId,
		   		    		"rollId":"",
		   		 		    "event":"",
		   		 		    "logResult":{},
		   		 		    "logCause":{},
		   		 		    "currentJobId":"",
		   		 		    "rollLength":"",
		   		 		    "startTime":"",
		   		 		    "finishTime":"",
		   		 		    "counterFeet":""
		   		    };
		   			/*if($scope.successMsgArray.length < (s+1)){
						$scope.successMsgArray = [];
						$scope.errorMsgArray = [];
		            	for(var f = 0; f < $scope.nbMachineCount; f++){
		            		$scope.successMsgArray.push(false);
		            		$scope.errorMsgArray.push(false);
		            	}
		   			}*/
		   			if(machine.status.id != 'RUNNING' && data.status.id != 'RUNNING' && machine.status.id != data.status.id){
			   				machineServiceAjax.updateMachine(machine).then(function(){
			   					//Add a log entry for this status change
			   					alog = jQuery.extend({}, $scope.defaultLog2);
			   					alog.machineId = machine.machineId;
			   					if(machine.rollOnProd != null){
			   						alog.rollId = machine.rollOnProd.rollId;
			   					}else if(machine.jobOnProd != null){
			   						alog.currentJobId = machine.jobOnProd.jobId;
			   					}
			   					
			   					if(machine.status.id == 'ON' || machine.status.id == 'OFF'){
			   						alog.event = 'ONOFF';
			   					}else{
			   						alog.event = 'SERVICE';
			   					}
			   					if(machine.status.id == 'ON'){
			   						alog.logResult.id = 'ON';
			   					}else if(machine.status.id == 'OFF'){
			   						alog.logResult.id = 'OFF';
			   					}else if(machine.status.id == 'SERVICE'){
			   						alog.logResult.id = 'SERVICE';
			   					}else if(machine.status.id == 'OUTSERVICE'){
			   						alog.logResult.id = 'REPAIR';
			   					}
			   					if(machine.status.id == 'ON'){
			   						alog.logCause.id = 'ONOFF';
			   					}else if(machine.status.id == 'OFF'){
			   						alog.logCause.id = 'ONOFF';
			   					}else if(machine.status.id == 'SERVICE'){
			   						alog.logCause.id = 'SERVICE';
			   					}else if(machine.status.id == 'OUTSERVICE'){
			   						alog.logCause.id = 'ISSUE';
			   					}
			   					
			   					alog.startTime = new Date();
			   					alog.counterFeet = machine.logs.length > 0 ? machine.logs[0].counterFeet : 0;
			   					logServiceAjax.addLogFromProd(alog).then(function(){
			   						$scope.loadStation($scope.stationId);  //TODO see if any better way to refresh the Dashboard data
			   						if(alog.logResult.id == 'ON' || alog.logResult.id == 'OFF'){
			   							toasty.info({
			   					          title: $translate.instant('productionDashboard_js.Machine_Status'),
			   					          msg: $translate.instant('productionDashboard_js.Machine_Turned_On_Off'),
				   					      showClose: true,
				   		                  clickToClose: true,
				   		                  timeout: 10000,
				   		                  sound: false,
				   		                  html: false,
				   		                  shake: false,
				   		                  theme: "bootstrap"
			   					      });
			   							$scope.alertMsg = $translate.instant('productionDashboard_js.Machine_Under_Service');
			   						}else{
			   							toasty.info({
				   					          title:  $translate.instant('productionDashboard_js.Machine_Status'),
				   					          msg: $translate.instant('productionDashboard_js.Machine_Under_Service'),
					   					      showClose: true,
					   		                  clickToClose: true,
					   		                  timeout: 10000,
					   		                  sound: false,
					   		                  html: false,
					   		                  shake: false,
					   		                  theme: "bootstrap"
				   					      });
			   							$scope.alertMsg = $translate.instant('productionDashboard_js.Machine_Under_Service');
			   						}
			   						/*if($scope.successMsgArray.length < (s+1)){
			   							$scope.successMsgArray = [];
			   							$scope.errorMsgArray = [];
			   			            	for(var f = 0; f < $scope.nbMachineCount; f++){
			   			            		$scope.successMsgArray.push(false);
			   			            		$scope.errorMsgArray.push(false);
			   			            	}
			   			   			}*/
			   			           // $scope.successMsgArray[s] = true;
			   			            $scope.errorMsgArray[s] = false;
			   					});
				   			});
			   		}else{
			   			//$scope.loadStation($scope.stationId);  //refresh the Dashboard data
			   			machine.status.id = data.status.id
			   			$scope.successMsgArray[s] = false;
			            $scope.errorMsgArray[s] = false;
			   		}
	   			});
		   	};
		   	
		   	$scope.updateJobSentFlag= function(job, theMachine){
		   		jobServiceAjax.updateJob(job).then(function(){
		   			rollServiceAjax.getRollById(theMachine.rollOnProd.rollId).then(function(rollOnProdData){
		   				for(var d = 0; d < rollOnProdData.jobs.length; d++){
		   					if(rollOnProdData.jobs[d].fileSentFlag == true){
		   						theMachine.currentJob = rollOnProdData.jobs[d];
		   					}
		   				}
		   				machineServiceAjax.updateMachine(theMachine).then(function(){
			   			});
		   			});
	   			});
		   	};
		   	  
		   	$scope.loadStatusOptions = function(){
		    	  lookupServiceAjax.readAll('MachineStatus').then(function(data){
			            $scope.statusOptions = data;
			         });
			};
			$scope.loadStatusOptions();
			$scope.status = {
				 repeatSelect: null,
			     availableOptions: $scope.statusOptions,
		    };
		   	  
			$scope.loadPaperTypeOptions = function(){
	 	    	  lookupServiceAjax.readAll('PaperType').then(function(data){
	 		            $scope.paperTypeOptions = data;
	 		         });
	 		};
	 		$scope.loadPaperTypeOptions();
	 		$scope.paperType = {
	 			    repeatSelect: null,
	 			    availableOptions: $scope.paperTypeOptions,
	 		};
	 		
	 		$scope.loadPriorityOptions = function(){
		     	  lookupServiceAjax.readAll('Priority').then(function(data){
		 	            $scope.priorityOptions = data;
		 	      });
	 	    };
	 	    $scope.loadPriorityOptions();
	 	    $scope.priority = {
	 		    repeatSelect: null,
	 		    availableOptions: $scope.priorityOptions,
	 		};
	 	    
	 	    $scope.loadLaminationOptions = function(){
	     	  lookupServiceAjax.readAll('Lamination').then(function(data){
	 	            $scope.laminationOptions = data;
	 	         });
		 	};
		 	$scope.loadLaminationOptions();
		 	$scope.lamination = {
		 		    repeatSelect: null,
		 		    availableOptions: $scope.laminationOptions,
		 	};
		 	
		 	$scope.loadBindingTypeOptions = function(){
		     	  lookupServiceAjax.readAll('BindingType').then(function(data){
		 	            $scope.bindingTypeOptions = data;
		 	         });
		 	};
		 	$scope.loadBindingTypeOptions();
		 	$scope.bindingType = {
		 		    repeatSelect: null,
		 		    availableOptions: $scope.bindingTypeOptions,
		 	};
		 	
		 	$scope.loadCritiriaOptions = function(){
		     	  lookupServiceAjax.readAll('Critiria').then(function(data){
		 	            $scope.critiriaOptions = data;
		 	         });
		 	};
		 	$scope.loadCritiriaOptions();
		 	$scope.critiria = {
		 		    repeatSelect: null,
		 		    availableOptions: $scope.critiriaOptions,
		 	};
	     
	       $scope.openViewRollModal = function (id) {
		     var modalInstance = $uibModal.open({
		        	backdrop  : 'static',
		        	keyboard  : false,
		        	animation: true,
		        	templateUrl: './views/viewRollModalContent.html',
		        	controller: 'EditRollModalInstanceCtrl',
		        	scope: $scope,
		        	size:'lg',
		        	resolve: {
		        		rollId: function () {
		        		return id;
		            }
		          }
		        });
		        modalInstance.result.then(function(selectedItem) {
		        }, function () {});
		   };
		   $scope.openViewBatchModal = function (id) {
			var modalInstance = $uibModal.open({
				   backdrop  : 'static',
				   keyboard  : false,
				   animation: true,
				   templateUrl: './views/viewBatchModalContent.html',
				   controller: 'BatchModalInstanceCtrl',
				   scope: $scope,
				   size:'lg',
				   resolve: {
					   batchId: function () {
					   return id;
				   }
				 }
			   });
			   modalInstance.result.then(function(selectedItem) {
			   }, function () {});
		  };	  
		  $scope.openViewSectionModal = function (id) {
			var modalInstance = $uibModal.open({
				   backdrop  : 'static',
				   keyboard  : false,
				   animation: true,
				   templateUrl: './views/viewSectionModalContent.html',
				   controller: 'SectionModalInstanceCtrl',
				   scope: $scope,
				   size:'lg',
				   resolve: {
					   sectionId: function () {
					   return id;
				   }
				 }
			   });
			   modalInstance.result.then(function(selectedItem) {
			   }, function () {});
		  };	        
		   $scope.openViewLogModal = function (id) {
			  var modalInstance = $uibModal.open({
		        	backdrop  : 'static',
		        	keyboard  : false,
		        	animation: true,
		        	templateUrl: './views/viewLogModalContent.html',
		        	controller: 'EditLogModalInstanceCtrl',
		        	scope: $scope,
		        	size:'lg',
		        	resolve: {
		        		logId: function () {
		        		return id;
		            }
		          }
		        });
		        modalInstance.result.then(function(selectedItem) {
		        }, function () {});
		   };
	    	 
	     $scope.openInterruptMachineModal = function (stationId, machineId, rollId, currentJobId, i) {
	    	 var modalInstance = $uibModal.open({
		        	backdrop  : 'static',
		        	keyboard  : false,
		        	animation: true,
		        	templateUrl: './views/interruptMachineModalContent.html',
		        	controller: 'InterruptMachineModalInstanceCtrl',
		        	scope: $scope,
		        	size:'lg',
		        	resolve: {
		        		
		        		stationId: function () {
		        			return stationId;
		        		},
		        		machineId: function () {
		        			return machineId;
		        		},
				        rollId: function () {
			     			return rollId;
			     		},
			     		currentJobId: function () {
			     			return currentJobId;
			     		},
			     		i: function () {
			     			return i;
			     		}
		          }
		        });
		        modalInstance.result.then(function(selectedItem) {
		        	
		        }, function () {});
		 };
		 
		 $scope.openAddLoadTagModal = function (job, machine) {
	         var modalInstance = $uibModal.open({
	           animation: true,
	           backdrop  : 'static',
	       	   keyboard  : false,
	           templateUrl: './views/addLoadTagModalContent.html',
	           controller: 'AddLoadTagModalInstanceCtrl',
	           scope: $scope,
	           size:'lg',
	           resolve: {
	        	    jobId: function () {
		        		return job.jobId;
		            },
		            finishTime: function () {
		        		return new Date();
		            },
		            startTime: function () {
		            	if(machine.runningAndAssignedJobs[0].loadTags.length > 0){
		            		if(machine.runningAndAssignedJobs[0].loadTags[0].finishTime != null){
		            			var ft1 = new Date(machine.runningAndAssignedJobs[0].loadTags[0].finishTime);
		            		}else{
		            			var ft1 = new Date(machine.runningAndAssignedJobs[0].loadTags[0].startTime);
		            		}
		            	}
		            	if(machine.logs.length > 0){
		            		if(machine.logs[0].finishTime != null){
		            			var ft2 = new Date(machine.logs[0].finishTime);
		            		}else{
		            			var ft2 = new Date(machine.logs[0].startTime);
		            		}
		            	}
		            	if(angular.isDefined(ft1) && angular.isDefined(ft2)){
		            		if(ft1 > ft2){
		            			return ft1;
		            		}else{
		            			return ft2;
		            		}
		            	}else if(angular.isDefined(ft1)){
		            		return ft1;
		            	}else if(angular.isDefined(ft2)){
		            		return ft2;
		            	}
		            	return null;
		            },
		            quantityMax: function () {
		        		return job.quantityNeeded - job.quantityProduced - job.totalWaste;
		            }
	           }
	         });
	         modalInstance.result.then(function () {
	        	 $scope.loadStation($scope.stationId);
	           }, function () {});
	     };
	     
	     $scope.openEditLoadTagModal = function (id, job) {
		        var modalInstance = $uibModal.open({
		        	backdrop  : 'static',
		        	keyboard  : false,
		        	animation: true,
		        	templateUrl: './views/editLoadTagModalContent.html',
		        	controller: 'EditLoadTagModalInstanceCtrl',
		        	scope: $scope,
		        	size:'lg',
		        	resolve: {
		        		loadTagId: function () {
			        		return id;
			            },
			            jobId: function () {
			        		return job.jobId;
			            },
			            loadtags: function () {
			        		return null;
			            },
			            part: function () {
			        		return null;
			            },
			            quantityMax: function () {
			        		return job.quantityNeeded - job.quantityProduced - job.totalWaste;
			            }
		          }
		        });
		        modalInstance.result.then(function(selectedItem) {
		        	$scope.loadStation($scope.stationId);
		        }, function () {});

		     };
		     
		     $scope.openViewLoadTagModal = function (id, jobId) {
		    	    var modalInstance = $uibModal.open({
			        	backdrop  : 'static',
			        	keyboard  : false,
			        	animation: true,
			        	templateUrl: './views/viewLoadTagModalContent.html',
			        	controller: 'EditLoadTagModalInstanceCtrl',
			        	scope: $scope,
			        	size:'lg',
			        	resolve: {
			        		loadTagId: function () {
				        		return id;
				            },
				            jobId: function () {
				        		return jobId;
				            },
				            loadtags: function () {
				        		return null;
				            },
				            part: function () {
				        		return null;
				            },
				            quantityMax: function () {
				        		return null;
				            }
			          }
			        });
			        modalInstance.result.then(function(selectedItem) {
			        }, function () {});

			 };
			 
			 $scope.dtOptionsForloadsStatus = DTOptionsBuilder.fromSource().withDOM('rtip').withOption('order', []).withPaginationType('full_numbers').withDisplayLength(5).withLanguage($scope.dataTables);
			 
			 $scope.openLoadsStatusModal = function (loadtags, part) {
		    	    var modalInstance = $uibModal.open({
			        	backdrop  : 'static',
			        	keyboard  : false,
			        	animation: true,
			        	templateUrl: './views/viewLoadsStatusModalContent.html',
			        	controller: 'EditLoadTagModalInstanceCtrl',
			        	scope: $scope,
			        	size:'lg',
			        	resolve: {
			        		loadtags: function () {
				        		return loadtags;
				            },
				            part: function () {
				        		return part;
				            },
				            loadTagId: function () {
				        		return 0;
				            },
				            quantityMax: function () {
				        		return null;
				            }
			            }
			        });
			        modalInstance.result.then(function(selectedItem) {
			        }, function () {});

			 };
			 
			 $scope.updateLoadTagUsedFlag= function(loadTag){
			   		loadTagServiceAjax.updateLoadTag(loadTag).then(function(){
			   			
		   			});
			 };
			 
			 $scope.selectedModeOption = 'STANLY_ONLY';
			 if($rootScope.includeStanly == 'false'){
				 $scope.selectedModeOption = 'PALETT_ONLY'; 
			 }
			  $scope.startResumeMachine = function(machineId, h){
				  //when Hunkeler, confirm production mode option
				  if($scope.stationId == 'PLOWFOLDER' && 
						  $scope.station.machines[h].assignedRolls.length > 0 &&
						  $scope.station.machines[h].assignedRolls[0].jobs[0].productionMode == 'STANLY' &&
						  ($scope.station.machines[h].logs.length == 0 || $scope.station.machines[h].logs[0].event != 'PAUSE')){
					  var modalInstance = $uibModal.open({
		       	           animation: true,
		       	           backdrop  : 'static',
		       	       	   keyboard  : false,
		       	           templateUrl: './views/productionModeOptionsModalContent.html',
		       	           controller: 'productionModeModalInstanceCtrl',
		       	           scope: $scope,
		       	           size:'s',
		       	           resolve: {
		       	        	   machineId: function () {
			    	        	    return machineId;
			       	        	},
			       	        	h: function () {
			    	        	    return h;
			       	        	},
			       	        	selectedModeOption: function () {
			    	        	    return $scope.selectedModeOption;
			       	        	}
		       	           }
	       	          });
	       	          modalInstance.result.then(function (selectedModeOption) {
	       	        	 $scope.selectedModeOption = selectedModeOption;
	       	          }, function () {});
	       	 		  
				  }else{
					  $scope.startResumeMachineTask(machineId, h, $scope.selectedModeOption);
				  }
		     };
			  
		     $scope.startResumeMachineTask = function (machineId, h, selectedModeOption) {
		    	 machineServiceAjax.startResumeMachine(machineId, selectedModeOption).then(function(){
		    		 machineServiceAjax.getMachineById(machineId).then(function(data){
	    				 $scope.station.machines[h] = data;
	    			 });
	    			 $scope.alertMsg = $translate.instant('productionDashboard_js.Machine_Running');
		             toasty.info({
				          title: $translate.instant('productionDashboard_js.Machine_Status'),
				          msg: $translate.instant('productionDashboard_js.Machine_Running'),
				          showClose: true,
		                  clickToClose: true,
		                  timeout: 10000,
		                  sound: false,
		                  html: false,
		                  shake: false,
		                  theme: "bootstrap"
				      });
		              $scope.errorMsgArray[h] = false;
			          $scope.successMsgDown = false;
			      	  $scope.errorMsgDown = false;
		   	     }, function(data, status, headers, config){
		   	    	//$scope.errorMsgArray[h] = true;
		   	    	//$scope.successMsgArray[h] = false;
		            //$scope.errors = data.errors;
	            	SweetAlert.swal($translate.instant('productionDashboard_js.Machine_Status'), data.data.errors.errors, "error");
		          });
				 var chbId = 'sentFlagCheckbox' + machineId;
				 $(":input[id^=chbId]").each(function(){
	        		 $(this).removeAttr('disabled');
	          	  });
		     };
}]);

angular.module('capApp')
.controller('productionModeModalInstanceCtrl', function ($scope, $uibModalInstance, machineId, h, selectedModeOption, $timeout, toasty, $localStorage) {
	
	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	  $scope.machineId = machineId;
	  $scope.selectedModeOption = selectedModeOption;
	  $scope.startResumeMachineTaskDo = function(){
		  $scope.startResumeMachineTask($scope.machineId, h, $scope.selectedModeOption);
		  $uibModalInstance.close();
	  };
	  $scope.closeModal = function(){
		  $uibModalInstance.dismiss();
	  };
	    
});

angular.module('capApp')
.controller('InterruptMachineModalInstanceCtrl', function ($scope, DTOptionsBuilder, DTColumnDefBuilder, $route, $uibModalInstance, $uibModal, logServiceAjax, lookupServiceAjax, machineServiceAjax, jobServiceAjax, rollServiceAjax, SweetAlert, stationId, machineId, rollId, currentJobId, i, toasty,$translatePartialLoader, $translate, $localStorage, $ngConfirm) {

	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	  var alertAddMessage = $translate.instant('productionDashboard_js.Interruption_Handled_Successfully');
	  // $scope.roll = jQuery.extend(true,{}, $scope.defaultRoll);
	  // $scope.roll.status.id = "NEW";
	  // $scope.errors = jQuery.extend({}, $scope.defaultRoll);
	  $scope.$parent.successMsgArray = []; 
	  $scope.$parent.errorMsgArray = []; 
	  /*for(var k = 0; k < $scope.nbMachineCount; k++){
		  $scope.$parent.successMsgArray.push(false);
		  $scope.$parent.errorMsgArray.push(false);
	  }*/
	  $scope.$parent.successMsgDown = false;
	  $scope.$parent.errorMsgDown = false;
	  $scope.errorMsg = false;
	  $scope.successMsg = false;
	  $scope.showCompleteOption = false;
	  $scope.producedRollId = null;
	  $scope.leftOverRollCreated = false;
	  $scope.rollOnProd = null;
	  
	  $scope.defaultLog = {
	    		"machineId":machineId,
	    		"rollId":rollId,
	 		    "event":"",
	 		    "logResult":null,
	 		    "logCause":null,
	 		    "currentJobId":currentJobId,
	 		    "rollLength":"",
	 		    "startTime":"",
	 		    "finishTime":"",
	 		    "counterFeet":""
	  };
	  

	  $scope.dtOptionsForCompletedJobs = DTOptionsBuilder.fromSource().withDOM('rti').withOption('order', []).withPaginationType('full_numbers').withDisplayLength(100).withLanguage($scope.dataTables);

	  $scope.dtColumnDefsForCompletedJobs = [
	                  	                DTColumnDefBuilder.newColumnDef(0).notSortable(),
	                  	                DTColumnDefBuilder.newColumnDef(1).notSortable(),
	                  	                DTColumnDefBuilder.newColumnDef(2).notSortable(),
	                  	                DTColumnDefBuilder.newColumnDef(3).notSortable(),
	                  	                DTColumnDefBuilder.newColumnDef(4).notSortable()
	                  	       ];
	  
	    $scope.log = jQuery.extend({}, $scope.defaultLog);
	    $scope.errors = jQuery.extend({}, $scope.defaultLog);
	   
	    $scope.loadLogCauseOptions = function(){
	    	  lookupServiceAjax.readAll('LogCause').then(function(data){
		            $scope.logCauseOptions = data;
		         });
		};
		$scope.loadLogCauseOptions();
		$scope.logCause = {
			    repeatSelect: null,
			    availableOptions: $scope.logCauseOptions,
		};
		
		machineServiceAjax.getMachineById(machineId).then(function(data){
	        $scope.machine = data;
	        $scope.rollOnProd = data.rollOnProd;
	        jobServiceAjax.getJobById(currentJobId).then(function(data2){
			    $scope.currentJob = data2;
			    if($scope.station.inputType == 'Batch' || $scope.machine.stationId == 'PRESS' || ($scope.machine.rollOnProd != null && $scope.machine.rollOnProd.allJobsComplete) || 
			    		($scope.currentJob != null && $scope.currentJob.quantityProduced >= $scope.currentJob.quantityNeeded)){
			    	$scope.showCompleteOption = true;
			    }
			});
		});
		
		$scope.toggleSelectAll = function(checked){
	       	  //var table = $('#completedJobsTable').DataTable();
	       	  if(checked){
	       		  //table.rows().select();
	       		$(":input[id^='completedJobCheckbox']").prop('checked', true);
	       		$(":input[id^='completedJobQty']").prop('disabled', false);
	       		$("[id^='showrequired']").show();
	       	  }else{
	       		  //table.rows().deselect();
	       		$(":input[id^='completedJobCheckbox']").prop('checked', false);
	       		//$(":input[id^='completedJobQty']").prop('value', '');
	       		$(":input[id^='completedJobQty']").prop('disabled', true);
	       		$("[id^='showrequired']").hide();
	       	  }
	       	  //$scope.toggleProduceButton();
	     };
	     
	     $scope.setAsDisabled = false;
	     $scope.validateProducedQty = function(){
	    	$scope.setAsDisabled = false;
	 		var elmts = $(':input[name^="completedJobQty"]');
	 		for(var x = 0; x < elmts.length; x++){
	 			var jobId = elmts[x].name.substring(15);
	 			var qtyNeededElmt = $('#neededJobQty'+jobId);
	 			if(qtyNeededElmt != null){
	 		    	if(Number(qtyNeededElmt.val()) < elmts[x].value){
	 		    		$scope.setAsDisabled = true;
	 		    		break;
	 		    	}	
	 		    }
	 		}
	 		//if(setAsDisabled){
	 		//	$('#saveBtn').prop('disabled', setAsDisabled);
	 		//}
	 	}
	     
//	     $scope.pauseOptionTrigger = function(){
//	 		$('#counterComplete').prop('disabled', true); $('#counterComplete').val('');
//	 		$('#optionyes1').prop('disabled', true); $('#optionyes1').prop('checked', false);
//	 		$('#optionno1').prop('disabled', true); $('#optionno1').prop('checked', false);
//	 		$('#counterStop').prop('disabled', true); $('#counterStop').val('');
//	 		$('#causeStop').prop('disabled', true); $('#causeStop').val('');
//	 		$('#optionyes2').prop('disabled', true); $('#optionyes2').prop('checked', false);
//	 		$('#optionno2').prop('disabled', true); $('#optionno2').prop('checked', false);
//	 		$('#checkboxAll').prop('disabled', true); $('#checkboxAll').prop('checked', false);
//	 		$(":input[id^='completedJobCheckbox']").prop('disabled', true); 
//	 		//$(":input[id^='completedJobCheckbox']").prop('checked', false);
//	 		$(":input[id^='completedJobQty']").prop('disabled', true); $(":input[id^='completedJobQty']").prop('value', ''); 
//	 		$('#logCause').removeAttr('disabled')
//	 	}
//	     
//	     $scope.completeOptionTrigger = function(){
//	 		$('#logCause').prop('disabled', true); $('#logCause').val('');
//	 		$('#counterComplete').prop('disabled', false); $('#counterComplete').val('');
//	 		$('#optionyes1').prop('disabled', false); $('#optionyes1').prop('checked', false);
//	 		$('#optionno1').prop('disabled', false); $('#optionno1').prop('checked', false);
//	 		$('#counterStop').prop('disabled', true); $('#counterStop').val('');
//	 		$('#causeStop').prop('disabled', true); $('#causeStop').val('');
//	 		$('#optionyes2').prop('disabled', true); $('#optionyes2').prop('checked', false);
//	 		$('#optionno2').prop('disabled', true); $('#optionno2').prop('checked', false);
//	 		$('#checkboxAll').prop('disabled', true); $('#checkboxAll').prop('checked', false);
//	 		$(":input[id^='completedJobCheckbox']").prop('disabled', true); 
//	 		//$(":input[id^='completedJobCheckbox']").prop('checked', false);
//	 		$(":input[id^='completedJobQty']").prop('disabled', true); $(":input[id^='completedJobQty']").prop('value', ''); 
//	 	}
//	     
//	    $scope.stopOptionTrigger = function(){
//	 		$('#logCause').prop('disabled', true); $('#logCause').val('');
//	 		$('#counterComplete').prop('disabled', true); $('#counterComplete').val('');
//	 		$('#optionyes1').prop('disabled', true); $('#optionyes1').prop('checked', false);
//	 		$('#optionno1').prop('disabled', true); $('#optionno1').prop('checked', false);
//	 		$('#counterStop').prop('disabled', false); $('#counterStop').val('');
//	 		$('#causeStop').prop('disabled', false); $('#causeStop').val('');
//	 		$('#optionyes2').prop('disabled', false); $('#optionyes2').prop('checked', false);
//	 		$('#optionno2').prop('disabled', false); $('#optionno2').prop('checked', false);
//	 		$('#checkboxAll').prop('disabled', false); $('#checkboxAll').prop('checked', false);
//	 		$(":input[id^='completedJobCheckbox']").prop('disabled', false);
//	 		//$(":input[id^='completedJobCheckbox']").prop('checked', false);
//	 	}
//	    
	     $scope.openAddRollModal = function () {
	    	 var rollJobs = [];
	    	 $(":input[id^='completedJobCheckbox']").each(function(){
      			  var jobId = $(this).attr('id').substring('completedJobCheckbox'.length);
      			  rollJobs.push(jobId +'_'+ $('#completedJobQty'+jobId).val());
             });
      		 $scope.log.completedJobQtys = rollJobs;
      		 jobServiceAjax.calculateLeftOverRollLength($scope.log).then(function(data){
      		   var modalInstance = $uibModal.open({
		           animation: true,
		           backdrop  : 'static',
		       	   keyboard  : false,
		           templateUrl: './views/addRollModalContent.html',
		           controller: 'AddRollFromProdModalInstanceCtrl',
		           scope: $scope,
		           size:'m',
		           resolve: {
		        	    machineId: function () {
		        			return machineId;
		        		},
				        rollId: function () {
			     			return rollId;
			     		},
			     		leftOverRollLength: function(){
			     			return data;
			     		}
		           }
		        });
	            modalInstance.result.then(function () {
	         	  //vm.dtInstance.reloadData();
	            }, function () {});
      		  });
	     };
	     
	    $scope.handleInterruption = function(){
	    	  $scope.$parent.successMsgDown = false;
	      	  $scope.$parent.errorMsgDown = false;
	      	  // if stop press without creating leftover roll, confirm with operator
	      	  if(stationId == 'PRESS' && $scope.log.event == 'STOP' && angular.isDefined(rollId)){
	      		rollServiceAjax.getLeftOverRoll(rollId).then(function(data){
		            if(data == null || data == ""){
		            	SweetAlert.swal({
		         		   title: $translate.instant('productionDashboard_js.Stop_Jobs'),
		         		   text: $translate.instant('productionDashboard_js.jobs_without_left_over_roll'),
		         		   type: "warning",
		         		   showCancelButton: true,
		         		   confirmButtonColor: "#DD6B55",
		         		   confirmButtonText: $translate.instant('productionDashboard_js.Yes_continue'),
		         		   closeOnConfirm: true},
		         		function(isConfirm){ 
		         			   if (isConfirm) {
		         				  $scope.handleInterruptionAction(false);
		         			   }   
		         		 });
		            }else{
		            	$scope.handleInterruptionAction(false);
		            }
		         });
	      		// if not all jobs on the plow are complete and stop, confirm with operator
	      	  }else if(stationId == 'PLOWFOLDER' && $scope.log.event == 'STOP' && angular.isDefined(rollId) && $scope.rollOnProd != null){
	      		//rollServiceAjax.getRollById(rollId).then(function(data){
	      			var stat = 'ok';
	      			var neededQtys = 0;
	      			var prodQtys = 0;
	      			var missingQty = 0;
	      			var wastedQtys = 0;
	      			for(var x = 0; x < $scope.rollOnProd.jobs.length; x ++){
	      				if($scope.rollOnProd.jobs[x].quantityNeeded > ($scope.rollOnProd.jobs[x].quantityProduced + $scope.rollOnProd.jobs[x].totalWaste)){
	      					stat = '!ok';
	      				}
	      				neededQtys = neededQtys + $scope.rollOnProd.jobs[x].quantityNeeded;
	      				prodQtys = prodQtys + $scope.rollOnProd.jobs[x].quantityProduced;
	      				wastedQtys = wastedQtys + $scope.rollOnProd.jobs[x].totalWaste;
	      			}
	      			missingQty = neededQtys - prodQtys - wastedQtys;
		            if(stat == '!ok'){
		            	$ngConfirm({
							theme: 'modern',
							boxWidth: '50%',
							useBootstrap: false,
							animation: 'zoom',
    						closeAnimation: 'scale',
		                    title: $translate.instant('productionDashboard_js.Missing_qty') + '<br/>' +
		                    				missingQty + ' out of ' + neededQtys,
		                    content: '<ul><li>' + $translate.instant('productionDashboard_js.cancel_txt') + '</li>' +
		                    		 '<li>' + $translate.instant('productionDashboard_js.reschedule_txt') + '</li>' +
		                    		 '<li>' + $translate.instant('productionDashboard_js.missingBooks_txt') + '</li>' + '</ul>',
		                    scope: $scope,
		                    buttons: {
		                    	cancel: function(scope, button){
		                    		 text: $translate.instant('productionDashboard_js.cancel')
			                    },
		                        sayBoo: {
		                            text: $translate.instant('productionDashboard_js.reschedule'),
		                            btnClass: 'btn-blue',
		                            action: function(scope, button){
		                            	$scope.handleInterruptionAction(false);
		                            }
		                        },
		                        somethingElse: {
		                            text: $translate.instant('productionDashboard_js.missingBooks'),
		                            btnClass: 'btn-orange',
		                            action: function(scope, button){
		                            	$scope.handleInterruptionAction(true);
		                            }
		                        }
		                    }
		                });
		            	/*SweetAlert.swal({
		            	   title: $translate.instant('productionDashboard_js.Stop_Jobs'),
		         		   text: $translate.instant('productionDashboard_js.jobs_incomplete'),
		         		   type: "warning",
		         		   showCancelButton: true,
		         		   confirmButtonColor: "#DD6B55",
		         		   cancelButtonText: $translate.instant('productionDashboard_js.cancel'),
		         		   confirmButtonText: $translate.instant('productionDashboard_js.Yes_continue'),
		         		   closeOnConfirm: false,
		         		   closeOnCancel: true},
		         		function(isConfirm){
	         				SweetAlert.swal({
	         	          	   title: $translate.instant('productionDashboard_js.Stop_Jobs'),
	         	       		   text: $translate.instant('productionDashboard_js.jobs_incomplete2'),
	         	       		   type: "warning",
	         	       		   showCancelButton: true,
	         	       		   confirmButtonColor: "#DD6B55",
	         	       		   cancelButtonText: $translate.instant('productionDashboard_js.reschedule'),
	         	       		   confirmButtonText: $translate.instant('productionDashboard_js.missingBooks'),
	         	       		   closeOnConfirm: true,
	         	       		   closeOnCancel: true},
	         	       		function(isConfirmOk){ 
	         	       			   if (isConfirmOk) {
	         	       				   $scope.handleInterruptionAction(true);
	         	       			   }else{
	         	       				   $scope.handleInterruptionAction(false);
	         	       			   }
	         	       	    });
		         		});*/
		            }else{
		            	$scope.handleInterruptionAction(false);
		            }
		        // });
	      		// if job not complete and stop, confirm with operator
	      	  }else if((stationId != 'PRESS' && stationId != 'PLOWFOLDER') && $scope.log.event == 'STOP'){
	      		  if($scope.currentJob.quantityNeeded > ($scope.currentJob.quantityProduced + $scope.currentJob.totalWaste)){
	      			/*SweetAlert.swal({
		            	   title: $translate.instant('productionDashboard_js.Stop_Jobs'),
		         		   text: $translate.instant('productionDashboard_js.jobs_incomplete'),
		         		   type: "warning",
		         		   showCancelButton: true,
		         		   confirmButtonColor: "#DD6B55",
		         		   cancelButtonText: $translate.instant('productionDashboard_js.cancel'),
		         		   confirmButtonText: $translate.instant('productionDashboard_js.Yes_continue'),
		         		   closeOnConfirm: false,
		         		   closeOnCancel: true},
		         		function(isConfirm){
	         				SweetAlert.swal({
	         	          	   title: $translate.instant('productionDashboard_js.Stop_Jobs'),
	         	       		   text: $translate.instant('productionDashboard_js.job_incomplete2'),
	         	       		   type: "warning",
	         	       		   showCancelButton: true,
	         	       		   confirmButtonColor: "#DD6B55",
	         	       		   cancelButtonText: $translate.instant('productionDashboard_js.reschedule'),
	         	       		   confirmButtonText: $translate.instant('productionDashboard_js.missingBooks'),
	         	       		   closeOnConfirm: true,
	         	       		   closeOnCancel: true},
	         	       		function(isConfirmOk){ 
	         	       			   if (isConfirmOk) {
	         	       				   	$scope.handleInterruptionAction(true);
	         	       			   }else{
	         	       				    $scope.handleInterruptionAction(false);
	         	       			   }
	         	       	    });
		         		});*/
	      			  var missingQty = $scope.currentJob.quantityNeeded - $scope.currentJob.quantityProduced - $scope.currentJob.totalWaste;
		      			$ngConfirm({
		      				theme: 'modern',
		      				boxWidth: '50%',
							useBootstrap: false,
							animation: 'zoom',
    						closeAnimation: 'scale',
		                    title: $translate.instant('productionDashboard_js.Missing_qty') + '<br/>' +
		                    				missingQty + ' out of ' + $scope.currentJob.quantityNeeded,
		                    content: '<ul><li>' + $translate.instant('productionDashboard_js.cancel_txt') + '</li>' +
		                    		 '<li>' + $translate.instant('productionDashboard_js.reschedule_txt') + '</li>' +
		                    		 '<li>' + $translate.instant('productionDashboard_js.missingBooks_txt') + '</li>' + '</ul>',
		                    scope: $scope,
		                    buttons: {
		                    	cancel: function(scope, button){
		                    		 text: $translate.instant('productionDashboard_js.cancel')
			                    },
		                        sayBoo: {
		                            text: $translate.instant('productionDashboard_js.reschedule'),
		                            btnClass: 'btn-blue',
		                            action: function(scope, button){
		                            	$scope.handleInterruptionAction(false);
		                            }
		                        },
		                        somethingElse: {
		                            text: $translate.instant('productionDashboard_js.missingBooks'),
		                            btnClass: 'btn-orange',
		                            action: function(scope, button){
		                            	$scope.handleInterruptionAction(true);
		                            }
		                        }
		                    }
		                });
	      		  }else{
	  	      		$scope.handleInterruptionAction(false);
		      	  }
	      	  }else{
	      		$scope.handleInterruptionAction(false);
	      	  }
		  };
		  
		  $scope.handleInterruptionAction = function(missingUnitsExist){
			  //Consider the completed jobs
			  var rollJobs = [];
			  if($scope.log.event == 'STOP' || $scope.log.event == 'COMPLETE'){
				  if(stationId == 'PRESS' || stationId == 'PLOWFOLDER'){
		       		  $(":input[id^='completedJobCheckbox']").each(function(){
		       			  var jobId = $(this).attr('id').substring('completedJobCheckbox'.length);
		       			  if(missingUnitsExist){
		       				  var missingQty = $('#neededJobQty'+jobId).val() - $('#completedJobQty'+jobId).val() - $('#wastedJobQty'+jobId).val();
		       				  if(missingQty > 0){
		       					  rollJobs.push(jobId +'_'+ $('#completedJobQty'+jobId).val() + '_' + missingQty);
		       				  }else{
		       					rollJobs.push(jobId +'_'+ $('#completedJobQty'+jobId).val() + '_0');
		       				  }
		       			  }else{
		       				  rollJobs.push(jobId +'_'+ $('#completedJobQty'+jobId).val() + '_0');
		       			  }
		              });
		       		  $scope.log.completedJobQtys = rollJobs;
		       	   }else{
		       		  if(missingUnitsExist){
	       				  var missingQty = $scope.currentJob.quantityNeeded - $scope.currentJob.quantityProduced - $scope.currentJob.totalWaste;
		       		      $scope.log.completedJobQtys = [$scope.log.currentJobId +'_'+ $scope.currentJob.quantityProduced +'_'+ missingQty];
		       		  }else{
		       			  $scope.log.completedJobQtys = [$scope.log.currentJobId +'_'+ $scope.currentJob.quantityProduced +'_0'];
		       		  }
		       	   }
			   }
	    	   logServiceAjax.handleInterruption($scope.log)
		          .then( function(data, status, headers, config){
		              $scope.errors = jQuery.extend({}, $scope.defaultLog);
		              $scope.$parent.alertMsg = alertAddMessage;
		              //$scope.$parent.successMsgArray[i] = true;
		              toasty.success({
		                  title: $translate.instant('productionDashboard_js.Interruption'),
		                  msg: $translate.instant('productionDashboard_js.Interruption') +'(' + $scope.log.event + ')'+ $translate.instant('productionDashboard_js.Handled_Successfully'),
		                  showClose: true,
		                  clickToClose: true,
		                  timeout: 10000,
		                  sound: false,
		                  html: false,
		                  shake: false,
		                  theme: "bootstrap"
		              });
		              if(stationId == 'PRESS' && $scope.log.event != 'PAUSE'){
		            	  rollServiceAjax.getProducedRoll(rollId).then( function(data){
		            		  if(data != null && data.rollId != null){
		            			  $scope.producedRollId = data.rollId;
		            			  $('#cancelBtn').removeAttr('disabled');
		            		  }else{
		            			  $uibModalInstance.close();
					              //$scope.loadStation($scope.stationId);  //refresh the Dashboard data
					              // TODO is there better way rather than using reload route?
					              $route.reload();
		            		  }
		            	  });
		              }else{
		            	  $uibModalInstance.close();
			              //$scope.loadStation($scope.stationId);  //refresh the Dashboard data
			              // TODO is there better way rather than using reload route?
			              $route.reload();
		              }
		              
		          }, function(data, status, headers, config){
		            $scope.errorMsg = true;
		            $scope.successMsg = false;
		            $scope.errors = data.errors;
		          });
		   };
		  
		  $scope.closeModal = function(){
			   $uibModalInstance.dismiss();
			   if($scope.producedRollId != null){
				// TODO is there better way rather than using reload route?
		              $route.reload();
			   }
		  };
});

angular.module('capApp')
.controller('AddRollFromProdModalInstanceCtrl', function ($scope, $uibModalInstance, $uibModal, rollServiceAjax, machineServiceAjax, lookupServiceAjax, machineId, rollId, leftOverRollLength, toasty,$translatePartialLoader, $translate, $localStorage) {

      $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	  var alertAddMessage = $translate.instant('productionDashboard_js.Leftover_Roll_Added');
	  $scope.keepOnMachine = false;
	  machineServiceAjax.getMachineById(machineId).then(function(data){
		  if(data.assignedRolls.length == 0){
			  $scope.keepOnMachine = true;
		  }
	  });
	  $scope.defaultRoll = {
	 		    "machineId":"",
	 		    "machineOrdering":"",
	 		    "rollType":{"id":"LEFTOVER"},
	 		    "parentRollId":rollId,
	 		    "length":"",
	 		    "width":"",
	 		    "weight":"",
	 		    "paperType":{"id":""},
	 		    "status":{"id":"AVAILABLE"},
	 		    "hours":"",
	 		    "utilization":""
	   };
	  
	   $scope.loadRollStatusOptions = function(){
	    	  lookupServiceAjax.readAll('RollStatus').then(function(data){
		            $scope.rollStatusOptions = data;
		         });
		};
		$scope.loadRollStatusOptions();
		$scope.rollStatus = {
			    repeatSelect: null,
			    availableOptions: $scope.rollStatusOptions,
		};
		
		$scope.loadRollTypeOptions = function(){
	    	  lookupServiceAjax.readAll('RollType').then(function(data){
		            $scope.rollTypeOptions = data;
		         });
		};
		$scope.loadRollTypeOptions();
		$scope.rollType = {
			    repeatSelect: null,
			    availableOptions: $scope.rollTypeOptions,
		};
		
		$scope.loadPaperTypeOptions = function(){
	    	  lookupServiceAjax.readAll('PaperType').then(function(data){
		            $scope.paperTypeOptions = data;
		         });
		};
		$scope.loadPaperTypeOptions();
		$scope.paperType = {
			    repeatSelect: null,
			    availableOptions: $scope.paperTypeOptions,
		};
		$scope.parentRollLength = 1;
		$scope.rollMinLength = 1;
		lookupServiceAjax.getItemById('Preference', 'ROLLMINLENGTH').then(function(data){
		 	$scope.rollMinLength = data.name;
		});
		
	 	rollServiceAjax.getRollById(rollId).then(function(data){
	 		 $scope.roll.parentRollId = rollId;
	 		 $scope.roll.width = data.width;
	 		 $scope.roll.paperType.id = data.paperType.id;
	 		 $scope.parentRollLength = data.length;
	 		 /*if(interruptionType == 'COMPLETE'){
	 			$scope.roll.length = parseInt((data.length * (1 - data.utilization / 100)).toFixed(2));
	 		 }else{
	 			rollServiceAjax.calculateLeftOverRollLength().then(function(data){
	 				$scope.roll.length = data;
	 			});
	 		 }*/
	 		 $scope.roll.length = Number(leftOverRollLength);
	 	});
	  
	   $scope.roll = jQuery.extend(true,{}, $scope.defaultRoll);
	  
	   $scope.loadMachineOptions = function(){
	 		$scope.machineOptions = [];
	 		machineServiceAjax.machines().then(function(data){
	 			for(var x = 0; x < data.length; x ++){
	 				if(data[x].machineId == machineId){//only load the current machine for this left over roll 
	 					$scope.machineOptions.push(data[x]);
	 					if(data[x].assignedRolls.length == 0){//pre-set the machine if no roll is assigned to that machine (in the queue)
	 						$scope.roll.machineId = machineId;
	 					}
	 				}
	 			}
	 		});
	 	};
	 	//$scope.loadMachineOptions();
	 	$scope.rollMachine = {
	 	    repeatSelect: null,
	 		availableOptions: $scope.machineOptions,
	 	};
	 	
	  $scope.errors = jQuery.extend({}, $scope.defaultRoll);
	  $scope.$parent.successMsg = false;
	  $scope.$parent.errorMsg = false;
	  $scope.errorMsg = false;
	     
	  $scope.addRoll = function(){
		if($('input[name=keepFlag]:checked').val() == 'true'){
			$scope.roll.machineId = machineId;
			$scope.roll.status.id = 'ASSIGNED';
			$scope.roll.machineOrdering = 0;
		}
  	    rollServiceAjax.addRoll($scope.roll)
	          .then(function(data, status, headers, config){
	        	  $scope.errors = jQuery.extend({}, $scope.defaultRoll);
	              $scope.$parent.alertMsg = $translate.instant('productionDashboard_js.Leftover_Rol_with_Id') + data + $translate.instant('productionDashboard_js.Successfully_Added');
	              //$scope.$parent.successMsg = true;
	              $scope.roll.rollId = data;
	              toasty.success({
	                  title: $translate.instant('productionDashboard_js.Adding_Roll'),
	                  msg: $translate.instant('productionDashboard_js.Leftover_Rol_with_Id') + data + $translate.instant('productionDashboard_js.Successfully_Added'),
	                  showClose: true,
	                  clickToClose: true,
	                  timeout: 10000,
	                  sound: false,
	                  html: false,
	                  shake: false,
	                  theme: "bootstrap"
	              });
	              //$uibModalInstance.close();
	              $scope.leftOverRollCreated = true;
	          }, function(data, status, headers, config){
	            $scope.errorMsg = true;
	            $scope.successMsg = false;
	            $scope.errors = data.errors;
	          });
  	  
	  };
	  $scope.closeModal = function(){
		  if($scope.leftOverRollCreated){
			  $('#cancelBtn').attr('disabled', 'disabled');
		  }
		 $uibModalInstance.dismiss();
	  };
	  
});
angular.module('capApp')
.controller('BatchModalInstanceCtrl', function ($scope, batchId, $uibModalInstance, $uibModal, DTOptionsBuilder, batchServiceAjax, toasty,$translatePartialLoader, $translate, $localStorage,SweetAlert,) {

	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	  $scope.errors = jQuery.extend({}, $scope.defaultRoll);
	  $scope.$parent.successMsg = false;
	  $scope.$parent.errorMsg = false;
	  $scope.errorMsg = false;
	  $scope.loadBatch = function (batchId) {
		if (isNumber(batchId)) {
			batchServiceAjax.getBatchById(batchId).then(function (data) {
				$scope.batch = data;
			});
		}
	  };
	  $scope.loadBatch(batchId);
	  function isNumber(n) {
		return !isNaN(parseFloat(n)) && isFinite(n);
	  }
	  $scope.closeModal = function(){
		  $uibModalInstance.dismiss();
	  };
	  $scope.dtOptionsForSectionsOnBatch = DTOptionsBuilder.fromSource().withDOM('frtip').withOption('order', []).withPaginationType('full_numbers').withDisplayLength(5).withLanguage($scope.dataTables);
		$scope.reCreateBatch = function (batchId) {
			batchServiceAjax.reCreateBatch(batchId).then(function (data) {
				batchServiceAjax.getBatchById(batchId).then(function (newBatch) {
					$scope.batch = newBatch;
				});
				if (data == true) {
					toasty.success({
						title: $translate.instant('productionDashboard_js.Handled_Successfully'),
						msg: $translate.instant('productionDashboard_js.Rerun_Batch_Successfully') + '(Batch ' + batchId + ')',
						showClose: true,
						clickToClose: true,
						timeout: 10000,
						sound: false,
						html: false,
						shake: false,
						theme: "bootstrap"
					});
				}
				else {
					$scope.alertMsg = $translate.instant('productionDashboard_js.cannot_recreate_batch');
					SweetAlert.swal($translate.instant('productionDashboard_js.recreation_interruption'), $scope.alertMsg, "error");
				}


			}, function (data, status, headers, config) {
				//$scope.errorMsgDown = true;
				//$scope.successMsgDown = false;
				//$scope.errors = data.errors;
				SweetAlert.swal($translate.instant('productionDashboard_js.Machine_Status'), data.data.errors.errors, "error");
			});

		}
		$scope.openViewSectionModal = function (id) {
			var modalInstance = $uibModal.open({
				backdrop: 'static',
				keyboard: false,
				animation: true,
				templateUrl: './views/viewSectionModalContent.html',
				controller: 'SectionModalInstanceCtrl',
				scope: $scope,
				size: 'lg',
				resolve: {
					sectionId: function () {
						return id;
					}
				}
			});
			modalInstance.result.then(function (selectedItem) {
			}, function () { });
		};

	});
angular.module('capApp')
.controller('SectionModalInstanceCtrl', function ($scope, sectionId, $uibModalInstance, $uibModal, DTOptionsBuilder, batchServiceAjax, toasty,$translatePartialLoader, $translate, $localStorage, SweetAlert) {

	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  $scope.token = token;
	  $scope.errors = jQuery.extend({}, $scope.defaultRoll);
	  $scope.$parent.successMsg = false;
	  $scope.$parent.errorMsg = false;
	  $scope.errorMsg = false;
	  $scope.loadSection = function (sectionId) {
		if (isNumber(sectionId)) {
			batchServiceAjax.getSectionById(sectionId).then(function (data) {
				$scope.section = data;
			});
		} else {
			batchServiceAjax.getSectionByName(sectionId).then(function (data) {
				$scope.section = data;
			});
		}
	  };
	  $scope.loadSection(sectionId);
	  function isNumber(n) {
		return !isNaN(parseFloat(n)) && isFinite(n);
	  }
	  $scope.closeModal = function(){
		  $uibModalInstance.dismiss();
	  };
	  $scope.dtOptionsForJobsOnSection = DTOptionsBuilder.fromSource().withDOM('frtip').withOption('order', []).withPaginationType('full_numbers').withDisplayLength(5).withLanguage($scope.dataTables);

	  $scope.reCreateSection = function(sectionId){
		batchServiceAjax.reCreateSection(sectionId).then(function(data){
			if (data && data.length != 0) {			
			$scope.alertMsg = $translate.instant('productionDashboard_js.cannot_recreate_section')+data;
			SweetAlert.swal($translate.instant('productionDashboard_js.recreation_interruption'), $scope.alertMsg, "error");
			}
			else {
				toasty.success({
					title: $translate.instant('productionDashboard_js.Handled_Successfully'),
					msg: $translate.instant('productionDashboard_js.Rerun_Section_Successfully') +'(section ' + sectionId + ')',
					showClose: true,
					clickToClose: true,
					timeout: 10000,
					sound: false,
					html: false,
					shake: false,
					theme: "bootstrap"
				});
			}

		}, function(data, status, headers, config){
			$scope.errorMsgDown = true;
			$scope.successMsgDown = false;
			$scope.errors = data.errors;
		  });
	
	 }
	 $scope.openViewBatchModal = function (id) {
		var modalInstance = $uibModal.open({
			   backdrop  : 'static',
			   keyboard  : false,
			   animation: true,
			   templateUrl: './views/viewBatchModalContent.html',
			   controller: 'BatchModalInstanceCtrl',
			   scope: $scope,
			   size:'lg',
			   resolve: {
				   batchId: function () {
				   return id;
			   }
			 }
		   });
		   modalInstance.result.then(function(selectedItem) {
		   }, function () {});
	  };	  
	  $scope.openViewJobModal = function (id) {
		   var modalInstance = $uibModal.open({
	        	backdrop  : 'static',
	        	keyboard  : false,
	        	animation: true,
	        	templateUrl: './views/viewJobModalContent.html',
	        	controller: 'EditJobModalInstanceCtrl',
	        	scope: $scope,
	        	size:'lg',
	        	resolve: {
	        		jobId: function () {
	        		return id;
	            }
	          }
	        });
	        modalInstance.result.then(function(selectedItem) {
	        }, function () {});
	   };
	     
});
