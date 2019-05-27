package com.epac.cap.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.OutboundEvent.Builder;
import org.glassfish.jersey.media.sse.SseBroadcaster;
import org.glassfish.jersey.media.sse.SseFeature;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import com.epac.cap.sse.beans.Event;
import com.epac.cap.utils.LogUtils;

@Controller
@Path("/notification")
public class NotificationService{
	
	Builder eventBuilder = new Builder();
	SseBroadcaster broadcaster = new SseBroadcaster();
	
	List<EventOutput> eventOutputList = new ArrayList<EventOutput>();
	
	@GET
	@Produces(SseFeature.SERVER_SENT_EVENTS)
	public EventOutput subscribe() {
		EventOutput eventOutput = new EventOutput();
		eventOutputList.add(eventOutput);
		broadcaster.add(eventOutput);
		return eventOutput;
	}
	
	public void broadcast(Event e) {		
		try {
			OutboundEvent event = eventBuilder.name("message").mediaType(MediaType.APPLICATION_JSON_TYPE)
					.data(Event.class, e).build();
			broadcaster.broadcast(event);
		} catch (RuntimeException e1) {
			LogUtils.debug("Unable to send sse event");
		}	
		
	}
	
	//close all sse connections and clean the broadcaster every 30 minutes
	@Scheduled(fixedRate = 1800000)
    public void closeAllSseConnections() {		
		try {
			broadcaster.closeAll();
			for(EventOutput eventOutput : eventOutputList) {
				broadcaster.remove(eventOutput);
			}
			eventOutputList.clear();
		} catch (Exception e1) {
			LogUtils.debug("Unable to close all sse connections");
		}
    }
	
	/**
	 * Author : Nabil Dridi
	 * Don't delete, for remote debugging
	 */
	@GET
	@Path("/closeAll")
	@Produces(MediaType.APPLICATION_JSON)
	public Response closeAll(){
		Map<String, String> map= new HashMap<String, String>();
		try {
			broadcaster.closeAll();
			for(EventOutput eventOutput : eventOutputList) {
				broadcaster.remove(eventOutput);
			}
			eventOutputList.clear();
		} catch (Exception e) {
			LogUtils.debug("Unable to close all sse connections");
			map.put("exception", e.getMessage());
			return Response.ok(map).build();
		}
		map.put("operation", "ok");
		return Response.ok(map).build();
	}
	
	@GET
	@Path("/list")
	@Produces(MediaType.APPLICATION_JSON)
	public Response list(){
		
		Map<String, String> map= new HashMap<String, String>();
		int index = 0;
		for(EventOutput eventOutput : eventOutputList) {
			map.put(index +"", eventOutput.getType().getTypeName());
			index++;
		}
		return Response.ok(map).build();
	}	

	@GET
	@Path("/sendSampleEvent")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendSampleEvent(){
		Map<String, String> map= new HashMap<String, String>();
		try {
			Event e = new Event("target", false, "Sample message", null);
			OutboundEvent event = eventBuilder.name("message").mediaType(MediaType.APPLICATION_JSON_TYPE)
					.data(Event.class, e).build();
			broadcaster.broadcast(event);
		} catch (Exception e1) {
			LogUtils.debug("Unable to send sse event");
		}
		map.put("operation", "ok");
		return Response.ok(map).build();
	}
	
	//*****************************--------------------------------------------------------------------------
	
	
	
	
}
