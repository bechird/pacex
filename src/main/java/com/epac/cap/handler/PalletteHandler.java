package com.epac.cap.handler;

import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.model.Customer;
import com.epac.cap.model.Job;
import com.epac.cap.model.LoadTag;
import com.epac.cap.model.Pallette;
import com.epac.cap.model.PalletteBook;
import com.epac.cap.repository.CustomerDAO;
import com.epac.cap.repository.CustomerSearchBean;
import com.epac.cap.repository.PalletteDao;
import com.epac.cap.repository.PalletteSearchBean;

@Service
public class PalletteHandler {

	@Autowired
	private PalletteDao palletteDAo;
	@Autowired
	private CustomerDAO  customerDAO;
	private static Logger logger = Logger.getLogger(PalletteHandler.class);



	public PalletteDao getPalletteDAo() {
		return palletteDAo;
	}



	public void setPalletteDAo(PalletteDao palletteDAo) {
		this.palletteDAo = palletteDAo;
	}

	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public boolean deletePalletteBook(PalletteBook bean) {		
		return palletteDAo.deletePalletteBook(bean);
	}

	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public void create(Pallette bean) throws PersistenceException {
		try {

			prepareBeans(bean);
			getPalletteDAo().create(bean);		   
		} catch (Exception ex) {
			bean.setId(null);
			logger.error("Error occurred creating a Pallette : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	private void prepareBeans(Pallette bean){
		if(bean.getCustomer() == null){
			bean.setCustomer(null);
		}else{
			CustomerSearchBean cus = new CustomerSearchBean();
			cus.setFirstName(bean.getCustomer().getFirstName());
			Customer customer = customerDAO.readAll(cus).get(0);
			bean.setCustomer(customer);
		}

	}
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void update(Pallette bean) throws PersistenceException {
		try {
			prepareBeans(bean);
			getPalletteDAo().update(bean);		   
		} catch (Exception ex) {
			logger.error("Error occurred updating a Pallette : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public boolean delete(Pallette bean) throws PersistenceException {
		try {
			return getPalletteDAo().delete(bean);
		} catch (Exception ex) {
			logger.error("Error occurred deleting a Pallette with id '" + (bean == null ? null : bean.getId()) + "' : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}

	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Pallette> readAll(PalletteSearchBean searchBean) throws PersistenceException{
		try{
			return getPalletteDAo().readAll(searchBean);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a list of Pallettes : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Pallette> readAllPalletteComplete(PalletteSearchBean searchBean) throws PersistenceException{
		try{
			return getPalletteDAo().readAllPalletteComplete(searchBean);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a list of Pallettes : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}

	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void updateQtyOfPcb(Long packageBookId,Long palletteId,int qty){
		getPalletteDAo().updateQtyOfPcb(packageBookId, palletteId, qty);
	}
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void editQtyOfPcb(Long packageBookId,Long palletteId,int qty){
		getPalletteDAo().editQtyOfPcb(packageBookId, palletteId, qty);
	}
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Pallette read(long palletteId) throws PersistenceException{
		try{
			return getPalletteDAo().read(palletteId);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a Pallette with id '" + palletteId + "' : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public int getMaxCount() throws PersistenceException{
		try{
			List<Integer> counts = getPalletteDAo().fetchMaxCountOf();
			Integer count = counts.get(0);
			if(count == null)count = 1;
			return count;
		} catch (Exception ex) {
			logger.error("Error occurred retrieving while fetch max count" );
			throw new PersistenceException(ex);
		}    
	}

	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<PalletteBook> fetchPalletteBookByPcbId(Long pcbId)throws PersistenceException{
		return palletteDAo.fetchPalletteBokkByPcbId(pcbId);
	}

	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Pallette> fetchPalletteWithoutBL()throws PersistenceException{
		return palletteDAo.fetchPalletteWithoutBL();
	}

	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Pallette> fetchPalletteToShipToday()throws PersistenceException{
		return palletteDAo.fetchPalletteToShipToday();
	}
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Pallette> fullSearch(String query, Integer maxResult, Integer offset) {	
		return palletteDAo.fullSearch(query, maxResult, offset);		
	}

}
