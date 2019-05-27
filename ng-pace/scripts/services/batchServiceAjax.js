'use strict';

/**
 * @ngdoc service
 * @name capApp.batchServiceAjax
 * @description
 * # batchServiceAjax
 * Factory in the capApp.
 */
angular.module('capApp')
  //.factory('batchServiceAjax', function ($http,$httpParamSerializer,envConf) {
	.factory('batchServiceAjax', function ($http,$httpParamSerializer,$rootScope) {
  
    // Service logic
    // ...

    // Public API here
    return {
      getBatchById: function(id){
        return $http.get($rootScope.API_BASE+"/batches/id/"+id).then(function(response){return response.data});
      },
      reCreateBatch: function(batchId){
    	  return $http.get($rootScope.API_BASE+"/batches/recreate/"+batchId).then(function(response){return response.data});
      },
      getSectionByName: function(name){
        return $http.get($rootScope.API_BASE+"/sections/name/"+name).then(function(response){return response.data});
      },
      getSectionById: function(id){
        return $http.get($rootScope.API_BASE+"/sections/id/"+id).then(function(response){return response.data});
      },
      reCreateSection: function(sectionId){
    	  return $http.get($rootScope.API_BASE+"/sections/rerun/"+sectionId).then(function(response){return response.data});
      },
      unassignSection: function(sectionId){
    	  return $http.get($rootScope.API_BASE+"/sections/unassign/"+sectionId).then(function(response){return response.data});
      },
      updateSection: function(section){
        return $http.put($rootScope.API_BASE+"/sections/",section).then(function(response){return response.data});
      }
    };
  });
