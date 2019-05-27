package com.epac.cap.handler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.springframework.stereotype.Component;

import com.epac.cap.utils.LogUtils;

import jp.co.fujifilm.xmf.oc.Printer;

@Component
public class PrintersHandler {

	Map<String, Printer> printers = new HashMap<>();
	
	
	public Printer getPrinter(String machineId) {
		return printers.get(machineId);
	}
	
	public void addPrinter(String machineId, Printer printer){
		if(printers.containsKey(machineId))
			return;
		LogUtils.debug("Printer listener is about to start...");
		printer.startListener();
		printers.put(machineId, printer);
		LogUtils.debug("Printer added for machine "+machineId);
	}
	
	public void shutdownPrinter(String machineId){
		if(!printers.containsKey(machineId))
			return;
		LogUtils.debug("Printer ["+machineId+"] is being shut down");
		Printer p = printers.remove(machineId);
		p.shutdown();
	}
	
	@PreDestroy
	public void shutdownPrinters() {
		LogUtils.debug("Predestroy component, shutting down all printers...");
		Collection<Printer> printersCollection = printers.values();
		for(Printer printer: printersCollection)
			printer.shutdown();
	}
}
