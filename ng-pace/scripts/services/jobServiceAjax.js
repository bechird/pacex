'use strict';

/**
 * @ngdoc service
 * @name capApp.jobServiceAjax
 * @description
 * # jobServiceAjax
 * Factory in the capApp.
 */
angular.module('capApp')
  //.factory('jobServiceAjax', function ($http,$httpParamSerializer,envConf) {
	.factory('jobServiceAjax', function ($http,$httpParamSerializer,$rootScope) {
  
    // Service logic
    // ...

    // Public API here
    return {
      jobs: function () {
        //return $http.get(envConf.getEnvConf()._env_service+$rootScope.API_BASE+"/jobs").then(function(response){return response.data});
        return $http.get($rootScope.API_BASE+"/jobs").then(function(response){return response.data});
      },
      addJob: function(job){
    	  return $http.post($rootScope.API_BASE+"/jobs/",job).then(function(response){return response.data});
      },
      updateJob: function(job){
        return $http.put($rootScope.API_BASE+"/jobs/",job).then(function(response){return response.data});
      },
      deleteJob: function(id){
          return $http.delete($rootScope.API_BASE+"/jobs/"+id).then(function(response){return response.data});
      },
      getJobById: function(id){
        return $http.get($rootScope.API_BASE+"/jobs/"+id).then(function(response){return response.data});
      },
      getJobByIdWithLam: function(id){
          return $http.get($rootScope.API_BASE+"/jobs/withLam/"+id).then(function(response){return response.data});
        },
      getAvailableJobsForScheduling: function () {
        return $http.get($rootScope.API_BASE+"/jobs/availableForScheduling").then(function(response){return response.data});
      },
      getStationJobs: function (stationId) {
          return $http.get($rootScope.API_BASE+"/jobs/stationJobs/"+stationId).then(function(response){return response.data});
      },
      getScheduledJobs: function (stationId) {
          return $http.get($rootScope.API_BASE+"/jobs/scheduled/"+stationId).then(function(response){return response.data});
      },
      calculatePressHours: function (colors, day, hoursType) {
        return $http.get($rootScope.API_BASE+"/jobs/calculatePressHours/"+colors+"/"+day+"/"+hoursType).then(function(response){return response.data});
      },
      getPressStationHours: function (colors, daysNeeded, cumulFlag, hoursType) {
          return $http.get($rootScope.API_BASE+"/jobs/getPressStationHours/"+colors+"/"+daysNeeded+"/"+cumulFlag+"/"+hoursType).then(function(response){return response.data});
      },
      getCapacityHours: function (colors, daysNeeded, cumulFlag) {
          return $http.get($rootScope.API_BASE+"/jobs/getCapacityHours/"+colors+"/"+daysNeeded+"/"+cumulFlag).then(function(response){return response.data});
      },
      getDayShifts: function () {
          return $http.get($rootScope.API_BASE+"/jobs/getDayShifts/").then(function(response){return response.data});
      },
      getJobsPercentagesByStatus: function (stationId, daysNeeded, hoursType, colors) {
          return $http.get($rootScope.API_BASE+"/jobs/getJobsPercentages/"+stationId+"/"+daysNeeded+"/"+hoursType+"/"+colors).then(function(response){return response.data});
      },
      calculateStationHours: function (stationId, hoursType) {
          return $http.get($rootScope.API_BASE+"/jobs/calculateStationHours/"+stationId+"/"+hoursType).then(function(response){return response.data});
      },
      splitJobs: function (jobId, newQuantity, cascadeFlag) {
          return $http.get($rootScope.API_BASE+"/jobs/splitJobs/"+jobId+"/"+newQuantity+"/"+cascadeFlag).then(function(response){return response.data});
      },
      unassignJob: function(jobId){
    	  return $http.get($rootScope.API_BASE+"/jobs/unassign/"+jobId).then(function(response){return response.data});
      },
      findNextStationJob: function(jobId){
    	  return $http.get($rootScope.API_BASE+"/jobs/findNext/"+jobId).then(function(response){return response.data});
      },
      findPrevJobData: function(jobId){
    	  return $http.get($rootScope.API_BASE+"/jobs/findPrevJobData/"+jobId).then(function(response){return response.data});
      },
      getOrderProducedQuantity: function(orderId){
    	  return $http.get($rootScope.API_BASE+"/jobs/orderProducedQuantity/"+orderId).then(function(response){return response.data});
      },
      getPartProducedQuantity: function(orderId, partNum){
        return $http.get($rootScope.API_BASE+"/jobs/partProducedQuantity/"+orderId+"/"+partNum).then(function(response){return response.data});
    },
      calculateLeftOverRollLength: function(log){
    	  return $http.post($rootScope.API_BASE+"/jobs/lorl/", log).then(function(response){return response.data});
      },
      calculateJobHoursAndLength: function(partNum, qty, rollWidth){
    	  return $http.get($rootScope.API_BASE+"/jobs/calculateJobHoursAndLength/"+partNum+"/"+qty+"/"+rollWidth).then(function(response){return response.data});
      },
      getJobOfPackagingByIsbn: function(isbn){
    	  return $http.get($rootScope.API_BASE+"/jobs/packaging/"+isbn).then(function(response){return response.data});
      }, getJobByOrderId: function(orderId){
    	  return $http.get($rootScope.API_BASE+"/jobs/Shippingjob/"+orderId).then(function(response){return response.data});
      },completeJob: function(jobId){
    	  return $http.get($rootScope.API_BASE+"/jobs/completeJob/"+jobId).then(function(response){return response.data});
      },
      getOrderJobs: function (orderId) {
          return $http.get($rootScope.API_BASE+"/jobs/orderJobs/"+orderId).then(function(response){return response.data});
      },
      jobsIds: function () {
          return $http.get($rootScope.API_BASE+"/jobs/idsList").then(function(response){return response.data});
      }
    };
  });
