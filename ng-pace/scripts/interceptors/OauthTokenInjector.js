angular.module('capApp')
	.factory('oauthTokenInjector',function($localStorage,$rootScope) {  
    var oauthTokenInjector = {
        
    		request: function(config) {
        		var token = $localStorage.oauthToken;
        		
        		if(angular.isUndefined(token) || token === null){
        			//console.warn("no oauth2 token to add to header");
        		}else{
        			//console.log("Read token from local storage :" + token);
        			if(config.url != $rootScope.API_SECURITY+"/token"){
        				config.headers.Authorization = "Bearer  " + token;
        			}
        			
        		}
        		
            return config;
        		
        }   
    
    };
    return oauthTokenInjector;
});

angular.module('capApp')
	.config(['$httpProvider', function($httpProvider) {  
    $httpProvider.interceptors.push('oauthTokenInjector');
}]);