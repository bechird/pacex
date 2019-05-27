'use strict';

/**
 * @ngdoc function
 * @name capApp.controller:MachinesListCtrl
 * @description
 * # MachinesListCtrl
 * Controller of the capApp
 */
angular.module('capApp')
  .controller('MachinesListCtrl', ['DTOptionsBuilder','DTColumnBuilder', '$scope', '$uibModal', '$log', 'machineServiceAjax','SweetAlert', '$confirm', 'toasty',
	  		  '$translatePartialLoader', '$translate', '$localStorage', '$rootScope',
    function (DTOptionsBuilder, DTColumnBuilder, $scope, $uibModal, $log, machineServiceAjax, SweetAlert, $confirm, toasty , $translatePartialLoader, $translate, $localStorage, $rootScope) {
	  
	  $translatePartialLoader.addPart('machineList');
	  $translate.refresh();
	  
	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	  var vm = this;
	  $scope.successMsg = false;
	  $scope.errorMsg = false;
	  
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
	  
	  vm.dtInstance = {};
      vm.dtOptions = DTOptionsBuilder.fromSource($rootScope.API_BASE+'/machines/quick?access_token=' + token)
          .withDOM('frtip')
          .withPaginationType('full_numbers')
          .withOption('responsive', true)
          //.withOption('dom', '<"top"i>rt<"bottom"flp><"clear">')
          .withDisplayLength(25)
          .withColumnFilter({

            aoColumns: [
              {
                type: 'text',
                bRegex: true,   // id
                bSmart: true
              },{
                type: 'text',
                bRegex: true,   //name
                bSmart: true
              },{
                type: 'text',
                bRegex: true,   // status
                bSmart: true
              },{
                  type: 'text',
                  bRegex: true,   // ip/port
                  bSmart: true
                },{
                    type: 'text',
                    bRegex: true,   // oc path
                    bSmart: true
                  }]
          })
          // Active Buttons extension
          .withButtons([
              'excel',
          ])
          .withLanguage(dataTables);

      vm.dtColumns = [
          DTColumnBuilder.newColumn('machineId').withTitle( $translate.instant('MACHINE_JS.ID')),
          DTColumnBuilder.newColumn('name').withTitle($translate.instant('MACHINE_JS.NAME')),
          DTColumnBuilder.newColumn('status.id').withTitle($translate.instant('MACHINE_JS.STATUS')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('fullIpAddress').withTitle($translate.instant('MACHINE_JS.ADDRESS')),
          DTColumnBuilder.newColumn('ocInputPath').withTitle($translate.instant('MACHINE_JS.OCPATH')),
          DTColumnBuilder.newColumn('machineId').withTitle(' ').notSortable().renderWith(function (data) {
        	  return "<button type='button' onclick=\"var $scope = angular.element(event.target).scope(); $scope.openEditMachineModal('"+data+"'); $scope.$apply()\" uib-tooltip='Edit' title='"+$translate.instant('MACHINE_JS.EDIT')+"' class='btn bgm-orange-900 waves-circle waves-effect'><i class='zmdi zmdi-edit'></i></button>&nbsp;<button type='button' onclick=\"var $scope = angular.element(event.target).scope(); $scope.deleteMachine('"+data+"'); $scope.$apply()\" uib-tooltip='Delete' title='"+$translate.instant('MACHINE_JS.DELETE_js')+"' class='btn bgm-deeporange waves-circle waves-effect'><i class='zmdi zmdi-delete'></i></button>";
   
          })
      ];

 /*    $scope.deleteMachine = function(id){
    	  $scope.errorMsg = false;
          $scope.successMsg = false;
    	  $confirm({text: 'Are you sure you want to delete the machine?', title: 'Delete Machine', ok: 'Ok', cancel: 'Cancel'})
	        .then(function() {
	        	machineServiceAjax.deleteMachine(id).then(function(data){
	        $scope.machines = data;
            vm.dtInstance.reloadData();
            $scope.successMsg = true;
            $scope.errorMsg = false;
            $scope.alertMsg = "Machine successfully deleted!";
            
    	  }, function(data, status, headers, config){
	            $scope.errorMsg = true;
	            $scope.successMsg = false;
	            $scope.alertMsg = data.errors.errors;
	      });
	     });
	   };*/
      $scope.deleteMachine = function(id) {
          $scope.errorMsg = false;
          $scope.successMsg = false;

          SweetAlert.swal({
                  title:  $translate.instant('MACHINE_JS.DELETE_MACHINE'),
                  text:  $translate.instant('MACHINE_JS.SURE_DELETE_MACHINE'),
                  type: "warning",
                  showCancelButton: true,
                  confirmButtonColor: "#DD6B55",
                  confirmButtonText: $translate.instant('MACHINE_JS.DELETE'),
                  closeOnConfirm: true
              },
              function(isConfirm) {
                  if (isConfirm) {
                      machineServiceAjax.deleteMachine(id).then(function(data) {
                          $scope.machines = data;
                          vm.dtInstance.reloadData();
                          toasty.success({
      		                  title: $translate.instant('MACHINE_JS.DELETE_MACHINE'),
      		                  msg: $translate.instant('MACHINE_JS.LOOKUP_SUCC_DELETED') ,
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
                          SweetAlert.swal(s$translate.instant('MACHINE_JS.DELETION_ERROR'), data.data.errors.errors, "error");
                      });
                  }
              });
      };
      
      $scope.openEditMachineModal = function (id) {
        var modalInstance = $uibModal.open({
        	backdrop  : 'static',
        	keyboard  : false,
        	animation: true,
        	templateUrl: 'editMachineModalContent.html',
        	controller: 'EditMachineModalInstanceCtrl',
        	size: 'lg',
        	scope: $scope,
        	resolve: {
        		machineId: function () {
        		return id;
            }
          }
        });

        modalInstance.result.then(function (selectedItem) {
        	vm.dtInstance.reloadData();
        }, function () {});
      };
      
      $scope.openAddMachineModal = function () {
        var modalInstance = $uibModal.open({
          animation: true,
          backdrop  : 'static',
      	  keyboard  : false,
          templateUrl: 'addMachineModalContent.html',
          controller: 'AddMachineModalInstanceCtrl',
          size: 'lg',
          scope: $scope,
          resolve: {
            
          }
        });

        modalInstance.result.then(function () {
        	vm.dtInstance.reloadData();
          }, function () {});

      };
      
  }]);
      
  angular.module('capApp')
  .controller('AddMachineModalInstanceCtrl', function ($scope, $uibModalInstance, machineServiceAjax, stationServiceAjax, lookupServiceAjax, toasty, $translatePartialLoader, $translate, $localStorage) {
	  
	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	  var defaultMachine = {
		    "name":"",
		    "description":"",
		    "stationId":"",
		    "status":{"id":"OFF"},
		    "speed":""
		  };
	  var alertAddMessage = $translate.instant('MACHINE_JS.MACHINE_ADDED') +"!!";
	  $scope.machine = jQuery.extend({}, defaultMachine);
	  $scope.errors = jQuery.extend({}, defaultMachine);
	  $scope.$parent.successMsg = false;
	  $scope.$parent.errorMsg = false;
	  $scope.errorMsg = false;
      
      $scope.loadStationOptions = function(){
    	  stationServiceAjax.stationsQuick().then(function(data){
	            $scope.stationOptions = data;
	         });
	    };
	    $scope.loadStationOptions();
	    $scope.station = {
		    repeatSelect: null,
		    availableOptions: $scope.stationOptions,
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
	    
	    $scope.loadTypeOptions = function(){
	    	  lookupServiceAjax.readAll('MachineType').then(function(data){
		            $scope.machineTypeOptions = data;
		         });
	    };
	    $scope.loadTypeOptions();
	    $scope.machineType = {
		    repeatSelect: null,
		    availableOptions: $scope.machineTypeOptions,
		};
	    
	    $scope.filterMachineTypes = function(){
	    	lookupServiceAjax.readAll('MachineType').then(function(data){
	    		$scope.machineTypeOptions = data;
		    	for(var k = 0; k < $scope.machineTypeOptions.length; k++){
		    		if($scope.machine.stationId != null && $scope.machine.stationId != '' && 
		    		  $scope.machine.stationId != $scope.machineTypeOptions[k].stationCategoryId){
		    			$scope.machineTypeOptions.splice(k, 1);k--;
		    		}
		    	}
	    	});
	    };
	    
	  $scope.addMachine = function(){
		  machineServiceAjax.addMachine($scope.machine)
	          .then( function(data, status, headers, config){
	        	  $scope.errors = jQuery.extend({}, defaultMachine);
	              $scope.$parent.alertMsg = alertAddMessage;
	              //$scope.$parent.successMsg = true;
	              toasty.success({
	                  title: $translate.instant('MACHINE_JS.ADDING_MACHINE'),
	                  msg:$translate.instant('MACHINE_JS.MACHINE_SUCC_ADDED'),
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
	            $scope.successMsg = false;
	            $scope.errorMsg = true;
	            $scope.errors = data.errors;
	          });
	  };
	  $scope.closeModal = function(){
		  $uibModalInstance.dismiss();
	  };
  });
  
  angular.module('capApp')
  .controller('EditMachineModalInstanceCtrl', function ($scope, $uibModalInstance, machineServiceAjax, stationServiceAjax, lookupServiceAjax, machineId, toasty, $translatePartialLoader, $translate, $localStorage) {

	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	  var defaultMachine = {
			  "id":"",
			    "name":"",
			    "description":"",
			    "stationId":"",
			    "status":"",
			    "speed":""
		  };
	  
	  var alertUpdateMessage = $translate.instant('MACHINE_JS.UPDATED_MACHINE');
	  $scope.errors = jQuery.extend({}, defaultMachine);
	  $scope.$parent.successMsg = false;
	  $scope.$parent.errorMsg = false;
	  $scope.errorMsg = false;
	  
	  $scope.loadStationOptions = function(){
    	  stationServiceAjax.stationsQuick().then(function(data){
	            $scope.stationOptions = data;
	         });
	    };
	    $scope.loadStationOptions();
	    $scope.station = {
		    repeatSelect: null,
		    availableOptions: $scope.stationOptions,
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
	    
	    $scope.loadTypeOptions = function(){
	    	  lookupServiceAjax.readAll('MachineType').then(function(data){
		            $scope.machineTypeOptions = data;
		         });
	    };
	    $scope.loadTypeOptions();
	    $scope.machineType = {
		    repeatSelect: null,
		    availableOptions: $scope.machineTypeOptions,
		};
	    
	    $scope.filterMachineTypes = function(){
	    	lookupServiceAjax.readAll('MachineType').then(function(data){
	    		$scope.machineTypeOptions = data;
		    	for(var k = 0; k < $scope.machineTypeOptions.length; k++){
		    		if($scope.machine.stationId != null && $scope.machine.stationId != '' && 
		    		  $scope.machine.stationId != $scope.machineTypeOptions[k].stationCategoryId){
		    			$scope.machineTypeOptions.splice(k, 1);k--;
		    		}
		    	}
	    	});
	    };
	    
	  machineServiceAjax.getMachineById(machineId).then(function(data){
	        $scope.machine = data;
	        $scope.updateMachine = function(){
	        	machineServiceAjax.updateMachine($scope.machine)
	            .then(function(){
	          	  $scope.errors = jQuery.extend({}, defaultMachine);
	              $scope.$parent.alertMsg = alertUpdateMessage;
	             // $scope.$parent.successMsg = true;
	              toasty.success({
	                  title:  $translate.instant('MACHINE_JS.UPDATING'),
	                  msg: 'Machine '  + $scope.machine.machineId + $translate.instant('MACHINE_JS.UPDATED'),
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
  
