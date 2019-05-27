'use strict';

/**
 * @ngdoc service
 * @name capApp.stationServiceAjax
 * @description
 * # stationServiceAjax
 * Factory in the capApp.
 */
angular.module('capApp')
  .factory('stationServiceAjax', function ($http,$httpParamSerializer,$rootScope) {
    // Service logic
    // ...

    // Public API here
    return {
      stations: function () {
        return $http.get($rootScope.API_BASE+"/stations/").then(function(response){return response.data});
      },
      stationsMenu: function () {
          return $http.get($rootScope.API_BASE+"/stations/menuList").then(function(response){return response.data});
      },
      stationsQuick: function () {
        return $http.get($rootScope.API_BASE+"/stations/quick").then(function(response){return response.data});
      },
      stationsForOverview: function () {
        return $http.get($rootScope.API_BASE+"/stations/overview").then(function(response){return response.data});
      },
      moveUp: function (itemId, stationId, level) {
          return $http.get($rootScope.API_BASE+"/stations/overview/"+itemId+'/'+stationId+'/'+level).then(function(response){return response.data});
      },
      addStation: function(station){
          return $http({
        	    method: 'POST',
        	    url: $rootScope.API_BASE+"/stations/",
        	    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                data: $httpParamSerializer(station)
        	}).then(function(response){return response.data});
      },
      updateStation: function(station){
        return $http({
      	    method: 'PUT',
      	    url: $rootScope.API_BASE+"/stations/",
      	    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            data: $httpParamSerializer(station)
      	}).then(function(response){return response.data});
      },
      deleteStation: function(id){
          return $http.delete($rootScope.API_BASE+"/stations/"+id).then(function(response){return response.data});
      },
      loadStationById: function(id){
        return $http.get($rootScope.API_BASE+"/stations/load/"+id).then(function(response){return response.data});
      },
      getStationById: function(id){
          return $http.get($rootScope.API_BASE+"/stations/"+id).then(function(response){return response.data});
        }
    };
  });
