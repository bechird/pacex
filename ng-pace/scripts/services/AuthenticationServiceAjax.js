'use strict';

/**
 * @ngdoc service
 * @name capApp.authenticationServiceAjax
 * @description
 * # authenticationServiceAjax
 * Factory in the capApp.
 */
angular.module('capApp')
  .factory('authenticationServiceAjax',  function ($localStorage, $http, $httpParamSerializer,$rootScope) {
    // Service logic
    // ...
	var AuthBasic = "Basic d2ViOnNlY3JldA==";
	var SecurityUrl =  $rootScope.API_SECURITY;
	

    // Public API here
    return {
      
    	
    	
    	
    	   getToken: function (username, password) {
	    	  	var  postData = {
	    	  					 	'username': username, 
	    	  					 	'password':password,
	    	  					 	'grant_type' : 'password'
	    	  					};

        
	        return $http({
	    	    method: 'POST',
	    	    url: SecurityUrl + "/token",
	    	    headers: {
	    	    				'Authorization': AuthBasic,
	    	    				'Content-Type': 'application/x-www-form-urlencoded; charset=utf-8'    	    				
	    	    			 },
	            data: $httpParamSerializer(postData)
	    		}).then(function successCallback(response) {    
    			
					console.log("setting token in local storage :" + response.data.access_token);
					
					//save token in local storage
					$localStorage.oauthToken = response.data.access_token;
					
					return response.data	;
	    		  }, function errorCallback(response) {
	    			  	if(response.status ===  503){    			  		
	    			  		console.error("Error 503 : can't reach the server at url "+ $rootScope.API_SECURITY+"/token/, please add config in httpd-vhosts.conf");
	    			  	}    			  	
	  				return null;
	  				
	    		  });	

      },
      
      revokeToken: function () {     	  
    	  		delete $localStorage.oauthToken;
      }
      
    
    
    
    
    
    
    
    };
  });
