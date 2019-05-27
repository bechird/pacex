package com.epac.owd.main;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.epac.owd.config.Configuration;
import com.epac.owd.config.ConfigurationConstants;
import com.epac.owd.service.CAPWebService;
import com.epac.owd.utils.LocalFileFetcher;
import com.epac.owd.utils.LogUtils;

public class Main {
	
	public static void main(String[] args) throws IOException {
		Configuration.load("owd.properties");
		loadData();

	    Path dir = Paths.get(System.getProperty(ConfigurationConstants.LOCAL_ORDER_NEW_DIR));
	    LocalFileFetcher fetcher = new LocalFileFetcher(dir);
	}
	
	private static void loadData(){
		LogUtils.debug("Retrieving lookups from DB");
		ConfigurationConstants.paperTypes = CAPWebService.getLookUps("PaperType");
		ConfigurationConstants.bindingTypes = CAPWebService.getLookUps("BindingType");
		ConfigurationConstants.laminations = CAPWebService.getLookUps("Lamination");
		
		//LogUtils.debug("We have : \n" + ConfigurationConstants.paperTypes.size() + " Papertypes \n" + ConfigurationConstants.bindingTypes.size() + " bindingTypes \n" + ConfigurationConstants.laminations.size() + " laminations \n");
		
	}
	
	static void usage() {
	    System.err.println("usage: java WatchDir dir");
	    System.exit(-1);
	}

	public static void print(Object e) {

		try {
			StringWriter writer = new StringWriter();
			JAXBContext jaxbContext = JAXBContext.newInstance(e.getClass());
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			
			List<String> strings = new ArrayList<String>();
			Map<String, String> map = new HashMap<>();

			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			jaxbMarshaller.marshal(e, writer);
			System.out.println(writer.getBuffer().toString());
		} catch (JAXBException e1) {
			e1.printStackTrace();
		}

	}
}
