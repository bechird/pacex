'use strict';
/**
 * @ngdoc function
 * @name capApp.controller:ShippingStationCtrl
 * @description
 * # ShippingStationCtrl
 * Controller of the capApp
 */
angular.module('capApp')
    .controller('SlipsCtrl', ['$localStorage', '$resource', 'DTOptionsBuilder', 'DTColumnBuilder', '$scope', 'rollServiceAjax', 'stationServiceAjax',
        'machineServiceAjax', 'jobServiceAjax', 'lookupServiceAjax', 'palletteServiceAjax', 'orderServiceAjax', '$uibModal',
        '$routeParams', '$route', '$timeout', 'SweetAlert', '$filter', 'toasty', '$translatePartialLoader', '$translate', '$http', '$rootScope', "$ngConfirm", "$compile",
        function($localStorage, $resource, DTOptionsBuilder, DTColumnBuilder, $scope, rollServiceAjax, stationServiceAjax, machineServiceAjax, jobServiceAjax,
            lookupServiceAjax, palletteServiceAjax, orderServiceAjax, $uibModal, $routeParams, $route, $timeout, SweetAlert, $filter, toasty,
            $translatePartialLoader, $translate, $http, $rootScope, $ngConfirm, $compile) {

            $translatePartialLoader.addPart('packagingSlipsOverview');
            $translate.refresh();

            $.fn.dataTable.ext.errMode = 'none';
            var token = $localStorage.oauthToken;

            var vm = this;
            vm.pals = {}
            vm.dtInstance = {};


            if ($translate.use() == 'fr') {
                $scope.dataTables = {
                    "sProcessing": "Traitement en cours...",
                    "sSearch": "Rechercher&nbsp;:",
                    "sLengthMenu": "Afficher _MENU_ &eacute;l&eacute;ments",
                    "sInfo": "Affichage de l'&eacute;l&eacute;ment _START_ &agrave; _END_ sur _TOTAL_ &eacute;l&eacute;ments",
                    "sInfoEmpty": "Affichage de l'&eacute;l&eacute;ment 0 &agrave; 0 sur 0 &eacute;l&eacute;ment",
                    "sInfoFiltered": "(filtr&eacute; de _MAX_ &eacute;l&eacute;ments au total)",
                    "sInfoPostFix": "",
                    "sLoadingRecords": "Chargement en cours...",
                    "sZeroRecords": "Aucun &eacute;l&eacute;ment &agrave; afficher",
                    "sEmptyTable": "Aucune donn&eacute;e disponible dans le tableau",
                    "oPaginate": {
                        "sFirst": "|<",
                        "sPrevious": "<",
                        "sNext": ">",
                        "sLast": ">|"
                    },
                    "oAria": {
                        "sSortAscending": ": activer pour trier la colonne par ordre croissant",
                        "sSortDescending": ": activer pour trier la colonne par ordre d&eacute;croissant"
                    }

                }
            } else if ($translate.use() == 'es') {
                $scope.dataTables = {
                    "sProcessing": "Procesando...",
                    "sLengthMenu": "Mostrar _MENU_ registros",
                    "sZeroRecords": "No se encontraron resultados",
                    "sEmptyTable": "Ningún dato disponible en esta tabla",
                    "sInfo": "Mostrando registros del _START_ al _END_ de un total de _TOTAL_ registros",
                    "sInfoEmpty": "Mostrando registros del 0 al 0 de un total de 0 registros",
                    "sInfoFiltered": "(filtrado de un total de _MAX_ registros)",
                    "sInfoPostFix": "",
                    "sSearch": "Buscar:",
                    "sUrl": "",
                    "sInfoThousands": ",",
                    "sLoadingRecords": "Cargando...",
                    "oPaginate": {
                        "sFirst": "|<",
                        "sPrevious": "<",
                        "sNext": ">",
                        "sLast": ">|"
                    },
                    "oAria": {
                        "sSortAscending": ": Activar para ordenar la columna de manera ascendente",
                        "sSortDescending": ": Activar para ordenar la columna de manera descendente"
                    }
                }
            };

            var serverData = function(sSource, aoData, fnCallback, oSettings) {
                $http.post($rootScope.API_BASE + "/pallets/slips/paginated", { aoData }).then(function(result) {

                    var records = {
                        'draw': result.data.draw,
                        'recordsTotal': result.data.recordsTotal,
                        'recordsFiltered': result.data.recordsFiltered,
                        'data': result.data.data
                    };

                    $scope.Pallettes = result.data.data;
                    fnCallback(records);
                });
            }

            vm.dtOptions = DTOptionsBuilder.newOptions()
                .withFnServerData(serverData)
                .withDOM('frtip')
                .withPaginationType('full_numbers')
                .withOption('responsive', false)
                .withOption('processing', true)
                .withOption('serverSide', true)
                .withOption('order', [1, 'asc'])
                .withDisplayLength(25)
                .withLanguage($scope.dataTables)
                .withOption('createdRow', function(row, data, index) { $compile(angular.element(row).contents())($scope) })
                .withColumnFilter({

                    aoColumns: [
                        { "bSearchable": false },
                        { type: 'number' },
                        { type: 'number' },
                        { type: 'text', bRegex: true, bSmart: true },
                        { type: 'text', bRegex: true, bSmart: true },
                        { type: 'text', bRegex: true, bSmart: true },
                        { type: 'select', bRegex: false, values: ['ACTIVE', 'PAUSED', 'COMPLETE', 'DELIVERED'] },
                        { type: 'text', bRegex: true, bSmart: true },
                        { "bSearchable": false }
                    ]
                });

            vm.dtColumns = [
                DTColumnBuilder.newColumn('id').withOption('name', 'expanded').notSortable().renderWith(function(data) {

                    data = '<button style="padding-left: 10px;" class="btn bgm-cyan waves-circle waves-effect" ng-click="getOrderByPalltte(' + data + ')">' +
                        '<i class="zmdi zmdi-open-in-new"></i>' +
                        '</button>';
                    return data;


                }),


                DTColumnBuilder.newColumn('id').withTitle('Pallet#').withOption('name', 'id').renderWith(function(data) {
                    data = 'PL' + data;
                    return data;
                }),

                DTColumnBuilder.newColumn('blNumber').withTitle("BL#").withOption('name', 'blNumber').withOption('defaultContent', ' '),
                DTColumnBuilder.newColumn('destination').withTitle($translate.instant('RE_ADRESS')).withOption('name', 'destination').withOption('defaultContent', ' '),
                DTColumnBuilder.newColumn('qtyPcbInPallette').withTitle($translate.instant('QTY_PCB_DELIVRED')).notSortable().withOption('name', 'qtyPcbInPallette').renderWith(function(data) {
                    data = data;
                    return data;
                }),
                DTColumnBuilder.newColumn('qtyBookInPallette').withTitle($translate.instant('QTY_BOOK_DELIVRED')).notSortable().withOption('name', 'qtyBookInPallette').renderWith(function(data) {
                    data = data;
                    return data;
                }),
                DTColumnBuilder.newColumn('statusPallette').withTitle($translate.instant('STATUS')).withOption('name', 'statusPallette').renderWith(function(data) {
                    if (data === 'COMPLETE') {
                        data = '<span class="badge bgm-complete">' + data + '</span>';
                    }

                    if (data === 'DELIVERED') {
                        data = '<span class="badge bgm-delivered">' + data + '</span>';
                    }

                    if (data === 'PAUSED') {
                        data = '<span class="badge bgm-paused">' + data + '</span>';
                    }

                    return data;
                }),

                DTColumnBuilder.newColumn('delivredDate').withTitle($translate.instant('DeliveryDate')).withOption('name', 'delivredDate').withOption('defaultContent', ' ').renderWith(function(data) {
                    data = $filter('date')(data, 'MMM dd');
                    return data;
                }), ,
                DTColumnBuilder.newColumn('id')
                .withOption('name', 'actions').notSortable().renderWith(function(data, type, full, meta) {
                    data = '<button style="padding-left: 10px;" class="btn bgm-cyan waves-circle waves-effect" ng-click="PalletteDelivred(' + data + ')">' +
                        '<i class="zmdi zmdi-refresh-alt"></i>' +
                        '</button>' +
                        '<button style="padding-left: 11px;" class="btn bgm-bluegray waves-circle waves-effect" ng-click="downloadPdf(' + data + ')">' +
                        '<i class="zmdi zmdi-print"></i>' +
                        '</button>' +
                        '<button style="padding-left: 11px;" class="btn bgm-orange-900 waves-circle waves-effect" ng-click="openPalletteToEdit(' + data + ')">' +
                        '<i class="zmdi zmdi-edit"></i>' +
                        '</button>' +
                        '<button style="padding-left: 11px;" class="btn bgm-deeporange waves-circle waves-effect" ng-click="deletePallette(' + data + ')">' +
                        '<i class="zmdi zmdi-delete"></i>' +
                        '</button>';
                    return data;

                })



            ];

            //$translate.instant('STATUS')




            $scope.fetchPalletByOrder = function(orderId) {
                orderServiceAjax.palletteByOrder(orderId).then(function(data) {
                    $scope.palletsByOrder = data;
                });
            };



            $scope.Pallettes = [];
            $scope.orders = [];
            $scope.checkedPallets = [];
            $scope.qtyDelivred = [];
            $scope.checkedOrders = [];
            $scope.booksQuantities = [];
            $scope.qtyInBl = [];
            $scope.palletteIds = [];
            //$scope.ordersInPallet = [];
            $scope.ordersByPallette = [];
            $scope.QuantitiesOrderByPallette = [];
            $scope.palletsByOrder = [];
            $scope.editPallette;
            /*orderServiceAjax.orderInPallette().then(function(data) {
                $scope.ordersInPallet = data;
            });*/

            $scope.fetchByPcbByPallette = function(pallet) {
                var qtyPcb = 0;
                var bookQty = 0;
                for (var i = 0; i < pallet.books.length; i++) {
                    var pcb = pallet.books[i];
                    qtyPcb += pcb.quantity;
                    bookQty += pcb.quantity * pcb.packageBook.quantity;
                }
                pallet.qtyPcb = qtyPcb;
                pallet.bookQty = bookQty;
            }
            $scope.completeOrderStatus = function(orderId) {

                orderServiceAjax.completeStatus(orderId).then(function(data) {

                });
            }
            $scope.getOrderByPalltte = function(id) {
                var ordersByPallette = [];
                var QuantitiesOrderByPallette = [];

                palletteServiceAjax.orderInfoByPallette(id).then(function(data) {
                    var orders = data.orders;
                    for (var i = 0; i < orders.length; i++) {
                        var order = orders[i];
                        ordersByPallette.push(orders[i]);
                        QuantitiesOrderByPallette.push(data.books[i]);

                    }


                    var htmlTable = '<table class="mdl-data-table table-bordered  mdl-shadow--1dp ng-isolate-scope no-footer" width="100%" >' +
                        '<thead>' +
                        '<tr>' +
                        '<th>Order#</th>' +
                        '<th>ISBN</th>' +
                        '<th>Titre</th>' +
                        '<th>Quantiiy Needed</th>' +
                        '<th>Quantity In Pallet</th>' +
                        '</tr>' +
                        '</thead>' +
                        '<tbody>';


                    for (var i = 0; i < ordersByPallette.length; i++) {
                        var order = ordersByPallette[i];


                        htmlTable = htmlTable + '<tr>' +

                            '<td>' + order.orderNum + '</td>' +
                            '<td>' + order.orderPart.part.isbn + '</td>' +
                            '<td>' + order.orderPart.part.title + '</td>' +
                            '<td>' + order.orderPart.quantity + '</td>' +
                            '<td>' + QuantitiesOrderByPallette[i] + '</td>' +
                            '</tr>';



                    }


                    htmlTable = htmlTable + '</tbody>' + '</table>';



                    $ngConfirm({
                        title: 'Orders List',
                        content: htmlTable,
                        theme: 'modern',
                        useBootstrap: false,
                        scope: $scope,
                        buttons: {

                            ok: {
                                text: 'close',
                                btnClass: 'btn-primary',
                                action: function(scope) {}
                            }


                        }

                    });



                });
            }
            $scope.deletePallette = function(id) {
                SweetAlert.swal({
                    title: $filter('translate')('slips_js.sure'),
                    text: $filter('translate')('slips_js.delete_pallet') + id + "!",
                    type: "warning",
                    showCancelButton: true,
                    confirmButtonColor: "#DD6B55",
                    confirmButtonText: $filter('translate')('slips_js.Yes_delete'),
                    cancelButtonText: $filter('translate')('slips_js.please_cancel'),
                    closeOnConfirm: true,
                    closeOnCancel: true
                }, function(isConfirm) {
                    if (isConfirm) {
                        palletteServiceAjax.deletePallette(id).then(function(data) {
                            $scope.Pallettes = $filter('filter')($scope.Pallettes, (item) => {
                                return item.id != id;
                            });
                        });
                    } else {
                        return;
                    }
                });
            }

            $scope.clearAll = function() {
                $('input[name=pallets]:checked').prop('checked', false);
                $scope.checkedPallets = [];
            };

            $scope.fillOrderChecked = function() {
                $scope.checkedOrders = [];
                $scope.booksQuantities = [];
                $scope.checkedPallets = [];
                $scope.palletteIds = [];
                $scope.orderShippedQty = [];
                var firstvalue = $('input[name=pallets]:checked')[0];
                var firstpallet = angular.fromJson(firstvalue.id);
                var dest = firstpallet.destination;
                var diffDest = false;
                //angular.forEach($('input[name=pallets]:checked'), function (value, key) {
                for (var i = 0; i < $('input[name=pallets]:checked').length; i++) {
                    var value = $('input[name=pallets]:checked')[i];
                    var pallet = angular.fromJson(value.id);
                    $scope.checkedPallets.push(pallet);
                    $scope.palletteIds.push(pallet.id);
                    palletteServiceAjax.orderInfoByPallette(pallet.id).then(function(data) {
                        var orders = data.orders;
                        for (var i = 0; i < orders.length; i++) {
                            var order = orders[i];
                            $scope.checkedOrders.push(order);
                            $scope.booksQuantities.push(data.books[i]);
                            palletteServiceAjax.orderShipped(order.orderNum, pallet.id).then(function(data) {
                                $scope.orderShippedQty.push(data);
                            });
                        }
                    });
                    diffDest = (diffDest || dest != pallet.destination);
                };
                if (diffDest === true) {
                    SweetAlert.swal({
                        title: $filter('translate')('slips_js.sure'),
                        text: $filter('translate')('slips_js.error_pallets'),
                        type: "warning",
                        showCancelButton: true,
                        confirmButtonColor: "#DD6B55",
                        confirmButtonText: "Continue",
                        cancelButtonText: $filter('translate')('slips_js.cancel'),
                        closeOnConfirm: true,
                        closeOnCancel: true
                    }, function(isConfirm) {
                        if (isConfirm) {
                            $scope.openBonDeLivraisonModal();
                        } else {
                            return;
                        }
                    });
                } else { $scope.openBonDeLivraisonModal(); }


            }

            /*
            orderServiceAjax.finishingOrder().then(function (data) {
            	$scope.orders = data;
            	$scope.qtyInPalletteByOrder();
            });
            */


            $scope.PalletteDelivred = function(id) {


                palletteServiceAjax.updatePalletteStatus(id).then(function(data) {
                    //pallette.statusPallette = data.statusPallette;
                    $scope.Pallettes = $filter('filter')($scope.Pallettes, (item) => {
                        return item.id != data.id;
                    });
                    $scope.Pallettes.push(data);
                });

            }



            $scope.qtyInPalletteByOrder = function() {
                angular.forEach($scope.orders, function(value, key) {
                    orderServiceAjax.qtyInPalletteByOrder(value.orderNum).then(function(data) {
                        $scope.qtyDelivred.push(data);
                        var bls = value.order_Bl;
                        var qty = 0;
                        angular.forEach(bls, function(value, key) {
                            qty += value.qty;
                        });
                        $scope.qtyInBl.push(qty);
                    });
                });
            }

            $scope.downloadPdf = function(id) {
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


            $scope.downloadBlInterforum = function() {
                orderServiceAjax.downloadBlInterforum().then(function(data) {
                    var blob = new Blob([data], { type: "application/pdf" });
                    var objectUrl = URL.createObjectURL(blob);
                    var a = document.createElement('a');
                    a.href = objectUrl;
                    a.target = '_blank';
                    a.download = "BL" + '.pdf';
                    document.body.appendChild(a);
                    a.click();
                    window.open(objectUrl);
                    orderServiceAjax.finishingOrder().then(function(data) { $scope.orders = data });
                });
            }

            $scope.openBonDeLivraisonModal = function() {
                var modalInstance = $uibModal.open({
                    backdrop: 'static',
                    keyboard: false,
                    animation: true,
                    reloadOnSearch: false,
                    templateUrl: './views/bonDeLivraisonModal.html',
                    controller: 'openBonDeLivraisonModalInstanceCtrl',
                    scope: $scope,
                    size: 'lg',
                });
            };

            $scope.openPalletteToEdit = function(id) {

                //find pallette
                var pallette;
                for (var i = 0; i < $scope.Pallettes.length; i++) {
                    if ($scope.Pallette[i].id == id) {
                        pallette = $scope.Pallette[i];
                    }
                }

                $scope.editPallette = pallette
                var modalInstance = $uibModal.open({
                    backdrop: 'static',
                    keyboard: false,
                    animation: true,
                    reloadOnSearch: false,
                    templateUrl: './views/palletteEditModal.html',
                    controller: 'palletteEditModalInstanceCtrl',
                    scope: $scope,
                    size: 'lg',
                    resolve: {

                    }
                });
                modalInstance.result.then(function() {
                    vm.dtInstance.reloadData();
                }, function() {});
            };
            $scope.reload = function() {
                $route.reload();
            }

        }
    ]);

angular.module('capApp')
    .controller('openBonDeLivraisonModalInstanceCtrl', function($rootScope, $scope, SweetAlert, $uibModalInstance, $uibModal, orderServiceAjax, customerServiceAjax, partServiceAjax, palletteServiceAjax, lookupServiceAjax, toasty, $filter, $localStorage) {
        $scope.downloadBl = function() {

            $.fn.dataTable.ext.errMode = 'none';
            var token = $localStorage.oauthToken;
            var msg = "";
            var hasBl = false;
            for (var i = 0; i < $scope.checkedPallets.length; i++) {
                var bl = $scope.checkedPallets[i].blNumber;
                var dest = $scope.checkedPallets[i].destination;
                if (bl !== null && bl !== undefined && bl !== "") {
                    hasBl = true;
                    msg = "Une des pallettes selectionée posséde une Bon de livraison";
                    break;
                }

            }
            if (hasBl == true) {
                SweetAlert.swal({
                    title: "Warrning",
                    text: msg,
                    type: "warning",
                    showCancelButton: false,
                    confirmButtonColor: "#4caf50",
                    confirmButtonText: $filter('translate')('shipping_js.OK'),
                    closeOnConfirm: true
                })

            } else if (hasBl == false) {

                orderServiceAjax.downloadBl($scope.checkedOrders, $scope.booksQuantities, $scope.palletteIds).then(function(data) {
                    var blob = new Blob([data], { type: "application/pdf" });
                    var objectUrl = URL.createObjectURL(blob);
                    var a = document.createElement('a');
                    a.href = objectUrl;
                    a.target = '_blank';
                    a.download = "BL" + '.pdf';
                    document.body.appendChild(a);
                    a.click();
                    window.open(objectUrl);
                    palletteServiceAjax.allPalletteNotActive().then(function(data) {
                        $scope.Pallettes = data;
                    });
                    $scope.reload();
                });


            }
            $scope.closeModal();
        }
        $scope.closeModal = function() {
            $uibModalInstance.dismiss();
        };
    });
angular.module('capApp')
    .controller('palletteEditModalInstanceCtrl', function($rootScope, $scope, $uibModalInstance, $uibModal, orderServiceAjax, customerServiceAjax, partServiceAjax, lookupServiceAjax, palletteServiceAjax, toasty, $filter) {
        $scope.closeModal = function() {
            $uibModalInstance.dismiss();
        };
        $scope.orderByPcb;
        $scope.editQtyPcbInPallette = function(pcb) {
            palletteServiceAjax.editQtyPalletteBook($scope.editPallette.id, pcb.packagePartId, pcb.newQty).then(function(data) {});
        }
        $scope.fetchOrderByPallette = function(packageBook) {
            palletteServiceAjax.orderByPcb(packageBook.packagePartId).then(function(data) {
                $scope.orderByPcb = data;
            });
        }
    });