'use strict';

/**
 * @ngdoc function
 * @name capApp.controller:SidebarCtrl
 * @description
 * # SidebarCtrl
 * Controller of the capApp
 */
angular.module('capApp')
  .controller('SidebarCtrl','$translatePartialLoader', '$translate', function($scope , $translatePartialLoader, $translate) {
   
	  $translatePartialLoader.addPart('sidebar');
	  $translate.refresh();
	  
    $scope.toggleSidebar = function() {
        $scope.toggle = !$scope.toggle;
        $cookieStore.put('toggle', $scope.toggle);
    };
    
});

