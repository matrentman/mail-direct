package com.mtlogic.controller;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mtlogic.service.DocumentService;

@Path("/api")	
public class DocumentController {
	final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
	
	public DocumentController() {
		// TODO Auto-generated constructor stub
	}

	@Path("/document")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response createDocument(@Context HttpServletRequest requestContext, String inputMessage) throws JSONException 
	{	
		logger.info(">>>ENTERED createJob()");
		Response response = null;
		
		try {
			DocumentService service = new DocumentService();
			response = service.createDocument(inputMessage);
		} catch (Exception e) {
			logger.error("Message could not be processed: " + e.getMessage());
			e.printStackTrace();
			response = Response.status(HttpStatus.SC_UNPROCESSABLE_ENTITY).entity("Message could not be processed: " + e.getMessage()).build();
		}
		
		logger.info("<<<EXITED createJob()");
		return response;
	}
	
}
