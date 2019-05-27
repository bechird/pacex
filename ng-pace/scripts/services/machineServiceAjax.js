'use strict';

/**
 * @ngdoc service
 * @name capApp.machineServiceAjax
 * @description
 * # machineServiceAjax
 * Factory in the capApp.
 */
angular.module('capApp')
  .factory('machineServiceAjax', function ($http,$rootScope) {
    // Service logic
    // ...

    // Public API here
    return {
      machines: function () {
        return $http.get($rootScope.API_BASE+"/machines/").then(function(response){return response.data});
      },
      addMachine: function(machine){
        return $http.post($rootScope.API_BASE+"/machines/",machine).then(function(response){return response.data});
      },
      deleteMachine: function(id){
        return $http.delete($rootScope.API_BASE+"/machines/"+id).then(function(response){return response.data});
      },
      updateMachine: function(machine){
        return $http.put($rootScope.API_BASE+"/machines/",machine).then(function(response){return response.data});
      },
      getMachineById: function(id){
        return $http.get($rootScope.API_BASE+"/machines/"+id).then(function(response){return response.data});
      },
      assignToMachine: function(selectedRollsForAssignment){
    	  return $http.post($rootScope.API_BASE+"/machines/toAssign",selectedRollsForAssignment).then(function(response){return response.data});
      },
      assignSectionsToMachine: function(selectedBatchesForAssignment){
    	  return $http.post($rootScope.API_BASE+"/machines/sectionsToAssign",selectedBatchesForAssignment).then(function(response){return response.data});
      },
      createSection: function(selectedBatchesForAssignment){
    	  return $http.post($rootScope.API_BASE+"/machines/createSection",selectedBatchesForAssignment).then(function(response){return response.data});
      },
      assignJobsToMachine: function(selectedJobsForAssignment){
    	  return $http.post($rootScope.API_BASE+"/machines/jobsToAssign",selectedJobsForAssignment).then(function(response){return response.data});
      },
      startResumeMachine: function(machineId, selectedModeOption){
    	  return $http.post($rootScope.API_BASE+"/machines/startResume/"+selectedModeOption,machineId).then(function(response){return response.data});
      },
      getMachineWorkingCount: function(stationId, color){
        return $http.get($rootScope.API_BASE+"/machines/machineCount/"+stationId+"/"+color).then(function(response){return response.data});
      },
      getRasterLocation: function(pressJobId){
          return $http.get($rootScope.API_BASE+"/machines/rasterLocation/"+pressJobId).then(function(response){return response.data});
      },
      getDefaultPrinterSpeed: function(){
          return $http.get($rootScope.API_BASE+"/machines/printerSpeed/").then(function(response){return response.data});
      },
      updateCurrentJobToMachine: function(orderId,machineId){
          return $http.get($rootScope.API_BASE+"/machines/currentJob/"+orderId+"/"+machineId).then(function(response){return response.data});
      }, 
      fetchALLMachineShipping: function(){
          return $http.get($rootScope.API_BASE+"/machines/SHIPPING/").then(function(response){return response.data});
      }, 
      machinesQuick: function(){
          return $http.get($rootScope.API_BASE+"/machines/quick/").then(function(response){return response.data});
      }
    };
  });
