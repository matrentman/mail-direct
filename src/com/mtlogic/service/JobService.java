package com.mtlogic.service;

import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobService {
	private static final String TARGET_URL = "https://rest.click2mail.com/molpro/jobs";
	final Logger logger = LoggerFactory.getLogger(JobService.class);
	
	public JobService() {
		// TODO Auto-generated constructor stub
	}

	public Response createJob(String jsonMessage) {
		logger.info(">>>ENTERED createJob()");
		
		Response response = null;
		Properties prop = new Properties();
		try {
			prop.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
			
			final JSONObject jsonObject = new JSONObject(jsonMessage);
			HttpAuthenticationFeature auth = HttpAuthenticationFeature.basic(prop.getProperty("username"), prop.getProperty("password"));
			
	        Client client = ClientBuilder.newBuilder()
	            .register(MultiPartFeature.class).register(auth).build();
	        WebTarget webTarget = client.target(TARGET_URL);
	        
	        MultivaluedMap<String, String> formData = new MultivaluedHashMap<String, String>();
	        formData.add("documentClass", jsonObject.getString("documentClass"));
	        formData.add("layout", jsonObject.getString("layout"));
	        formData.add("productionTime", jsonObject.getString("productionTime"));
	        formData.add("envelope", jsonObject.getString("envelope"));
	        formData.add("color", jsonObject.getString("color"));
	        formData.add("paperType", jsonObject.getString("paperType"));
	        formData.add("printOption", jsonObject.getString("printOption"));
	        formData.add("documentId", jsonObject.getString("documentId"));
	        formData.add("addressId", jsonObject.getString("addressId"));
	        
	        response = webTarget.request()
	            .post(Entity.form(formData));
	
	        System.out.println(response.getStatus() + " "
	            + response.getStatusInfo() + " " + response);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception in POST createJob - " + e.getMessage());
		}
		
		logger.info("<<<EXITED createJob(" + response.getStatus() + ")");
		return response;
	}
	
	// initial try using MultiPartFormData which did not work - may prove useful later
	public Response createJob2(String jsonMessage) {
		logger.info(">>>ENTERED createJob()");
		final JSONObject jsonObject = new JSONObject(jsonMessage);
		//String user = jsonObject.getString("username");
		//String password = jsonObject.getString("password");
		StringBuilder sb = new StringBuilder();
		Response response = null;
		
		try {
		HttpAuthenticationFeature auth = HttpAuthenticationFeature.basic("mtrentman", "GS1150es");
		
        Client client = ClientBuilder.newBuilder()
            .register(MultiPartFeature.class).register(auth).build();
        WebTarget webTarget = client.target(TARGET_URL);
        MultiPart multiPart = new MultiPart();
        //multiPart.setMediaType(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
        multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

        FormDataMultiPart documentClass = new FormDataMultiPart();
        documentClass.setMediaType(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
        documentClass.field("documentClass", jsonObject.getString("documentClass"));
        multiPart.bodyPart(documentClass);
        FormDataMultiPart layout = new FormDataMultiPart();
        layout.field("layout", jsonObject.getString("layout"));
        multiPart.bodyPart(layout);
        FormDataMultiPart productionTime = new FormDataMultiPart();
        productionTime.field("productionTime", jsonObject.getString("productionTime"));
        multiPart.bodyPart(productionTime);
        FormDataMultiPart envelope = new FormDataMultiPart();
        envelope.field("envelope", jsonObject.getString("envelope"));
        multiPart.bodyPart(envelope);
        FormDataMultiPart color = new FormDataMultiPart();
        color.field("color", jsonObject.getString("color"));
        multiPart.bodyPart(color);
        FormDataMultiPart paperType = new FormDataMultiPart();
        paperType.field("paperType", jsonObject.getString("paperType"));
        multiPart.bodyPart(paperType);
        FormDataMultiPart printOption = new FormDataMultiPart();
        printOption.field("printOption", jsonObject.getString("printOption"));
        multiPart.bodyPart(printOption);
        FormDataMultiPart documentId = new FormDataMultiPart();
        documentId.field("documentId", jsonObject.getString("documentId"));
        multiPart.bodyPart(documentId);
        FormDataMultiPart addressId = new FormDataMultiPart();
        addressId.field("addressId", jsonObject.getString("addressID"));
        multiPart.bodyPart(addressId);
        
        response = webTarget.request(MediaType.APPLICATION_FORM_URLENCODED)
            .post(Entity.entity(multiPart, multiPart.getMediaType()));

        System.out.println(response.getStatus() + " "
            + response.getStatusInfo() + " " + response);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception in POST createJob - " + e.getMessage());
		}
		
		logger.info("<<<EXITED createJob(" + sb.toString() + ")");
		//return sb.toString();
		return response;
	}
	
}
