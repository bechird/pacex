'use strict';


angular.module('capApp')
  .factory('prodReportsServiceAjax', function ($http,$httpParamSerializer,$rootScope) {
   
    return {
        getReportByDay: function(infos){
            return $http.post($rootScope.API_BASE+"/reports/day", infos).then(function(response){return response.data});
        },getReportByRange:function(infos){
           return $http.post($rootScope.API_BASE+"/reports/range", infos).then(function(response){return response.data});
        }
        
        
    };
  });
