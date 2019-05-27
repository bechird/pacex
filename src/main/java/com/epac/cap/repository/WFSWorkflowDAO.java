package com.epac.cap.repository;

import org.springframework.stereotype.Repository;

import com.epac.cap.model.WFSPartWorkflow;
import com.epac.cap.model.WFSWorkflow;

@Repository
public class WFSWorkflowDAO extends BaseEntityPersister{
	
	public void save(WFSWorkflow wfsWorkflow) {
		getEntityManager().persist(wfsWorkflow);
		getEntityManager().flush();
	}
	
	public void update(WFSWorkflow wfsWorkflow) {
		getEntityManager().merge(wfsWorkflow);
		getEntityManager().flush();
	}
	
	public WFSWorkflow getWorkflow(Integer workflowId) {
		return getEntityManager().find(WFSWorkflow.class, workflowId);
	}
	
	public void savePartWorkflow(WFSPartWorkflow wfsPartWorkflow) {
		getEntityManager().persist(wfsPartWorkflow);
		getEntityManager().flush();
	}
	
	public void updatePartWorkflow(WFSPartWorkflow wfsPartWorkflow) {
		getEntityManager().merge(wfsPartWorkflow);
		getEntityManager().flush();
	}
	
	public WFSPartWorkflow getPartWorkflow(Integer wfsPartWorkflowId) {
		return getEntityManager().find(WFSPartWorkflow.class, wfsPartWorkflowId);
	}

}
