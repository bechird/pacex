'use strict';

/**
 * @ngdoc overview
 * @name capApp
 * @description
 * # capApp
 *
 * Main module of the application.
 */

var env_service = '';
var env_security = '';

//Import variables if present (from env.js)
if(window){  
	Object.assign(env_service, window._env_service);
	Object.assign(env_security, window._env_security);
}

angular
  .module('capApp', [
    'ngAnimate',
    'ngAria',
    'ngCookies',
    'ngMessages',
    'ngResource',
    'ngRoute',
    'ngSanitize',
    'ngTouch',
    'datatables',
    'datatables.columnfilter',
    'datatables.buttons',
    'datatables.light-columnfilter',
    'datatables.select',
    'ui.tree',
    'slick',
    'xeditable',
    'ui.bootstrap',
    'ui.router',
    'daterangepicker',
    /*'ngecharts',*/
    'ui.pwgen',
    'multiStepForm',
    'mgo-angular-wizard',
    'chart.js',
//    'angular-chart.js'
    '720kb.datepicker',
    'datatables.scroller',
    'dndLists',
    'ui.sortable',
   // 'as.sortable',
    'angular-confirm',
    'checklist-model',
    'ngFileUpload',
    'ngScrollbar',
    'ngScrollable',
    'localytics.directives',
    'oitozero.ngSweetAlert',
    'matchMedia',   
    'angular-toasty',
    '720kb.datepicker',
    'moment-picker',
    'ngLoadingSpinner',
    "angucomplete-alt",
    'pascalprecht.translate',
    'ngStorage',
    '720kb.tooltips',
    'cp.ngConfirm',
    'ngUAParser'
  ])
