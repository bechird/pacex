package com.epac.cap.utils;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.epac.cap.config.ConfigurationConstants;

public class EmailUtil{

	public static boolean sendEmail(String toEmail, String ccEmail, String fromEmail, String fromName, String subject ,String content) throws MessagingException, MessagingException, UnsupportedEncodingException {
		LogUtils.start();
		//String username = "preflight@esprint.com.mt";//System.getProperty(ConfigurationConstants.EMAIL_USERNAME);
		String username = "cHJlZmxpZ2h0QGVzcHJpbnQuY29tLm10";//"preflight@esprint.com.mt";
		String password = "UHLCo2ZsIVQ=";//"Pr£fl!T";//System.getProperty(ConfigurationConstants.EMAIL_PASSWORD);
		String server   = "smtp.esprint.com.mt";//System.getProperty(ConfigurationConstants.EMAIL_SERVER);
		
		
		int port   = -1;
		
		try {
			port = 465;//Integer.parseInt(System.getProperty(ConfigurationConstants.EMAIL_PORT));
		} catch (Exception e) {
			LogUtils.error("Error occured when retreiving email server port", e);
			return false;
		}
		
		String security   = "SSL";//System.getProperty(ConfigurationConstants.EMAIL_SECURITY);
		
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		
		if("TLS".equals(security))
			props.put("mail.smtp.starttls.enable", "true");
		else if("SSL".equals(security))
			props.put("mail.smtp.ssl.enable", "true");
		else if(security != null && "NONE".equalsIgnoreCase(security)){
			LogUtils.error("Property "+security+" should be either SSL or TLS");
			return false;
		}
		props.put("mail.smtp.host", server);
		props.put("mail.smtp.port", port);
		
		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		  });
		session.setDebug(true);
		LogUtils.debug("Sending mail ["+subject+"] to ["+toEmail+"]");
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(username));
		message.setRecipients(Message.RecipientType.TO,
			InternetAddress.parse(toEmail));
		
		if(ccEmail != null)
			message.addRecipients(Message.RecipientType.CC, InternetAddress.parse(ccEmail));
		message.setFrom(new InternetAddress(fromEmail, fromName));
		message.setSubject(subject);
		
		Multipart multipart = new MimeMultipart();
		MimeBodyPart htmlPart = new MimeBodyPart();
		htmlPart.setContent(content, "text/html; charset=utf-8");
		
		multipart.addBodyPart(htmlPart);
		
		message.setContent(multipart);
		LogUtils.debug("Sending html mail to ["+toEmail+"]");
		Transport.send(message);//, username, password);//send(message);
		LogUtils.end();
		return true;
			
	}
	
	public static void main(String[] args) {
		try {
			sendEmail("prepress@esprint.com.mt", null, "preflight@esprint.com.mt", "Esprint PrePress/Preflight", "Test Mail", "test");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
