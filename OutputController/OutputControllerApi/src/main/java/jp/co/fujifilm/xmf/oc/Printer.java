package jp.co.fujifilm.xmf.oc;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import jp.co.fujifilm.xmf.oc.model.Event;
import jp.co.fujifilm.xmf.oc.model.Event.EventType;
import jp.co.fujifilm.xmf.oc.model.jobs.Job;
import jp.co.fujifilm.xmf.oc.model.jobs.JobList;
import jp.co.fujifilm.xmf.oc.model.printers.PaperFeed;
import jp.co.fujifilm.xmf.oc.model.printers.PrinterStatus;
import jp.co.fujifilm.xmf.oc.model.printing.ErrorList;
import jp.co.fujifilm.xmf.oc.model.printing.PrintingJob;
import jp.co.fujifilm.xmf.oc.model.printing.PrintingJobRequest;
import jp.co.fujifilm.xmf.oc.model.printing.PrintingJobsList;

public class Printer {

	private static final Logger LOG = Logger.getLogger(Printer.class.getCanonicalName());

	public static final String STATUS_ERROR 			= "ERROR";
	public static final String STATUS_READY 			= "READY";
	public static final String STATUS_RUNNING 			= "RUNNING";
	public static final String STATUS_OFFLINE 			= "OFFLINE";

	private static final String REST_PRINTING_REQUEST 	= "/printing";
	private static final String REST_PRINTING_CANCEL 	= "/printing/cancel";
	private static final String REST_PRINTING_RESUME 	= "/printing/resume";

	private static final String REST_PAPAER_FEED 		= "/paper/feed";
	private static final String REST_PAPAER_INFO 		= "/paper/info";

	private static final String REST_JOBS_INFO 			= "/job/";
	private static final String REST_JOBS_DELETE 		= "/job/";

	private static final String REST_EVENT_URL 			= "/events";

	private static final String[] API_PACKAGES = { 
			"jp.co.fujifilm.xmf.oc", 
			"jp.co.fujifilm.xmf.oc.model.jobs",
			"jp.co.fujifilm.xmf.oc.model.printingjobs", 
			"jp.co.fujifilm.xmf.oc.model.printers" };
	

	private final List<EventListener> listeners = new ArrayList<>();
	private String name;
	private String service;
	private JobList jobs;
	private PrinterStatus status;
	private PrintingJobsList printingJobsList;
	
	
	
	public Printer(){}
	
	public Printer(String service, String name) {
		LOG.log(Level.INFO, "Printer instance created for OC: "+service);
		if(service.matches("/^(https?:\\/\\/)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([\\/\\w \\.-]*)*\\/?$/"))
			throw new IllegalArgumentException("OutputController URL is not valid: "+ this.service);
		// remove trailing slash
		this.name = name;
		this.service = service.replaceFirst("(.*)/$", "$1").concat("/printer");
	}

