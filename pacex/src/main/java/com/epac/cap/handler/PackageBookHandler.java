package com.epac.cap.handler;

import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.model.PackageBook;
import com.epac.cap.repository.PackageBookDao;;

@Service
public class PackageBookHandler {

	private static Logger logger = Logger.getLogger(PackageBookHandler.class);

	@Autowired
	PackageBookDao packageBookDao;
	
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void update(PackageBook bean) throws PersistenceException {
		try {
			packageBookDao.update(bean);		   
		} catch (Exception ex) {
			logger.error("Error occurred updating a PackageBook : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public Response create(PackageBook bean) throws PersistenceException {
		try {
			packageBookDao.create(bean);
			return Response.ok(bean.getPackagePartId()).build();
		} catch (Exception ex) {
			logger.error("Error occurred creating a PackageBook : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public PackageBook read(long packagePartId){
		return packageBookDao.read(packagePartId);
	}

	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public Integer fetchOrder(long packageBookId){
		Integer ordersId = packageBookDao.fectchOrder(packageBookId);
		return ordersId;
		
	}
	
}
