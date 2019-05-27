'use strict';

/**
 * @ngdoc function
 * @name capApp.controller:LookupsCtrl
 * @description
 * # LookupsCtrl
 * Controller of the capApp
 */
angular.module('capApp')
  .controller('LookupsCtrl', ['DTOptionsBuilder','DTColumnBuilder', '$scope', '$routeParams', '$uibModal', 'lookupServiceAjax','SweetAlert', '$confirm', 'toasty', '$translatePartialLoader', '$translate', '$localStorage','$rootScope', '$route', '$location',
     function (DTOptionsBuilder, DTColumnBuilder, $scope, $routeParams, $uibModal, lookupServiceAjax, SweetAlert, $confirm, toasty , $translatePartialLoader, $translate, $localStorage, $rootScope, $route, $location) {
	  
	  $translatePartialLoader.addPart('lookups');
	  $translate.refresh();
	  
	  $scope.type = $routeParams.type;
	  $scope.prefGroup = $routeParams.prefGroup;
	  $scope.prefSubject = $routeParams.prefSubject;
	  $scope.id = $routeParams.id;
	  $scope.allPrefSubjects = null;
	  $scope.clientId = angular.isDefined($routeParams.clientId) ? $routeParams.clientId : "undefined";
	  
	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	  var vm = this;
	  $scope.successMsg = false;
	  $scope.errorMsg = false;
	  vm.dtInstance = {};
	  vm.dtInstancePNL = {};
	  
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
    	 };
   	}
	  vm.src = $rootScope.API_BASE+'/lookups/'+$scope.type+ '?access_token=' + token;
	  if(angular.isDefined($routeParams.prefSubject)){
		  vm.src = $rootScope.API_BASE+'/lookups/prefSubjects/items/'+$scope.prefSubject+'/'+$scope.clientId+ '?access_token=' + token;
		  $scope.type = 'Preference';
      }
	  vm.type = $scope.type;
	  vm.prefSubject = $scope.prefSubject;
      vm.dtOptions = DTOptionsBuilder.fromSource(vm.src)
          .withDOM('frtip')
          .withPaginationType('full_numbers')
          .withOption('responsive', true)
          .withOption('order', [])
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
              }]
          })
          // Active Buttons extension
          .withButtons([
              'excel',
          ])
          .withLanguage(dataTables);
      
      vm.dtColumns = [
          DTColumnBuilder.newColumn('id').withTitle( $translate.instant('LOOKUPS_JS.ID')),
          DTColumnBuilder.newColumn('name').withTitle($translate.instant('LOOKUPS_JS.NAME')),
          DTColumnBuilder.newColumn('description').withTitle($translate.instant('LOOKUPS_JS.DESCRIPTION')),
          DTColumnBuilder.newColumn('id').withTitle(' ').notSortable().renderWith(function (data) {
        	  return "<button type='button' onclick=\"var $scope = angular.element(event.target).scope(); $scope.openEditLookupModal('"+$scope.type+"','"+data+"'); $scope.$apply()\" uib-tooltip='Edit' title='"+$translate.instant('LOOKUPS_JS.EDIT')+"' class='btn bgm-orange-900 waves-circle waves-effect'><i class='zmdi zmdi-edit'></i></button>&nbsp;<button type='button' onclick=\"var $scope = angular.element(event.target).scope(); $scope.deleteItem('"+$scope.type+"','"+data+"'); $scope.$apply()\" uib-tooltip='Delete' title='"+$translate.instant('LOOKUPS_JS.DELETE_js')+"' class='btn bgm-deeporange waves-circle waves-effect'><i class='zmdi zmdi-delete'></i></button>";
          })
      ];
      
      vm.dtOptionsPNL = DTOptionsBuilder.fromSource(vm.src)
      .withDOM('frtip')
      .withPaginationType('full_numbers')
      .withOption('responsive', true)
      .withOption('order', [])
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
              type: 'text',   // client
              bRegex: true,
              bSmart: true
            },{
                type: 'text',   //part
                bRegex: true,
                bSmart: true
              }]
      })
      // Active Buttons extension
      .withButtons([
          'excel',
      ])
      .withLanguage(dataTables);
  vm.dtColumnsPNL = [
      DTColumnBuilder.newColumn('id').withTitle( $translate.instant('LOOKUPS_JS.ID')),
      DTColumnBuilder.newColumn('name').withTitle($translate.instant('LOOKUPS_JS.NAME')),
      DTColumnBuilder.newColumn('description').withTitle($translate.instant('LOOKUPS_JS.DESCRIPTION')),
      DTColumnBuilder.newColumn('clientId').withTitle($translate.instant('loukup_details.CLIENT')),
      DTColumnBuilder.newColumn('partNum').withTitle($translate.instant('loukup_details.PART')),
      DTColumnBuilder.newColumn('id').withTitle(' ').notSortable().renderWith(function (data) {
    	  return "<button type='button' onclick=\"var $scope = angular.element(event.target).scope(); $scope.openEditLookupModal('"+$scope.type+"','"+data+"'); $scope.$apply()\" uib-tooltip='Edit' title='"+$translate.instant('LOOKUPS_JS.EDIT')+"' class='btn bgm-orange-900 waves-circle waves-effect'><i class='zmdi zmdi-edit'></i></button>&nbsp;<button type='button' onclick=\"var $scope = angular.element(event.target).scope(); $scope.deleteItem('"+$scope.type+"','"+data+"'); $scope.$apply()\" uib-tooltip='Delete' title='"+$translate.instant('LOOKUPS_JS.DELETE_js')+"' class='btn bgm-deeporange waves-circle waves-effect'><i class='zmdi zmdi-delete'></i></button>";
      })
  ];

 /*     $scope.deleteItem = function(type,id){
    	  $scope.errorMsg = false;
          $scope.successMsg = false;
    	  $confirm({text: 'Are you sure you want to delete the lookup item?', title: 'Delete Lookup', ok: 'Ok', cancel: 'Cancel'})
	        .then(function() {
	        	lookupServiceAjax.deleteItem(type,id).then(function(data){
	        $scope.items = data;
            vm.dtInstance.reloadData();
            $scope.successMsg = true;
            $scope.errorMsg = false;
            $scope.alertMsg = "Lookup Item successfully deleted!";
            
    	  }, function(data, status, headers, config){
	            $scope.errorMsg = true;
	            $scope.successMsg = false;
	            $scope.alertMsg = data.errors.errors;
	      });
	     });
	   };*/
      
     
      $scope.deleteItem = function(type,id) {
          $scope.errorMsg = false;
          $scope.successMsg = false;
          SweetAlert.swal({
                  title: $translate.instant('LOOKUPS_JS.DELETE_LOOKUP'),
                  text: $translate.instant('LOOKUPS_JS.SURE_DELETE_LOOKUP'),
                  type: "warning",
                  showCancelButton: true,
                  confirmButtonColor: "#DD6B55",
                  confirmButtonText: $translate.instant('LOOKUPS_JS.DELETE'),
                  closeOnConfirm: false
              },
              function(isConfirm) {
                  if (isConfirm) {
                      lookupServiceAjax.deleteItem(type, id).then(function(data) {
                          $scope.items = data;
                          if(type == 'Preference' && $scope.prefSubject == 'PNL'){
                        	  vm.dtInstancePNL.reloadData();
                          }else{
                        	  vm.dtInstance.reloadData();
                          }
                          // $scope.successMsg = false;
                          //$scope.errorMsg = false;
                          //$scope.alertMsg = "Order successfully deleted!";
                          SweetAlert.swal( $translate.instant('LOOKUPS_JS.DELETE_LOOKUP'), $translate.instant('LOOKUPS_JS.LOOKUP_SUCC_DELETED'), "success");
                      },function(data, status, headers, config) {
                          // $scope.errorMsg = true;
                          //  $scope.successMsg = false;
                          // $scope.alertMsg = data.errors.errors;
                          SweetAlert.swal($translate.instant('LOOKUPS_JS.DELETION_ERROR'), data.data.errors.errors, "error");
                      });
                  }
              });
      };
      
      $scope.openEditLookupModal = function (type,id) {
          var modalInstance = $uibModal.open({
        	backdrop  : 'static',
          	keyboard  : false,
            animation: true,
            templateUrl: 'editLookupModalContent.html',
            controller: 'EditLookupModalInstanceCtrl',
            scope: $scope,
            size: 'lg',
            resolve: {
              typeId: function () {
                return type;
              },
              itemId: function () {
              	return id;
              }
            }
          });

          modalInstance.result.then(function (selectedItem) {
        	  if(type == 'Preference' && $scope.prefSubject == 'PNL'){
            	  vm.dtInstancePNL.reloadData();
              }else{
            	  vm.dtInstance.reloadData();
              }
            }, function () {});

      };
        
      var currentPath = $location.path();	
      
        $scope.openAddLookupModal = function (type) {
            var modalInstance = $uibModal.open({
          	  backdrop  : 'static',
              keyboard  : false,
              animation: true,
              templateUrl: './views/addLookupModalContent.html',
              controller: 'AddLookupModalInstanceCtrl',
              scope: $scope,
              size: 'lg',
              resolve: {
            	    typeId: function () {
                      return type;
                    },
                    prefGroup: function () {
                        return $scope.prefGroup;
                    },
                    prefSubject: function () {
                        return $scope.prefSubject == undefined ? '' : $scope.prefSubject;
                    },
                    clientId: function () {
                        return $scope.clientId;
                    }
              }
            });

            modalInstance.result.then(function (id) {
            	if(currentPath.match("prefSubjects/list")){
            		$route.reload();
            	}else{
            		if(type == 'Preference' && $scope.prefSubject == 'PNL'){
                  	  vm.dtInstancePNL.reloadData();
                    }else{
                  	  vm.dtInstance.reloadData();
                    }
            		//reopen is edit mode for PNL templates
            		if(type == 'PNLTemplate'){
            			$scope.openEditLookupModal(type, id)
            		}
            	}
              }, function () {});

        };
        
		if(currentPath.match("prefSubjects/list")){
			lookupServiceAjax.prefGroups($scope.clientId == '' ? 'undefined' : $scope.clientId).then(function(data){
				$scope.prefGroups = data;
				if(!angular.isDefined($scope.prefGroup) || $scope.prefGroup == 'undefined'){
					$scope.prefGroup = data[0];
				}
				lookupServiceAjax.prefSubjects($scope.prefGroup, $scope.clientId == '' ? 'undefined' : $scope.clientId).then(function(data){
					$scope.prefSubjects = [];
					var tmp = [];
					var i = 0;
					while(i < data.length){
						tmp.push(data[i]); 
						if(i+1 < data.length) tmp.push(data[i+1]); 
						if(i+2 < data.length) tmp.push(data[i+2]);
						$scope.prefSubjects.push(tmp);
						tmp = [];
						i+=3;
					}
		        });
	        });
			
			$scope.loadClientOptions = function() {
	            lookupServiceAjax.readAll('Client').then(function(data) {
	                $scope.clientOptions = data;
	            });
	        };
	        $scope.loadClientOptions();
	        $scope.client = {
	            repeatSelect: null,
	            availableOptions: $scope.clientOptions
	        };
		}

		$scope.prefSubjectsByClient = function(clientId){
			  $scope.clientId = clientId;
			  lookupServiceAjax.prefGroups($scope.clientId == '' ? 'undefined' : $scope.clientId).then(function(data){
					$scope.prefGroups = data;
					if(!angular.isDefined($scope.prefGroup) || $scope.prefGroup == 'undefined'){
						$scope.prefGroup = data[0];
					}
		    	  lookupServiceAjax.prefSubjects($scope.prefGroup, $scope.clientId == '' ? 'undefined' : $scope.clientId).then(function(data){
		    		  $scope.prefSubjects = [];
						var tmp = [];
						var i = 0;
						while(i < data.length){
							tmp.push(data[i]); 
							if(i+1 < data.length) tmp.push(data[i+1]); 
							if(i+2 < data.length) tmp.push(data[i+2]);
							$scope.prefSubjects.push(tmp);
							tmp = [];
							i+=3;
						}
			      });
			  });
	    };
	    
	    if(currentPath.match("prefSubjects/items")){
	    	$scope.loadClientOptions = function() {
	            lookupServiceAjax.readAll('Client').then(function(data) {
	                $scope.clientOptions = data;
	            });
	        };
	        $scope.loadClientOptions();
	        $scope.client = {
	            repeatSelect: null,
	            availableOptions: $scope.clientOptions
	        };
		    $scope.prefsByClient = function(clientId){
		    	$scope.clientId = clientId == '' ? 'undefined' : clientId;
		    	$location.path(currentPath.substring(0, currentPath.lastIndexOf("/")+1).concat($scope.clientId));
		    	$route.reload();
		    };
		    if(angular.isDefined($scope.prefSubject)){
		    	lookupServiceAjax.prefGroupBySubject($scope.prefSubject).then(function(data){
	                $scope.prefGroup = data;
	            });
		    }
	    }
	    
	    if(currentPath.match("prefSubjects/list") || currentPath.match("prefSubjects/items")){
	    	lookupServiceAjax.prefSubjects('undefined', 'undefined').then(function(data){
	    		$scope.allPrefSubjects = data;
	    	});
	    }
	    
  }]);

