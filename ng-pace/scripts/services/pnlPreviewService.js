'use strict';

angular.module('capApp')
  .factory('pnlPreviewService', function ($http,$httpParamSerializer,$rootScope, $log, $uibModal) {

	  
    return {
        
    	showPreviewModal: function(resolvedTemplate){
    		
    		var modalInstance = $uibModal.open({
 		       animation: true,
 		       backdrop  : 'static',
 		   	   keyboard  : false,
 		       templateUrl: './views/PnlPreviewModal.html',
 		       controller: 'PnlPreviewModalInstanceCtrl',
 		       //scope: $scope,
 		       size:'lg',
 		       resolve: {
 		    	  getResolvedTemplate: function () {
                      return resolvedTemplate;
                  }
 		       }
 		     });
 		     
    		modalInstance.result.then(function () {
 		     	//vm.dtInstance.reloadData();
 		     }, function () {});
 		     
            return true;
        },
    	getResolvedPnl: function(templateId){
            return $http.get($rootScope.API_BASE+"/lookups/pnl/preview/resolve/"+templateId).then(function(response){return response.data});
        },
        generatePdfPreview: function(pnlInfo){
            return $http.post($rootScope.API_BASE+"/lookups/pnl/preview/generate/",pnlInfo).then(function(response){return response.data});
        }
    
    
    
    };
    
    
  });
