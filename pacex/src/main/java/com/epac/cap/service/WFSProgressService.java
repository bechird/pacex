package com.epac.cap.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.handler.OrderHandler;
import com.epac.cap.handler.PartHandler;
import com.epac.cap.handler.WFSProgressHandler;
import com.epac.cap.model.Part;
import com.epac.cap.model.WFSAction;
import com.epac.cap.model.WFSPartWorkflow;
import com.epac.cap.model.WFSProgress;

@Controller
@Path("/progress")
public class WFSProgressService extends AbstractService{
	
	@Autowired
	private WFSProgressHandler wfsProgressHandler;
	
	@Autowired
	private OrderHandler orderHandler;
	
	@Autowired
	private PartHandler partHandler;
	
	@GET
	@Path("/part/{orderId}")
	public String getProgress(@PathParam("orderId") Integer orderId) throws PersistenceException {

		WFSAction wfsAction = null;
		Part part = null;
		try {// TODO check if this right
			part = orderHandler.read(orderId).getOrderParts().iterator().next().getPart();
			System.out.println(part);
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!part.getSubParts().isEmpty()){
			part = partHandler.read(part.getSubParts().iterator().next().getId().getSubPartNum());
		}
		//ProgressSearchBean progressSearchBean = new ProgressSearchBean();
		//progressSearchBean.setPartNumb(part + "T");
		//List<WFSProgress> progress = wfsProgressHandler.readAll(progressSearchBean);
		//System.out.println(progress);
		//List<WFSProgress> progress = wfsProgressHandler.findByPartNb(partNumber);
		/*if (!progress.isEmpty()) {
			progress.sort(Comparator.comparing(WFSProgress::getProgressId));
			WFSProgress wfsProgress = null;
			for (WFSProgress progres : progress){
				if (progres.getStatus().equalsIgnoreCase("pending")){
					wfsProgress = progres;
					break;
				}
			}
			if (wfsProgress != null)
				//wfsAction = wfsActionHandler.getAction(wfsSequenceHandler.getSequence(wfsProgress.getSequenceId()).getActionId());
				wfsAction = wfsSequenceHandler.getSequence(wfsProgress.getSequenceId()).getWfAction();
			}*/
		WFSProgress wfsProgress = null;
		if(part != null){
			WFSPartWorkflow pw = part.getPartWorkFlowOnProd();
			if(pw != null){
				for(WFSProgress pr : pw.getProgresses()){
					if (pr.getStatus().equalsIgnoreCase("pending")){
						wfsProgress = pr;
						break;
					}
				}
			}
			if (wfsProgress != null){
				wfsAction = wfsProgress.getSequence().getWfAction();
			}
			if (wfsAction != null)
				switch (wfsAction.getName()) {
				case "impose": return "IMPOSING";
				case "impose_popline": return "IMPOSING";
				case "impose_cover": return "IMPOSING";
				case "download": return "DOWNLOADING";
				case "rip": return "RIPPING";
				}
			else if (pw != null && !pw.getProgresses().isEmpty())
				return "READY";
			else return "DOWNLOADING";
		}
		return "PENDING";
		
	}
	
}
