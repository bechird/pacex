package com.epac.cap.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.model.WFSLocation;
import com.epac.cap.repository.WFSLocationDAO;

@Service
public class WFSLocationHandler {

	@Autowired
	private WFSLocationDAO wfsLocationDAO;
	
	public WFSLocationHandler() {
		// TODO Auto-generated constructor stub
	}
	
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void save(WFSLocation wfsLocation) {
		wfsLocationDAO.save(wfsLocation);
	}
	
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void update(WFSLocation wfsLocation) {
		wfsLocationDAO.update(wfsLocation);
	}
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public WFSLocation getLocation(Integer locationId) {
		return wfsLocationDAO.getLocation(locationId);
	}
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public WFSLocation getLocationByDsId(Integer dsId) {
		return wfsLocationDAO.getLocationByDSId(dsId);
	}
	
}
