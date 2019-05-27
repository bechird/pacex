package com.epac.imposition.rip;

import java.io.File;
import java.io.StringWriter;

import com.epac.imposition.bookblock.ImpositionJob;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

public class TemplateUtil {
	private static final String TMPL_EXTENSION = ".tmpl";
	private static Configuration configuration;
	
	@SuppressWarnings("deprecation")
	public static void Initialize (ImpositionJob job){
		try {
			configuration = new Configuration();
			//configuration.setDirectoryForTemplateLoading(new File("jdf"));
			configuration.setClassForTemplateLoading(TemplateUtil.class, "/com/epac/imposition/rip");
			configuration.setDefaultEncoding("UTF-8");
			configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			System.out.println("Freemarker template engine initialized");
		} catch (Exception e) {
			System.out.println("Error occured when initializing Freemarker"+e);
		}
		
	}
	public static String generate(ImpositionJob job, Object model, String name) {
		Initialize (job);
		
		System.out.println("Generating content from template "+name);
		try {
			Template template = configuration.getTemplate(name + TMPL_EXTENSION);
			StringWriter writer = new StringWriter();
			template.process(model, writer);
			return writer.getBuffer().toString();
		} catch (Exception e) {
			System.out.println("Error occured when generating string from template "+name+e);
			throw new RuntimeException(e.getMessage());
		}
	}
}