/*.config(['$qProvider', function ($qProvider) {
  $qProvider.errorOnUnhandledRejections(false);
}])*/
  
 /* .service('envConf', function($http, _env_service, _env_security) {  
	  this.getEnvConf = function getEnvConf(){
		  var tmp = $http
		  
	  		.get('scripts/services/config/envConf.json')
	  		.then(function(response){
	    	  return response.data;
	  		});
		  return tmp;
	  };
  })*/
  //Sse pub/sub constants
  .constant('SSE_CONSTANTS', {
	    appEventsTopic: 'appEventsTopic',
	    machinesEventsTopic: 'machinesEventsTopic'
	})

  
  //Register environment in AngularJS as constant
  .constant('_env_service', env_service)
  .constant('_env_security', env_security)
  .config(['$uibTooltipProvider', function ($uibTooltipProvider) {
     var parser = new UAParser();
     var result = parser.getResult();
     var touch = result.device && (result.device.type === 'tablet' || result.device.type === 'mobile');
     if ( touch ){
         $uibTooltipProvider.options({trigger: 'dontTrigger'});
     } else {
         $uibTooltipProvider.options({trigger: 'mouseenter'});
    }
  }])
  .config( function($translateProvider, $translatePartialLoaderProvider ) {

	  $translateProvider.useLoader('$translatePartialLoader', {
		  urlTemplate: 'translations/{part}/{lang}.json'
		});
	  $translateProvider.useLoaderCache(true);
	  $translateProvider.useSanitizeValueStrategy('escape');
	  $translateProvider.fallbackLanguage('en');
	
	//  $translateProvider.determinePreferredLanguage();
	  
	//  $translatePartialLoader.addPart('shipping');
	  $translateProvider.preferredLanguage('en');
	  $translateProvider.forceAsyncReload(true);
	  
	  $translatePartialLoaderProvider.addPart('orders');
	  $translatePartialLoaderProvider.addPart('shipping');
	  $translatePartialLoaderProvider.addPart('main');
	  $translatePartialLoaderProvider.addPart('jobsList');
      $translatePartialLoaderProvider.addPart('lookups');
      $translatePartialLoaderProvider.addPart('loadTagList');
      $translatePartialLoaderProvider.addPart('logsList');
      $translatePartialLoaderProvider.addPart('machineList');
     
      $translatePartialLoaderProvider.addPart('overview');
      $translatePartialLoaderProvider.addPart('partList');
      
      $translatePartialLoaderProvider.addPart('productionDashboard');
      $translatePartialLoaderProvider.addPart('rolesList');
      $translatePartialLoaderProvider.addPart('rolls');
      $translatePartialLoaderProvider.addPart('scheduling');
      $translatePartialLoaderProvider.addPart('packagingSlipsOverview');
      $translatePartialLoaderProvider.addPart('stationList');
      $translatePartialLoaderProvider.addPart('usersList');
      $translatePartialLoaderProvider.addPart('bonList');
      $translatePartialLoaderProvider.addPart('login');	 
      $translatePartialLoaderProvider.addPart('sectionsList');
      $translatePartialLoaderProvider.addPart('prodReportsByDates');
      $translatePartialLoaderProvider.addPart('deliveryBuilder');
      $translatePartialLoaderProvider.addPart('deliveryFormGenerator');
      
      
})
  
  .config(function ($routeProvider) {
    $routeProvider
    .when('/list', {
        templateUrl: 'views/list.html',
        controller: 'ListCtrl',
        controllerAs: 'list'
      })
      .when('/add', {
        templateUrl: 'views/add.html',
        controller: 'AddCtrl',
        controllerAs: 'add'
      })
    .when('/contactslist', {
        templateUrl: 'views/contactslist.html',
        controller: 'ContactsCtrl',
        controllerAs: 'contacts'
      })  
        
    .when('/', {
        templateUrl: 'views/main.html',
        controller: 'MainCtrl',
        controllerAs: 'main'
        
      })
    .when('/home', {
        templateUrl: 'views/home.html',
        controller: 'HomeCtrl',
        controllerAs: 'home'
      })
      .when('/about', {
        templateUrl: 'views/about.html',
        controller: 'AboutCtrl',
        controllerAs: 'about'
      })
      .when('/Sidebar', {
        templateUrl: 'views/sidebar.html',
        controller: 'SidebarCtrl',
        controllerAs: 'Sidebar'
      })
      
      .when('/orders/acceptance', {
        templateUrl: 'views/order_acceptance.html',
        controller: 'OrdersAcceptanceListCtrl',
        controllerAs: 'ordersAcceptanceList'
      })
      
      .when('/orders', {
        templateUrl: 'views/ordersList.html',
        controller: 'OrdersListCtrl',
        controllerAs: 'ordersList'
      })
      
      .when('/parts', {
        templateUrl: 'views/partsList.html',
        controller: 'PartsListCtrl',
        controllerAs: 'partsList'
      })
      .when('/parts/doc/:category/:partNum', {
        templateUrl: '',
        controller: 'EditPartModalInstanceCtrl',
        controllerAs: 'editPart'
      })
      
      .when('/productionDashboard/:stationId', {
        templateUrl: 'views/productionDashboard.html',
        controller: 'ProductionDashboardCtrl',
        controllerAs: 'productionDashboard'
      })
      
      .when('/productionDashboard/handheld/:stationId', {
        templateUrl: 'views/handheldProductionDashboard.html',
        controller: 'ProductionDashboardCtrl',
        controllerAs: 'productionDashboard'
      })
      
      .when('/jobs/', {
        templateUrl: 'views/jobsList.html',
        controller: 'JobsListCtrl',
        controllerAs: 'jobsList'
      })
      
      .when('/rolls/', {
        templateUrl: 'views/rollsList.html',
        controller: 'RollsListCtrl',
        controllerAs: 'rollsList'
      })

      .when('/sections/', {
        templateUrl: 'views/sectionsList.html',
        controller: 'SectionsListCtrl',
        controllerAs: 'sectionsList'
      })

       .when('/bondelivraisonList/', {
        templateUrl: 'views/bonDeLivraisonList.html',
        controller: 'bonListCtrl',
        controllerAs: 'bonList'
      })
      
      .when('/loadTags/', {
        templateUrl: 'views/loadTagsList.html',
        controller: 'LoadTagsListCtrl',
        controllerAs: 'loadTagsList'
      })
      
      .when('/logs/', {
        templateUrl: 'views/logsList.html',
        controller: 'LogsListCtrl',
        controllerAs: 'logsList'
      })
      
      .when('/scheduling/:color', {
        templateUrl: 'views/scheduling.html',
        controller: 'SchedulingCtrl',
        controllerAs: 'scheduling'
      })
      .when('/overview', {
        templateUrl: 'views/overview.html',
        controller: 'OverviewCtrl',
        controllerAs: 'overview'
      })
      
       .when('/PRESS', {
        templateUrl: 'views/press_station.html',
        controller: 'PressStationCtrl',
        controllerAs: 'pressStation'
      })
      .when('/press-station2', {
        templateUrl: 'views/press-station2.html',
        controller: 'PressStation2Ctrl',
        controllerAs: 'pressStation2'
      })
      .when('/lookups', {
        templateUrl: 'views/lookups.html',
        controller: '',
        controllerAs: ''
      })
      .when('/lookups/:type', {
        templateUrl: 'views/lookup_details.html',
        controller: 'LookupsCtrl',
        controllerAs: 'lookups'
      })
      .when('/lookups/prefSubjects/list/:prefGroup/:clientId', {
        templateUrl: 'views/preferences.html',
        controller: 'LookupsCtrl',
        controllerAs: 'lookups'
      })
      .when('/lookups/prefSubjects/items/:prefSubject/:clientId', {
        templateUrl: 'views/lookup_details.html',
        controller: 'LookupsCtrl',
        controllerAs: 'lookups'
      })
      .when('/stations', {
        templateUrl: 'views/stationsList.html',
        controller: 'StationsListCtrl',
        controllerAs: 'stationsList'
      })
      .when('/machines', {
        templateUrl: 'views/machinesList.html',
        controller: 'MachinesListCtrl',
        controllerAs: 'machinesList'
      })
      .when('/roles', {
        templateUrl: 'views/rolesList.html',
        controller: 'RolesListCtrl',
        controllerAs: 'rolesList'
      })
      .when('/users', {
        templateUrl: 'views/usersList.html',
        controller: 'UsersListCtrl',
        controllerAs: 'usersList'
      })
      
      .when('/customers', {
        templateUrl: 'views/customersList.html',
        controller: 'CustomersListCtrl',
        controllerAs: 'customersList'
      })
       .when('/pressMachine', {
        templateUrl: 'views/pressMachineForPhone.html',
        controller: 'UsersListCtrl',
        controllerAs: 'usersList'
      })
      .when('/plowFolderMachine', {
        templateUrl: 'views/PlowFolderMachineForPhone.html',
        controller: 'UsersListCtrl',
        controllerAs: 'usersList'
      })
      .when('/shipping', {
        templateUrl: 'views/shipping-station.html',
        controller: 'ShippingStationCtrl',
        controllerAs: 'shippingstation'
      })
      .when('/slips', {
        templateUrl: 'views/packingSlipsOverview.html',
        controller: 'SlipsCtrl',
        controllerAs: 'slips'
      })
      .when('/packaging', {
        templateUrl: 'views/wrapPlastic.html',
        controller: 'ShippingStationCtrl',
        controllerAs: 'shippingstation'
      }) 
      .when('/orderDelivery', {
        templateUrl: 'views/orderToDelivred.html',
        controller: 'SlipsCtrl',
        controllerAs: 'slips'
      }) 
      .when('/productionReportsByDate', {
        templateUrl: 'views/productionReports.html',
        controller: 'ProductionReportsByDatesCtrl',
        controllerAs: 'productionReportsByDates'
      }) 
      .when('/blBuild', {
        templateUrl: 'views/deliveryBuilder.html',
        controller: 'DeliveryFormBuilderCtrl',
        controllerAs: 'deliveryFormBuilderCtrl'
      }) 
      .when('/blSelect', {
        templateUrl: 'views/deliverySelector.html',
        controller: 'DeliverySelectorCtrl',
        controllerAs: 'deliverySelectorCtrl'
      }) 
      .when('/sseServersSatus', {
        templateUrl: 'views/adminSseStatus.html',
        controller: 'AdminSseStatusCtrl',
        controllerAs: 'adminSseStatusCtrl'
      }) 
      .when('/login', {
        templateUrl: 'views/login.html',
        controller: 'LoginCtrl',
        controllerAs: 'logins'
      }) 
      .otherwise({
        redirectTo: '/'
      });
  })