angular.module('capApp')
.controller('AddLookupModalInstanceCtrl', function ($scope, $uibModalInstance, $uibModal, lookupServiceAjax, typeId, prefGroup, prefSubject, clientId, toasty,$translatePartialLoader, $translate, $localStorage) {

	$.fn.dataTable.ext.errMode = 'none';
	var token = $localStorage.oauthToken;
	  
	  
  var defaultItem = {}
  if(typeId == 'PaperType'){
	  defaultItem = {
			"shortName":"",
	        "dropFolder":"",
			"thickness":0,
			"weight":0
	  };
  }else if(typeId == 'PaperTypeMedia'){
	  defaultItem = {
			"id":"",
			"name":"",
			"paperTypeId":"",
			"rollWidth":0,
			"rollLength":0,
			"description":""
	  };
  }else if(typeId == 'Preference'){
	  defaultItem = {
			"id":"",
			"name":"",
			"description":"",
			"groupingValue":prefGroup,
			"prefSubject":prefSubject,
			"orderId":"",
			"partNum":"",
			"clientId":clientId
	  };
  }else{
	  defaultItem = {
				"id":"",
				"name":"",
				"description":""
		  };
  }
    
  $scope.loadClientOptions = function() {
      lookupServiceAjax.readAll('Client').then(function(data) {
          $scope.clientOptions = data;
      });
  };
  $scope.loadClientOptions();
  $scope.client = {
      repeatSelect: null,
      availableOptions: $scope.clientOptions
  };
  
  var alertAddMessage = $translate.instant('LOOKUPS_JS.ITEM_ADDED');
  $scope.typeId = typeId;
  $scope.errors = jQuery.extend({}, defaultItem);
  $scope.item = jQuery.extend({}, defaultItem);
  $scope.$parent.successMsg = false;
  $scope.$parent.errorMsg = false;
  $scope.errorMsg = false;
  $scope.addItem = function(){
     lookupServiceAjax.addItem(typeId, $scope.item)
         .then( function(data, status, headers, config){
        	 $scope.errors = jQuery.extend({}, defaultItem);
        	 $scope.$parent.alertMsg = alertAddMessage;
             //$scope.$parent.successMsg = true;
        	 toasty.success({
                 title: $translate.instant('LOOKUPS_JS.ADDING_ITEM'),
                 msg:  $translate.instant('LOOKUPS_JS.ITEM_SUCC_ADDED'),
                 showClose: true,
                 clickToClose: true,
                 timeout: 10000,
                 sound: false,
                 html: false,
                 shake: false,
                 theme: "bootstrap"
             });
            $uibModalInstance.close(data);
           },function(data, status, headers, config){
        	  $scope.successMsg = false;
        	  $scope.errorMsg = true;
            $scope.errors = data.data.errors.errors;
          }) ;
  	};
  $scope.closeModal = function(){
	  $uibModalInstance.dismiss();
  };
  
  $scope.openPrefNamingConventionsModal = function () {
		lookupServiceAjax.prefSubjectsItems('Naming_Convention', clientId).then(function(data){
	          $scope.prefNamingConventions = data;
	    });
	    var modalInstance = $uibModal.open({
	  	  backdrop  : 'static',
	      keyboard  : false,
	      animation: true,
	      templateUrl: './views/pnlPreferenceNamingConvention.html',
	      scope: $scope,
	      size: 'lg',
	      resolve: {
	      }
	    });
	    modalInstance.result.then(function (prefNC) {
	    	if(angular.isDefined(prefNC)){
	    		$scope.item.id = $scope.item.id + prefNC;
	    	}
	    }, function () {});
	    
	    $scope.closePrefNCModal = function(prefNC){
			modalInstance.close(prefNC);
		};
  };
});

