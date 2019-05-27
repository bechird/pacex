package com.epac.cap.repository;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.epac.cap.model.BonLivraison;
import com.epac.cap.model.BonLivraison.blStatus;
import com.epac.cap.model.Order;
import com.epac.cap.model.OrderBl;
import com.epac.cap.model.Pallette;


@Repository
public class BonLivraisonDao extends BaseEntityPersister{
	
	 public Integer fetchMaxCountOf(){
		 String sql ="SELECT MAX(num) FROM deliveryNote";

			Query query = getEntityManager().createNativeQuery(sql);
			return (Integer) query.getSingleResult();
	 }
	 public void create(BonLivraison bean) {
			getEntityManager().persist(bean);
			getEntityManager().flush();
		}
	 public void update(BonLivraison bean) {
			getEntityManager().merge(bean);
			getEntityManager().flush();
		}

	 public BonLivraison fetchBl(long id ){
		 return getEntityManager().find(BonLivraison.class,id);
	 }
	public void create(OrderBl oderBl) {
		getEntityManager().persist(oderBl);
		getEntityManager().flush();		
	}
	
	 @SuppressWarnings("unchecked")
	public List<BonLivraison> fetchAllBl() {
		 String sql = "SELECT * FROM deliveryNote";
			Query query = getEntityManager().createNativeQuery(sql,BonLivraison.class);
			List<BonLivraison> bd = (List<BonLivraison>)query.getResultList();
			return bd;
		}
	 
	 public List<Order> fetchOrdersByBl(long blId) {
		 String sql = "SELECT * FROM order_T o where o.Order_Id in (select Order_Order_Id from order_T_OrderBl where order_Bl_id in "
		 		+ "(select id from OrderBl where bonLivraison_id = "+blId+"))";
			Query query = getEntityManager().createNativeQuery(sql,Order.class);
			List<Order> orders = (List<Order>)query.getResultList();
			return orders;
		}
	 public List<Pallette> fetchPalletteByBL(int blNum) {
		 String sql = "SELECT * FROM Pallette p where p.blNumber = "+blNum;
			Query query = getEntityManager().createNativeQuery(sql,Pallette.class);
			List<Pallette> pallettes = (List<Pallette>)query.getResultList();
			return pallettes;
		}
	
}
