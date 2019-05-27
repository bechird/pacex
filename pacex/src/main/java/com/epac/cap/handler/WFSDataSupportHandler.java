package com.epac.cap.handler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.model.WFSDataSupport;
import com.epac.cap.model.WFSLocation;
import com.epac.cap.repository.WFSDataSupportDAO;
import com.epac.cap.repository.WFSDataSupportSearchBean;

@Service
public class WFSDataSupportHandler {
	
	@Autowired
	private WFSDataSupportDAO wfsDataSupportDAO;
	
	public WFSDataSupportHandler() {
		// TODO Auto-generated constructor stub
	}
	
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void save(WFSDataSupport dataSupport) {
		wfsDataSupportDAO.save(dataSupport);
	}
	
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void update(WFSDataSupport wfsDataSupport) {
		wfsDataSupportDAO.update(wfsDataSupport);
	}
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public WFSDataSupport getSupport(Integer supportId) {
		return wfsDataSupportDAO.get(supportId);
	}
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<WFSDataSupport> readAll(WFSDataSupportSearchBean sb) {
		List<WFSDataSupport> locs =  wfsDataSupportDAO.readAll(sb);
		return locs;
	}

}
