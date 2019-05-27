'use strict';

angular.module('capApp')
.controller('DeliverySelectorCtrl', function (palletteServiceAjax, orderServiceAjax, $scope, $log, $location, $localStorage, $window, $route, $rootScope, $translate, $ngConfirm){
	  

	  $scope.groups;
	  $scope.currentPallette;
  
	  
	  palletteServiceAjax.getPallettesToShipToday().then(function(data){  
			
			$scope.groups = data;			
			
		});    
    	  
 
      
	  $scope.confirmCreateBl = function(key) {
		  
	      	$ngConfirm({
	            title: $translate.instant('deliveryFormBuilderI18n.confirmTitle'),
	            
	            content: 	'<strong>' + $translate.instant('deliveryFormGeneratorI18n.confirmCreate') + " : " + key + '</strong>',
	            scope: $scope,
	            buttons: { 

	                ok: { 
	                    text: $translate.instant('deliveryFormBuilderI18n.save'),
	                    btnClass: 'btn-primary',
	                    action: function(scope){	 
	                    		$scope.createBl(key);
	                    }
	                },
	                cancel: { 
	                    text: $translate.instant('deliveryFormBuilderI18n.cancel'),
	                    action: function(scope){}
	                }      
	                
	                
	            }    	
	      	
	        });	
			
	  }

	  
	  $scope.changeDeliveryStatus = function(pallette) {
		  
	      	$ngConfirm({
	            title: $translate.instant('deliveryFormGeneratorI18n.changeShipStatus'),
	            
	            content: 	'<strong>' + $translate.instant('deliveryFormGeneratorI18n.promptChange') + '</strong> &nbsp&nbsp:&nbsp&nbsp'
	            			  + '<label class="checkbox checkbox-inline m-r-20"><input ng-model="currentPallette.selected" type="checkbox"><i class="input-helper"></i></label>',
	            scope: $scope,
	            onScopeReady: function(scope){
            			scope.currentPallette = angular.copy(pallette);
	            },
	            buttons: { 

	                ok: { 
	                    text: $translate.instant('deliveryFormBuilderI18n.save'),
	                    btnClass: 'btn-primary',
	                    action: function(scope){	
	                    		pallette.selected = scope.currentPallette.selected;
	                    		scope.$apply();
	                    }
	                },
	                cancel: { 
	                    text: $translate.instant('deliveryFormBuilderI18n.cancel')
	                }      
	                
	                
	            }    	
	      	
	        });	
			
	  }
	  

	  $scope.createBl = function(key) {
	  
			orderServiceAjax.downloadTodaySelectedBl($scope.groups[key]).then(function (data) {
				var blob = new Blob([data], { type: "application/pdf" });
				var objectUrl = URL.createObjectURL(blob);
				var a = document.createElement('a');
				a.href = objectUrl;
				a.target = '_blank';
				a.download = "BL" + '.pdf';
				document.body.appendChild(a);
				a.click();
				window.open(objectUrl);				
			});		
			
	  }
	  
	  
	  
     
      
});


