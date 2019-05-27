'use strict';
/**
 * @ngdoc function
 * @name capApp.controller:LoadTagsCtrl
 * @description
 * # LoadTagsCtrl
 * Controller of the capApp
 */
angular.module('capApp')
.controller('LoadTagsListCtrl', ['DTOptionsBuilder','DTColumnBuilder', '$scope', '$log', 'loadTagServiceAjax', 'jobServiceAjax','SweetAlert', 'lookupServiceAjax', '$confirm', '$uibModal','toasty', 
			'$translatePartialLoader', '$translate', '$localStorage','$rootScope','$http',
    function (DTOptionsBuilder, DTColumnBuilder, $scope, $log, loadTagServiceAjax, jobServiceAjax, SweetAlert, lookupServiceAjax, $confirm, $uibModal,toasty , 
    			  $translatePartialLoader, $translate, $localStorage,$rootScope, $http) {
		
		$translatePartialLoader.addPart('loadTagList');
		$translate.refresh();
		
		$.fn.dataTable.ext.errMode = 'none';
		var token = $localStorage.oauthToken;
	
	    $scope.result1 = '';
	    $scope.options1 = null;
	    $scope.details1 = '';

	    var vm = this;
	    var monthNames = ["Jan ", $translate.instant('LOADTAG_JS.FEB'), "Mar ", $translate.instant('LOADTAG_JS.APR'), $translate.instant('LOADTAG_JS.MAY'), $translate.instant('LOADTAG_JS.JUN'), $translate.instant('LOADTAG_JS.JUL'), $translate.instant('LOADTAG_JS.AUG'), "Sep ", "Oct ", "Nov ", "Dec "];
        $scope.successMsg = false;
	    $scope.errorMsg = false;
	    $scope.defaultLoadTag = {
	 		    "jobId":"",
	 		    "tagNum":"",
	 		    "quantity":"",
	 		    "startTime":"",
	 		    "finishTime":"",
	 		    "waste":"",
	 		    "cartNum":"",
	 		    "usedFlag":""
	 	 };
	    
	    vm.dtInstance = {};
	    
	    var serverData = function(sSource, aoData, fnCallback, oSettings) {
	           $http.post($rootScope.API_BASE+"/loadTags/paginated",{aoData}).then(function(result){
	        	   
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
                  type: 'number'    //loadTagId                
              },{
            	  type: 'number'    //jobId
              },{
            	  type: 'number'   //quantity
              },{
                  type: 'date',   // startTime
                  bRegex: true,
                  bSmart: true
              },{
              	type: 'date',   //finishTime
                  bRegex: true,
                  bSmart: true
              },{
            	  type: 'number'  //waste
              },{
            	  type: 'text',   
                  bRegex: true,
                  bSmart: true  // cartNum
              },{
            	  type: 'boolean'   //used flag
              }]
          })
          .withLanguage(dataTables);

       vm.dtColumns = [
          DTColumnBuilder.newColumn('loadTagId').withTitle($translate.instant('LOADTAG_JS.LOADTAG_ID')),
          DTColumnBuilder.newColumn('jobId').withTitle($translate.instant('LOADTAG_JS.JOB')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('quantity').withTitle($translate.instant('LOADTAG_JS.QTY')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('startTime').withTitle($translate.instant('LOADTAG_JS.START_TIME')).renderWith(function (data) {
              var res = "_";
              if(data > 0){
                  var date = new Date(data);
                  res = monthNames[date.getMonth()] + date.getDate() + ", " + date.getHours() + ":" + date.getMinutes();
              }
              return res;
          }),
          DTColumnBuilder.newColumn('finishTime').withTitle($translate.instant('LOADTAG_JS.FINISH_TIME')).renderWith(function (data) {
              var res = "_";
              if(data > 0){
                  var date = new Date(data);
                  var month = date.getMonth() + 1;
                  res = monthNames[date.getMonth()] + date.getDate() + ", " + date.getHours() + ":" + date.getMinutes();
              }
              return res;
          }),
          DTColumnBuilder.newColumn('waste').withTitle($translate.instant('LOADTAG_JS.Waste') ).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('cartNum').withTitle($translate.instant('LOADTAG_JS.Cart')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('usedFlag').withTitle($translate.instant('LOADTAG_JS.TAG_USED')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('loadTagId').withTitle(' ').notSortable().renderWith(function (data) {
        	  return "<div style='display:flex;'><button type='button' onclick=\"var $scope = angular.element(event.target).scope(); $scope.openEditLoadTagModal('"+data+"'); $scope.$apply()\" uib-tooltip='Edit' title='"+$translate.instant('LOADTAG_JS.EDIT')+"' class='btn bgm-orange-900 waves-circle waves-effect'><i class='zmdi zmdi-edit'></i></button>&nbsp;<button type='button' onclick=\"var $scope = angular.element(event.target).scope(); $scope.deleteLoadTag('"+data+"'); $scope.$apply()\" uib-tooltip='Delete' title='"+$translate.instant('LOADTAG_JS.DELETE_js')+"' class='btn bgm-deeporange waves-circle waves-effect'><i class='zmdi zmdi-delete'></i></button></div>";
          })
      ];
      
   /*   $scope.deleteLoadTag = function(id){
    	  $scope.errorMsg = false;
          $scope.successMsg = false;
    	  $confirm({text: 'Are you sure you want to delete the loadTag?', title: 'Delete LoadTag', ok: 'Ok', cancel: 'Cancel'})
	        .then(function() {
	        loadTagServiceAjax.deleteLoadTag(id).then(function(data){
	          $scope.loadTags = data;
              vm.dtInstance.reloadData();
              $scope.successMsg = true;
              $scope.errorMsg = false;
              $scope.alertMsg = "LoadTag successfully deleted!";
    	  }, function(data, status, headers, config){
	            $scope.errorMsg = true;
	            $scope.successMsg = false;
	            $scope.alertMsg = data.errors.errors;
	      });
	     });
	   };*/
       
       $scope.dateOptions = {
	    	    /*dateDisabled: disabled,
	    	    formatYear: 'yy',
	    	    maxDate: new Date(2020, 5, 22),
	    	    minDate: new Date(),
	    	    startingDay: 1*/
	    		type:'datetime-local'
	    };
	    $scope.open1 = function() {
	        $scope.popup1.opened = true;
	    };
	    $scope.popup1 = {
	    		opened: false
	    };
	    $scope.open2 = function() {
	        $scope.popup2.opened = true;
	    };
	    $scope.popup2 = {
	    		opened: false
	    };
	    $scope.formats = ['MM/dd/yyyy', 'MM-dd-yyyy', 'shortDate'];
	    $scope.format = $scope.formats[0];
	    
       $scope.deleteLoadTag = function(id) {
           $scope.errorMsg = false;
           $scope.successMsg = false;

           SweetAlert.swal({
                   title: $translate.instant('LOADTAG_JS.DELETE_LOADTAG'),
                   text:  $translate.instant('LOADTAG_JS.SURE_DELETE_JOB'),
                   type: "warning",
                   showCancelButton: true,
                   confirmButtonColor: "#DD6B55",
                   confirmButtonText:  $translate.instant('LOADTAG_JS.DELETE'),
                   closeOnConfirm: true
               },
               function(isConfirm) {
                   if (isConfirm) {
                       loadTagServiceAjax.deleteLoadTag(id).then(function(data) {
                           $scope.loadTags = data;
                           vm.dtInstance.reloadData();
                           toasty.success({
     		                  title: $translate.instant('LOADTAG_JS.DELETE_LOADTAG'),
     		                  msg: $translate.instant('LOADTAG_JS.LoadTag_SUCC_DELETED') ,
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
                           SweetAlert.swal($translate.instant('LOADTAG_JS.DELETION_ERROR'), data.data.errors.errors, "error");
                       });
                   }
               });
       };
	    
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
		        		return null;
		            }
	           }
	         });
	         modalInstance.result.then(function () {
	         	vm.dtInstance.reloadData();
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
		        		return null;
		            }
	          }
	        });
	        modalInstance.result.then(function(selectedItem) {
	        	vm.dtInstance.reloadData();
	        }, function () {});

	     };
	     
	 	   $scope.loadTag = jQuery.extend({}, $scope.defaultLoadTag);
		 	
		 	$scope.loadJobOptions = function(){
		 		jobServiceAjax.jobsIds().then(function(data){
		 			$scope.jobOptions = data;
		 		});
		 	};
		 	$scope.loadJobOptions();
		 	$scope.loadTagJob = {
		 	    repeatSelect: null,
		 		availableOptions: $scope.jobOptions,
		 	};

}]);

