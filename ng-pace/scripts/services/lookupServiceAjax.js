'use strict';

/**
 * @ngdoc service
 * @name capApp.lookupServiceAjax
 * @description
 * # lookupServiceAjax
 * Factory in the capApp.
 */
angular.module('capApp')
  .factory('lookupServiceAjax', function ($http,$httpParamSerializer,$rootScope) {
    // Service logic
    // ...

    // Public API here
    return {
      readAll: function (type) {
        return $http.get($rootScope.API_BASE+"/lookups/"+type).then(function(response){return response.data});
      },
      addItem: function(type, item){
          return $http({
        	    method: 'POST',
        	    url: $rootScope.API_BASE+"/lookups/"+type,
        	    headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
                data: $httpParamSerializer(item)
        	}).then(function(response){
        		return response.data});
      },
      updateItem: function(type, item){
        return $http({
      	    method: 'PUT',
      	    url: $rootScope.API_BASE+"/lookups/"+type,
      	    headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
            data: $httpParamSerializer(item)
      	}).then(function(response){return response.data});
      },
      deleteItem: function(type, id){
        return $http.delete($rootScope.API_BASE+"/lookups/"+type+"/"+id).then(function(response){return response.data});
      },
      getItemById: function(type, id){
        return $http.get($rootScope.API_BASE+"/lookups/"+type+"/"+id).then(function(response){return response.data});
      },
      clearCache: function(){
          return $http.get($rootScope.API_BASE+"/lookups/cache").then(function(response){return response.data});
      },
      prefGroups: function(clientId){
          return $http.get($rootScope.API_BASE+"/lookups/prefGroups/"+clientId).then(function(response){return response.data});
      },
      prefGroupBySubject: function(prefSubject){
          return $http.get($rootScope.API_BASE+"/lookups/prefGroupBySubject/"+prefSubject).then(function(response){return response.data});
      },
      prefSubjects: function(groupId, clientId){
          return $http.get($rootScope.API_BASE+"/lookups/prefSubjects/list/"+groupId+"/"+clientId).then(function(response){return response.data});
      },
      prefSubjectsItems: function(prefSubject, clientId){
          return $http.get($rootScope.API_BASE+"/lookups/prefSubjects/items/"+prefSubject+"/"+clientId).then(function(response){return response.data});
      },
      getTmpLineById: function(id, type){
        return $http.get($rootScope.API_BASE+"/lookups/"+type+"/lines/"+id).then(function(response){return response.data});
      },
      addTemplateLine: function(tmpLine, type){
          return $http.post($rootScope.API_BASE+"/lookups/"+type+"/lines/",tmpLine).then(function(response){return response.data});
      },
      deleteTemplateLine: function(id, type){
          return $http.delete($rootScope.API_BASE+"/lookups/"+type+"/lines/"+id).then(function(response){return response.data});
      },
      updateTemplateLine: function(tmpLine, type){
         return $http.put($rootScope.API_BASE+"/lookups/"+type+"/lines/",tmpLine).then(function(response){return response.data});
      },
      templateNamingConventions: function(){
          return $http.get($rootScope.API_BASE+"/lookups/templateConventions").then(function(response){return response.data});
      },
      getPNLInfoTrial: function(partNum, widthHeight){
          return $http.post($rootScope.API_BASE+"/lookups/pnlTrial/"+partNum, widthHeight).then(function(response){return response.data});
      }
    };
  });
