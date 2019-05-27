package com.epac.cap.service;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.handler.BonLivraisonHandler;
import com.epac.cap.handler.OrderHandler;
import com.epac.cap.handler.PalletteHandler;
import com.epac.cap.model.BonLivraison;
import com.epac.cap.model.BonLivraison.blStatus;
import com.epac.cap.model.Order;
import com.epac.cap.model.OrderBl;
import com.epac.cap.model.Pallette;
import com.epac.cap.utils.LogUtils;
import com.epac.om.api.order.OrderStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@Path("/bls")
public class BonLivraisonService {

	@Autowired
	BonLivraisonHandler bonLivraisonHandler;

	@Autowired
	OrderHandler orderHandler;

	@Autowired
	PalletteHandler palletteHandler;
	private static Logger logger = Logger.getLogger(OrderHandler.class);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBls(){
		List<BonLivraison> bls = new ArrayList<BonLivraison>();
		bls = bonLivraisonHandler.fetchAllBl();
		return Response.ok(bls).build();
	}


	@GET
	@Path("/{blId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPallettes(@PathParam("blId") long blId){
		List<Pallette> pallettes = new ArrayList<Pallette>();
		BonLivraison bl = bonLivraisonHandler.fetchBl(blId);
		pallettes = bonLivraisonHandler.fetchPalletteByBL(bl.getNum());
		return Response.ok(pallettes).build();
	}

	@POST
	@Path("/{blId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response edit(@PathParam("blId") long blId,Map<String,Object> params){
		ObjectMapper mapper = new ObjectMapper();
		List<Long> pallettesId = mapper.convertValue(params.get("pallettes"), new TypeReference<List<Long>>() { });
		List<Long> newPallettesId = mapper.convertValue(params.get("newpallettes"), new TypeReference<List<Long>>() { });

		String newAdresse  = (String)params.get("newAdresse");
		
		BonLivraison bl = bonLivraisonHandler.fetchBl(blId);
		if(newAdresse != null){
			bl.setDestination(newAdresse);
			bonLivraisonHandler.update(bl);
		}
		try {
			int qty = bl.getQty();
			for(Long id : pallettesId){
				Pallette pallette = palletteHandler.read(id);

				Map<String,Object> res = orderHandler.fetchOrderByPAllette(id);
				List<Order> orders = (List<Order>)res.get("orders");
				List<Integer> books = (List<Integer>)res.get("books");
				for(int i = 0; i < orders.size(); i++){
					qty -= books.get(i);
					Order order = orders.get(i);
					List<OrderBl> ordBls = new ArrayList<>(order.getOrder_Bl());
					int index = 0;
					for(OrderBl orderbl:ordBls){
						if(orderbl.getBonLivraison().getId().equals(blId)){
							index = ordBls.indexOf(orderbl);
							break; 
						}
					}
					ordBls.remove(index);
					order.setOrder_Bl(new HashSet<>(ordBls));
					orderHandler.update(order);

					pallette.setBlNumber(null);
					palletteHandler.update(pallette);
				}
				bl.setQty(qty);
				
			}
			int newqty = bl.getQty();
			for(Long id : newPallettesId){
				Pallette pallette = palletteHandler.read(id);

				Map<String,Object> res = orderHandler.fetchOrderByPAllette(id);
				List<Order> orders = (List<Order>)res.get("orders");
				List<Integer> books = (List<Integer>)res.get("books");
				for(int i = 0; i < orders.size(); i++){
					newqty += books.get(i);
					Order order = orders.get(i);
					OrderBl ordBl = new OrderBl();
					ordBl.setBonLivraison(bl);
					ordBl.setQty(books.get(i));
					order.getOrder_Bl().add(ordBl);
					orderHandler.update(order);

					pallette.setBlNumber(bl.getNum());
					palletteHandler.update(pallette);
				}
				bl.setQty(newqty);
				bonLivraisonHandler.update(bl);
			}
			
			
		} catch (PersistenceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return Response.ok().build();
	}

	@POST
	@Path("/status/{blId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response changeStatus(@PathParam("blId") long blId){
		BonLivraison bl = bonLivraisonHandler.fetchBl(blId);
		bl.setStatus(blStatus.DELIVERED);
		List<Order> orders = bonLivraisonHandler.fetchOrdersByBl(blId);
		for(Order order : orders){
			order.setStatus(Order.OrderStatus.DELIVERED.getName());
			try {
				orderHandler.update(order);
			} catch (PersistenceException e) {
				// TODO Auto-generated catch block
				logger.error("Error occurred wehere Change status of Order : " + order.getOrderNum()+ " to DELIVERED"); 

			}
		}
		
		bonLivraisonHandler.update(bl);
		return Response.ok().build();
	}

}
