'use strict';

/**
 * @ngdoc function
 * @name capApp.controller:PartsListCtrl
 * @description
 * # PartsListCtrl
 * Controller of the capApp
 */
angular.module('capApp')
  .controller('PartsListCtrl', ['DTOptionsBuilder','DTColumnBuilder', '$scope', '$uibModal', '$log', 'partServiceAjax', 'SweetAlert', 'lookupServiceAjax', '$timeout', '$confirm','toasty',
	  		  '$translatePartialLoader', '$translate', '$localStorage','$rootScope', '$http',
    function (DTOptionsBuilder, DTColumnBuilder, $scope, $uibModal, $log, partServiceAjax, SweetAlert, lookupServiceAjax, $timeout, $confirm,toasty ,
    			  $translatePartialLoader, $translate, $localStorage, $rootScope, $http) {
	  
	  $translatePartialLoader.addPart('partList');
	  $translate.refresh();
	  
	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	  var vm = this;
	  $scope.token = token;
	  $scope.successMsg = false;
	  $scope.errorMsg = false;
	  
	  var dataTables = {};
	  if($translate.use() == 'fr')
	    {
	  		   dataTables = {
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
	       dataTables = {
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
	           $http.post($rootScope.API_BASE+"/parts/paginated",{aoData}).then(function(result){
	        	   
	        	   var records = {
	                       'draw': result.data.draw,
	                       'recordsTotal': result.data.recordsTotal,
	                       'recordsFiltered': result.data.recordsFiltered,
	                       'data': result.data.data  
	                   };
	        	   
	               fnCallback(records);
	           });
	       }
	    
	  vm.dtInstance = {};
      vm.dtOptions = DTOptionsBuilder.newOptions()
	   	  .withFnServerData(serverData)
	      .withDOM('frtip')          
	      .withPaginationType('full_numbers')
	      .withOption('responsive', true)
	      .withOption('processing', true)
	      .withOption('serverSide', true) // for server side processing 
	      .withDisplayLength(25)
	      .withOption('order', [0, 'desc'])
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
                              type: 'text',    //best sheet/waste
                              bRegex: true,
                              bSmart: true
                            }]
          })
          // Active Buttons extension
          .withButtons([
              'print',
              'excel'
          ])
          .withLanguage(dataTables);

      vm.dtColumns = [
          DTColumnBuilder.newColumn('partNum').withTitle($translate.instant('js_parts.Num')),
          DTColumnBuilder.newColumn('isbn').withTitle('ISBN'),
          DTColumnBuilder.newColumn('title').withTitle( $translate.instant('js_parts.Title')),
          DTColumnBuilder.newColumn('category.name').withTitle($translate.instant('js_parts.Category')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('colors').withTitle($translate.instant('js_parts.Colors')),
          DTColumnBuilder.newColumn('size').withTitle($translate.instant('js_parts.Size')),
          DTColumnBuilder.newColumn('bindingType.name').withTitle($translate.instant('js_parts.Binding')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('paperType.name').withTitle($translate.instant('js_parts.Paper')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn('lamination.name').withTitle($translate.instant('js_parts.Lamination')).withOption('defaultContent', ' '),
          DTColumnBuilder.newColumn(null).withTitle($translate.instant('js_parts.BestSheet')).renderWith(function (data) {
        	  return (data.bestSheet != null ? data.bestSheet : "") + (data.bestSheet != null && data.bestSheetWaste != null ? "/" : "") + (data.bestSheetWaste != null ? data.bestSheetWaste : "");
          }),
          DTColumnBuilder.newColumn(null).withTitle(' ').notSortable().renderWith(function (data) {
        	  if(data.topParts.length == 0){
        		  return "<div style='display:flex'><button type='button' uib-tooltip='Edit' title='"+$translate.instant('js_parts.EDIT')+"' onclick=\"var $scope = angular.element(event.target).scope(); $scope.openEditPartModal('"+data.partNum+"'); $scope.$apply()\" class='btn bgm-orange-900 waves-circle waves-effect'><i class='zmdi zmdi-edit'></i></button>&nbsp;<button type='button' onclick=\"var $scope = angular.element(event.target).scope(); $scope.deletePart('"+data.partNum+"'); $scope.$apply()\" uib-tooltip='Delete' title='"+$translate.instant('js_parts.EDIT')+"' class='btn bgm-deeporange waves-circle waves-effect'><i class='zmdi zmdi-delete'></i></button></div>";
        	  }else{
        		  return "";
        	  }
          })
      ];

     // $scope.deletePart = function(id){
    	//  $scope.errorMsg = false;
         // $scope.successMsg = false;
    	  //$confirm({text: 'Are you sure you want to delete the part?', title: 'Delete Part', ok: 'Ok', cancel: 'Cancel'})
    	    //    .then(function() {
    	   // partServiceAjax.deletePart(id).then(function(data){
            //$scope.parts = data;
           // vm.dtInstance.reloadData();
            //$scope.successMsg = true;
            //$scope.errorMsg = false;
            //$scope.alertMsg = "Part successfully deleted!";
            //}, function(data, status, headers, config){
	          //  $scope.errorMsg = true;
	            //$scope.successMsg = false;
	           // $scope.alertMsg = data.errors.errors;
	       // });
    	 // });
     // };
      $scope.deletePart = function(id){
    	  $scope.errorMsg = false;
          $scope.successMsg = false;
         
      SweetAlert.swal({
		   title: $translate.instant('js_parts.DeletePart'),
		   text:  $translate.instant('js_parts.delete_sure'),
		   type: 'warning',
		   showCancelButton: true,
		   confirmButtonColor: "#DD6B55",
		   confirmButtonText: $translate.instant('js_parts.Yes_delete'),
		   closeOnConfirm: true},
		function(isConfirm){ 
			   if (isConfirm) {
				    partServiceAjax.deletePart(id).then(function(data) {
	               $scope.parts = data;
	               vm.dtInstance.reloadData();
	               toasty.success({
		                  title: $translate.instant('js_parts.DeletePart'),
		                  msg: $translate.instant('js_parts.Part_succ_deleted') ,
		                  showClose: true,
		                  clickToClose: true,
		                  timeout: 5000,
		                  sound: false,
		                  html: false,
		                  shake: false,
		                  theme: "bootstrap"
		              });
		    	    }, function(data, status, headers, config){
			           // $scope.errorMsg = true;
			          //  $scope.successMsg = false;
			           // $scope.alertMsg = data.errors.errors;
		    	    	 SweetAlert.swal( $translate.instant('js_parts.DeletionError'), data.data.errors.errors, "error");
			        });
			   }   
		 });
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
	
	$scope.loadLaminationOptions = function(){
    	  lookupServiceAjax.readAll('Lamination').then(function(data){
	            $scope.laminationOptions = data;
	         });
	};
	$scope.loadLaminationOptions();
	$scope.lamination = {
		    repeatSelect: null,
		    availableOptions: $scope.laminationOptions,
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
	
	$scope.showAdminData = true;
	$scope.disableFields = false;
	
	$scope.open3 = function() {
        $scope.popup3.opened = true;
    };
    $scope.popup3 = {
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

    
	 $scope.openEditPartModal = function (id) {
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
        			return false;
        		}
          }
        });
        modalInstance.result.then(function (result) {
        	vm.dtInstance.reloadData();
        }, function () {});
      };
      
      $scope.openAddPartModal = function () {
        var modalInstance = $uibModal.open({
          animation: true,
          backdrop  : 'static',
      	  keyboard  : false,
          templateUrl: './views/addPartModalContent.html',
          controller: 'AddPartModalInstanceCtrl',
          scope: $scope,
          size: 'lg',
          resolve: {
            
          }
        });
        modalInstance.result.then(function (result) {
        	//vm.dtInstance.reloadData();
        	$scope.openEditPartModal(result.partNum);
          }, function () {});
      };
	  
  }]);
      
  angular.module('capApp')
  .controller('AddPartModalInstanceCtrl', ['$scope', '$uibModalInstance', 'partServiceAjax', 'SweetAlert', '$timeout','toasty','$translate', '$localStorage',
           function ($scope, $uibModalInstance, partServiceAjax, SweetAlert, $timeout,toasty,$translate, $localStorage) {
	  
	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	  var defaultPart = {
		    "partNum":"",
		    "isbn":"",
		    "title":"",
		    "softDelete":"",
		    "activeFlag":"",
		    "bindingType":{"id":'PERFECT'},
		    "lamination":{"id":'GLOSS'}
		  };
	  $scope.showPNLData = false;
	  $scope.uploadedFileName = []; $scope.uploadedFileName.push(""); $scope.uploadedFileName.push("");
      $scope.uploadedFileId = [];  $scope.uploadedFileId.push(""); $scope.uploadedFileId.push("");
      
	  var alertAddMessage =  $translate.instant('js_parts.Part_Added') +"!!";
	  $scope.part = jQuery.extend({}, defaultPart);
	  
	  $scope.generateNewPartNum = function(){
    	  partServiceAjax.generateNewPartNum()
          .then( function(data, status, headers, config){
        	  $scope.part.partNum = data;
          },function(data, status, headers, config){
            $scope.errorMsg = true;
            $scope.successMsg = false;
            $scope.errors = data.errors;
          });
	  };
	  
	  $scope.part.softDelete = false;
	  $scope.part.activeFlag = true;
	  $scope.part.thickness = 0;
	  $scope.generateNewPartNum();
	  
	  $scope.errors = jQuery.extend({}, defaultPart);
	  $scope.$parent.successMsg = false;
	  $scope.$parent.errorMsg = false;
	  $scope.errorMsg = false;
	  
	  $scope.addPart = function(){
    	  partServiceAjax.addPart($scope.part)
	          .then( function(data, status, headers, config){
	              $scope.errors = jQuery.extend({}, defaultPart);
	              $scope.$parent.alertMsg = alertAddMessage;
	              //$scope.$parent.successMsg = true;
	              /*toasty.success({
	                  title:  ,
	                  msg: 'Part ' + $scope.part.partNum + $translate.instant('js_parts.ADDED'),
	                  showClose: true,
	                  clickToClose: true,
	                  timeout: 10000,
	                  sound: false,
	                  html: false,
	                  shake: false,
	                  theme: "bootstrap"
	              });*/
	              SweetAlert.swal($translate.instant('js_parts.Adding_Part'), 'Part ' + data + $translate.instant('js_parts.ADDED'), "success");
                  $scope.part.partNum = data;//case of selfcover part where the partNum gets changed by adding 'T' to the end of it
                  $uibModalInstance.close($scope.part);
	          }, function(data, status, headers, config){
	            $scope.errorMsg = true;
	            $scope.successMsg = false;
	            $scope.errors = data.data.errors.errors;
	          });
	  };
	  $scope.closeModal = function(){
		  $uibModalInstance.dismiss();
	  };
	  
	    
  }]);
  
  angular.module('capApp')
  .controller('EditPartModalInstanceCtrl', function ($scope, $uibModalInstance, partServiceAjax, SweetAlert, $timeout, $window, $confirm, partNum, disableFields,toasty,$translate, $localStorage, Upload, $rootScope) {

	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	  var defaultPart = {
			    "partNum":"",
			    "isbn":"",
			    "title":"",
				 "pagesCount":"",
		         "thickness":""
	  };
	  $scope.showPNLData = false;
	  $scope.uploadedFileName = []; $scope.uploadedFileName.push(""); $scope.uploadedFileName.push("");
	  $scope.uploadedFileName.push(""); $scope.uploadedFileName.push("")
      $scope.uploadedFileId = [];  $scope.uploadedFileId.push(""); $scope.uploadedFileId.push("");
      $scope.uploadedFileId.push(""); $scope.uploadedFileId.push("");
      
      $scope.disableFields = disableFields;
      
      $scope.unlockOrderData = function(){
		  $scope.disableFields = false;
	  };
	  
	  var alertUpdateMessage = $translate.instant('js_parts.Part_Added');
	  $scope.part = jQuery.extend({}, defaultPart);
	  $scope.errors = jQuery.extend({}, defaultPart);
	  $scope.$parent.successMsg = false;
	  $scope.$parent.errorMsg = false;
	  $scope.errorMsg = false;
	  $scope.publishDate = null;
	  $scope.lastPrinted = null;
	  $scope.uploadTextFileError = false;
	  $scope.uploadCoverFileError = false;
	  $scope.uploadDJFileError = false;
	  $scope.uploadESFileError = false;
	  
	  $scope.uploadFiles = function(file, errFiles, partCategory, addEdit) {
		  if($scope.originPartWidth != $scope.part.width || $scope.originPartLength != $scope.part.length){
			  SweetAlert.swal($translate.instant('editPartModalContent.UPLOAD_TEXT'), $translate.instant('editPartModalContent.UPLOAD_ISSUE'), "");
			  return;
		  }
	      if (file) {
	    	  file.upload = Upload.upload({
	              url: $rootScope.API_BASE+'/parts/upload/' + addEdit + '?access_token=' + token,
	              data: {
	            	  partNum: $scope.part.partNum,
	            	  isbn: $scope.part.isbn,
	            	  fileName: file.name,
	            	  file: file,
	            	  partCategory: partCategory}
	          });

	          file.upload.then(function (response) {
	        	  if(partCategory == 'Text'){
	        		  $scope.uploadedFileId[0] = response.data.FileName;
		        	  $scope.uploadedFileName[0] = file.name;
					  $scope.part.pagesCount = (angular.isDefined(response.data.NumberOfPage) && response.data.NumberOfPage != null && response.data.NumberOfPage > 0) ? response.data.NumberOfPage : 0;
	        	  }else{
	        		  if(partCategory == 'Cover'){
		        		  $scope.uploadedFileId[1] = response.data.FileName;
			        	  $scope.uploadedFileName[1] = file.name;
						  $scope.part.thickness = response.data.Spine;
	        		  }else if(partCategory == 'DustJacket'){
	        			  $scope.uploadedFileId[2] = response.data.FileName;
			        	  $scope.uploadedFileName[2] = file.name;
	        		  }else if(partCategory == 'EndSheet'){
	        			  $scope.uploadedFileId[3] = response.data.FileName;
			        	  $scope.uploadedFileName[3] = file.name;
	        		  }
					  //reread the sub parts in case there was not a cover uploaded before (selfcover) and now we uploaded one so we now have a child cover part
					  partServiceAjax.getPartById(partNum).then(function(data){
					      $scope.part.subParts = data.subParts;
					      $scope.part.children = data.children;
					  });
	        	  }
	        	  
	              $timeout(function () {
	                  file.result = response.data;
	                });
	          }, function (response) {
	              console.log(response);
	              if (response.status > 0)
	            	  if(partCategory == 'Text'){
	            		  $scope.uploadTextFileError = true;
	            		  $scope.part.pagesCount = 0;
	            	  }else if(partCategory == 'Cover'){
	            		  $scope.uploadCoverFileError = true;
	            		  $scope.part.thickness = 0;
	            	  }else if(partCategory == 'DustJacket'){
	            		  $scope.uploadDJFileError = true;
	            		  
	            	  }else if(partCategory == 'EndSheet'){
	            		  $scope.uploadESFileError = true;
	            		  
	            	  }
	                  //$scope.errorMsg = response.status + ': ' + response.data.errors.errors;
					  	SweetAlert.swal({
		         		   title: 'ERROR',
		         		   text:  response.data.errors.errors == 'No TrimBox' ? $translate.instant('js_parts.no_trimbox') : response.data.errors.errors,
		         		   type: "error",
		         		   showCancelButton: false,
		         		   confirmButtonColor: "#DD6B55",
		         		   confirmButtonText: "OK",
		         		   closeOnConfirm: true})
	          }, function (evt) {
	        	  //var progressPercentage = parseInt(100.0 * evt.loaded / evt.total);
	        	  // Math.min is to fix IE which reports 200% sometimes
	              file.progress = Math.min(99, parseInt(100.0 * evt.loaded / evt.total));
	              console.log(partCategory + ' upload progress: ' + file.progress + '% ' + evt.config.data.file.name);
	          });
	      }
	  };
	  partServiceAjax.getIsPartProducible(partNum).then(function(data1){
		if(disableFields == false && data1 == true){
			$scope.disableFields = true;
	    }
	    partServiceAjax.getPartById(partNum).then(function(data){
	        $scope.part = data;
	        $scope.originPartWidth = data.width;
	        $scope.originPartLength = data.length;
	        $scope.textPart = jQuery.extend({}, defaultPart);
	        $scope.coverPart = jQuery.extend({}, defaultPart);
	        $scope.djPart = jQuery.extend({}, defaultPart);
	        $scope.esPart = jQuery.extend({}, defaultPart);
	        $scope.publishDate = data.publishDate != null ? new Date(data.publishDate) : null;
	        $scope.lastPrinted = data.lastPrinted != null ? new Date(data.lastPrinted) : null;
	        
	        partServiceAjax.getPartByCategory('Text',$scope.part.partNum)
	        .then(function(data1){
	        	$scope.textPart = data1;
	        },function(data, status, headers, config){
            	$scope.errorMsg = true;
            	$scope.successMsg = false;
                $scope.errors = data.errors;
            }) ;
	        partServiceAjax.getPartByCategory('Cover',$scope.part.partNum)
	        .then(function(data2){
	        	$scope.coverPart = data2;
	        },function(data, status, headers, config){
            	$scope.errorMsg = true;
            	$scope.successMsg = false;
                $scope.errors = data.errors;
            }) ;
	        if($scope.part.dustJacket){
	        	partServiceAjax.getPartByCategory('DustJacket',$scope.part.partNum)
		        .then(function(data3){
		        	$scope.djPart = data3;
		        },function(data, status, headers, config){
	            	$scope.errorMsg = true;
	            	$scope.successMsg = false;
	                $scope.errors = data.errors;
	            }) ;
	        }
	        if($scope.part.printedEndSheet){
	        	partServiceAjax.getPartByCategory('EndSheet',$scope.part.partNum)
		        .then(function(data4){
		        	$scope.esPart = data4;
		        },function(data, status, headers, config){
	            	$scope.errorMsg = true;
	            	$scope.successMsg = false;
	                $scope.errors = data.errors;
	            }) ;
	        }
	        if($scope.part.pagesCount == null){
	        	$scope.part.pagesCount = 0;
	        }
	       //$scope.readUploadedFile = function(category, subPartNum){
	         //partServiceAjax.readUploadedFile(category, subPartNum)
	         //.then(function(){
	         //},
             //function(data, status, headers, config){
            	//$scope.errorMsg = true;
            	//$scope.successMsg = false;
                //$scope.errors = data.errors;
             //}) ;
	        //};
	        $scope.updatePart = function(){
	        	//$scope.part.publishDate = $scope.publishDate;
	        	//$scope.part.lastPrinted = $scope.lastPrinted;
	        	$scope.part.publishDate = $("[id^='publishDate']").val() != null ? new Date($("[id^='publishDate']").val()) : null;   
	        	$scope.part.lastPrinted = $("[id^='lastPrinted']").val() != null ? new Date($("[id^='lastPrinted']").val()) : null;
	      	   //$scope.part.pagesCount = $scope.uploadedPart.pagesCount;
			  //$scope.part.thickness = $scope.uploadedPart.thickness;
	        	
	        	$scope.part.critiriasOrigin = $scope.part.critirias;
	        	
	        	if((($scope.textPart.displayFileName == null || $scope.textPart.displayFileName == '') && $scope.uploadedFileName[0] == '' && $scope.part.bindingType.id !== 'CARDSS')
	        	|| (($scope.coverPart.displayFileName == null || $scope.coverPart.displayFileName == '') && $scope.uploadedFileName[1] == '' && $scope.part.critirias.indexOf('SELFCOVER') == -1)){
	        		SweetAlert.swal({
	        			   title: $translate.instant('editPartModalContent.TITLE'),
	        			   text: $translate.instant('editPartModalContent.savePartWithNoFileUploaded'),
	        			   type:  'warning',
	        			   showCancelButton: true,
	        			   confirmButtonColor: "#DD6B55",
	        			   closeOnConfirm: true},
	        			   function(isConfirm){ 
	        				   if (isConfirm) {
	        					   $scope.updatePartTask();
	        				   }
	        		  });
	        	}else{
	        		$scope.updatePartTask();
	        	}
	          };
	  	    });
	    });
	  $scope.closeModal = function(){
		  $uibModalInstance.dismiss();
	  };
	  
	  $scope.updatePartTask = function() {
		  partServiceAjax.updatePart($scope.part).then(function(){
          	  $scope.alertMsg = alertUpdateMessage;
              $scope.errors = jQuery.extend({}, defaultPart);
              $scope.$parent.alertMsg = alertUpdateMessage;
             // $scope.$parent.successMsg = true;
              toasty.success({
                  title: 'Updating Part',
                  msg: 'Part ' + $scope.part.partNum + ' Updated ',
                  showClose: true,
                  clickToClose: true,
                  timeout: 10000,
                  sound: false,
                  html: false,
                  shake: false,
                  theme: "bootstrap"
              });
              $uibModalInstance.close($scope.part);
           },function(data, status, headers, config){
	        	$scope.errorMsg = true;
	        	$scope.successMsg = false;
	            $scope.errors = data.data.errors.errors;
          }) ;
	  };
	  
	  /*$scope.getOriginalFileName = function(fileName) {
		  if(fileName != null){
			  var originalName = fileName.substring(0, fileName.lastIndexOf("_"));
			  var extension = fileName.substring( fileName.lastIndexOf("."));
			  return originalName + extension;
		  }
	  };*/
	  
	  $scope.removeFile = function(partCategory, partNum){
    	  $scope.errorMsg = false;
          $scope.successMsg = false;
         
	      SweetAlert.swal({
			   title: $translate.instant('js_parts.DeleteFile'),
			   text: $translate.instant('js_parts.file_delete_sure'),
			   type:  'warning',
			   showCancelButton: true,
			   confirmButtonColor: "#DD6B55",
			   confirmButtonText: $translate.instant('js_parts.Yes_delete'),
			   closeOnConfirm: false},
			function(isConfirm){ 
			   if (isConfirm) {
				   partServiceAjax.removeFile(partCategory, partNum).then(function(){
					   if(partCategory == 'Text'){
	        				//$scope.textPart.filePath = null; 
	        				$scope.textPart.displayFileName = null;
	        			}
	        			if(partCategory == 'Cover'){
	        				//$scope.coverPart.filePath = null;
	        				$scope.coverPart.displayFileName = null;
	        			}
	        			if(partCategory == 'DustJacket'){
	        				$scope.djPart.displayFileName = null;
	        			}
	        			if(partCategory == 'EndSheet'){
	        				$scope.esPart.displayFileName = null;
	        			}
	                SweetAlert.swal($translate.instant('js_parts.DeleteFile'), $translate.instant('js_parts.file_succ_deleted'), "success");
		    	    }, function(data, status, headers, config){
			            $scope.errorMsg = true;
	        			$scope.successMsg = false;
	        			$scope.alertMsg = data.data.errors.errors;
		    	    	 SweetAlert.swal($translate.instant('js_parts.DeletionError'), data.data.errors.errors, "error");
			        });
			   }   
		 });
    };
	  
  });
  
