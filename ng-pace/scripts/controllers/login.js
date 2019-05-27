'use strict';

/**
 * @ngdoc function
 * @name capApp.controller:LoginCtrl
 * @description
 * # LoginCtrl
 * Controller of the capApp
 */
angular.module('capApp')
.controller('LoginCtrl', ['authenticationServiceAjax', '$scope', '$log', '$location', '$localStorage', '$window', '$route', 'userServiceAjax','$rootScope','$translate',
    function (authenticationServiceAjax, $scope, $log, $location, $localStorage, $window, $route, userServiceAjax, $rootScope, $translate){
	  

	    
	  $scope.username = null;
	  $scope.password = null;
	  $scope.errorFlag = false;
	  
      $scope.validateLogin = function() {			
    	  		//$log.info("entered login :" +  $scope.username +" and " + $scope.password); 
			
			//call authentification service to validate credentials
			authenticationServiceAjax.getToken($scope.username, $scope.password).then(function(data){
				if(!data){
					//$log.error("login error");
					$scope.errorFlag = true;
				}else{					
					//redirect to the saved page
					 $scope.errorFlag = false;
					//$log.info("login success, going to :", $localStorage.currentView);
					var originalPath = $localStorage.currentView;
					
					if(angular.isUndefined(originalPath) || originalPath === null){
						$location.path("/");
					}else{
						$location.path(originalPath);
					}
				}
	 		});
      };
      
      
}]);