angular.module('capApp')
.controller('EditLookupModalInstanceCtrl', function ($scope, $uibModalInstance, $uibModal, $confirm, DTOptionsBuilder, defaultStationServiceAjax, lookupServiceAjax, typeId, itemId, toasty,$translatePartialLoader, $translate, $localStorage, pnlPreviewService) {

	$.fn.dataTable.ext.errMode = 'none';
	var token = $localStorage.oauthToken;
	  
	  
	var defaultItem = {}
	  if(typeId == 'PaperType'){
		  defaultItem = {
				"shortName":"",
				"dropFolder":"",
				"thickness":0,
				"weight":0
		  };
	  }else if(typeId == 'PaperTypeMedia'){
		  defaultItem = {
				"id":"",
				"name":"",
				"paperTypeId":"",
				"rollWidth":0,
				"rollLength":0,
				"description":""
		  };
	  }else if(typeId == 'Preference'){
		  defaultItem = {
				"id":"",
				"name":"",
				"description":"",
				"groupingValue":""
		  };
	  }else{
		  defaultItem = {
					"id":"",
					"name":"",
					"description":""
			  };
	  }
	
	
  var alertUpdateMessage = "Item Updated!!";
  $scope.errors = jQuery.extend({}, defaultItem);
  $scope.$parent.successMsg = false;
  $scope.$parent.errorMsg = false;
  $scope.errorMsg = false;
  
  lookupServiceAjax.getItemById(typeId, itemId).then(function(data){
      $scope.item = data;
      $scope.typeId = typeId; 

  });
   $scope.update = function(){
	   $('#updateItemBtn').removeAttr('disabled');
	  lookupServiceAjax.updateItem(typeId, $scope.item)
      .then(function(){
    	$scope.errors = jQuery.extend({}, defaultItem);
    	$scope.$parent.alertMsg = alertUpdateMessage;
       // $scope.$parent.successMsg = true;
    	toasty.success({
            title: $translate.instant('LOOKUPS_JS.UPDATING'),
            msg: $translate.instant('LOOKUPS_JS.ITEM') + $scope.item.id +  $translate.instant('LOOKUPS_JS.UPDATED'),
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
        $scope.errors = data.data.errors.errors;
      }) ;
   };
   $scope.closeModal = function(){
	  $uibModalInstance.dismiss();
   };
  
   $scope.dtOptionsForLookupDetails = DTOptionsBuilder.fromSource().withDOM('frtip').withOption('order', []).withPaginationType('full_numbers').withDisplayLength(5);

   $scope.dtOptionsForTmpLineDetails = DTOptionsBuilder.fromSource().withDOM('rtip').withOption('order', []).withPaginationType('full_numbers').withDisplayLength(5);
   
   $scope.openEditDefaultStationModal = function (id) {
      var modalInstance = $uibModal.open({
        animation: true,
        backdrop  : 'static',
    	keyboard  : false,
        templateUrl: 'editDefaultStationModalContent.html',
        controller: 'EditDefaultStationModalInstanceCtrl',
        scope: $scope,
        size: 'lg',
        resolve: {
    		defaultStationId: function () {
    		 return id;
        }
        }
      });
      modalInstance.result.then(function (result) {
    	  lookupServiceAjax.getItemById(typeId, itemId).then(function(data){
    	      $scope.item = data;
    	      $scope.typeId = typeId;
    	  });
      }, function () {});
    };
    
    $scope.openAddDefaultStationModal = function (partCategoryId) {
        var modalInstance = $uibModal.open({
          animation: true,
          backdrop  : 'static',
      	  keyboard  : false,
          templateUrl: 'addDefaultStationModalContent.html',
          controller: 'AddDefaultStationModalInstanceCtrl',
          scope: $scope,
          size: 'lg',
          resolve: {
        	partCategoryId: function () {
      		 return partCategoryId;
            }
          }
        });
        modalInstance.result.then(function (result) {
        	lookupServiceAjax.getItemById(typeId, itemId).then(function(data){
        	      $scope.item = data;
        	      $scope.typeId = typeId; 
        	  });
        }, function () {});
     };
     
     $scope.deleteDefaultStation = function(id){
   	  	 $scope.errorMsg = false;
         $scope.successMsg = false;
   	  	 $confirm({text: $translate.instant('LOOKUPS_JS.SURE_DELETE_STATION'), title: $translate.instant('LOOKUPS_JS.DELETE_STATION'), ok: 'Ok', cancel: $translate.instant('LOOKUPS_JS.CANCEL')})
	        .then(function() {
	        	var ds = id.categoryId + "_" + id.critiriaId + "_" + id.bindingTypeId + "_" + id.stationCategoryId;
	        	defaultStationServiceAjax.deleteDefaultStation(ds).then(function(data){
	        		lookupServiceAjax.getItemById(typeId, itemId).then(function(data){
	          	      $scope.item = data;
	          	      $scope.typeId = typeId; 
	          	  });
	           $scope.successMsg = true;
	           $scope.errorMsg = false;
	           $scope.alertMsg = $translate.instant('LOOKUPS_JS.DEFAULT_STATION');
	           
		       }, function(data, status, headers, config){
	            $scope.errorMsg = true;
	            $scope.successMsg = false;
	            $scope.alertMsg = data.data.errors.errors;
	      });
	     });
	 };
	 
	 $scope.openEditTemplateLineModal = function (id) {
	      var modalInstance = $uibModal.open({
	        animation: true,
	        backdrop  : 'static',
	    	keyboard  : false,
	        templateUrl: 'editTemplateLineModalContent.html',
	        controller: 'EditTemplateLineModalInstanceCtrl',
	        scope: $scope,
	        size: 'lg',
	        resolve: {
	    		tmpLineId: function () {
	    		 return id;
	    		},
	    		typeId: function () {
		    		 return $scope.typeId;
		    	}
	        }
	      });
	      modalInstance.result.then(function (result) {
	    	  lookupServiceAjax.getItemById(typeId, itemId).then(function(data){
	    	      $scope.item = data;
	    	      $scope.typeId = typeId;
	    	  });
	      }, function () {});
	    };
	    
	    $scope.openAddTemplateLineModal = function (tmplId) {
	        var modalInstance = $uibModal.open({
	          animation: true,
	          backdrop  : 'static',
	      	  keyboard  : false,
	          templateUrl: 'addTemplateLineModalContent.html',
	          controller: 'AddTemplateLineModalInstanceCtrl',
	          scope: $scope,
	          size: 'lg',
	          resolve: {
	        	tmplId: function () {
	      		 return tmplId;
	            },
	            typeId: function () {
		    		 return $scope.typeId;
		    	},
	            ordering: function () {
		    		 return $scope.item.templateLines.length > 0 ? $scope.item.templateLines.length + 1 : 1;
		    	}
	          }
	        });
	        modalInstance.result.then(function (result) {
	        	lookupServiceAjax.getItemById(typeId, itemId).then(function(data){
	        	      $scope.item = data;
	        	      $scope.typeId = typeId; 
	        	  });
	        }, function () {});
	     };
	     
	     $scope.deleteTemplateLine = function(id){
	   	  	 $scope.errorMsg = false;
	         $scope.successMsg = false;
	   	  	 $confirm({text: $translate.instant('LOOKUPS_JS.SURE_DELETE_TMPLINE'), title: $translate.instant('LOOKUPS_JS.DELETE_TMPLINE'), ok: 'Ok', cancel: $translate.instant('LOOKUPS_JS.CANCEL')})
		        .then(function() {
		        	lookupServiceAjax.deleteTemplateLine(id, $scope.typeId).then(function(data){
		        		lookupServiceAjax.getItemById(typeId, itemId).then(function(data){
		          	      $scope.item = data;
		          	      $scope.typeId = typeId; 
		          	  });
		           $scope.successMsg = true;
		           $scope.errorMsg = false;
		           $scope.alertMsg = $translate.instant('LOOKUPS_JS.DEFAULT_STATION');
		           
			       }, function(data, status, headers, config){
		            $scope.errorMsg = true;
		            $scope.successMsg = false;
		            $scope.alertMsg = data.data.errors.errors;
		      });
		     });
		 };
		 
		 
		 $scope.openPreviewModal = function (tepmlateId) {
			//call ws to resolve the pnl and send it as test
			 pnlPreviewService.getResolvedPnl(tepmlateId).then(function (resolvedTemplate) {
		            
					//open preview modal			
					pnlPreviewService.showPreviewModal(resolvedTemplate);
				   	
				   	
	
		       });

		 };
	   
});

angular.module('capApp')
.controller('AddDefaultStationModalInstanceCtrl', function ($scope, $uibModalInstance, lookupServiceAjax, defaultStationServiceAjax, partCategoryId, toasty ,$translatePartialLoader, $translate, $localStorage) {
	
	$.fn.dataTable.ext.errMode = 'none';
	var token = $localStorage.oauthToken;
	  
	var defaultStation = {
		    "id":{"categoryId":partCategoryId,"critiriaId":"","bindingTypeId":"","stationCategoryId":""},
		    "productionOrdering":""
	  };
	 
	  $scope.defaultStation = jQuery.extend({}, defaultStation);
	  
	  var alertAddMessage =  $translate.instant('LOOKUPS_JS.DEFAULT_STATION_ADDED') +"!!";
	  
	  $scope.errors = jQuery.extend({}, defaultStation);
	  $scope.successMsg = false;
	  $scope.errorMsg = false;
	  
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
	    
	  $scope.addDefaultStation = function(){
		  defaultStationServiceAjax.addDefaultStation($scope.defaultStation)
	          .then( function(data, status, headers, config){
	        	  $scope.errors = jQuery.extend({}, defaultStation);
	              $scope.$parent.alertMsg = alertAddMessage;
	             // $scope.$parent.successMsg = true;
	              toasty.success({
	                  title: $translate.instant('LOOKUPS_JS.ADDING_DEFAULT_STATION'),
	                  msg: $translate.instant('LOOKUPS_JS.DEFAULT_STATION_ADDED'),
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

angular.module('capApp')
.controller('EditDefaultStationModalInstanceCtrl', function ($scope, $uibModalInstance, defaultStationServiceAjax, defaultStationId, toasty,$translatePartialLoader, $translate, $localStorage) {
	
	$.fn.dataTable.ext.errMode = 'none';
	var token = $localStorage.oauthToken;
	
	var defaultStation = {
		    "id":defaultStationId,
		    "productionOrdering":""
	  };
	  
	  var alertUpdateMessage = $translate.instant('LOOKUPS_JS.DEFAULT_STATION_UPDATED');
	  $scope.errors = jQuery.extend({}, defaultStation);
	  $scope.$parent.successMsg = false;
	  $scope.$parent.errorMsg = false;
	  $scope.errorMsg = false;
	  var ds = defaultStationId.categoryId + "_" + defaultStationId.critiriaId + "_" + defaultStationId.bindingTypeId + "_" + defaultStationId.stationCategoryId;
  	
	  defaultStationServiceAjax.getDefaultStationById(ds).then(function(data){
	        $scope.defaultStation = data;
	        $scope.updateDefaultStation = function(){
	        	defaultStationServiceAjax.updateDefaultStation($scope.defaultStation)
	            .then(function(){
	          	$scope.alertMsg = alertUpdateMessage;
	              $scope.errors = jQuery.extend({}, defaultStation);
	              $scope.$parent.alertMsg = alertUpdateMessage;
	             // $scope.$parent.successMsg = true;
	              toasty.success({
	                  title: $translate.instant('LOOKUPS_JS.UPDATING_DEFAULT_STATION'),
	                  msg:  $translate.instant('LOOKUPS_JS.DEFAULT_STATION_UPDATED'),
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
.controller('TemplateLineModalInstanceCtrl', function ($scope, $uibModal, lookupServiceAjax, $translatePartialLoader, $translate, $localStorage) {

	$scope.openTemplateNamingConventionsModal = function () {
		lookupServiceAjax.templateNamingConventions().then(function(data){
	          $scope.templateNamingConventions = data;
	    });
	    var modalInstance = $uibModal.open({
	  	  backdrop  : 'static',
	      keyboard  : false,
	      animation: true,
	      templateUrl: './views/pnlStandardNamingConvention.html',
	      scope: $scope,
	      size: 'lg',
	      resolve: {
	      }
	    });
	    modalInstance.result.then(function (tmplNC) {
	    	if(angular.isDefined(tmplNC)){
	    		$scope.tmpLine.lineText = $scope.tmpLine.lineText + '%' + tmplNC + '%';
	    	}
	    }, function () {});
	    
	    $scope.closeTmplNCModal = function(tmplNC){
			modalInstance.close(tmplNC);
		};
	};
	
});

angular.module('capApp')
.controller('AddTemplateLineModalInstanceCtrl', function ($scope, $uibModalInstance, $controller, lookupServiceAjax, tmplId, typeId, ordering, toasty ,$translatePartialLoader, $translate, $localStorage) {
	
	  $controller('TemplateLineModalInstanceCtrl', { $scope: $scope });
	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  var tmpLine = {
		    "id":"",
		    "templateId":tmplId,
			"ordering":ordering,
			"lineText":"",
			"fontType":"",
			"fontSize":"",
			"fontBold":"",
			"fontItalic":""
	  };
	  $scope.tmpLine = jQuery.extend({}, tmpLine);
	  
	  var alertAddMessage =  $translate.instant('LOOKUPS_JS.TMPL_LINE_ADDED');
	  
	  $scope.errors = jQuery.extend({}, tmpLine);
	  $scope.successMsg = false;
	  $scope.errorMsg = false;
	    
	  $scope.addTemplateLine = function(){
		  lookupServiceAjax.addTemplateLine($scope.tmpLine, typeId)
	          .then( function(data, status, headers, config){
	        	  $scope.errors = jQuery.extend({}, tmpLine);
	              $scope.$parent.alertMsg = alertAddMessage;
	             // $scope.$parent.successMsg = true;
	              toasty.success({
	                  title: $translate.instant('LOOKUPS_JS.ADDING_TMPL_LINE'),
	                  msg: $translate.instant('LOOKUPS_JS.TMPL_LINE_ADDED'),
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

angular.module('capApp')
.controller('EditTemplateLineModalInstanceCtrl', function ($scope, $uibModalInstance, $controller, lookupServiceAjax, tmpLineId, typeId, toasty,$translatePartialLoader, $translate, $localStorage) {
	
	  $controller('TemplateLineModalInstanceCtrl', { $scope: $scope });
	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  var tmpLine = {
		    "id":"",
		    "templateId":"",
			"ordering":"",
			"lineText":"",
			"fontType":"",
			"fontSize":"",
			"fontBold":"",
			"fontItalic":""
	  };
	  
	  var alertUpdateMessage = $translate.instant('LOOKUPS_JS.TMPL_LINE_UPDATED');
	  $scope.errors = jQuery.extend({}, tmpLine);
	  $scope.$parent.successMsg = false;
	  $scope.$parent.errorMsg = false;
	  $scope.errorMsg = false;
	  
	  lookupServiceAjax.getTmpLineById(tmpLineId, typeId).then(function(data){
	        $scope.tmpLine = data;
	        $scope.updateTemplateLine = function(){
	        	lookupServiceAjax.updateTemplateLine($scope.tmpLine, typeId)
	            .then(function(){
	          	$scope.alertMsg = alertUpdateMessage;
	              $scope.errors = jQuery.extend({}, tmpLine);
	              $scope.$parent.alertMsg = alertUpdateMessage;
	             // $scope.$parent.successMsg = true;
	              toasty.success({
	                  title: $translate.instant('LOOKUPS_JS.UPDATING_TMPL_LINE'),
	                  msg:  $translate.instant('LOOKUPS_JS.TMPL_LINE_UPDATED'),
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

