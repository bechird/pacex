'use strict';
/**
 * @ngdoc function
 * @name capApp.controller:JobsCtrl
 * @description
 * # JobsCtrl
 * Controller of the capApp
 */
angular.module('capApp')
.controller('JobsListCtrl', ['DTOptionsBuilder','DTColumnBuilder', '$scope', '$log', 'jobServiceAjax', 'orderServiceAjax', 'partServiceAjax', 'SweetAlert', 'stationServiceAjax', 'machineServiceAjax', 
			'rollServiceAjax', 'lookupServiceAjax', '$confirm', '$uibModal', '$filter','toasty', '$translatePartialLoader', '$translate', '$localStorage','$rootScope', '$http', 
    function (DTOptionsBuilder, DTColumnBuilder, $scope, $log, jobServiceAjax, orderServiceAjax, partServiceAjax, SweetAlert, stationServiceAjax, machineServiceAjax, rollServiceAjax, lookupServiceAjax, 
    			  $confirm, $uibModal, $filter,toasty ,$translatePartialLoader, $translate, $localStorage,$rootScope, $http) {
      
	 $translatePartialLoader.addPart('jobsList');
     $translate.refresh();
	
     $.fn.dataTable.ext.errMode = 'none';
     var token = $localStorage.oauthToken;
	
	    var vm = this;
	    $scope.addLoadTagfromJob = true;
        $scope.successMsg = false;
	    $scope.errorMsg = false;
	    $scope.defaultJob = {
	 		    "machineId":"",
	 		    "orderId":"",
	 		    "partNum":"",
	 		    "jobStatus":{"id":""},
	 		    "stationId":"",
	 		    "productionOrdering":"",
	 		    "rollOrdering":"",
	 		    "machineOrdering":"",
	 		    "hours":"",
	 		    "rollId":"",
	 		    "jobType":{"id":""},
	 		    "fileSentFlag":"",
	 		    "jobPriority":{"id":""},
	 		    "quantityProduced":""
	 	 };
	    
	    vm.dtInstance = {};

	    
       
	    var serverData = function(sSource, aoData, fnCallback, oSettings) {
           $http.post($rootScope.API_BASE+"/jobs/paginated",{aoData}).then(function(result){
        	   
        	   var records = {
                       'draw': result.data.draw,
                       'recordsTotal': result.data.recordsTotal,
                       'recordsFiltered': result.data.recordsFiltered,
                       'data': result.data.data  
                   };
        	   
               fnCallback(records);
           });
       }
       
       
       
	       
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
	    
       vm.dtOptions = DTOptionsBuilder.newOptions()
       	  .withFnServerData(serverData)
          .withDOM('frtip')          
          .withPaginationType('full_numbers')
          .withOption('responsive', true)
          .withOption('processing', true)
          .withOption('serverSide', true) // for server side processing 
          .withDisplayLength(20)
          .withOption('order', [0, 'desc'])
          .withButtons(['print', 'csv'])
          .withColumnFilter({
              
              aoColumns: [
               {
                  type: 'number'    //jobId                
              },{
            	  type: 'number'   //orderId
              },{
                  type: 'text',   //partNum
                  bRegex: true,
                  bSmart: true
              },{
            	  type: 'number'   //rollId
              },{
                  type: 'text',   //status id 
                  bRegex: true,
                  bSmart: true
              },{
                  type: 'text',   // station Name
                  bRegex: true,
                  bSmart: true
              },{
              	type: 'text',   //split level
                  bRegex: true,
                  bSmart: true
              },{
            	  type: 'number'  //hours
              },{
            	  type: 'number'  // quantity needed
              },{
            	  type: 'number'  // quantity produced
              },{
            	  type: 'number'  // quantity wasted
              }]
          })
          .withLanguage($scope.dataTables);

       vm.dtColumns = [
          DTColumnBuilder.newColumn('jobId').withTitle($translate.instant('JOBS_JS.JOB_ID')).withClass('text-center'),
          DTColumnBuilder.newColumn('orderId').withTitle($translate.instant('JOBS_JS.ORDER_ID')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('partNum').withTitle($translate.instant('JOBS_JS.Part')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('rollId').withTitle($translate.instant('editJobModal.ROLL')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('jobStatus.id').withTitle( $translate.instant('JOBS_JS.STATUS')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('stationId').withTitle($translate.instant('JOBS_JS.Station')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('splitLevel').withTitle($translate.instant('JOBS_JS.SPLIT_LEVEL')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('hours').withTitle($translate.instant('JOBS_JS.HOURS')).withOption('defaultContent', ' ').
          renderWith(function (data) {
              return $filter('number')(data, 2);
          }),
          DTColumnBuilder.newColumn('quantityNeeded' ).withTitle($translate.instant('JOBS_JS.NEEDES_QTY')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('quantityProduced').withTitle($translate.instant('JOBS_JS.PRODUCED_QTY')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('totalWaste').withTitle($translate.instant('JOBS_JS.WASTED_QTY')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('jobId').withTitle(' ').notSortable().renderWith(function (data) {
        	  return "<div style='display:flex;'><button type='button'  uib-tooltip='Edit'  title='"+$translate.instant('JOBS_JS.EDIT')+"'  onclick=\"var $scope = angular.element(event.target).scope(); $scope.openEditJobModal('"+data+"'); $scope.$apply()\" class='btn bgm-orange-900 waves-circle waves-effect'><i class='zmdi zmdi-edit'></i></button>&nbsp;<button type='button' onclick=\"var $scope = angular.element(event.target).scope(); $scope.deleteJob('"+data+"'); $scope.$apply()\" uib-tooltip='Delete' title='"+$translate.instant('JOBS_JS.DELETE_js')+"' class='btn bgm-deeporange waves-circle waves-effect'><i class='zmdi zmdi-delete'></i></button></div>";
          })
      ];
      
    /*  $scope.deleteJob = function(id){
    	  $scope.errorMsg = false;
          $scope.successMsg = false;
    	  $confirm({text: 'Are you sure you want to delete the job?', title: 'Delete Job', ok: 'Ok', cancel: 'Cancel'})
	        .then(function() {
	        	jobServiceAjax.deleteJob(id).then(function(data){
	          $scope.jobs = data;
            vm.dtInstance.reloadData();
            $scope.successMsg = true;
            $scope.errorMsg = false;
            $scope.alertMsg = "Job successfully deleted!";
    	  }, function(data, status, headers, config){
	            $scope.errorMsg = true;
	            $scope.successMsg = false;
	            $scope.alertMsg = data.errors.errors;
	      });
	     });
	   };*/
       $scope.deleteJob = function(id){
     	  $scope.errorMsg = false;
           $scope.successMsg = false;
          
       SweetAlert.swal({
 		   title:  $translate.instant('JOBS_JS.DELETE_JOB'), 
 		   text: $translate.instant('JOBS_JS.SURE_DELETE_JOB'),
 		   type: "warning",
 		   showCancelButton: true,
 		   confirmButtonColor: "#DD6B55",
 		   confirmButtonText:  $translate.instant('JOBS_JS.DELETE'),
 		   closeOnConfirm: true},
 		function(isConfirm){ 
 			  if (isConfirm) {
	 			   jobServiceAjax.deleteJob(id).then(function(data){
	 	           $scope.jobs = data;
	               vm.dtInstance.reloadData();
 	               toasty.success({
		                  title: $translate.instant('JOBS_JS.DELETE_PART'),
		                  msg: $translate.instant('JOBS_JS.then_DELETE') ,
		                  showClose: true,
		                  clickToClose: true,
		                  timeout: 5000,
		                  sound: false,
		                  html: false,
		                  shake: false,
		                  theme: "bootstrap"
		              });
 		    	    }, function(data, status, headers, config){
 			           // $scope.errorMsg = true;
 			          //  $scope.successMsg = false;
 			           // $scope.alertMsg = data.errors.errors;
 		    	    	 SweetAlert.swal($translate.instant('JOBS_JS.DELETION_ERROR'), data.data.errors.errors, "error");
 			        });
 			   }   
 		 });
     };
	    
	    $scope.openAddJobModal = function () {
	         var modalInstance = $uibModal.open({
	           animation: true,
	           backdrop  : 'static',
	       	   keyboard  : false,
	           templateUrl: './views/addJobModalContent.html',
	           controller: 'AddJobModalInstanceCtrl',
	           scope: $scope,
	           size:'xl',
	           resolve: {
	             
	           }
	         });
	         modalInstance.result.then(function () {
	         	vm.dtInstance.reloadData();
	           }, function () {});
	     };
	   
	    $scope.openEditJobModal = function (id) {
	        var modalInstance = $uibModal.open({
	        	backdrop  : 'static',
	        	keyboard  : false,
	        	animation: true,
	        	templateUrl: './views/editJobModalContent.html',
	        	controller: 'EditJobModalInstanceCtrl',
	        	scope: $scope,
	        	size:'xl',
	        	resolve: {
	        		jobId: function () {
	        		return id;
	            }
	          }
	        });
	        modalInstance.result.then(function(selectedItem) {
	        	vm.dtInstance.reloadData();
	        }, function () {});

	     };
	     
	 	 $scope.job = jQuery.extend({}, $scope.defaultJob);
	 		  
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
	 	    
	 	    $scope.loadJobStatusOptions = function(){
	 	    	  lookupServiceAjax.readAll('JobStatus').then(function(data){
	 		            $scope.jobStatusOptions = data;
	 		         });
	 		};
	 		$scope.loadJobStatusOptions();
	 		$scope.status = {
	 			    repeatSelect: null,
	 			    availableOptions: $scope.jobStatusOptions,
	 		};
	 		
	 		$scope.loadJobTypeOptions = function(){
	 	    	  lookupServiceAjax.readAll('JobType').then(function(data){
	 		            $scope.jobTypeOptions = data;
	 		         });
	 		};
	 		$scope.loadJobTypeOptions();
	 		$scope.jobType = {
	 			    repeatSelect: null,
	 			    availableOptions: $scope.jobTypeOptions,
	 		};
	 		
	 		/*$scope.loadOrderOptions = function(){
		 		orderServiceAjax.orders().then(function(data){
		 			$scope.orderOptions = data;
		 		});
		 	};
		 	$scope.loadOrderOptions();
		 	$scope.jobOrder = {
		 	    repeatSelect: null,
		 		availableOptions: $scope.orderOptions,
		 	};
	 		
		 	$scope.loadPartOptions = function(){
		 		partServiceAjax.parts().then(function(data){
		 			$scope.partOptions = data;
		 		});
		 	};
		 	$scope.loadPartOptions();
		 	$scope.job.part = {
		 	    repeatSelect: null,
		 		availableOptions: $scope.partOptions,
		 	};*/
	 		
		 	$scope.loadStationOptions = function(){
		 		stationServiceAjax.stationsQuick().then(function(data){
		 			$scope.stationOptions = data;
		 		});
		 	};
		 	$scope.loadStationOptions();
		 	$scope.job.station = {
		 	    repeatSelect: null,
		 		availableOptions: $scope.stationOptions,
		 	};
		 	
		 	/*$scope.loadMachineOptions = function(){
		 		machineServiceAjax.machines().then(function(data){
		 			$scope.machineOptions = data;
		 		});
		 	};
		 	$scope.loadMachineOptions();
		 	$scope.job.machine = {
		 	    repeatSelect: null,
		 		availableOptions: $scope.machineOptions,
		 	};
		 	
		 	$scope.loadRollOptions = function(){
		 		rollServiceAjax.rolls().then(function(data){
		 			$scope.rollOptions = data;
		 		});
		 	};
		 	$scope.loadRollOptions();
		 	$scope.job.roll = {
		 	    repeatSelect: null,
		 		availableOptions: $scope.rollOptions,
		 	};*/
		 	
}]);


angular.module('capApp')
.controller('AddJobModalInstanceCtrl', function ($scope, $uibModalInstance, $uibModal, jobServiceAjax, orderServiceAjax, partServiceAjax, lookupServiceAjax, toasty ,$translatePartialLoader, $translate, $localStorage) {

	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	  var alertAddMessage = $translate.instant('JOBS_JS.JOB_ADDED');
	  $scope.job = jQuery.extend({}, $scope.defaultJob);
	  
	  //$scope.job.jobStatusId = "NEW";
	  $scope.errors = jQuery.extend({}, $scope.defaultJob);
	  $scope.$parent.thenMsg = false;
	  $scope.$parent.errorMsg = false;
	  $scope.errorMsg = false;

	  $scope.addJob = function(){
		if($scope.job.hours == null){
			$scope.job.hours = 0;
		}
		if($scope.job.quantityProduced == null){
			$scope.job.quantityProduced = 0;
		}
  	    jobServiceAjax.addJob($scope.job)
	          .then( function(data, status, headers, config){
	              $scope.errors = jQuery.extend({}, $scope.defaultJob);
	              $scope.$parent.alertMsg = alertAddMessage;
	             // $scope.$parent.thenMsg = true;
	              toasty.success({
	                  title: $translate.instant('JOBS_JS.ADING_JOB'),
	                  msg:  $translate.instant('JOBS_JS.JOB_SUCC_ADDED'),
	                  showClose: true,
	                  clickToClose: true,
	                  timeout: 10000,
	                  sound: false,
	                  html: false,
	                  shake: false,
	                  theme: "bootstrap"

	              });
	              $uibModalInstance.close();
	          }, function(data, status, headers, config){
	            $scope.errorMsg = true;
	            $scope.successMsg = false;
	            $scope.errors = data.errors;
	          });
	  };
	  $scope.closeModal = function(){
		 $uibModalInstance.dismiss();
	  };
	  
});

angular.module('capApp')
.controller('EditJobModalInstanceCtrl', function ($scope, $uibModalInstance, $uibModal, SweetAlert, DTOptionsBuilder, jobServiceAjax, orderServiceAjax, partServiceAjax, stationServiceAjax, machineServiceAjax, loadTagServiceAjax, lookupServiceAjax, jobId, toasty,$translatePartialLoader, $translate, $localStorage) {

	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	  
	  var alertUpdateMessage = $translate.instant('JOBS_JS.JOB_UPDATED');
	  $scope.job = jQuery.extend({}, $scope.defaultJob);
	  $scope.errors = jQuery.extend({}, $scope.defaultJob);
	  $scope.$parent.thenMsg = false;
	  $scope.$parent.errorMsg = false;
	  $scope.errorMsg = false;
	  
	  $scope.loadStationOptions = function(){
	 		stationServiceAjax.stationsQuick().then(function(data){
	 			$scope.stationOptions = data;
	 		});
	  };
	  $scope.loadStationOptions();
		    
	  jobServiceAjax.getJobById(jobId).then(function(data){
	        $scope.job = data;
	        
	        $scope.updateJob = function(){
	        	if($scope.job.hours == null){
	    			$scope.job.hours = 0;
	    		}
	        	if($scope.job.quantityProduced == null){
	    			$scope.job.quantityProduced = 0;
	    		}
	        	jobServiceAjax.updateJob($scope.job)
	            .then(function(){
	          	$scope.alertMsg = alertUpdateMessage;
	              $scope.errors = jQuery.extend({}, $scope.defaultJob);
	              $scope.$parent.alertMsg = alertUpdateMessage;
	             // $scope.$parent.thenMsg = true;
	              toasty.success({
	                  title: $translate.instant('JOBS_JS.UPDATING_JOB'),
	                  msg: 'Job ' + $scope.job.jobId + $translate.instant('JOBS_JS.UPDATED'),
	                  showClose: true,
	                  clickToClose: true,
	                  timeout: 10000,
	                  sound: false,
	                  html: false,
	                  shake: false,
	                  theme: "bootstrap"
	              });
	              $uibModalInstance.close();
	             },function(data, status, headers, config){
	            	$scope.errorMsg = true;
	            	$scope.successMsg = false;
	                $scope.errors = data.errors;
	            }) ;
	        };
	  	});
	   $scope.closeModal = function(){
		  $uibModalInstance.dismiss();
	   };
	   $scope.dtOptionsForLoadTagsOnJob = DTOptionsBuilder.fromSource().withDOM('frtip').withOption('order', []).withPaginationType('full_numbers').withDisplayLength(5).withLanguage($scope.dataTables);

	   $scope.openAddLoadTagModal = function (jobId) {
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
		        		return jobId;
		            },
		            finishTime: function () {
		        		return null;
		            },
		            startTime: function () {
		            	return null;
		            },
		            quantityMax: function () {
		            	return $scope.job.quantityNeeded - $scope.job.quantityProduced - $scope.job.totalWaste;
		            }
	           }
	         });
	         modalInstance.result.then(function () {
	        	 jobServiceAjax.getJobById(jobId).then(function(data){
		    	        $scope.job = data;
		        	});
	           }, function () {});
	     };
	     
	  	$scope.openEditLoadTagModal = function (id) {
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
	        		loadtags: function () {
		        		return null;
		            },
		            part: function () {
		        		return null;
		            },
		            quantityMax: function () {
		            	return $scope.job.quantityNeeded - $scope.job.quantityProduced - $scope.job.totalWaste;
		            }
	          }
	        });
	        modalInstance.result.then(function(selectedItem) {
	        	jobServiceAjax.getJobById(jobId).then(function(data){
	    	        $scope.job = data;
	        	});
	        }, function () {});

	     };
	     
	     $scope.deleteLoadTag = function(id) {
	           $scope.errorMsg = false;
	           $scope.successMsg = false;

	           SweetAlert.swal({
	                   title: $translate.instant('JOBS_JS.Delete_loadTag'),
	                   text:  $translate.instant('JOBS_JS.SURE_DELETE_LOADTAG'),
	                   type: "warning",
	                   showCancelButton: true,
	                   confirmButtonColor: "#DD6B55",
	                   confirmButtonText: $translate.instant('JOBS_JS.DELETE'),
	                   closeOnConfirm: false
	               },
	               function(isConfirm) {
	                   if (isConfirm) {
	                       loadTagServiceAjax.deleteLoadTag(id).then(function(data) {
	                           $scope.loadTags = data;
	                           jobServiceAjax.getJobById(jobId).then(function(data){
		           	    	        $scope.job = data;
		           	           });
	                           // $scope.successMsg = false;
	                           //$scope.errorMsg = false;
	                           //$scope.alertMsg = "Order successfully deleted!";
	                           SweetAlert.swal($translate.instant('JOBS_JS.Delete_loadTag'), $translate.instant('JOBS_JS.LoadTag_SUCC_ADDED'), "success");
	                       },function(data, status, headers, config) {
	                           // $scope.errorMsg = true;
	                           //  $scope.successMsg = false;
	                           // $scope.alertMsg = data.errors.errors;
	                           SweetAlert.swal($translate.instant('JOBS_JS.DELETION_ERROR'), data.data.errors.errors, "error");
	                       });
	                   }
	               });
	       };
});


angular.module('capApp')
.controller('SplitJobModalInstanceCtrl', function ($scope, $uibModalInstance, $uibModal, jobServiceAjax, jobId, orderId, cascadeFlag, splitLevel, hours, originalQuantity, toasty, $translatePartialLoader, $translate, $localStorage) {

	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	  $scope.alertMsg =  $translate.instant('JOBS_JS.then_SPLIT_JOB') + jobId;
	  $scope.$parent.thenMsg = false;
	  $scope.$parent.errorMsg = false;
	  $scope.errorMsg = false;
	  $scope.jobId = jobId;
	  $scope.orderId = orderId;
	  $scope.splitLevel = splitLevel;
	  $scope.hours = hours;
	  $scope.originalQuantity = originalQuantity;
	  $scope.newQuantity = '';
	  $scope.cascadeFlag = cascadeFlag;
	  $scope.splitJobs = function(jobId, newQuantity, cascadeFlag) {
		  jobServiceAjax.splitJobs(jobId, newQuantity, cascadeFlag).then(function(data){
			  	$scope.alertMsg = $scope.alertMsg;
	            $scope.$parent.alertMsg = $scope.alertMsg;
	            //$scope.$parent.thenMsg = true;
	            toasty.success({
	                  title:  $translate.instant('JOBS_JS.SPLIT_JOB'), 
	                  msg:  $translate.instant('JOBS_JS.then_SPLIT_JOB') + jobId,
	                  showClose: true,
	                  clickToClose: true,
	                  timeout: 10000,
	                  sound: false,
	                  html: false,
	                  shake: false,
	                  theme: "bootstrap"
	              });
	            $uibModalInstance.close();
		  },function(data, status, headers, config){
	          	$scope.errorMsg = true;
	          	$scope.successMsg = false;
	            $scope.errors = data.errors;
		  });
	  };
	  $scope.closeModal = function(){
		  $uibModalInstance.dismiss();
	  };
      
});

