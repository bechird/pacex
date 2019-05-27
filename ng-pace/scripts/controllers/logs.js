'use strict';
/**
 * @ngdoc function
 * @name capApp.controller:LogsCtrl
 * @description
 * # LogsCtrl
 * Controller of the capApp
 */
angular.module('capApp')
.controller('LogsListCtrl', ['DTOptionsBuilder','DTColumnBuilder', '$scope', '$log', 'logServiceAjax', 'jobServiceAjax','SweetAlert', 'machineServiceAjax', 'rollServiceAjax', 'lookupServiceAjax', 
			'$confirm', '$uibModal', 'toasty','$translatePartialLoader', '$translate', '$localStorage','$rootScope', '$http',
    function (DTOptionsBuilder, DTColumnBuilder, $scope, $log, logServiceAjax, jobServiceAjax, SweetAlert, machineServiceAjax, rollServiceAjax, lookupServiceAjax, $confirm, $uibModal, toasty , 
    			  $translatePartialLoader, $translate, $localStorage,$rootScope, $http) {
      
	 	$translatePartialLoader.addPart('logsList');
	 	$translate.refresh();
	 	
	 	$.fn.dataTable.ext.errMode = 'none';
	 	var token = $localStorage.oauthToken;
	
	    var vm = this;
	    var monthNames = ["Jan ", $translate.instant('LOGS_JS.FEB'), "Mar ", $translate.instant('LOGS_JS.APR'), $translate.instant('LOGS_JS.MAY'), $translate.instant('LOGS_JS.JUN'), $translate.instant('LOGS_JS.JUL'), $translate.instant('LOGS_JS.AUG'), "Sep ", "Oct ", "Nov ", "Dec "];
        $scope.successMsg = false;
	    $scope.errorMsg = false;
	    $scope.defaultLog = {
	    		"machineId":"",
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
	    
	    vm.dtInstance = {};

	    var serverData = function(sSource, aoData, fnCallback, oSettings) {
	           $http.post($rootScope.API_BASE+"/logs/paginated",{aoData}).then(function(result){
	        	   
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
	  		  var dataTables = {
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
    	      var dataTables = {
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
          .withDisplayLength(17)
          .withOption('order', [0, 'desc'])
          .withColumnFilter({
              
              aoColumns: [
               {
                  type: 'number'    //logId                
              },{
            	  type: 'text',   
                  bRegex: true,
                  bSmart: true    //machineId
              },{
            	  type: 'number'   //job id
              },{
            	  type: 'text',   
                  bRegex: true,
                  bSmart: true    //event
              },{
            	  type: 'text',   
                  bRegex: true,
                  bSmart: true    //cause
              },{
            	  type: 'text',   
                  bRegex: true,
                  bSmart: true    //result
              },{
                  type: 'date',   // startTime
                  bRegex: true,
                  bSmart: true
              },{
              	type: 'date',   //finishTime
                  bRegex: true,
                  bSmart: true
              },{
            	  type: 'number'  //roll id
              },{
            	  type: 'number'  //roll length
              },{
            	  type: 'number'  //counter feet
              }]
          })
          .withLanguage(dataTables);

       vm.dtColumns = [
          DTColumnBuilder.newColumn('logId').withTitle($translate.instant('LOGS_JS.LOG_ID')).withClass('text-center'),
          DTColumnBuilder.newColumn('machineId').withTitle($translate.instant('LOGS_JS.Machine')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('currentJobId').withTitle( $translate.instant('LOGS_JS.JOB_ID')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('event').withTitle($translate.instant('LOGS_JS.EVENT')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('logCause.name').withTitle($translate.instant('LOGS_JS.Cause')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('logResult.name').withTitle( $translate.instant('LOGS_JS.RESULT')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('startTime').withTitle($translate.instant('LOGS_JS.START_TIME')).renderWith(function (data) {
              var res = "_";
              if(data > 0){
                  var date = new Date(data);
                  res = monthNames[date.getMonth()] + date.getDate() + ", " + date.getHours() + ":" + date.getMinutes();
              }
              return res;
          }),
          DTColumnBuilder.newColumn('finishTime').withTitle($translate.instant('LOGS_JS.FINISH_TIME')).renderWith(function (data) {
              var res = "_";
              if(data > 0){
                  var date = new Date(data);
                  res = monthNames[date.getMonth()] + date.getDate() + ", " + date.getHours() + ":" + date.getMinutes();
              }
              return res;
          }),
          DTColumnBuilder.newColumn('rollId').withTitle($translate.instant('LOGS_JS.ROLL_ID')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('rollLength').withTitle($translate.instant('LOGS_JS.ROLL_LENGTH')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('counterFeet').withTitle( $translate.instant('LOGS_JS.COUNTER_FEET')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('logId').withTitle(' ').notSortable().renderWith(function (data) {
        	  return "<div style='display:flex;'><button type='button' onclick=\"var $scope = angular.element(event.target).scope(); $scope.openEditLogModal('"+data+"'); $scope.$apply()\" uib-tooltip='Edit' title='"+$translate.instant('LOGS_JS.EDIT')+"' class='btn bgm-orange-900 waves-circle waves-effect'><i class='zmdi zmdi-edit'></i></button>&nbsp;<button type='button' onclick=\"var $scope = angular.element(event.target).scope(); $scope.deleteLog('"+data+"'); $scope.$apply()\" uib-tooltip='Delete' title='"+$translate.instant('LOGS_JS.DELETE_js')+"' class='btn bgm-deeporange waves-circle waves-effect'><i class='zmdi zmdi-delete'></i></button></div>";
          })
      ];
      
       $scope.deleteLog = function(id) {
           $scope.errorMsg = false;
           $scope.successMsg = false;

           SweetAlert.swal({
                   title: $translate.instant('LOGS_JS.DELETE_ROLL'),
                   text:  $translate.instant('LOGS_JS.SURE_DELETE_LOG'),
                   type: "warning",
                   showCancelButton: true,
                   confirmButtonColor: "#DD6B55",
                   confirmButtonText:  $translate.instant('LOGS_JS.DELETE'),
                   closeOnConfirm: true
               },
               function(isConfirm) {
                   if (isConfirm) {
                       logServiceAjax.deleteLog(id).then(function(data) {
                           $scope.logs = data;
                           vm.dtInstance.reloadData();
                           toasty.success({
      		                  title: $translate.instant('LOGS_JS.DELETE_LOG'),
      		                  msg: $translate.instant('LOGS_JS.LOG_SUCC_DELETED') ,
      		                  showClose: true,
      		                  clickToClose: true,
      		                  timeout: 5000,
      		                  sound: false,
      		                  html: false,
      		                  shake: false,
      		                  theme: "bootstrap"
      		               });
                       },function(data, status, headers, config) {
                           // $scope.errorMsg = true;
                           //  $scope.successMsg = false;
                           // $scope.alertMsg = data.errors.errors;
                           SweetAlert.swal($translate.instant('LOGS_JS.DELETION_ERROR'), data.data.errors.errors, "error");
                       });
                   }
               });
       };
	    
	    $scope.openAddLogModal = function () {
	         var modalInstance = $uibModal.open({
	           animation: true,
	           backdrop  : 'static',
	       	   keyboard  : false,
	           templateUrl: './views/addLogModalContent.html',
	           controller: 'AddLogModalInstanceCtrl',
	           scope: $scope,
	           size:'lg',
	           resolve: {
	             
	           }
	         });
	         modalInstance.result.then(function () {
	         	vm.dtInstance.reloadData();
	           }, function () {});
	     };
	   
	    $scope.openEditLogModal = function (id) {
	        var modalInstance = $uibModal.open({
	        	backdrop  : 'static',
	        	keyboard  : false,
	        	animation: true,
	        	templateUrl: './views/editLogModalContent.html',
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
	        	vm.dtInstance.reloadData();
	        }, function () {});

	     };
	     
	 	   $scope.log = jQuery.extend({}, $scope.defaultLog);
	 	   
	 	    $scope.loadMachineOptions = function(){
		 		machineServiceAjax.machinesQuick().then(function(data){
		 			$scope.machineOptions = data;
		 		});
		 	};
		 	$scope.loadMachineOptions();
		 	$scope.logMachine = {
		 	    repeatSelect: null,
		 		availableOptions: $scope.machineOptions,
		 	};
		 	
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
	 		
	 		$scope.loadLogResultOptions = function(){
	 	    	  lookupServiceAjax.readAll('LogResult').then(function(data){
	 		            $scope.logResultOptions = data;
	 		         });
	 		};
	 		$scope.loadLogResultOptions();
	 		$scope.logResult = {
	 			    repeatSelect: null,
	 			    availableOptions: $scope.logResultOptions,
	 		};

}]);

angular.module('capApp')
.controller('AddLogModalInstanceCtrl', function ($scope, $uibModalInstance, $uibModal, logServiceAjax, jobServiceAjax, lookupServiceAjax, toasty,$translatePartialLoader, $translate, $localStorage) {

	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	  var alertAddMessage =  $translate.instant('LOGS_JS.LOG_ADDED');
	  $scope.log = jQuery.extend(true,{}, $scope.defaultLog);
	  
	  $scope.errors = jQuery.extend({}, $scope.defaultLog);
	  $scope.$parent.successMsg = false;
	  $scope.$parent.errorMsg = false;
	  $scope.errorMsg = false;
	     
	  $scope.addLog = function(){
		  if($scope.log.counterFeet == null){
			  $scope.log.counterFeet = 0;
		  }
		  $scope.log.startTime = new Date($scope.log.startTime);
		  $scope.log.finishTime = new Date($scope.log.finishTime);
  	    logServiceAjax.addLog($scope.log)
	          .then( function(data, status, headers, config){
	              $scope.errors = jQuery.extend({}, $scope.defaultLog);
	              $scope.$parent.alertMsg = alertAddMessage;
	             // $scope.$parent.successMsg = true;
	              toasty.success({
	                  title: $translate.instant('LOGS_JS.ADDING_LOG'),
	                  msg: $translate.instant('LOGS_JS.LOADTAG_SUCC_ADDED'),
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
.controller('EditLogModalInstanceCtrl', function ($scope, $uibModalInstance, $uibModal, logServiceAjax, jobServiceAjax, lookupServiceAjax, logId, toasty,$translatePartialLoader, $translate, $localStorage) {

	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	  var alertUpdateMessage =  $translate.instant('LOGS_JS.UPDATING_LOG') +"!!";
	  $scope.log = jQuery.extend({}, $scope.defaultLog);
	  $scope.errors = jQuery.extend({}, $scope.defaultLog);
	  $scope.$parent.successMsg = false;
	  $scope.$parent.errorMsg = false;
	  $scope.errorMsg = false;
		    
	  logServiceAjax.getLogById(logId).then(function(data){
	        $scope.log = data;
	        $scope.startTime = new Date(data.startTime);
	        $scope.startTime = moment(data.startTime).format('MM-DD-YYYY, HH:mm');
	        $scope.finishTime = new Date(data.finishTime);
	        $scope.finishTime = moment(data.finishTime).format('MM-DD-YYYY, HH:mm');
	        $scope.updateLog = function(){	        	
	        	$scope.log.startTime = new Date($scope.startTime);
	            $scope.log.finishTime = new Date($scope.finishTime);
	            if($scope.log.counterFeet == null){
	  			   $scope.log.counterFeet = 0;
	  		    }
	        	logServiceAjax.updateLog($scope.log)
	            .then(function(){
	          	$scope.alertMsg = alertUpdateMessage;
	              $scope.errors = jQuery.extend({}, $scope.defaultLog);
	              $scope.$parent.alertMsg = alertUpdateMessage;
	              //$scope.$parent.successMsg = true;
	              toasty.success({
	                  title: $translate.instant('LOGS_JS.UPDATING'),
	                  msg: 'Log ' + $scope.log.logId + $translate.instant('LOGS_JS.UPDATED'),
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
	  
});
