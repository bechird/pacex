'use strict';

/**
 * @ngdoc service
 * @name capApp.partServiceAjax
 * @description
 * # partServiceAjax
 * Factory in the capApp.
 */
angular.module('capApp')
  .factory('partServiceAjax', function ($http,$httpParamSerializer,$rootScope) {
    // Service logic
    // ...

    // Public API here
    return {
      parts: function () {
        return $http.get($rootScope.API_BASE+"/parts/");
      },
      getIsbnPartsForAdd: function () {
          return $http.get($rootScope.API_BASE+"/parts/parentParts/add").then(function(response){return response.data});
      },
      generateNewPartNum: function () {
          return $http.get($rootScope.API_BASE+"/parts/generateNewPartNum").then(function(response){return response.data});
      },
      getIsbnPartsForEdit: function () {
            return $http.get($rootScope.API_BASE+"/parts/parentParts/edit").then(function(response){return response.data});
          },
      addPart: function(part){
          return $http.post($rootScope.API_BASE+"/parts/",part).then(function(response){return response.data});
      },
      updatePart: function(part){
        return $http.put($rootScope.API_BASE+"/parts/",part).then(function(response){return response.data});
      },
      searchParts: function(partSearchBean){
        return $http.post($rootScope.API_BASE+"/parts/search",partSearchBean).then(function(response){return response.data});
      },
      deletePart: function(id){
          return $http.delete($rootScope.API_BASE+"/parts/"+id).then(function(response){return response.data});
      },
      getPartById: function(id){
        return $http.get($rootScope.API_BASE+"/parts/"+id).then(function(response){return response.data});
      },
      getPartByCategory: function(partCategory, partNum){
          return $http.get($rootScope.API_BASE+"/parts/"+partCategory+"/"+partNum).then(function(response){return response.data});
      },
      getIsPartProducible: function(id){
          return $http.get($rootScope.API_BASE+"/parts/producible/"+id).then(function(response){return response.data});
      },
      readUploadedFile: function(partCategory, partNum){
          return $http.get($rootScope.API_BASE+"/parts/doc/"+partCategory+"/"+partNum).then(function(response){return response.data});
      },
      readFile: function(dataSupportId){
          return $http.get($rootScope.API_BASE+"/parts/document/"+dataSupportId).then(function(response){return response.data});
      },
      removeFile: function(partCategory, partNum){
          return $http.delete($rootScope.API_BASE+"/parts/doc/"+partCategory+"/"+partNum).then(function(response){return response.data});
      },
      getPartByIsbn: function(isbn){
        return $http.get($rootScope.API_BASE+"/parts/byIsbn/"+isbn).then(function(response){return response.data});
      },
      distinctIsbns: function () {
        return $http.get($rootScope.API_BASE+"/parts/distinctIsbn/").then(function(response){return response.data});
      },
    };
  }); 
