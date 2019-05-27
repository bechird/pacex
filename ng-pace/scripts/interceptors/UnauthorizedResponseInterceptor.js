angular.module('capApp')
	.factory('authInterceptor',function($rootScope, $q, $location, $localStorage) {  
    var authInterceptor = {
    
    		responseError: function(response){
    			//if response error is 401
    			if(response.status === 401){
    				console.error("401 error : UNAUTHORIZED");
    				
    				// delete the token
    				delete $localStorage.oauthToken;
    				
    				//delete user info
    				delete $rootScope.loggedInUser;
    				
    				//save current view
    				var currentPath = $location.path();				    				
    				if(currentPath != "/login"){
    					console.log("currentPath :",  currentPath);
    					$localStorage.currentView = currentPath;
    				}    				
    				
    				//redirect to login page to request a new token
    				$location.path("/login");
    			}    			
    			   			
            return $q.reject(response);
        }  
    		
    
    
    
    };
    return authInterceptor;
});

angular.module('capApp')
	.config(['$httpProvider', function($httpProvider) {  
    $httpProvider.interceptors.push('authInterceptor');
}]);