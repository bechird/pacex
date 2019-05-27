'use strict';


angular.module('capApp')
.factory('palletteServiceAjax', function ($http,$httpParamSerializer,$rootScope) {
  // Service logic
  // ...

  // Public API here
  return {
    activePallette: function (machineId) {
      return $http.get($rootScope.API_BASE+"/pallets/active/"+machineId,{
              'Content-Type' : 'application/json; charset=UTF-8'
              }).then(function(response){return response.data});
    },
    updatePackageBook: function (pcb,qty) {
      return $http.post($rootScope.API_BASE+"/pcbs/update/"+qty,pcb).then(function(response){return response.data});
    },
    updateQtyPalletteBook : function(palletId,packageBookId,qty){
    	return $http.post($rootScope.API_BASE+"/pallets/update/qtyPcb/"+palletId,{packageBookId,qty}).then(function(response){return response.data});
    }, 
    addPallette : function(type,machineId,dest){
    	return $http.post($rootScope.API_BASE+"/pallets/add/",{type,machineId,dest}).then(function(response){return response.data});
    },
    closePallette : function(palletteId){
    	return $http.get($rootScope.API_BASE+"/pallets/closePallette/"+palletteId).then(function(response){return response.data});
    },
    calcQtyOrderInPallette : function(palletteId,orderId){
    	return $http.get($rootScope.API_BASE+"/pallets/qtyOrder/"+palletteId+"/"+orderId).then(function(response){return response.data});
    },
    changeStatusPcb : function(packageBookId){
    	return $http.get($rootScope.API_BASE+"/pcbs/updateStatus/"+packageBookId).then(function(response){return response.data});
    },
    palletteByMachine : function(machineId){
    	return $http.get($rootScope.API_BASE+"/pallets/pausedByMachine/"+machineId).then(function(response){return response.data});
    },
    qtyPcbByPallette : function(palletteId){
    	return $http.get($rootScope.API_BASE+"/pallets/qtyPcbPallette/"+palletteId).then(function(response){return response.data});
    },
    qtyLivreByPallette : function(palletteId){
    	return $http.get($rootScope.API_BASE+"/pallets/qtyLivrePallette/"+palletteId).then(function(response){return response.data});
    },
    allPalletteNotActive : function(){
    	return $http.get($rootScope.API_BASE+"/pallets/slips").then(function(response){return response.data});
    },
    updatePalletteStatus : function(palletteid){
    	return $http.post($rootScope.API_BASE+"/pallets/update/status/"+palletteid).then(function(response){return response.data});
    },
    fetchPalletteInfo : function(palletteid){
    	return $http.get($rootScope.API_BASE+"/pallets/slips/info/"+palletteid).then(function(response){return response.data});
    },
    fetchOrderByPallette : function(palletteid){
    	return $http.get($rootScope.API_BASE+"/pallets/order/"+palletteid).then(function(response){return response.data});
    },
    downloadPackagingSlip : function(palletteId){
      return $http.get($rootScope.API_BASE+"/pallets/packSlip/"+palletteId,{ responseType: 'arraybuffer' }).then(function(response){return response.data});
    },
    updateDestination : function(palletteId,destination){
      return $http.post($rootScope.API_BASE+"/pallets/updateDestination/"+palletteId,destination).then(function(response){return response.data});
    },
    updateLeftOver : function(palletteId,quantity,type,orderId){
      return $http.post($rootScope.API_BASE+"/pallets/updateLeftOver/"+palletteId,{type,quantity,orderId}).then(function(response){return response.data});
    },
    fetchPackByPcb : function(pcbId){
      return $http.get($rootScope.API_BASE+"/packages/packageByPcb/"+pcbId).then(function(response){return response.data});
    },
    qtyShippedByPcb : function(pcbId,palletteId){
      return $http.get($rootScope.API_BASE+"/pallets/alreadyShipped/"+pcbId+"/"+palletteId).then(function(response){return response.data});
    },
    pausePallette : function(palletteId,machineId){
      return $http.get($rootScope.API_BASE+"/pallets/pause/"+palletteId+"/"+machineId).then(function(response){return response.data});
    },
    resumePallette : function(palletteId,machineId){
      return $http.get($rootScope.API_BASE+"/pallets/resume/"+palletteId+"/"+machineId).then(function(response){return response.data});
    },
    orderInfoByPallette : function(palletteId){
      return $http.get($rootScope.API_BASE+"/pallets/orderInfo/"+palletteId).then(function(response){return response.data});
    },
    editQtyPalletteBook : function(palletteId,packageBookId,qty){
      return $http.post($rootScope.API_BASE+"/pallets/edit/qtyPcb/"+palletteId,{packageBookId,qty}).then(function(response){return response.data});
    },
    orderShipped: function(orderNum,palletteId){
      return $http.get($rootScope.API_BASE+"/pallets/ShippedOrder/"+orderNum+"/"+palletteId).then(function(response){return response.data});
    },
    orderByPcb: function(pcbId){
      return $http.get($rootScope.API_BASE+"/pcbs/order/"+pcbId).then(function(response){return response.data});
    },
    deletePallette: function(palletteId){
      return $http.get($rootScope.API_BASE+"/pallets/delete/"+palletteId).then(function(response){return response.data});
    },
    readPallette: function(palletteId){
      return $http.get($rootScope.API_BASE+"/pallets/readRelated/"+palletteId).then(function(response){return response.data});
    },
    saveBlRelatedPallette: function(pallette){
      return $http.post($rootScope.API_BASE+"/pallets/readRelated/save", pallette).then(function(response){return response.data});
    },
    getPallettesToShipToday: function(){
      return $http.get($rootScope.API_BASE+"/pallets/toShipToday").then(function(response){return response.data});
    }
    
  };
});
