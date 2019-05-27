package com.epac.cap.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;

import com.epac.cap.common.OrderBy;
import com.epac.cap.common.PersistenceException;
import com.epac.cap.config.ConfigurationConstants;
import com.epac.cap.handler.OrderHandler;
import com.epac.cap.handler.PackageBookHandler;
import com.epac.cap.handler.PackageHandler;
import com.epac.cap.handler.PalletteHandler;
import com.epac.cap.model.LoadTag;
import com.epac.cap.model.Order;
import com.epac.cap.model.OrderBl;
import com.epac.cap.model.Package;
import com.epac.cap.model.Package.PackageType;
import com.epac.cap.model.PackageBook;
import com.epac.cap.model.PackageBook.typePcb;
import com.epac.cap.model.Pallette;
import com.epac.cap.model.Pallette.PalletteStatus;
import com.epac.cap.model.Pallette.PalletteType;
import com.epac.cap.model.PalletteBook;
import com.epac.cap.model.PalletteBooksId;
import com.epac.cap.model.Preference;
import com.epac.cap.repository.LoadTagSearchBean;
import com.epac.cap.repository.LookupDAO;
import com.epac.cap.repository.PalletteSearchBean;
import com.epac.cap.utils.AoData;
import com.epac.cap.utils.AoDataParser;
import com.epac.cap.utils.PaginatedResult;
import com.epac.cap.validator.InputResponseError;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;


@Controller
@Path("/pallets")
public class PalletteService extends AbstractService{

	@Autowired
	private PalletteHandler palletteHandler;

	@Autowired
	private OrderHandler orderHandler;

	@Autowired
	PackageBookHandler packageBookHandler;

	@Autowired
	PackageHandler packageHandler;
	@Autowired
	LookupDAO lookupDAO;
	
	private static Logger logger = Logger.getLogger(PalletteService.class);

