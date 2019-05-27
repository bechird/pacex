'use strict';

/**
 * @ngdoc service
 * @name capApp.defaultStationServiceAjax
 * @description
 * # defaultStationServiceAjax
 * Factory in the capApp.
 */
angular.module('capApp')
  .factory('defaultStationServiceAjax', function ($http,$rootScope) {
    // Service logic
    // ...

    // Public API here
    return {
    	defaultStations: function () {
        return $http.get($rootScope.API_BASE+"/defaultStations/").then(function(response){return response.data});
      },
      addDefaultStation: function(defaultStation){
        return $http.post($rootScope.API_BASE+"/defaultStations/",defaultStation).then(function(response){return response.data});
      },
      deleteDefaultStation: function(id){
        return $http.delete($rootScope.API_BASE+"/defaultStations/"+id).then(function(response){return response.data});
      },
      updateDefaultStation: function(defaultStation){
        return $http.put($rootScope.API_BASE+"/defaultStations/",defaultStation).then(function(response){return response.data});
      },
      getDefaultStationById: function(id){
        return $http.get($rootScope.API_BASE+"/defaultStations/"+id).then(function(response){return response.data});
      }
    };
  });
