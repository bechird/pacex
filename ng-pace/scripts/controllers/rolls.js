	'use strict';
/**
 * @ngdoc function
 * @name capApp.controller:RollsCtrl
 * @description
 * # RollsCtrl
 * Controller of the capApp
 */
angular.module('capApp')
.controller('RollsListCtrl', ['DTOptionsBuilder','DTColumnBuilder', '$scope', '$log', 'rollServiceAjax', 'SweetAlert', 'machineServiceAjax', 'lookupServiceAjax', '$confirm', '$uibModal', 
			'$filter', 'toasty', '$translatePartialLoader', '$translate', '$localStorage', '$rootScope', '$http',
    function (DTOptionsBuilder, DTColumnBuilder, $scope, $log, rollServiceAjax, SweetAlert, machineServiceAjax, lookupServiceAjax, $confirm, $uibModal, $filter, toasty, $translatePartialLoader , 
    		      $translate, $localStorage, $rootScope, $http) {
      
	
		$translatePartialLoader.addPart('rolls');
		$translate.refresh();
		
		$.fn.dataTable.ext.errMode = 'none';
		var token = $localStorage.oauthToken;
	
	    var vm = this;
	    
	    var monthNames = ["Jan ", $translate.instant('ORDERS_JS.FEB'), "Mar ", $translate.instant('ORDERS_JS.APR'), $translate.instant('ORDERS_JS.MAY'), $translate.instant('ORDERS_JS.JUN'), $translate.instant('ORDERS_JS.JUL'), $translate.instant('ORDERS_JS.AUG'), "Sep ", "Oct ", "Nov ", "Dec "];
        
        $scope.successMsg = false;
	    $scope.errorMsg = false;
	    $scope.defaultRoll = {
	 		    "machineId":"",
	 		    "machineOrdering":"",
	 		    "rollType":{"id":""},
	 		    "length":"",
	 		    "width":"",
	 		    "weight":"",
	 		    "paperType":{"id":""},
	 		    "status":{"id":""},
	 		    "hours":"",
	 		    "utilization":""
	 	 };
	    
	    vm.dtInstance = {};
	    
       var serverData = function(sSource, aoData, fnCallback, oSettings) {
           $http.post($rootScope.API_BASE+"/rolls/paginated",{aoData}).then(function(result){
        	   
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
          .withOption('order', [])
          .withButtons(['print', 'csv'])
          .withColumnFilter({
              aoColumns: [
               {
                  type: 'number'    //rollId
              },{
                  type: 'text',   //type
                  bRegex: true,
                  bSmart: true
              },{
                  type: 'date'   //created date
                  
              },{
                  type: 'text',   //status
                  bRegex: true,
                  bSmart: true
              },{
            	  type: 'number'   //roll length
              },{
                  type: 'text',   // machine
                  bRegex: true,
                  bSmart: true
              },{
              	type: 'text',   //paper type
                  bRegex: true,
                  bSmart: true
              },{
            	  type: 'number'  //hours
              },{
            	  type: 'number'  // utilization
              }]
          })
          .withLanguage($scope.dataTables);

       vm.dtColumns = [
          DTColumnBuilder.newColumn('rollId').withTitle($translate.instant('rolls_js.Id')).withClass('text-center'),
          DTColumnBuilder.newColumn('rollType.name').withTitle($translate.instant('rolls_js.Type') ).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('creationDate').withTitle($translate.instant('rolls_js.Date_Created')).withOption('type', 'date')
          .renderWith(function(data) {
              var res = "_";
              if(angular.isDefined($rootScope.dateFormat)){
            	  res = $filter('date')(data, $rootScope.dateFormat);
              }else{
	              if (data > 0) {
	                  var date = new Date(data);
	                  res = monthNames[date.getMonth()] + date.getDate() + " " + date.getFullYear();
	              }
              }
              return res;
          }),
          DTColumnBuilder.newColumn('status.name').withTitle($translate.instant('rolls_js.Status')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('length').withTitle($translate.instant('rolls_js.Length')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('machineId').withTitle($translate.instant('rolls_js.Machine')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('paperType.name').withTitle($translate.instant('rolls_js.PaperType')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('hours').withTitle( $translate.instant('rolls_js.Hours')).withOption('defaultContent', ' ').
          renderWith(function (data) {
              return $filter('number')(data, 2);
          }),
          DTColumnBuilder.newColumn('utilization').withTitle( $translate.instant('rolls_js.Utilization')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('rollId').withTitle(' ').notSortable().renderWith(function (data) {
        	  return "<div style='display:flex;'><button type='button' uib-tooltip='Edit' title='"+$translate.instant('rolls_js.EDIT')+"' onclick=\"var $scope = angular.element(event.target).scope(); $scope.openEditRollModal('"+data+"'); $scope.$apply()\" class='btn bgm-orange-900	 waves-circle waves-effect'><i class='zmdi zmdi-edit'></i></button>&nbsp;<button type='button' onclick=\"var $scope = angular.element(event.target).scope(); $scope.deleteRoll('"+data+"'); $scope.$apply()\" uib-tooltip='Delete' title='"+$translate.instant('rolls_js.DELETE_js')+"' class='btn bgm-deeporange waves-circle waves-effect'><i class='zmdi zmdi-delete'></i></button></div>";
          })
      ];
      
      /*$scope.deleteRoll = function(id){
    	  $scope.errorMsg = false;
          $scope.successMsg = false;
    	  $confirm({text: 'Are you sure you want to delete the roll?', title: 'Delete Roll', ok: 'Ok', cancel: 'Cancel'})
	        .then(function() {
	        rollServiceAjax.deleteRoll(id).then(function(data){
	          $scope.rolls = data;
              vm.dtInstance.reloadData();
              $scope.successMsg = true;
              $scope.errorMsg = false;
              $scope.alertMsg = "Roll successfully deleted!";
    	  }, function(data, status, headers, config){
	            $scope.errorMsg = true;
	            $scope.successMsg = false;
	            $scope.alertMsg = data.errors.errors;
	      });
	     });
	   };
	    */
       
       
       $scope.deleteRoll = function(id) {
           $scope.errorMsg = false;
           $scope.successMsg = false;

           SweetAlert.swal({
                   title: $translate.instant('rolls_js.DeleteRoll'),
                   text: $translate.instant('rolls_js.delete_sure'),
                   type: 'warning',
                   showCancelButton: true,
                   confirmButtonColor: "#DD6B55",
                   confirmButtonText: $translate.instant('rolls_js.Yes_delete'),
                   closeOnConfirm: true
               },
               function(isConfirm) {
                   if (isConfirm) {
                       rollServiceAjax.deleteRoll(id).then(function(data) {
                           $scope.rolls = data;
                           vm.dtInstance.reloadData();
                           toasty.success({
       		                  title: $translate.instant('rolls_js.DeleteRoll'),
       		                  msg: $translate.instant('JOBS_JS.Part_succ_deleted') ,
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
                           SweetAlert.swal($translate.instant('rolls_js.DeletionError'), data.data.errors.errors, "error");
                       });
                   }
               });
       };
       
       $scope.rollMinLength = 1;
       lookupServiceAjax.getItemById('Preference', 'ROLLMINLENGTH').then(function(data){
 			$scope.rollMinLength = data.name;
 	   });
       
	   $scope.openAddRollModal = function () {
	         var modalInstance = $uibModal.open({
	           animation: true,
	           backdrop  : 'static',
	       	   keyboard  : false,
	           templateUrl: './views/addRollModalContent.html',
	           controller: 'AddRollModalInstanceCtrl',
	           scope: $scope,
	           size:'lg',
	           resolve: {
	             
	           }
	         });
	         modalInstance.result.then(function () {
	         	vm.dtInstance.reloadData();
	           }, function () {});
	     };
	   
	     $scope.openEditRollModal = function (id) {
	        var modalInstance = $uibModal.open({
	        	backdrop  : 'static',
	        	keyboard  : false,
	        	animation: true,
	        	templateUrl: './views/editRollModalContent.html',
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
	        	vm.dtInstance.reloadData();
	        }, function () {});

	      };
	     
	      $scope.openEditPartModal = function (id) {
	       		$scope.loadCritiriaOptions = function(){
	          	  lookupServiceAjax.readAll('Critiria').then(function(data){
	      	            $scope.critiriaOptions = data;
	      	         });
		      	};
		      	$scope.loadCritiriaOptions();
		      	$scope.loadPaperTypeOptions = function(){
		 	    	  lookupServiceAjax.readAll('PaperType').then(function(data){
		 		            $scope.paperTypeOptions = data;
		 		         });
		 		};
		 		$scope.loadPaperTypeOptions();
		 		$scope.loadLaminationOptions = function(){
		 	    	  lookupServiceAjax.readAll('Lamination').then(function(data){
		 		            $scope.laminationOptions = data;
		 		         });
		 		};
		 		$scope.loadLaminationOptions();
		 		$scope.loadBindingTypeOptions = function(){
		 	    	  lookupServiceAjax.readAll('BindingType').then(function(data){
		 		            $scope.bindingTypeOptions = data;
		 		         });
		 		};
		 		$scope.loadBindingTypeOptions();
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
		   
	 	   $scope.roll = jQuery.extend({}, $scope.defaultRoll);
	 		  
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
		 	
		 	$scope.loadMachineOptions = function(){
		 		machineServiceAjax.machinesQuick().then(function(data){
		 			$scope.machineOptions = data;
		 		});
		 	};
		 	$scope.loadMachineOptions();
		 	$scope.rollMachine = {
		 	    repeatSelect: null,
		 		availableOptions: $scope.machineOptions,
		 	};

		 	$scope.loadRollOptions = function(){
		 		rollServiceAjax.rollsIds().then(function(data){
		 			$scope.rollOptions = data;
		 		});
		 	};
		 	$scope.loadRollOptions();
		 	$scope.parentRoll = {
		 	    repeatSelect: null,
		 		availableOptions: $scope.rollOptions,
		 	};

}]);

angular.module('capApp')
.controller('AddRollModalInstanceCtrl', function ($scope, $uibModalInstance, $uibModal, rollServiceAjax, toasty,$translatePartialLoader, $translate, $localStorage) {

	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	  var alertAddMessage =  $translate.instant('rolls_js.Roll_Added') +"!!";
	  $scope.roll = jQuery.extend(true,{}, $scope.defaultRoll);
	  
	  $scope.roll.status.id = "NEW";
	  $scope.errors = jQuery.extend({}, $scope.defaultRoll);
	  $scope.$parent.successMsg = false;
	  $scope.$parent.errorMsg = false;
	  $scope.errorMsg = false;
	     
	  $scope.addRoll = function(){
		  if($scope.roll.width == null){
			  $scope.roll.width = 0;
		  }
		  if($scope.roll.hours == null){
			  $scope.roll.hours = 0;
		  }
  	    rollServiceAjax.addRoll($scope.roll)
	          .then( function(data, status, headers, config){
	              $scope.errors = jQuery.extend({}, $scope.defaultRoll);
	              $scope.$parent.alertMsg = alertAddMessage;
	             //$scope.$parent.successMsg = true;
	              toasty.success({
	                  title: $translate.instant('rolls_js.Adding_Roll'),
	                  msg: $translate.instant('rolls_js.Roll_Added'),
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
.controller('EditRollModalInstanceCtrl', function ($scope, $uibModalInstance, $uibModal, DTOptionsBuilder, rollServiceAjax, rollId, toasty,$translatePartialLoader, $translate, $localStorage) {

	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	  var alertUpdateMessage =  $translate.instant('rolls_js.Roll_Updated') +"!!";
	  $scope.roll = jQuery.extend({}, $scope.defaultRoll);
	  $scope.errors = jQuery.extend({}, $scope.defaultRoll);
	  $scope.$parent.successMsg = false;
	  $scope.$parent.errorMsg = false;
	  $scope.errorMsg = false;
		    
	  rollServiceAjax.getRollById(rollId).then(function(data){
	        $scope.roll = data;
	        $scope.updateRoll = function(){
	        	if($scope.roll.width == null){
	  			   $scope.roll.width = 0;
	  		    }
	        	if($scope.roll.hours == null){
	  			   $scope.roll.hours = 0;
	  		    }
	        	rollServiceAjax.updateRoll($scope.roll)
	            .then(function(){
	          	$scope.alertMsg = alertUpdateMessage;
	              $scope.errors = jQuery.extend({}, $scope.defaultRoll);
	              $scope.$parent.alertMsg = alertUpdateMessage;
	              //$scope.$parent.successMsg = true;
	              toasty.success({
	                  title: $translate.instant('rolls_js.Updating_Roll'),
	                  msg: $translate.instant('rolls_js.Roll_Updated'),
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
	  $scope.dtOptionsForJobsOnRoll = DTOptionsBuilder.fromSource().withDOM('frtip').withOption('order', []).withPaginationType('full_numbers').withDisplayLength(5).withLanguage($scope.dataTables);

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