.run(function ($rootScope, $translate , userServiceAjax, lookupServiceAjax, stationServiceAjax, $location, usSpinnerService) {
  $rootScope.API_BASE = configData.Api_Base;
  $rootScope.MODE = configData.Mode;
  $rootScope.API_SECURITY = configData.Api_Security;
  $rootScope.$on('$translatePartialLoaderStructureChanged', function () {
    $translate.refresh();
  });
  
  $rootScope.$on('$routeChangeStart', function (event, next, current){
	  usSpinnerService.spin('spinner-1');
	  $('#pacex_spinner').addClass("overlay-load");
	   		if(angular.isUndefined($rootScope.loggedInUser) || $rootScope.loggedInUser === null){	   			
	   			userServiceAjax.getLoggedInUserId().then(function(data){
					 $rootScope.loggedInUserName = data.firstName + ' '+ data.lastName;
					 $rootScope.loggedInUserId = data.loginName;
					 $rootScope.loggedInUser = data;
					 
					 if(angular.isUndefined($rootScope.overs)){
					  		lookupServiceAjax.getItemById('Preference', 'OVERS').then(function(data){
					  			$rootScope.overs = data;
					  		});
			  			}
			  			if(angular.isUndefined($rootScope.unders)){
					  		lookupServiceAjax.getItemById('Preference', 'UNDERS').then(function(data){
					  			$rootScope.unders = data;
					  		});
			  			}
			  			if(angular.isUndefined($rootScope.oversAdditif)){
					  		lookupServiceAjax.getItemById('Preference', 'OVERSADDITIF').then(function(data){
					  			$rootScope.oversAdditif = data;
					  		});
			  			}
			  			if(angular.isUndefined($rootScope.undersAdditif)){
					  		lookupServiceAjax.getItemById('Preference', 'UNDERSADDITIF').then(function(data){
					  			$rootScope.undersAdditif = data;
					  		});
			  			}
			  			if(angular.isUndefined($rootScope.unitSystem)){
					  		lookupServiceAjax.getItemById('Preference', 'UNITSYSTEM').then(function(data){
					  			if(data != null){
					  				$rootScope.unitSystem = data.name;
					  			}else{
					  				$rootScope.unitSystem = "US";
					  			}
					  		});
			  			}
			  			if(angular.isUndefined($rootScope.dateFormat)){
					  		lookupServiceAjax.getItemById('Preference', 'DATE_FORMAT').then(function(data){
					  			if(data != null && data != ''){
					  				$rootScope.dateFormat = data.name;
					  			}
					  			/*else if($rootScope.loggedInUser.language == 'en'){
					  				$rootScope.dateFormat = "dd MM";
					  			}else if($rootScope.loggedInUser.language == 'fr'){
					  				$rootScope.dateFormat = "dd MM";
					  			}else if($rootScope.loggedInUser.language == 'es'){
					  				$rootScope.dateFormat = "dd MM";
					  			}*/
					  		});
			  			}
			  			if(angular.isUndefined($rootScope.facility)){
					  		lookupServiceAjax.getItemById('Preference', 'FACILITY').then(function(data){
					  			$rootScope.facility = data.name;
					  		});
			  			}
			  			if(angular.isUndefined($rootScope.aClient)){
			  				lookupServiceAjax.readAll('Client').then(function(data) {
				                $rootScope.aClient = data[0].id;
				            });
			  			}
			  			
			  			if(angular.isUndefined($rootScope.includeShipping)){
					  		lookupServiceAjax.getItemById('Preference', 'INCLUDESHIPPING').then(function(data){
					  			$rootScope.includeShipping = data.name;
					  		});
			  			}
			  			if(angular.isUndefined($rootScope.includeStanly)){
					  		lookupServiceAjax.getItemById('Preference', 'INCLUDESTANLY').then(function(data){
					  			$rootScope.includeStanly = data.name;
					  		});
			  			}
			  			if(angular.isUndefined($rootScope.stationsMenuOptions)){
			  				stationServiceAjax.stationsMenu().then(function(data){
			  					$rootScope.stationsMenuOptions = data;
			  		      });
			  			}
					 $translate.use(data.language);
					 $translate.refresh();
				 }, function(error){});	

	   		}
   });
  
})

  
  //.run(
	//function ($rootScope) {
	//	$rootScope.user = null;

	//	$rootScope.$on("angularFireAuth:login", function (evt, user) {
	//	     $rootScope.user = user;

	//    });
	//})
	.directive('convertToNumber', function() {
	  return {
	    require: 'ngModel',
	    link: function(scope, element, attrs, ngModel) {
	      ngModel.$parsers.push(function(val) {
	        return parseInt(val, 10);
	      });
	      ngModel.$formatters.push(function(val) {
	        return '' + val;
	      });
	    }
	  };
	});
	