angular.module('capApp')
.controller('AddLoadTagModalInstanceCtrl', function ($rootScope, $scope, $uibModalInstance, $uibModal, loadTagServiceAjax, jobServiceAjax, lookupServiceAjax, jobId, quantityMax, startTime, finishTime,toasty,$translatePartialLoader, $translate, $localStorage) {

	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	  var alertAddMessage = $translate.instant('LOADTAG_JS.LOADTAG_ADDED');
	  $scope.loadTag = jQuery.extend(true,{}, $scope.defaultLoadTag);
	  $scope.loadTag.jobId = jobId;
	  $scope.quantityMax = quantityMax;
	  if(angular.isDefined($scope.fromProd) && $scope.fromProd == true){
		  $scope.loadTag.finishTime = moment(finishTime).format('MM-DD-YYYY, HH:mm');
	  }
	  if(angular.isDefined($scope.fromProd) && $scope.fromProd == true){
		  $scope.loadTag.startTime = moment(startTime).format('MM-DD-YYYY, HH:mm');
	  }
	  if(angular.isDefined($scope.fromProd) && $scope.fromProd == true){
		  $scope.loadTag.quantity = quantityMax;
	  }
	  
	  $scope.errors = jQuery.extend({}, $scope.defaultLoadTag);
	  $scope.$parent.successMsg = false;
	  $scope.$parent.errorMsg = false;
	  $scope.errorMsg = false;
	     
	  $scope.addLoadTag = function(){
		  $scope.loadTag.startTime = new Date($scope.loadTag.startTime);
		  $scope.loadTag.finishTime = new Date($scope.loadTag.finishTime);
		  if($scope.loadTag.waste == null){
			  $scope.loadTag.waste = 0;
		  }
		  if(!angular.isDefined($scope.quantityMax) || $scope.quantityMax == null || $scope.quantityMax == ''){
			  jobServiceAjax.getJobById($scope.loadTag.jobId).then(function(jobData){
				  $scope.quantityMax = jobData.quantityNeeded - jobData.quantityProduced - jobData.totalWaste;
				  if($scope.loadTag.quantity + $scope.loadTag.waste > $scope.quantityMax){
					  $scope.errorMsg = true;
			          $scope.successMsg = false;
			          $scope.errors = $translate.instant('LOADTAG_JS.LOAD_WASTE_NEEDED');
					  return;
				  }
			    });
		  }else{
			  if($scope.loadTag.quantity + $scope.loadTag.waste > $scope.quantityMax){
				  $scope.errorMsg = true;
		          $scope.successMsg = false;
		          $scope.errors = $translate.instant('LOADTAG_JS.LOAD_WASTE_NEEDED');
				  return;
			  }
		  }
		  if(angular.isDefined($scope.fromProd) && $scope.fromProd == true){
  	    	loadTagServiceAjax.addLoadTagFromProd($scope.loadTag)
	          .then( function(data, status, headers, config){
	              $scope.errors = jQuery.extend({}, $scope.defaultLoadTag);
	              $scope.$parent.alertMsg = alertAddMessage;
	             // $scope.$parent.successMsg = true;
	              toasty.success({
	                  title: $translate.instant('LOADTAG_JS.ADDING_LOADTAG'),
	                  msg: $translate.instant('LOADTAG_JS.LOADTAG_SUCC_ADDED'),
	                  showClose: true,
	                  clickToClose: true,
	                  timeout: 10000,
	                  sound: false,
	                  html: false,
	                  shake: false,
	                  theme: "bootstrap"

	              });
	              $rootScope.openLoadTag(data);
	              $uibModalInstance.close();
	          },function(data, status, headers, config){
	            $scope.errorMsg = true;
	            $scope.successMsg = false;
	            $scope.errors = data.errors;
	          });
		  }else{
			  loadTagServiceAjax.addLoadTag($scope.loadTag)
	          .then( function(data, status, headers, config){
	              $scope.errors = jQuery.extend({}, $scope.defaultLoadTag);
	              $scope.$parent.alertMsg = alertAddMessage;
	             // $scope.$parent.successMsg = true;
	              toasty.success({
	                  title: $translate.instant('LOADTAG_JS.ADDING_LOADTAG'),
	                  msg: $translate.instant('LOADTAG_JS.LOADTAG_SUCC_ADDED'),
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
		  }
	  };
	  $scope.closeModal = function(){
		 $uibModalInstance.dismiss();
	  };
	  
});

angular.module('capApp')
.controller('EditLoadTagModalInstanceCtrl', function ($scope, $uibModalInstance, $uibModal, loadTagServiceAjax, jobServiceAjax, lookupServiceAjax, loadTagId, quantityMax, loadtags, part,toasty ,$translatePartialLoader, $translate, $localStorage) {
	
	$.fn.dataTable.ext.errMode = 'none';
	var token = $localStorage.oauthToken;
	
	var alertUpdateMessage =  $translate.instant('LOADTAG_JS.UPDATING_LOADTAG') +"!!";
	  $scope.loadtag = jQuery.extend({}, $scope.defaultLoadTag);
	  $scope.errors = jQuery.extend({}, $scope.defaultLoadTag);
	  $scope.$parent.successMsg = false;
	  $scope.$parent.errorMsg = false;
	  $scope.errorMsg = false;
	 
	  // Needed for the openLoadsStatusModal Binder roots data
	  $scope.loadtags = loadtags;
	  $scope.part = part;
	  if(loadTagId != 0){
	    loadTagServiceAjax.getLoadTagById(loadTagId).then(function(data){
	        $scope.loadTag = data;
	        $scope.startTime = new Date(data.startTime);
	        $scope.startTime = moment(data.startTime).format('MM-DD-YYYY, HH:mm');
	        $scope.finishTime = new Date(data.finishTime);
	        $scope.finishTime = moment(data.finishTime).format('MM-DD-YYYY, HH:mm');
	        if(!angular.isDefined(quantityMax) || quantityMax == null || quantityMax == ''){
	        	jobServiceAjax.getJobById(data.jobId).then(function(jobData){
					quantityMax = jobData.quantityNeeded - jobData.quantityProduced - jobData.totalWaste;
					$scope.quantityMax = quantityMax + $scope.loadTag.quantity + ($scope.loadTag.waste != null ? $scope.loadTag.waste : 0);
			    });
	        }else{
	        	$scope.quantityMax = quantityMax + $scope.loadTag.quantity + ($scope.loadTag.waste != null ? $scope.loadTag.waste : 0);
	        }
	        
	        $scope.updateLoadTag = function(){
	        	$scope.loadTag.startTime = new Date($scope.startTime);
	            $scope.loadTag.finishTime = new Date($scope.finishTime);
	            if($scope.loadTag.waste == null){
	  			   $scope.loadTag.waste = 0;
	  		    }
	            if($scope.loadTag.quantity + $scope.loadTag.waste > $scope.quantityMax){
	  			    $scope.errorMsg = true;
		            $scope.successMsg = false;
		            $scope.errors = $translate.instant('LOADTAG_JS.LOAD_WASTE_NEEDED');
	  			    return;
	  		    }
	        	if(angular.isDefined($scope.fromProd) && $scope.fromProd == true){
		        	loadTagServiceAjax.updateLoadTagFromProd($scope.loadTag)
			            .then(function(){
			          	$scope.alertMsg = alertUpdateMessage;
			              $scope.errors = jQuery.extend({}, $scope.defaultLoadTag);
			              $scope.$parent.alertMsg = alertUpdateMessage;
			             // $scope.$parent.successMsg = true;
			              toasty.success({
			                  title: $translate.instant('LOADTAG_JS.UPDATING'),
			                  msg: 'LoadTag ' + $scope.loadTag.loadTagId + $translate.instant('LOADTAG_JS.UPDATED'),
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
	        	 }else{
	        		 loadTagServiceAjax.updateLoadTag($scope.loadTag)
			            .then(function(){
			          	$scope.alertMsg = alertUpdateMessage;
			              $scope.errors = jQuery.extend({}, $scope.defaultLoadTag);
			              $scope.$parent.alertMsg = alertUpdateMessage;
			              //$scope.$parent.successMsg = true;
			              toasty.success({
			                  title: $translate.instant('LOADTAG_JS.UPDATING') +'!' ,
			                  msg: 'LoadTag ' + $scope.loadTag.loadTagId + $translate.instant('LOADTAG_JS.UPDATED'),
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
	        	 }
	        };
	  	});
	  }
	  $scope.closeModal = function(){
		  $uibModalInstance.dismiss();
	  };
	  
});
