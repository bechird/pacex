'use strict';

/**
 * @ngdoc function
 * @name capApp.controller:UsersListCtrl
 * @description
 * # UsersListCtrl
 * Controller of the capApp
 */
angular.module('capApp')
  .controller('UsersListCtrl', ['DTOptionsBuilder','$templateCache','DTColumnBuilder', '$scope', '$uibModal', '$log', 'userServiceAjax','SweetAlert', '$confirm', 'toasty', 
	  		  '$translatePartialLoader', '$translate','$filter', '$localStorage', '$rootScope',
    function (DTOptionsBuilder, $templateCache,DTColumnBuilder, $scope, $uibModal, $log, userServiceAjax, SweetAlert, $confirm, toasty , $translatePartialLoader, 
    			  $translate, $filter, $localStorage, $rootScope) {
	
	  $translatePartialLoader.addPart('usersList');
	  $translate.refresh();
	  
	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	  var vm = this;
	  $scope.successMsg = false;
	  $scope.errorMsg = false;
	  
	  $scope.LanguageOptions = ["en","fr","es"];

	  
	  
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
      vm.dtOptions = DTOptionsBuilder.fromSource($rootScope.API_BASE+'/users?access_token=' + token)
          .withDOM('frtip')
          .withPaginationType('full_numbers')
          .withOption('responsive', true)
          //.withOption('dom', '<"top"i>rt<"bottom"flp><"clear">')
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
                        type: 'text',
                        bRegex: true,
                        bSmart: true
                      },{
                    	 type: 'boolean'
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
          
    //  var firstName = $translate.instant('js.FirstName');
      vm.dtColumns = [
          DTColumnBuilder.newColumn('firstName').withTitle($translate.instant('js.FirstName')),
          DTColumnBuilder.newColumn('lastName').withTitle($translate.instant('js.LastName')),
          DTColumnBuilder.newColumn('email').withTitle('Email'),
          DTColumnBuilder.newColumn('phoneNum').withTitle($translate.instant('js.Phone')),
          DTColumnBuilder.newColumn('language').withTitle($translate.instant('js.Language')),
          DTColumnBuilder.newColumn('activeFlag').withTitle($translate.instant('js.Active')),
          DTColumnBuilder.newColumn('roles').withTitle($translate.instant('js.UserRoles')),
          DTColumnBuilder.newColumn('userId').withTitle(' ').notSortable().renderWith(function (data) {
        	  return "<button type='button' onclick=\"var $scope = angular.element(event.target).scope(); $scope.openEditUserModal('"+data+"'); $scope.$apply()\" uib-tooltip='Edit' title='"+$translate.instant('js.EDIT')+"' class='btn bgm-orange-900 waves-circle waves-effect'><i class='zmdi zmdi-edit'></i></button>&nbsp;<button type='button' onclick=\"var $scope = angular.element(event.target).scope(); $scope.deleteUser('"+data+"'); $scope.$apply()\" uib-tooltip='Delete'  title='"+$translate.instant('js.DELETE_js')+"' class='btn bgm-deeporange waves-circle waves-effect'><i class='zmdi zmdi-delete'></i></button>";
   
          })
      ];
      
   /*   $scope.deleteUser = function(id){
    	  $scope.errorMsg = false;
          $scope.successMsg = false;
    	  $confirm({text: 'Are you sure you want to delete the user?', title: 'Delete User', ok: 'Ok', cancel: 'Cancel'})
	        .then(function() {
    	  userServiceAjax.deleteUser(id).then(function(data){
            $scope.users = data;
            vm.dtInstance.reloadData();
            $scope.successMsg = true;
            $scope.errorMsg = false;
            $scope.alertMsg = "User successfully deleted!";
            
    	  }, function(data, status, headers, config){
	            $scope.errorMsg = true;
	            $scope.successMsg = false;
	            $scope.alertMsg = data.errors.errors;
	      });
	     });
	   };*/
      $scope.deleteUser = function(id) {
          $scope.errorMsg = false;
          $scope.successMsg = false;

          SweetAlert.swal({
                  title: $translate.instant('js.TITLE_DELETE'),
                  text: $translate.instant('js.TEXT_DELETE'),
                  type: "warning",
                  showCancelButton: true,
                  confirmButtonColor: "#DD6B55",
                  confirmButtonText: $translate.instant('js.CONFIRM_DELETE'),
                  closeOnConfirm: true
              },
              function(isConfirm) {
                  if (isConfirm) {
                      userServiceAjax.deleteUser(id).then(function(data) {
                          $scope.users = data;
                          vm.dtInstance.reloadData();
                          toasty.success({
       		                  title: $translate.instant('js.DeleteRole'),
       		                  msg: $translate.instant('js.ROLE_DELETED'),
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
                          SweetAlert.swal($translate.instant('js.DeletionError'), data.data.errors.errors, "error");
                      });
                  }
              });
      };
      
      $scope.openEditUserModal = function (id) {
        var modalInstance = $uibModal.open({
        	backdrop  : 'static',
        	keyboard  : false,
        	animation: true,
        	templateUrl: 'editUserModalContent.html',
        	controller: 'EditUserModalInstanceCtrl',
        	size: 'lg',
        	scope: $scope,
        	resolve: {
        		userId: function () {
        		return id;
            }
          }
        });

        modalInstance.result.then(function (selectedItem) {
        	vm.dtInstance.reloadData();
        }, function () {});
     //console.log("DEBUG : roles edit callback");
      };
      
      $scope.openAddUserModal = function () {
        var modalInstance = $uibModal.open({
          animation: true,
          backdrop  : 'static',
      	  keyboard  : false,
          templateUrl: 'addUserModalContent.html',
          controller: 'AddUserModalInstanceCtrl',
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
  .controller('AddUserModalInstanceCtrl', function ($scope, $uibModalInstance, userServiceAjax, toasty ,$translate, $localStorage) {

	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	
	  var defaultUser = {
		    "firstName":"",
		    "lastName":"",
		    "email":"",
		    "phoneNum":"",
		    "loginName":"",
		    "loginPassword":"",
		    "activeFlag":"",
			"language":""
		  };
	  var alertAddMessage =  $translate.instant('js.USER_UPDATED');
	  $scope.user = jQuery.extend({}, defaultUser);
	  $scope.errors = jQuery.extend({}, defaultUser);
	  $scope.$parent.successMsg = false;
	  $scope.$parent.errorMsg = false;
	  $scope.errorMsg = false;
	  $scope.loadRoleOptions = function(){
	         userServiceAjax.roleOptions().then(function(data){
	            $scope.roleOptions = data;
	         });
	    };
		
	       
	    $scope.loadRoleOptions();
	    $scope.userRoles = {
		    repeatSelect: null,
		    availableOptions: $scope.roleOptions,
		};
	    $scope.user.activeFlag = true;
	    
	  $scope.addUser = function(){
		  if($scope.user.loginName == '' || $scope.user.loginName == null){
			  $scope.user.loginName = $scope.user.email;
		  }
    	  userServiceAjax.addUser($scope.user)
	          .then( function(data, status, headers, config){
	        	  $scope.errors = jQuery.extend({}, defaultUser);
	              $scope.$parent.alertMsg = alertAddMessage;
	             // $scope.$parent.successMsg = true;
	              toasty.success({
	                  title: $translate.instant('js.ADING_USER'),
	                  msg: $translate.instant('js.USER_ADDED'),
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
	            $scope.errors = data.data.errors.errors;
	          });
	  };
	  $scope.closeModal = function(){
		  $uibModalInstance.dismiss();
	  };
  });
  
  angular.module('capApp')
  .controller('EditUserModalInstanceCtrl', function ($scope, $uibModalInstance, userServiceAjax, userId, toasty, $translate, $localStorage) {

	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	  var defaultUser = {
			  "firstName":"",
			    "lastName":"",
			    "email":"",
			    "phoneNum":"",
			    "loginName":"",
			    "loginPassword":"",
			    "activeFlag":""
	  };
	  
	  var alertUpdateMessage =  $translate.instant('js.USER_UPDATED');
	  $scope.errors = jQuery.extend({}, defaultUser);
	  $scope.$parent.successMsg = false;
	  $scope.$parent.errorMsg = false;
	  $scope.errorMsg = false;
	  
	  $scope.loadRoleOptions = function(){
	         userServiceAjax.roleOptions().then(function(data){
	            $scope.roleOptions = data;
	         });
	    };
	    $scope.loadRoleOptions();
	    $scope.userRoles = {
		    repeatSelect: null,
		    availableOptions: $scope.roleOptions,
		};
	    
	  userServiceAjax.getUserById(userId).then(function(data){
	        $scope.user = data;
	        $scope.user.loginPassword = "******";
	        $scope.updateUser = function(){
	        	$scope.user.rolesOrigin = $scope.user.roles;
	      	  userServiceAjax.updateUser($scope.user)
	            .then(function(){
	          	$scope.alertMsg = alertUpdateMessage;
	              $scope.errors = jQuery.extend({}, defaultUser);
	              $scope.$parent.alertMsg = alertUpdateMessage;
	              //$scope.$parent.successMsg = true;
	              toasty.success({
	                  title: $translate.instant('js.EDDINT_USER'),
	                  msg: $translate.instant('js.USER_UPDATED2'),
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
	  	});
	  $scope.closeModal = function(){
		  $uibModalInstance.dismiss();
	  };
  });
  
