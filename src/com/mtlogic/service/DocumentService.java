package com.mtlogic.service;

import java.io.File;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.MultiPartMediaTypes;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentService {
	private static final String TARGET_URL = "https://rest.click2mail.com/molpro/documents";
	final Logger logger = LoggerFactory.getLogger(JobService.class);
	
	public DocumentService() {
		// TODO Auto-generated constructor stub
	}

	public Response createDocument(String jsonMessage) {
		logger.info(">>>ENTERED createDocument()");
		Response response = null;
		Properties prop = new Properties();
		try {
			prop.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
			HttpAuthenticationFeature auth = HttpAuthenticationFeature.basic(prop.getProperty("username"), prop.getProperty("password"));
			
			Client client = ClientBuilder.newBuilder()
		            .register(MultiPartFeature.class).register(auth).build();
	
	        final FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("file",
	        	new File("C:/temp/sample-document.docx"),
	            MediaType.APPLICATION_OCTET_STREAM_TYPE);
	        
	        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
	        final FormDataMultiPart multipart = (FormDataMultiPart) formDataMultiPart.field("documentName", "Sample Letter").field("documentClass", "Letter 8.5 x 11").field("documentFormat", "DOCX").bodyPart(fileDataBodyPart);
	        
	        final WebTarget target = client.target(TARGET_URL);
	        response = target.request().post(Entity.entity(multipart, multipart.getMediaType()));
	
	        formDataMultiPart.close();
	        multipart.close();
	        
	        System.out.println(response.getStatus() + " "
	            + response.getStatusInfo() + " " + response);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception in POST createDocument - " + e.getMessage());
		}
		
		logger.info("<<<EXITED createDocument(" + response.getStatus() + ")");
		return response;
	}
}
