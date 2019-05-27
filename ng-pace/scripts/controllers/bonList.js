'use strict';

/**
 * @ngdoc function
 * @name capApp.controller:RolesListCtrl
 * @description
 * # RolesListCtrl
 * Controller of the capApp
 */
angular.module('capApp')
    .controller('bonListCtrl', ['DTOptionsBuilder', 'DTColumnBuilder', '$scope','$route', '$uibModal', '$log', 'roleServiceAjax', 'SweetAlert', '$confirm', 'toasty', '$translatePartialLoader', '$translate', 'orderServiceAjax', 'blServiceAjax', 'palletteServiceAjax', '$localStorage','$rootScope',
        function (DTOptionsBuilder, DTColumnBuilder, $scope,$route, $uibModal, $log, roleServiceAjax, SweetAlert, $confirm, toasty, $translatePartialLoader, $translate, orderServiceAjax, blServiceAjax, palletteServiceAjax, $localStorage,$rootScope) {
            
            $translatePartialLoader.addPart('bonList');
            $translate.refresh();

            $.fn.dataTable.ext.errMode = 'none';
            var token = $localStorage.oauthToken;

            var vm = this;
            var monthNames = ["Jan ", $translate.instant('BONLIST_JS.FEB'), "Mar ", $translate.instant('BONLIST_JS.APR'), $translate.instant('BONLIST_JS.MAY'), $translate.instant('BONLIST_JS.JUN'), $translate.instant('BONLIST_JS.JUL'), $translate.instant('BONLIST_JS.AUG'), "Sep ", "Oct ", "Nov ", "Dec "];
            var blToId;
            $scope.newAdresse = "";
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
            }
            else if ($translate.use() == 'es') {
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

            vm.dtOptions = DTOptionsBuilder.fromSource($rootScope.API_BASE+'/bls?access_token=' + token)
                .withDOM('frtip')
                .withPaginationType('full_numbers')
                .withOption('responsive', true)
                //.withOption('dom', '<"top"i>rt<"bottom"flp><"clear">')
                .withDisplayLength(25)
                .withColumnFilter({

                    aoColumns: [

                        {
                            type: 'number'    //blNum                
                        }, {
                            type: 'text',   //destination
                            bRegex: true,
                            bSmart: true
                        }, {
                            type: 'date',   //creationDate
                            bRegex: true,
                            bSmart: true
                        }, {
                            type: 'number'   //qty
                        }, {
                            type: 'text',   //Status
                            bRegex: true,
                            bSmart: true
                        }]
                })
                .withLanguage($scope.dataTables);

            vm.dtColumns = [
                DTColumnBuilder.newColumn('num').withTitle($translate.instant('BONLIST_JS.BL')).withClass('text-center'),
                DTColumnBuilder.newColumn('destination').withTitle($translate.instant('BONLIST_JS.Adresse')).withOption('defaultContent', ' '),
                DTColumnBuilder.newColumn('creationDate').withTitle($translate.instant('BONLIST_JS.Date_Creation')).withOption('defaultContent', ' ').
                    renderWith(function (data) {
                        var res = "_";
                        if (data > 0) {
                            var date = new Date(data);
                            res = monthNames[date.getMonth()] + date.getDate();
                        }
                        return res;
                    }),
                DTColumnBuilder.newColumn('qty').withTitle($translate.instant('BONLIST_JS.Quantity')).withOption('defaultContent', ' '),
                DTColumnBuilder.newColumn('status').withTitle($translate.instant('BONLIST_JS.Status')).withOption('defaultContent', ' '),
                DTColumnBuilder.newColumn(null).withTitle(' ').notSortable().renderWith(function (data) {
                    if(data.status != 'DELIVERED'){
                        return "<div style='display:flex;'><button type='button'  uib-tooltip='Status' title='" + $translate.instant('BONLIST_JS.Status') + "' onclick=\"var $scope = angular.element(event.target).scope(); $scope.changeStatus('" + data.id + "'); $scope.$apply()\" class='btn bgm-orange-900	 waves-circle waves-effect'><i class='zmdi zmdi-refresh-alt'> </i></button>&nbsp;<button type='button'  uib-tooltip='Print' title='" + $translate.instant('BONLIST_JS.Print') + "' onclick=\"var $scope = angular.element(event.target).scope(); $scope.downloadBl('" + data.id + "'); $scope.$apply()\" class='btn bgm-orange-900	 waves-circle waves-effect'><i class='zmdi zmdi-print'></i></button>&nbsp;<button type='button' uib-tooltip='Edit' title='" + $translate.instant('BONLIST_JS.EDIT') + " ' onclick=\"var $scope = angular.element(event.target).scope(); $scope.openEditBonLivraisonModal('" + data.id + "'); $scope.$apply()\" class='btn bgm-orange-900 waves-circle waves-effect'><i class='zmdi zmdi-edit'></i></button></div>";
                    }
                    return "<div style='display:flex;'><button type='button'  uib-tooltip='Status' title='" + $translate.instant('BONLIST_JS.Status') + "' onclick=\"var $scope = angular.element(event.target).scope(); $scope.changeStatus('" + data.id + "'); $scope.$apply()\" class='btn bgm-orange-900	 waves-circle waves-effect'><i class='zmdi zmdi-refresh-alt'> </i></button>&nbsp;<button type='button'  uib-tooltip='Print' title='" + $translate.instant('BONLIST_JS.Print') + "' onclick=\"var $scope = angular.element(event.target).scope(); $scope.downloadBl('" + data.id + "'); $scope.$apply()\" class='btn bgm-orange-900	 waves-circle waves-effect'><i class='zmdi zmdi-print'></i></button>&nbsp;</div>";

                })
            ];


            $scope.openEditBonLivraisonModal = function (id) {
                var modalInstance = $uibModal.open({
                    backdrop: 'static',
                    keyboard: false,
                    animation: true,
                    templateUrl: './views/editBonLivraisonModal.html',
                    controller: 'editBonLivraisonModalInstanceCtrl',
                    scope: $scope,
                    size: 'lg',
                    resolve: {
                        partNum: function () {
                            return id;
                        },
                        disableFields: function () {
                            return false;
                        }
                    },

                });
                $scope.blToId = id
                blServiceAjax.getPallettes(id).then(function (data) {
                    $scope.pallettesBl = data;
                    $scope.newAdresse = data.destination;
                })
                modalInstance.result.then(function (result) {
                    vm.dtInstance.reloadData();
                }, function () { });
            };

            $scope.PcbByPallette = function (pallette) {
                palletteServiceAjax.qtyPcbByPallette(pallette.id).then(function (data) {
                    pallette.qtyPcbInPallette = data;
                });
            };
            $scope.bookByPallette = function (pallette) {
                palletteServiceAjax.qtyLivreByPallette(pallette.id).then(function (data) {
                    pallette.qtyBookInPallette = data;
                });
            }

            $scope.getOrderByPalltte = function (palletteId) {
                $scope.ordersByPallette = [];
                $scope.QuantitiesOrderByPallette = [];
                palletteServiceAjax.orderInfoByPallette(palletteId).then(function (data) {
                    var orders = data.orders;
                    for (var i = 0; i < orders.length; i++) {
                        var order = orders[i];
                        $scope.ordersByPallette.push(orders[i]);
                        $scope.QuantitiesOrderByPallette.push(data.books[i]);

                    }
                });
            }
            $scope.changeStatus = function (blId) {
                blServiceAjax.changeStatus(blId).then(function (data) {
                    $route.reload();
                });

            }
            $scope.downloadBl = function (blId) {
                orderServiceAjax.printBl(blId).then(function (data) {
                    var blob = new Blob([data], { type: "application/pdf" });
                    var objectUrl = URL.createObjectURL(blob);
                    var a = document.createElement('a');
                    a.href = objectUrl;
                    a.target = '_blank';
                    a.download = "BL" + '.pdf';
                    document.body.appendChild(a);
                    a.click();
                    window.open(objectUrl);
                });
            }
        }]);
