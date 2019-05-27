'use strict';

describe('Controller: SchedulingCtrl', function () {

  // load the controller's module
  beforeEach(module('capApp'));

  var SchedulingCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    SchedulingCtrl = $controller('SchedulingCtrl', {
      $scope: scope
      // place here mocked dependencies
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(SchedulingCtrl.awesomeThings.length).toBe(3);
  });
});
