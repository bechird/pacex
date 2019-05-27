'use strict';
/**
 * @ngdoc function
 * @name capApp.controller:OrdersCtrl
 * @description
 * # OrdersCtrl
 * Controller of the capApp
 */

angular.module('capApp')
    .controller('ListOrderParentInstanceCtrl', function($rootScope, $scope, $uibModal, jobServiceAjax, lookupServiceAjax, ProductionOrderServiceAjax, notificationService, $translatePartialLoader, $translate, $localStorage) {

        $scope.formats = ['MM/dd/yyyy', 'MM-dd-yyyy', 'shortDate'];
        $scope.format = $scope.formats[0];

        $scope.calculateUnscheduledHours = function(day) {
            // $scope.unscheduled4CHours = 0;
            // $scope.unscheduled1CHours = 0;
            jobServiceAjax.calculatePressHours("4C", day, "U").then(function(data) {
                $scope.unscheduled4CHours = data;
            });
            jobServiceAjax.calculatePressHours("1C", day, "U").then(function(data) {
                $scope.unscheduled1CHours = data;
            });
        };

        $scope.calculateScheduledHours = function(day) {
            // $scope.scheduled4CHours = 0;
            // $scope.scheduled1CHours = 0;
            jobServiceAjax.calculatePressHours("4C", day, "S").then(function(data) {
                $scope.scheduled4CHours = data;
            });
            jobServiceAjax.calculatePressHours("1C", day, "S").then(function(data) {
                $scope.scheduled1CHours = data;
            });
        };

        $scope.openOrderPartDsModal = function(orderId, ordpart, source) {
            var modalInstance = $uibModal.open({
                backdrop: 'static',
                keyboard: false,
                animation: true,
                templateUrl: './views/orderPartDs.html',
                controller: 'OrderPartDsModalInstanceCtrl',
                scope: $scope,
                size: 'xl',
                resolve: {
                    orderId: function() {
                        return orderId;
                    },
                    ordpart: function() {
                        return ordpart;
                    },
                    source: function() {
                        return source;
                    }
                }
            });
            modalInstance.result.then(function() {
                vm.dtInstance.reloadData();
            }, function() {});
        };

    });

