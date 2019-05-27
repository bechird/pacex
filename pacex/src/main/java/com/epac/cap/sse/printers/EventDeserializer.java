package com.epac.cap.sse.printers;

import java.io.IOException;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import jp.co.fujifilm.xmf.oc.model.Event;
import jp.co.fujifilm.xmf.oc.model.Event.EventType;
import jp.co.fujifilm.xmf.oc.model.jobs.JobList;
import jp.co.fujifilm.xmf.oc.model.printers.PrinterStatus;
import jp.co.fujifilm.xmf.oc.model.printing.ErrorList;
import jp.co.fujifilm.xmf.oc.model.printing.PrintingJob;

class EventDeserializer extends StdDeserializer<Event>{
	private static final long serialVersionUID = 1L;

	protected EventDeserializer(Class<?> t) {
		super(t);
	}

	@Override
	public Event deserialize(JsonParser jp, DeserializationContext ctx)
			throws IOException, JsonProcessingException {
		
		JsonNode node = jp.getCodec().readTree(jp);

        JsonNode objectNode = node.get("object");
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        
        Object object;
        if(objectNode.has("recorderStatus")) {
        	PrinterStatus r = mapper.treeToValue(objectNode, PrinterStatus.class);
        	object = r;
        }else if(objectNode.has("jobs")) {
        	JobList r = mapper.treeToValue(objectNode, JobList.class);
        	object = r;
        }else if(objectNode.isArray()) {
        	PrintingJob[] r = mapper.treeToValue(objectNode, PrintingJob[].class);
        	object = Arrays.asList(r);
        }else if(objectNode.has("errorList")) {
        	ErrorList r = mapper.treeToValue(objectNode, ErrorList.class);
        	object = r;
        }else {
        	object = objectNode.asText();
        }
        	
        
        String type = node.get("type").asText();
        return new Event(EventType.valueOf(type), object);
		
	}
	
}
