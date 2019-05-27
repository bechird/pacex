'use strict';

describe('Controller: OrderAcceptanceCtrl', function () {

  // load the controller's module
  beforeEach(module('capApp'));

  var OrderAcceptanceCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    OrderAcceptanceCtrl = $controller('OrderAcceptanceCtrl', {
      $scope: scope
      // place here mocked dependencies
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(OrderAcceptanceCtrl.awesomeThings.length).toBe(3);
  });
});
