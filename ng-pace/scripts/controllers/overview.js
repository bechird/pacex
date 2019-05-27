'use strict';

/**
 * @ngdoc function
 * @name capApp.controller:OverviewScheduleBoardCtrl
 * @description
 * # OverviewScheduleBoardCtrl
 * Controller of the capApp
 */
angular.module('capApp')
  .controller('OverviewCtrl', ['DTOptionsBuilder', 'DTColumnDefBuilder', '$rootScope', '$scope', '$route', '$q', '$uibModal', 'stationServiceAjax', 'jobServiceAjax', 'lookupServiceAjax', 'toasty', '$translatePartialLoader', '$translate',
   function (DTOptionsBuilder, DTColumnDefBuilder, $rootScope, $scope, $route, $q, $uibModal, stationServiceAjax, jobServiceAjax, lookupServiceAjax, toasty , $translatePartialLoader, $translate) {
	  
	  $translatePartialLoader.addPart('overview');
	  $translate.refresh();
	  
	   var vm = this;
	   $scope.selectedStation = "";
	   $scope.loadStationOptions = function(){
    	  stationServiceAjax.stationsForOverview().then(function(data){
	            $scope.stationOptions = data;
	            $scope.stationOptionsCount = data.length;
	            $scope.stationOptionsIter = [];
	            for (var i = 0; i < data.length; i++) {
	            	if(i % 2  == 0){
	            		$scope.stationOptionsIter.push(i);
	            	}
	            	/*data[i].unscheduledHours = $scope.getStationUnscheduledHours(data[i].stationId);
	            	data[i].scheduledHours = $scope.getStationScheduledHours(data[i].stationId);
	            	data[i].jobs = $scope.getStationJobs(data[i].stationId);*/
	            }
	         });
	    };
		
	   $scope.loadStationOptions();
	  
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
	   
	   vm.dtOptionsForRolls = DTOptionsBuilder.newOptions()
       .withOption('order', [])
       .withScroller()
	   .withOption('scrollY', 280)
	   .withDOM('ftr')
	   .withLanguage(dataTables);

	   vm.dtColumnDefsForRolls = [
	           DTColumnDefBuilder.newColumnDef(0).notSortable(),
	           DTColumnDefBuilder.newColumnDef(1).notSortable(),
	           DTColumnDefBuilder.newColumnDef(2).notSortable(),
	           DTColumnDefBuilder.newColumnDef(3).notSortable(),
	           DTColumnDefBuilder.newColumnDef(4).notSortable(),
	           DTColumnDefBuilder.newColumnDef(5).notSortable(),
	           DTColumnDefBuilder.newColumnDef(6).notSortable()
	   ];

       vm.dtOptionsForJobs = DTOptionsBuilder.newOptions()
       .withOption('order', [])
       .withScroller()
	   .withOption('scrollY', 280)
	   .withDOM('ftr')
	   .withLanguage(dataTables);

       	vm.dtColumnDefsForJobs = [
                  DTColumnDefBuilder.newColumnDef(0).notSortable(),
                  DTColumnDefBuilder.newColumnDef(1).notSortable(),
                  DTColumnDefBuilder.newColumnDef(2).notSortable(),
                  DTColumnDefBuilder.newColumnDef(3).notSortable(),
                  DTColumnDefBuilder.newColumnDef(4).notSortable(),
                  DTColumnDefBuilder.newColumnDef(5).notSortable(),
                  DTColumnDefBuilder.newColumnDef(6).notSortable(),
                  DTColumnDefBuilder.newColumnDef(7).notSortable(),
                  DTColumnDefBuilder.newColumnDef(8).notSortable()
        ];
	   
       	$scope.moveUp = function(i, itemId, stationId, inputType, level){
       		stationServiceAjax.moveUp(itemId, stationId, level).then(function(){
       			/*if(inputType == 'Roll'){
       				var table = $('#tableRoll-'+stationId).DataTable();
       				//vm.dtInstance[i].reloadData();
       				//table.destroy();
       				//table = $('#tableRoll-'+stationId).DataTable();
       				//table.rerender();
       				//vm.dtInstance[i].reloadData();
       				//vm.dtInstance[i].rerender();
       				//vm.dtInstance[i].rerender();
       				$scope.loadStationOptions();
       				
       				
       				
       				
       				
       				
       				if(level == 'one'){
       					var selectedRowIndex = table.cell($('#oneMoveBtn-'+stationId+'-'+itemId).closest('td')).index();
       					var selectedRow = table.rows(selectedRowIndex.row);
       					var prevRowData = table.rows(selectedRowIndex.row - 1).data();
       					var tmpData = selectedRow.data();
       					selectedRow.data(prevRowData).draw();
       					table.rows(selectedRowIndex.row - 1).data(tmpData).draw();
       					//table.rows(0).data(table.rows(1).data()).draw();
       					//table.rows().data()[0]=table.rows().data()[1];
       					//table.rows().draw();
       				}else if(level == 'top'){
       					
       				}
       				
       				
       			}else if(inputType == 'Job'){
       				var table = $('#tableSheet-'+stationId).DataTable();
       				
       				if(level == 'one'){
       					
       				}else if(level == 'top'){
       					
       				}
       				
       				
       			}*/
       			
       			$scope.loadStationOptions();
       			
       			//$scope.successMsg = true;
                $scope.errorMsg = false;
                $scope.warningMsg = false;
                $scope.alertMsg = $translate.instant('overview_js.following_item_id') + itemId +  $translate.instant('overview_js.moved') + (level == 'top' ?  $translate.instant('overview_js.to_the_top') : $translate.instant('overview_js.one_level_up') );
                toasty.success({
                    title: $translate.instant('overview_js.Move'),
                    msg: $translate.instant('overview_js.following_item_id') + itemId + $translate.instant('overview_js.moved') + (level == 'top' ?  $translate.instant('overview_js.to_the_top') : $translate.instant('overview_js.one_level_up') ),
                    showClose: true,
                    clickToClose: true,
                    timeout: 10000,
                    sound: false,
                    html: false,
                    shake: false,
                    theme: "bootstrap"
                });
                $scope.selectedStation = stationId;
	   	    },function(data, status, headers, config){
	            $scope.errorMsg = true;
	            $scope.successMsg = false;
	            $scope.warningMsg = false;
	            $scope.errors = data.errors;
	        });
       	};
       	
	  /* $scope.getJobsPercentagesByStatus = function(stationId){
    	  jobServiceAjax.getJobsPercentagesByStatus(stationId).then(function(data){
    		  $scope.jobsPercentagesByStatus = data;
    		  $scope.heights = [];
    		  for(var i = 0; i < data.length ; i++){
    			  $scope.heights.push(["{'height': '" + data[i][0] + "%'}", data[i][1], "{'height': '" + data[i][2] + "%'}", data[i][3]]);
    		  }
    	  });
	   };
	   $scope.getStationUnscheduledHours = function(stationId){
	  	var deferred = $q.defer(); 
	  	deferred.resolve(jobServiceAjax.stationUnscheduledHours(stationId).then(function(data2){
	  		return data2;
	        }));
	  	return deferred.promise;
		};
		
		$scope.getStationScheduledHours = function(stationId){
			var deferred = $q.defer(); 
			deferred.resolve(jobServiceAjax.stationScheduledHours(stationId).then(function(data3){
	  		return data3;
	        }));
			return deferred.promise;
		};
		
		$scope.getStationJobs = function(stationId){
			var deferred = $q.defer(); 
			deferred.resolve(jobServiceAjax.getStationJobs(stationId).then(function(data4){
	  		return data4;
	        }));
			return deferred.promise;
		};*/
	  
	   /* $(document).ready(function() {
		  var tableSheet = $('#tableSheet0').DataTable( {
		        "columnDefs": [ {
		            "searchable": true,
		            "orderable": true
		        } ],
		        "order": [[ 1, 'asc' ]]
		    } );
		  var tableRoll = $('#tableRoll').DataTable();
	    });*/
       	
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
  }]);
