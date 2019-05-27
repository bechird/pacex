'use strict';
/**
 * @ngdoc function
 * @name capApp.controller:ShippingStationCtrl
 * @description
 * # ShippingStationCtrl
 * Controller of the capApp
 */
angular.module('capApp')
  .controller('ShippingStationCtrl', ['$localStorage', '$resource','$window','DTOptionsBuilder','DTColumnBuilder','$sce', '$scope', 'rollServiceAjax', 'stationServiceAjax','palletteServiceAjax', 'machineServiceAjax', 'orderServiceAjax', 'jobServiceAjax', 'lookupServiceAjax','partServiceAjax', '$uibModal', '$routeParams', '$route', '$timeout', 'SweetAlert', '$filter','toasty', '$translatePartialLoader', '$translate', 
    function ($localStorage, $resource,$window,DTOptionsBuilder, DTColumnBuilder,  $sce,$scope, rollServiceAjax, stationServiceAjax,palletteServiceAjax, machineServiceAjax,orderServiceAjax, jobServiceAjax, lookupServiceAjax,partServiceAjax, $uibModal, $routeParams, $route, $timeout, SweetAlert, $filter, toasty, $uibModalInstance , $translatePartialLoader, $translate) {
	  
	$.fn.dataTable.ext.errMode = 'none';
	var token = $localStorage.oauthToken;
	  
	/*$translatePartialLoader.addPart('shipping');
	$translate.refresh();*/
	$scope.machineId = $window.localStorage.getItem("machineId");
	if($scope.machineId == undefined){
					  $scope.machineId = "PALLETT A1";
	}
	  $scope.qtyPcbInPallette;
  $scope.qtyBookInPallette ;
	 $scope.closePalette = false;
	$scope.closePalletafterCompleteOrder = false;

	$scope.switchStation = function(){
			  $window.localStorage.setItem("machineId",$scope.machineId);

    palletteServiceAjax.activePallette($scope.machineId).then(function(data){
		  $scope.palletteInfo =  data;
			  $scope.pallette =   $scope.palletteInfo.pallette;
					if($scope.pallette != null){
						$scope.palletteId = "PL"+$scope.pallette.id;  
				  palletteServiceAjax.qtyPcbByPallette($scope.pallette.id).then(function(qtyPcb){
			  $scope.qtyPcbInPallette = qtyPcb;

		  });
		  palletteServiceAjax.qtyLivreByPallette($scope.pallette.id).then(function(qtyBook){
			  $scope.qtyBookInPallette = qtyBook;
		  });  
		}  else{
			$scope.palletteId ="";
		}
	  });
		$scope.fetchCurrentJob();
	
	}
	machineServiceAjax.fetchALLMachineShipping().then(function(data){
        $scope.machines = data;
		});
	  $scope.fetchPackageByPcb = function(pcb){
			var pcbId = pcb.packagePartId;
         palletteServiceAjax.fetchPackByPcb(pcbId).then(function(data){
					  pcb.count = data.count;
				 });
		}

  $scope.pause = function(){
		if($scope.currentJob != null){
				SweetAlert.swal({
			   title:  "Pause",
			   text:   "S'il vous plait fermer l'ordre actif avant de pauser la pallette",
			   type: "warning",
			   showCancelButton: false,
			   confirmButtonColor: "#4caf50",confirmButtonText: $filter('translate')('shipping_js.OK'),
			   closeOnConfirm: true})
	
		}else{
		if($scope.pallette != null){
		palletteServiceAjax.pausePallette($scope.pallette.id,$scope.machineId).then(function(data){
			palletteServiceAjax.activePallette($scope.machineId).then(function(data){
		  $scope.palletteInfo =  data;
			  $scope.pallette =   $scope.palletteInfo.pallette;
					if($scope.pallette != null){
						$scope.palletteId = "PL"+$scope.pallette.id;  
				  palletteServiceAjax.qtyPcbByPallette($scope.pallette.id).then(function(qtyPcb){
			  $scope.qtyPcbInPallette = qtyPcb;

		  });
		  palletteServiceAjax.qtyLivreByPallette($scope.pallette.id).then(function(qtyBook){
			  $scope.qtyBookInPallette = qtyBook;
		  });  
		}  else{
			$scope.palletteId ="";
			$scope.qtyBookInPallette ="";
			$scope.qtyPcbInPallette = "";
		}
	  });
		});
		}}
	}


	  $scope.localSearch = function(str) {
		  var matches = [];
		  $scope.isbns.forEach(function(part) {
		   
		    if ((part.isbn.toLowerCase().indexOf(str.toString().toLowerCase()) >= 0)) {
		      matches.push(part);
		    }
		  });
			if(matches.length == 1 ){
					$scope.partSelectedToShipping = matches[0];
				$scope.partSelected9 = matches[0];
			}
		  return matches;
		};
		 $scope.selectedPart = function(selected) {
		      if (selected) {
		        $scope.partSelected9 = selected.originalObject;
					$scope.fetchPartByIsbn($scope.partSelected9.isbn);
		      } else {
		        $scope.partSelected9 = null;
		      }
		    }
	  $scope.class = "";
	  $scope.orderNum = "";
	  $scope.pallette = {};
	  $scope.selectedItemChanged = function(machineId) {
		  $scope.machineId = machineId;
		  palletteServiceAjax.palletteByMachine($scope.machineId).then(function(data){
			  $scope.listPallette = data
		  });
		 palletteServiceAjax.activePallette($scope.machineId).then(function(data){
			  $scope.palletteInfo =  data;
			  $scope.pallette =   $scope.palletteInfo.pallette;
					if($scope.pallette != null){ 
								$scope.palletteId = "PL"+$scope.pallette.id;  
					  palletteServiceAjax.qtyPcbByPallette($scope.pallette.id).then(function(qtyPcb){
			  $scope.qtyPcbInPallette = qtyPcb;

		  });
		  palletteServiceAjax.qtyLivreByPallette($scope.pallette.id).then(function(qtyBook){
			  $scope.qtyBookInPallette = qtyBook;
		  });  
		}   else{
				$scope.palletteId = "";  
		}
		  });		    
		};
	  $scope.changeClass = function(){
	    if ($scope.class === "red")
	      $scope.class = "blue";
	    else
	      $scope.class = "red";
	  };
	
		$scope.fetchCurrentJob = function(){
			machineServiceAjax.getMachineById($scope.machineId).then(function(data){
			  $scope.currentJob =  data.currentJob;
			  if($scope.currentJob != null){
				  var orderPackage = $scope.currentJob.order.orderPackages;
				  angular.forEach( orderPackage, function(value, key){
					  var pack = value.packages;
					  angular.forEach( pack, function(value, key){
						  var pcb = value.pcbs;
						  angular.forEach( pcb, function(value, key){
							  value.newQty = 0;
						});
					  });
					  
					});
			  }
		    });
			$route.reload();
		}

	
	  $scope.count = 0;
	  $scope.closePallet = function(){
		  SweetAlert.swal({
			   title:   $filter('translate')('shipping_js.sure'),
			   text: $filter('translate')('shipping_js.Closing_Current_Palette'),
			   type: "warning",
			   showCancelButton: true,
			   confirmButtonColor: "#4caf50",confirmButtonText: $filter('translate')('shipping_js.YES'),
			   cancelButtonText: $filter('translate')('shipping_js.NO'),
			   closeOnConfirm: true,
			   closeOnCancel: true }, 
			function(isConfirm){ 
			   if (isConfirm) {
					 if($scope.currentJob != null){
						 	$scope.closePalletafterCompleteOrder = true;
						 $scope.completeBookOnPalletModal();
					 }else{ 
						 	$scope.closePalletafterCompleteOrder = false;
						   palletteServiceAjax.closePallette($scope.pallette.id).then(function(data){
							 $scope.palletteId = "";
				    $scope.downloadPdf($scope.pallette.id);
          palletteServiceAjax.activePallette($scope.machineId).then(function(data){
			  $scope.palletteInfo =  data;
			  $scope.pallette =   $scope.palletteInfo.pallette;
					$scope.qtyBookInPallette = 0;
				$scope.qtyPcbInPallette = 0;
				if($scope.pallette != null){ 	
					$scope.palletteId = "PL"+$scope.pallette.id;  
				  palletteServiceAjax.qtyPcbByPallette($scope.pallette.id).then(function(qtyPcb){
			    $scope.qtyPcbInPallette = qtyPcb;
		  });
				}else{
				$scope.palletteId = "";  
		}
		});
		 });
					 }
				 
			   } else {
			      SweetAlert.swal("Cancelled", "Annulation");
			   }
			});
	  }
	  $scope.downloadPdf =function(id){
				 palletteServiceAjax.downloadPackagingSlip(id).then(function (data) {
             var blob = new Blob([data], {type: "application/pdf"});
            var objectUrl = URL.createObjectURL(blob);
            var a         = document.createElement('a');
            a.href        = objectUrl; 
            a.target      = '_blank';
            a.download    = "PDFFSlip"+'.pdf';
            document.body.appendChild(a);
            a.click();
              window.open(objectUrl);
						 });

		}
	  partServiceAjax.distinctIsbns().then(function(data){
		  $scope.isbns =  data;
	  });
	  $scope.testIndefinedPallette= function(packageBook){
		 if( $scope.pallette == undefined || $scope.pallette == null){
			 return true;
		 }
		 return false;
	  }
	  palletteServiceAjax.palletteByMachine($scope.machineId).then(function(data){
		  $scope.listPallette = data
	  });
	 palletteServiceAjax.activePallette($scope.machineId).then(function(data){
		  $scope.palletteInfo =  data;
			  $scope.pallette =   $scope.palletteInfo.pallette;
			if($scope.pallette != null){
				$scope.palletteId = "PL"+$scope.pallette.id;  
				  palletteServiceAjax.qtyPcbByPallette($scope.pallette.id).then(function(qtyPcb){

			  $scope.qtyPcbInPallette = qtyPcb;

		  });
		  palletteServiceAjax.qtyLivreByPallette($scope.pallette.id).then(function(qtyBook){
			  $scope.qtyBookInPallette = qtyBook;
		  });  
		}  else{
				$scope.palletteId = "";  
		}
	  });
	  $scope.qtyPerPallette ="";
	 $scope.calQntyParPallette = function(pallette){
		 palletteServiceAjax.qtyPcbByPallette(pallette.id).then(function(data){
			pallette.sumQty =  data;
			return pallette;
		 });
	 }
	  $scope.fetchPartOfPalletByIsbn= function(packageBook){
		
		  partServiceAjax.getPartByIsbn(packageBook.barcode).then(function(data){
			  $scope.partPallet =  data;
			  
		  });
		  return  $scope.partPallet
		 }
		 $scope.pcbRecordedByPallette = function(pcb){
			 					        var pallettesBook = $scope.pallette.books;
             angular.forEach( pallettesBook, function(value, key){
                				  var palletteBook = value.packageBook;
                       if(palletteBook.packagePartId == pcb.packagePartId)pcb.recorded = value.quantity;
									});
		 }
		 
		 machineServiceAjax.getMachineById($scope.machineId).then(function(data){
			  $scope.currentJob =  data.currentJob;
				if($scope.currentJob != null){
				  var orderPackage = $scope.currentJob.order.orderPackages;
				  angular.forEach( orderPackage, function(value, key){
					  var pack = value.packages;
					  angular.forEach( pack, function(value, key){
						  var pcb = value.pcbs;
						  angular.forEach( pcb, function(value, key){
							  value.newQty = 0;
						});
					  });
					  
					});
				}
		  });
		 
	  $scope.qtyShippedByPcb = function(pcb,palletteId){
			palletteServiceAjax.qtyShippedByPcb(pcb.packagePartId,palletteId).then(function(data){
            pcb.shipped = data;
			});
		}

	  $scope.savePcbDelivred = function(pcb){
			if($scope.pallette == null){
				 	SweetAlert.swal({
			   title:  $filter('translate')('shipping_js.Warning'),
			   text:  $filter('translate')('shipping_js.add_palette'),
			   type: "warning",
			   showCancelButton: false,
			   confirmButtonColor: "#4caf50",confirmButtonText: $filter('translate')('shipping_js.OK'),
			   closeOnConfirm: true})
				}else{
			var pcbShipped = pcb.recorded + pcb.newQty;
			var estimted = pcb.quantity * pcb.count;
			if(pcbShipped > estimted){
       	SweetAlert.swal({
			   title: $filter('translate')('shipping_js.Warning'),
			   text:   $filter('translate')('shipping_js.quantity_less_estimated_pcb'),
			   type: "warning",
			   showCancelButton: false,
			   confirmButtonColor: "#4caf50",confirmButtonText: $filter('translate')('shipping_js.OK'),
			   closeOnConfirm: true})
			}else{
		  palletteServiceAjax.updateQtyPalletteBook($scope.pallette.id,pcb.packagePartId,pcb.newQty).then(function(data){
					 palletteServiceAjax.activePallette($scope.machineId).then(function(data){
		  $scope.palletteInfo =  data;
			  $scope.pallette =   $scope.palletteInfo.pallette;
				if($scope.pallette != null){ 	
					$scope.palletteId = "PL"+$scope.pallette.id;  
				  palletteServiceAjax.qtyPcbByPallette($scope.pallette.id).then(function(qtyPcb){
			  $scope.qtyPcbInPallette = qtyPcb;

		  });
		  palletteServiceAjax.qtyLivreByPallette($scope.pallette.id).then(function(qtyBook){
			  $scope.qtyBookInPallette = qtyBook;
		  });  
		}  else{
				$scope.palletteId = "";  
		}
	  });
	   	$scope.fetchCurrentJob();
			});
		}
		}
	  }

$scope.editQtyPcbInPallette = function(pcb){
	 palletteServiceAjax.editQtyPalletteBook($scope.pallette.id,pcb.packagePartId,pcb.newQty).then(function(data){
					 palletteServiceAjax.activePallette($scope.machineId).then(function(data){
		  $scope.palletteInfo =  data;
			  $scope.pallette =   $scope.palletteInfo.pallette;
				if($scope.pallette != null){ 	
					$scope.palletteId = "PL"+$scope.pallette.id;  
				  palletteServiceAjax.qtyPcbByPallette($scope.pallette.id).then(function(qtyPcb){
			  $scope.qtyPcbInPallette = qtyPcb;

		  });
		  palletteServiceAjax.qtyLivreByPallette($scope.pallette.id).then(function(qtyBook){
			  $scope.qtyBookInPallette = qtyBook;
		  });  
		}  else{
				$scope.palletteId = "";  
		}
	  });
	   	$scope.fetchCurrentJob();
			});
}

	  $scope.fetchOrder = function() {
	  orderServiceAjax.getOrderByOrderNum($scope.orderNum).then(function(data){
	        $scope.order = data;
	        $scope.isbn = $scope.order.orderPart.part.isbn;
	        $scope.client = $scope.order.customer.firstName;
	        $scope.Qty = $scope.order.orderPart.quantity;
	        $scope.title = $scope.order.orderPart.part.title;
	        $scope.packages = $scope.order.packages.package_;
	        
	  });}
	  $scope.fetchJobPackagingByIsbn = function(){
if($scope.partSelected9 != null){
	  jobServiceAjax.getJobOfPackagingByIsbn($scope.partSelected9.isbn).then(function(data){
	        $scope.orders = data;
					if($scope.orders.length == 0){
						SweetAlert.swal({
			   title: $filter('translate')('shipping_js.Warning'),
			   text: $filter('translate')('shipping_js.No_Job_Finishing') +$scope.partSelected9.isbn +"!",
			   type: "warning",
			   showCancelButton: true,
			   confirmButtonColor: "#4caf50",confirmButtonText: $filter('translate')('shipping_js.OK'),
			   closeOnConfirm: true})
					}
	  });}
	}
  $scope.fetchPartByIsbn= function(isbn){
		
	  partServiceAjax.getPartByIsbn(isbn).then(function(data){
		  $scope.part = data;
		  
	  });
	}
  $scope.calcQtyOrderInPallette = function(){
		if($scope.currentJob != null && $scope.pallette != null){
		 palletteServiceAjax.calcQtyOrderInPallette($scope.pallette.id,$scope.currentJob.orderId).then(function(data){
			 $scope.qtyCurrentOrderInPallette = data;
		  });
			}
	 }
  $scope.calcqtyPerPcb = function(pcb){
	   $scope.qtyPerPcb = pcb.heightQty *pcb.widthQty *pcb.depthQty;
	   return  $scope.qtyPerPcb;
  }
 
  
	
  $scope.calcqtyPcbEstimated = function(pcb){
	   $scope.pcbEstimted = Math.floor(pcb.quantity / $scope.qtyPerPcb);
	   return  $scope.pcbEstimted;
 }
  $scope.ChangeStatusPcb = function(pcb){
	  palletteServiceAjax.changeStatusPcb(pcb.packagePartId).then(function(data){
	  });
  }
  
	  $scope.openPalletListModal = function (id) {
	        var modalInstance = $uibModal.open({
	        	backdrop  : 'static',
	        	keyboard  : false,
	        	 animation: true,reloadOnSearch: false,
	        	templateUrl: './views/palletListModal.html',
	        	controller: 'openPalletListModalInstanceCtrl',
	        	scope: $scope,
	        	size:'lg',
	        	resolve: {
	        		orderId: function () {
	        		return id;
	            }
	          }
	        });  modalInstance.result.then(function () {
	        	 vm.dtInstance.reloadData();
	           }, function () {});
	     };
	     $scope.closeModal = function(){
			  $uibModalInstance.dismiss();
		  };
		  $scope.openScanBookModal = function (id) {
				if($scope.currentJob != null){
				SweetAlert.swal({
		         		   title: $filter('translate')('shipping_js.Warring'),
		         		   text:  $filter('translate')('shipping_js.close_current_order'),
		         		   type: "warning",
		         		   showCancelButton: false,
		         		   confirmButtonColor: "#DD6B55",
		         		   confirmButtonText: $filter('translate')('shipping_js.OK'),
		         		   closeOnConfirm: true})
				}else{
		        var modalInstance = $uibModal.open({
		        	backdrop  : 'static',
		        	keyboard  : false,
		        	 animation: true,reloadOnSearch: false,
		        	templateUrl: './views/scanBookModal.html',
		        	controller: 'openScanBookModalInstanceCtrl',
		        	scope: $scope,
		        	size:'lg',
		        	resolve: {
		        		orderId: function () {
		        		return id;
		            }
		          }
		        });  modalInstance.result.then(function () {
		        	 vm.dtInstance.reloadData();
		           }, function () {});
		     }};
		     $scope.addOrderManualyModal = function (id) {
			        var modalInstance = $uibModal.open({
			        	backdrop  : 'static',
			        	keyboard  : false,
			        	 animation: true,reloadOnSearch: false,
			        	templateUrl: './views/addOrderManualyModal.html',
			        	controller: 'addOrderManualyModalInstanceCtrl',
			        	scope: $scope,
			        	size:'xs',
			        	resolve: {
			        		orderId: function () {
			        		return id;
			            }
			          }
			        });  modalInstance.result.then(function () {
			        	 vm.dtInstance.reloadData();
			           }, function () {});
			     };
			     $scope.addPalletModal = function (id) {
					//	 if($scope.currentJob !== null && $scope.currentJob !== undefined){
				        var modalInstance = $uibModal.open({
				        	backdrop  : 'static',
				        	keyboard  : false,
				        	 animation: true,reloadOnSearch: false,
				        	templateUrl: './views/addPalletModal.html',
				        	controller: 'addPalletModalInstanceCtrl',
				        	scope: $scope,
				        	size:'sm',
				        	resolve: {
				        		orderId: function () {
				        		return id;
				            }
				          }
				        });  modalInstance.result.then(function () {
				        	 vm.dtInstance.reloadData();
				           }, function () {});
						/* }else{
					SweetAlert.swal({
		         		   title: "Warning",
		         		   text: "Machine "+$scope.machineId+" n'a pas un order",
		         		   type: "warning",
		         		   showCancelButton: false,
		         		   confirmButtonColor: "#DD6B55",
		         		   confirmButtonText: "OK",
		         		   closeOnConfirm: true})
			}*/
				     };
			     $scope.orderPalletteToEdit;
			     $scope.palletteBookToEdit;
			     
		     $scope.openSaveOrderPachingModal = function (order,palleteBook) {
		    	 $scope.orderPalletteToEdit = order;
		    	 $scope.palletteBookToEdit = palleteBook;
		    	 var orderPackage = $scope.orderPalletteToEdit.orderPackages;
				  angular.forEach( orderPackage, function(value, key){
					  var pack = value.packages;
					  angular.forEach( pack, function(value, key){
						  var pcb = value.pcbs;
						  angular.forEach( pcb, function(value, key){
							  value.newQty = 0;
						});
					  });
					  
					});
			        var modalInstance = $uibModal.open({
			        	backdrop  : 'static',
			        	keyboard  : false,
			        	 animation: true,reloadOnSearch: false,
			        	templateUrl: './views/addOrderPackingSlip.html',
			        	controller: 'openSaveOrderPachingModalInstanceCtrl',
			        	scope: $scope,
			        	size:'lg',
			        	resolve: {
			        		
			          }
			        });  modalInstance.result.then(function () {
			        	 vm.dtInstance.reloadData();
			           }, function () {});
			     };
			     $scope.completeBookOnPalletModal = function () {
						 if($scope.currentJob != null){
				        var modalInstance = $uibModal.open({
				        	backdrop  : 'static',
				        	keyboard  : false,
				        	 animation: true,reloadOnSearch: false,
				        	templateUrl: './views/completeBookOnPalletModal.html',
				        	controller: 'completeBookOnPalletModallInstanceCtrl',
				        	scope: $scope,
				        	size:'lg',
				        	resolve: {
				        	
				            }
				          }
				        );  modalInstance.result.then(function () {
				        	 vm.dtInstance.reloadData();
				           }, function () {});
						 }
				     };
				     $scope.closePalletModal = function () {
					        var modalInstance = $uibModal.open({
					        	backdrop  : 'static',
					        	keyboard  : false,
					        	 animation: true,reloadOnSearch: false,
					        	templateUrl: './views/closePalletModal.html',
					        	controller: 'closePalletModallInstanceCtrl',
					        	scope: $scope,
					        	size:'lg',
					        	resolve: {
					        	
					            }
					          }
					        );  modalInstance.result.then(function () {
					        	 vm.dtInstance.reloadData();
					           }, function () {});
					     };

  }
  ]);
