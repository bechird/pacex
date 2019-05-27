'use strict';

/**
 * @ngdoc service
 * @name capApp.customerServiceAjax
 * @description
 * # customerServiceAjax
 * Factory in the capApp.
 */
angular.module('capApp')
  .factory('customerServiceAjax', function ($http,$httpParamSerializer,$rootScope) {
    // Service logic
    // ...

    // Public API here
    return {
    	customers: function () {
        return $http.get($rootScope.API_BASE+"/customers/").then(function(response){return response.data});
      },
      addCustomer: function(customer){
          return $http.post($rootScope.API_BASE+"/customers/",customer).then(function(response){return response.data});
        },
        deleteCustomer: function(id){
          return $http.delete($rootScope.API_BASE+"/customers/"+id).then(function(response){return response.data});
        },
        updateCustomer: function(customer){
          return $http.put($rootScope.API_BASE+"/customers/",customer).then(function(response){return response.data});
        },
        getCustomerById: function(id){
          return $http.get($rootScope.API_BASE+"/customers/"+id).then(function(response){return response.data});
        },
        getCustomerByEmail: function(email){
            return $http.get($rootScope.API_BASE+"/customers/email/"+email).then(function(response){return response.data});
        }
    };
  });
