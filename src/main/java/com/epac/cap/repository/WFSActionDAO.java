package com.epac.cap.repository;

import org.springframework.stereotype.Repository;

import com.epac.cap.model.WFSAction;

@Repository
public class WFSActionDAO extends BaseEntityPersister{
	
	public void save(WFSAction wfsAction) {
		getEntityManager().persist(wfsAction);
		getEntityManager().flush();
	}
	
	public void update(WFSAction wfsAction) {
		getEntityManager().merge(wfsAction);
		getEntityManager().flush();
	}
	
	public WFSAction getAction(Integer actionId) {
		return getEntityManager().find(WFSAction.class, actionId);
	}

}