angular.module('capApp')
.controller('completeBookOnPalletModallInstanceCtrl', function ($rootScope, $scope, $uibModalInstance, $uibModal, orderServiceAjax, customerServiceAjax, partServiceAjax, palletteServiceAjax,lookupServiceAjax,jobServiceAjax,toasty ,$filter, $localStorage) {

	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	  $scope.completeJob = function(){
		if($scope.currentJob != null){
			if(	$scope.leftover === true){
				palletteServiceAjax.updateLeftOver($scope.pallette.id,$scope.leftoverQty,"OVER",$scope.currentJob.orderId).then(function(data){
			   
			});
			}
			jobServiceAjax.completeJob($scope.currentJob.jobId).then(function(data){
				if($scope.closePalletafterCompleteOrder === true){
					palletteServiceAjax.closePallette($scope.pallette.id).then(function(data){
						$scope.palletteId = "";
						$scope.downloadPdf($scope.pallette.id);
					});
				}
				if($scope.palletteInfo.orders.length == 0){
					if($scope.pallette != null){
						palletteServiceAjax.updateDestination($scope.pallette.id,"").then(function(data){
								$scope.currentJob ={};
					palletteServiceAjax.activePallette($scope.machineId).then(function(data){
								$scope.palletteInfo =  data;
								$scope.pallette =   $scope.palletteInfo.pallette;
								if($scope.pallette != null){ 
									$scope.palletteId = "PL"+$scope.pallette.id; 
									palletteServiceAjax.qtyPcbByPallette($scope.pallette.id).then(function(qtyPcb){
										$scope.qtyPcbInPallette = qtyPcb;
									});}else{$scope.palletteId = "";}
							});
				$scope.fetchCurrentJob();
						})
					};
				}
					$scope.currentJob ={};
					palletteServiceAjax.activePallette($scope.machineId).then(function(data){
								$scope.palletteInfo =  data;
								$scope.pallette =   $scope.palletteInfo.pallette;
								if($scope.pallette != null){ 
									$scope.palletteId = "PL"+$scope.pallette.id; 
									palletteServiceAjax.qtyPcbByPallette($scope.pallette.id).then(function(qtyPcb){
										$scope.qtyPcbInPallette = qtyPcb;
									});}else{$scope.palletteId = "";}
							});
				$scope.fetchCurrentJob();
			})

		}
	};
	$scope.closeModalCompleteOrder = function(){
	
		$scope.completeJob ();
		
	}
	$scope.leftover = false;
		$scope.noleftover = false;

  $scope.leftoverQty = 0;

	$scope.NoLeftOver = function(){
		$scope.noleftover = true;
		$scope.leftover = false;

	}
		$scope.setLeftOverWithQty = function(){
		$scope.leftover = true;
				$scope.noleftover = false;




	}
	  $scope.closeModal = function(){
		  $uibModalInstance.dismiss();
			
	  };    
});
angular.module('capApp')
.controller('openScanBookModalInstanceCtrl', function ($rootScope, $route,$scope, $uibModalInstance, $uibModal, SweetAlert,orderServiceAjax, customerServiceAjax, partServiceAjax,jobServiceAjax,machineServiceAjax,palletteServiceAjax, lookupServiceAjax,toasty ,$filter, $localStorage) {

	  $.fn.dataTable.ext.errMode = 'none';
	  var token = $localStorage.oauthToken;
	  
	   $scope.isbnToPallette ="";
	   $scope.fetchPartOfPalletteByIsbn= function(){
			  partServiceAjax.getPartByIsbn($scope.isbnToPallette).then(function(data){
			  $scope.partToPallette = data;
				  
			})};
			 $scope.selectedPartToShipping = function(selected) {
		      if (selected) {
		        $scope.partSelectedToShipping = selected.originalObject;
					$scope.fetchPartByIsbn($scope.partSelectedToShipping.isbn);
					$scope.fetchJobPalletteByIsbn();
		      } else {
		        $scope.partSelectedToShipping = null;
		      }
		    }
	   $scope.fetchJobPalletteByIsbn = function(){
			  jobServiceAjax.getJobOfPackagingByIsbn($scope.partSelectedToShipping.isbn).then(function(data){
			        $scope.ordersToPallette = data;
			  })};
		$scope.assignJobRefrechPallette = function(order){
						var dest = order.orderPackages[0].destination;
						if($scope.pallette != null){
						if($scope.pallette.destination == "" || $scope.pallette.destination == null){
	            machineServiceAjax.updateCurrentJobToMachine(order.orderId,$scope.machineId).then(function(data){$scope.fetchCurrentJob()});
					     palletteServiceAjax.updateDestination($scope.pallette.id,dest).then(function(data){
								 palletteServiceAjax.activePallette($scope.machineId).then(function(data){
								$scope.palletteInfo =  data;
								$scope.pallette =   $scope.palletteInfo.pallette;
								if($scope.pallette != null){ 
									$scope.palletteId = "PL"+$scope.pallette.id; 
									palletteServiceAjax.qtyPcbByPallette($scope.pallette.id).then(function(qtyPcb){
										$scope.qtyPcbInPallette = qtyPcb;
									});}else{$scope.palletteId = "";}
							});
							$route.reload();
							 });
						}else{
							if(dest !== $scope.pallette.destination){
								 SweetAlert.swal({
		         		   title: $filter('translate')('shipping_js.Error'),
		         		   text:  $filter('translate')('shipping_js.order_address_palette'),
		         		   type: "error",
		         		   showCancelButton: false,
		         		   confirmButtonColor: "#DD6B55",
		         		   confirmButtonText: $filter('translate')('shipping_js.OK'),
										 closeOnConfirm: true})
						}else{
		     machineServiceAjax.updateCurrentJobToMachine(order.orderId,$scope.machineId).then(function(data){	$scope.fetchCurrentJob()});
						}	
		}}else{
			machineServiceAjax.updateCurrentJobToMachine(order.orderId,$scope.machineId).then(function(data){	$scope.fetchCurrentJob()});

		}
		}
	$scope.choseNewJob = function(order){
		  var dest = order.orderPackages[0].destination;
		if($scope.pallette != null){
			if($scope.palletteInfo.orders != undefined){
			var orderInPallet ="";
			 if($scope.palletteInfo.orders[0] != undefined ){
				 orderInPallet = $scope.palletteInfo.orders[0].orderNum;
			 }
			 if($scope.pallette.typePallette === "SINGLE" && (orderInPallet != undefined && orderInPallet != "")&& orderInPallet != order.orderNum){
				 	 SweetAlert.swal({
		         		   title: $filter('translate')('shipping_js.Error'),
		         		   text:  $filter('translate')('shipping_js.not_put_different_orders_in_pallet'),
		         		   type: "error",
		         		   showCancelButton: false,
		         		   confirmButtonColor: "#DD6B55",
		         		   confirmButtonText: $filter('translate')('shipping_js.OK'),
		         		   closeOnConfirm: true})
			 }else {
					$scope.assignJobRefrechPallette(order);
					machineServiceAjax.updateCurrentJobToMachine(order.orderId,$scope.machineId).then(function(data){	$scope.fetchCurrentJob()});

			 }
			}else{

			 $scope.assignJobRefrechPallette(order);
			 machineServiceAjax.updateCurrentJobToMachine(order.orderId,$scope.machineId).then(function(data){	$scope.fetchCurrentJob()});

		}
	}else{
		$scope.assignJobRefrechPallette(order);
			 machineServiceAjax.updateCurrentJobToMachine(order.orderId,$scope.machineId).then(function(data){	$scope.fetchCurrentJob()});

		}
			$scope.closeModal();
		$route.reload();
};
	  $scope.closeModal = function(){
			if($scope.part != undefined){
						$scope.part.title = "";
					}
		  $uibModalInstance.dismiss();
	  };
});
angular.module('capApp')
.controller('openPalletListModalInstanceCtrl', function ($rootScope, $scope,$route, $uibModalInstance, $uibModal, orderServiceAjax, customerServiceAjax, partServiceAjax, lookupServiceAjax,toasty,SweetAlert,palletteServiceAjax ,$filter, $localStorage) {
 
	$.fn.dataTable.ext.errMode = 'none';
	var token = $localStorage.oauthToken;
	
 palletteServiceAjax.palletteByMachine($scope.machineId).then(function(data){
	 $scope.pallets = data;
 });
 $scope.resume = function(palletteId){
   palletteServiceAjax.resumePallette(palletteId,$scope.machineId).then(function(data){
		 			palletteServiceAjax.activePallette($scope.machineId).then(function(data){
         $scope.palletteInfo =  data;
			  $scope.pallette =   $scope.palletteInfo.pallette;
					if($scope.pallette != null){
						$scope.palletteId = "PL"+$scope.pallette.id;  
				  palletteServiceAjax.qtyPcbByPallette($scope.pallette.id).then(function(qtyPcb){
			  $scope.qtyPcbInPallette = qtyPcb;

		  });
		  palletteServiceAjax.qtyLivreByPallette($scope.pallette.id).then(function(qtyBook){
			  $scope.qtyBookInPallette = qtyBook;
		  });  
		}  else{
			$scope.palletteId ="";
		}
		 palletteServiceAjax.palletteByMachine($scope.machineId).then(function(data){
	 $scope.pallets = data;
 });
});
$route.reload();
 $scope.closeModal ();
	  });
 };
 $scope.fetchOrderByPallette = function(palletteId){
	palletteServiceAjax.fetchOrderByPallette(palletteId).then(function(data){
   $scope.ordersList = data.orders;
	});
 }
	  $scope.closeModal = function(){
		  $uibModalInstance.dismiss();
	  };    
});
angular.module('capApp')
.controller('addPalletModalInstanceCtrl', function ($rootScope, $scope, $route,$uibModalInstance, $uibModal, orderServiceAjax, customerServiceAjax, partServiceAjax, palletteServiceAjax,lookupServiceAjax,toasty,SweetAlert ,$filter, $localStorage) {

	$.fn.dataTable.ext.errMode = 'none';
	var token = $localStorage.oauthToken;
	
	$scope.nameNewPallette = "";
	$scope.typePallette = "";
	var dest ="";
	$scope.savePallette = function(){
		if($scope.currentJob != null){
			var dest = $scope.currentJob.order.orderPackages[0].destination;
		}
		palletteServiceAjax.addPallette($scope.typePallette,$scope.machineId,dest).then(function(data){
        $scope.closeModal();
     
		},function(status){
			if(status.error == true){
					SweetAlert.swal({
		         		   title: $filter('translate')('shipping_js.Error'),
		         		   text: status.errors.errors,
		         		   type: "error",
		         		   showCancelButton: false,
		         		   confirmButtonColor: "#DD6B55",
		         		   confirmButtonText: $filter('translate')('shipping_js.OK'),
		         		   closeOnConfirm: true})
			}

		});
	}
	
	  $scope.closeModal = function(){
		  $uibModalInstance.dismiss();
		 	 $route.reload();
	  };    
});
angular.module('capApp')
.controller('addOrderManualyModalInstanceCtrl', function ($rootScope, $scope, $uibModalInstance, $uibModal, orderServiceAjax, customerServiceAjax, partServiceAjax, lookupServiceAjax,toasty ,$filter) {
 
	
	  $scope.closeModal = function(){
		  $uibModalInstance.dismiss();
	  };    
});
angular.module('capApp')
.controller('openSaveOrderPachingModalInstanceCtrl', function ($rootScope, $scope, $uibModalInstance, $uibModal, orderServiceAjax, customerServiceAjax, partServiceAjax, lookupServiceAjax,toasty ,$filter) {
	  $scope.closeModal = function(){
		  $uibModalInstance.dismiss();
	  };    
});
angular.module('capApp')
.controller('closePalletModallInstanceCtrl', function ($rootScope, $scope, $uibModalInstance, $uibModal, orderServiceAjax, customerServiceAjax, partServiceAjax, lookupServiceAjax,toasty ,$filter) {
	  $scope.closeModal = function(){
		  $uibModalInstance.dismiss();
	  };    
});
