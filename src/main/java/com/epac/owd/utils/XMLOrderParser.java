package com.epac.owd.utils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.epac.cap.model.BindingType;
import com.epac.cap.model.Customer;
import com.epac.cap.model.Lamination;
import com.epac.cap.model.Order;
import com.epac.cap.model.OrderPart;
import com.epac.cap.model.OrderPartId;
import com.epac.cap.model.PaperType;
import com.epac.cap.model.Part;
import com.epac.owd.config.Configuration;
import com.epac.owd.config.ConfigurationConstants;
import com.epac.owd.service.CAPWebService;

@Component
public class XMLOrderParser {

	private CAPWebService capWebService;

	private static boolean shutdown;

	XPathFactory xpf = XPathFactory.newInstance();
	XPath xPath = xpf.newXPath();
	PaperType paper;
	Lamination lamination;
	BindingType bindingType;

	boolean error = false;
	String errorMessage;

	String content = "";
	String technicalContent = "";

	static {
		Configuration.load("owd.properties");
	}

	public void parseXML(File xmlFile) {

		List<Order> orders = new ArrayList<Order>();
		Order order;
		OrderPart orderPart;
		Set<OrderPart> setOrderParts = new HashSet<OrderPart>();
		Customer customer;
		Part bookPart = null;
		String filename = "";
		File currentExcelFile = null;

		try {

			content = "";
			technicalContent = "";

			// Customer Data
			XPathExpression requestorEmail = xPath.compile("requestorEmail");
			XPathExpression fisrtName = xPath.compile("fisrtName");
			XPathExpression lastName = xPath.compile("lastName");

			// Order Data
			XPathExpression PONumber = xPath.compile("PONumber");
			XPathExpression dueDate = xPath.compile("dueDate");
			XPathExpression note = xPath.compile("note");

			// OrderPart Data
			XPathExpression quantity = xPath.compile("quantity");

			currentExcelFile = xmlFile;

			LogUtils.debug("element" + " is " + xmlFile + "\n ");
			filename = xmlFile.toString().substring(xmlFile.toString().lastIndexOf("/") + 1);
			content += "File : incoming/" + filename + "\n";
			content += "Date: " + TimeUtils.nowInUTC() + "\n";
			content += "Here's the rssults: \n";

			File tmpFile = File.createTempFile((System.currentTimeMillis() + new String("_")) + filename, null);
			tmpFile.deleteOnExit();

			System.out.println(tmpFile.getName());
			InputSource inputSource = new InputSource(xmlFile.getPath());
			NodeList contentNode = (NodeList) xPath.evaluate("/ordersSubmissionRequest", inputSource,
					XPathConstants.NODESET);
			LogUtils.debug("There's " + contentNode.getLength() + " ordersSubmissionRequest to be processed ");

			NodeList orderNodes = (NodeList) xPath.evaluate("/ordersSubmissionRequest/order", inputSource,
					XPathConstants.NODESET);
			NodeList partNodes = (NodeList) xPath.evaluate("/ordersSubmissionRequest/part", inputSource,
					XPathConstants.NODESET);

			LogUtils.debug("There's " + orderNodes.getLength() + " orders to be processed ");
			LogUtils.debug("There's " + partNodes.getLength() + " parts to be processed ");
			for (int x = 0; x < orderNodes.getLength(); x++) {

				order = new Order();
				// order.setSource("AUTO");
				orderPart = new OrderPart();
				bookPart = new Part();
				paper = new PaperType();
				// job = new Job();
				lamination = new Lamination();
				bindingType = new BindingType();
				Node orderElement = orderNodes.item(x);
				// Customer
				customer = new Customer();
				customer.setEmail((String) requestorEmail.evaluate(orderElement, XPathConstants.STRING));
				customer.setFirstName((String) fisrtName.evaluate(orderElement, XPathConstants.STRING));
				customer.setLastName((String) lastName.evaluate(orderElement, XPathConstants.STRING));
				customer.setCreatedDate(new Date());
				customer.setCreatorId("System_Auto");

				order.setCustomer(customer);
				NodeList orderPartNodes = (NodeList) xPath.evaluate("/ordersSubmissionRequest/order/part", inputSource,
						XPathConstants.NODESET);
				System.out.println("We have " + orderPartNodes.getLength() + " Parts in the " + x + " order");

				// Order
				order.setOrderNum((String) PONumber.evaluate(orderElement, XPathConstants.STRING));
				order.setDueDate(formatDate((String) dueDate.evaluate(orderElement, XPathConstants.STRING)));
				order.setNotes((String) note.evaluate(orderElement, XPathConstants.STRING));

				// Part
				XPathExpression ISBN = xPath.compile("ISBN");
				for (int i = 0; i < orderPartNodes.getLength(); i++) {
					bookPart = populatePart(orderPartNodes.item(i));
					String partNumber = CAPWebService.addPart(bookPart);
					bookPart.setPartNum((partNumber != null)?partNumber:null);

				}

				// TODO : send the Part to be saved in the DataBase

				orderPart.setQuantity(((Double) quantity.evaluate(orderElement, XPathConstants.NUMBER)).intValue());
				orderPart.setPart(bookPart);
				OrderPartId orderPartId = new OrderPartId(null, bookPart.getPartNum());
				//orderPart.setId(orderPartId);
//				order.setOrderPart(orderPart);
				setOrderParts.add(orderPart);
				order.setOrderParts(setOrderParts);
				if (error == true)
					order.setStatus(com.epac.cap.model.Order.OrderStatus.ERROR.getName());
				else
					order.setStatus(com.epac.cap.model.Order.OrderStatus.PENDING.getName());
				order.setPriority(com.epac.cap.model.Order.OrderPriorities.NORMAL.getName());
				order.setCreatedDate(new Date());
				order.setCreatorId("System_Auto");
				orders.add(order);

			}
			Part part;
			for (int i = 0; i < partNodes.getLength(); i++) {
				part = null;
				part = populatePart(partNodes.item(i));
				// TODO : send the Part to be saved in the DataBase
				if (!error)
					CAPWebService.addPart(part);
//				String partNumber = CAPWebService.addPart(part);
			}

			if (orders.size() > 0) {
				// TODO : send the order to be saved in the DB
				
				
				// Send Email with the different orders found
				String body = "";

				body = "This is the report of the Orders Metadata submitted recently \n";
				body += "We have " + orders.size() + " orders : \n";
				for (Order order2 : orders) {
					if (!error){
						CAPWebService.addOrder(order2);
						for (OrderPart orderPart2 : order2.getOrderParts()) {
							
							part = null;
							part = orderPart2.getPart();
							System.out.println(part.getIsbn());
							body += "Order " + order2.getOrderNum() + " With Book "
									+ order2.getOrderPart().getPart().getIsbn() + "\n";
						}
					}
					
				}
				body += "" + content;
				body += "Cordially, \n" + "EPAC Technologies\n";
				String subject = "Order Metadata Report";

				

			}

			LogUtils.debug("XML successfully parsed, (" + orders.size() + ") orders found");
			// FileObject tempFile =
			// manager.resolveFile(RemoteFileFetcher.createConnectionString(System.getProperty(ConfigurationConstants.SFTP_TEMP_DIR)
			// + (filename)));
			File tempFile = new File(System.getProperty(ConfigurationConstants.LOCAL_ORDER_OLD_DIR) + "/"
					+ new Date().getTime() + "_" + (filename));
			FileUtils.moveFile(xmlFile, tempFile);
			LogUtils.debug("file moved to " + tempFile.toString());

		} catch (XPathExpressionException xpe) {
			xpe.printStackTrace();
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		} catch (Exception e) {
			LogUtils.error("Error parsing the Metadata files : ", e);
			technicalContent += "" + e + "\n";
			String body = "";
			body = "This is the report of the Orders Metadata submitted recently \n";
			body += technicalContent;
			body += "Cordially, \n" + "EPAC Technologies";

			if (currentExcelFile != null) {
				try {
					File tempFile = new File(System.getProperty(ConfigurationConstants.LOCAL_ORDER_OLD_DIR) + "/"
							+ new Date().getTime() + "_" + (filename));
					FileUtils.moveFile(currentExcelFile, tempFile);
					LogUtils.debug("file moved to " + tempFile.toString());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

		}
	}

	private Part populatePart(Node partNode) throws XPathExpressionException {

		error = false;

		//String localPath = System.getProperty(ConfigurationConstants.PDF_FILE_DIR);
		// String partNb = CAPWebService.generatePartNumber();

		XPathExpression deleteAfterwards = xPath.compile("deleteAfterwards");

		// Criteria
		XPathExpression selfCover = xPath.compile("selfCover");
		XPathExpression threeHoleDrill = xPath.compile("ThreeHoleDrill");
		XPathExpression perforation = xPath.compile("perforation");
		XPathExpression shrinkWrap = xPath.compile("shrinkWrap");

		// Part Data
		// XPathExpression partNumber = xPath.compile("partNumber");
		XPathExpression ISBN = xPath.compile("ISBN");
		XPathExpression title = xPath.compile("title");
		XPathExpression textColors = xPath.compile("textColors");
		// XPathExpression coverColors = xPath.compile("part/coverColors");
		XPathExpression partVersion = xPath.compile("partVersion");
		XPathExpression author = xPath.compile("author");
		XPathExpression publishDate = xPath.compile("publishDate");
		XPathExpression partDimension = xPath.compile("partDimension");
		// Paper Type Data
		XPathExpression paperType = xPath.compile("paperType");
		// Lamination Data
		XPathExpression partLamination = xPath.compile("lamination");
		// Binding Type Data
		XPathExpression partBindingType = xPath.compile("bindingType");
		XPathExpression pageCount = xPath.compile("pageCount");
		XPathExpression textFileURL = xPath.compile("textFileURL");
		XPathExpression coverFileURL = xPath.compile("coverFileURL");
		XPathExpression spineThickness = xPath.compile("spineThickness");
		XPathExpression partNote = xPath.compile("partNote");

		/*
		 * String currentISBN = (String) ISBN.evaluate(partNode,
		 * XPathConstants.STRING); Part bookPart =
		 * null;//CAPWebService.getPart(currentISBN);
		 * 
		 * if (bookPart != null) { return bookPart; } else
		 */
		Part bookPart = new Part();

		// bookPart.setPartNum((String)
		// partNumber.evaluate(partNode,XPathConstants.STRING));
		// bookPart.setPartNum(partNb);
		bookPart.setTitle((String) title.evaluate(partNode, XPathConstants.STRING));
		bookPart.setAuthor((String) author.evaluate(partNode, XPathConstants.STRING));
		bookPart.setVersion(((Double) partVersion.evaluate(partNode, XPathConstants.NUMBER)).intValue());
		bookPart.setPublishDate(formatDate((String) publishDate.evaluate(partNode, XPathConstants.STRING)));
		bookPart.setColors((String) textColors.evaluate(partNode, XPathConstants.STRING));

		float[] dimensions = formatDimension((String) partDimension.evaluate(partNode, XPathConstants.STRING));
		if (dimensions != null) {
			bookPart.setLength(dimensions[0]);
			bookPart.setWidth(dimensions[1]);
		}
		int lookUpIndex = -1;
		lookUpIndex = checkLookUp((String) paperType.evaluate(partNode, XPathConstants.STRING), "PaperType");
		if (lookUpIndex != -1)
			bookPart.setPaperType(ConfigurationConstants.paperTypes.get(lookUpIndex));
		else {
			error = true;
			content += "<p>PaperType " + (String) paperType.evaluate(partNode, XPathConstants.STRING)
					+ " does not exist in the database</p> \n";
		}

		lookUpIndex = checkLookUp((String) partLamination.evaluate(partNode, XPathConstants.STRING), "Lamination");
		if (lookUpIndex != -1)
			bookPart.setLamination(ConfigurationConstants.laminations.get(lookUpIndex));
		else {
			error = true;
			content += "<p>Lamination " + (String) partLamination.evaluate(partNode, XPathConstants.STRING)
					+ " does not exist in the database</p> \n";
		}

		lookUpIndex = checkLookUp((String) partBindingType.evaluate(partNode, XPathConstants.STRING), "BindingType");
		if (lookUpIndex != -1)
			bookPart.setBindingType(ConfigurationConstants.bindingTypes.get(lookUpIndex));
		else {
			error = true;
			content += "<p>BindingType " + (String) partBindingType.evaluate(partNode, XPathConstants.STRING)
					+ " does not exist in the database</p> \n";
		}

		bookPart.setPagesCount(Integer.parseInt((String) pageCount.evaluate(partNode, XPathConstants.STRING)));

		bookPart.setThickness((Float) spineThickness.evaluate(partNode, XPathConstants.STRING));
		bookPart.setNotes((String) partNote.evaluate(partNode, XPathConstants.STRING));

		/*
		 * bookPart.setCategory(coverPC); bookPart.setColors((String)
		 * coverColors.evaluate(partNode, XPathConstants.STRING));
		 * bookPart.setFilePath((String) coverFileURL.evaluate(partNode,
		 * XPathConstants.STRING)); PartCritiriaId critiriaId = new
		 * PartCritiriaId(bookPart.getPartNum(), (String)
		 * selfCover.evaluate(partNode, XPathConstants.STRING));
		 * critiria.setId(critiriaId); critirias.add(critiria);
		 */

		Set<String> criterias = new HashSet<String>();
		if ("Yes".equalsIgnoreCase((String) selfCover.evaluate(partNode, XPathConstants.STRING)))
			criterias.add("SELFCOVER");
		if ("Yes".equalsIgnoreCase((String) threeHoleDrill.evaluate(partNode, XPathConstants.STRING)))
			criterias.add("3HOLEDRILL");
		if ("Yes".equalsIgnoreCase((String) perforation.evaluate(partNode, XPathConstants.STRING)))
			criterias.add("PERFORATION");
		if ("Yes".equalsIgnoreCase((String) shrinkWrap.evaluate(partNode, XPathConstants.STRING)))
			criterias.add("SHRINKWRAP");
		bookPart.setCritirias(criterias);

		bookPart.setIsbn((String) ISBN.evaluate(partNode, XPathConstants.STRING));
		bookPart.setSoftDelete(
				"Yes".equalsIgnoreCase((String) deleteAfterwards.evaluate(partNode, XPathConstants.STRING)) ? true
						: false);

		// TODO : Download the Cover and text files
		//FileDownloader.downloadFile((String) textFileURL.evaluate(partNode, XPathConstants.STRING));
		/*
		 * try { PDFFilesValidator.download((String)
		 * textFileURL.evaluate(partNode, XPathConstants.STRING), localPath +
		 * "/" + bookPart.getIsbn() + "/" + bookPart.getIsbn() + ".text.pdf",
		 * bookPart); } catch (Exception e) {
		 * LogUtils.error("Error in downloading the Text pdf file !!"); } try {
		 * PDFFilesValidator.download((String) coverFileURL.evaluate(partNode,
		 * XPathConstants.STRING), localPath + "/" + bookPart.getIsbn() + "/" +
		 * bookPart.getIsbn() + ".cover.pdf", bookPart); } catch (Exception e) {
		 * LogUtils.error("Error in downloading the Cover pdf file !!"); }
		 */

		String filePath = (String) coverFileURL.evaluate(partNode,XPathConstants.STRING) + ";" + (String) textFileURL.evaluate(partNode,XPathConstants.STRING);
		bookPart.setFilePath(filePath);
		bookPart.setFileName(bookPart.getIsbn() + ".text.pdf");
		bookPart.setCreatedDate(new Date());
		bookPart.setCreatorId("System_Auto");

		return bookPart;
	}

	private float[] formatDimension(String dimensions) throws NumberFormatException {
		String textDimensions = dimensions;
		int xIndex = 0;
		float height = 0;
		float width = 0;

		float[] dimension = new float[2];
		if (dimensions.contains("("))
			textDimensions = dimensions.substring(0, dimensions.indexOf("(") - 1).trim();
		if (textDimensions.toLowerCase().contains("x"))
			xIndex = textDimensions.toLowerCase().indexOf("x");

		if (xIndex == 0) {
			//dimensionReaderError = true;
			return dimension;
		}

		if (textDimensions.contains("mm")) {
			textDimensions = textDimensions.substring(0, textDimensions.indexOf("mm") - 1);

			height = Float.parseFloat(textDimensions.substring(0, xIndex));
			width = Float.parseFloat(textDimensions.substring(xIndex + 1, textDimensions.length()));
		} else {
			if (textDimensions.contains("-")) {
				String textHeight = textDimensions.substring(xIndex + 1, textDimensions.length()).trim();
				String textWidth = textDimensions.substring(0, xIndex).trim();

				height = (textHeight.contains("-")) ? stringToFloat(textHeight) : Float.parseFloat(textHeight);
				width = (textWidth.contains("-")) ? stringToFloat(textWidth) : Float.parseFloat(textWidth);

			}
		}

		dimension[0] = height;
		dimension[1] = width;
		return dimension;

	}

	public static void main(String[] args) {
		System.out.println((double) 5 / 2);
	}

	private float stringToFloat(String dimension) {
		String[] dimensionParts = dimension.split("-");
		String[] fractions = dimensionParts[1].split("/");
		float value = Float.parseFloat(dimensionParts[0]);
		value += (float) (Float.parseFloat(fractions[0]) / Float.parseFloat(fractions[1]));
		return value;
	}

	public Date formatDate(String date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		Date returnDate = new Date(0);

		try {
			returnDate = dateFormat.parse(date);
		} catch (ParseException e) {
			LogUtils.error("Problems when parsing the publishDate time ", e);
			e.printStackTrace();
		}

		return returnDate;
	}

	public int checkLookUp(String item, String type) {
		int exists = -1;
		List<String> items = new ArrayList<String>();
		if (type.equalsIgnoreCase("PaperType")) {
			for (PaperType paperType : ConfigurationConstants.paperTypes)
				items.add(paperType.getId());
		} else if (type.equalsIgnoreCase("BindingType")) {
			for (BindingType bindingType : ConfigurationConstants.bindingTypes)
				items.add(bindingType.getId());
		} else if (type.equalsIgnoreCase("Lamination")) {
			for (Lamination lamination : ConfigurationConstants.laminations)
				items.add(lamination.getId());
		}

		if (items.contains(item))
			exists = items.indexOf(item);

		return exists;
	}

}