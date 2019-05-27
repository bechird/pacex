'use strict';


angular.module('capApp')
    .factory('ProductionOrderServiceAjax', function($http, $httpParamSerializer, $rootScope) {

        return {
            getOnlineOrder: function() {
                return $http.get($rootScope.API_BASE + "/production/synchronize").then(function(response) { return response.data });
            },



        };
    });