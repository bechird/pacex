'use strict';

angular.module('capApp')
    .controller('DeliveryFormBuilderCtrl', function(palletteServiceAjax, $scope, $log, $location, $localStorage, $window, $route, $rootScope, $translate, $ngConfirm) {


        $scope.palettId;
        $scope.pallette;
        $scope.currentBook;
        $scope.orderUnits;
        $scope.toogleAll = true;
        $scope.currentOrder;

        $scope.getPallett = function() {

            //re init scope data
            $scope.pallette = null;

            palletteServiceAjax.readPallette($scope.palettId).then(function(data) {

                if (data.error != null) {

                    if (data.error === "notFound") { $ngConfirm("<strong>" + $translate.instant('deliveryFormBuilderI18n.errorNotFound') + "</strong>", "<strong style='color:red;'>" + $translate.instant('deliveryFormBuilderI18n.errorTitle') + "</strong>"); }
                    if (data.error === "alreadyDelivered") { $ngConfirm("<strong>" + $translate.instant('deliveryFormBuilderI18n.errorAlreadyDelivered') + "</strong>", "<strong style='color:red;'>" + $translate.instant('deliveryFormBuilderI18n.errorTitle') + "</strong>"); }
                    if (data.error === "badRecord") { $ngConfirm("<strong>" + $translate.instant('deliveryFormBuilderI18n.errorBadRecord') + "</strong>", "<strong style='color:red;'>" + $translate.instant('deliveryFormBuilderI18n.errorTitle') + "</strong>"); }

                    return;
                }

                if (data.destination === null || data.destination === '') {
                    $ngConfirm("<strong>Destination address missing</strong>", "<strong style='color:red;'>This pallet doesn't contain destination address</strong>");
                    return;
                }

                $scope.pallette = data;

                //initialy expand the first order
                if ($scope.pallette.orders && $scope.pallette.orders.length > 0) {
                    //$scope.pallette.orders[0].expanded = true;
                }

                for (var i = 0; i < $scope.pallette.orders.length; i++) {
                    $scope.calcUnits($scope.pallette.orders[i]);
                }

            });

        };
        $scope.printSlip = function(id) {
            palletteServiceAjax.downloadPackagingSlip(id).then(function(data) {
                var blob = new Blob([data], { type: "application/pdf" });
                var objectUrl = URL.createObjectURL(blob);
                var a = document.createElement('a');
                a.href = objectUrl;
                a.target = '_blank';
                a.download = "PDFFSlip" + '.pdf';
                document.body.appendChild(a);
                a.click();
                window.open(objectUrl);
            });
        }


        $scope.calcUnits = function(order) {
            var sum = 0;
            for (var i = 0; i < order.books.length; i++) {
                var q = order.books[i].packageBook.heightQty * order.books[i].packageBook.widthQty * order.books[i].packageBook.depthQty * order.books[i].quantity
                sum = sum + q;

            }
            order.units = sum;
        }

        $scope.toggleExpanded = function(order) {

            order.expanded = !order.expanded;
        }

        $scope.toggleMulti = function() {
            $scope.toogleAll = !$scope.toogleAll;
        }

        $scope.editBook = function(book, order) {



            $ngConfirm({
                theme: 'modern',
                boxWidth: '50%',
                useBootstrap: false,
                animation: 'top',
                closeAnimation: 'top',
                title: $translate.instant('deliveryFormBuilderI18n.edit'),

                content: '<div class="row">' +

                    '<div class="col-md-3">' +
                    '<label class="control-label">' + $translate.instant('deliveryFormBuilderI18n.height') + '</label>' +
                    '<input type="number" min="0" ng-model="currentBook.packageBook.heightQty" class="form-control">' +
                    '</div>' +

                    '<div class="col-md-3">' +
                    '<label class="control-label">' + $translate.instant('deliveryFormBuilderI18n.width') + '</label>' +
                    '<input type="number" min="0" ng-model="currentBook.packageBook.widthQty" class="form-control">' +
                    '</div>' +

                    '<div class="col-md-3">' +
                    '<label class="control-label">' + $translate.instant('deliveryFormBuilderI18n.depth') + '</label>' +
                    '<input type="number" min="0" ng-model="currentBook.packageBook.depthQty" class="form-control">' +
                    '</div>' +

                    '<div class="col-md-3">' +
                    '<label class="control-label">#PCBs</label>' +
                    '<input type="number" min="0" ng-model="currentBook.quantity" class="form-control">' +
                    '</div>' +

                    '</div>',
                scope: $scope,
                onScopeReady: function(scope) {
                    scope.currentBook = angular.copy(book);
                },
                buttons: {


                    ok: {
                        text: $translate.instant('deliveryFormBuilderI18n.save'),
                        btnClass: 'btn-primary',
                        keys: ['enter'],
                        action: function(scope) {
                            book.packageBook.heightQty = scope.currentBook.packageBook.heightQty;
                            book.packageBook.widthQty = scope.currentBook.packageBook.widthQty;
                            book.packageBook.depthQty = scope.currentBook.packageBook.depthQty;
                            book.quantity = scope.currentBook.quantity;

                            $scope.calcUnits(order);
                            scope.$apply();
                        }
                    },
                    cancel: {
                        text: $translate.instant('deliveryFormBuilderI18n.cancel'),
                        action: function(scope) {}
                    }



                }


            });

        }


        $scope.addLine = function(order) {

            var sourceBook = order.books[0];
            var targetBook = angular.copy(sourceBook);

            targetBook.id.palletteId = null;
            targetBook.id.packagePartId = null;

            targetBook.packageBook.heightQty = 0;
            targetBook.packageBook.widthQty = 0;
            targetBook.packageBook.depthQty = 0;
            targetBook.quantity = 0;

            order.books.push(targetBook);
        }

        $scope.saveChanges = function() {



            $ngConfirm({
                title: $translate.instant('deliveryFormBuilderI18n.confirmTitle'),

                content: '<strong>' + $translate.instant('deliveryFormBuilderI18n.confirmBody') + '</strong>',
                scope: $scope,
                buttons: {

                    ok: {
                        text: $translate.instant('deliveryFormBuilderI18n.save'),
                        btnClass: 'btn-primary',
                        action: function(scope) {
                            palletteServiceAjax.saveBlRelatedPallette($scope.pallette).then(function(data) {

                            });
                        }
                    },
                    cancel: {
                        text: $translate.instant('deliveryFormBuilderI18n.cancel'),
                        action: function(scope) {}
                    }



                }


            });

        }





        $scope.confirmDelete = function(order) {



            $ngConfirm({
                title: $translate.instant('deliveryFormBuilderI18n.confirmTitle'),

                content: '<strong>' + $translate.instant('deliveryFormBuilderI18n.confirmDeleteBody') + '</strong>',
                scope: $scope,
                buttons: {

                    ok: {
                        text: $translate.instant('deliveryFormBuilderI18n.delete'),
                        btnClass: 'btn-primary',
                        action: function(scope) {
                            //loop in pallett orders and delete the chosen order from the array
                            for (var i = 0; i < scope.pallette.orders.length; i++) {
                                var ord = scope.pallette.orders[i];
                                if (ord.orderId === order.orderId) {
                                    scope.pallette.orders.splice(i, 1);
                                    break;
                                }
                            }

                            //update display
                            scope.$apply();
                        }
                    },
                    cancel: {
                        text: $translate.instant('deliveryFormBuilderI18n.cancel'),
                        action: function(scope) {}
                    }



                }


            });

        }



    });