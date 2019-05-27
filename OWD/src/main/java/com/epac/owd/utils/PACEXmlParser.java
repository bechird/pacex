package com.epac.owd.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.epac.cap.common.EpacException;
import com.epac.cap.model.BindingType;
import com.epac.cap.model.Client;
import com.epac.cap.model.Customer;
import com.epac.cap.model.Lamination;
import com.epac.cap.model.Order;
import com.epac.cap.model.OrderPart;
import com.epac.cap.model.PNLData;
import com.epac.cap.model.PaperType;
import com.epac.cap.model.Part;
import com.epac.owd.config.Configuration;
import com.epac.owd.config.ConfigurationConstants;
import com.epac.owd.service.CAPWebService;

@Component
public class PACEXmlParser {

	private boolean error = false;
	private String errorMessage = "";
	
	static {
		Configuration.load("owd.properties");
	}


	public void parse(String xmlFile) {
		
		XMLOrderParser xmlOrderParser = new XMLOrderParser();
		
		try {
			LogUtils.debug("element" + " is " + xmlFile + " ");
			String filename = xmlFile.toString().substring(xmlFile.toString().lastIndexOf("/") + 1);
			
			
			File inputFile = new File(xmlFile);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();
			System.out.print("Root element: ");
			String rootElement = doc.getDocumentElement().getNodeName();
			System.out.println(rootElement);
			if ("FMPXMLRESULT".equalsIgnoreCase(rootElement)) {
				NodeList nList = doc.getElementsByTagName("ROW");
				System.out.println("----------------------------");
				List<Order> orders = new ArrayList<Order>();
				for (int temp = 0; temp < nList.getLength(); temp++) {
					Node nNode = nList.item(temp);
					System.out.println("\nCurrent Element :");
					System.out.print(nNode.getNodeName());
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						NodeList metadataNameList = eElement.getElementsByTagName("COL");
						
						// Here where we can fill the Order and part data
						String theValue;
						Order order = new Order();
						OrderPart orderPart = new OrderPart();
						Part bookPart = new Part();
						Set<OrderPart> setOrderParts = new HashSet<OrderPart>();
						Node node1 = metadataNameList.item(0);
						if (node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("PO Number : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							order.setOrderNum(theValue);
						}
						node1 = metadataNameList.item(1);
						if (node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("Due Date : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							order.setDueDate(formatDate(theValue));
						}
						node1 = metadataNameList.item(2);
						if (node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("Quantity : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							try{
								orderPart.setQuantity(Integer.parseInt(theValue));
							}catch(NumberFormatException nfe){
								errorMessage += "- Quantity value is wrong: " + theValue + ".\n";
							}
						}
						node1 = metadataNameList.item(3);
						if (node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("ISBN : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							bookPart.setIsbn(theValue.replace("-", ""));
						}
						node1 = metadataNameList.item(4);
						if (node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("Title : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							bookPart.setTitle(theValue);
						}
						node1 = metadataNameList.item(5);
						if (node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("Text Colors : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							bookPart.setColors(theValue);
						}
						node1 = metadataNameList.item(6);
						if (node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("Width : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							try{
								bookPart.setWidth(Float.parseFloat(theValue));
							}catch(NumberFormatException nfe){
								errorMessage += "- Width value is wrong: " + theValue + ".\n";
							}
						}
						node1 = metadataNameList.item(7);
						if (node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("Length : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							try{
								bookPart.setLength(Float.parseFloat(theValue));
							}catch(NumberFormatException nfe){
								errorMessage += "- Length value is wrong: " + theValue + ".\n";
							}
						}
						node1 = metadataNameList.item(8);
						if (node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("PaperType : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							int lookUpIndex = -1;
							lookUpIndex = checkLookUp(theValue, "PaperType");
							if (lookUpIndex != -1)
								bookPart.setPaperType(ConfigurationConstants.paperTypes.get(lookUpIndex));
							else {
								//error = true;
								errorMessage += "- PaperType " + theValue
										+ " does not exist in the database.\n";
								//bookPart.setPaperType(ConfigurationConstants.paperTypes.get(lookUpIndex));
							}
						}
						node1 = metadataNameList.item(9);
						if (node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("Lamination : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							int lookUpIndex = -1;
							lookUpIndex = checkLookUp(theValue, "Lamination");
							if (lookUpIndex != -1)
								bookPart.setLamination(ConfigurationConstants.laminations.get(lookUpIndex));
							else {
								//error = true;
								errorMessage += "- Lamination " + theValue
										+ " does not exist in the database.\n";
							}
						}
						node1 = metadataNameList.item(10);
						if (node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("Binding Type : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							int lookUpIndex = -1;
							lookUpIndex = checkLookUp(theValue, "BindingType");
							if (lookUpIndex != -1)
								bookPart.setBindingType(ConfigurationConstants.bindingTypes.get(lookUpIndex));
							else {
								//error = true;
								errorMessage += "- BindingType " + theValue
										+ " does not exist in the database.\n";
							}
						}
						node1 = metadataNameList.item(11);
						if (node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("Page Count : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							try{
								bookPart.setPagesCount(Integer.parseInt(theValue));
							}catch(NumberFormatException nfe){
								errorMessage += "- Pages count value is wrong: " + theValue + ".\n";
							}
						}
						String textFileURL = null;
						node1 = metadataNameList.item(12);
						if (node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("Text File URL : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							textFileURL = exists(theValue);
							if ( textFileURL == null) {
								error = true;
								errorMessage += "- Text File URL is not correct: " + theValue + ".\n";
							}
						}
						String coverfileURL = null;
						node1 = metadataNameList.item(13);
						if (node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("Cover File URL : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							coverfileURL = exists(theValue);
							if ( coverfileURL == null) {// TODO how if self cover
								error = true;
								errorMessage += "- Cover File URL is not correct: " + theValue + ".\n";
							}
						}
						node1 = metadataNameList.item(14);
						if (node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("Spine Thickness : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							try{
								bookPart.setThickness(Float.parseFloat(theValue));
							}catch(NumberFormatException nfe){
								errorMessage += "- Thickness value is wrong: " + theValue + ".\n";
							}
						}

						Set<String> criterias = new HashSet<String>();

						node1 = metadataNameList.item(15);
						if (node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("Self Cover : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							if ("Yes".equalsIgnoreCase(theValue))
								criterias.add("SELFCOVER");
						}
						node1 = metadataNameList.item(16);
						if (node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("3 Hole Drill : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							if ("Yes".equalsIgnoreCase(theValue))
								criterias.add("3HOLEDRILL");
						}
						node1 = metadataNameList.item(17);
						if (node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("Perforation : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							if ("Yes".equalsIgnoreCase(theValue))
								criterias.add("PERFORATION");
						}
						node1 = metadataNameList.item(18);
						if (node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("shrinkwrap : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							if ("Yes".equalsIgnoreCase(theValue))
								criterias.add("SHRINKWRAP");
						}
						bookPart.setCritirias(criterias);
						
						String filePath = coverfileURL + ";" + textFileURL;
						bookPart.setFilePath(filePath);
						bookPart.setFileName(bookPart.getIsbn() + ".text.pdf");
						
						node1 = metadataNameList.item(19);
						if (node1 != null && node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("Spine Type : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							bookPart.setSpineType(theValue);
						}
						
						node1 = metadataNameList.item(20);
						if (node1 != null && node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("Head & Tail Bands : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							bookPart.setHeadTailBands(theValue);
						}
						
						node1 = metadataNameList.item(21);
						if (node1 != null && node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("Color For PaceX CVR : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							bookPart.setCoverColor(theValue);
						}
						
						node1 = metadataNameList.item(22);
						if (node1 != null && node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("Creator : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							String[] namePortions = theValue.split(" ");
							if(namePortions.length > 1){
								order.setCustomer(new Customer(namePortions[0], namePortions[1]));
							}
						}
						
						node1 = metadataNameList.item(23);
						if (node1 != null && node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("Wire Color : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							bookPart.setWireColor(theValue);
						}
						
						node1 = metadataNameList.item(24);
						if (node1 != null && node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("Client : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							int lookUpIndex = -1;
							lookUpIndex = checkLookUp(theValue, "Client");
							if (lookUpIndex != -1){
								order.setClientId(theValue);
							}else{
								errorMessage += "- Client " + theValue
										+ " does not exist in the database.\n";
							}
						}
						// For now we will only go with PNL level 'part':
						// order.setPnlData(new PNLData());
						node1 = metadataNameList.item(25);
						if (node1 != null && node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("pnlLocation : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							bookPart.setPnlLocation(theValue);
						}
						 
						node1 = metadataNameList.item(26);
						if (node1 != null && node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("pnlLevel : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							//order.getPnlData().setPnlLevel(theValue);
						}
						
						node1 = metadataNameList.item(27);
						if (node1 != null && node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("pnlNotNeeded : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							bookPart.setPnlNotNeeded(theValue);
						}
						
						node1 = metadataNameList.item(28);
						if (node1 != null && node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("pnlTemplate : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							bookPart.setPnlTemplateId(theValue);
						}
						
						node1 = metadataNameList.item(29);
						if (node1 != null && node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("pnlLanguage : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							//order.getPnlData().setPnlLanguage(theValue);
						}
						
						node1 = metadataNameList.item(30);
						if (node1 != null && node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("pnlPageNumber : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							bookPart.setPnlPageNumber(theValue);
						}
						
						node1 = metadataNameList.item(31);
						if (node1 != null && node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("pnlNumber : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							bookPart.setPnlPrintingNumber(theValue);
						}
						
						node1 = metadataNameList.item(32);
						if (node1 != null && node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("pnlHmargin : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							bookPart.setPnlHmargin(theValue);
						}
						
						node1 = metadataNameList.item(33);
						if (node1 != null && node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("pnlVmargin : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							bookPart.setPnlVmargin(theValue);
						}
						
						node1 = metadataNameList.item(34);
						if (node1 != null && node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("pnlFontType : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							bookPart.setPnlFontType(theValue);
						}
						
						node1 = metadataNameList.item(35);
						if (node1 != null && node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("pnlFontSize : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							bookPart.setPnlFontSize(theValue);
						}
						
						node1 = metadataNameList.item(36);
						if (node1 != null && node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("pnlFontBold : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							//order.getPnlData().setPnlFontBold(Boolean.valueOf(theValue));
						}
						
						node1 = metadataNameList.item(37);
						if (node1 != null && node1.getNodeType() == Node.ELEMENT_NODE) {
							Element DATA = (Element) node1;
							System.out.print("pnlFontItalic : ");
							theValue = DATA.getFirstChild() != null ? (DATA.getFirstChild().getFirstChild() != null ? (DATA.getFirstChild().getFirstChild().getNodeValue() != null ? DATA.getFirstChild().getFirstChild().getNodeValue().trim() : "") : "") : "";
							System.out.println(theValue);
							//order.getPnlData().setPnlFontItalic(Boolean.valueOf(theValue));
						}
						
						bookPart.setCreatedDate(new Date());
						bookPart.setCreatorId("System_Auto");
						//if(!error){
							//try {
								//String partNumber = CAPWebService.addPart(bookPart);
								//LogUtils.debug("Part successfully persisted " + partNumber);
								//bookPart.setPartNum(partNumber);
								
								orderPart.setPart(bookPart);
								
								setOrderParts.add(orderPart);
								order.getOrderParts().addAll(setOrderParts);
								order.setOrderPart(orderPart);
								
							//} catch (EpacException e) {
							//	e.printStackTrace();
							//}
							
						//}
						
						order.setPriority(com.epac.cap.model.Order.OrderPriorities.NORMAL.getName());
						order.setCreatedDate(new Date());
						order.setCreatorId("System_Auto");
						order.setNotes(errorMessage.length() > 200 ? errorMessage.substring(0, 199) : errorMessage);
						
						if (error == true || !StringUtils.isBlank(errorMessage))
							order.setStatus(com.epac.cap.model.Order.OrderStatus.ERROR.getName());
						else
							order.setStatus(com.epac.cap.model.Order.OrderStatus.PENDING.getName());
						
						orders.add(order);
					}
					errorMessage = "";
				}
				if (orders.size() > 0) {
					// TODO : send the order to be saved in the DB
					for (Order order2 : orders) {
						//if(!error){
							try {
								CAPWebService.addOrder(order2);
							} catch (EpacException e) {
								e.printStackTrace();
							}
							
						//}
					}
					

				}
				
				LogUtils.debug("XML successfully parsed, (" + orders.size() + ") orders found");
		
				File tempFile = new File(System.getProperty(ConfigurationConstants.LOCAL_ORDER_OLD_DIR) + "/"
						+ new Date().getTime() + "_" + (filename));
				FileUtils.moveFile(new File(xmlFile), tempFile);
				LogUtils.debug("file moved to " + tempFile.toString());
				inputFile = null;
				doc = null;
				dBuilder = null;
			} else if ("ordersSubmissionRequest".equalsIgnoreCase(rootElement))
				xmlOrderParser.parseXML(new File(xmlFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		item = item.toLowerCase();
		int exists = -1;
		List<String> items = new ArrayList<String>();
		if (type.equalsIgnoreCase("PaperType")) {
			for (PaperType paperType : ConfigurationConstants.paperTypes)
				items.add(paperType.getId().toLowerCase());
		}  else if (type.equalsIgnoreCase("Client") && ConfigurationConstants.clients != null) {
			for (Client cl : ConfigurationConstants.clients)
				items.add(cl.getId().toLowerCase());
		} else if (type.equalsIgnoreCase("BindingType")) {
			for (BindingType bindingType : ConfigurationConstants.bindingTypes)
				items.add(bindingType.getName().toLowerCase());
		} else if (type.equalsIgnoreCase("Lamination") && ConfigurationConstants.laminations != null) {
			for (Lamination lamination : ConfigurationConstants.laminations)
				items.add(lamination.getName().toLowerCase());
		}

		if (items.contains(item))
			exists = items.indexOf(item);

		return exists;
	}
	
	public String exists(String url) {
		try {
			url = java.net.URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LogUtils.error("Problem decoding url ", e);
		}
		String prefix = System.getProperty(ConfigurationConstants.REPOSITORY_PREFIX);
		url = url.replaceAll(".*/"+prefix+"/(.*)", "$1");
		
		String repository = System.getProperty(ConfigurationConstants.REPOSITORY_TRANSFER);
		
		File file = new File(repository, url);
		File directory = file.getParentFile();
		
		if(!directory.exists())
			return null;
		
		String filename = file.getName();
		
		File[] files = directory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(filename) && (name.endsWith(".pdf") || name.endsWith(".PDF")) && !name.contains("result");
			}
		});
		
		if(files.length <= 0)
			return null;
		
		return files[0].getAbsolutePath();
	
	}
	
}
