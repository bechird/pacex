'use strict';

/**
 * @ngdoc service
 * @name capApp.userServiceAjax
 * @description
 * # userServiceAjax
 * Factory in the capApp.
 */
angular.module('capApp')
  .factory('userServiceAjax', function ($http,$rootScope) {
    // Service logic
    // ...

    // Public API here
    return {
      users: function () {
        return $http.get($rootScope.API_BASE+"/users/").then(function(response){return response.data});
      },
      roleOptions: function () {
          return $http.get($rootScope.API_BASE+"/roles/").then(function(response){return response.data});
      },
      addUser: function(user){
        return $http.post($rootScope.API_BASE+"/users/",user).then(function(response){return response.data});
      },
      deleteUser: function(id){
        return $http.delete($rootScope.API_BASE+"/users/"+id).then(function(response){return response.data});
      },
      updateUser: function(user){
        return $http.put($rootScope.API_BASE+"/users/",user).then(function(response){return response.data});
      },
      getUserById: function(id){
        return $http.get($rootScope.API_BASE+"/users/"+id).then(function(response){return response.data});
      },
      getLoggedInUserId: function(){
          return $http.get($rootScope.API_BASE+"/users/currentUser").then(function(response){return response.data});
        }
    };
  });
