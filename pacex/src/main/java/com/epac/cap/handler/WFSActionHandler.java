package com.epac.cap.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.model.WFSAction;
import com.epac.cap.repository.WFSActionDAO;

@Service
public class WFSActionHandler {

	@Autowired
	private WFSActionDAO wfsActionDAO;
	
	public WFSActionHandler() {
		// TODO Auto-generated constructor stub
	}
	
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void save(WFSAction wfsAction) {
		wfsActionDAO.save(wfsAction);
	}
	
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void update(WFSAction wfsAction) {
		wfsActionDAO.update(wfsAction);
	}
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public WFSAction getAction(Integer actionId) {
		return wfsActionDAO.getAction(actionId);
	}
	
}
