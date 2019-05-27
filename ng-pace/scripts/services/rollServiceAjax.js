'use strict';

/**
 * @ngdoc service
 * @name capApp.rollServiceAjax
 * @description
 * # rollServiceAjax
 * Factory in the capApp.
 */
angular.module('capApp')
  .factory('rollServiceAjax', function ($http,$httpParamSerializer,$rootScope) {
    // Service logic
    // ...

    // Public API here
    return {
      rolls: function () {
        return $http.get($rootScope.API_BASE+"/rolls").then(function(response){return response.data});
      },
      rollsQuick: function () {
          return $http.get($rootScope.API_BASE+"/rolls/quick").then(function(response){return response.data});
        },
      addRoll: function(roll){
    	  return $http.post($rootScope.API_BASE+"/rolls/",roll).then(function(response){return response.data});
      },
      updateRoll: function(roll){
        return $http.put($rootScope.API_BASE+"/rolls/",roll).then(function(response){return response.data});
      },
      deleteRoll: function(id){
          return $http.delete($rootScope.API_BASE+"/rolls/"+id).then(function(response){return response.data});
      },
      getRollById: function(id){
        return $http.get($rootScope.API_BASE+"/rolls/"+id).then(function(response){return response.data});
      },
      getAvailableRolls: function (color) {
        return $http.get($rootScope.API_BASE+"/rolls/available/"+color).then(function(response){return response.data});
      },
      getLeftOverRoll: function(parentRollId){
          return $http.get($rootScope.API_BASE+"/rolls/leftover/"+parentRollId).then(function(response){return response.data});
      },
      getProducedRoll: function(parentRollId){
          return $http.get($rootScope.API_BASE+"/rolls/produced/"+parentRollId).then(function(response){return response.data});
      },
      proposeJobs: function (color, paperType, rollWidth, rollId) {
          return $http.get($rootScope.API_BASE+"/rolls/proposeJobs/"+color+"/"+paperType+"/"+rollWidth+"/"+rollId).then(function(response){return response.data});
        },
      getScheduledRolls: function (stationId) {
          return $http.get($rootScope.API_BASE+"/rolls/scheduled/"+stationId).then(function(response){return response.data});
        },
      produceRoll: function(selectedJobsForProduction, selectedPfStationOption, selectedModeOption, rollWidth){
    	  return $http.post($rootScope.API_BASE+"/rolls/toProduce/"+selectedPfStationOption+"/"+selectedModeOption+"/"+rollWidth,selectedJobsForProduction).then(function(response){return response.data});
      },
      unassignRoll: function(rollId){
    	  return $http.get($rootScope.API_BASE+"/rolls/unassign/"+rollId).then(function(response){return response.data});
      },
      rollsIds: function () {
          return $http.get($rootScope.API_BASE+"/rolls/idsList").then(function(response){return response.data});
      },
      checkTrimCutSizes: function(rollWidth, selectedJobsForProduction){
    	  return $http.post($rootScope.API_BASE+"/rolls/checkTrimCut/"+rollWidth,selectedJobsForProduction).then(function(response){return response.data});
      }
    };
  });