angular.module('capApp')
    .controller('OrdersAcceptanceListCtrl', ['DTOptionsBuilder', 'DTColumnBuilder', 'DTColumnDefBuilder', '$rootScope', '$scope', '$controller', '$log', '$route', 'machineServiceAjax', 'orderServiceAjax', 'customerServiceAjax',
        'partServiceAjax', 'jobServiceAjax', 'lookupServiceAjax', 'Upload', '$timeout', '$confirm', '$location', '$uibModal', 'SweetAlert', '$filter', 'toasty', '$translatePartialLoader',
        '$translate', '$localStorage', 'notificationService', '$http', 'SSE_CONSTANTS', 'usSpinnerService',
        function(DTOptionsBuilder, DTColumnBuilder, DTColumnDefBuilder, $rootScope, $scope, $controller, $log, $route, machineServiceAjax, orderServiceAjax, customerServiceAjax, partServiceAjax, jobServiceAjax,
            lookupServiceAjax, Upload, $timeout, $confirm, $location, $uibModal, SweetAlert, $filter, toasty, $translatePartialLoader, $translate, $localStorage, notificationService, $http, SSE_CONSTANTS, usSpinnerService) {

            $controller('ListOrderParentInstanceCtrl', { $scope: $scope });

            $translatePartialLoader.addPart('orders');
            $translate.refresh();
            $.fn.dataTable.ext.errMode = 'none';
            var token = $localStorage.oauthToken;
            $scope.token = token;
            var vm = this;
            $scope.successMsg = false;
            $scope.errorMsg = false;
            $scope.warningMsg = false;
            vm.dtInstanceAcceptance = null;
            var monthNames = ["Jan ", $translate.instant('ORDERS_JS.FEB'), "Mar ", $translate.instant('ORDERS_JS.APR'), $translate.instant('ORDERS_JS.MAY'), $translate.instant('ORDERS_JS.JUN'), $translate.instant('ORDERS_JS.JUL'), $translate.instant('ORDERS_JS.AUG'), "Sep ", "Oct ", "Nov ", "Dec "];
            var todaysDate = new Date();
            var tomorrosDate = new Date();
            tomorrosDate.setDate(tomorrosDate.getDate() + 1);
            var t2 = new Date();
            t2.setDate(t2.getDate() + 2);
            var t3 = new Date();
            t3.setDate(t3.getDate() + 3);
            var t4 = new Date();
            t4.setDate(t4.getDate() + 4);
            var t5 = new Date();
            t5.setDate(t5.getDate() + 5);
            var t6 = new Date();
            t6.setDate(t6.getDate() + 6);
            $scope.cLabels = [$filter('date')(todaysDate, 'EEEE'), $filter('date')(tomorrosDate, 'EEEE'),
                $filter('date')(t2, 'EEEE'), $filter('date')(t3, 'EEEE'),
                $filter('date')(t4, 'EEEE'), $filter('date')(t5, 'EEEE'), $filter('date')(t6, 'EEEE')
            ];
            $scope.cSeries = [$translate.instant('ORDERS_JS.SCHED'), $translate.instant('ORDERS_JS.UNSCHED'), $translate.instant('ORDERS_JS.SELECTED'), $translate.instant('ORDERS_JS.WORK_HOURS'), $translate.instant('ORDERS_JS.DAYLIMIT')];

            /*$scope.cSeries = ['Non Work Hours', 'Free Hours', 'Due This Day', 'Late Work', 'Due After Today'];*/
            $scope.daysNeeded = '6';
            $scope.cumulFlag = 'false';
            $scope.acceptButtonClicked = false;
            $rootScope.selectedOrdersToBeAccepted = [];

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

            $scope.getWorkStatusHours = function(daysNeeded, cumulFlag) {
                $scope.data4C = [];
                $scope.data1C = [];
                $scope.data1CReady = false;
                $scope.data4CReady = false;
                machineServiceAjax.getMachineWorkingCount("PRESS", "4C").then(function(dataMachineCount) {
                    $scope.machineWorkingCount4C = dataMachineCount;
                    jobServiceAjax.getPressStationHours("4C", $scope.daysNeeded, $scope.cumulFlag, "S").then(function(data) {
                        $scope.data4C.push(divideList(data, $scope.machineWorkingCount4C));
                        jobServiceAjax.getPressStationHours("4C", $scope.daysNeeded, $scope.cumulFlag, "U").then(function(data2) {
                            $scope.data4C.push(divideList(data2, $scope.machineWorkingCount4C));
                            $scope.data4C.push([0, 0, 0, 0, 0, 0, 0]);
                            jobServiceAjax.getCapacityHours("4C", $scope.daysNeeded, $scope.cumulFlag).then(function(data22) {
                                $scope.data4C.push(divideList(data22, $scope.machineWorkingCount4C));
                                $scope.data4C.push([24 - (data22[0] / $scope.machineWorkingCount4C),
                                    24 - (data22[1] / $scope.machineWorkingCount4C),
                                    24 - (data22[2] / $scope.machineWorkingCount4C),
                                    24 - (data22[3] / $scope.machineWorkingCount4C),
                                    24 - (data22[4] / $scope.machineWorkingCount4C),
                                    24 - (data22[5] / $scope.machineWorkingCount4C),
                                    24 - (data22[6] / $scope.machineWorkingCount4C)
                                ]);
                                $scope.data4CReady = true;
                            });
                        });
                    });
                });
                machineServiceAjax.getMachineWorkingCount("PRESS", "1C").then(function(dataMachineCount2) {
                    $scope.machineWorkingCount1C = dataMachineCount2;
                    jobServiceAjax.getPressStationHours("1C", $scope.daysNeeded, $scope.cumulFlag, "S").then(function(data3) {
                        $scope.data1C.push(divideList(data3, $scope.machineWorkingCount1C));
                        jobServiceAjax.getPressStationHours("1C", $scope.daysNeeded, $scope.cumulFlag, "U").then(function(data4) {
                            $scope.data1C.push(divideList(data4, $scope.machineWorkingCount1C));
                            $scope.data1C.push([0, 0, 0, 0, 0, 0, 0]);
                            jobServiceAjax.getCapacityHours("1C", $scope.daysNeeded, $scope.cumulFlag).then(function(data44) {
                                $scope.data1C.push(divideList(data44, $scope.machineWorkingCount1C));
                                $scope.data1C.push([24 - (data44[0] / $scope.machineWorkingCount1C),
                                    24 - (data44[1] / $scope.machineWorkingCount1C),
                                    24 - (data44[2] / $scope.machineWorkingCount1C),
                                    24 - (data44[3] / $scope.machineWorkingCount1C),
                                    24 - (data44[4] / $scope.machineWorkingCount1C),
                                    24 - (data44[5] / $scope.machineWorkingCount1C),
                                    24 - (data44[6] / $scope.machineWorkingCount1C)
                                ]);
                                $scope.data1CReady = true;
                            });
                        });
                    });
                });
            };
            var divideList = function(theList, dataMachineCount) {
                var newResult = [];
                for (var i = 0; i < theList.length; i++) {
                    newResult.push(theList[i] / dataMachineCount);
                }
                return newResult;
            };

            $scope.getWorkStatusHours($scope.daysNeeded, $scope.cumulFlag);

            $scope.datasetOverride = [{
                    label: $translate.instant('ORDERS_JS.SCHED'),
                    backgroundColor: 'transparent',
                    borderColor: 'blue',
                    borderWidth: 2,
                    type: 'bar'
                },
                {
                    label: $translate.instant('ORDERS_JS.UNSCHED'),
                    backgroundColor: 'transparent',
                    borderColor: 'green',
                    borderWidth: 2,
                    type: 'bar'
                },
                {
                    label: $translate.instant('ORDERS_JS.SELECTED'),
                    backgroundColor: 'transparent',
                    borderColor: 'grey',
                    borderWidth: 2,
                    type: 'bar'
                },
                {
                    label: $translate.instant('ORDERS_JS.WORK_HOURS'),
                    backgroundColor: 'transparent',
                    borderWidth: 3,
                    type: 'line'
                },
                {
                    label: $translate.instant('ORDERS_JS.DAYLIMIT'),
                    backgroundColor: '',
                    borderWidth: 3,
                    type: 'line'
                }
            ];

            $scope.cOptions = {
                responsive: true,
                tooltips: {
                    callbacks: {
                        label: function(valueObj) {
                            return $scope.datasetOverride[valueObj.datasetIndex].label + ' ' + formatNumber(valueObj.yLabel, 2, '.', ',');
                        }
                    }
                },
                title: {
                    display: true,

                },
                scales: {
                    xAxes: [{
                        ticks: {
                            beginAtZero: true
                        },
                        stacked: false
                    }],
                    yAxes: [{
                        ticks: {
                            beginAtZero: true
                        },
                        stacked: true
                    }]
                }
            };

            function formatNumber(number, decimalsLength, decimalSeparator, thousandSeparator) {
                var n = number,
                    decimalsLength = isNaN(decimalsLength = Math.abs(decimalsLength)) ? 2 : decimalsLength,
                    decimalSeparator = decimalSeparator == undefined ? "." : decimalSeparator,
                    thousandSeparator = thousandSeparator == undefined ? "," : thousandSeparator,
                    sign = n < 0 ? "-" : "",
                    i = parseInt(n = Math.abs(+n || 0).toFixed(decimalsLength)) + "",
                    j = (j = i.length) > 3 ? j % 3 : 0;

                return sign +
                    (j ? i.substr(0, j) + thousandSeparator : "") +
                    i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + thousandSeparator) +
                    (decimalsLength ? decimalSeparator + Math.abs(n - i).toFixed(decimalsLength).slice(2) : "");
            }

            vm.dtOptionsAcceptance = DTOptionsBuilder.fromSource($rootScope.API_BASE + '/orders/acceptance?access_token=' + token)
                .withDOM('frtip')
                .withPaginationType('simple_numbers')
                // .withOption('responsive', true)
                .withOption('select', { style: 'multi', selector: 'tr input[type="checkbox"]' })
                .withOption('order', [])
                .withDisplayLength(25)
                .withColumnFilter({
                    aoColumns: [{
                        type: 'number'
                    }, {
                        type: 'text',
                        bRegex: true,
                        bSmart: true
                    }, {
                        type: 'text',
                        bRegex: true,
                        bSmart: true
                    }, {
                        type: 'text',
                        bRegex: true,
                        bSmart: true
                    }, {
                        type: 'text',
                        bRegex: true,
                        bSmart: true
                    }, {
                        type: 'date',
                        bRegex: true,
                        bSmart: true
                    }, {
                        type: 'date',
                        bRegex: true,
                        bSmart: true
                    }, {
                        type: 'number'
                    }, {
                        type: 'text',
                        bRegex: true,
                        bSmart: true
                    }, ]
                })
                .withLanguage($scope.dataTables);

            vm.dtColumnsAcceptance = [

                DTColumnBuilder.newColumn('orderId').withTitle($translate.instant('ORDERS_JS.ORDER')).renderWith(function(data) {
                    return "<button title='" + $translate.instant('ORDERS_JS.QTY') + "' type='button' class='btn bgm-btn-blue-500 waves-effect' onclick=\"var $scope = angular.element(event.target).scope(); $scope.openEditOrderModal('" + data + "'); $scope.$apply()\">" + data + "</button>";
                }),
                DTColumnBuilder.newColumn('orderNum').withTitle('PO#'),
                DTColumnBuilder.newColumn('allIsbns').withTitle('ISBN').renderWith(function(data) {
                    return "<div class='ellipses' title='" + data + "'  uib-tooltip='" + data + "' tooltip-placement='left'>" + data + "</div>";
                }),
                DTColumnBuilder.newColumn(null).withTitle('C').
                renderWith(function(data) {
                    var res = data.orderPart.part.colors;
                    if (data.spotVarnish) {
                        res = res + '/<font title = "Spot Varnish" color="red"><b>SV<b/></font>';
                    }
                    return res;
                }),
                DTColumnBuilder.newColumn('source').withTitle($translate.instant('ORDERS_JS.Source')).withOption('defaultContent', ' '),
                DTColumnBuilder.newColumn('recievedDate').withTitle($translate.instant('ORDERS_JS.RECEIVED')).
                renderWith(function(data) {
                    var res = "_";
                    if (data > 0) {
                        var date = new Date(data);
                        res = monthNames[date.getMonth()] + date.getDate() + ", " + date.getHours() + ":" + date.getMinutes();
                    }
                    return res;
                }),
                DTColumnBuilder.newColumn('dueDate').withTitle($translate.instant('ORDERS_JS.Due')).
                renderWith(function(data) {
                    var res = "_";
                    if (data > 0) {
                        var date = new Date(data);
                        res = monthNames[date.getMonth()] + date.getDate();
                    }
                    return res;
                }),
                DTColumnBuilder.newColumn('orderPart.printingHours').withTitle($translate.instant('ORDERS_JS.HOURS')).withOption('defaultContent', ' ').
                renderWith(function(data) {
                    return $filter('number')(data, 2);
                }),
                DTColumnBuilder.newColumn(null).withTitle($translate.instant('ORDERS_JS.PRIORITY')).notSortable().renderWith(function(data) {
                    var prname = 'priorityDd' + data.orderId;
                    var prInput = $('#' + prname);
                    var sel = "<option value='HIGH**'>High**</option><option value='HIGH*'>High*</option><option value='HIGH'>High</option><option value='NORMAL'>Normal</option>";
                    if (data.priority == 'HIGH**') {
                        sel = "<option value='HIGH**' selected>High**</option><option value='HIGH*'>High*</option><option value='HIGH'>High</option><option value='NORMAL'>Normal</option>"
                    } else if (data.priority == 'HIGH*') {
                        sel = "<option value='HIGH**' >High**</option><option value='HIGH*' selected>High*</option><option value='HIGH'>High</option><option value='NORMAL'>Normal</option>"
                    } else if (data.priority == 'HIGH') {
                        sel = "<option value='HIGH**' >High**</option><option value='HIGH*' >High*</option><option value='HIGH' selected>High</option><option value='NORMAL'>Normal</option>"
                    } else if (data.priority == 'NORMAL') {
                        sel = "<option value='HIGH**' >High**</option><option value='HIGH*' >High*</option><option value='HIGH' >High</option><option value='NORMAL' selected>Normal</option>"
                    }
                    return "<select onchange=\"var $scope = angular.element(event.target).scope(); $scope.updateOrderPriority('" + data.orderId + "'); $scope.$apply()\" class='form-control' name='" + prname + "' id='" + prname + "' >" + sel + "</select>"; //,'"+ prInput.val()+"'
                }),

                DTColumnBuilder.newColumn('orderId').withTitle(' ').notSortable().renderWith(function(data) {
                    var ddname = 'actionDd' + data;
                    return "<select onchange=\"var $scope = angular.element(event.target).scope(); $scope.updateOrderStatus('" + data + "'); $scope.$apply()\" class='form-control' name='" + ddname + "' id='" + ddname + "' style='width: 99%'><option value=''>Select</option><option value='MODIFY'>Modify</option><option value='REJECTED'>Reject</option></select>";
                }),

                DTColumnBuilder.newColumn('orderId').withTitle(' ').notSortable().renderWith(function(data) {
                    var ckbname = 'checkboxForAcceptance' + data;
                    return "<label class='checkbox checkbox-inline m-r-20'><input id='" + ckbname + "' name='" + ckbname + "' type='checkbox' onclick=\"var $scope = angular.element(event.target).scope(); $scope.toggleAcceptButton('" + data + "'); $scope.$apply()\" '><i class='input-helper'></i></label>";
                })
            ];

            $scope.openEditOrderModal = function(id) {
                var modalInstance = $uibModal.open({
                    backdrop: 'static',
                    keyboard: false,
                    animation: true,
                    templateUrl: './views/editOrderModalContent.html',
                    controller: 'EditOrderModalInstanceCtrl',
                    scope: $scope,
                    size: 'xl',
                    resolve: {
                        orderId: function() {
                            return id;
                        }
                    }
                });

                modalInstance.result.then(function(selectedItem) {
                    vm.dtInstanceAcceptance.reloadData();
                }, function() {});

            };

            $scope.updateOrderStatus = function(orderId) {
                $scope.actionSelected = $('#actionDd' + orderId).val();
                if ($scope.actionSelected == '') {

                } else if ($scope.actionSelected == 'MODIFY') {
                    $scope.openEditOrderModal(orderId);
                } else {
                    orderServiceAjax.getOrderById(orderId).then(function(data) {
                        $scope.order = data;
                        $scope.order.status = $scope.actionSelected;
                        orderServiceAjax.updateOrder($scope.order)
                            .then(function() {
                                //$scope.successMsg = true;
                                $scope.errorMsg = false;
                                $scope.warningMsg = false;
                                $scope.alertMsg = "$translate.instant('ORDERS_JS.ORDER_SUCC_UPDATED')";
                                toasty.success({
                                    title: $translate.instant('ORDERS_JS.ORDER_UPDATE'),
                                    msg: $translate.instant('ORDERS_JS.ORDER_SUCC_UPDATED'),
                                    showClose: true,
                                    clickToClose: true,
                                    timeout: 10000,
                                    sound: false,
                                    html: false,
                                    shake: false,
                                    theme: "bootstrap"
                                });
                                vm.dtInstanceAcceptance.reloadData();
                            });
                    });
                }
            };

            $scope.updateOrderPriority = function(orderId) {
                orderServiceAjax.getOrderById(orderId).then(function(data) {
                    $scope.order = data;
                    $scope.order.priority = $('#priorityDd' + orderId).val();
                    orderServiceAjax.updateOrder($scope.order)
                        .then(function() {
                            //$scope.successMsg = true;
                            $scope.errorMsg = false;
                            $scope.warningMsg = false;
                            $scope.alertMsg = $translate.instant('ORDERS_JS.ORDER') + $scope.order.orderId + $translate.instant('ORDERS_JS.PRIORITY_SUCC_UPDATED');
                            toasty.success({
                                title: $translate.instant('ORDERS_JS.ORDER_UPDATE'),
                                msg: $translate.instant('ORDERS_JS.ORDER') + $scope.order.orderId + $translate.instant('ORDERS_JS.PRIORITY_SUCC_UPDATED'),
                                showClose: true,
                                clickToClose: true,
                                timeout: 10000,
                                sound: false,
                                html: false,
                                shake: false,
                                theme: "bootstrap"
                            });
                            vm.dtInstanceAcceptance.reloadData();
                        });
                });
            };

            $scope.selectedOrdersForAcceptance = [];
            $scope.selected4CHours = 0;
            $scope.selected1CHours = 0;
            $scope.toggleSelectAll = function(checked) {
                $scope.selected4CHours = 0;
                $scope.selected1CHours = 0;
                var table = $('#ordersTable').DataTable();
                if (checked) {
                    table.rows().select();
                    $(':checkbox').prop('checked', true);
                    var dt = table.rows().data();
                    for (var i = 0; i < table.rows().count(); i++) {
                        /* if(dt[i].orderPart.part.colors == '4C'){
				  $scope.selected4CHours = $scope.selected4CHours + dt[i].orderPart.printingHours;
			  }else{
				  $scope.selected1CHours = $scope.selected1CHours + dt[i].orderPart.printingHours;
			  }*/
                        $scope.toggleAcceptButton(dt[i].orderId);
                    }
                } else {
                    table.rows().deselect();
                    $(':checkbox').prop('checked', false);
                    var dt = table.rows().data();
                    for (var i = 0; i < table.rows().count(); i++) {
                        $scope.toggleAcceptButton(dt[i].orderId);
                    }
                }
            };

            $scope.toggleAcceptButton = function(orderId) {
                /*var chbid = '#checkboxForAcceptance'+orderId;
	  var chb = $(chbid);
	  if(chb != null){
		  if (chb.is(':checked')) {
	        	 $('#acceptButton').removeAttr('disabled');
	        }
	  }*/
                var inputs = [];
                var makeItDisabled = true;
                var table = $('#ordersTable').DataTable();
                var dt = table.rows().data();
                $scope.selected4CHours = 0;
                $scope.selected1CHours = 0;

                $('input[type="checkbox"]').each(function() {
                    inputs.push('#' + $(this).attr('id'));
                });

                //re-initialize the chart data for the 'selected' bars
                /*for(var i = 0; i < 4; i++){
		  $scope.data4C[1][i] = $scope.data4C[0][i];
		  $scope.data1C[1][i] = $scope.data1C[0][i];
	  }
	  for(var i = 0; i < 7; i++){
		  $scope.data4C[2][i] = 0;
		  $scope.data1C[2][i] = 0;
	  }*/

                // $scope.getWorkStatusHours('3');

                var todaysDate = new Date();
                var todaysDatePlusOne = new Date();
                todaysDatePlusOne.setDate(todaysDate.getDate() + 1);
                var todaysDatePlusTwo = new Date();
                todaysDatePlusTwo.setDate(todaysDatePlusOne.getDate() + 1);
                var todaysDatePlusThree = new Date();
                todaysDatePlusThree.setDate(todaysDatePlusTwo.getDate() + 1);
                var todaysDatePlusFour = new Date();
                todaysDatePlusFour.setDate(todaysDatePlusThree.getDate() + 1);
                var todaysDatePlusFive = new Date();
                todaysDatePlusFive.setDate(todaysDatePlusFour.getDate() + 1);
                var todaysDatePlusSix = new Date();
                todaysDatePlusSix.setDate(todaysDatePlusFive.getDate() + 1);


                $scope.alertMsg = $translate.instant('ORDERS_JS.SELECTED_ORDERS');
                /*toasty.info({
          title: 'Order Acceptance',
          msg: 'The following orders are selected: ',
          showClose: true,
          clickToClose: true,
          timeout: 10000,
          sound: false,
          html: false,
          shake: false,
          theme: "bootstrap"
      });*/
                $scope.warningMsg = true;
                $scope.successMsg = false;
                $scope.errorMsg = false;
                for (var i = 0; i < inputs.length; i++) {
                    if ($(inputs[i]).is(':checked') && inputs[i] != '#checkboxAll') {
                        $('#acceptButton').removeAttr('disabled');
                        $scope.alertMsg = $scope.alertMsg + inputs[i].substring(22) + ', ';
                        makeItDisabled = false;
                        for (var j = 0; j < table.rows().count(); j++) {
                            if (dt[j].orderId == inputs[i].substring(22)) {
                                var theDate = new Date(dt[j].dueDate);
                                if ($scope.data1CReady && $scope.data4CReady) {
                                    if (dt[j].orderPart.part.colors == '4C') {
                                        $scope.selected4CHours = $scope.selected4CHours + dt[j].orderPart.printingHours;
                                        //Update the chart bars for selected hours...
                                        if (theDate.getDate() == todaysDate.getDate() && theDate.getMonth() == todaysDate.getMonth() && theDate.getFullYear() == todaysDate.getFullYear()) {
                                            $scope.data4C[2][0] += dt[j].orderPart.printingHours / $scope.machineWorkingCount4C;
                                        } else if (theDate.getDate() == todaysDatePlusOne.getDate() && theDate.getMonth() == todaysDatePlusOne.getMonth() && theDate.getFullYear() == todaysDatePlusOne.getFullYear()) {
                                            $scope.data4C[2][1] += dt[j].orderPart.printingHours / $scope.machineWorkingCount4C;
                                        } else if (theDate.getDate() == todaysDatePlusTwo.getDate() && theDate.getMonth() == todaysDatePlusTwo.getMonth() && theDate.getFullYear() == todaysDatePlusTwo.getFullYear()) {
                                            $scope.data4C[2][2] += dt[j].orderPart.printingHours / $scope.machineWorkingCount4C;
                                        } else if (theDate.getDate() == todaysDatePlusThree.getDate() && theDate.getMonth() == todaysDatePlusThree.getMonth() && theDate.getFullYear() == todaysDatePlusThree.getFullYear()) {
                                            $scope.data4C[2][3] += dt[j].orderPart.printingHours / $scope.machineWorkingCount4C;
                                        } else if (theDate.getDate() == todaysDatePlusFour.getDate() && theDate.getMonth() == todaysDatePlusFour.getMonth() && theDate.getFullYear() == todaysDatePlusFour.getFullYear()) {
                                            $scope.data4C[2][4] += dt[j].orderPart.printingHours / $scope.machineWorkingCount4C;
                                        } else if (theDate.getDate() == todaysDatePlusFive.getDate() && theDate.getMonth() == todaysDatePlusFive.getMonth() && theDate.getFullYear() == todaysDatePlusFive.getFullYear()) {
                                            $scope.data4C[2][5] += dt[j].orderPart.printingHours / $scope.machineWorkingCount4C;
                                        } else if (theDate.getDate() == todaysDatePlusSix.getDate() && theDate.getMonth() == todaysDatePlusSix.getMonth() && theDate.getFullYear() == todaysDatePlusSix.getFullYear()) {
                                            $scope.data4C[2][6] += dt[j].orderPart.printingHours / $scope.machineWorkingCount4C;
                                        } else if (theDate.getFullYear() < todaysDate.getFullYear() ||
                                            (theDate.getFullYear() == todaysDate.getFullYear() && theDate.getMonth() < todaysDate.getMonth()) ||
                                            (theDate.getFullYear() == todaysDate.getFullYear() && theDate.getMonth() == todaysDate.getMonth() && theDate.getDate() < todaysDate.getDate())) {
                                            $scope.data4C[2][0] += dt[j].orderPart.printingHours / $scope.machineWorkingCount4C;
                                        }
                                    } else {
                                        $scope.selected1CHours = $scope.selected1CHours + dt[j].orderPart.printingHours;
                                        //Update the chart bars for selected hours...
                                        if (theDate.getDate() == todaysDate.getDate() && theDate.getMonth() == todaysDate.getMonth() && theDate.getFullYear() == todaysDate.getFullYear()) {
                                            $scope.data1C[2][0] += dt[j].orderPart.printingHours / $scope.machineWorkingCount1C;
                                        } else if (theDate.getDate() == todaysDatePlusOne.getDate() && theDate.getMonth() == todaysDatePlusOne.getMonth() && theDate.getFullYear() == todaysDatePlusOne.getFullYear()) {
                                            $scope.data1C[2][1] += dt[j].orderPart.printingHours / $scope.machineWorkingCount1C;
                                        } else if (theDate.getDate() == todaysDatePlusTwo.getDate() && theDate.getMonth() == todaysDatePlusTwo.getMonth() && theDate.getFullYear() == todaysDatePlusTwo.getFullYear()) {
                                            $scope.data1C[2][2] += dt[j].orderPart.printingHours / $scope.machineWorkingCount1C;
                                        } else if (theDate.getDate() == todaysDatePlusThree.getDate() && theDate.getMonth() == todaysDatePlusThree.getMonth() && theDate.getFullYear() == todaysDatePlusThree.getFullYear()) {
                                            $scope.data1C[2][3] += dt[j].orderPart.printingHours / $scope.machineWorkingCount1C;
                                        } else if (theDate.getDate() == todaysDatePlusFour.getDate() && theDate.getMonth() == todaysDatePlusFour.getMonth() && theDate.getFullYear() == todaysDatePlusFour.getFullYear()) {
                                            $scope.data1C[2][4] += dt[j].orderPart.printingHours / $scope.machineWorkingCount1C;
                                        } else if (theDate.getDate() == todaysDatePlusFive.getDate() && theDate.getMonth() == todaysDatePlusFive.getMonth() && theDate.getFullYear() == todaysDatePlusFive.getFullYear()) {
                                            $scope.data1C[2][5] += dt[j].orderPart.printingHours / $scope.machineWorkingCount1C;
                                        } else if (theDate.getDate() == todaysDatePlusSix.getDate() && theDate.getMonth() == todaysDatePlusSix.getMonth() && theDate.getFullYear() == todaysDatePlusSix.getFullYear()) {
                                            $scope.data1C[2][6] += dt[j].orderPart.printingHours / $scope.machineWorkingCount1C;
                                        } else if (theDate.getFullYear() < todaysDate.getFullYear() ||
                                            (theDate.getFullYear() == todaysDate.getFullYear() && theDate.getMonth() < todaysDate.getMonth()) ||
                                            (theDate.getFullYear() == todaysDate.getFullYear() && theDate.getMonth() == todaysDate.getMonth() && theDate.getDate() < todaysDate.getDate())) {
                                            $scope.data1C[2][0] += dt[j].orderPart.printingHours / $scope.machineWorkingCount1C;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (makeItDisabled) {
                    $('#acceptButton').attr('disabled', 'disabled');
                    $scope.warningMsg = false;
                    $scope.alertMsg = '';
                }

            };

            $scope.calculateUnscheduledHours("A");

            $scope.calculateScheduledHours("A");

            $scope.acceptOrders = function() {
                //$('#acceptButton').attr('disabled', 'disabled');disable the button so user doesn't click it several times
                usSpinnerService.spin('spinner-1');
                $('#pacex_spinner').addClass("overlay-load");

                $scope.acceptButtonClicked = true;
                var table = $('#ordersTable').DataTable();
                var dt = table.rows({ selected: true }).data();
                $scope.selectedOrdersForAcceptance = [];
                for (var i = 0; i < table.rows({ selected: true }).count(); i++) {
                    $scope.selectedOrdersForAcceptance.push(dt[i].orderId);
                }
                if ($rootScope.selectedOrdersToBeAccepted != $scope.selectedOrdersForAcceptance &&
                    !$scope.arraysIdentical($rootScope.selectedOrdersToBeAccepted, $scope.selectedOrdersForAcceptance)) {
                    $rootScope.selectedOrdersToBeAccepted = $scope.selectedOrdersForAcceptance;
                    orderServiceAjax.acceptOrders($scope.selectedOrdersForAcceptance).then(function() {
                        //$route.reload().
                        $scope.acceptButtonClicked = false;
                        $scope.successMsg = true;
                        $scope.errorMsg = false;
                        $scope.warningMsg = false;
                        /*toasty.success({
                            title: 'Order Acceptance',
                            msg: "The following orders have successfully been accepted!: " + $scope.selectedOrdersForAcceptance,
                            showClose: true,
                            clickToClose: true,
                            timeout: 10000,
                            sound: false,
                            html: false,
                            shake: false,
                            theme: "bootstrap"
                        });*/

                        //$translate.instant('ORDERS_JS.ACCEPTED_ORDERS') 
                        $scope.alertMsg = $translate.instant('ORDERS_JS.ACCEPTED_ORDERS') + $scope.selectedOrdersForAcceptance;
                        $scope.selectedOrdersForAcceptance = [];
                        vm.dtInstanceAcceptance.reloadData();
                        $('#checkboxAll').prop('checked', false);
                        //$scope.getWorkStatusHours('3','false');
                        $scope.getWorkStatusHours($scope.daysNeeded, $scope.cumulFlag);
                        $scope.calculateUnscheduledHours("A");
                        $scope.calculateScheduledHours("A");

                        usSpinnerService.stop('spinner-1');
                        $('#pacex_spinner').removeClass("overlay-load");
                    });
                } else {
                    usSpinnerService.stop('spinner-1');
                    $('#pacex_spinner').removeClass("overlay-load");
                }
            };

            $scope.arraysIdentical = function(a, b) {
                var i = a.length;
                if (i != b.length) return false;
                while (i--) {
                    if (a[i] !== b[i]) return false;
                }
                return true;
            };

            $(document).ready(function() {
                $('input[type="checkbox"]').click(function() {
                    if ($(this).attr('id') != 'checkboxAll') {
                        if ($(this).is(':checked')) {
                            $('#acceptButton').removeAttr('disabled');
                        } else {
                            $('#acceptButton').attr('disabled', 'disabled');
                        }
                    }
                });
            });

            //5 seconds delay
            $timeout(function() {
                usSpinnerService.stop('spinner-1');
                $('#pacex_spinner').removeClass("overlay-load");
            }, 5000);

        }
    ]);

angular.module('capApp')
    .controller('OrdersListCtrl', ['DTOptionsBuilder', 'DTColumnBuilder', 'DTColumnDefBuilder', '$rootScope', '$scope', '$controller', '$log', '$route', 'machineServiceAjax', 'orderServiceAjax', 'customerServiceAjax',
        'partServiceAjax', 'jobServiceAjax', 'ProductionOrderServiceAjax', 'lookupServiceAjax', 'Upload', '$timeout', '$confirm', '$location', '$uibModal', 'SweetAlert', '$filter', 'toasty', '$translatePartialLoader',
        '$translate', '$localStorage', 'notificationService', '$http', 'SSE_CONSTANTS',
        function(DTOptionsBuilder, DTColumnBuilder, DTColumnDefBuilder, $rootScope, $scope, $controller, $log, $route, machineServiceAjax, orderServiceAjax, customerServiceAjax, partServiceAjax, jobServiceAjax, ProductionOrderServiceAjax, lookupServiceAjax, Upload, $timeout, $confirm, $location, $uibModal, SweetAlert, $filter, toasty, $translatePartialLoader, $translate, $localStorage, notificationService, $http, SSE_CONSTANTS) {

            $controller('ListOrderParentInstanceCtrl', { $scope: $scope });

            $translatePartialLoader.addPart('orders');
            $translate.refresh();
            $.fn.dataTable.ext.errMode = 'none';
            var token = $localStorage.oauthToken;

            //sse management 
            notificationService.subscribeToApp();
            $scope.$on('$destroy', function() {
                $log.log("leaving OrdersListCtrl controller, unsubscribe from sse");
                notificationService.getPubSub().unsubscribe(SSE_CONSTANTS.appEventsTopic);
                notificationService.unsubscribeFromApp();
            });
            //*****************************************************************************

            $scope.token = token;
            $scope.successMsg = false;
            $scope.errorMsg = false;
            $scope.warningMsg = false;
            var monthNames = ["Jan ", $translate.instant('ORDERS_JS.FEB'), "Mar ", $translate.instant('ORDERS_JS.APR'), $translate.instant('ORDERS_JS.MAY'), $translate.instant('ORDERS_JS.JUN'), $translate.instant('ORDERS_JS.JUL'), $translate.instant('ORDERS_JS.AUG'), "Sep ", "Oct ", "Nov ", "Dec "];

            var vm = this;
            vm.dtInstance = null;
            $scope.progress = 'PENDING';
            $scope.completeOrder = function(id) {
                SweetAlert.swal({
                        title: $translate.instant('ORDERS_JS.Complete'),
                        text: $translate.instant('ORDERS_JS.sure_complete_order'),
                        type: "warning",
                        showCancelButton: true,
                        confirmButtonColor: "#4caf50",
                        confirmButtonText: $translate.instant('ORDERS_JS.YES'),
                        cancelButtonText: $translate.instant('ORDERS_JS.NO'),
                        closeOnConfirm: true,
                        closeOnCancel: true
                    },
                    function(isConfirm) {
                        if (isConfirm) {
                            orderServiceAjax.completeStatus(id).then(function(data) {
                                $scope.orders = $filter('filter')($scope.orders, (item) => {
                                    return item.orderNum != orderNum;
                                });
                                //$scope.orders.push(data);
                            });
                        } else {
                            SweetAlert.swal("Cancelled", "Annulation");
                        }
                    });
            }
            $scope.getOnlineOrder = function() {
                ProductionOrderServiceAjax.getOnlineOrder().then(function(data) {
                    vm.dtInstanceAcceptance.reloadData();
                });
            }
            $scope.pushOrderInfoSse = function() {

                notificationService.getPubSub().subscribe(SSE_CONSTANTS.appEventsTopic, function(event) {
                    var eventData = JSON.parse(event.data);
                    if (eventData.target == 'Order' && eventData.error == false) {
                        if (angular.isDefined(vm.dtInstance) && vm.dtInstance != null) {
                            vm.dtInstance.DataTable.draw();
                        }
                        if (angular.isDefined(vm.dtInstanceAcceptance) && vm.dtInstanceAcceptance != null) {
                            vm.dtInstanceAcceptance.reloadData();
                        }
                    }
                    //$scope.$apply();
                })

            };
            $scope.pushOrderInfoSse();


            $scope.showAdminData = false;

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
                $http.post($rootScope.API_BASE + "/orders/paginated", { aoData }).then(function(result) {

                    var records = {
                        'draw': result.data.draw,
                        'recordsTotal': result.data.recordsTotal,
                        'recordsFiltered': result.data.recordsFiltered,
                        'data': result.data.data
                    };

                    fnCallback(records);
                });
            }

            vm.dtOptions = DTOptionsBuilder.newOptions()
                .withFnServerData(serverData)
                .withDOM('frtip')
                .withPaginationType('simple_numbers')
                .withOption('responsive', true)
                .withOption('fnRowCallback', rowCallback)
                .withButtons(['print', 'csv'])
                .withOption('processing', true)
                .withOption('serverSide', true)
                .withOption('order', [0, 'desc'])
                .withDisplayLength(25)
                .withColumnFilter({

                    aoColumns: [{
                        type: 'number'
                    }, {
                        type: 'text',
                        bRegex: true,
                        bSmart: true
                    }, {
                        type: 'text',
                        bRegex: true,
                        bSmart: true
                    }, {
                        type: 'text',
                        bRegex: true,
                        bSmart: true
                    }, {
                        type: 'text',
                        bRegex: true,
                        bSmart: true
                    }, {
                        type: 'text',
                        bRegex: true,
                        bSmart: true
                    }, {
                        type: 'text',
                        bRegex: true,
                        bSmart: true
                    }, {
                        type: 'text',
                        bRegex: true,
                        bSmart: true
                    }, {
                        type: 'text',
                        bRegex: true,
                        bSmart: true
                    }, {
                        type: 'select',
                        bRegex: false,
                        values: ['Normal', 'High', 'High*', 'High**']
                    }, {
                        type: 'select',
                        bRegex: false,
                        values: ['ACCEPTED', 'CANCELLED', 'COMPLETE', 'ERROR', 'ONPROD', 'PENDING', 'REJECTED', 'TOEPAC']
                    }]
                })
                .withLanguage($scope.dataTables);

            $scope.edit = $translate.instant('ORDERS_JS.ORDER');
            vm.dtColumns = [

                DTColumnBuilder.newColumn('orderId').withTitle($translate.instant('ORDERS_JS.ORDER')).renderWith(function(data) {
                    return "<button type='button' uib-tooltip='Production Overview'  title='" + $translate.instant('OVERVIEWORDER.TITLE') + data + ")' onclick=\"var $scope = angular.element(event.target).scope(); $scope.openOverviewOrderModal('" + data + "'); $scope.$apply()\" class='btn btn-info waves-effect fixed-table-button'>" + data + "</button>";
                }),
                DTColumnBuilder.newColumn('orderNum').withTitle('PO#'),
                DTColumnBuilder.newColumn('customer.email').withTitle('Email').withOption('defaultContent', ' '),
                DTColumnBuilder.newColumn('customer.fullName').withTitle($translate.instant('ORDERS_JS.NAME')).withOption('defaultContent', ' '),
                DTColumnBuilder.newColumn('allIsbns').withTitle('ISBN').renderWith(function(data) {
                    return "<div class='ellipses' title='" + data + "'  uib-tooltip='" + data + "' tooltip-placement='left'>" + data + "</div>";
                }),
                DTColumnBuilder.newColumn('source').withTitle($translate.instant('ORDERS_JS.Source')),
                DTColumnBuilder.newColumn('recievedDate').withTitle($translate.instant('ORDERS_JS.RECEIVED')).
                renderWith(function(data) {
                    var res = "_";
                    if (data > 0) {
                        var offset = new Date().getTimezoneOffset() * 60 * 1000;
                        var date = new Date(data - offset);
                        res = monthNames[date.getMonth()] + date.getDate() + ", " + date.getHours() + ":" + date.getMinutes();
                    }
                    return res;
                }),
                DTColumnBuilder.newColumn('dueDate').withTitle($translate.instant('ORDERS_JS.Due')).
                renderWith(function(data) {
                    var res = "_";
                    if (data > 0) {
                        var offset = new Date().getTimezoneOffset() * 60 * 1000;
                        var date = new Date(data - offset);
                        res = monthNames[date.getMonth()] + date.getDate();
                    }
                    return res;
                }),
                DTColumnBuilder.newColumn('quantity').withTitle($translate.instant('ORDERS_JS.QTY')).notSortable().withOption('defaultContent', ' '),
                DTColumnBuilder.newColumn('priority').withTitle($translate.instant('ORDERS_JS.PRIORITY')).withOption('defaultContent', ' '),
                DTColumnBuilder.newColumn('status').withTitle($translate.instant('ORDERS_JS.STATUS')).withOption('defaultContent', ' ').renderWith(function(data) {
                    if (data == "ERROR") {
                        return "<span class='badge badge-erroneous'>" + data + "</span>"
                    } else return data;
                }),
                DTColumnBuilder.newColumn('orderId').withTitle(' ').notSortable().withClass('display-flex').renderWith(function(data) {
                    var res = "<button type='button' uib-tooltip='Edit' title='" + $translate.instant('ORDERS_JS.EDIT') + "   " + data + "' onclick=\"var $scope = angular.element(event.target).scope(); $scope.openEditOrderModal('" + data + "'); $scope.$apply()\" class='btn bgm-orange-900 waves-circle waves-effect'><i class='zmdi zmdi-edit'></i></button>&nbsp;<button type='button' uib-tooltip='Delete' title='" + $translate.instant('ORDERS_JS.DELETE_js') + " " + data + "' onclick=\"var $scope = angular.element(event.target).scope(); $scope.deleteOrder('" + data + "'); $scope.$apply()\" class='btn bgm-deeporange waves-circle waves-effect'><i class='zmdi zmdi-delete'></i></button>&nbsp;<button type='button' uib-tooltip='overview'  title='" + $translate.instant('ORDERS_JS.View_Order_Status') + " " + data + "' onclick=\"var $scope = angular.element(event.target).scope(); $scope.openProductionOverviewOrderModal('" + data + "'); $scope.$apply()\" class='btn bgm-info-btn waves-circle waves-effect'><i class='zmdi zmdi-info'></i></i></button>";
                    //if($rootScope.includeShipping == 'true'){
                    //  res += "&nbsp;<button type='button' uib-tooltip='Complete' title='"+$translate.instant('ORDERS_JS.View_Order_Status')+" " + data +"' onclick=\"var $scope = angular.element(event.target).scope();$scope.completeOrder('"+data+"'); $scope.$apply()\" class='btn bgm-teal waves-circle waves-effect'><i class='zmdi zmdi-check'></i></i></button>";
                    //}
                    return res;
                })
            ];

            //title=' 'ORDERS_JS.QTY' | translate
            $scope.dtColumnDefsForDataSupports = [
                DTColumnDefBuilder.newColumnDef(0).notSortable(),
                DTColumnDefBuilder.newColumnDef(1).notSortable(),
                DTColumnDefBuilder.newColumnDef(2).notSortable(),
                DTColumnDefBuilder.newColumnDef(3).notSortable(),
                DTColumnDefBuilder.newColumnDef(4).notSortable()
            ];

            vm.dtOptionsForDataSupports = DTOptionsBuilder.fromSource().withDOM('rtip').withOption('order', []).withOption('paging', 'first_last_numbers').withDisplayLength(5).withLanguage($scope.dataTables);

            vm.dtOptionsForOrderProductionStatus = DTOptionsBuilder.fromSource().withDOM('rt').withOption('order', []).withDisplayLength(500).withScroller()
                .withOption('defaultContent', ' ').withOption('scrollY', 600).withLanguage($scope.dataTables);

            $scope.dtColumnDefsForOrderProductionStatus = [
                DTColumnDefBuilder.newColumnDef(0).notSortable(),
                DTColumnDefBuilder.newColumnDef(1).notSortable(),
                DTColumnDefBuilder.newColumnDef(2).notSortable(),
                DTColumnDefBuilder.newColumnDef(3).notSortable(),
                DTColumnDefBuilder.newColumnDef(4).notSortable()
            ];

            function rowCallback(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
                switch (aData.status) {
                    case 'ERROR':
                        $(nRow).css('color', 'red')
                        break;
                }
                return nRow;
            }

            $scope.refreshQtys = function() {
                $('#quantityMax').val(Math.floor(Number($('#quantity').val()) + Number($('#quantity').val()) * Number($rootScope.overs.name) / 100) + Number($rootScope.oversAdditif.name));
                $('#quantityMin').val(Math.floor(Number($('#quantity').val()) - Number($('#quantity').val()) * Number($rootScope.unders.name) / 100) - Number($rootScope.undersAdditif.name));
                if (angular.isDefined($scope.order.orderPart)) {
                    $scope.order.orderPart.quantityMax = Number($('#quantityMax').val());
                    $scope.order.orderPart.quantityMin = Number($('#quantityMin').val());
                }
            };

            $scope.errorStatusCount = function() {
                orderServiceAjax.errorStatusCount().then(function(data) {
                    $scope.errorCount = data;
                });
            };
            $scope.errorStatusCount();

            $scope.deleteOrder = function(id) {
                $scope.errorMsg = false;
                $scope.successMsg = false;
                $scope.warningMsg = false;
                SweetAlert.swal({
                        title: $translate.instant('ORDERS_JS.DELETE_ORDER'),
                        text: $translate.instant('ORDERS_JS.SURE_DELETE_ORDER'),
                        type: "warning",
                        showCancelButton: true,
                        confirmButtonColor: "#DD6B55",
                        confirmButtonText: $translate.instant('ORDERS_JS.DELETE'),
                        closeOnConfirm: true
                    },
                    function(isConfirm) {
                        if (isConfirm) {
                            orderServiceAjax.deleteOrder(id).then(function(data) {
                                $scope.orders = data;
                                vm.dtInstance.reloadData();
                                toasty.success({
                                    title: $translate.instant('ORDERS_JS.DELETE_ORDER'),
                                    msg: $translate.instant('ORDERS_JS.ORDER_SUCC_DELETED'),
                                    showClose: true,
                                    clickToClose: true,
                                    timeout: 5000,
                                    sound: false,
                                    html: false,
                                    shake: false,
                                    theme: "bootstrap"
                                });
                            }, function(data, status, headers, config) {
                                // $scope.errorMsg = true;
                                //  $scope.successMsg = false;
                                // $scope.alertMsg = data.errors.errors;
                                SweetAlert.swal($translate.instant('ORDERS_JS.DELETION_ERROR'), data.data.errors.errors, "error");
                            });
                        }
                    });
            };

            $scope.openAddOrderAutomatically = function() {
                $location.path('/orders');
            };
            $scope.dateOptions = {
                /*dateDisabled: disabled,
                formatYear: 'yy',
                maxDate: new Date(2020, 5, 22),
                minDate: new Date(),
                startingDay: 1*/
                type: 'datetime-local'
            };
            $scope.open1 = function() {
                $scope.popup1.opened = true;
            };
            $scope.popup1 = {
                opened: false
            };
            $scope.open3 = function() {
                $scope.popup3.opened = true;
            };
            $scope.popup3 = {
                opened: false
            };
            $scope.open2 = function() {
                $scope.popup2.opened = true;
            };
            $scope.popup2 = {
                opened: false
            };

            $scope.openEditOrderModal = function(id) {
                var modalInstance = $uibModal.open({
                    backdrop: 'static',
                    keyboard: false,
                    animation: true,
                    templateUrl: './views/editOrderModalContent.html',
                    controller: 'EditOrderModalInstanceCtrl',
                    scope: $scope,
                    size: 'xl',
                    resolve: {
                        orderId: function() {
                            return id;
                        }
                    }
                });

                modalInstance.result.then(function(selectedItem) {
                    vm.dtInstance.reloadData();
                }, function() {});

            };

            $scope.openAddOrderModal = function() {
                var modalInstance = $uibModal.open({
                    backdrop: 'static',
                    keyboard: false,
                    animation: true,
                    templateUrl: './views/addOrderModalContent.html',
                    controller: 'AddOrderModalInstanceCtrl',
                    scope: $scope,
                    size: 'xl',
                    resolve: {
                        vmDataTable: function() {
                            return vm;
                        }
                    }
                });
                modalInstance.result.then(function(result) {
                    vm.dtInstance.reloadData();
                }, function() {});
            };

            $scope.openOverviewOrderModal = function(id) {
                var modalInstance = $uibModal.open({
                    backdrop: 'static',
                    keyboard: false,
                    animation: true,
                    templateUrl: './views/overviewOrder.html',
                    controller: 'OverviewditOrderModalInstanceCtrl',
                    scope: $scope,
                    size: 'xl',
                    resolve: {
                        orderId: function() {
                            return id;
                        },
                        actionType: function() {
                            return "overview";
                        }
                    }
                });
                modalInstance.result.then(function() {
                    vm.dtInstance.reloadData();
                }, function() {});
            };

            $scope.openProductionOverviewOrderModal = function(id) {
                var modalInstance = $uibModal.open({
                    backdrop: 'static',
                    keyboard: false,
                    animation: true,
                    templateUrl: './views/overviewProductionOrder.html',
                    controller: 'OverviewditOrderModalInstanceCtrl',
                    scope: $scope,
                    size: 'xl',
                    resolve: {
                        orderId: function() {
                            return id;
                        },
                        actionType: function() {
                            return "production";
                        }
                    }
                });
                modalInstance.result.then(function() {

                }, function() {});
            };

            $scope.order = jQuery.extend({}, $scope.defaultOrder);

            $scope.loadCustomerOptions = function() {
                customerServiceAjax.customers().then(function(data) {
                    $scope.customerOptions = data;
                });
            };
            $scope.loadCustomerOptions();
            $scope.order.customer = {
                repeatSelect: null,
                availableOptions: $scope.customerOptions,
            };

            $scope.loadPriorityOptions = function() {
                lookupServiceAjax.readAll('Priority').then(function(data) {
                    $scope.priorityOptions = data;
                    // for (var i = 0; i < data.length; i += 1) {
                    //  $('#priority').append('<option value="' + data[i].id + '">' + data[i].name + '</option>');
                    //  }
                    // for (prio in data) {
                    //  $('#priority').append('<option value="' + prio.id + '">' + prio.name + '</option>');
                    // }
                    //  var t = '1';
                    //   var p = '1';
                    //  $('#priority').append('<option value="' + t + '">' + p + '</option>');
                });
            };
            $scope.loadPriorityOptions();
            $scope.priority = {
                repeatSelect: null,
                availableOptions: $scope.priorityOptions,
            };

            /*$scope.loadOrderStatusOptions = function(){
	 	    	  lookupServiceAjax.readAll('OrderStatus').then(function(data){
	 		            $scope.orderStatusOptions = data;
	 		         });
	 		};
	 		$scope.loadOrderStatusOptions();
	 		$scope.orderStatus = {
	 			    repeatSelect: null,
	 			    availableOptions: $scope.orderStatusOptions
	 		};*/

            $scope.loadPaperTypeOptions = function() {
                lookupServiceAjax.readAll('PaperType').then(function(data) {
                    $scope.paperTypeOptions = data;
                });
            };
            $scope.loadPaperTypeOptions();
            $scope.paperType = {
                repeatSelect: null,
                availableOptions: $scope.paperTypeOptions,
            };

            $scope.loadLaminationOptions = function() {
                lookupServiceAjax.readAll('Lamination').then(function(data) {
                    $scope.laminationOptions = data;
                });
            };
            $scope.loadLaminationOptions();
            $scope.lamination = {
                repeatSelect: null,
                availableOptions: $scope.laminationOptions,
            };

            $scope.loadBindingTypeOptions = function() {
                lookupServiceAjax.readAll('BindingType').then(function(data) {
                    $scope.bindingTypeOptions = data;
                });
            };
            $scope.loadBindingTypeOptions();
            $scope.bindingType = {
                repeatSelect: null,
                availableOptions: $scope.bindingTypeOptions,
            };

            $scope.loadCritiriaOptions = function() {
                lookupServiceAjax.readAll('Critiria').then(function(data) {
                    $scope.critiriaOptions = data;
                });
            };
            $scope.loadCritiriaOptions();
            $scope.critiria = {
                repeatSelect: null,
                availableOptions: $scope.critiriaOptions,
            };

            $scope.uploadFiles = function(file, errFiles, localScope, partCategory, addEdit) {
                if (file) {
                    file.upload = Upload.upload({
                        url: $rootScope.API_BASE + '/parts/upload/' + addEdit + '?access_token=' + token,
                        data: {
                            partNum: localScope.part.partNum,
                            isbn: localScope.part.isbn,
                            fileName: file.name,
                            file: file,
                            partCategory: partCategory
                        }
                    });

                    file.upload.then(function(response) {
                        if (partCategory == 'Text') {
                            localScope.uploadedFileId[0] = response.data;
                            localScope.uploadedFileName[0] = file.name;
                        } else {
                            localScope.uploadedFileId[1] = response.data;
                            localScope.uploadedFileName[1] = file.name;
                        }
                        $timeout(function() {
                            file.result = response.data;
                        });
                    }, function(response) {
                        console.log(response);
                        if (response.status > 0)
                            $scope.errorMsg = response.status + ': ' + response.data;
                    }, function(evt) {
                        //var progressPercentage = parseInt(100.0 * evt.loaded / evt.total);
                        // Math.min is to fix IE which reports 200% sometimes
                        file.progress = Math.min(99, parseInt(100.0 * evt.loaded / evt.total));
                        console.log(partCategory + ' upload progress: ' + file.progress + '% ' + evt.config.data.file.name);
                    });
                }
            };
            //	 		$scope.pushOrderSSe();
        }
    ]);

angular.module('capApp')
    .controller('CRUDOrderParentInstanceCtrl', function($rootScope, $scope, Upload, lookupServiceAjax, customerServiceAjax, $translatePartialLoader, $translate, $localStorage) {

        $scope.loadOrderStatusOptions = function() {
            lookupServiceAjax.readAll('OrderStatus').then(function(data) {
                $scope.orderStatusOptions = data;
            });
        };
        $scope.loadOrderStatusOptions();
        $scope.orderStatus = {
            repeatSelect: null,
            availableOptions: $scope.orderStatusOptions
        };

        $scope.defaultOrder = {
            "orderNum": "",
            "orderPart": { "part": { "partNum": "", "isbn": "", "title": "" }, "quantity": "", "quantityMax": 1, "quantityMin": 1 },
            "parts": [''],
            "status": "",
            "dueDate": "",
            "recievedDate": "",
            "priority": "NORMAL",
            "notes": "",
            "customer": { "email": "" }
        };
        $scope.order = jQuery.extend({}, $scope.defaultOrder);

        $scope.loadCustomerOptions = function() {
            customerServiceAjax.customers().then(function(data) {
                $scope.customerOptions = data;
            });
        };
        $scope.loadCustomerOptions();
        $scope.order.customer = {
            repeatSelect: null,
            availableOptions: $scope.customerOptions,
        };
        
        $scope.loadClientOptions = function() {
            lookupServiceAjax.readAll('Client').then(function(data) {
                $scope.clientOptions = data;
            });
        };
        $scope.loadClientOptions();
        $scope.client = {
            repeatSelect: null,
            availableOptions: $scope.clientOptions
        };

        $scope.loadPriorityOptions = function() {
            lookupServiceAjax.readAll('Priority').then(function(data) {
                $scope.priorityOptions = data;
            });
        };
        $scope.loadPriorityOptions();
        $scope.priority = {
            repeatSelect: null,
            availableOptions: $scope.priorityOptions,
        };

        $scope.loadPaperTypeOptions = function() {
            lookupServiceAjax.readAll('PaperType').then(function(data) {
                $scope.paperTypeOptions = data;
            });
        };
        $scope.loadPaperTypeOptions();
        $scope.paperType = {
            repeatSelect: null,
            availableOptions: $scope.paperTypeOptions,
        };

        $scope.loadLaminationOptions = function() {
            lookupServiceAjax.readAll('Lamination').then(function(data) {
                $scope.laminationOptions = data;
            });
        };
        $scope.loadLaminationOptions();
        $scope.lamination = {
            repeatSelect: null,
            availableOptions: $scope.laminationOptions,
        };

        $scope.loadBindingTypeOptions = function() {
            lookupServiceAjax.readAll('BindingType').then(function(data) {
                $scope.bindingTypeOptions = data;
            });
        };
        $scope.loadBindingTypeOptions();
        $scope.bindingType = {
            repeatSelect: null,
            availableOptions: $scope.bindingTypeOptions,
        };

        $scope.loadCritiriaOptions = function() {
            lookupServiceAjax.readAll('Critiria').then(function(data) {
                $scope.critiriaOptions = data;
            });
        };
        $scope.loadCritiriaOptions();
        $scope.critiria = {
            repeatSelect: null,
            availableOptions: $scope.critiriaOptions,
        };

        $scope.uploadFiles = function(file, errFiles, localScope, partCategory, addEdit) {
            if (file) {
                file.upload = Upload.upload({
                    url: $rootScope.API_BASE + '/parts/upload/' + addEdit + '?access_token=' + token,
                    data: {
                        partNum: localScope.part.partNum,
                        isbn: localScope.part.isbn,
                        fileName: file.name,
                        file: file,
                        partCategory: partCategory
                    }
                });

                file.upload.then(function(response) {
                    if (partCategory == 'Text') {
                        localScope.uploadedFileId[0] = response.data;
                        localScope.uploadedFileName[0] = file.name;
                    } else {
                        localScope.uploadedFileId[1] = response.data;
                        localScope.uploadedFileName[1] = file.name;
                    }
                    $timeout(function() {
                        file.result = response.data;
                    });
                }, function(response) {
                    console.log(response);
                    if (response.status > 0)
                        $scope.errorMsg = response.status + ': ' + response.data;
                }, function(evt) {
                    //var progressPercentage = parseInt(100.0 * evt.loaded / evt.total);
                    // Math.min is to fix IE which reports 200% sometimes
                    file.progress = Math.min(99, parseInt(100.0 * evt.loaded / evt.total));
                    console.log(partCategory + ' upload progress: ' + file.progress + '% ' + evt.config.data.file.name);
                });
            }
        };

        $scope.calculateUnscheduledHours("A");

        $scope.calculateScheduledHours("A");
    });

angular.module('capApp')
    .controller('AddOrderModalInstanceCtrl', function($rootScope, $scope, $controller, $uibModalInstance, $uibModal, orderServiceAjax, customerServiceAjax, partServiceAjax, lookupServiceAjax, toasty, $translatePartialLoader, $translate, $localStorage, vmDataTable, SweetAlert) {

        $controller('CRUDOrderParentInstanceCtrl', { $scope: $scope });

        $.fn.dataTable.ext.errMode = 'none';
        var token = $localStorage.oauthToken;

        var alertAddMessage = $translate.instant('ORDERS_JS.ORDER_ADDED') + "!!";
        $scope.order = jQuery.extend(true, {}, $scope.defaultOrder);

        $scope.order.status = 'PENDING';
        $scope.title = "";
        $scope.pagesCount = 0;
        $scope.order.orderPart.quantityMax = null;
        $scope.order.orderPart.quantityMin = null;
        $scope.errors = jQuery.extend({}, $scope.defaultOrder);
        $scope.$parent.successMsg = false;
        $scope.$parent.errorMsg = false;
        $scope.$parent.warningMsg = false;
        $scope.errorMsg = false;

        $scope.order.orderPart.quantityMax = 1;
        $scope.order.orderPart.quantityMin = 1;
        $scope.order.orderParts = [];
        $scope.olCount = 0;



        $scope.openEditOrderLineModal = function(count) {
            var modalInstance = $uibModal.open({
                backdrop: 'static',
                keyboard: false,
                animation: true,
                templateUrl: './views/editOrderLine.html',
                controller: 'editOrderLineModalInstanceCtrl',
                scope: $scope,
                size: 'lg',
                resolve: {
                    count: function() {
                        return count;
                    }
                }
            });
            modalInstance.result.then(function(result) {
                //vm.dtInstance.reloadData();
                //update the title
                //$scope.order.orderPart.isbn = result.isbn
                //$scope.loadBookOptions();
                $scope.order.orderPart.part.partNum = result.partNum;
                $scope.order.orderPart.part.isbn = result.isbn;
                $scope.pagesCount = result.pagesCount;
                $scope.title = result.title;
            }, function() {});
        };

        $scope.addOrder = function() {
            /* if($scope.order.orderPart.quantityMax == null || $scope.order.orderPart.quantityMax == ''){
			    $scope.order.orderPart.quantityMax =  Math.ceil(Number($scope.order.orderPart.quantity) + Number($scope.order.orderPart.quantity) *
					Number($rootScope.overs.name) / 100);
		    }
		    if($scope.order.orderPart.quantityMin == null || $scope.order.orderPart.quantityMin == ''){
			    $scope.order.orderPart.quantityMin =  Math.floor(Number($scope.order.orderPart.quantity) - Number($scope.order.orderPart.quantity) *
					Number($rootScope.unders.name) / 100);
		    }*/
            if ($('#quantityMax').val() != '') {
                $scope.order.orderPart.quantityMax = Number($('#quantityMax').val());
            }
            if ($('#quantityMin').val() != '') {
                $scope.order.orderPart.quantityMin = Number($('#quantityMin').val());
            }
            orderServiceAjax.addOrder($scope.order)
                .then(function(data, status, headers, config) {
                    $scope.errors = jQuery.extend({}, $scope.defaultOrder);
                    $scope.$parent.alertMsg = alertAddMessage;
                    //$scope.$parent.successMsg = true;
                    $scope.$parent.errorMsg = false;
                    $scope.$parent.warningMsg = false;
                    toasty.success({
                        title: $translate.instant('ORDERS_JS.ADDING_ORDER'),
                        msg: $translate.instant('ORDERS_JS.ORDER2') + data + $translate.instant('ORDERS_JS.ADDED'),
                        clickToClose: true,
                        sound: true,
                        timeout: 10000,
                        html: false,
                        shake: false,
                        theme: "bootstrap",
                        position: 'top-right'
                    });
                    $uibModalInstance.close();
                    vmDataTable.dtInstance.DataTable.draw();
                }, function(data, status, headers, config) {
                    $scope.errorMsg = true;
                    /*SweetAlert.swal({
	         		   title: 'ERROR',
	         		   text:  $translate.instant('ORDERS_JS.ORDER2') + data.data.errors.errors,
	         		   type: "error",
	         		   showCancelButton: false,
	         		   confirmButtonColor: "#DD6B55",
	         		   confirmButtonText: "OK",
	         		   closeOnConfirm: true});*/
                    $scope.successMsg = false;
                    $scope.warningMsg = false;
                    $scope.errors = data.data.errors.errors;
                });

        };
        $scope.closeModal = function() {
            // $scope.order.orderPart.part.partNum = "";
            $uibModalInstance.dismiss();
        };

        $scope.refreshOrderPart = function(id) {
            partServiceAjax.getPartById(id).then(function(data) {
                //$scope.order.parts = [id];
                $scope.title = data.title;
                $scope.order.orderPart.part.partNum = data.partNum;
                $scope.order.orderPart.part.isbn = data.isbn;
                $scope.pagesCount = data.pagesCount;
                //$scope.order.parts.repeatSelect = data.partNum;
            });
        };

        $scope.removeOrderPart = function(index, quantity) {
            if ($scope.order.orderParts.length == 1) {
                SweetAlert.swal($translate.instant('js_parts.DeletionError'), $translate.instant('partsList.errorDelete'), "error");
            } else {
                SweetAlert.swal({
                        title: $translate.instant('partsList.confirmationDol'),
                        text: $translate.instant('partsList.confirmDelete'),
                        type: "warning",
                        showCancelButton: true,
                        confirmButtonColor: "#DD6B55",
                        confirmButtonText: $translate.instant('js_Scheduling.Continue'),
                        closeOnConfirm: true
                    },
                    function(isConfirm) {
                        if (isConfirm) {
                            $scope.order.orderParts.splice(index, 1);
                            $scope.olCount -= quantity;
                        }
                    });
            }

        }

        $scope.loadBookOptions = function() {
            partServiceAjax.getIsbnPartsForAdd().then(function(data) {
                $scope.bookOptions = data;
            });
        };
        // $scope.loadBookOptions();
        // $scope.order.orderPart.part = {
        //   repeatSelect: null,
        //	availableOptions: $scope.bookOptions,
        // };

        $scope.openAddOrderLineModal = function(id) {
            var modalInstance = $uibModal.open({
                backdrop: 'static',
                keyboard: false,
                animation: true,
                templateUrl: './views/addOrderLineModal.html',
                controller: 'addOrderLineModalInstanceCtrl',
                scope: $scope,
                size: 'lg',
                resolve: {
                    partNum: function() {
                        return id;
                    }
                }
            });
            modalInstance.result.then(function(result) {
                //vm.dtInstance.reloadData();
                //update the title
                //$scope.order.orderPart.isbn = result.isbn
                //$scope.loadBookOptions();
                $scope.order.orderPart.part.partNum = result.partNum;
                $scope.order.orderPart.part.isbn = result.isbn;
                $scope.pagesCount = result.pagesCount;
                $scope.title = result.title;
            }, function() {});
        };
        $scope.openEditPartModal = function(id) {
            var modalInstance = $uibModal.open({
                backdrop: 'static',
                keyboard: false,
                animation: true,
                templateUrl: './views/editPartModalContent.html',
                controller: 'EditPartModalInstanceCtrl',
                scope: $scope,
                size: 'lg',
                resolve: {
                    partNum: function() {
                        return id;
                    },
                    disableFields: function() {
                        return false;
                    }
                }
            });
            modalInstance.result.then(function(result) {
                //vm.dtInstance.reloadData();
                //update the title
                //$scope.order.orderPart.isbn = result.isbn
                //$scope.loadBookOptions();
                $scope.order.orderPart.part.partNum = result.partNum;
                $scope.order.orderPart.part.isbn = result.isbn;
                $scope.pagesCount = result.pagesCount;
                $scope.title = result.title;
            }, function() {});
        };

        $scope.openAddPartModal = function() {
            var modalInstance = $uibModal.open({
                animation: true,
                backdrop: 'static',
                keyboard: false,
                templateUrl: './views/addPartModalContent.html',
                controller: 'AddPartModalInstanceCtrl',
                scope: $scope,
                size: 'lg',
                resolve: {

                }
            });
            modalInstance.result.then(function(result) {
                //vm.dtInstance.reloadData();
                //$scope.order.orderPart.isbn = result.isbn
                //$scope.loadBookOptions();
                $scope.order.orderPart.part.partNum = result.partNum;
                $scope.order.orderPart.part.isbn = result.isbn;
                $scope.title = result.title;
                $scope.pagesCount = result.pagesCount;
                $scope.openEditPartModal(result.partNum);
            }, function() {});
        };

        $scope.openSearchPartModal = function() {
            var modalInstance = $uibModal.open({
                backdrop: 'static',
                keyboard: false,
                animation: true,
                templateUrl: './views/searchPartModalContent.html',
                controller: 'SearchPartModalInstanceCtrl',
                scope: $scope,
                size: 'lg',
                resolve: {}
            });
            modalInstance.result.then(function(result) {
                //vm.dtInstance.reloadData();
                //update the title
                //$scope.order.orderPart.isbn = result.isbn
                //$scope.loadBookOptions();
                $scope.order.orderPart.part.partNum = result.partNum;
                $scope.order.orderPart.part.isbn = result.isbn;
                $scope.pagesCount = result.pagesCount;
                $scope.title = result.title;
            }, function() {});
        };

        $scope.openAddCustomerModal = function() {
            var modalInstance = $uibModal.open({
                animation: true,
                backdrop: 'static',
                keyboard: false,
                templateUrl: 'addCustomerModalContent.html',
                controller: 'AddCustomerModalInstanceCtrl',
                size: 'lg',
                scope: $scope,
                resolve: {

                }
            });
            modalInstance.result.then(function(result) {
                $scope.loadCustomerOptions();
                $scope.order.customer.email = result.email;
            }, function() {});
        };

    });

angular.module('capApp')
    .controller('OrderPartDsModalInstanceCtrl', function($rootScope, $scope, $uibModalInstance, $uibModal, lookupServiceAjax, orderServiceAjax, jobServiceAjax, ordpart, orderId, source, toasty, $translatePartialLoader, SweetAlert, $translate, $localStorage) {
        var token = $localStorage.oauthToken;
        $scope.token = token;
        $scope.source = source;
        $scope.partNum = partNum;
        $scope.ordpart = ordpart;
        //$scope.part = {};
        /*orderServiceAjax.getOrderPartDs(ordpart).then(function(data){
		 $scope.part = data;
	 });*/

        $scope.fileSteps = ['PENDING', 'DOWNLOADING', 'IMPOSING', 'RIPPING', 'READY'];
        //Fill the file Status
        $scope.getAction = function(id) {
            $.ajax({
                url: $rootScope.API_BASE + "/progress/part/" + id + '?access_token=' + token,
                type: 'GET',
                success: function(data) {
                    $scope.progress = data;
                }
            });
        }
        $scope.getAction(orderId);
        $scope.closeModal = function() {
            // $scope.order.orderPart.part.partNum = "";
            $uibModalInstance.dismiss();
        };

        jobServiceAjax.getPartProducedQuantity(orderId, ordpart.part.partNum).then(function(data) {
            $scope.partQuantityProduced = data;
        });

        $scope.openEditPartModal = function(id) {
            var modalInstance = $uibModal.open({
                backdrop: 'static',
                keyboard: false,
                animation: true,
                templateUrl: './views/editPartModalContent.html',
                controller: 'EditPartModalInstanceCtrl',
                scope: $scope,
                size: 'lg',
                resolve: {
                    partNum: function() {
                        return id;
                    },
                    disableFields: function() {
                        return false;
                    }
                }
            }).result.then(function() {}, function(res) {});
        }
        $scope.reRunWorkFlow = function(partNum, source) {
            SweetAlert.swal({
                    title: $translate.instant('OVERVIEWORDER.RERUNWORKFLOW'),
                    text: $translate.instant('OVERVIEWORDER.RERUNWORKFLOW') + "?",
                    type: "warning",
                    showCancelButton: true,
                    confirmButtonColor: "#DD6B55",
                    closeOnConfirm: true
                },
                function(isConfirm) {
                    if (isConfirm) {
                        orderServiceAjax.workFlow(partNum, orderId, source).then(function(data) {
                            toasty.success({
                                title: $translate.instant('OVERVIEWORDER.RERUNWORKFLOW'),
                                msg: $translate.instant('OVERVIEWORDER.RERUNWORKFLOW') + " ok.",
                                showClose: true,
                                clickToClose: true,
                                timeout: 5000,
                                sound: false,
                                html: false,
                                shake: false,
                                theme: "bootstrap"
                            });
                        }, function(data, status, headers, config) {
                            // $scope.errorMsg = true;
                            //  $scope.successMsg = false;
                            // $scope.alertMsg = data.errors.errors;
                            SweetAlert.swal($translate.instant('OVERVIEWORDER.RERUNWORKFLOW'), data.data.errors.errors, "error");
                        });
                    }
                });
        }
        $scope.pnlTrial = function(partNum) {
        	var dim = [];
        	dim.push(null);
        	dim.push(null);
        	lookupServiceAjax.getPNLInfoTrial(partNum, dim).then(function(data) {
        		SweetAlert.swal($translate.instant('OVERVIEWORDER.PNLTRIAL'), data, "success");
            }, function(data, status, headers, config) {
                SweetAlert.swal($translate.instant('OVERVIEWORDER.PNLTRIAL'), data.data.errors.errors, "error");
            });
         }
});


angular.module('capApp').controller('EditPartDsModalInstanceCtrl', function($rootScope, $scope, $uibModalInstance, $uibModal, orderServiceAjax, jobServiceAjax, ordpart, orderId, source, disableFields, toasty, $translatePartialLoader, SweetAlert, $translate, $localStorage) {
    var token = $localStorage.oauthToken;
    $scope.token = token;
    $scope.source = source;
    $scope.ordpart = ordpart;
    $scope.quantity = $scope.ordpart.quantity;
    $scope.quantityMax = $scope.ordpart.quantityMax;
    $scope.quantityMin = $scope.ordpart.quantityMin;
    $scope.disableOrderFields = disableFields;
    var alertUpdateMessage = $translate.instant('ORDERS_JS.UPDATED_ORDER');

    $scope.closeModal = function() {
        // $scope.order.orderPart.part.partNum = "";
        $uibModalInstance.dismiss();
    };

    $scope.refreshQtys = function() {
        $('#quantityMax').val(Math.floor(Number($('#quantity').val()) + Number($('#quantity').val()) * Number($rootScope.overs.name) / 100) + Number($rootScope.oversAdditif.name));
        $('#quantityMin').val(Math.floor(Number($('#quantity').val()) - Number($('#quantity').val()) * Number($rootScope.unders.name) / 100) - Number($rootScope.undersAdditif.name));

        $scope.quantityMax = Number($('#quantityMax').val());
        $scope.quantityMin = Number($('#quantityMin').val());
    };

    $scope.updateOrderPart = function() {
        $scope.ordpart.quantity = $scope.quantity;
        $scope.ordpart.quantityMax = $scope.quantityMax;
        $scope.ordpart.quantityMin = $scope.quantityMin;
        orderServiceAjax.updateOrderPart($scope.ordpart)
            .then(function() {
                $scope.alertMsg = alertUpdateMessage;
                toasty.success({
                    title: $translate.instant('ORDERS_JS.ORDER_UPDATE'),
                    msg: $translate.instant('ORDERS_JS.UPDATED_ORDER') + "!",
                    showClose: true,
                    clickToClose: true,
                    timeout: 10000,
                    sound: false,
                    html: false,
                    shake: false,
                    theme: "bootstrap"
                });
                $scope.errors = jQuery.extend({}, $scope.defaultOrder);
                $scope.$parent.alertMsg = alertUpdateMessage;
                // $scope.$parent.successMsg = true;
                $scope.$parent.errorMsg = false;
                $scope.$parent.warningMsg = false;
                $uibModalInstance.close();
            }, function(data, status, headers, config) {
                $scope.errorMsg = true;
                $scope.successMsg = false;
                $scope.warningMsg = false;
                $scope.errors = data.errors;
            });

    };
    jobServiceAjax.getPartProducedQuantity(orderId, ordpart.part.partNum).then(function(data) {
        $scope.partQuantityProduced = data;
    });

    $scope.openEditPartModal = function(id) {

        var modalInstance = $uibModal.open({
            backdrop: false,
            keyboard: false,
            animation: true,
            templateUrl: './views/editPartModalContent.html',
            controller: 'EditPartModalInstanceCtrl',
            scope: $scope,
            size: 'lg',
            resolve: {
                partNum: function() {
                    return id;
                },
                disableFields: function() {
                    return $scope.disableOrderFields;
                }
            }
        }).result.then(function(result) {

            $scope.ordpart.part = result;

        }, function() {});
    }
});

angular.module('capApp')
    .controller('OverviewditOrderModalInstanceCtrl', function($rootScope, $scope, $uibModalInstance, $uibModal, orderServiceAjax,
        jobServiceAjax, customerServiceAjax, partServiceAjax, lookupServiceAjax, actionType, orderId, toasty, $translatePartialLoader, SweetAlert,
        $translate, $localStorage, notificationService, SSE_CONSTANTS, $log) {

        var alertUpdateMessage = $translate.instant('ORDERS_JS.UPDATED_ORDER');
        var token = $localStorage.oauthToken;
        $scope.token = token;
        $scope.rerunValid = false;
        $scope.order = jQuery.extend({}, $scope.defaultOrder);
        $scope.title = "";
        $scope.errors = jQuery.extend({}, $scope.defaultOrder);
        $scope.$parent.successMsg = false;
        $scope.$parent.errorMsg = false;
        $scope.$parent.warningMsg = false;
        $scope.errorMsg = false;
        $scope.collapseRows = true;
        $scope.jobs = {};

        if (actionType == 'production') {
            jobServiceAjax.getOrderJobs(orderId).then(function(data) {
                $scope.jobs = data;
                var cumulNeeded = 0;
                var cumulProduced = 0;
                for (var i = 0; i < data.length; i++) {
                    cumulNeeded = cumulNeeded + data[i].quantityNeeded;
                    cumulProduced = cumulProduced + data[i].quantityProduced;
                    if (i == data.length - 1 || data[i].stationId != data[i + 1].stationId) {
                        data[i].jobName = '' + cumulProduced + '/' + cumulNeeded;
                        cumulNeeded = 0;
                        cumulProduced = 0;
                    }
                }
            });
        }

        $scope.fileSteps = ['PENDING', 'DOWNLOADING', 'IMPOSING', 'RIPPING', 'READY'];
        $scope.orderSteps = ['PENDING', 'ACCEPTED', 'ONPROD', 'COMPLETE'];

        //Fill the file Status
        $scope.getAction = function(id) {
            $.ajax({
                url: $rootScope.API_BASE + "/progress/part/" + id + '?access_token=' + token,
                type: 'GET',
                success: function(data) {
                    $scope.progress = data;
                }
            });
        }
        $scope.loadOrderStatusOptions = function() {
            lookupServiceAjax.readAll('OrderStatus').then(function(data) {
                $scope.orderStatusOptions = data;
            });
        };
        $scope.loadOrderStatusOptions();
        $scope.orderStatus = {
            repeatSelect: null,
            availableOptions: $scope.orderStatusOptions
        };
        $scope.loadClientOptions = function() {
            lookupServiceAjax.readAll('Client').then(function(data) {
                $scope.clientOptions = data;
            });
        };
        $scope.loadClientOptions();
        $scope.client = {
            repeatSelect: null,
            availableOptions: $scope.clientOptions
        };
        orderServiceAjax.getOrderOverviewById(orderId).then(function(data) {
            $scope.order = data;
            $scope.title = "";
            $scope.rerunValid = $scope.order.parts.length != 1
            if (data.orderPart != null && data.orderPart.part != null) {
                $scope.title = data.orderPart.part.title;
                //$scope.order.orderPart.part.partNum = data.orderPart.part.partNum;
                //$scope.order.orderPart.part.isbn = data.orderPart.part.isbn;
            }
            $scope.dueDate = new Date(data.dueDate);
            jobServiceAjax.getOrderProducedQuantity(orderId).then(function(data2) {
                $scope.orderQuantityProduced = data2;
            });
            $scope.getAction(orderId);
            $scope.updateOrder = function() {
                $scope.order.dueDate = $scope.dueDate;
                orderServiceAjax.updateOrder($scope.order)
                    .then(function() {
                        $scope.alertMsg = alertUpdateMessage;
                        toasty.success({
                            title: $translate.instant('ORDERS_JS.ORDER_UPDATE'),
                            msg: $translate.instant('ORDERS_JS.UPDATED_ORDER') + "!",
                            showClose: true,
                            clickToClose: true,
                            timeout: 10000,
                            sound: false,
                            html: false,
                            shake: false,
                            theme: "bootstrap"
                        });
                        $scope.errors = jQuery.extend({}, $scope.defaultOrder);
                        $scope.$parent.alertMsg = alertUpdateMessage;
                        // $scope.$parent.successMsg = true;
                        $scope.$parent.errorMsg = false;
                        $scope.$parent.warningMsg = false;
                        $uibModalInstance.close();
                    }, function(data, status, headers, config) {
                        $scope.errorMsg = true;
                        $scope.successMsg = false;
                        $scope.warningMsg = false;
                        $scope.errors = data.errors;
                    });
            };
        });
        $scope.getProducedQuantityByPart = function(partNum) {
            jobServiceAjax.getPartProducedQuantity($scope.order.orderId, partNum).then(function(data) {
                $scope.partQuantityProduced = data;
            });
        };
        $scope.closeModal = function() {
            // $scope.closeStatusSSE();
            $uibModalInstance.dismiss();
        };

        $scope.refreshOrderPart = function(id) {
            partServiceAjax.getPartById(id).then(function(data) {
                //$scope.order.parts = [id];
                $scope.title = data.title;
                $scope.order.orderPart.part.partNum = data.partNum;
                $scope.order.orderPart.part.isbn = data.isbn;
                //$scope.order.parts.repeatSelect = data.partNum;
            });
        };

        $scope.loadBookOptions = function() {
            partServiceAjax.getIsbnPartsForEdit().then(function(data) {
                $scope.bookOptions = data;
            });
        };
        //$scope.loadBookOptions();
        //$scope.order.orderPart.part = {
        //  repeatSelect: null,
        //availableOptions: $scope.bookOptions,
        // };

        $scope.openEditPartModal = function(id) {
            var modalInstance = $uibModal.open({
                backdrop: 'static',
                keyboard: false,
                animation: true,
                templateUrl: './views/editPartModalContent.html',
                controller: 'EditPartModalInstanceCtrl',
                scope: $scope,
                size: 'lg',
                resolve: {
                    partNum: function() {
                        return id;
                    },
                    disableFields: function() {
                        return true;
                    }
                }
            });
            modalInstance.result.then(function(result) {
                //vm.dtInstance.reloadData();
                //update the title
                //$scope.order.orderPart.isbn = result.isbn
                //$scope.loadBookOptions();
                $scope.order.orderPart.part.partNum = result.partNum;
                $scope.order.orderPart.part.isbn = result.isbn;
                $scope.title = result.title;
            }, function() {});
        };

        $scope.openAddPartModal = function() {
            var modalInstance = $uibModal.open({
                animation: true,
                backdrop: 'static',
                keyboard: false,
                templateUrl: './views/addPartModalContent.html',
                controller: 'AddPartModalInstanceCtrl',
                scope: $scope,
                size: 'lg',
                resolve: {

                }
            });
            modalInstance.result.then(function(result) {
                //vm.dtInstance.reloadData();
                //$scope.order.orderPart.isbn = result.isbn
                //$scope.loadBookOptions();
                $scope.order.orderPart.part.partNum = result.partNum;
                $scope.order.orderPart.part.isbn = result.isbn;
                $scope.title = result.title;
                $scope.pagesCount = result.pagesCount;
                $scope.openEditPartModal(result.partNum);
            }, function() {});
        };

        $scope.openSearchPartModal = function() {
            var modalInstance = $uibModal.open({
                backdrop: 'static',
                keyboard: false,
                animation: true,
                templateUrl: './views/searchPartModalContent.html',
                controller: 'SearchPartModalInstanceCtrl',
                scope: $scope,
                size: 'lg',
                resolve: {}
            });
            modalInstance.result.then(function(result) {
                //vm.dtInstance.reloadData();
                //update the title
                //$scope.order.orderPart.isbn = result.isbn
                //$scope.loadBookOptions();
                $scope.order.orderPart.part.partNum = result.partNum;
                $scope.order.orderPart.part.isbn = result.isbn;
                $scope.title = result.title;
            }, function() {});
        };

        $scope.openAddCustomerModal = function() {
            var modalInstance = $uibModal.open({
                animation: true,
                backdrop: 'static',
                keyboard: false,
                templateUrl: 'addCustomerModalContent.html',
                controller: 'AddCustomerModalInstanceCtrl',
                size: 'lg',
                scope: $scope,
                resolve: {

                }
            });
            modalInstance.result.then(function(result) {
                $scope.loadCustomerOptions();
                $scope.order.customer.email = result.email;
                //$scope.$apply()
                //$scope.$evalAsync($scope.loadCustomerOptions());
            }, function() {});
        };



        // SSE Service for Status automatic change
        $scope.getSatusSSE = function() {
            var obj = {
                a: 0,
                b: 1,
                c: 2,
                d: 3
            };
            $.each(obj, function(key, value) {
                $('#order-' + value)
                    .removeClass("complete")
                    .removeClass("active");
            });
            /*$scope.orderStatusSource.onmessage = function(event) {
            	var receivedOrderId = event.data.slice(2,event.data.indexOf(",")-1);
            	var receivedOrderStatus = event.data.slice(event.data.indexOf(",")+2,event.data.length-2);
            	if (receivedOrderId === orderId)
            		updateStatus(receivedOrderStatus);
            }*/
        };

        function updateStatus(status) {
            var obj = {
                a: 0,
                b: 1,
                c: 2,
                d: 3
            };
            $.each(obj, function(key, value) {
                if ($('#order-' + value).html() === status)
                    $('#order-' + value).removeClass("complete")
                    .addClass("active");
                else
                    $('#order-' + value).removeClass("complete")
                    .removeClass("active").addClass(
                        "complete");
            });
        }

        // SSE Service for action status automatic change
        $scope.getActionSSE = function() {

            var obj = {
                a: 0,
                b: 1,
                c: 2,
                d: 3,
                e: 4
            };
            $.each(obj, function(key, value) {
                $('#file-' + value)
                    .removeClass("complete")
                    .removeClass("active");
            });
            /*$scope.progressSource.onmessage = function(event) {
            	updateAction(event.data);
            }*/
        };

        function updateAction(status) {
            var obj = {
                a: 0,
                b: 1,
                c: 2,
                d: 3,
                e: 4
            };
            $.each(obj, function(key, value) {
                if ($('#file-' + value).html() === status)
                    $('#file-' + value).removeClass("complete")
                    .addClass("active");
                else
                    $('#file-' + value).removeClass("complete")
                    .removeClass("active").addClass(
                        "complete");
            });
        }

        /*$scope.getSatusSSE();
		        $scope.getActionSSE();*/

        //sse management 
        notificationService.subscribeToApp();
        $scope.$on('$destroy', function() {
            $log.log("leaving OverviewditOrderModalInstanceCtrl controller, unsubscribe from sse");
            notificationService.getPubSub().unsubscribe(SSE_CONSTANTS.appEventsTopic);
            notificationService.unsubscribeFromApp();
        });
        //*****************************************************************************

        $scope.pushChangeInfoSse = function() {
            notificationService.getPubSub().subscribe(SSE_CONSTANTS.appEventsTopic, function(event) {
                var eventData = JSON.parse(event.data);
                if (eventData.target == 'WFSProgress' && eventData.error == false) {
                    updateAction(eventData);
                }
                if (eventData.target == 'OrderStatus' && eventData.error == false && eventData.indexOf(",") > -1) {
                    var receivedOrderId = eventData.slice(2, eventData.indexOf(",") - 1);
                    var receivedOrderStatus = eventData.slice(eventData.indexOf(",") + 2, eventData.length - 2);
                    if (receivedOrderId === orderId)
                        updateStatus(receivedOrderStatus);
                }
                //$scope.$apply();
            })
        };
        $scope.pushChangeInfoSse();

        $scope.reRunWorkFlows = function(orderId) {
            SweetAlert.swal({
                    title: $translate.instant('OVERVIEWORDER.RERUNWORKFLOW'),
                    text: $translate.instant('OVERVIEWORDER.RERUNWORKFLOW') + "?",
                    type: "warning",
                    showCancelButton: true,
                    confirmButtonColor: "#DD6B55",
                    closeOnConfirm: true
                },
                function(isConfirm) {
                    if (isConfirm) {
                        orderServiceAjax.AllWorkFlows(orderId).then(function(data) {
                            //SweetAlert.swal($translate.instant('OVERVIEWORDER.RERUNWORKFLOW'),$translate.instant('OVERVIEWORDER.RERUNWORKFLOW') + " ok.", "success");
                            toasty.success({
                                title: $translate.instant('OVERVIEWORDER.RERUNWORKFLOW'),
                                msg: $translate.instant('OVERVIEWORDER.RERUNWORKFLOW') + " ok.",
                                showClose: true,
                                clickToClose: true,
                                timeout: 5000,
                                sound: false,
                                html: false,
                                shake: false,
                                theme: "bootstrap"
                            });
                        }, function(data, status, headers, config) {
                            // $scope.errorMsg = true;
                            //  $scope.successMsg = false;
                            // $scope.alertMsg = data.errors.errors;
                            SweetAlert.swal($translate.instant('OVERVIEWORDER.RERUNWORKFLOW'), data.data.errors.errors, "error");
                        });
                    }
                });
        }

    });



angular.module('capApp')
    .controller('EditOrderModalInstanceCtrl', function($rootScope, $scope, $controller, $uibModalInstance, $uibModal, orderServiceAjax, customerServiceAjax, partServiceAjax, jobServiceAjax, lookupServiceAjax, orderId, toasty, $translatePartialLoader, $translate, $localStorage, SweetAlert) {

        $controller('CRUDOrderParentInstanceCtrl', { $scope: $scope });

        $.fn.dataTable.ext.errMode = 'none';
        var token = $localStorage.oauthToken;

        var alertUpdateMessage = $translate.instant('ORDERS_JS.UPDATED_ORDER') + "!!";

        $scope.order = jQuery.extend({}, $scope.defaultOrder);
        $scope.title = "";
        $scope.errors = jQuery.extend({}, $scope.defaultOrder);
        $scope.$parent.successMsg = false;
        $scope.$parent.errorMsg = false;
        $scope.$parent.warningMsg = false;
        $scope.errorMsg = false;
        $scope.disableOrderFields = false;

        $scope.unlockOrderData = function() {
            $scope.disableOrderFields = false;
        };
        jobServiceAjax.getOrderProducedQuantity(orderId).then(function(data2) {
            $scope.orderQuantityProduced = data2;
        });
        orderServiceAjax.getOrderById(orderId).then(function(data) {
            $scope.order = data;
            $scope.title = "";
            if (data.orderPart != null && data.orderPart.part != null) {
                $scope.title = data.orderPart.part.title;
                //$scope.order.orderPart.part.partNum = data.orderPart.part.partNum;
                //$scope.order.orderPart.part.isbn = data.orderPart.part.isbn;
            }
            $scope.dueDate = new Date(data.dueDate);

            if ($scope.order.status == 'ACCEPTED' || $scope.order.status == 'ONPROD' || $scope.order.status == 'COMPLETE') {
                $scope.disableOrderFields = true;
            }

            $scope.updateOrder = function() {
                $scope.order.dueDate = $scope.dueDate;
                /*if($scope.order.orderPart.quantityMax == null || $scope.order.orderPart.quantityMax == ''){
	  			    $scope.order.orderPart.quantityMax =  Math.ceil(Number($scope.order.orderPart.quantity) + Number($scope.order.orderPart.quantity) *
	  					Number($rootScope.overs.name) / 100);
	  		    }
	  		    if($scope.order.orderPart.quantityMin == null || $scope.order.orderPart.quantityMin == ''){
	  			    $scope.order.orderPart.quantityMin =  Math.floor(Number($scope.order.orderPart.quantity) - Number($scope.order.orderPart.quantity) *
	  					Number($rootScope.unders.name) / 100);
	  		    }*/
                if ($('#quantityMax').val() != '') {
                    $scope.order.orderPart.quantityMax = Number($('#quantityMax').val());
                }
                if ($('#quantityMin').val() != '') {
                    $scope.order.orderPart.quantityMin = Number($('#quantityMin').val());
                }
                orderServiceAjax.updateOrder($scope.order)
                    .then(function(data, status, headers, config) {
                        $scope.alertMsg = alertUpdateMessage;
                        toasty.success({
                            title: $translate.instant('ORDERS_JS.ORDER_UPDATE'),
                            msg: $translate.instant('ORDERS_JS.ORDER2') + data + $translate.instant('ORDERS_JS.UPDATED'),
                            showClose: true,
                            clickToClose: true,
                            timeout: 10000,
                            sound: false,
                            html: false,
                            shake: false,
                            theme: "bootstrap"
                        });
                        $scope.errors = jQuery.extend({}, $scope.defaultOrder);
                        $scope.$parent.alertMsg = alertUpdateMessage;
                        //$scope.$parent.successMsg = true;
                        $scope.$parent.errorMsg = false;
                        $scope.$parent.warningMsg = false;
                        $uibModalInstance.close();
                    }, function(data, status, headers, config) {
                        $scope.errorMsg = true;
                        $scope.successMsg = false;
                        $scope.warningMsg = false;
                        $scope.errors = data.data.errors.errors;
                    });
            };
        });
        $scope.closeModal = function() {
            $uibModalInstance.dismiss();
        };
        $scope.openAddOrderLineModal = function(id) {
            $scope.order.orderPart.part.partNum = "";
            $scope.order.orderPart.part.isbn = "";
            $scope.title = "";
            $scope.order.orderPart.quantity = "";
            $scope.order.orderPart.quantityMin = "";
            $scope.order.orderPart.quantityMax = "";
            var modalInstance = $uibModal.open({
                backdrop: 'static',
                keyboard: false,
                animation: true,
                templateUrl: './views/addOrderLineModal.html',
                controller: 'addOrderLineModalInstanceCtrl',
                scope: $scope,
                size: 'lg',
                resolve: {
                    partNum: function() {
                        return id;
                    }
                }
            });
            modalInstance.result.then(function(result) {
                //vm.dtInstance.reloadData();
                //update the title
                //$scope.order.orderPart.isbn = result.isbn
                //$scope.loadBookOptions();
                $scope.order.orderPart.part.partNum = result.partNum;
                $scope.order.orderPart.part.isbn = result.isbn;
                $scope.pagesCount = result.pagesCount;
                $scope.title = result.title;
            }, function() {});
        };
        $scope.refreshOrderPart = function(id) {
            partServiceAjax.getPartById(id).then(function(data) {
                //$scope.order.parts = [id];
                $scope.title = data.title;
                $scope.order.orderPart.part.partNum = data.partNum;
                $scope.order.orderPart.part.isbn = data.isbn;
                //$scope.order.parts.repeatSelect = data.partNum;
            });
        };
        $scope.removeOrderPart = function(index, quantity) {
            if ($scope.order.orderParts.length == 1) {
                SweetAlert.swal($translate.instant('js_parts.DeletionError'), $translate.instant('partsList.errorDelete'), "error");
            } else {
                SweetAlert.swal({
                        title: $translate.instant('partsList.confirmationDol'),
                        text: $translate.instant('partsList.confirmDelete'),
                        type: "warning",
                        showCancelButton: true,
                        confirmButtonColor: "#DD6B55",
                        confirmButtonText: $translate.instant('js_Scheduling.Continue'),
                        closeOnConfirm: true
                    },
                    function(isConfirm) {
                        if (isConfirm) {
                            $scope.order.orderParts.splice(index, 1);
                            $scope.order.quantity -= quantity;
                        }
                    });
            }

        }
        $scope.loadBookOptions = function() {
            partServiceAjax.getIsbnPartsForEdit().then(function(data) {
                $scope.bookOptions = data;
            });
        };
        //$scope.loadBookOptions();
        //$scope.order.orderPart.part = {
        //  repeatSelect: null,
        //availableOptions: $scope.bookOptions,
        // };
        $scope.openEditPartDsModal = function(orderId, ordpart, source) {
            var modalInstance = $uibModal.open({
                backdrop: 'static',
                keyboard: false,
                animation: true,
                templateUrl: './views/editPartDs.html',
                controller: 'EditPartDsModalInstanceCtrl',
                scope: $scope,
                size: 'xl',
                resolve: {
                    orderId: function() {
                        return $scope.order.orderId;
                    },
                    ordpart: function() {
                        return ordpart;
                    },
                    source: function() {
                        return source;
                    },
                    disableFields: function() {
                        return $scope.disableOrderFields;
                    }
                }
            });
            modalInstance.result.then(function() {
                //vm.dtInstance.reloadData();
                orderServiceAjax.getOrderById($scope.order.orderId).then(function(data) {
                    $scope.order = data;
                });
            }, function() {});
        };

        $scope.openEditPartModal = function(id) {
            var modalInstance = $uibModal.open({
                backdrop: 'static',
                keyboard: false,
                animation: true,
                templateUrl: './views/editPartModalContent.html',
                controller: 'EditPartModalInstanceCtrl',
                scope: $scope,
                size: 'lg',
                resolve: {
                    partNum: function() {
                        return id;
                    },
                    disableFields: function() {
                        return $scope.disableOrderFields;
                    }
                }
            });
            modalInstance.result.then(function(result) {
                //vm.dtInstance.reloadData();
                //update the title
                //$scope.order.orderPart.isbn = result.isbn
                //$scope.loadBookOptions();
                $scope.order.orderPart.part.partNum = result.partNum;
                $scope.order.orderPart.part.isbn = result.isbn;
                $scope.title = result.title;
            }, function() {});
        };

        $scope.openAddPartModal = function() {
            var modalInstance = $uibModal.open({
                animation: true,
                backdrop: 'static',
                keyboard: false,
                templateUrl: './views/addPartModalContent.html',
                controller: 'AddPartModalInstanceCtrl',
                scope: $scope,
                size: 'lg',
                resolve: {

                }
            });
            modalInstance.result.then(function(result) {
                //vm.dtInstance.reloadData();
                //$scope.order.orderPart.isbn = result.isbn
                //$scope.loadBookOptions();
                $scope.order.orderPart.part.partNum = result.partNum;
                $scope.order.orderPart.part.isbn = result.isbn;
                $scope.title = result.title;
                $scope.pagesCount = result.pagesCount;
                $scope.openEditPartModal(result.partNum);
            }, function() {});
        };

        $scope.openSearchPartModal = function() {
            var modalInstance = $uibModal.open({
                backdrop: 'static',
                keyboard: false,
                animation: true,
                templateUrl: './views/searchPartModalContent.html',
                controller: 'SearchPartModalInstanceCtrl',
                scope: $scope,
                size: 'lg',
                resolve: {}
            });
            modalInstance.result.then(function(result) {
                //vm.dtInstance.reloadData();
                //update the title
                //$scope.order.orderPart.isbn = result.isbn
                //$scope.loadBookOptions();
                $scope.order.orderPart.part.partNum = result.partNum;
                $scope.order.orderPart.part.isbn = result.isbn;
                $scope.title = result.title;
            }, function() {});
        };

        $scope.openAddCustomerModal = function() {
            var modalInstance = $uibModal.open({
                animation: true,
                backdrop: 'static',
                keyboard: false,
                templateUrl: 'addCustomerModalContent.html',
                controller: 'AddCustomerModalInstanceCtrl',
                size: 'lg',
                scope: $scope,
                resolve: {

                }
            });
            modalInstance.result.then(function(result) {
                $scope.loadCustomerOptions();
                $scope.order.customer.email = result.email;
                //$scope.$apply()
                //$scope.$evalAsync($scope.loadCustomerOptions());
            }, function() {});
        };
    });

angular.module('capApp')
    .controller('SearchPartModalInstanceCtrl', function($scope, DTOptionsBuilder, $uibModalInstance, $uibModal, partServiceAjax, lookupServiceAjax, $translatePartialLoader, $translate, $localStorage) {

        $.fn.dataTable.ext.errMode = 'none';
        var token = $localStorage.oauthToken;


        var defaultPartSearchBean = {
            "isbn": "",
            "partNumLike": "",
            "title": "",
            "version": "",
            "author": ""
        };

        $scope.partSearchBean = jQuery.extend({}, defaultPartSearchBean);
        $scope.errors = jQuery.extend({}, defaultPartSearchBean);
        $scope.searchResult = [];
        $scope.successMsg = false;
        $scope.errorMsg = false;
        $scope.warningMsg = false;

        $scope.dtOptionsForPartSearchResult = DTOptionsBuilder.fromSource().withDOM('rtip').withOption('order', []).withPaginationType('full_numbers').withDisplayLength(5).withLanguage($scope.dataTables);

        $scope.searchParts = function() {
            partServiceAjax.searchParts($scope.partSearchBean)
                .then(function(result) {
                    $scope.searchResult = result;
                    $scope.alertMsg = $translate.instant('ORDERS_JS.SEARCH_RETURNED');
                    $scope.errors = jQuery.extend({}, $scope.defaultPartSearchBean);
                    $scope.successMsg = true;
                    $scope.errorMsg = false;
                }, function(data, status, headers, config) {
                    $scope.errorMsg = true;
                    $scope.successMsg = false;
                    $scope.errors = data.errors;
                });
        };

        $scope.selectPart = function(partNum) {
            partServiceAjax.getPartById(partNum).then(function(data) {

                    $uibModalInstance.close(data);
                },
                function(data, status, headers, config) {
                    $scope.errorMsg = true;
                    $scope.successMsg = false;
                    $scope.warningMsg = false;
                    $scope.errors = data.errors;
                });
        };

        $scope.closeModal = function() {
            $uibModalInstance.dismiss();
        };

    });

angular.module('capApp')
    .controller('AddCustomerModalInstanceCtrl', function($scope, $uibModalInstance, customerServiceAjax, $translatePartialLoader, $translate, $localStorage) {

        $.fn.dataTable.ext.errMode = 'none';
        var token = $localStorage.oauthToken;

        var defaultCustomer = {
            "customerId": null,
            "firstName": "",
            "lastName": "",
            "email": "",
            "phoneNum": ""
        };

        $scope.customer = jQuery.extend({}, defaultCustomer);
        $scope.errors = jQuery.extend({}, defaultCustomer);
        $scope.successMsg = false;
        $scope.errorMsg = false;
        $scope.warningMsg = false;

        $scope.addCustomer = function() {
            customerServiceAjax.addCustomer($scope.customer)
                .then(function(data, status, headers, config) {
                    $scope.errors = jQuery.extend({}, defaultCustomer);
                    $scope.successMsg = true;
                    //$uibModalInstance.close($scope.customer.email);
                    $uibModalInstance.close($scope.customer);
                }, function(data, status, headers, config) {
                    $scope.errorMsg = true;
                    $scope.successMsg = false;
                    $scope.errors = data.errors;
                });
        };
        $scope.closeModal = function() {
            $uibModalInstance.dismiss();
        };
    });
angular.module('capApp')
    .controller('addOrderLineModalInstanceCtrl', function($scope, $uibModalInstance, customerServiceAjax, $translatePartialLoader, $translate, $localStorage) {

        $.fn.dataTable.ext.errMode = 'none';
        var token = $localStorage.oauthToken;

        var orderPart = {
            "part": {
                "partNum": "",
                "title": "",
                "isbn": ""
            },
            "quantity": "",
            "quantityMax": "",
            "quantityMin": ""
        };

        $scope.orderLineForm = {};

        $scope.successMsg = false;
        $scope.errorMsg = false;
        $scope.warningMsg = false;
        $scope.resetForm = function() {
            $scope.orderLineForm.$dirty = false;
            $scope.orderLineForm.$pristine = true;
            $scope.orderLineForm.$submitted = false;
        };

        $scope.addOrderLine = function() {
            if ($('#quantityMax').val() != '') {
                orderPart.quantityMax = Number($('#quantityMax').val());
            }
            if ($('#quantityMin').val() != '') {
                orderPart.quantityMin = Number($('#quantityMin').val());
            }
            //console.log($scope.$parent)
            orderPart.part.partNum = $scope.$parent.order.orderPart.part.partNum;
            orderPart.part.isbn = $scope.$parent.order.orderPart.part.isbn;
            orderPart.part.title = $scope.$parent.title;
            orderPart.quantity = $scope.$parent.order.orderPart.quantity;
            $scope.$parent.order.orderParts.push(orderPart);
            if ($scope.$parent.olCount == undefined)
                $scope.$parent.order.quantity += orderPart.quantity;
            else
                $scope.$parent.olCount += orderPart.quantity;
            $scope.closeModal();
            $scope.$parent.order.orderPart.part.partNum = "";
            $scope.$parent.order.orderPart.part.isbn = "";
            $scope.$parent.title = "";
            $scope.$parent.order.orderPart.quantity = "";
            $scope.$parent.order.orderPart.quantityMin = "";
            $scope.$parent.order.orderPart.quantityMax = "";
            $scope.resetForm();

        };

        $scope.closeModal = function() {
            $uibModalInstance.dismiss();
        };
    });
angular.module('capApp')
    .controller('editOrderLineModalInstanceCtrl', function($scope, count, $uibModalInstance, customerServiceAjax, $translatePartialLoader, $translate, $localStorage) {

        $.fn.dataTable.ext.errMode = 'none';
        var token = $localStorage.oauthToken;

        var orderPart = {
            "part": {
                "partNum": "",
                "title": "",
                "isbn": ""
            },
            "quantity": "",
            "quantityMax": "",
            "quantityMin": ""
        };
        $scope.count = count;
        $scope.orderLineForm = {};
        $scope.editedOrderLine = $scope.$parent.order.orderParts[count];
        $scope.papa = $scope.$parent.order.orderParts;
        $scope.successMsg = false;
        $scope.errorMsg = false;
        $scope.warningMsg = false;
        $scope.resetForm = function() {
            $scope.orderLineForm.$dirty = false;
            $scope.orderLineForm.$pristine = true;
            $scope.orderLineForm.$submitted = false;
        };

        $scope.editOrderLine = function() {
            if ($('#quantityMax').val() != '') {
                orderPart.quantityMax = Number($('#quantityMax').val());
            }
            if ($('#quantityMin').val() != '') {
                orderPart.quantityMin = Number($('#quantityMin').val());
            }
            //console.log($scope.$parent)
            orderPart.part.partNum = $scope.$parent.order.orderPart.part.partNum;
            orderPart.part.isbn = $scope.$parent.order.orderPart.part.isbn;
            orderPart.part.title = $scope.$parent.title;
            orderPart.quantity = $scope.$parent.order.orderPart.quantity;
            $scope.$parent.order.orderParts.push(orderPart);
            $scope.$parent.olCount += orderPart.quantity;
            $scope.closeModal();
            $scope.$parent.order.orderPart.part.partNum = "";
            $scope.$parent.order.orderPart.part.isbn = "";
            $scope.$parent.title = "";
            $scope.$parent.order.orderPart.quantity = "";
            $scope.$parent.order.orderPart.quantityMin = "";
            $scope.$parent.order.orderPart.quantityMax = "";
            $scope.resetForm();

        };

        $scope.closeModal = function() {
            $uibModalInstance.dismiss();
        };
    });