package com.epac.cap.config;

import java.util.concurrent.CopyOnWriteArrayList;

import com.epac.cap.utils.LogUtils;

public class ServiceListener{
 
    private static final CopyOnWriteArrayList<PacexService> services = new CopyOnWriteArrayList<>();
    
    public static void addService(PacexService service){
    	services.add(service);
    }
    
    public static void shutdown() {
         for(PacexService service: services){
        	LogUtils.debug("Shutting down "+service.getClass().getSimpleName()+" instance");
         	service.shutdown();
         }
    }
 
    
}