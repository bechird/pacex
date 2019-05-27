package com.epac.cap.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.model.BonLivraison;
import com.epac.cap.model.BonLivraison.blStatus;
import com.epac.cap.model.Order;
import com.epac.cap.model.OrderBl;
import com.epac.cap.model.Pallette;
import com.epac.cap.repository.BonLivraisonDao;

@Service
public class BonLivraisonHandler {

	@Autowired
	BonLivraisonDao bonLivraisonDao;

	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public int getMaxCount() throws PersistenceException{
		try{
			Integer count = bonLivraisonDao.fetchMaxCountOf();
			if(count == null)count = 999;
			return count;
		} catch (Exception ex) {
			throw new PersistenceException(ex);
		}    
	}
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public void save(BonLivraison bl){
		bonLivraisonDao.create(bl);
	}
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public void update(BonLivraison bl){
		bonLivraisonDao.update(bl);
	}

	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public BonLivraison fetchBl(long id){
		return bonLivraisonDao.fetchBl(id);
	}
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public void createOrderBl(OrderBl oderBl) {
		bonLivraisonDao.create(oderBl);
	}

	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public List<BonLivraison> fetchAllBl() {
		return bonLivraisonDao.fetchAllBl();
	}
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public List<Order> fetchOrdersByBl(long blId) {
		return bonLivraisonDao.fetchOrdersByBl(blId);
	}
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public List<Pallette> fetchPalletteByBL(int blnum) {
		return bonLivraisonDao.fetchPalletteByBL(blnum);
	}
	

}
