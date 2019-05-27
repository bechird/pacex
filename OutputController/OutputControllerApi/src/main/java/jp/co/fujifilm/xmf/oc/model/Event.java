package jp.co.fujifilm.xmf.oc.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Event {
	
	private static final SimpleDateFormat formetter = new SimpleDateFormat("yyyy-MM-dd hh:mm");
	public enum EventType{
		PING,
		PRINTER,
		JOBS,
		PRINTING, 
		SYSTEM,
		HEARTBEAT,
	};
	
	private String time;
	private EventType type;
	private Object object;
	
	
	public Event() {
	}
	
	public Event(EventType type, Object object) {
		this.type = type;
		this.object = object;
		this.time = formetter.format(new Date());
	}
	
	
	public EventType getType() {
		return type;
	}
	
	public void setType(EventType type) {
		this.type = type;
	}
	
	public void setObject(Object object) {
		this.object = object;
	}
	
	public Object getObject() {
		return object;
	}
	
	public String getTime() {
		return time;
	}
	
}
