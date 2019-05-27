package com.epac.owd.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.epac.cap.common.EpacException;
import com.epac.cap.model.Order;
import com.epac.cap.model.Part;
import com.epac.owd.config.Configuration;
import com.epac.owd.config.ConfigurationConstants;
import com.epac.owd.utils.LogUtils;

public class CAPWebService {

	private final static String CAP_URL;

	static {
		Configuration.load("owd.properties");
		CAP_URL = System.getProperty(ConfigurationConstants.CAP_ADDRESS);

	}

	// HTTP GET request to get the LookUps
	public static List getLookUps(String type) {
		PacexClient client = new PacexClient();
		String url = CAP_URL + "/lookups/" + type;

		try {
			client.authenticate();
			return client.get(url);
		} catch (PacexClientException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}

	public static String addPart(Part part) throws EpacException{

		String url = CAP_URL + "/parts";
		PacexClient client = new PacexClient();
		String partNb = null;

		try {

			client.authenticate();
			ResponseEntity<String> response = client.post(part, url);
			if (response.getStatusCodeValue() != 200) {
				throw new EpacException("This part cannot be saved ");
			}
			partNb = response.getBody();
			//partNb = part.getPartNum();
		} catch (Exception e) {

			e.printStackTrace();

		}
		return partNb;

	}

	// HTTP POST request to add an Order
	public static void addOrder(Order order) throws EpacException {

		String url = CAP_URL + "/orders";
		PacexClient client = new PacexClient();

		try {
			LogUtils.debug("adding the order to --> "+url);
			client.authenticate();
			ResponseEntity<String> response = client.post(order, url);
			LogUtils.debug("Order added, PO# is " + order.getOrderNum());
			if (response.getStatusCodeValue() != 200) {
				throw new EpacException("Duplicate Order Number ");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private static String stringifyDate(Date date) {
		
		String stringDate = null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int time = calendar.get(Calendar.MONTH)+1;
		String month = (time < 10) ? "0"+time : time+"";
		time = calendar.get(Calendar.DAY_OF_MONTH);
		String day = (time < 10) ? "0"+time : time+"";
		stringDate = calendar.get(Calendar.YEAR)+"-"+month+"-"+day+"T00:00:00.000Z";
		
		return stringDate;
	}
}
