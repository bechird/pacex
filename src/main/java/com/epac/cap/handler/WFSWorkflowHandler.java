package com.epac.cap.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.model.WFSPartWorkflow;
import com.epac.cap.model.WFSWorkflow;
import com.epac.cap.repository.WFSWorkflowDAO;

@Service
public class WFSWorkflowHandler {
	
	@Autowired
	private WFSWorkflowDAO wfsWorkflowDAO;
	
	public WFSWorkflowHandler() {
		
	}
	
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void save(WFSWorkflow wfsWorkflow) {
		wfsWorkflowDAO.save(wfsWorkflow);
	}
	
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void update(WFSWorkflow wfsWorkflow) {
		wfsWorkflowDAO.update(wfsWorkflow);
	}
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public WFSWorkflow getWorkflow(Integer workflowId) {
		return wfsWorkflowDAO.getWorkflow(workflowId);
	}
	
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void savePartWorkflow(WFSPartWorkflow wfsPartWorkflow) {
		wfsWorkflowDAO.savePartWorkflow(wfsPartWorkflow);
	}
	
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void updatePartWorkflow(WFSPartWorkflow wfsPartWorkflow) {
		wfsWorkflowDAO.updatePartWorkflow(wfsPartWorkflow);
	}
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public WFSPartWorkflow getPartWorkflow(Integer partWorkflowId) {
		return wfsWorkflowDAO.getPartWorkflow(partWorkflowId);
	}

}
