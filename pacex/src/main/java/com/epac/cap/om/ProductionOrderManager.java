package com.epac.cap.om;

import java.io.StringReader;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.config.ConfigurationConstants;
import com.epac.cap.handler.OrderHandler;
import com.epac.om.api.common.Criteria;
import com.epac.om.api.common.Notification;
import com.epac.om.api.order.OnlineOrder;
import com.epac.om.api.order.ProductionOrderNotification;
import com.epac.om.api.production.ProductionOrder;
import com.epac.om.api.production.ProductionOrder.ProductionOrderType;
import com.epac.om.api.utils.LogUtils;

@Component
public class ProductionOrderManager{
	
	@Autowired
	private ProductionClient productionClient;
	
	private Executor executor = Executors.newCachedThreadPool();
	
	@Autowired
	EsprintToPaceX esprintToPaceX;
	
	@Autowired
	private OrderHandler orderHandler;
	
	private final String[] API_PACKAGES = { OnlineOrder.class.getPackage().getName() };

	
	public Map<String, Object> updateJobInOm(long id, Map<String, Object> mapToSend, String token) {

		String resource = "job/" + id;

		Map<String, Object> map = null;
		try {
			map = productionClient.post(mapToSend, resource, token);
		} catch (Exception e) {
			LogUtils.error("Authentication error:", e);
		}
		return map;

	}


	public synchronized void doHandleNotification(Notification n, String token) {
		LogUtils.debug("Received Notification : " + n.getClass().getSimpleName());
		if (n instanceof ProductionOrderNotification) {
			ProductionOrderNotification notification = (ProductionOrderNotification) n;
			ProductionOrder po = notification.getProductionOrder();
			Object t = po.getType();
			ProductionOrderType type = (ProductionOrderType) t;
			String orderTypes = System.getProperty(ConfigurationConstants.Order_Type);
			String[] types = orderTypes.split(";");
			boolean receive = false;
			if(types.length > 0 ){
				for(String ty : types){
					if(type.toString().equals(ty)){
						receive = true;
						break;
					}
				}
			}
			if(receive){
				LogUtils.debug(" Received order  type " + po.getType());
				Runnable task = new Runnable() {
					@Override
					public void run() {
						try {
							esprintToPaceX.productionOrderToOrder(po, token);
						} catch (Exception e) {
							LogUtils.error("", e);
						}
					}
				};
				executor.execute(task);
				
			}
		}	
			
	}

	
	public void getOrders(Criteria criteria) {		
		
		Set<ProductionOrder> gotOrders = null;
		try {
			gotOrders = productionClient.getOrders(criteria, null);
		} catch (Exception e1) {
			LogUtils.error("error:", e1);
		}
		if(gotOrders == null)return;
		for (ProductionOrder po : gotOrders) {
			try {
				if(orderHandler.readByOrderNum(po.getOrder())== null){
					esprintToPaceX.productionOrderToOrder(po, null);
				}
			} catch (PersistenceException e) {
				LogUtils.error("error:", e);
			}
		}
	}
	
	
	public Notification resolveNotification(String message) {
		
		LogUtils.debug("Resolving notification: " + message);
		 
		try {		
			
			InputSource inputSource = new InputSource(new StringReader(message));

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();

			Document document = db.parse(inputSource);
			String rootTag = document.getDocumentElement().getNodeName();

			String firstChar = rootTag.charAt(0) + "";

			rootTag = rootTag.replaceFirst(firstChar, firstChar.toUpperCase());
			LogUtils.debug("Notification has type: " + rootTag);
			Class<?> t = null;
			String type = null;
			for (int i = 0; i < API_PACKAGES.length; i++) {

				try {
					type = API_PACKAGES[i].concat(".").concat(rootTag);
					t = Class.forName(type);
					break;
				} catch (Exception e) {
				}

			}

			JAXBContext jaxbContext = JAXBContext.newInstance(t);
			Unmarshaller jaxbMarshaller = jaxbContext.createUnmarshaller();
			LogUtils.debug("Unmarchall notification of type: " + t.getName());

			ProductionOrderNotification notification = (ProductionOrderNotification) jaxbMarshaller
					.unmarshal(new StringReader(message));

			return notification;
		} catch (Exception e) {
			LogUtils.error("Error occured while resolving notification", e);
			throw new RuntimeException("Could not parse XML message: " + message, e);
		}
	}
}
