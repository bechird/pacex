package com.epac.cap.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.mail.MessagingException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.springframework.stereotype.Controller;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.epac.cap.config.ConfigurationConstants;
import com.epac.cap.handler.CustomerHandler;
import com.epac.cap.handler.OrderHandler;
import com.epac.cap.handler.PartHandler;
import com.epac.cap.model.BindingType;
import com.epac.cap.model.Customer;
import com.epac.cap.model.Lamination;
import com.epac.cap.model.Order;
import com.epac.cap.model.OrderPart;
import com.epac.cap.model.PaperType;
import com.epac.cap.model.Part;
import com.epac.cap.model.PartCategory;
import com.epac.cap.model.Preference;
import com.epac.cap.model.SubPart;
import com.epac.cap.repository.LookupDAO;

@Controller
public class OrderXMLParser {

	private static boolean shutdown = false;

	public void parseXML(CustomerHandler customerHandler, PartHandler partHandler, LookupDAO lookupDAO,
			OrderHandler orderHandler) {

		boolean error = false;

		List<Order> 	orders 				= new ArrayList<Order>();
		Order 			order;
		OrderPart 		orderPart;
		Customer 		customer;
		SortedSet<SubPart> 	parts 				= new TreeSet<SubPart>();
		SubPart 		subPart 			= new SubPart();
		Part 			bookPart 			= null;
		PaperType 		paper;
		Lamination 		lamination;
		BindingType 	bindingType;
		String 			typeOfContent 		= "";

		String 			content 			= "";
		String 			technicalContent 	= "";
		int 			sftpError 			= 0;
		String 			filename 			= "";
		FileObject 		currentExcelFile 	= null;

		String 			orderRepository 	= "";
		String 			senderEmail 		= "";
		String 			receiverEmails 		= "";
		String			ccEmails			= "";

		StandardFileSystemManager manager 	= new StandardFileSystemManager();
		try {
			manager.init();
		} catch (FileSystemException e) {
			LogUtils.error("Error occured when initializing StandardFileSystemManager", e);
			// Try to close silently
			try {
				manager.close();
			} catch (Exception e2) {
			}
			LogUtils.debug("Exiting...");
			System.exit(-1);
		}

		while (!shutdown) {
			try {

				orderRepository 	= lookupDAO.read("ORDERREPOSITORY", Preference.class).getName();
				senderEmail 		= lookupDAO.read("SENDEREMAIL", Preference.class).getName();
				receiverEmails 		= lookupDAO.read("RECEIVEREMAILS", Preference.class).getName();
				ccEmails			= lookupDAO.read("CCEMAILS", Preference.class).getName();

				content 			= "";
				technicalContent 	= "";
				FileObject remoteFile;
				FileObject[] files = null;
				if (orderRepository.startsWith("sftp")) {
					
					try {
						remoteFile = manager.resolveFile(PDFFilesValidator.createConnectionString(orderRepository),
								PDFFilesValidator.createDefaultOptions(System.getProperty(ConfigurationConstants.SFTP_HOSTNAME)));
						files = remoteFile.getChildren();
					}catch (org.apache.commons.vfs2.FileSystemException e) {
						LogUtils.error(e.getMessage());
						e.printStackTrace();
						remoteFile = manager.resolveFile(PDFFilesValidator.createConnectionString(orderRepository));
						files = remoteFile.getChildren();
					}
							
				} else {
					remoteFile = manager.resolveFile(PDFFilesValidator.createConnectionString(orderRepository));
				}

				if (files.length > 0) {
					content = "<p>Hello,</p>" + "<p>The Order file has been processed successfully</p>";
					LogUtils.debug("File content is " + files.length + "  " + orderRepository);
				}
				
				// Iterate in the xml files 
				for (FileObject file : files){
					
					currentExcelFile = file;

					LogUtils.debug("element" + " is " + file + " " + file.getType().toString());
					filename = file.toString().substring(file.toString().lastIndexOf("/") + 1,
							file.toString().length());
					content += "<p>File : incoming/" + filename + "</p>";
					content += "<p>Date: " + TimeUtils.nowInUTC() + "</p><br></br>";
					content += "<p>Here's the rssults: </p>";

					if (filename.substring(filename.lastIndexOf(".") + 1, filename.length()).equalsIgnoreCase("xml")) {

						File tmpFile = File.createTempFile((new Date().getTime() + new String("_")) + filename, null);
						tmpFile.deleteOnExit();

						FileObject localFile = manager.resolveFile(tmpFile.getAbsolutePath());
						localFile.copyFrom(file, Selectors.SELECT_SELF);
						
						String localPath = (lookupDAO.read("FILEREPOSITORY", Preference.class) != null)
								? (lookupDAO.read("FILEREPOSITORY", Preference.class).getName())
								: System.getProperty(ConfigurationConstants.DIR_DOWNLOAD);

						XPathFactory xpf 					= XPathFactory.newInstance();
						XPath xPath 						= xpf.newXPath();

						// Customer Data
						XPathExpression requestorEmail 		= xPath.compile("requestorEmail");
						XPathExpression fisrtName 			= xPath.compile("fisrtName");
						XPathExpression lastName 			= xPath.compile("lastName");

						// Order Data
						XPathExpression PONumber 			= xPath.compile("PONumber");
						XPathExpression dueDate 			= xPath.compile("dueDate");
						XPathExpression note 				= xPath.compile("note");

						// OrderPart Data
						XPathExpression quantity 			= xPath.compile("quantity");

						// Unrelated Data
						XPathExpression contentType 		= xPath.compile("contentType");
						XPathExpression deleteAfterwards 	= xPath.compile("part/deleteAfterwards");
						XPathExpression category 			= xPath.compile("part/category");

						// Criteria
						XPathExpression selfCover 			= xPath.compile("part/selfCover");
						XPathExpression threeHoleDrill 		= xPath.compile("part/ThreeHoleDrill");
						XPathExpression perforation 		= xPath.compile("part/perforation");
						XPathExpression shrinkWrap 			= xPath.compile("part/shrinkWrap");

						// Part Data
						XPathExpression partNumber 			= xPath.compile("part/partNumber");
						XPathExpression ISBN 				= xPath.compile("part/ISBN");
						XPathExpression title 				= xPath.compile("part/title");
						XPathExpression textColors 			= xPath.compile("part/textColors");
						// XPathExpression coverColors =
						// xPath.compile("part/coverColors");

						XPathExpression partWidth 			= xPath.compile("part/partWidth");
						XPathExpression partHeight 			= xPath.compile("part/partHeight");
						// Paper Type Data
						XPathExpression paperType 			= xPath.compile("part/paperType");
						// Lamination Data
						XPathExpression partLamination 		= xPath.compile("part/lamination");
						// Binding Type Data
						XPathExpression partBindingType 	= xPath.compile("part/bindingType");
						XPathExpression pageCount 			= xPath.compile("part/pageCount");
						XPathExpression textFileURL 		= xPath.compile("part/textFileURL");
						XPathExpression coverFileURL 		= xPath.compile("part/coverFileURL");
						XPathExpression spineThickness 		= xPath.compile("part/spineThickness");
						XPathExpression partNote 			= xPath.compile("part/partNote");
						System.out.println(tmpFile.getName());
						InputSource inputSource = new InputSource(localFile.getName().getPath());
						NodeList contentNode = (NodeList) xPath.evaluate("/ordersSubmissionRequest", inputSource,
								XPathConstants.NODESET);
						LogUtils.debug("There's " + contentNode.getLength() + " ordersSubmissionRequest to be processed ");

						NodeList orderNodes = (NodeList) xPath.evaluate("/ordersSubmissionRequest/order", inputSource,
								XPathConstants.NODESET);
						LogUtils.debug("There's " + orderNodes.getLength() + " orders to be processed ");
						for (int x = 0; x < orderNodes.getLength(); x++) {

							order = new Order();
							//order.setSource("AUTO");
							orderPart = new OrderPart();
							bookPart = new Part();
							paper = new PaperType();
							// job = new Job();
							lamination = new Lamination();
							bindingType = new BindingType();
							Node orderElement = orderNodes.item(x);

							// if (typeOfContent.equalsIgnoreCase("1")) {
							typeOfContent = (String) contentType.evaluate(orderElement, XPathConstants.STRING);

							customer = customerHandler
									.readByEmail((String) requestorEmail.evaluate(orderElement, XPathConstants.STRING));
							if (customer == null) {
								// Customer
								customer = new Customer();
								customer.setEmail((String) requestorEmail.evaluate(orderElement, XPathConstants.STRING));
								customer.setFirstName((String) fisrtName.evaluate(orderElement, XPathConstants.STRING));
								customer.setLastName((String) lastName.evaluate(orderElement, XPathConstants.STRING));
								customer.setCreatedDate(new Date());
								customer.setCreatorId("System_Auto");
								customerHandler.create(customer);
							}
							order.setCustomer(customer);

							if (("1").equalsIgnoreCase(typeOfContent) || ("2").equalsIgnoreCase(typeOfContent)) {
								// Order
								order.setOrderNum((String) PONumber.evaluate(orderElement, XPathConstants.STRING));

								String date = (String) dueDate.evaluate(orderElement, XPathConstants.STRING);
								order.setDueDate(formatDate(date));
								order.setNotes((String) note.evaluate(orderElement, XPathConstants.STRING));
							}

							// Part
							if (("1").equalsIgnoreCase(typeOfContent) || ("3").equalsIgnoreCase(typeOfContent)) {
								// PartCategory partCategory = new PartCategory();
								// bookPart.setCategory(partCategory);// category);
								bookPart.setPartNum((String) partNumber.evaluate(orderElement, XPathConstants.STRING));
								bookPart.setTitle((String) title.evaluate(orderElement, XPathConstants.STRING));
								bookPart.setColors((String) textColors.evaluate(orderElement, XPathConstants.STRING));

								if (!partWidth.evaluate(orderElement, XPathConstants.NUMBER).toString().equalsIgnoreCase("NaN"))
									bookPart.setWidth(
											Float.parseFloat((String) partWidth.evaluate(orderElement, XPathConstants.STRING)));
								if (!partHeight.evaluate(orderElement, XPathConstants.NUMBER).toString()
										.equalsIgnoreCase("NaN"))
									bookPart.setLength(Float
											.parseFloat((String) partHeight.evaluate(orderElement, XPathConstants.STRING)));

								paper = (PaperType) lookupDAO.read(
										(String) paperType.evaluate(orderElement, XPathConstants.STRING), PaperType.class);
								if (paper == null) {
									bookPart.setPaperType(null);
									error = true;
								} else
									bookPart.setPaperType(paper);

								lamination = (Lamination) lookupDAO.read(
										(String) partLamination.evaluate(orderElement, XPathConstants.STRING),
										Lamination.class);
								if (lamination == null) {
									bookPart.setLamination(null);
									error = true;
								} else
									bookPart.setLamination(lamination);

								bindingType = (BindingType) lookupDAO.read(
										(String) partBindingType.evaluate(orderElement, XPathConstants.STRING),
										BindingType.class);
								if (bindingType == null) {
									bookPart.setBindingType(null);
									error = true;
								} else
									bookPart.setBindingType(bindingType);

								bookPart.setPagesCount(
										Integer.parseInt((String) pageCount.evaluate(orderElement, XPathConstants.STRING)));

								bookPart.setThickness((float) spineThickness.evaluate(orderElement, XPathConstants.NUMBER));
								bookPart.setNotes((String) partNote.evaluate(orderElement, XPathConstants.STRING));

								/*
								 * bookPart.setCategory(coverPC);
								 * bookPart.setColors((String)
								 * coverColors.evaluate(orderElement,
								 * XPathConstants.STRING));
								 * bookPart.setFilePath((String)
								 * coverFileURL.evaluate(orderElement,
								 * XPathConstants.STRING)); PartCritiriaId critiriaId =
								 * new PartCritiriaId(bookPart.getPartNum(), (String)
								 * selfCover.evaluate(orderElement,
								 * XPathConstants.STRING)); critiria.setId(critiriaId);
								 * critirias.add(critiria);
								 */

								Set<String> criterias = new HashSet<String>();
								if ("Yes".equalsIgnoreCase((String) selfCover.evaluate(orderElement, XPathConstants.STRING)))
									criterias.add("SELFCOVER");
								if ("Yes".equalsIgnoreCase(
										(String) threeHoleDrill.evaluate(orderElement, XPathConstants.STRING)))
									criterias.add("3HOLEDRILL");
								if ("Yes".equalsIgnoreCase((String) perforation.evaluate(orderElement, XPathConstants.STRING)))
									criterias.add("PERFORATION");
								if ("Yes".equalsIgnoreCase((String) shrinkWrap.evaluate(orderElement, XPathConstants.STRING)))
									criterias.add("SHRINKWRAP");
								bookPart.setCritirias(criterias);

								bookPart.setCategory(
										new PartCategory("4", (String) category.evaluate(orderElement, XPathConstants.STRING)));
								bookPart.setIsbn((String) ISBN.evaluate(orderElement, XPathConstants.STRING));
								bookPart.setSoftDelete("Yes".equalsIgnoreCase(
										(String) deleteAfterwards.evaluate(orderElement, XPathConstants.STRING)) ? true
												: false);

								subPart = new SubPart(bookPart.getPartNum(), bookPart.getPartNum());
								bookPart.getSubParts().add(subPart);
								bookPart.setSubParts(parts);
								try {
									PDFFilesValidator.download(
											(String) textFileURL.evaluate(orderElement, XPathConstants.STRING),
											localPath + "/" + bookPart.getIsbn() + "/" + bookPart.getIsbn() + ".text.pdf",
											bookPart);
								} catch (Exception e) {
									LogUtils.error("Error in downloading the Text pdf file !!");
								}
								try {
									PDFFilesValidator.download(
											(String) coverFileURL.evaluate(orderElement, XPathConstants.STRING),
											localPath + "/" + bookPart.getIsbn() + "/" + bookPart.getIsbn() + ".cover.pdf",
											bookPart);
								} catch (Exception e) {
									LogUtils.error("Error in downloading the Cover pdf file !!");
								}

								//bookPart.setFilePath(
									//	localPath + "/" + bookPart.getIsbn() + "/" + bookPart.getIsbn() + ".text.pdf");
								//bookPart.setFileName(bookPart.getIsbn() + ".text.pdf");
								bookPart.setCreatedDate(new Date());
								bookPart.setCreatorId("System_Auto");
								partHandler.create(bookPart);

							}

							if (("1").equalsIgnoreCase(typeOfContent)) {
								orderPart.setQuantity(
										Integer.parseInt((String) quantity.evaluate(orderElement, XPathConstants.STRING)));
								orderPart.setPart(bookPart);
								order.setOrderPart(orderPart);
								if (error == true)
									order.setStatus(com.epac.cap.model.Order.OrderStatus.ERROR.getName());
								else
									order.setStatus(com.epac.cap.model.Order.OrderStatus.PENDING.getName());
								order.setPriority(com.epac.cap.model.Order.OrderPriorities.NORMAL.getName());
								order.setCreatedDate(new Date());
								order.setCreatorId("System_Auto");
							}

							if (("1").equalsIgnoreCase(typeOfContent) || ("2").equalsIgnoreCase(typeOfContent))
								orders.add(order);

						}

						if (orders.size() > 0)
							for (Order tempOrder : orders)
								orderHandler.create(tempOrder);

						LogUtils.debug("XML successfully parsed, (" + orders.size() + ") orders found");
						}
					FileObject tempFile = manager.resolveFile(PDFFilesValidator.createConnectionString(
							System.getProperty(ConfigurationConstants.SFTP_TEMP_DIR) + (filename)));
					file.moveTo(tempFile);
					LogUtils.debug("file moved to " + tempFile.toString());
						
				}

				

			} catch (org.apache.commons.vfs2.FileSystemException fe) {
				technicalContent += "<p><b>" + fe.getMessage() + "</b></p>";
				String body = "";
				body = "<p>This is the report of the Orders Metadata submitted recently</p>";
				body += technicalContent;
				body += "<p>Cordially, </p>" + "<p>EPAC Technologies</p>";
				LogUtils.error("Error when trying to connect to (S)FTP ", fe);
				System.err.println(fe.getMessage());

				sftpError += 1;
				if (sftpError == 3) {
					sftpError = 0;
					String subject = "Order Metadata Report";

						String to 	= receiverEmails ;
						String from = senderEmail;
						String name = System.getProperty(ConfigurationConstants.EMAIL_NAME);
						String cc	= ccEmails;
						try {
							EmailUtil.sendEmail(to, cc, from, name, subject, body);
						} catch (UnsupportedEncodingException | MessagingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
							


				}
			} catch (Exception e) {
				LogUtils.error("Error parsing the Metadata files : ", e);
				technicalContent += "<p><b>" + e + "</b></p>";
				String body = "";
				body = "<p>This is the report of the Orders Metadata submitted recently</p>";
				body += technicalContent;
				body += "<p>Cordially, </p>" + "<p>EPAC Technologies</p>";

				if (currentExcelFile != null) {
					try {
						FileObject tempFile = manager.resolveFile(PDFFilesValidator.createConnectionString(
								System.getProperty(ConfigurationConstants.SFTP_TEMP_DIR) + (filename)));
						currentExcelFile.moveTo(tempFile);
						LogUtils.debug("file moved to " + tempFile.toString());
					} catch (FileSystemException | UnsupportedEncodingException e1) {
						e1.printStackTrace();
					}
				}

				String subject = "Order Metadata Report";
				try {
					String to 	= receiverEmails ;
					String from = senderEmail;
					String name = System.getProperty(ConfigurationConstants.EMAIL_NAME);
					String cc	= ccEmails;
					EmailUtil.sendEmail(to, cc, from, name, subject, body);
				} catch (Exception ee) {
					ee.printStackTrace();
				}

			}

			try {
				Thread.sleep(Integer.parseInt(System.getProperty(ConfigurationConstants.TIME_LAPSE)));
			} catch (InterruptedException e) {
				LogUtils.debug("WatchDog thread stopped");
				break;
			}
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

}