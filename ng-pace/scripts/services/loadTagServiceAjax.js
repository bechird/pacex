'use strict';

/**
 * @ngdoc service
 * @name capApp.loadTagServiceAjax
 * @description
 * # loadtagServiceAjax
 * Factory in the capApp.
 */
angular.module('capApp')
  .factory('loadTagServiceAjax', function ($http,$httpParamSerializer,$rootScope) {
    // Service logic
    // ...

    // Public API here
    return {
      loadTags: function () {
        return $http.get($rootScope.API_BASE+"/loadTags").then(function(response){return response.data});
      },
      addLoadTag: function(loadTag){
    	  return $http.post($rootScope.API_BASE+"/loadTags/",loadTag).then(function(response){return response.data});
      },
      addLoadTagFromProd: function(loadTag){
    	  return $http.post($rootScope.API_BASE+"/loadTags/fromProd",loadTag).then(function(response){return response.data});
      },
      updateLoadTag: function(loadTag){
        return $http.put($rootScope.API_BASE+"/loadTags/",loadTag).then(function(response){return response.data});
      },
      updateLoadTagFromProd: function(loadTag){
          return $http.put($rootScope.API_BASE+"/loadTags/fromProd",loadTag).then(function(response){return response.data});
      },
      deleteLoadTag: function(id){
          return $http.delete($rootScope.API_BASE+"/loadTags/"+id).then(function(response){return response.data});
      },
      getLoadTagById: function(id){
        return $http.get($rootScope.API_BASE+"/loadTags/"+id).then(function(response){return response.data});
      }
    };
  });
