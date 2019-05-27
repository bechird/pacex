'use strict';

/**
 * @ngdoc function
 * @name capApp.controller:RolesListCtrl
 * @description
 * # RolesListCtrl
 * Controller of the capApp
 */
angular.module('capApp')
  .controller('RolesListCtrl', ['DTOptionsBuilder','DTColumnBuilder', '$scope', '$uibModal', '$log', 'roleServiceAjax','SweetAlert', '$confirm', 'toasty', 
	  		  '$translatePartialLoader', '$translate', '$localStorage', '$rootScope',
    function (DTOptionsBuilder, DTColumnBuilder, $scope, $uibModal, $log, roleServiceAjax, SweetAlert, $confirm, toasty , $translatePartialLoader, $translate, $localStorage, $rootScope) {
	  
	  
	  $translatePartialLoader.addPart('rolesList');
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
      vm.dtOptions = DTOptionsBuilder.fromSource($rootScope.API_BASE+'/roles?access_token=' + token)
          .withDOM('frtip')
          .withPaginationType('full_numbers')
          .withOption('responsive', true)
          //.withOption('dom', '<"top"i>rt<"bottom"flp><"clear">')
          .withDisplayLength(20)
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
          .withLanguage(dataTables);;

      vm.dtColumns = [
          DTColumnBuilder.newColumn('roleId').withTitle($translate.instant('role_js.ROLE_ID')),
          DTColumnBuilder.newColumn('roleName').withTitle( $translate.instant('role_ROLE_NAME')),
          DTColumnBuilder.newColumn('roleDescription').withTitle($translate.instant('role_js.ROLE_DESC')),
          // .notVisible() does not work in this case. Use .withClass('none') instead
          DTColumnBuilder.newColumn('roleId').withTitle(' ').notSortable().renderWith(function (data) {
        	  return "<button type='button' onclick=\"var $scope = angular.element(event.target).scope(); $scope.openEditRoleModal('"+data+"'); $scope.$apply()\" uib-tooltip='Edit' title='"+$translate.instant('role_js.EDIT')+"' class='btn bgm-orange-900 waves-circle waves-effect'><i class='zmdi zmdi-edit'></i></button>&nbsp;<button type='button' onclick=\"var $scope = angular.element(event.target).scope(); $scope.deleteRole('"+data+"'); $scope.$apply()\" uib-tooltip='Delete' title='"+$translate.instant('role_js.DELETE_js')+"' class='btn bgm-deeporange waves-circle waves-effect'><i class='zmdi zmdi-delete'></i></button>";
   
          })
      ];

 /*     $scope.deleteRole = function(id){
    	  $scope.errorMsg = false;
          $scope.successMsg = false;
    	  $confirm({text: 'Are you sure you want to delete the role?', title: 'Delete Role', ok: 'Ok', cancel: 'Cancel'})
    	        .then(function() {
    	    roleServiceAjax.deleteRole(id).then(function(data){
            $scope.roles = data;
            vm.dtInstance.reloadData();
            $scope.successMsg = true;
            $scope.errorMsg = false;
            $scope.alertMsg = "Role successfully deleted!";
            }, function(data, status, headers, config){
	            $scope.errorMsg = true;
	            $scope.successMsg = false;
	            $scope.alertMsg = data.errors.errors;
	        });
    	  });
      };*/
      $scope.deleteRole = function(id) {
          $scope.errorMsg = false;
          $scope.successMsg = false;

          SweetAlert.swal({
                  title:  $translate.instant('role_js.DeleteRole'),
                  text:  $translate.instant('role_js.delete_sure'),
                  type: "warning",
                  showCancelButton: true,
                  confirmButtonColor: "#DD6B55",
                  confirmButtonText:  $translate.instant('role_js.Yes_delete'),
                  closeOnConfirm: true
              },
              function(isConfirm) {
                  if (isConfirm) {
                     roleServiceAjax.deleteRole(id).then(function(data){
                      $scope.roles = data;
                      vm.dtInstance.reloadData();
                          toasty.success({
    		                  title: $translate.instant('role_js.DeleteRole'),
    		                  msg: $translate.instant('role_js.Role_succ_deleted') ,
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
                          SweetAlert.swal($translate.instant('role_js.DeletionError'), data.data.errors.errors, "error");
                      });
                  }
              });
      };
      $scope.openEditRoleModal = function (id) {
        var modalInstance = $uibModal.open({
        	backdrop  : 'static',
        	keyboard  : false,
        	animation: true,
        	templateUrl: 'editRoleModalContent.html',
        	controller: 'EditRoleModalInstanceCtrl',
        	scope: $scope,
        	resolve: {
        		roleId: function () {
        		return id;
            }
          }
        });
        modalInstance.result.then(function (selectedItem) {
        	vm.dtInstance.reloadData();
        }, function () {});
     //console.log("DEBUG : roles edit callback");
      };
      
      $scope.openAddRoleModal = function () {
        var modalInstance = $uibModal.open({
          animation: true,
          backdrop  : 'static',
      	  keyboard  : false,
          templateUrl: 'addRoleModalContent.html',
          controller: 'AddRoleModalInstanceCtrl',
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
  .controller('AddRoleModalInstanceCtrl', function ($scope, $uibModalInstance, roleServiceAjax, toasty,$translatePartialLoader, $translate, $localStorage) {

	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	  var defaultRole = {
		    "id":"",
		    "name":"",
		    "description":""
		  };
	  
	  //var ctrl = $controller('MyDirectiveController', { /* no locals */ }, { name: 'Clark Kent' });
      
	  var alertAddMessage = $translate.instant('role_js.Role_Added') +" !!";
	  $scope.role = jQuery.extend({}, defaultRole);
	  $scope.errors = jQuery.extend({}, defaultRole);
	  $scope.$parent.successMsg = false;
	  $scope.$parent.errorMsg = false;
	  $scope.errorMsg = false;
	  
	  $scope.addRole = function(){
    	  roleServiceAjax.addRole($scope.role)
	          .then( function(data, status, headers, config){
	              $scope.errors = jQuery.extend({}, defaultRole);
	              $scope.$parent.alertMsg = alertAddMessage;
	              //$scope.$parent.successMsg = true;
	              toasty.success({
	                  title: $translate.instant('role_js.Adding_Role'),
	                  msg: $translate.instant('role_js.Role_succ_added'),
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
  .controller('EditRoleModalInstanceCtrl', function ($scope, $uibModalInstance, roleServiceAjax, roleId, toasty,$translatePartialLoader, $translate, $localStorage) {

	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	  var defaultRole = {
		    "id":"",
		    "name":"",
		    "description":""
		  };
	  
	  var alertUpdateMessage = $translate.instant('role_js.Role_Updated') +" !!";
	  $scope.errors = jQuery.extend({}, defaultRole);
	  $scope.$parent.successMsg = false;
	  $scope.$parent.errorMsg = false;
	  $scope.errorMsg = false;
	  
	  roleServiceAjax.getRoleById(roleId).then(function(data){
	        $scope.role = data;
	        $scope.updateRole = function(){
	      	  roleServiceAjax.updateRole($scope.role)
	            .then(function(){
	          	$scope.alertMsg = alertUpdateMessage;
	              $scope.errors = jQuery.extend({}, defaultRole);
	              $scope.$parent.alertMsg = alertUpdateMessage;
	              //$scope.$parent.successMsg = true;
	              toasty.success({
	                  title: $translate.instant('role_js.Updating_Role'),
	                  msg: $translate.instant('role_js.Role_succ_updated'),
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
  
