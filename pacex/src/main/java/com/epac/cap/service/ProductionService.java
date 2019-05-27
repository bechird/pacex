package com.epac.cap.service;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.epac.cap.om.ProductionOrderManager;
import com.epac.om.api.common.Criteria;
import com.epac.om.api.order.OrderStatus;
import com.epac.om.api.utils.LogUtils;

@Controller
@Path("/production")
public class ProductionService extends AbstractService{
	
	
	@Autowired
	private ProductionOrderManager productionManager;
	
	
	
	
	@GET
	@Path("/synchronize")
	@Produces(MediaType.APPLICATION_JSON)
	public ResponseEntity<?> synchronize(HttpServletRequest request) {
		try{

			Criteria criteria = new Criteria();
			criteria.setCount(1000);
			criteria.setDirection(true);
			criteria.setFilter(OrderStatus.PRODUCTION.name());
			criteria.setPage(0);

			criteria.setSort("timestamp");

			productionManager.getOrders(criteria);

			return ResponseEntity.ok().build();
		}catch(Exception e){
			LogUtils.error("Could not retreive ONLINE orders with type ", e);
			return ResponseEntity.badRequest().body("Could not retreive ONLINE orders");
		}
	}

}
