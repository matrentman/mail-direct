package com.mtlogic.service;

import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddressService {
	private static final String TARGET_URL = "https://rest.click2mail.com/molpro/addressLists";
	final Logger logger = LoggerFactory.getLogger(JobService.class);
	
	public AddressService() {
		// TODO Auto-generated constructor stub
	}

	public Response createAddressList(String payload) {
		logger.info(">>>ENTERED createAddressList()");
		Response response = null;
		Properties prop = new Properties();
		try {
			prop.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
			
			HttpAuthenticationFeature auth = HttpAuthenticationFeature.basic(prop.getProperty("username"), prop.getProperty("password"));
			
	        Client client = ClientBuilder.newBuilder()
	            .register(auth).build();
	        WebTarget webTarget = client.target(TARGET_URL);
	        
	        response = webTarget.request(MediaType.APPLICATION_XML_TYPE).post(Entity.xml(payload));
	
	        System.out.println(response.getStatus() + " "
	            + response.getStatusInfo() + " " + response);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception in POST createDocument - " + e.getMessage());
		}
		
		logger.info("<<<EXITED createAddressList(" + response.getStatus() + ")");
		return response;
	}
}
