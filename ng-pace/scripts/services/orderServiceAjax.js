'use strict';

/**
 * @ngdoc service
 * @name capApp.orderServiceAjax
 * @description
 * # orderServiceAjax
 * Factory in the capApp.
 */
angular.module('capApp')
  .factory('orderServiceAjax', function ($http,$httpParamSerializer,$rootScope) {
    // Service logic
    // ...

    // Public API here
    return {
      orders: function () {
        return $http.get($rootScope.API_BASE+"/orders").then(function(response){return response.data});
      },
      addOrder: function(order){
    	  return $http.post($rootScope.API_BASE+"/orders/",order).then(function(response){return response.data});
      },
      updateOrder: function(order){
        return $http.put($rootScope.API_BASE+"/orders/",order).then(function(response){return response.data});
      },      
      updateOrderPart: function(orderPart){
        return $http.put($rootScope.API_BASE+"/orders/orderPart",orderPart).then(function(response){return response.data});
      },
      deleteOrder: function(id){
          return $http.delete($rootScope.API_BASE+"/orders/"+id).then(function(response){return response.data});
      },
      getOrderById: function(id){
        return $http.get($rootScope.API_BASE+"/orders/"+id).then(function(response){return response.data});
      },
      getOrderByOrderNum: function(id){
          return $http.get($rootScope.API_BASE+"/orders/byNum/"+id).then(function(response){return response.data});
        },
      getOrderOverviewById: function(id){
          return $http.get($rootScope.API_BASE+"/orders/overview/"+id).then(function(response){return response.data});
        },
      acceptOrders: function(listoforderids){
    	  return $http.post($rootScope.API_BASE+"/orders/toAccept",listoforderids).then(function(response){return response.data});
      },
      finishingOrder :function(){
      return $http.get($rootScope.API_BASE+"/orders/finishing").then(function(response){return response.data});
      },
      completeStatus: function(id){
    	  return $http.get($rootScope.API_BASE+"/orders/completeStatus/"+id).then(function(response){return response.data});
      },
      errorStatusCount: function(){
    	  return $http.get($rootScope.API_BASE+"/orders/errorStatusCount").then(function(response){return response.data});
      },
      downloadBl: function(orders,quantities,palletteIds){
    	  return $http.post($rootScope.API_BASE+"/orders/Bl/",{orders,quantities,palletteIds},{ responseType: 'arraybuffer' }).then(function(response){return response.data});
      },
      qtyInPalletteByOrder: function(orderNum){
    	  return $http.get($rootScope.API_BASE+"/orders/qtyInPallete/"+orderNum).then(function(response){return response.data});
      },
      printBl: function(blId){
    	  return $http.get($rootScope.API_BASE+"/orders/Bl/"+blId,{ responseType: 'arraybuffer' }).then(function(response){return response.data});
      },
      workFlow: function(partNum, orderId, source){
    	  return $http.get($rootScope.API_BASE+"/orders/workflow/"+partNum+"/"+orderId+"/"+source).then(function(response){return response.data});
      },
      AllWorkFlows: function(orderId){
    	  return $http.get($rootScope.API_BASE+"/orders/workflows/"+orderId).then(function(response){return response.data});
      },
      orderInPallette : function(){
        return $http.get($rootScope.API_BASE+"/orders/inPallets/").then(function(response){return response.data});
      },
      palletteByOrder : function(orderId){
        return $http.get($rootScope.API_BASE+"/orders/pallets/"+orderId).then(function(response){return response.data});
      },
      downloadTodaySelectedBl: function(selectedPallettes){
    	  return $http.post($rootScope.API_BASE+"/orders/downloadTodaySelectedBl",{selectedPallettes},{ responseType: 'arraybuffer' }).then(function(response){return response.data});
      },
      getOrderPartDs: function(partNum){
          return $http.get($rootScope.API_BASE+"/orders/orderPartDs/"+partNum).then(function(response){return response.data});
      }
    };
  });
