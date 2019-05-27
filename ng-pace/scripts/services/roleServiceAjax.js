'use strict';

/**
 * @ngdoc service
 * @name capApp.roleServiceAjax
 * @description
 * # roleServiceAjax
 * Factory in the capApp.
 */
angular.module('capApp')
  .factory('roleServiceAjax', function ($http,$rootScope) {
    // Service logic
    // ...

    // Public API here
    return {
      roles: function () {
        return $http.get($rootScope.API_BASE+"/roles/").then(function(response){return response.data});
      },
      addRole: function(role){
        return $http.post($rootScope.API_BASE+"/roles/",role).then(function(response){return response.data});
      },
      deleteRole: function(id){
        return $http.delete($rootScope.API_BASE+"/roles/"+id).then(function(response){return response.data});
      },
      updateRole: function(role){
        return $http.put($rootScope.API_BASE+"/roles/",role).then(function(response){return response.data});
      },
      getRoleById: function(id){
        return $http.get($rootScope.API_BASE+"/roles/"+id).then(function(response){return response.data});
      }
    };
  });
