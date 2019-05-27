package com.epac.cap.handler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.model.WFSSequence;
import com.epac.cap.repository.WFSSequenceDAO;

@Service
public class WFSSequenceHandler {
	
	@Autowired
	private WFSSequenceDAO wfsSequenceDAO;
	
	public WFSSequenceHandler() {
		// TODO Auto-generated constructor stub
	}
	
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void save(WFSSequence wfsSequence) {
		wfsSequenceDAO.save(wfsSequence);
	}
	
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void update(WFSSequence wfsSequence) {
		wfsSequenceDAO.update(wfsSequence);
	}
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public WFSSequence getSequence(Integer sequenceId) {
		return wfsSequenceDAO.getSequence(sequenceId);
	}
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<WFSSequence> findByWorkflowId(Integer workflowId) {
		return wfsSequenceDAO.findByWorkflowId(workflowId);
	}
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<WFSSequence> findByActionId(Integer actionId) {
		return wfsSequenceDAO.findByActionId(actionId);
	}
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<WFSSequence> findByProgressId(Integer progressId) {
		return wfsSequenceDAO.findByProgressId(progressId);
	}

}
