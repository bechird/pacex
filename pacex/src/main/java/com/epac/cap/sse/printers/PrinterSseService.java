package com.epac.cap.sse.printers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.epac.cap.sse.interfaces.TokenGetter;
import com.epac.cap.sse.manager.ConnectionDetails;
import com.epac.cap.sse.manager.SseManager;
import com.epac.om.api.utils.LogUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import jp.co.fujifilm.xmf.oc.model.Event;

@Service
public class PrinterSseService {
	
	@Autowired
	private  SseManager sseManager;
	
	private Map<String, jp.co.fujifilm.xmf.oc.EventListener> map = new HashMap<String, jp.co.fujifilm.xmf.oc.EventListener>();
	
	
    public void connect(String service, String printerName, jp.co.fujifilm.xmf.oc.EventListener listener) {
    	
    	service = service.replaceFirst("(.*)/$", "$1").concat("/printer/events");
    	printerName = "Printer " + printerName;
    	
    	map.put(printerName, listener);
		
		TokenGetter tokenGetter = new PrinterTokenGetter();
    	ConnectionDetails connectionDetails = new ConnectionDetails(printerName, tokenGetter, PrinterSseConnector.class, 10);
    	connectionDetails.setUrl(service);
    	sseManager.add(connectionDetails);
	}
	
	
	@EventListener
	public void onReceiveMssage(PrinterSseNotification notification) {		
		String line = notification.getPayload();
		Event event = resolveEvent(line);
		fireEvent(event, notification.getPrinterName());
	    
	}
	
	private Event resolveEvent(String jsonEvent) {
		if(jsonEvent.isEmpty())return null;

		try {
			ObjectMapper mapper = new ObjectMapper();
			SimpleModule module = new SimpleModule();
			module.addDeserializer(Event.class, new EventDeserializer(Event.class));
			mapper.registerModule(module);
			return mapper.readValue(jsonEvent, Event.class);
		} catch (Exception e) {
			//LogUtils.error("Error resolving event: "+jsonEvent);
		}
		return null;
		
	}
	
	
	private void fireEvent(Event event, String printerName) {
		
		if(map.containsKey(printerName)) {
			jp.co.fujifilm.xmf.oc.EventListener listener = map.get(printerName);

			try {				
				listener.handleEvent(event);
			} catch (Exception e) {
				LogUtils.error("Error occured while handling event: "+event.getType(), e);
			}   		
    		
    	}

	}
	
	

}