	@GET
	@Path("/{machineId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPallettesByMachine(@PathParam("machineId") String machineId){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Pallette> pallettes = new ArrayList<Pallette>();
		try {
			PalletteSearchBean searchBean = new PalletteSearchBean();
			searchBean.setMachineId(machineId);
			pallettes = palletteHandler.readAll(searchBean);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of job records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(pallettes).build();
	}
	@GET
	@Path("/active/{machineId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getActivePalletteByMachine(@PathParam("machineId") String machineId){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Pallette> pallettes = new ArrayList<Pallette>();
		Map<Object,Object> response = new HashMap<>();
		try {
			PalletteSearchBean searchBean = new PalletteSearchBean();
			searchBean.setMachineId(machineId);
			searchBean.setStatusPallette(PalletteStatus.ACTIVE);
			pallettes = palletteHandler.readAll(searchBean);
			Pallette pallette = null;
			List<Order> orders = new ArrayList<>();

			if(pallettes.size() > 0){
				pallette = pallettes.get(0);
				response.put("pallette", pallette);
				List<PalletteBook> palletteBooks = pallettes.get(0).getBooks();
				for(PalletteBook palletB: palletteBooks){
					PackageBook pcb =  palletB.getPackageBook();
					long pckBookId = pcb.getPackagePartId();
					Integer orderId = packageBookHandler.fetchOrder(pckBookId);
					if(orderId != null){
						Order order = orderHandler.read(orderId);
						orders.add(order);
					}

				}


			}
			response.put("orders", orders);
			response.put("pallette", pallette);

		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of job records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(response).build();
	}

	@POST
	@Path("/update/status/{palletteid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updatePalletteStatus(@PathParam("palletteid") Long id){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Pallette pallette = null;
		try {
			pallette = palletteHandler.read(id);

			PalletteStatus oldStatus = pallette.getStatusPallette();
			PalletteStatus status = PalletteStatus.DELIVERED;
			if(PalletteStatus.DELIVERED.equals(oldStatus)){
				status = PalletteStatus.COMPLETE;
				pallette.setDelivredDate(null);
			}
			pallette.setStatusPallette(status);
			if(PalletteStatus.DELIVERED.equals(status)){
				pallette.setDelivredDate(new Date());
			}
			palletteHandler.update(pallette);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of job records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(pallette).build();
	}
	@POST
	@Path("/update/type/{palletteid}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updatePalletteType(@PathParam("palletteid") Long id, PalletteType type){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			PalletteSearchBean searchBean = new PalletteSearchBean();
			searchBean.setId(id);
			Pallette pallette = palletteHandler.readAll(searchBean).get(0);
			pallette.setTypePallette(type);
			palletteHandler.update(pallette);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of job records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok().build();
	}
	@POST
	@Path("/update/qtyPcb/{palletteid}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateQtyPalletteBook(@PathParam("palletteid") Long palletteId,Map<String,Object> values){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			int qty = (int)values.get("qty");
			long packageBookId = (int)values.get("packageBookId");
			palletteHandler.updateQtyOfPcb(packageBookId, palletteId, qty);
		} catch (Exception e) {
			constraintViolationsMessages.put("errors", "An error occurred while updting the qty!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok().build();
	}
	@POST
	@Path("/edit/qtyPcb/{palletteId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response editQtyPalletteBook(@PathParam("palletteId") Long palletteId,Map<String,Object> values){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			int qty = (int)values.get("qty");
			long packageBookId = (int)values.get("packageBookId");
			palletteHandler.editQtyOfPcb(packageBookId, palletteId, qty);
		} catch (Exception e) {
			constraintViolationsMessages.put("errors", "An error occurred while editing qty in pallette!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok().build();
	}
	@POST
	@Path("/add")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addNewPallette(Map<String,Object> values){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			PalletteType type = PalletteType.valueOf((String)values.get("type"));
			String machineId = (String) values.get("machineId");
			String dest = (String) values.get("dest");

			PalletteSearchBean searchBean = new PalletteSearchBean();
			searchBean.setMachineId(machineId);
			searchBean.setStatusPallette(PalletteStatus.ACTIVE);
			List<Pallette> pallettes = palletteHandler.readAll(searchBean);
			if(pallettes.size() <= 0){

				Pallette pallette = new Pallette();
				pallette.setCreatedDate(new Date());
				pallette.setStatusPallette(PalletteStatus.ACTIVE);
				pallette.setTypePallette(type);
				pallette.setMachineId(machineId);
				pallette.setStartDate(new Date());
				pallette.setDestination(dest);
				int count = palletteHandler.getMaxCount();
				int newCount = count + 1;
				pallette.setCount(newCount);
				palletteHandler.create(pallette);
			}else{
				constraintViolationsMessages.put("Erreur", "Existe une autre Pallette Active pour la machine "+machineId);
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				return inputResponseError.createResponse();
			}
		} catch (Exception e) {
			constraintViolationsMessages.put("errors", "An error occurred while Add new Pallet!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok().build();
	}
	@GET
	@Path("/closePallette/{palletteId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response closePalletteOfMachine(@PathParam("palletteId")long palletteId){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			Pallette pallette = palletteHandler.read(palletteId);
			pallette.setStatusPallette(PalletteStatus.COMPLETE);
			pallette.setEndDate(new Date());
			palletteHandler.update(pallette);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while closing pallet!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
	}

	@GET
	@Path("/pause/{palletteId}/{machineId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response pausePallette(@PathParam("palletteId")long palletteId,@PathParam("machineId")String machineId){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			PalletteSearchBean bean = new PalletteSearchBean();
			bean.setStatusPallette(PalletteStatus.PAUSED);
			bean.setMachineId(machineId);
			List<Pallette> pausedPallette = palletteHandler.readAll(bean);
			Pallette pallettePaused = null;
			/*if(pausedPallette .size() > 0){
				pallettePaused = pausedPallette.get(0);
				pallettePaused.setStatusPallette(PalletteStatus.ACTIVE);
				palletteHandler.update(pallettePaused);

			}*/
			Pallette pallette = palletteHandler.read(palletteId);
			pallette.setStatusPallette(PalletteStatus.PAUSED);
			palletteHandler.update(pallette);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while closing pallet!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
	}
	@GET
	@Path("/resume/{palletteId}/{machineId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response resumePalletteMachine(@PathParam("palletteId")long palletteId,@PathParam("machineId")String machineId){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			PalletteSearchBean bean = new PalletteSearchBean();
			bean.setStatusPallette(PalletteStatus.ACTIVE);
			bean.setMachineId(machineId);
			List<Pallette> activePallettes = palletteHandler.readAll(bean);
			Pallette palletteActive = null;
			if(activePallettes .size() > 0){
				palletteActive = activePallettes.get(0);
				palletteActive.setStatusPallette(PalletteStatus.PAUSED);
				palletteHandler.update(palletteActive);

			}
			Pallette pallette = palletteHandler.read(palletteId);
			pallette.setStatusPallette(PalletteStatus.ACTIVE);
			palletteHandler.update(pallette);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while closing pallet!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
	}
	@POST
	@Path("/updateDestination/{palletteId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateDestinationPallette(@PathParam("palletteId")long palletteId,String destination){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			Pallette pallette = palletteHandler.read(palletteId);
			pallette.setDestination(destination);
			palletteHandler.update(pallette);
			return Response.ok().build();
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while closing pallet!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
	}

	@GET
	@Path("/qtyOrder/{palletteId}/{orderId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getquantityOrderInPallette(@PathParam("palletteId")long palletteId,@PathParam("orderId")int orderId){


		int qty = 0;
		try {
			Pallette pallette = palletteHandler.read(palletteId);
			Order order = orderHandler.read(orderId);
			qty = orderHandler.calcQtyBookInOrder(pallette,order);
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return Response.ok(qty).build();

	}
	@GET
	@Path("/pausedByMachine/{machineId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllPalletteByMachine(@PathParam("machineId") String machineId){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Pallette> pallettes = new ArrayList<Pallette>();
		try {
			PalletteSearchBean searchBean = new PalletteSearchBean();
			searchBean.setMachineId(machineId);
			searchBean.setStatusPallette(PalletteStatus.PAUSED);
			pallettes = palletteHandler.readAll(searchBean);

		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of job records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(pallettes).build();
	}
	@GET
	@Path("/qtyLivrePallette/{palletteId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response calcQtyPerPallette(@PathParam("palletteId") long palletteId){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {

			Pallette pallette = palletteHandler.read(palletteId);
			List<PalletteBook> palletttePCb = pallette.getBooks();
			int sum = 0;
			for(PalletteBook pcb: palletttePCb){
				PackageBook pckBook = pcb.getPackageBook();
				int qtyInPcb = pckBook.getDepthQty()*pckBook.getHeightQty()*pckBook.getWidthQty();
				sum+= qtyInPcb*pcb.getQuantity();
			}
			return Response.ok(sum).build();
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of job records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}

	}
	@GET
	@Path("/qtyPcbPallette/{palletteId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response calcQtyPcbPerPallette(@PathParam("palletteId") long palletteId){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			Pallette pallette = palletteHandler.read(palletteId);
			List<PalletteBook> palletttePCb = pallette.getBooks();
			int sum = 0;
			for(PalletteBook pcb: palletttePCb){
				sum+= pcb.getQuantity();
			}
			return Response.ok(sum).build();
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of pallette records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}

	}
	@GET
	@Path("/slips")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllPalletteNotActive(){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Pallette> pallettes = new ArrayList<Pallette>();
		Map<Object,Object> response = new HashMap<>();
		try {
			PalletteSearchBean searchBean = new PalletteSearchBean();
			searchBean.setStatusPallette(PalletteStatus.COMPLETE);
			pallettes = palletteHandler.readAllPalletteComplete(searchBean);

			List<Order> orders = new ArrayList<>();
			response.put("pallettes", pallettes);
			if(pallettes.size() > 0){
				for(Pallette pallette :pallettes){

					List<PalletteBook> palletteBooks = pallette.getBooks();
					for(PalletteBook palletB: palletteBooks){
						PackageBook pcb =  palletB.getPackageBook();
						long pckBookId = pcb.getPackagePartId();
						Integer orderId = packageBookHandler.fetchOrder(pckBookId);
						if(orderId != null){
							Order order = orderHandler.read(orderId);
							orders.add(order);}

					}
				}


			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of pallette records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(pallettes).build();
	}
	@GET
	@Path("/slips/info/{palletteId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetchInfoPallette(@PathParam("palletteId") long palletteId){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Order> orders = new ArrayList<>();

		Pallette pallette  ;
		try {
			pallette = palletteHandler.read(palletteId);

			if(pallette != null){

				List<PalletteBook> palletteBooks = pallette.getBooks();
				for(PalletteBook palletB: palletteBooks){
					PackageBook pcb =  palletB.getPackageBook();
					long pckBookId = pcb.getPackagePartId();
					int orderId = packageBookHandler.fetchOrder(pckBookId);
					Order order = orderHandler.read(orderId);
					orders.add(order);

				}


			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of pallette records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(orders).build();
	}
	@GET
	@Path("/order/{palletteId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrderByPallette(@PathParam("palletteId") Long palletteId){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Map<Object,Object> response = new HashMap<>();
		try {

			Pallette pallette = palletteHandler.read(palletteId);
			List<Order> orders = new ArrayList<>();

			if(pallette != null){
				List<PalletteBook> palletteBooks = pallette.getBooks();
				for(PalletteBook palletB: palletteBooks){
					PackageBook pcb =  palletB.getPackageBook();
					long pckBookId = pcb.getPackagePartId();
					int orderId = packageBookHandler.fetchOrder(pckBookId);
					Order order = orderHandler.read(orderId);
					orders.add(order);

				}


			}
			response.put("orders", orders);

		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of job records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(response).build();
	}
	@POST
	@Path("/updateLeftOver/{palletteId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateleftOver(@PathParam("palletteId") Long palletteId, Map<String,Object>parameter){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			Pallette pallette = palletteHandler.read(palletteId);
			int over = (Integer)parameter.get("quantity");
			typePcb type = typePcb.valueOf(parameter.get("type").toString());
			PackageBook newPcb = new PackageBook();
			newPcb.setWidthQty(over);
			newPcb.setHeightQty(1);
			newPcb.setDepthQty(1);
			newPcb.setDelivered(1);
			newPcb.setQuantity(over);
			newPcb.setType(type);
			//packageBookHandler.create(newPcb);

			int orderId = (Integer)parameter.get("orderId");
			if(pallette != null){	

				Order order = orderHandler.read(orderId);
				Package package_ = new Package();
				package_.setCount(1);
				package_.setQuantity(over);
				package_.setTypePackage(PackageType.SHRINK_WRAPPED.toString());
				Set<PackageBook> packageBooks = new HashSet<>();
				packageBooks.add(newPcb);
				package_.setPcbs(packageBooks);
				packageHandler.create(package_);
				Set<Package> packs = order.getOrderPackages();
				Package packageParent = packs.iterator().next();
				packageParent.getPackages().add(package_);
				orderHandler.update(order);

				PalletteBook palBook = new PalletteBook();
				palBook.setPackageBook(newPcb);
				PalletteBooksId id = new PalletteBooksId();
				id.setPackagePartId(newPcb.getPackagePartId());
				id.setPalletteId(palletteId);
				palBook.setId(id);
				palBook.setQuantity(1);
				pallette.getBooks().add(palBook);
				palletteHandler.update(pallette);

			}

		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of pallette records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok().build();
	}

	@GET
	@Path("/packSlip/{palletteId}/")
	public Response createPdf(@PathParam("palletteId") long palletteId) {

		File slipFile = null;
		Pallette pallette  ;
		try {
			pallette = palletteHandler.read(palletteId);

			if(pallette != null){

				List<PalletteBook> palletteBooks = pallette.getBooks();


				slipFile = File.createTempFile("slipsPDF_Info", ".pdf");
				List<ByteArrayOutputStream>  listContentPdf = new ArrayList<>();

				FileInputStream  file = new FileInputStream(new File(System.getProperty(ConfigurationConstants.PDFSLIPFR)));

				Preference packingSlip = lookupDAO.read("packingSlips", Preference.class);

				if(packingSlip != null){
					if("en".equals(packingSlip.getName())){
						file = new FileInputStream(new File(System.getProperty(ConfigurationConstants.PDFSLIP)));

					}
				}

				PdfReader reader = new PdfReader(file);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PdfStamper  stamper = new PdfStamper(reader, baos);
				listContentPdf.add(baos);
				AcroFields form = stamper.getAcroFields();
				form.setField("pallet_id", "PL"+pallette.getId());
				form.setField("barcode", "*" +  pallette.getId() + "*");				
				form.setField("slip", pallette.getPalletteSlip());
				form.setField("destination", pallette.getDestination());
				
				SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
				String d = format.format(new Date());
				form.setField("date" , d);
				
				if(pallette.getBlNumber() != null) {
					form.setField("num_bl", String.valueOf(pallette.getBlNumber()));
				}else {
					form.setField("num_bl", "");
				}
				
				Integer totalBook = 0;
				Integer qtyPcb = 0;
				int i = 0;
				for(PalletteBook pcb: palletteBooks){
					PackageBook pckBook = pcb.getPackageBook();
					int qtyInPcb = pckBook.getDepthQty()*pckBook.getHeightQty()*pckBook.getWidthQty();
					totalBook+= qtyInPcb*pcb.getQuantity();
					qtyPcb+= pcb.getQuantity();

					long pckBookId = pckBook.getPackagePartId();
					int orderId = packageBookHandler.fetchOrder(pckBookId);
					Order order = orderHandler.read(orderId);
					form.setField("order "+(i+1),order.getOrderNum());
					form.setField("isbn "+(i+1), order.getOrderPart().getPart().getIsbn());
					form.setField("title "+(i+1),order.getOrderPart().getPart().getTitle());
					String pcbType = pckBook.getDepthQty()+"*"+pckBook.getHeightQty()+"*"+pckBook.getWidthQty();
					form.setField("pcb_type "+(i+1),pcbType);
					Package package_ = packageHandler.fetchPackageByPcbId(pckBook.getPackagePartId());
					form.setField("pcb_needed "+(i+1),String.valueOf(package_.getCount()));
					form.setField("qty_pallet "+(i+1),String.valueOf(pcb.getQuantity()));
					form.setField("shipped "+(i+1),String.valueOf(pckBook.getDelivered()));
					i++;
				}

				form.setField("qty_pcb", qtyPcb.toString());
				form.setField("total_book", totalBook.toString());
				stamper.setFormFlattening(true);
				stamper.close();
				reader.close();
				Document document = new Document();

				PdfCopy copy = new PdfCopy(document, new FileOutputStream(slipFile));
				document.open();
				for(ByteArrayOutputStream br : listContentPdf){
					reader = new PdfReader(br.toByteArray());
					//document.open();
					copy.addDocument(reader);
					reader.close();
				}
				copy.freeReader(reader);
				document.close();


				HttpHeaders headers = new HttpHeaders();
				headers.put("Content-Type",  Arrays.asList("application/pdf"));
				headers.put("Content-Disposition", Arrays.asList("attachment;filename=PackagingSlip_"+pallette.getPalletteSlip()));
				byte [] out = FileUtils.readFileToByteArray(slipFile);
				headers.put("Content-Length",  Arrays.asList(String.valueOf(out.length)));
				ByteArrayInputStream input = new ByteArrayInputStream(out);



				return  Response.ok(input).build();
			}
		} catch (Exception e) {

		}
		return null;

	}

	@GET
	@Path("/alreadyShipped/{pcbId}/{palletteId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response calcAlreadyShippedQty(@PathParam("pcbId") Long pcbId,@PathParam("palletteId") Long palletteId){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		int sum = 0;

		try {

			List<PalletteBook> pallettesBook = palletteHandler.fetchPalletteBookByPcbId(pcbId);
			for(PalletteBook p : pallettesBook){
				if(!p.getId().getPalletteId().equals(palletteId)){
					sum += p.getQuantity();
				}
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of pallette records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(sum).build();
	}

	@GET
	@Path("/orderInfo/{palletteId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrderInfoPalletteByMachine(@PathParam("palletteId") Long palletteId){

		Map<String,Object> response = orderHandler.fetchOrderByPAllette(palletteId);
		return Response.ok(response).build();
	}

	@GET
	@Path("/ShippedOrder/{orderNum}/{palletteId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response calcAlreadyShippedOrderQty(@PathParam("orderNum") String orderNum,@PathParam("palletteId") Long palletteId){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		int sum = 0;

		try {
			Order order = orderHandler.readByOrderNum(orderNum);
			Set<Package> orderPackages = order.getOrderPackages();
			for(Package pack : orderPackages){
				Set<Package> packages = pack.getPackages();
				for(Package pk : packages){
					Set<PackageBook> pcbs = pk.getPcbs();
					for(PackageBook pcb : pcbs){
						long pcbId = pcb.getPackagePartId();
						List<PalletteBook> pallettesBook = palletteHandler.fetchPalletteBookByPcbId(pcbId);
						for(PalletteBook p : pallettesBook){
							if(p.getId().getPalletteId().equals(palletteId)){
								PackageBook pkBook = p.getPackageBook();
								int pcbForm = pkBook.getDepthQty()*pkBook.getHeightQty()*pkBook.getWidthQty();
								sum += p.getQuantity()*pcbForm;
							}
						}
					}
				}
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of pallette records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(sum).build();
	}
	@GET
	@Path("/withoutBl")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response ftechPalletteWithoutBl(){
		List<Pallette> pallettes = new ArrayList<>(0);
		try {
			pallettes = palletteHandler.fetchPalletteWithoutBL();
		} catch (Exception e) {

		}
		return Response.ok(pallettes).build();
	}
	@GET
	@Path("/delete/{palletteId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deletePallette(@PathParam("palletteId") long palletteId){
		
			Pallette pallette;
			try {
				pallette = palletteHandler.read(palletteId);
				palletteHandler.delete(pallette);
			} catch (PersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		return Response.ok().build();
	}
	
	@GET
	@Path("/readRelated/{palletteId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response readPallette(@PathParam("palletteId") String palletteIdStr){
			
			ObjectMapper mapper = new ObjectMapper();
			JsonNode palletteJson = null;

			try {
				
				long palletteId = 0;
				try {
					palletteId = Long.parseLong(palletteIdStr);
				} catch (Exception e) {}
				
				//get the palette
				Pallette pallette = palletteHandler.read(palletteId);
							
				if(pallette == null) {
					Map<String, String> errors = new HashMap<String, String>();
					errors.put("error", "notFound");
					return Response.ok(errors).build();
				}
				
				if(pallette.getStatusPallette() == PalletteStatus.DELIVERED || pallette.getBlNumber() != null) {
					
					Map<String, String> errors = new HashMap<String, String>();
					errors.put("error", "alreadyDelivered");
					return Response.ok(errors).build();
				}
							
				changeAdressForDislay(pallette);
				
				palletteJson = mapper.valueToTree(pallette);
				
				//get orders and their book quantities of the pallet
				Map<String,Object> ordersAndTheirBooksQty = orderHandler.fetchOrderByPAllette(palletteId);
				List<Order> orders = (List<Order>) ordersAndTheirBooksQty.get("orders");
				List<Integer> qtyBookOrders  = (List<Integer>) ordersAndTheirBooksQty.get("books");
				
				
				JsonNode ordersArray = mapper.valueToTree(orders);				
				for (int i=0;i<ordersArray.size();i++) {
					
					//merge qty books of the order with the order 
					JsonNode jn = ordersArray.get(i);									
					int qtyBooks = qtyBookOrders.get(i);
					((ObjectNode) jn).put("qtyBooks", qtyBooks);	
					
					//merge shipped to the order
					Order order = orders.get(i);					
					int orderShippedQty = 0;
					
					Set<OrderBl> orderBls = order.getOrder_Bl();
					if(orderBls != null) {
						for(OrderBl ob : orderBls) {
							orderShippedQty += ob.getQty();
						}
						
					}
					((ObjectNode) jn).put("shippedQty", orderShippedQty);	
					
					
					//merge other palett to the order
					List<Pallette> palettsOfOrders = new ArrayList<>(orderHandler.fetchPalletByOrder(order));
					int qtyOtherPalettsOrder = 0;
					for(Pallette p: palettsOfOrders) {
						if(p.getId().longValue() != palletteId && (p.getBlNumber() == null || p.getStatusPallette() != PalletteStatus.DELIVERED)){
							int qty = orderHandler.calcQtyBookInOrder(p,order);
							qtyOtherPalettsOrder += qty;
						}
						
					}
					((ObjectNode) jn).put("qtyOtherPaletts", qtyOtherPalettsOrder);
					
					
					//merge books of the order
					List<PalletteBook> palletteBooks = pallette.getBooks();
					List<PalletteBook> listBooksOfTheOrder = new ArrayList<PalletteBook>();
					for(PalletteBook palletB: palletteBooks){
						PackageBook pcb =  palletB.getPackageBook();
						long pckBookId = pcb.getPackagePartId();
						int orderId = packageBookHandler.fetchOrder(pckBookId);
						if(orderId == order.getOrderId().intValue()) {
							listBooksOfTheOrder.add(palletB);
						}
					}
					ArrayNode booksArray = mapper.valueToTree(listBooksOfTheOrder);					
					((ObjectNode) jn).putArray("books").addAll(booksArray);
					
					
					//merge 'expanded' value holder
					((ObjectNode) jn).put("expanded", Boolean.FALSE);

					//merge 'units' value holder
					((ObjectNode) jn).put("units", 0);
					
				}
				
				
				//add totalBooks to pallet object
				int totalBooks = 0;
				for(Integer qty: qtyBookOrders){					
					totalBooks+= qty;
				}
				((ObjectNode) palletteJson).put("totalBooks", totalBooks);
				
				
				//merge orders in the pallette
				((ObjectNode) palletteJson).putArray("orders").addAll((ArrayNode)ordersArray);
				
				//merge 'ship today' value holder
				if(isToday(pallette.getDelivredDate()) ) {
					((ObjectNode) palletteJson).put("shipToday", Boolean.TRUE);
				}else {
					((ObjectNode) palletteJson).put("shipToday", Boolean.FALSE);
				}
				
				
								
			} catch (Exception e) {
				Map<String, String> errors = new HashMap<String, String>();
				errors.put("error", "badRecord");
				return Response.ok(errors).build();
			}
		
		return Response.ok(palletteJson).build();
	}
	
	private void changeAdressForDislay(Pallette pallette) {
		
		//compact
		String compact = null;
		if(pallette.getDestination() != null) {
			//get the first line of the address
			String[] lines = pallette.getDestination().split("\n");
			if(lines.length > 0) {
				String line = lines[0];
				
				//remove known
				if(line.contains("Éditions"))line = line.replaceAll("Éditions", "");
				if(line.toLowerCase().contains("editions"))line = line.replaceAll("(?i)editions", "");
				if(line.toLowerCase().contains("edition"))line = line.replaceAll("(?i)edition", "");
				if(line.toLowerCase().contains("société"))line = line.replaceAll("(?i)société", "");
				
				/*String[] words = line.split(" ");
				if(words.length > 1){
					compact = words[0] + " " + words[1];
				}
				
				if(words.length == 1){
					compact = words[0];
				}*/
				
				compact = line;
			}
		}
		
		//adress to code hard code
		if(pallette.getDestination() != null && pallette.getDestination().toLowerCase().contains("interforum")
											 && pallette.getDestination().toLowerCase().contains("malesherbes")) {
			pallette.setDestination("INTF");
			compact = null;
		}

		//adress to code hard code
		if(pallette.getDestination() != null && pallette.getDestination().toLowerCase().contains("interforum")
											 && pallette.getDestination().toLowerCase().contains("ivry")) {
			pallette.setDestination("IVRY");
			compact = null;
		}
		
		if(pallette.getDestination() != null && pallette.getDestination().toLowerCase().contains("ads picardie") ) {
			pallette.setDestination("ADS PICARDIE");
			compact = null;
		}

		if(pallette.getDestination() != null && pallette.getDestination().toLowerCase().contains("editis") ) {
			pallette.setDestination("EDITIS");
			compact = null;
		}

		if(pallette.getDestination() != null && pallette.getDestination().toLowerCase().contains("le cherche midi editeur") ) {
			pallette.setDestination("CHERCHE MIDI");
			compact = null;
		}

		if(pallette.getDestination() != null && pallette.getDestination().toLowerCase().contains("setralog") ) {
			pallette.setDestination("SETRALOG");
			compact = null;
		}

		if(pallette.getDestination() != null && pallette.getDestination().toLowerCase().contains("toulon palais des congres neptune") ) {
			pallette.setDestination("TOULON NEPTUNE");
			compact = null;
		}

		
		if(pallette.getDestination() != null  && pallette.getDestination().toLowerCase().contains("place des editeurs")
				 							 && pallette.getDestination().toLowerCase().contains("italie")) {
			pallette.setDestination("PDE AVENUE D'ITALIE");
			compact = null;
		}

		if(pallette.getDestination() != null  && pallette.getDestination().toLowerCase().contains("place des editeurs")
				 								&& pallette.getDestination().toLowerCase().contains("vandrezanne")) {
			pallette.setDestination("PDE RUE VANDREZANNE");
			compact = null;
		}

		if(pallette.getDestination() != null && pallette.getDestination().toLowerCase().contains("vinci centre de congres") ) {
			pallette.setDestination("VINCI CC");
			compact = null;
		}
		
		
		if(compact !=null)pallette.setDestination(compact.toUpperCase());
	}
	
	
	
	@POST
	@Path("/readRelated/save")
	@Produces(MediaType.APPLICATION_JSON)
	public Response savePalletteForBl(String json){
		
		ObjectMapper mapper = new ObjectMapper();		

		try {
			
			JsonNode palletteJson = mapper.readTree(json);			
			
			long palletteId = palletteJson.get("id").asLong();
						
			Pallette pallette = palletteHandler.read(palletteId);

			boolean toShipToday = palletteJson.get("shipToday").asBoolean();
			
			if(toShipToday) {
				pallette.setDelivredDate(new Date());
				pallette.setStatusPallette(PalletteStatus.COMPLETE);
			}else {
				pallette.setDelivredDate(null);
				pallette.setStatusPallette(PalletteStatus.ACTIVE);
			}
			
			
			
			//update and/or add pcbs
			//get orders array from json
			JsonNode ordersJsonArray = palletteJson.get("orders");
			
			for(JsonNode orderJson : ordersJsonArray) {
				
				JsonNode booksOfTheOrder = orderJson.get("books");
				int orderId = orderJson.get("orderId").asInt();
				
				List<PalletteBook> palletteBooks = mapper.convertValue(booksOfTheOrder, new TypeReference<List<PalletteBook>>() { });
				
				for(PalletteBook pb : palletteBooks) {
					
					
					if(pb.getId() == null || pb.getId().getPalletteId() == null || pb.getId().getPackagePartId() == null) {
						
						PackageBook newPcb = new PackageBook();
						newPcb.setWidthQty(pb.getPackageBook().getWidthQty());
						newPcb.setHeightQty(pb.getPackageBook().getHeightQty());
						newPcb.setDepthQty(pb.getPackageBook().getDepthQty());
						newPcb.setDelivered(1);
						newPcb.setQuantity(pb.getQuantity());
						
						//UNDER,OVER,MATCH
						newPcb.setType(typePcb.OVER);
						
						Order order = orderHandler.read(orderId);
						Package pkg = new Package();
						pkg.setCount(1);
						pkg.setQuantity(pb.getQuantity());
						pkg.setTypePackage(PackageType.SHRINK_WRAPPED.toString());
						Set<PackageBook> packageBooks = new HashSet<>();
						packageBooks.add(newPcb);
						pkg.setPcbs(packageBooks);
						packageHandler.create(pkg);
						Set<Package> packs = order.getOrderPackages();
						Package packageParent = packs.iterator().next();
						packageParent.getPackages().add(pkg);
						orderHandler.update(order);
						
						
						
						PalletteBook palBook = new PalletteBook();
						palBook.setPackageBook(newPcb);
						PalletteBooksId id = new PalletteBooksId();
						id.setPackagePartId(newPcb.getPackagePartId());
						id.setPalletteId(palletteId);
						palBook.setId(id);
						palBook.setQuantity(1);
						pallette.getBooks().add(palBook);						
						
					}else {

						//search for the PalletteBook
						for(PalletteBook palBook : pallette.getBooks()) {
							
							if(palBook.getId().getPalletteId().longValue() ==  pb.getId().getPalletteId().longValue() &&
								palBook.getId().getPackagePartId().longValue() ==  pb.getId().getPackagePartId().longValue()) {
								
								palBook.setQuantity(pb.getQuantity());
								palBook.getPackageBook().setHeightQty(pb.getPackageBook().getHeightQty());
								palBook.getPackageBook().setWidthQty(pb.getPackageBook().getWidthQty());
								palBook.getPackageBook().setDepthQty(pb.getPackageBook().getDepthQty());
								
								packageBookHandler.update(palBook.getPackageBook());
								
								break;

							}							
									
						}
						
						
					}
					
				}
				
				
			}
			
			//remove deleted orders from the pallettes
			List<Integer> remainingOrdersIds = new ArrayList<Integer>();
			for(JsonNode orderJson : ordersJsonArray) {
				int orderId = orderJson.get("orderId").asInt();
				remainingOrdersIds.add(orderId);
			}
			
			List<PalletteBook> source = pallette.getBooks();
			List<PalletteBook> target = new ArrayList<PalletteBook>();
			for(PalletteBook pb: source){
				PackageBook pcb =  pb.getPackageBook();
				long pckBookId = pcb.getPackagePartId();
				int orderId = packageBookHandler.fetchOrder(pckBookId);
				if( remainingOrdersIds.contains(new  Integer(orderId) )) {
					target.add(pb);
				}else {
					palletteHandler.deletePalletteBook(pb);					
				}

			}
			
			pallette.setBooks(target);
			palletteHandler.update(pallette);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		
		return Response.ok().build();
	}
	
	
	
	@GET
	@Path("/toShipToday")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPallettesToShipToday(){
			
			
			
			ObjectMapper mapper = new ObjectMapper();
			
			Map<String, List<JsonNode>> groups = new HashMap<String, List<JsonNode>>();
			

			try {
				
				List<Pallette> pallettes = palletteHandler.fetchPalletteToShipToday();
				
								
				//change adress
				for(Pallette pallette : pallettes) {
					changeAdressForDislay(pallette);	
				}
				
				
				//group by adress
				for(Pallette pallette : pallettes) {
					
					if(pallette.getDestination() == null || pallette.getDestination().isEmpty()) {
						pallette.setDestination("No ship address, FIXME");
					}
					
					JsonNode palletteJson = mapper.valueToTree(pallette);
					
					Map<String,Object> ordersAndTheirBooksQty = orderHandler.fetchOrderByPAllette(pallette.getId());
					List<Integer> qtyBookOrders  = (List<Integer>) ordersAndTheirBooksQty.get("books");
					List<Order> orders = (List<Order>) ordersAndTheirBooksQty.get("orders");
					
					//add totalBooks to pallet object
					int totalBooks = 0;
					for(Integer qty: qtyBookOrders){					
						totalBooks+= qty;
					}
					((ObjectNode) palletteJson).put("totalBooks", totalBooks);
					
					if(orders != null) {
						((ObjectNode) palletteJson).put("totalOrders", orders.size());
					}else {
						((ObjectNode) palletteJson).put("totalOrders", 0);
					}
					
					
					//merge 'selected' value holder
					((ObjectNode) palletteJson).put("selected", Boolean.TRUE);
					
					if(groups.containsKey(pallette.getDestination())) {
						groups.get(pallette.getDestination()).add(palletteJson);
					}else {
						groups.put(pallette.getDestination(), new ArrayList<JsonNode>());
						groups.get(pallette.getDestination()).add(palletteJson);
					}
					
				}				

				
								
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		return Response.ok(groups).build();
	}
	
	
	private boolean isToday(Date date) {
        return isSameDay(date, Calendar.getInstance().getTime());
    }
	
	
	private boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }
    

	private boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
        		return false;
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }
	
	
	@POST
	@Path("/slips/paginated")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllPalletteNotActivePaginated(String data){
		
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		PaginatedResult paginatedResult = new PaginatedResult();		
		PalletteSearchBean searchBean = new PalletteSearchBean();		
		List<Pallette> pallettes = new ArrayList<Pallette>();
		List<Object> pageList = new ArrayList<Object>();
		
		
		ObjectMapper mapper = new ObjectMapper();
		AoData aoDataObject = AoDataParser.parse(data);
		try {

			
			int draw = aoDataObject.getAjaxRequestId();
			int start = aoDataObject.getStartIndex();
			int pageLength = aoDataObject.getPageLength();
			
			//logger.debug("get page");
			//logger.debug("draw:" + draw);
			//logger.debug("start:" + start);
			//logger.debug("pageLength:" + pageLength);
			
			Integer count = 0;		
			

			//sorting
			String sortingColumnName = aoDataObject.getSortingColumnName();
			
			OrderBy orderBy = new OrderBy();			
			orderBy.setDirection(aoDataObject.getOrderBy());

			
			if(sortingColumnName.equals("id"))orderBy.setName("id");
			if(sortingColumnName.equals("blNumber"))orderBy.setName("blNumber");
			if(sortingColumnName.equals("destination"))orderBy.setName("destination");
			if(sortingColumnName.equals("statusPallette"))orderBy.setName("statusPallette");
			if(sortingColumnName.equals("delivredDate"))orderBy.setName("delivredDate");
			
			searchBean.getOrderByList().clear();
			searchBean.addOrderBy(orderBy);
			//-----------------------------------------------------------------------------------------------------
			
			if(aoDataObject.isColumnsFiltering() && !aoDataObject.isGeneralFiltering()) {
				//-----------------------------------------------------------------------------------------------------
				
			
				//filtering
				for(String columnNameToFilter  : aoDataObject.getColumnsFilters().keySet()) {
					String filterValue = aoDataObject.getColumnsFilters().get(columnNameToFilter);
					
					if(columnNameToFilter.equals("id")) {
						try {	searchBean.setId(Long.parseLong(filterValue));} catch (Exception e) {}
					}
					
					if(columnNameToFilter.equals("blNumber")) {
						try {	searchBean.setBlNumber(Integer.parseInt(filterValue));} catch (Exception e) {}
					}
					
					if(columnNameToFilter.equals("destination")) {
						try {	searchBean.setDestination(filterValue);} catch (Exception e) {}
					}
					
					if(columnNameToFilter.equals("statusPallette")) {
						try {	searchBean.setStatusPallette(PalletteStatus.valueOf(filterValue));} catch (Exception e) {}
					}
					
					if(columnNameToFilter.equals("delivredDate")) {
						try {					
							SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
						    Date date = parser.parse(filterValue); 
							searchBean.setDelivredDate(date);
						} catch (Exception e) {}
					}
				}
				
				searchBean.setResultOffset(null);
				searchBean.setMaxResults(null);
				pallettes = palletteHandler.readAllPalletteComplete(searchBean);
				count = pallettes.size();
				
				searchBean.setResultOffset(start);
				searchBean.setMaxResults(pageLength);
				pallettes = palletteHandler.readAllPalletteComplete(searchBean);
		

				//-----------------------------------------------------------------------------------------------------
			}else if(aoDataObject.isGeneralFiltering()) {
				
				pallettes = palletteHandler.fullSearch(aoDataObject.getGeneralFilter(), null, null);
				count = pallettes.size();				
				pallettes = palletteHandler.fullSearch(aoDataObject.getGeneralFilter(), pageLength, start);
				
			}else {
				
				searchBean.setResultOffset(null);
				searchBean.setMaxResults(null);
				pallettes = palletteHandler.readAllPalletteComplete(searchBean);
				count = pallettes.size();
				
				searchBean.setResultOffset(start);
				searchBean.setMaxResults(pageLength);
				pallettes = palletteHandler.readAllPalletteComplete(searchBean);
			}
			
			
			
			
			//-----------------------------------------------------------------------------------------------------
			

			if(pallettes.size() > 0){
				for(Pallette pallette :pallettes){
					
					JsonNode palletteNode = mapper.valueToTree(pallette);				
					
					
					List<PalletteBook> palletttePCb = pallette.getBooks();
					
					int pcbSum = 0;
					for(PalletteBook pcb: palletttePCb){
						pcbSum+= pcb.getQuantity();
					}
					((ObjectNode) palletteNode).put("qtyPcbInPallette", pcbSum);	
					

					int bookSum = 0;
					for(PalletteBook pcb: palletttePCb){
						PackageBook pckBook = pcb.getPackageBook();
						int qtyInPcb = pckBook.getDepthQty()*pckBook.getHeightQty()*pckBook.getWidthQty();
						bookSum+= qtyInPcb*pcb.getQuantity();
					}
					((ObjectNode) palletteNode).put("qtyBookInPallette", bookSum);
					
					pageList.add(palletteNode);
				}
			}
			
			
			
			
			
			paginatedResult.setDraw(draw);
			paginatedResult.setRecordsFiltered(count);
			paginatedResult.setRecordsTotal(count);
			paginatedResult.setData(pageList);
			
			
			
			
			
			
			
			
			} catch (Exception e) {
			
				e.printStackTrace();
				
				constraintViolationsMessages.put("errors", "An error occurred while reading the list of roll records!");
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				return inputResponseError.createResponse();
			}
			
			
			return Response.ok(paginatedResult).build();		
	}
	
}
