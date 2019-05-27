'use strict';


angular.module('capApp').factory('notificationService',  ['$localStorage', '$rootScope', '$log', 'SSE_CONSTANTS' ,
	function ($localStorage, $rootScope, $log, SSE_CONSTANTS) {
	
	var applicationSubscribersNumber = 0;
	var machinesSubscribersNumber = 0;
	
	var applicationEventSource = null;
	var machinesEventSources = [];
	
	var servicePubsub = new PubSub();
	
	var sseStation = null;
	
    return {
    			
    			getPubSub : function(){
    				return servicePubsub;
    			},
    			
    			// application sse
		    	subscribeToApp: function () {     	  
	
						$log.log("subscribeToApp...");
						applicationSubscribersNumber++;
						$log.log("applicationSubscribersNumber :" + applicationSubscribersNumber);
						
						if(angular.isUndefined(applicationEventSource) || applicationEventSource === null){
							var token = $localStorage.oauthToken;
							
							
							if(angular.isUndefined(token) || token === null){
				    			$log.warn("Can't connect to SSE without oauth2 token");
				    		}else{
				    			$log.log("appNotificationSse is null and token is NOT null");
				    			applicationEventSource = new ReconnectingEventSource($rootScope.API_BASE+"/notification?access_token=" + token);
				    			
				    			applicationEventSource.onopen = function (evt) {
				    				$log.log("applicationEventSource : sse opened");
				    			}
				    			applicationEventSource.onerror = function (evt) {
				    				$log.log("applicationEventSource : sse error :" + JSON.stringify(evt));
				    			}		
				    			
				    			applicationEventSource.addEventListener("message", function(evt) {
				    				$log.log("applicationEventSource : publishing new message...");
				    				servicePubsub.publish(SSE_CONSTANTS.appEventsTopic, evt);
				    			}, false);
				    		}
							
						
							
							
							
						}
					
		    	}, 
		    	unsubscribeFromApp: function () {     	  
		    		
					$log.log("unsubscribeFromApp...");
					applicationSubscribersNumber--;
					$log.log("applicationSubscribersNumber :" + applicationSubscribersNumber);
					
					if(applicationSubscribersNumber == 0){
						$log.log("no subscibers to the application sse, closing connection...");
						applicationEventSource.close();
						applicationEventSource = null;
					}
					if(applicationSubscribersNumber < 0){
						applicationSubscribersNumber = 0;
					}
				
		    	},
		    	//***********************************************************************************************************************************
    			// machines sse
		    	subscribeToMachines: function (station) {     	  
	
						$log.log("subscribeToMachines...");						
						sseStation = station;
						machinesSubscribersNumber++;
						$log.log("machinesSubscribersNumber :" + machinesSubscribersNumber);
						
						if(machinesEventSources.length == 0){
							var token = $localStorage.oauthToken;
							
							
							if(angular.isUndefined(token) || token === null){
				    			$log.warn("Can't connect to SSE without oauth2 token");
				    		}else{
				    			
				    			$log.log("machinesEventSources is empty and token is NOT null");
				    			var nbMachineCount = station != null ? station.machines.length : 0;
				    			
				    			 for(var i = 0; i < nbMachineCount; i++){
				    					if(station.machines[i].ipAddress != null && station.machines[i].ipAddress != ''){
				    						
				    						
				    						$log.log("Sse connecting to : " + 'http://'+ station.machines[i].fullIpAddress+'/printer/events');
				    						var printerEventSource =  new ReconnectingEventSource('http://'+ station.machines[i].fullIpAddress+'/printer/events');
				    						
				    						printerEventSource.onopen = function (evt) {
							    				$log.log("machinesEventSources : sse opened");
							    			}
				    						printerEventSource.onerror = function (evt) {
							    				$log.log("machinesEventSources : lost connection with machine :" + evt.target.url);
							    			}		
				    						
				    						printerEventSource.addEventListener("message", function(evt) {
							    				$log.log("machinesEventSources : publishing new message...");
							    				servicePubsub.publish(SSE_CONSTANTS.machinesEventsTopic, evt);
							    			}, false);
							    			
				    						machinesEventSources.push(printerEventSource);
				    						
							    			
				    					}
				    			 }
				    			
				    			
				    			
				    			
				    			
				    			
				    			
				    			
				    		}
							
						
							
							
							
						}
					
		    	}, 
		    	unsubscribeFromMachines: function () {     	  
		    		
					$log.log("unsubscribeFromMachines...");
					machinesSubscribersNumber--;
					$log.log("machinesSubscribersNumber :" + machinesSubscribersNumber);
					
					if(machinesSubscribersNumber == 0){
						$log.log("no subscibers to the machines sse, closing connection...");
						var connectionsCount = machinesEventSources.length;
						for(var i = 0; i < connectionsCount; i++){
							machinesEventSources[i].close();
							machinesEventSources[i] = null;
						}
						machinesEventSources= [];
						
					}
					
					if(machinesSubscribersNumber < 0){
						machinesSubscribersNumber = 0;
					}
				
		    	},		    	
		    	//***********************************************************************************************************************************
		    	
		    	
		    	
		      closeAll: function () {     	  
		    	  	
					
					$log.log("logout, closing all connections.....");
					if(applicationEventSource !== null){
						applicationEventSource.close();
						applicationEventSource = null;
					}
					
					var connectionsCount = machinesEventSources.length;
					if(connectionsCount > 0){				
						for(var i = 0; i < connectionsCount; i++){
							machinesEventSources[i].close();
							machinesEventSources[i] = null;
						}
						machinesEventSources= [];
						
					}
					
		      }
    
    
    
    
    };
  }]);
