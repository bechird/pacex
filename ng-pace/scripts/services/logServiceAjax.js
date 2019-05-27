'use strict';

/**
 * @ngdoc service
 * @name capApp.logServiceAjax
 * @description
 * # logServiceAjax
 * Factory in the capApp.
 */
angular.module('capApp')
  .factory('logServiceAjax', function ($http,$httpParamSerializer,$rootScope) {
    // Service logic
    // ...

    // Public API here
    return {
      logs: function () {
        return $http.get($rootScope.API_BASE+"/logs").then(function(response){return response.data});
      },
      addLog: function(log){
    	  return $http.post($rootScope.API_BASE+"/logs/",log).then(function(response){return response.data});
      },
      addLogFromProd: function(log){
    	  return $http.post($rootScope.API_BASE+"/logs/fromProd",log).then(function(response){return response.data});
      },
      updateLog: function(log){
        return $http.put($rootScope.API_BASE+"/logs/",log).then(function(response){return response.data});
      },
      deleteLog: function(id){
          return $http.delete($rootScope.API_BASE+"/logs/"+id).then(function(response){return response.data});
      },
      getLogById: function(id){
        return $http.get($rootScope.API_BASE+"/logs/"+id).then(function(response){return response.data});
      },
      setCompletedJobsFromProd: function(completedJobs){
        return $http.post($rootScope.API_BASE+"/logs/completedJobs",completedJobs).then(function(response){return response.data});
      },
      setAllCompletedJobsFromProd: function(completedJobs){
          return $http.post($rootScope.API_BASE+"/logs/allCompletedJobs",completedJobs).then(function(response){return response.data});
      },
      handleInterruption: function(log){
          return $http.post($rootScope.API_BASE+"/logs/handleInterruption",log).then(function(response){return response.data});
      }
    };
  });
