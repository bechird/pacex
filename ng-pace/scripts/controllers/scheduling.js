'use strict';
/**
 * @ngdoc function
 * @name capApp.controller:SchedulingCtrl
 * @description
 * # SchedulingCtrl
 * Controller of the capApp
 */
angular.module('capApp')

	.controller('SchedulingCtrl', ['$resource', 'DTOptionsBuilder', 'DTColumnBuilder', 'DTColumnDefBuilder', '$scope', '$rootScope', 'rollServiceAjax',
		'stationServiceAjax', 'machineServiceAjax', 'jobServiceAjax', 'lookupServiceAjax', '$uibModal', '$routeParams', '$route', '$timeout', 'SweetAlert',
		'$filter', 'toasty', '$translatePartialLoader', '$translate','$localStorage', 'notificationService', 'usSpinnerService', 'SSE_CONSTANTS', '$log',
		function ($resource, DTOptionsBuilder, DTColumnBuilder, DTColumnDefBuilder, $scope, $rootScope, rollServiceAjax, stationServiceAjax,
				machineServiceAjax, jobServiceAjax, lookupServiceAjax, $uibModal, $routeParams, $route, $timeout, SweetAlert, $filter, toasty,
				$translatePartialLoader, $translate, $localStorage, notificationService, usSpinnerService, SSE_CONSTANTS, $log) {
			
			/* $translatePartialLoader.addPart('scheduling');
			 $translate.refresh();
			   */
	  		$.fn.dataTable.ext.errMode = 'none';	  
	  		var token = $localStorage.oauthToken;
	  
			var vm = this;
			var monthNames = ["Jan ", $translate.instant('js_Scheduling.FEB'), "Mar ", $translate.instant('js_Scheduling.APR'), $translate.instant('js_Scheduling.MAY'), $translate.instant('js_Scheduling.JUN'), $translate.instant('js_Scheduling.JUL'), $translate.instant('js_Scheduling.AUG'), "Sep ", "Oct ", "Nov ", "Dec "];
			$scope._4C1C = $routeParams.color;
			vm._4C1C = $scope._4C1C;
			vm.paperType = "ALL";
			vm.rollWidth = "18.06";
			var todaysDate = new Date();
			var tomorrosDate = new Date(); tomorrosDate.setDate(tomorrosDate.getDate() + 1);
			var t2 = new Date(); t2.setDate(t2.getDate() + 2);
			var t3 = new Date(); t3.setDate(t3.getDate() + 3);
			var t4 = new Date(); t4.setDate(t4.getDate() + 4);
			var t5 = new Date(); t5.setDate(t5.getDate() + 5);
			var t6 = new Date(); t6.setDate(t6.getDate() + 6);
			var t7 = new Date(); t7.setDate(t7.getDate() + 7);
			var t8 = new Date(); t8.setDate(t8.getDate() + 8);
			var t9 = new Date(); t9.setDate(t9.getDate() + 9);
			var t10 = new Date(); t10.setDate(t10.getDate() + 10);
			$scope.cLabels = [$filter('date')(todaysDate, 'EEE'), $filter('date')(tomorrosDate, 'EEE'),
			$filter('date')(t2, 'EEE'), $filter('date')(t3, 'EEE'), $filter('date')(t4, 'EEE'),
			$filter('date')(t5, 'EEE'), $filter('date')(t6, 'EEE'), $filter('date')(t7, 'EEE'),
			$filter('date')(t8, 'EEE'), $filter('date')(t9, 'EEE'), $filter('date')(t10, 'EEE')];

			$scope.cSeries = [$translate.instant('js_Scheduling.Before_this_Day'), $translate.instant('js_Scheduling.Scheduled'), $translate.instant('js_Scheduling.Unscheduled'), $translate.instant('js_Scheduling.Cumulative_Capacity')];
			//$scope.colors = ['transparent', 'green', '#ff6384'];

			$scope.getHours = function (daysNeeded, cumulFlag) {
				$scope.data1CReady = false;
		     	$scope.data4CReady = false;
				$scope.data4C = []; $scope.data4C.push([]);
				$scope.data1C = []; $scope.data1C.push([]);
				//jobServiceAjax.getScheduledHours("4C", daysNeeded, cumulFlag).then(function(data){
				jobServiceAjax.getPressStationHours("4C", daysNeeded, cumulFlag, "S").then(function (data1) {
					$scope.data4C.push(data1);
					jobServiceAjax.getPressStationHours("4C", daysNeeded, cumulFlag, "U").then(function (data11) {
						$scope.data4C.push(data11);
						$scope.data4C[0][0] = 0;
						for (var i = 1; i < $scope.cLabels.length; i++) {
							$scope.data4C[0][i] = 0;
							for (var j = 0; j < i; j++) {
								$scope.data4C[0][i] = $scope.data4C[0][i] + data1[j] + data11[j];
							}
						}
						jobServiceAjax.getCapacityHours("4C", daysNeeded, 'true').then(function (data4) {
							$scope.data4C.push(data4);
							$scope.data4CReady = true;
						});
					});
				});
				//});
				//jobServiceAjax.getScheduledHours("1C", daysNeeded, cumulFlag).then(function(data2){
				jobServiceAjax.getPressStationHours("1C", daysNeeded, cumulFlag, "S").then(function (data3) {
					$scope.data1C.push(data3);
					jobServiceAjax.getPressStationHours("1C", daysNeeded, cumulFlag, "U").then(function (data31) {
						$scope.data1C.push(data31);
						$scope.data1C[0][0] = 0;
						for (var i = 1; i < $scope.cLabels.length; i++) {
							$scope.data1C[0][i] = 0;
							for (var j = 0; j < i; j++) {
								$scope.data1C[0][i] = $scope.data1C[0][i] + data3[j] + data31[j];
							}
						}
						jobServiceAjax.getCapacityHours("1C", daysNeeded, 'true').then(function (data5) {
							$scope.data1C.push(data5);
							$scope.data1CReady = true;
						});
					});
				});
				//});
			};
			$scope.getHours($scope.cLabels.length - 1, 'false');

			$scope.printerSpeed = 20000;
			machineServiceAjax.getDefaultPrinterSpeed().then(function (speed) {
				$scope.printerSpeed = speed;
			});

			$scope.cOptions = {
				responsive: true,
				//tooltipTemplate: "<%= datasetLabel %> - <%= value.toLocaleString() %>",
				tooltips: {
					callbacks: {
						label: function (valueObj) {
							return $scope.datasetOverride[valueObj.datasetIndex].label + ' ' + formatNumber(valueObj.yLabel, 2, '.', ',');
						}
					}
				},
				scales: {
					xAxes: [{
	                    /*ticks: {
	                        beginAtZero:true
	                    },*/
						stacked: true
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

			$scope.datasetOverride = [
				{
					label: $translate.instant('js_Scheduling.Before_this_Day'),
					backgroundColor: 'transparent',
					borderColor: 'grey',
					borderWidth: 0,
					type: 'bar'
				},
				{
					label: $translate.instant('js_Scheduling.Scheduled'),
					backgroundColor: 'transparent',
					borderColor: 'blue',
					borderWidth: 2,
					type: 'bar'
				},
				{
					label: $translate.instant('js_Scheduling.Unscheduled'),
					backgroundColor: 'transparent',
					borderColor: 'green',
					borderWidth: 2,
					type: 'bar'
				},
				{
					label: $translate.instant('js_Scheduling.Cumulative_Capacity'),
					backgroundColor: 'transparent',
					borderWidth: 3,
					borderColor: 'red',
					type: 'line'
				}
			];

			$scope.successMsg = false;
			$scope.errorMsg = false;
			$scope.warningMsg = false;

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

			vm.dtInstance = {};

			vm.dtOptions = DTOptionsBuilder.fromSource($rootScope.API_BASE+'/jobs/availableForScheduling/' + $scope._4C1C + '/' + vm.paperType + '?access_token=' + token)

				.withDOM('frti')
				//.withPaginationType('full_numbers')
				//.withOption('responsive', true)
				.withScroller()
				.withOption('scrollY', 400)
				//.withOption('serverSide', true)
				.withOption('order', [])
				//.withOption('rowCallback', rowCallback)
				//.withOption('dom', '<"top"i>rt<"bottom"flp><"clear">')
				.withOption('select', { style: 'multi', selector: 'tr input[type="checkbox"]' })
				.withDisplayLength(1000)
				.withColumnFilter({
					aoColumns: [
						{
							type: 'text',
							bRegex: true,		//order Id / partNum
							bSmart: true      
						}, {
							type: 'text',
							bRegex: true,		//colors
							bSmart: true
						}, {
							type: 'text',
							bRegex: true,		
							bSmart: true 	//quantity (Shortorder)
						}, {
							type: 'text',
							bRegex: true,		//paper type
							bSmart: true
						}, {
							type: 'text',
							bRegex: true,		//binding type
							bSmart: true
						}, {
							type: 'date',
							bRegex: true,		//due date
							bSmart: true
						}, {
							type: 'number'	//hours
						}, {
							type: 'text',
							bRegex: true,		//imposition schema / part length
							bSmart: true
						}, {
							type: 'text',
							bRegex: true,		//priority
							bSmart: true
						}, {
							type: 'text',
							bRegex: true,		//bindery
							bSmart: true
						}]
				})
				.withLanguage($scope.dataTables);

			vm.dtColumns = [
				DTColumnBuilder.newColumn(null).withTitle($translate.instant('js_Scheduling.Order')).notSortable().renderWith(function (data) {
					var result = data.orderId;
					if(data.orderPartsCount > 1){
						result = result + "/<br/>" + data.partNum;
					}
					return result;
				}),
				DTColumnBuilder.newColumn('jobId').withTitle('Job').notSortable(),
				DTColumnBuilder.newColumn('partColor').withTitle("<span title='Color'>Clr</span>").withOption('defaultContent', ' ').notSortable(),
				DTColumnBuilder.newColumn(null).withTitle($translate.instant('js_Scheduling.Qty')).notSortable().renderWith(function (data) {
					var result = '';
					result = data.quantityNeeded;
					if(data.jobName != null && data.jobName.indexOf('Auto_Re_Order') > -1){
						result = result + "<br/>" + "(SO)";
					}
					return result;
				}),
				DTColumnBuilder.newColumn(null).withTitle($translate.instant('js_Scheduling.Paper')).notSortable().
				renderWith(function (data) {
					var res = data.partPaperShortName;
					if (data.jobName != null && data.jobName.indexOf("_W") > -1) {
						var width = data.jobName.substring(data.jobName.lastIndexOf("_W") + 2)
						res =  res + '<br/>' + '(' + width + ')';
					}
					return res;
				}),
				DTColumnBuilder.newColumn('bindingTypeId').withTitle($translate.instant('js_Scheduling.Binding')).withOption('defaultContent', ' ').notSortable(),
				DTColumnBuilder.newColumn('dueDate').withTitle('Date').notSortable().
					renderWith(function (data) {
						var res = "_";
						if (data > 0) {
							var date = new Date(data);
							res = monthNames[date.getMonth()] + date.getDate();
						}
						return res;
					}),
				DTColumnBuilder.newColumn('hours').withTitle('Hrs').withOption('defaultContent', ' ').notSortable().
					renderWith(function (data) {
						return $filter('number')(data, 2);
					}),
				DTColumnBuilder.newColumn('impPartHeight').withTitle("<span title='Imposition/BookHeight'>Imp/Height</span>").withOption('defaultContent', ' ').notSortable(),
				DTColumnBuilder.newColumn('jobPriority.name').withTitle($translate.instant('js_Scheduling.Priority')).withOption('defaultContent', ' ').notSortable(),
				DTColumnBuilder.newColumn(null).withTitle($translate.instant('js_Scheduling.Bindery')).notSortable().renderWith(function (data) {
					var prname = 'priorityDd' + data.jobId;
					var prInput = $('#' + prname);
					var sel = "<option value='HIGH'>High</option><option value='NORMAL'>Normal</option>";
					if (data.binderyPriority != null) {
						if (data.binderyPriority.id == 'HIGH') {
							sel = "<option value='HIGH' selected>" + $translate.instant('js_Scheduling.HIGH') + "</option><option value='NORMAL'>" + $translate.instant('js_Scheduling.NORMAL') + "</option>"
						} else if (data.binderyPriority.id == 'NORMAL') {
							sel = "<option value='HIGH'>" + $translate.instant('js_Scheduling.HIGH') + "</option><option value='NORMAL' selected>" + $translate.instant('js_Scheduling.NORMAL') + "</option>"
						}
					}
					return "<select onchange=\"var $scope = angular.element(event.target).scope(); $scope.updateJobBinderyPriority('" + data.jobId + "'); $scope.$apply()\" class='form-control' name='" + prname + "' id='" + prname + "' >" + sel + "</select>";//,'"+ prInput.val()+"'
				}),
				DTColumnBuilder.newColumn(null).withClass('text-center').withTitle("<label class='checkbox checkbox-inline m-r-20'><input  id='checkboxAll' name='checkboxAll' type='checkbox' onclick='var $scope = angular.element(event.target).scope(); $scope.toggleSelectAll(this.checked); $scope.$apply()'/><i class='input-helper'></i></label>").notSortable().renderWith(function (data) {
					var ckbname = 'checkboxForAcceptance_' + data.orderId + '_' + data.jobId;
					return "<label class='checkbox checkbox-inline m-r-20'><input id='" + ckbname + "' name='" + ckbname + "' type='checkbox' onclick=\"var $scope = angular.element(event.target).scope(); $scope.toggleProduceButton(); $scope.$evalAsync()\" '><i class='input-helper'></i></label>";
				}),
				DTColumnBuilder.newColumn(null).withTitle(' ').notSortable().renderWith(function (data) {
					return "<button title='" + $translate.instant('productionDashboard_js.Split_Job') + "' " + data.jobId + " for Order  " + data.orderId + "' uib-tooltip='Split' class='btn bgm-lightgreen waves-effect' type='button' onclick=\"var $scope = angular.element(event.target).scope(); $scope.openSplitJobsModal(" + data.jobId + "," + data.orderId + ", 'A'," + (data.splitLevel + 1) + "," + data.hours + "," + data.quantityNeeded + "); $scope.$apply()\"><i class='zmdi zmdi-arrow-split'></i></button></button>";
				})
			];
			
			vm.dtColumnDefs = [];
			lookupServiceAjax.getItemById('Preference', 'USEBINDERYPRIORITY').then(function(data){
				if(data != null && data.name != null && data.name == 'false'){
					vm.dtColumnDefs= [DTColumnDefBuilder.newColumnDef(10).notVisible()];
				}
		 	});

			$scope.calculateUnscheduledHours = function (day) {
				$scope.unscheduled4CHours = 0;
				$scope.unscheduled1CHours = 0;
				jobServiceAjax.calculatePressHours("4C", day, "U").then(function (data) {
					$scope.unscheduled4CHours = data;
				});
				jobServiceAjax.calculatePressHours("1C", day, "U").then(function (data) {
					$scope.unscheduled1CHours = data;
				});
			};
			$scope.calculateUnscheduledHours("A");

			$scope.calculateScheduledHours = function (day) {
				$scope.scheduled4CHours = 0;
				$scope.scheduled1CHours = 0;
				jobServiceAjax.calculatePressHours("4C", day, "S").then(function (data) {
					$scope.scheduled4CHours = data;
				});
				jobServiceAjax.calculatePressHours("1C", day, "S").then(function (data) {
					$scope.scheduled1CHours = data;
				});
			};
			$scope.calculateScheduledHours("A");

			$scope.updateJobBinderyPriority = function (jobId) {
				jobServiceAjax.getJobById(jobId).then(function (data) {
					$scope.job = data;
					$scope.job.binderyPriority = {};
					$scope.job.binderyPriority.id = $('#priorityDd' + jobId).val();
					jobServiceAjax.updateJob($scope.job)
						.then(function () {
							//$scope.successMsg = true;
							$scope.errorMsg = false;
							$scope.warningMsg = false;
							$scope.alertMsg = $translate.instant('js_Scheduling.Order2') + $scope.job.orderId + $translate.instant('js_Scheduling.Bindery_Priority_updated') + jobId;
							toasty.success({
								title: $translate.instant('js_Scheduling.Job_Update'),
								msg: $translate.instant('js_Scheduling.Order2') + $scope.job.orderId + $translate.instant('js_Scheduling.Bindery_Priority_updated') + jobId,
								showClose: true,
								clickToClose: true,
								timeout: 10000,
								sound: false,
								html: false,
								shake: false,
								theme: "bootstrap"
							});
							vm.dtInstance.reloadData();
						});
				});
			};

			$scope.highlightedRollId = null;
			$scope.toggleHighlightedRoll = function (rollId) {
				// replaced this with  $event.stopPropagation();
				// if($scope.availableRolls.length > 0 && $scope.highlightedRollId != rollId){
				if ($scope.availableRolls.length > 0) {
					usSpinnerService.spin('spinner-1');
					$('#pacex_spinner').addClass("overlay-load");
					$scope.highlightedRollId = rollId;
					$scope.highlightedRoll = null;
					var table = $('#schedulingTable').DataTable();
					var rowId = "#rollRow" + rollId;
					for (var k = 0; k < $scope.availableRolls.length; k++) {
						if ($scope.availableRolls[k].rollId == rollId) {
							$scope.highlightedRoll = $scope.availableRolls[k];
							vm.paperType = $scope.availableRolls[k].paperType.id;
							vm.rollWidth = $scope.availableRolls[k].width;
						}
					}
					// first of all filter the jobs to only keep the ones that have same paper type as the selected roll
					// vm.dtInstance.changeData($rootScope.API_BASE+'/jobs/availableForScheduling/'+$scope._4C1C+'/'+$scope.paperType);
					//table.rows().draw();
					/*for(var l = 0; l < dts.length; l++){
						var rowIndex = table.cell($('#checkboxForAcceptance_'+dts[l].order.orderId+'_'+dts[l].jobId).closest('td')).index();
						if(angular.isDefined(rowIndex) && $scope.availableRolls[k].paperType.id != dts[l].part.paperType.id){
							table.rows(rowIndex.row).remove().draw();
						}
					}
					for(var k = 0; k < $scope.availableRolls.length; k++){
									if($scope.availableRolls[k].rollId == rollId){
									   $scope.paperType = $scope.availableRolls[k].paperType.id;
										$resource($rootScope.API_BASE+'/jobs/availableForScheduling/'+$scope._4C1C+'/'+$scope.paperType).query().$promise.then(function(jobs) {
										   // vm.dtInstance = jobs;
											//table.draw();
											vm.dtInstance.changeData(jobs);
										});
									}
									break;
								}*/
					// vm.dtInstance.rerender();
					//vm.dtInstance.reloadData();
					//table.rows().draw();


					vm.dtInstance.reloadData(function callback(json) {
						// vm.dtInstance.changeData.then(function callback(json) {
						//vm.dtInstance.rerender();

						var dts = table.rows().data();
						for (var l = 0; l < dts.length; l++) {
							var rowIndex = table.cell($('#checkboxForAcceptance_' + dts[l].orderId + '_' + dts[l].jobId).closest('td')).index();
							if (angular.isDefined(rowIndex) && $scope.highlightedRoll.paperType.id != dts[l].partPaperId) {
								table.rows(rowIndex.row).remove().draw();
							}
						}
						// remove the clicked class from the row that has it, then add the class to the new clicked rollRow
						$("div[id^='rollRow']").each(function () {
							$(this).removeClass('clicked');
						});
						//also disable all produce buttons, and un-check all checkboxes
						$('input[type="checkbox"]').each(function () {
							$(this).prop('checked', false);
						});

						table.rows().deselect();

						$(":input[id^='produceButton']").each(function () {
							$(this).attr('disabled', 'disabled');
						});

						$(rowId).addClass('clicked');

						$scope.warningMsg = true;
						$scope.successMsg = false;
						$scope.errorMsg = false;
						$scope.alertMsg = $translate.instant('js_Scheduling.Roll') + rollId + $translate.instant('js_Scheduling.selected_Orders_selected');

						/*if($scope.availableRolls.length > 0){
							  for(var k = 0; k < $scope.availableRolls.length; k++){
									 $scope.availableRolls[k].hours = 0;
									 $scope.availableRolls[k].utilization = 0;
							  }
						}*/
						if ($scope._4C1C == '4C' || $scope._4C1C == '1C') {
							rollServiceAjax.proposeJobs($scope._4C1C, vm.paperType, vm.rollWidth, $scope.highlightedRollId).then(function (data) {
								var inputs = [];
								$('input[type="checkbox"]').each(function () {
									inputs.push('#' + $(this).attr('id'));
								});
								for (var i = 0; i < data.length; i++) {
									for (var j = 0; j < inputs.length; j++) {
										if (inputs[j].lastIndexOf("_") > -1 && data[i][0] == inputs[j].substring(inputs[j].lastIndexOf("_") + 1)) {
											$(inputs[j]).click();
											break;
										}
									}
								}
								var dt = table.rows({ selected: true }).data();
								var cumulHours = 0;
								if (dt.length > 0) {
									for (var l = 0; l < dt.length; l++) {
										//re-calculate job hours based on roll width:
										for (var t = 0; t < data.length; t++) {
											if (data[t][0] == dt[l].jobId) {
												dt[l].hours = data[t][1];
												break;
											}
										}

										// TODO also display this new value in the scheduled jobs table	  

										cumulHours += dt[l].hours;
									}
								}
								$scope.highlightedRoll.hours = cumulHours;
								if ($scope.highlightedRoll.length != null && $scope.highlightedRoll.length > 0) {
									$scope.highlightedRoll.utilization = (cumulHours * $scope.printerSpeed * 100) / $scope.highlightedRoll.length;
								} else {
									$scope.highlightedRoll.utilization = 0;
								}
							});
						}

					}
						, false);
					
					usSpinnerService.stop('spinner-1');
			   	    $('#pacex_spinner').removeClass("overlay-load");
				}
				/*toasty.wait({
					title: 'roll!',
					msg: $scope.alertMsg,
					clickToClose: true,
					timeout: 10000,
				});*/
			};

			$scope.toggleSelectAll = function (checked) {
				var table = $('#schedulingTable').DataTable();
				//var ordersId = "";
				if (checked) {
					table.rows().select();
					$(':checkbox').prop('checked', true);
					//ordersId = "All Orders.";
				} else {
					table.rows().deselect();
					$(':checkbox').prop('checked', false);
				}
				$scope.toggleProduceButton();
				$scope.tableRowSelection(checked == true ? 'select' : 'deselect', table);
			};

			/*function rowCallback(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
				// Unbind first in order to avoid any duplicate handler (see https://github.com/l-lin/angular-datatables/issues/87)
				$('td', nRow).unbind('click');
				$('td', nRow).bind('click', function() {
					$scope.$apply(function() {
				   	
							   }
					});
				});
				return nRow;
			}*/
			$scope.toggleProduceButton = function () {
				var inputs = [];
				var makeItDisabled = true;
				var btnId = "#produceButton" + $scope.highlightedRollId;
				var orderIds = "";

				$('input[type="checkbox"]').each(function () {
					inputs.push('#' + $(this).attr('id'));
				});
				for (var i = 0; i < inputs.length; i++) {
					if ($(inputs[i]).is(':checked') && $scope.highlightedRollId != null) {
						$(btnId).removeAttr('disabled');
						makeItDisabled = false;
						if (inputs[i].indexOf("#checkboxForAcceptance") > -1) {
							orderIds += inputs[i].substring("#checkboxForAcceptance_".length, inputs[i].lastIndexOf("_")) + ", ";
						}
					}
				}

				var table = $('#schedulingTable').DataTable();
				table.on('select', function (e, dt, type, indexes) {
					$scope.tableRowSelection('select', table);
				});
				table.on('deselect', function (e, dt, type, indexes) {
					$scope.tableRowSelection('deselect', table);
				});

				if (makeItDisabled) {
					$(btnId).attr('disabled', 'disabled');
				}
				/*if(orderId == "All Orders."){
					  $scope.alertMsg = $scope.alertMsg.substring(0, $scope.alertMsg.indexOf(":")+1) + " " + orderId;
				}else{
					$scope.alertMsg += " " + orderId + ",";
				}*/
				if ($scope.alertMsg != null && $scope.alertMsg.indexOf(": ") > -1) {
					$scope.alertMsg = $scope.alertMsg.substring(0, $scope.alertMsg.lastIndexOf(": ") + 2) + orderIds;

				}

			};

			$scope.tableRowSelection = function (selectDeselect, table) {
				var data = table.rows({ selected: true }).data();
				var btnId = "#produceButton" + $scope.highlightedRollId;
				if ($scope.availableRolls.length > 0) {
					for (var k = 0; k < $scope.availableRolls.length; k++) {
						if ($scope.availableRolls[k].rollId == $scope.highlightedRollId) {
							var cumulHours = 0;
							if (data.length > 0) {
								for (var l = 0; l < data.length; l++) {
									cumulHours += data[l].hours;
								}
							}
							$scope.availableRolls[k].hours = cumulHours;
							if ($scope.availableRolls[k].length != null && $scope.availableRolls[k].length > 0) {
								$scope.availableRolls[k].utilization = (cumulHours * $scope.printerSpeed * 100) / $scope.availableRolls[k].length;
							} else {
								$scope.availableRolls[k].utilization = 0;
							}
							/*if(selectDeselect == 'select'){
								// If utilization exceeds 100% then disable the produce button
								if($scope.availableRolls[k].utilization > 100){
								  $(btnId).attr('disabled', 'disabled');
								}else{
								  $(btnId).removeAttr('disabled');
								}
							}else if(selectDeselect == 'deselect'){
								// If utilization is less than 100% then enable the produce button
								if($scope.availableRolls[k].utilization <= 100 && $scope.availableRolls[k].utilization > 0){
								  $(btnId).removeAttr('disabled');
								}else{
								  $(btnId).attr('disabled', 'disabled');
								}
							}*/
							if ($scope.availableRolls[k].utilization > 0) {
								$(btnId).removeAttr('disabled');
							} else {
								$(btnId).attr('disabled', 'disabled');
							}
							//if option is All, and highlighted roll color is 1C and selected job is 4C, then disable produce button
							if ($scope._4C1C == 'All') {
								if ($scope.availableRolls[k].machineId != null && $scope.availableRolls[k].machineId.indexOf('1C') > -1) {
									for (var l = 0; l < data.length; l++) {
										if (data[l].partColor == '4C') {
											$(btnId).attr('disabled', 'disabled');
										}
									}
								}
							}
							// check on paper type of roll that should be same as job paper type
							// Done with an Ajax call in the toggleHighlightedRoll method
							break;
						}
					}
				}
			};
			
			//sse management 
			notificationService.subscribeToApp();
			$scope.$on('$destroy', function() {
				$log.log("leaving SchedulingCtrl controller, unsubscribe from sse");
				notificationService.getPubSub().unsubscribe(SSE_CONSTANTS.appEventsTopic);
				notificationService.unsubscribeFromApp();
			});
			//*****************************************************************************
			
			$scope.pushPrintJobInfoSse = function () {
				notificationService.getPubSub().subscribe(SSE_CONSTANTS.appEventsTopic, function (event) {
					var eventData = JSON.parse(event.data);
					if (eventData.target == 'OrderStatus' && eventData.error == false && eventData.object.status == 'ACCEPTED') {
						vm.dtInstance.reloadData();
						$scope.loadAvailableRolls($scope._4C1C);
						//$scope.calculateUnscheduledHours("A");
						//$scope.calculateScheduledHours("A");
						//$scope.getHours($scope.cLabels.length - 1, 'false');
					}
					//if ((eventData.target == 'RollStatus' || eventData.target == 'Job') && eventData.error == false) {
						//vm.dtInstance.reloadData();
						//$scope.loadAvailableRolls($scope._4C1C);
						//$scope.calculateUnscheduledHours("A");
						//$scope.calculateScheduledHours("A");
						//$scope.getHours($scope.cLabels.length -1, 'false');
					//}
				})
			};
			$scope.pushPrintJobInfoSse();

			$scope.produceRoll = function (rollId, rollWidth, utilization) {
				$scope.highlightedRollId = rollId;
				$scope.utilization = utilization;
				$scope.rollWidth = rollWidth;
				usSpinnerService.spin('spinner-1');
				$('#pacex_spinner').addClass("overlay-load");
				stationServiceAjax.getStationById('PLOWFOLDER').then(function (pfStation) {
					$scope.pfTypes = pfStation.pfMachineTypes;
					$scope.selectedPfStationOption = 'PLOWFOLDER';
					$scope.selectedModeOption = 'STANLY';
					if($rootScope.includeStanly == 'false'){
						 $scope.selectedModeOption = 'PALETT_ONLY'; 
					}
					var table = $('#schedulingTable').DataTable();
					var dt = table.rows({ selected: true }).data();
					for (var i = 0; i < table.rows({ selected: true }).count(); i++) {
						if (dt[i].bindingTypeId == 'LOOSELEAF') {
							$scope.selectedPfStationOption = 'POPLINE';
						}
					}
					var modalInstance = $uibModal.open({
						animation: true,
						backdrop: 'static',
						keyboard: false,
						templateUrl: './views/pfStationOptionsModalContent.html',
						controller: 'pfMachineTypeModalInstanceCtrl',
						scope: $scope,
						size: 's',
						resolve: {
							rollId: function () {
								return $scope.highlightedRollId;
							},
							rollWidth: function () {
								return $scope.rollWidth;
							},
							utilization: function () {
								return $scope.utilization;
							},
							selectedPfStationOption: function () {
								return $scope.selectedPfStationOption;
							},
							selectedModeOption: function () {
								return $scope.selectedModeOption;
							}
						}
					});
					usSpinnerService.stop('spinner-1');
			   	    $('#pacex_spinner').removeClass("overlay-load");
					modalInstance.result.then(function (selectedPfStationOption, selectedModeOption) {
						$scope.selectedPfStationOption = selectedPfStationOption;
						$scope.selectedModeOption = selectedModeOption;
					}, function () { });
				});
				$scope.closeModal = function () {
					$uibModalInstance.dismiss();
				};
			};

			$scope.produceRollConfirm = function (rollId, rollWidth, utilization, selectedPfStationOption, selectedModeOption) {
				var table = $('#schedulingTable').DataTable();
				var dt = table.rows({ selected: true }).data();
				var selectedJobsForProduction = [];
				//the first value to push to the collection is the rollId.
				selectedJobsForProduction.push(rollId);
				for (var i = 0; i < table.rows({ selected: true }).count(); i++) {
					selectedJobsForProduction.push(dt[i].jobId);
				}
				if (utilization > 100) {
					SweetAlert.swal({
						title: $translate.instant('js_Scheduling.Confirm_Job_Split'),
						text: $translate.instant('js_Scheduling.Roll_Utilization_exceeds'),
						type: "warning",
						showCancelButton: true,
						confirmButtonColor: "#DD6B55",
						confirmButtonText: $translate.instant('js_Scheduling.Continue'),
						closeOnConfirm: true
					},
						function (isConfirm) {
							if (isConfirm) {
								$scope.checkTrimCutSizes(selectedJobsForProduction, rollId, rollWidth, selectedPfStationOption, selectedModeOption);
							}
						});
				} else {
					$scope.checkTrimCutSizes(selectedJobsForProduction, rollId, rollWidth, selectedPfStationOption, selectedModeOption);
				}
			};
			
			$scope.checkTrimCutSizes = function (selectedJobsForProduction, rollId, rollWidth, selectedPfStationOption, selectedModeOption) {
				rollServiceAjax.checkTrimCutSizes(rollWidth, selectedJobsForProduction).then(function (result) {
					if(result.length > 0){
						var resultString = '';
						for(var i = 0; i < result.length; i++){
							resultString = resultString + result[i] + ', '; 
						}
						SweetAlert.swal({
							title: $translate.instant('js_Scheduling.Roll_Production'),
							text: $translate.instant('js_Scheduling.Confirm_TrimCust_Size') + resultString,
							type: "warning",
							showCancelButton: true,
							confirmButtonColor: "#DD6B55",
							confirmButtonText: $translate.instant('js_Scheduling.Continue'),
							closeOnConfirm: true
						},
						function (isConfirm) {
							if (isConfirm) {
								$scope.produceRollTask(selectedJobsForProduction, rollId, rollWidth, selectedPfStationOption, selectedModeOption);
							}
						});
					}else{
						$scope.produceRollTask(selectedJobsForProduction, rollId, rollWidth, selectedPfStationOption, selectedModeOption);
					}
				});
			};

			$scope.produceRollTask = function (selectedJobsForProduction, rollId, rollWidth, selectedPfStationOption, selectedModeOption) {
				usSpinnerService.spin('spinner-1');
				$('#pacex_spinner').addClass("overlay-load");
				rollServiceAjax.produceRoll(selectedJobsForProduction, selectedPfStationOption, selectedModeOption, rollWidth).then(function (data) {
					//$scope.successMsg = true;
					$scope.errorMsg = false;
					$scope.warningMsg = false;
					selectedJobsForProduction[0] = "";
					rollId = data;
					if (rollId != 0) {
						$scope.alertMsg = $translate.instant('js_Scheduling.jobs_assigned_to_roll') + rollId + $translate.instant('js_Scheduling.for_production') + selectedJobsForProduction;
						toasty.success({
							title: $translate.instant('js_Scheduling.Roll_Production'),
							msg: $translate.instant('js_Scheduling.jobs_assigned_to_roll') + rollId + $translate.instant('js_Scheduling.for_production') + selectedJobsForProduction,
							showClose: true,
							clickToClose: true,
							timeout: 10000,
							sound: false,
							html: false,
							shake: false,
							theme: "bootstrap"
						});
						vm.dtInstance.reloadData();
						$('#checkboxAll').prop('checked', false);
						$scope.loadAvailableRolls($scope._4C1C);
						//$scope.calculateUnscheduledHours("A");
						//$scope.calculateScheduledHours("A");
						//$scope.getHours($scope.cLabels.length - 1, 'false');
					} else {
						SweetAlert.swal({
							title: $translate.instant('js_Scheduling.Roll_Production'),
							text: $translate.instant('js_Scheduling.Roll_not_produced'),
							type: "warning",
							showCancelButton: false,
							confirmButtonColor: "#DD6B55",
							confirmButtonText: $translate.instant('js_Scheduling.close'),
							closeOnConfirm: true
						});
						/*toasty.info({
							  title: $translate.instant('js_Scheduling.Roll_Production'),
							  msg:  $translate.instant('js_Scheduling.Roll_not_produced'),
							  showClose: true,
							  clickToClose: true,
							  timeout: 20000,
							  sound: false,
							  html: false,
							  shake: false,
							  theme: "bootstrap"
						  });*/
						usSpinnerService.stop('spinner-1');
				   	    $('#pacex_spinner').removeClass("overlay-load");
					}
					
				});
			};

			/*$(document).ready(function(){
				  var btnId = "#produceButton" + $scope.highlightedRollId;
		   $('input[type="checkbox"]').click(function() {
			 if ($(this).is(':checked')) {
				  $(btnId).removeAttr('disabled');
			 } else {
				 $(btnId).attr('disabled', 'disabled');
			 }
		   });
		   
	   });*/

			$scope.defaultRoll = {
				"machineId": "",
				"machineOrdering": "",
				"rollType": { "id": "" },
				"length": "",
				"width": "",
				"weight": "",
				"paperType": { "id": "" },
				"status": { "id": "" },
				"hours": "",
				"utilization": ""
			};
			$scope.roll = jQuery.extend({}, $scope.defaultRoll);

			$scope.loadAvailableRolls = function (color) {
				rollServiceAjax.getAvailableRolls(color).then(function (data) {
					var emptyRollsCounter = 0;
					$scope.allAvailableRolls = data;
					$scope.availableRolls = data;
					$scope.availablePaperTypes = [];
					$scope.filterByPaperType = 'All Paper Types';
					$scope.availableRollsMachines = [];
					$scope.emptyMachine = { "name": "", "machineType": { "id": "" } };
					/*for(var k = 0; k < 30 ; k++){
						$scope.availablePaperTypes.push('All'+k);
					}*/
					$scope.availablePaperTypes.push('ALL');
					for (var i = 0; i < $scope.availableRolls.length; i++) {
						if ($scope.availableRolls[i].rollId == null) {
							$scope.availableRolls[i].rollId = emptyRollsCounter;
							emptyRollsCounter--;
						}
						if ($scope.availablePaperTypes.indexOf($scope.availableRolls[i].paperType.name) === -1) {
							$scope.availablePaperTypes.push($scope.availableRolls[i].paperType.name);
						}
					}
					machineServiceAjax.machinesQuick().then(function (dataMachines) {
						$scope.allMachine = dataMachines;
						for (var i = 0; i < $scope.availableRolls.length; i++) {
							if ($scope.availableRolls[i].machineId != null && $scope.availableRolls[i].machineId != '') {
								for (var j = 0; j < dataMachines.length; j++) {
									if (dataMachines[j].machineId == $scope.availableRolls[i].machineId) {
										$scope.availableRollsMachines.push(dataMachines[j]);
										$scope.availableRolls[i].machine = dataMachines[j];
										break;
									}
								}
							} else {
								$scope.availableRollsMachines.push($scope.emptyMachine);
							}
						}
						
						usSpinnerService.stop('spinner-1');
				   	    $('#pacex_spinner').removeClass("overlay-load");
					});
				});
			};
			$scope.loadAvailableRolls($scope._4C1C);

			$scope.filterAvailableRolls = function (filter) {
				if (filter == 'ALL') {
					$scope.filterByPaperType = 'All Paper Types';
					$scope.availableRolls = $scope.allAvailableRolls;
				} else {
					$scope.filterByPaperType = filter;
					$scope.availableRolls = [];
					for (var i = 0; i < $scope.allAvailableRolls.length; i++) {
						if ($scope.allAvailableRolls[i].paperType.name == filter) {
							var roll = $scope.allAvailableRolls[i];
							$scope.availableRolls.push(roll);
						}
					}
				}
       
			};

			$scope.filterRollsJobsBy = function (filter) {
				$scope.successMsg = false;
				$scope.errorMsg = false;
				$scope.warningMsg = false;
				$("input[id^='scheduleButton']").each(function () {
					$(this).removeClass('active');
				});
				$('#scheduleButton' + filter).addClass('active');



				$scope._4C1C = filter;
				vm.paperType = "ALL";
				//vm.dtInstance.changeData($rootScope.API_BASE+'/jobs/availableForScheduling/'+$scope._4C1C);
				// vm.buildJobDatatable($scope._4C1C);




				//$route.updateParams({'color':$scope._4C1C});
				//$route.reload();
				vm.dtInstance.changeData($rootScope.API_BASE+'/jobs/availableForScheduling/' + $scope._4C1C + '/' + vm.paperType + '?access_token=' + token);

				$scope.loadAvailableRolls($scope._4C1C);
				$('#scheduleButton' + filter).removeAttr('disabled');
			};

			/* $scope.loadAvailableJobs = function(){
						jobServiceAjax.getAvailableJobsForScheduling().then(function(data){
							$scope.availableJobs = data;
						});
			 };
			 $scope.loadAvailableJobs();
			 */

			$scope.loadRollStatusOptions = function () {
				lookupServiceAjax.readAll('RollStatus').then(function (data) {
					$scope.rollStatusOptions = data;
				});
			};
			$scope.loadRollStatusOptions();
			$scope.rollStatus = {
				repeatSelect: null,
				availableOptions: $scope.rollStatusOptions,
			};

			$scope.loadRollTypeOptions = function () {
				lookupServiceAjax.readAll('RollType').then(function (data) {
					$scope.rollTypeOptions = data;
				});
			};
			$scope.loadRollTypeOptions();
			$scope.rollType = {
				repeatSelect: null,
				availableOptions: $scope.rollTypeOptions,
			};

			$scope.loadPaperTypeOptions = function () {
				lookupServiceAjax.readAll('PaperType').then(function (data) {
					$scope.paperTypeOptions = data;
				});
			};
			$scope.loadPaperTypeOptions();
			$scope.paperType = {
				repeatSelect: null,
				availableOptions: $scope.paperTypeOptions,
			};

			/*$scope.loadMachineOptions = function () {
				machineServiceAjax.machinesLight().then(function (data) {
					$scope.machineOptions = data;
				});
			};
			$scope.loadMachineOptions();
			$scope.rollMachine = {
				repeatSelect: null,
				availableOptions: $scope.machineOptions,
			};

			$scope.loadRollOptions = function () {
				rollServiceAjax.rolls().then(function (data) {
					$scope.rollOptions = data;
				});
			};
			$scope.loadRollOptions();
			$scope.parentRoll = {
				repeatSelect: null,
				availableOptions: $scope.rollOptions,
			};*/

			$scope.openAddRollModal = function () {
				var modalInstance = $uibModal.open({
					animation: true,
					backdrop: 'static',
					keyboard: false,
					templateUrl: './views/addRollModalContent.html',
					controller: 'AddRollModalInstanceCtrl',
					scope: $scope,
					size: 'm',
					resolve: {

					}
				});
				modalInstance.result.then(function () {
					$scope.warningMsg = false;
					$scope.loadAvailableRolls("All");
				}, function () { });
			};

			$scope.openSplitJobsModal = function (jobId, orderId, cascadeFlag, splitLevel, hours, originalQuantity) {
				var modalInstance = $uibModal.open({
					animation: true,
					backdrop: 'static',
					keyboard: false,
					templateUrl: './views/splitJobsModalContent.html',
					controller: 'SplitJobModalInstanceCtrl',
					scope: $scope,
					size: 'm',
					resolve: {
						jobId: function () {
							return jobId;
						},
						orderId: function () {
							return orderId;
						},
						cascadeFlag: function () {
							return cascadeFlag;
						},
						splitLevel: function () {
							return splitLevel;
						},
						hours: function () {
							return hours;
						},
						originalQuantity: function () {
							return originalQuantity;
						}
					}
				});
				modalInstance.result.then(function () {
					vm.dtInstance.reloadData();
				}, function () { });
			};
		}
	]);

angular.module('capApp')
	.controller('pfMachineTypeModalInstanceCtrl', function ($rootScope, $scope, $uibModalInstance, rollId, rollWidth, utilization, selectedPfStationOption, selectedModeOption, $timeout, toasty, $localStorage) {

		$.fn.dataTable.ext.errMode = 'none';
		var token = $localStorage.oauthToken;
		  
		$scope.highlightedRollId = rollId;
		$scope.utilization = utilization;
		$scope.rollWidth = rollWidth;
		$scope.selectedPfStationOption = selectedPfStationOption;
		$scope.selectedModeOption = selectedModeOption;
		 
		$scope.produceRollConfirmation = function () {
			if($rootScope.includeStanly == 'false'){
				$scope.produceRollConfirm($scope.highlightedRollId, $scope.rollWidth, $scope.utilization, $('input[name=pfOption]:checked').val(), 'PALETT_ONLY');
			}else{
				$scope.produceRollConfirm($scope.highlightedRollId, $scope.rollWidth, $scope.utilization, $('input[name=pfOption]:checked').val(), $('input[name=modeOption]:checked').val());
			}
			$uibModalInstance.close();
		};
		$scope.closeModal = function () {
			$uibModalInstance.dismiss();
		};

	});

