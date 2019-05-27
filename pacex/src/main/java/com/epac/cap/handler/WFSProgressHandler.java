package com.epac.cap.handler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.model.WFSProgress;
import com.epac.cap.repository.ProgressSearchBean;
import com.epac.cap.repository.WFSProgressDAO;

@Service
public class WFSProgressHandler {
	
	@Autowired
	private WFSProgressDAO wfsProgressDAO;
	
	public WFSProgressHandler(){
		
	}
	
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void save(WFSProgress wfsProgress) {
		wfsProgressDAO.save(wfsProgress);
	}
	
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void update(WFSProgress wfsProgress) {
		wfsProgressDAO.update(wfsProgress);
	}
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public WFSProgress getProgress(Integer progressId) {
		return wfsProgressDAO.getProgress(progressId);
	}
	
	/*@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<WFSProgress> findByPartNb(String partNb) {
		return wfsProgressDAO.findByPartNumb(partNb);
	}*/
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public WFSProgress findBySequenceId(Integer sequenceId, String partNb) {
		ProgressSearchBean searchBean = new ProgressSearchBean();
		searchBean.setSequenceId(sequenceId);
		searchBean.setPartNumb(partNb);
		return wfsProgressDAO.findBySequenceId(searchBean);
	}
	
	/**
	 * Calls the corresponding readAll method on the OrderDAO.
	 */
	@Transactional(rollbackFor = PersistenceException.class, readOnly = true, propagation = Propagation.SUPPORTS)
	public List<WFSProgress> readAll(ProgressSearchBean searchBean) throws PersistenceException {
		try {
			return wfsProgressDAO.readAll(searchBean);
		} catch (Exception ex) {
			//logger.error("Error occurred retrieving a list of Orders : " + ex.getMessage(), ex);
			throw new PersistenceException(ex);
		}
	}
	
	/*
	private static SseBroadcaster broadcaster = new SseBroadcaster();

	public void register(EventOutput eventOutput) {
		broadcaster.add(eventOutput);
	}

	public static void broadcast(String actionName) {

		OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
		OutboundEvent event = eventBuilder.name("message").mediaType(MediaType.APPLICATION_JSON_TYPE)
				.data(String.class, actionName).build();

		broadcaster.broadcast(event);
	}
	*/
}
