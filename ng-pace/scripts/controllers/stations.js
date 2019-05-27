'use strict';

/**
 * @ngdoc function
 * @name capApp.controller:StationsListCtrl
 * @description
 * # StationsListCtrl
 * Controller of the capApp
 */
angular.module('capApp')
  .controller('StationsListCtrl', ['DTOptionsBuilder','DTColumnBuilder', '$scope', '$uibModal', '$log', 'stationServiceAjax','SweetAlert', '$confirm', 'toasty',
	  		  '$translatePartialLoader', '$translate', '$localStorage', '$rootScope',
	  		  function (DTOptionsBuilder, DTColumnBuilder, $scope, $uibModal, $log, stationServiceAjax, SweetAlert, $confirm, toasty , $translatePartialLoader, $translate, $localStorage, $rootScope) {
	  
	  $translatePartialLoader.addPart('stationList');
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
      vm.dtOptions = DTOptionsBuilder.fromSource($rootScope.API_BASE+'/stations/quick?access_token=' + token)
          .withDOM('frtip')
          .withPaginationType('full_numbers')
          .withOption('responsive', true)
         // .withOption('dom', '<"top"i>rt<"bottom"flp><"clear">')
          .withDisplayLength(25)
          .withColumnFilter({

            aoColumns: [
              {
                type: 'text',
                bRegex: true,
                bSmart: true
              },{
                type: 'text',
                bRegex: true,
                bSmart: true
              },{
                  type: 'text',
                  bRegex: true,
                  bSmart: true
                },{
                type: 'text',
                bRegex: true,
                bSmart: true
              },{
                  type: 'number'
                },{
                  type: 'number'
                }]
          })
          // Active Buttons extension
          .withButtons([
              'excel',
          ])
          .withLanguage(dataTables);

      vm.dtColumns = [
          DTColumnBuilder.newColumn('stationId').withTitle($translate.instant('js_Station.ID')),
          DTColumnBuilder.newColumn('name').withTitle($translate.instant('js_Station.NAME')),
          DTColumnBuilder.newColumn('stationCategoryId').withTitle($translate.instant('js_Station.CATEGORY')),
          DTColumnBuilder.newColumn('description').withTitle( $translate.instant('js_Station.DESCRIPTION')),
          DTColumnBuilder.newColumn('activeFlag').withTitle($translate.instant('js_Station.Active')),
          DTColumnBuilder.newColumn('productionOrdering').withTitle($translate.instant('js_Station.ORDERING')),
          DTColumnBuilder.newColumn('stationId').withTitle(' ').notSortable().renderWith(function (data) {
        	  return "<div style='display:flex;'><button type='button' onclick=\"var $scope = angular.element(event.target).scope(); $scope.openEditStationModal('"+data+"'); $scope.$apply()\" uib-tooltip='Edit' title='"+$translate.instant('js_Station.EDIT')+"' class='btn bgm-orange-900 waves-circle waves-effect'><i class='zmdi zmdi-edit'></i></button>&nbsp;<button type='button' onclick=\"var $scope = angular.element(event.target).scope(); $scope.deleteStation('"+data+"'); $scope.$apply()\" uib-tooltip='Delete' title='"+$translate.instant('js_Station.DELETE_js')+"' class='btn bgm-deeporange waves-circle waves-effect'><i class='zmdi zmdi-delete'></i></button></div>";
   
          })
      ];
      
      $scope.loadStationOptions = function(){
    	  stationServiceAjax.stationsQuick().then(function(data){
	            $scope.stationOptions = data;
	      });
	  };
	  $scope.loadStationOptions();

      /*$scope.deleteStation = function(id){
    	  $scope.errorMsg = false;
          $scope.successMsg = false;
    	  $confirm({text: 'Are you sure you want to delete the station?', title: 'Delete Station', ok: 'Ok', cancel: 'Cancel'})
	        .then(function() {
	        	stationServiceAjax.deleteStation(id).then(function(data){
	        $scope.stations = data;
            vm.dtInstance.reloadData();
            $scope.successMsg = true;
            $scope.errorMsg = false;
            $scope.alertMsg = "Station successfully deleted!";
            
    	  }, function(data, status, headers, config){
	            $scope.errorMsg = true;
	            $scope.successMsg = false;
	            $scope.alertMsg = data.errors.errors;
	      });
	     });
	   };*/
	  $scope.deleteStation = function(id) {
	       $scope.errorMsg = false;
	       $scope.successMsg = false;

	       SweetAlert.swal({
	               title: $translate.instant('js_Station.DeleteStation'),
	               text: $translate.instant('js_Station.delete_sure'),
	               type: "warning",
	               showCancelButton: true,
	               confirmButtonColor: "#DD6B55",
	               confirmButtonText: $translate.instant('js_Station.Yes_delete'),
	               closeOnConfirm: true
	           },
	           function(isConfirm) {
	               if (isConfirm) {
	                    stationServiceAjax.deleteStation(id).then(function(data){
	                      $scope.stations = data;
	                      vm.dtInstance.reloadData();
                          toasty.success({
       		                  title: $translate.instant('js_Station.DeleteStation'),
       		                  msg: $translate.instant('js_Station.Station_succ_added') ,
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
	                       SweetAlert.swal($translate.instant('js_Station.DeletionError'), data.data.errors.errors, "error");
	                   });
	               }
	           });
	   };
      $scope.openEditStationModal = function (id) {
        var modalInstance = $uibModal.open({
        	backdrop  : 'static',
        	keyboard  : false,
        	animation: true,
        	templateUrl: 'editStationModalContent.html',
        	controller: 'EditStationModalInstanceCtrl',
        	size: 'lg',
        	scope: $scope,
        	resolve: {
        		stationId: function () {
        		return id;
            }
          }
        });

        modalInstance.result.then(function (selectedItem) {
        	vm.dtInstance.reloadData();
        }, function () {});
      };
      
      $scope.openAddStationModal = function () {
        var modalInstance = $uibModal.open({
          animation: true,
          backdrop  : 'static',
      	  keyboard  : false,
          templateUrl: 'addStationModalContent.html',
          controller: 'AddStationModalInstanceCtrl',
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
  .controller('AddStationModalInstanceCtrl', function ($scope, $uibModalInstance, stationServiceAjax, lookupServiceAjax, toasty,$translatePartialLoader, $translate, $localStorage) {

	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	  var defaultStation = {
		    "id":"",
		    "name":"",
		    "stationCategoryId":"",
		    "parentStationId":"",
		    "inputType":"",
		    "activeFlag":"",
		    "productionOrdering":"",
		    "description":""
		  };
	  var alertAddMessage = $translate.instant('js_Station.Station_Added') +" !!";
	  $scope.station = jQuery.extend({}, defaultStation);
	  $scope.errors = jQuery.extend({}, defaultStation);
	  $scope.$parent.successMsg = false;
	  $scope.$parent.errorMsg = false;
	  $scope.errorMsg = false;
      
        $scope.loadStationOptions = function(){
    	  stationServiceAjax.stationsQuick().then(function(data){
	            $scope.stationOptions = data;
	         });
	    };
	    $scope.loadStationOptions();
	    $scope.parentStation = {
		    repeatSelect: null,
		    availableOptions: $scope.stationOptions,
		};
	    
	    $scope.loadStationCategoryOptions = function(){
	    	  lookupServiceAjax.readAll('StationCategory').then(function(data){
		            $scope.stationCategoryOptions = data;
		         });
		    };
		    $scope.loadStationCategoryOptions();
		    $scope.stationCategory = {
			    repeatSelect: null,
			    availableOptions: $scope.stationCategoryOptions,
			};
	    
	  $scope.addStation = function(){
		  stationServiceAjax.addStation($scope.station)
	          .then( function(data, status, headers, config){
	        	  $scope.errors = jQuery.extend({}, defaultStation);
	              $scope.$parent.alertMsg = alertAddMessage;
	              //$scope.$parent.successMsg = true;
	              toasty.success({
	                  title: $translate.instant('js_Station.Adding_Station'),
	                  msg: $translate.instant('js_Station.Station_Added'),
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
  .controller('EditStationModalInstanceCtrl', function ($scope, $uibModalInstance, stationServiceAjax, lookupServiceAjax, stationId, toasty,$translatePartialLoader, $translate, $localStorage) {

	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	  var defaultStation = {
			    "id":"",
			    "name":"",
			    "stationCategoryId":"",
			    "parentStationId":"",
			    "inputType":"",
			    "activeFlag":"",
			    "productionOrdering":"",
			    "description":""
			  };
	  
	  var alertUpdateMessage = $translate.instant('js_Station.Station_Updated') +" !!";
	  $scope.errors = jQuery.extend({}, defaultStation);
	  $scope.$parent.successMsg = false;
	  $scope.$parent.errorMsg = false;
	  $scope.errorMsg = false;
	 
	  $scope.loadStationOptions = function(){
    	  stationServiceAjax.stationsQuick().then(function(data){
	            $scope.stationOptions = data;
	         });
	    };
	    $scope.loadStationOptions();
	    $scope.parentStation = {
		    repeatSelect: null,
		    availableOptions: $scope.stationOptions,
		};
	    
	    $scope.loadStationCategoryOptions = function(){
	    	  lookupServiceAjax.readAll('StationCategory').then(function(data){
		            $scope.stationCategoryOptions = data;
		         });
		    };
		    $scope.loadStationCategoryOptions();
		    $scope.stationCategory = {
			    repeatSelect: null,
			    availableOptions: $scope.stationCategoryOptions,
			};
		    
	  stationServiceAjax.getStationById(stationId).then(function(data){
	        $scope.station = data;
	        $scope.updateStation = function(){
	        	stationServiceAjax.updateStation($scope.station)
	            .then(function(){
	              $scope.errors = jQuery.extend({}, defaultStation);
	              $scope.$parent.alertMsg = alertUpdateMessage;
	             // $scope.$parent.successMsg = true;
	              toasty.success({
	                  title: $translate.instant('js_Station.Updating_Station'),
	                  msg: $translate.instant('js_Station.Station_Updated'),
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
  
  angular.module('capApp')
  .controller('StationsMenuCtrl', function ($scope,  stationServiceAjax) {
	  
	  $scope.loadStationsMenu = function(){
    	  stationServiceAjax.stationsMenu().then(function(data){
	            $scope.stationsMenuOptions = data;
	      });
	  };
	  $scope.loadStationsMenu();
	   
  });
  
  
