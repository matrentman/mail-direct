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
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mtlogic.service.AuthenticationService;

@Path("/api")
public class AuthenticationController {
	final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
	
	public static final int CLIENT_276 = 1;
	public static final int ALVEO_276 = 2;
	public static final int PAYOR_277 = 3;
	public static final int ALVEO_277 = 4;
	
	@Path("/token")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response requestToken(@Context HttpServletRequest requestContext, String inputMessage) throws JSONException 
	{	
		logger.info(">>>ENTERED requestToken()");
		Response response = null;
		String token = null;

		int responseCode = HttpStatus.SC_ACCEPTED;
		
		try {
			AuthenticationService tokenManager = new AuthenticationService();
			token = tokenManager.requestToken(inputMessage);
		} catch (Exception e) {
			logger.error("Message could not be processed: " + e.getMessage());
			e.printStackTrace();
			response = Response.status(HttpStatus.SC_UNPROCESSABLE_ENTITY).entity("Message could not be processed: " + e.getMessage()).build();
		}
		
		if (token == null) {
			responseCode = HttpStatus.SC_UNAUTHORIZED;
		}
		response = Response.status(responseCode).entity(token).build();
		
		logger.info("<<<EXITED requestToken()");
		return response;
	}
	
	@Path("/token/validate")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response validateToken(String inputMessage) throws JSONException 
	{	
		logger.info(">>>ENTERED validateToken()");
		Response response = null;
		String token = null;
		String responseString = null;

		int responseCode = HttpStatus.SC_OK;
		
		try {
			JSONObject obj = new JSONObject(inputMessage);
			token = obj.getString("token");
			AuthenticationService tokenManager = new AuthenticationService();
			responseString = tokenManager.verifyToken(token);
		} catch (Exception e) {
			logger.error("Message could not be processed: " + e.getMessage());
			e.printStackTrace();
			response = Response.status(HttpStatus.SC_UNPROCESSABLE_ENTITY).entity("Message could not be processed: " + e.getMessage()).build();
		}
		
		if (token == null || responseString.contains("false")) {
			responseCode = HttpStatus.SC_UNAUTHORIZED;
		}
		response = Response.status(responseCode).entity(responseString).build();
		
		logger.info("<<<EXITED validateToken()");
		return response;
	}
	
	@Path("/credentials")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response addCredentials(String inputMessage) throws JSONException 
	{	
		logger.info(">>>ENTERED addCredentials()");
		Response response = null;
		String statusMessage = null;

		int responseCode = HttpStatus.SC_ACCEPTED;
		
		try {
			AuthenticationService tokenManager = new AuthenticationService();
			statusMessage = tokenManager.addCredentials(inputMessage);
		} catch (Exception e) {
			logger.error("Message could not be processed: " + e.getMessage());
			e.printStackTrace();
			response = Response.status(HttpStatus.SC_UNPROCESSABLE_ENTITY).entity("Message could not be processed: " + e.getMessage()).build();
		}
		
		if (response == null) {
			response = Response.status(responseCode).entity(statusMessage).build();
		}
		logger.info("<<<EXITED addCredentials()");
		return response;
	}
	
	@Path("/password")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response resetPassword(String inputMessage) throws JSONException 
	{	
		logger.info(">>>ENTERED resetPassword()");
		Response response = null;
		String statusMessage = null;

		int responseCode = HttpStatus.SC_ACCEPTED;
		
		try {
			AuthenticationService tokenManager = new AuthenticationService();
			statusMessage = tokenManager.resetPassword(inputMessage);
		} catch (Exception e) {
			logger.error("Message could not be processed: " + e.getMessage());
			e.printStackTrace();
			response = Response.status(HttpStatus.SC_UNPROCESSABLE_ENTITY).entity("Message could not be processed: " + e.getMessage()).build();
		}
		
		if (response == null) {
			response = Response.status(responseCode).entity(statusMessage).build();
		}
		logger.info("<<<EXITED resetPassword()");
		return response;
	}
	
	@Path("/password/overwrite")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response overwritePassword(String inputMessage) throws JSONException 
	{	
		logger.info(">>>ENTERED overwritePassword()");
		Response response = null;
		String statusMessage = null;

		int responseCode = HttpStatus.SC_ACCEPTED;
		
		try {
			AuthenticationService tokenManager = new AuthenticationService();
			statusMessage = tokenManager.overwritePassword(inputMessage);
		} catch (Exception e) {
			logger.error("Message could not be processed: " + e.getMessage());
			e.printStackTrace();
			response = Response.status(HttpStatus.SC_UNPROCESSABLE_ENTITY).entity("Message could not be processed: " + e.getMessage()).build();
		}
		
		if (response == null) {
			response = Response.status(responseCode).entity(statusMessage).build();
		}
		logger.info("<<<EXITED overwritePassword()");
		return response;
	}
	
}

