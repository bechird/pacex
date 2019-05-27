	'use strict';

/**
 * @ngdoc function
 * @name capApp.controller:ProductionDashboardCtrl
 * @description
 * # ProductionDashboardCtrl
 * Controller of the capApp
 */
angular.module('capApp')
  .controller('AdminSseStatusCtrl', ['$rootScope', '$scope', '$filter', 'notificationService','SSE_CONSTANTS', '$log', '$controller',
   function ($rootScope, $scope, $filter, notificationService, SSE_CONSTANTS, $log, $controller) {
	  	
	     $scope.serversStatus = [];
	  
		//sse management
	    notificationService.subscribeToApp();
		$scope.$on('$destroy', function() {
			$log.log("leaving AdminSseStatusCtrl controller, unsubscribe from sse");
			notificationService.getPubSub().unsubscribe(SSE_CONSTANTS.appEventsTopic);
			notificationService.unsubscribeFromApp();
		});
		//*****************************************************************************
	  
	  notificationService.getPubSub().subscribe(SSE_CONSTANTS.appEventsTopic, function (event) {
			var eventData = JSON.parse(event.data);
			if(eventData.target == 'SseStatus'){
				$scope.serversStatus = eventData.object;
				$scope.$apply();
			}			
		});


}]);