angular.module('capApp').controller('editBonLivraisonModalInstanceCtrl',
    function ($scope, $route, $uibModalInstance, $uibModal, SweetAlert, partServiceAjax, $timeout, toasty, $translate, blServiceAjax, palletteServiceAjax, $filter) {


        $scope.openAddPalletBonLivraisonModal = function () {
            var modalInstance = $uibModal.open({
                backdrop: 'static',
                keyboard: false,
                animation: true,
                templateUrl: './views/addPalletBonLivraisonModal.html',
                controller: 'openAddPalletBonLivraisonModalModalInstanceCtrl',
                scope: $scope,
                size: 'lg',
                resolve: {

                },

            });

            modalInstance.result.then(function (result) {
                vm.dtInstance.reloadData();
            }, function () { });
        };
        $scope.closeModal = function () {
            $uibModalInstance.dismiss();
        };
        $scope.chekedPalletToRemove = [];
        $scope.palletteIdAdd = [];
        $scope.fillpalletToAdd = function () {
            $scope.chekedPalletToAdd = [];
            $scope.palletteIdAdd = [];
            angular.forEach($('input[name=addpallet]:checked'), function (value, key) {
                var pallet = angular.fromJson(value.id);
                $scope.chekedPalletToAdd.push(pallet);
                $scope.palletteIdAdd.push(pallet.id);
            });
        }
        $scope.edit = function () {
            $scope.chekedPalletToRemove = [];
            $scope.palletteId = [];
            angular.forEach($('input[name=palletRemove]:checked'), function (value, key) {
                var pallet = angular.fromJson(value.id);
                $scope.chekedPalletToRemove.push(pallet);
                $scope.palletteId.push(pallet.id);
            });

            blServiceAjax.edit($scope.blToId, $scope.palletteId, $scope.newAdresse, $scope.palletteIdAdd).then(function (data) {
                $route.reload();

            });
        }
    });


angular.module('capApp')
    .controller('openAddPalletBonLivraisonModalModalInstanceCtrl',
    function ($scope, $uibModalInstance, SweetAlert, partServiceAjax, $timeout, toasty, $translate, blServiceAjax, palletteServiceAjax, $filter) {

        blServiceAjax.fetchPaletsWithoutBl().then(function (data) { $scope.palletesWB = data });

        $scope.closeModal = function () {
            $uibModalInstance.dismiss();
        }
    });
