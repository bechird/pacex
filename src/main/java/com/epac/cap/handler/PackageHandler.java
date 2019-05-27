package com.epac.cap.handler;

import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.model.Package;
import com.epac.cap.repository.PackageDAO;

@Service
public class PackageHandler {
	private static Logger logger = Logger.getLogger(PackageHandler.class);

	@Autowired
	PackageDAO packageDAO;
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void update(Package bean) throws PersistenceException {
		try {
			packageDAO.update(bean);		   
		} catch (Exception ex) {
			logger.error("Error occurred updating a PackageBook : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public Response create(Package bean) throws PersistenceException {
		try {
			packageDAO.create(bean);
			return Response.ok(bean.getPackageId()).build();
		} catch (Exception ex) {
			logger.error("Error occurred creating a PackageBook : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public Package read(long id){
		return packageDAO.read(id);
	}
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public Package fetchPackageByPcbId(Long pcbId) throws PersistenceException{
		return packageDAO.fetchPackageByPcbId(pcbId);
	}
}
