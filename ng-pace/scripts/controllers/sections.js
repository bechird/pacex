	'use strict';
/**
 * @ngdoc function
 * @name capApp.controller:sectionsCtrl
 * @description
 * # RollsCtrl
 * Controller of the capApp
 */
angular.module('capApp')
.controller('SectionsListCtrl', ['DTOptionsBuilder','DTColumnBuilder', '$scope', '$log', 'rollServiceAjax', 'SweetAlert', 'machineServiceAjax', 'lookupServiceAjax', '$confirm', '$uibModal', 
			'$filter', 'toasty', '$translatePartialLoader', '$translate', '$localStorage', '$rootScope', '$http',
    function (DTOptionsBuilder, DTColumnBuilder, $scope, $log, rollServiceAjax, SweetAlert, machineServiceAjax, lookupServiceAjax, $confirm, $uibModal, $filter, toasty, $translatePartialLoader , 
    		      $translate, $localStorage, $rootScope, $http) {

		var monthNames = ["Jan ", $translate.instant('productionDashboard_js.FEB'), "Mar ",  $translate.instant('productionDashboard_js.APR'),  $translate.instant('productionDashboard_js.MAY'),  $translate.instant('productionDashboard_js.JUN'), $translate.instant('productionDashboard_js.JUL'), $translate.instant('productionDashboard_js.AUG'), "Sep ", "Oct ", "Nov ", "Dec "];

		$.fn.dataTable.ext.errMode = 'none';
		var token = $localStorage.oauthToken;
	
	    var vm = this;
	    
        $scope.successMsg = false;
	    $scope.errorMsg = false;
	    $scope.defaultSection = {
				 "Section_Id":"",
				 "Creation_Date":"",
				 "Creator_id":"",
				 "Modification_Date":"",
				 "Modifier_Id":"",
				 "batchId":"",
				 "copyStatus":"",
				 "Section_Name":"",
				 "dueDate":"",
				 "Machine_Id":"",
				 "Machine_Ordering":"",
				 "path":"",
				 "priority":"",
				 "Quantity":"",
				 "Lamination":"",
				 "Status":"",
				 "binderId":""
		};
	    
	    vm.dtInstance = {};
	    
	    
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
	    
	     	var serverData = function(sSource, aoData, fnCallback, oSettings) {
		           $http.post($rootScope.API_BASE+"/sections/paginated",{aoData}).then(function(result){
		        	   
		        	   var records = {
		                       'draw': result.data.draw,
		                       'recordsTotal': result.data.recordsTotal,
		                       'recordsFiltered': result.data.recordsFiltered,
		                       'data': result.data.data  
		                   };
		        	   
		        	   $scope.Pallettes = result.data.data;
		               fnCallback(records);
		           });
		       }
	     	
       vm.dtOptions = DTOptionsBuilder.newOptions()
       	  .withFnServerData(serverData)
          .withDOM('frtip')
          .withPaginationType('full_numbers')
          .withOption('responsive', true)
	      .withOption('processing', true)
	      .withOption('serverSide', true) 
          .withDisplayLength(25)
          .withColumnFilter({
              
              aoColumns: [
               {
				  Id: 'number'    //sectionId                
              },{
                  type: 'text',   //name
                  bRegex: true,
                  bSmart: true
              },{
                  type: 'text',   //status
                  bRegex: true,
                  bSmart: true
              },{
            	  type: 'number'   //quantity
              },{
                  type: 'text',   // lamination
                  bRegex: true,
                  bSmart: true
              },{
              	type: 'text',   //priority
                  bRegex: true,
                  bSmart: true
              },
			  {
				  type: 'date',
				bRegex: true,		//due date
				bSmart: true
			  }]
          })
          .withLanguage($scope.dataTables);

       vm.dtColumns = [
          DTColumnBuilder.newColumn('coverSectionId').withTitle($translate.instant('sections_js.Id')).withClass('text-center'),
          DTColumnBuilder.newColumn('coverSectionName').withTitle($translate.instant('sections_js.Name') ).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('status.name').withTitle($translate.instant('sections_js.Status')).withOption('defaultContent', ' '),
		  DTColumnBuilder.newColumn('laminationType.id').withTitle($translate.instant('sections_js.Quantity')).withOption('defaultContent', ' '),
		  DTColumnBuilder.newColumn('quantity').withTitle($translate.instant('sections_js.Lamination')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('priority').withTitle($translate.instant('sections_js.Priority')).withOption('defaultContent', ' '),
		  DTColumnBuilder.newColumn('dueDate').notSortable().withTitle($translate.instant('sections_js.DueDate')).
		  renderWith(function (data) {
			 var res = "_";
			 if(data > 0){
			   var date = new Date(data);
			   res = monthNames[date.getMonth()] + date.getDate();
			 }
			 return res;
		 }),
		 DTColumnBuilder.newColumn('coverSectionId').withTitle(' ').notSortable().renderWith(function (data) {
			return "<div style='display:flex;'><button type='button' uib-tooltip='Edit' title='"+$translate.instant('rolls_js.EDIT')+"' onclick=\"var $scope = angular.element(event.target).scope(); $scope.openEditSectionModal('"+data+"'); $scope.$apply()\" class='btn bgm-orange-900	 waves-circle waves-effect'><i class='zmdi zmdi-edit'></i></button>";
		})
      ];
      
	  $scope.openEditSectionModal = function (id) {
		var modalInstance = $uibModal.open({
			backdrop  : 'static',
			keyboard  : false,
			animation: true,
			templateUrl: './views/editSectionModalContent.html',
			controller: 'EditSectionModalInstanceCtrl',
			scope: $scope,
			size:'lg',
			resolve: {
				sectionId: function () {
				return id;
			}
		  }
		});
		modalInstance.result.then(function(selectedItem) {
			vm.dtInstance.reloadData();
		}, function () {});

	  };

}]);

angular.module('capApp')
	.controller('EditSectionModalInstanceCtrl', function ($scope, $uibModalInstance, $uibModal, DTOptionsBuilder, lookupServiceAjax, machineServiceAjax, batchServiceAjax, sectionId, toasty, $translatePartialLoader, $translate, $localStorage) {

		$.fn.dataTable.ext.errMode = 'none';
		var token = $localStorage.oauthToken;

		var alertUpdateMessage = $translate.instant('sections_js.Section_Updated') + "!!";
		$scope.roll = jQuery.extend({}, $scope.defaultRoll);
		$scope.errors = jQuery.extend({}, $scope.defaultRoll);
		$scope.$parent.successMsg = false;
		$scope.$parent.errorMsg = false;
		$scope.errorMsg = false;
		$scope.copyStatusOptions = ["NOT_STARTED", "IN_PROGRESS", "FINISHED", "ERROR"];

		batchServiceAjax.getSectionById(sectionId).then(function (data) {
			$scope.section = data;
		});
		$scope.updateSection = function () {
			batchServiceAjax.updateSection($scope.section)
				.then(function () {
					$scope.alertMsg = alertUpdateMessage;
					$scope.errors = jQuery.extend({}, $scope.defaultSection);
					$scope.$parent.alertMsg = alertUpdateMessage;
					//$scope.$parent.successMsg = true;
					toasty.success({
						title: $translate.instant('sections_js.Updating_Section'),
						msg: $translate.instant('sections_js.Section_Updated'),
						showClose: true,
						clickToClose: true,
						timeout: 10000,
						sound: false,
						html: false,
						shake: false,
						theme: "bootstrap"
					});
					$uibModalInstance.close();
				}, function (data, status, headers, config) {
					$scope.errorMsg = true;
					$scope.successMsg = false;
					$scope.errors = data.errors;
				});
		};
		$scope.loadSectionStatusOptions = function () {
			$scope.sectionStatusOptions = [{"creatorId":"","lastModifiedDate":1464180654000,"lastModifiedByUserName":"","id":"NEW","name":"New","description":"New"},{"creatorId":"","lastModifiedDate":1464180706000,"lastModifiedByUserName":"","id":"RETIRED","name":"Retired","description":"Exhausted"}]
			/*lookupServiceAjax.readAll('SectionStatus').then(function (data) {
				$scope.sectionStatusOptions = data;
			});*/
		};
		$scope.loadSectionStatusOptions();
		$scope.loadLaminationOptions = function () {
			lookupServiceAjax.readAll('Lamination').then(function (data) {
				$scope.laminationOptions = data;
			});
		};
		$scope.loadLaminationOptions();
		$scope.loadMachineOptions = function () {
			machineServiceAjax.machinesQuick().then(function (data) {
				$scope.machineOptions = data;
			});
		};
		$scope.loadMachineOptions();
		$scope.closeModal = function () {
			$uibModalInstance.dismiss();
		};
		$scope.startsWith = function (actual, expected) {
			var lowerStr = (actual + "").toLowerCase();
			return lowerStr.indexOf(expected.toLowerCase()) === 0;
		}
		$scope.openViewJobModal = function (id) {
			var modalInstance = $uibModal.open({
				backdrop: 'static',
				keyboard: false,
				animation: true,
				templateUrl: './views/viewJobModalContent.html',
				controller: 'EditJobModalInstanceCtrl',
				scope: $scope,
				size: 'lg',
				resolve: {
					jobId: function () {
						return id;
					}
				}
			});
			modalInstance.result.then(function (selectedItem) {
			}, function () { });
		};

	});
