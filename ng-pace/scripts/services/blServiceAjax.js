'use strict';

/**
 * @ngdoc service
 * @name capApp.customerServiceAjax
 * @description
 * # customerServiceAjax
 * Factory in the capApp.
 */
angular.module('capApp')
  .factory('blServiceAjax', function ($http,$httpParamSerializer,$rootScope) {
    // Service logic
    // ...

    // Public API here
    return {
        getPallettes: function(blId){
            return $http.get($rootScope.API_BASE+"/bls/"+blId).then(function(response){return response.data});
        },
        edit:function(blId,pallettes,newAdresse,newpallettes){
           return $http.post($rootScope.API_BASE+"/bls/"+blId,{pallettes,newAdresse,newpallettes}).then(function(response){return response.data});
        },
        fetchPaletsWithoutBl(){
            return $http.get($rootScope.API_BASE+"/pallets/withoutBl").then(function(response){return response.data});
        },
        changeStatus(blId){
            return $http.post($rootScope.API_BASE+"/bls/status/"+blId).then(function(response){return response.data});

        }
    };
  });
