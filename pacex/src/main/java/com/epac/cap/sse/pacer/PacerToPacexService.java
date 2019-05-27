package com.epac.cap.sse.pacer;

import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.epac.cap.handler.LookupHandler;
import com.epac.cap.handler.OrderHandler;
import com.epac.cap.model.Order;
import com.epac.cap.model.OrderPart;
import com.epac.cap.model.Part;
import com.epac.cap.model.Preference;
import com.epac.cap.sse.interfaces.TokenGetter;
import com.epac.cap.sse.manager.ConnectionDetails;
import com.epac.cap.sse.manager.SseManager;
import com.epac.cap.utils.Format;
import com.epac.om.api.utils.LogUtils;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PacerToPacexService {
	
	@Autowired
	private  SseManager sseManager;
	
	@Autowired
	private OrderHandler orderHandler;
	
	@Autowired
	private LookupHandler lookupHandler;
	
	private final String UNIT_FR = "FR";
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@PostConstruct
    public void init() {
		
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		
		TokenGetter tokenGetter = new PacerTokenGetter();
    	ConnectionDetails connectionDetails = new ConnectionDetails("Pacer", tokenGetter, PacerSseConnector.class, 12);
    	sseManager.add(connectionDetails);
	}
	
	@EventListener
	public void onReceiveMssage(PacerSseNotification notification) {
		
		Preference unitValue = lookupHandler.getLookupDAO().read("UNITSYSTEM", Preference.class);
	    LogUtils.debug("unitValue:" + unitValue );
	    
		String line = notification.getPayload();
		
		LogUtils.debug("received order from PACER:" + line );			    
	    
	    JsonNode pacexOrderNode;
		Order order = null;
		try {
			pacexOrderNode = objectMapper.readTree(line);			
			//order
			order = objectMapper.treeToValue(pacexOrderNode, Order.class);
			
			//width length conversion			
			Set<OrderPart> orderParts = order.getOrderParts();
			
			for(OrderPart op : orderParts ) {
				Part part = op.getPart();			 
				    
			    if(UNIT_FR.equals(unitValue.getName())){
			    	float cengagaWidth = part.getWidth();
			    	cengagaWidth = Format.inch2mm(cengagaWidth);
			    	part.setWidth(cengagaWidth);
			    	
			    	float cengageLength = part.getLength();
			    	cengageLength = Format.inch2mm(cengageLength);
			    	part.setLength(cengageLength);		
			    	
				}
			    
			    
				    
			}
		   
		    
		} catch (Exception e) {
			LogUtils.error("error parsing PACER order, exiting...");
			return;
		} 

		//----------------------------------------------------------------------------------------------------------------
	    try {
			Order order_ = orderHandler.readByOrderNum(order.getOrderNum());
			if(order_ != null){
				LogUtils.debug("Order ["+order.getOrderNum()+"] already exists");
			}else {
				//partHandler.create(part);
				orderHandler.create(order);
			}
			
		} catch (Exception e) {
			LogUtils.info("error occured while saving order ["+order.getOrderNum()+"]");

		}
		    
	    
	    
	    
	    
	}
	
	

}