	public void shutdown() {
		try {
			listeners.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	

	
	public void startListener(){
		
	}
	
	
	
	
	public void addListener(EventListener listener) {
		listeners.add(listener);
	}

	public boolean isEpacMode() {
		if (service == null || status == null)
			return false;

		if (STATUS_OFFLINE.equals(status.getRecorderStatus()))
			return false;

		return true;
	}

	public PrinterStatus getPrinterStatus() {
		return status;
	}

	public JobList getJobsList() {
		return jobs;
	}

	public PrintingJobsList getPrintingJobsStatus() {
		return printingJobsList;
	}

	public Object print(PrintingJobRequest request) {
		RestTemplate rest = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();

		// set response type to XML to easily know the response type from root
		// tag
		headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

		HttpEntity<PrintingJobRequest> entity = new HttpEntity<>(request, headers);

		ResponseEntity<String> response = rest.exchange(service.concat(REST_PRINTING_REQUEST), HttpMethod.POST, entity,
				String.class);

		//if (response.getStatusCode() == HttpStatus.ACCEPTED)
			//return false;

		String xml = response.getBody();
		LOG.log(Level.INFO, xml);
		Object object = null;

		try {
			object = resolve("Response:" + xml);
		} catch (Exception e) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				object = mapper.readValue(xml, ErrorList.class);
			}catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		if (object == null)
			return null;

		if (object instanceof PrintingJobsList) {
			Event event = new Event(EventType.PRINTER, object);
			handleAndForward(event);
		} else if (object instanceof ErrorList) {
			Event event = new Event(EventType.SYSTEM, object);
			handleAndForward(event);
			Object obj = event.getObject();
			if(obj instanceof ErrorList){
				ErrorList errors = (ErrorList)obj;
				return errors;
			}
		}

		return true;
	}
	
	public boolean resume() {
		// to resume, calculate number of printed sheets and ignore last copy
		
		RestTemplate rest = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();

		// set response type to XML to easily know the response type from root
		// tag
		headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

		HttpEntity<PrintingJobRequest> entity = new HttpEntity<>(headers);

		ResponseEntity<String> response = rest.exchange(service.concat(REST_PRINTING_RESUME), HttpMethod.GET, entity,
				String.class);

		if (response.getStatusCode() == HttpStatus.ACCEPTED)
			return true;
		return false;
		
	}
	
	public boolean stop() {
		RestTemplate rest = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();

		// set response type to XML to easily know the response type from root
		// tag
		headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

		HttpEntity<PrintingJobRequest> entity = new HttpEntity<>(headers);

		ResponseEntity<Map> response = rest.exchange(service.concat(REST_PRINTING_CANCEL), HttpMethod.GET, entity,
				Map.class);

		if (response.getStatusCode() == HttpStatus.ACCEPTED) {
			LOG.log(Level.INFO, response.getBody().toString());
			return true;
		}
		return false;
	}
	
	public boolean setPaperInfo(int width, int length) {
		// TODO: impelemnt this method
		throw new IllegalAccessError("Not impelemented yet");
	}
	
	public boolean feedPaper(int duration) {
		
		PaperFeed feed = new PaperFeed();
		
		feed.setDuration(duration);
		
		RestTemplate rest = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<PaperFeed> entity = new HttpEntity<>(feed, headers);

		ResponseEntity<String> response = rest.exchange(service.concat(REST_PAPAER_FEED), HttpMethod.POST, entity, String.class);

		if (response.getStatusCode() == HttpStatus.ACCEPTED)
			return true;
		return false;
	}
	public boolean deleteJob(String id) {
		RestTemplate rest = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<?> entity = new HttpEntity<>(headers);

		ResponseEntity<String> response = rest.exchange(service.concat(REST_JOBS_DELETE).concat(id), HttpMethod.DELETE, entity, String.class);

		if (response.getStatusCode() == HttpStatus.ACCEPTED)
			return true;
		return false;
	}
	
	public Job getJob(String id) {
		RestTemplate rest = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<?> entity = new HttpEntity<>(headers);

		ResponseEntity<Job> response = rest.exchange(service.concat(REST_JOBS_INFO).concat(id), HttpMethod.GET, entity,
				Job.class);

		if (response.getStatusCode() == HttpStatus.ACCEPTED)
			return response.getBody();
		return null;
	}

	private void handleAndForward(Event event) {
		if(event == null)
			return;
		//LOG.log(Level.INFO, "Received event of type: "+event);
		if (Event.EventType.PRINTER == event.getType()) {
			status = (PrinterStatus) event.getObject();
		} else if (Event.EventType.JOBS == event.getType()) {
			jobs = (JobList) event.getObject();
		} else if (Event.EventType.PRINTING == event.getType()) {
			Object obj = event.getObject();
			if(obj instanceof PrintingJobsList)
				printingJobsList = (PrintingJobsList) obj;
			//FIXME: obj could be an arraylist that should be converted into printingJobsList
			/*else if(obj instanceof ArrayList) {
				printingJobsList = new PrintingJobsList();
				ArrayList list = (ArrayList) obj;
				printingJobsList.setPrintingJobStatusList(list);
					
			}else
				return;*/
		}

		fireEvent(event);

	}

	private void fireEvent(Event event) {
		LOG.log(Level.INFO, "("+listeners.size()+") listeners will handle the event: "+event.getType());
		for (EventListener listener : listeners) {
			try {
				LOG.log(Level.INFO, "calling handleEvent for listener "+listener);
				listener.handleEvent(event);
			} catch (Exception e) {
				LOG.log(Level.SEVERE, "Error occured while handling event: "+event.getType(), e);
			}
			
		}
	}

	private Event resolveEvent(String eventStr) throws JsonParseException, JsonMappingException, IOException {
		if(eventStr.isEmpty())
			return null;
		
		String jsonEvent = eventStr.replace("data:", "");
		//LOG.log(Level.INFO, "resolve event: "+eventStr+" for Printer: "+service.concat(REST_EVENT_URL));
		try {
			ObjectMapper mapper = new ObjectMapper();
			SimpleModule module = new SimpleModule();
			module.addDeserializer(Event.class, new EventDeserializer(Event.class));
			mapper.registerModule(module);
			return mapper.readValue(jsonEvent, Event.class);
		} catch (Exception e) {
			LOG.log(Level.SEVERE,"Error resolving event: "+eventStr, e);
		}
		return null;
		
	}
	private Object resolve(String xml) throws JAXBException, ParserConfigurationException, SAXException, IOException {
		String msg = xml.replaceAll("data:", "");
		InputSource inputSource = new InputSource(new StringReader(msg));

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();

		Document document = db.parse(inputSource);
		String rootTag = document.getDocumentElement().getNodeName();

		Class<?> t = null;
		String type = null;

		for (int i = 0; i < API_PACKAGES.length; i++) {

			try {
				type = API_PACKAGES[i].concat(".").concat(rootTag);
				t = Class.forName(type);
				break;
			} catch (Exception e) {
			}

		}

		JAXBContext jaxbContext = JAXBContext.newInstance(t);
		Unmarshaller jaxbMarshaller = jaxbContext.createUnmarshaller();

		Object object = jaxbMarshaller.unmarshal(new StringReader(msg));

		return object;
	}

	public boolean isReady() {
		if(status != null && STATUS_READY.equals(status.getRecorderStatus()))
			return true;
		return false;
	}

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
	
	public  ResponseEntity<Map> copy(String src,String dest){
		List<HttpMessageConverter<?>> converters = new ArrayList<>();
		converters.add(new MappingJackson2HttpMessageConverter());
		converters.add(new StringHttpMessageConverter());
		converters.add(new FormHttpMessageConverter());
		RestTemplate rest = new RestTemplate();
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("src", src);
		params.put("dest", dest);
		rest.setMessageConverters(converters);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
		headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<Object> request = new HttpEntity<Object>(params, headers);
		LOG.log(Level.INFO,"Copy file request sent: " + src + "to "+ service.concat("/files"));

		ResponseEntity<Map> response = rest.exchange(service.concat("/files"), HttpMethod.POST, request,
				Map.class);
		return response;
	}

}
