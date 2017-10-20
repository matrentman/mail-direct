package com.mtlogic.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mtlogic.service.AddressService;

@Path("/api")	
public class AddressController {
	final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
	
	public AddressController() {
		// TODO Auto-generated constructor stub
	}

	@Path("/address")
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	public Response createAddressList(@Context HttpServletRequest requestContext, String inputMessage) throws JSONException 
	{	
		logger.info(">>>ENTERED createAddressList()");
		Response response = null;
		
		try {
			AddressService service = new AddressService();
			response = service.createAddressList(inputMessage);
		} catch (Exception e) {
			logger.error("Message could not be processed: " + e.getMessage());
			e.printStackTrace();
			response = Response.status(HttpStatus.SC_UNPROCESSABLE_ENTITY).entity("Message could not be processed: " + e.getMessage()).build();
		}
		
		logger.info("<<<EXITED createAddressList()");
		return response;
	}
	
	@POST
	@Path("/address")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadTargetFile(@Context HttpHeaders headers, @FormDataParam("uploadFile") InputStream fileInputStream,
			@FormDataParam("uploadFile") FormDataContentDisposition fileFormDataContentDisposition) {
		System.out.println(">>>ENTERED uploadTargetFile()");
		
		Response response = null;
		String fileName = null;
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			fileName = fileFormDataContentDisposition.getFileName();
			System.out.println("FileName=" + fileName);
			br = new BufferedReader(new InputStreamReader(fileInputStream));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			System.out.println(sb.toString());
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			try {fileInputStream.close();} catch (IOException e) {}
		}
		
		try {
			AddressService service = new AddressService();
			response = service.createAddressList(sb.toString());
		} catch (Exception e) {
			logger.error("Message could not be processed: " + e.getMessage());
			e.printStackTrace();
			response = Response.status(HttpStatus.SC_UNPROCESSABLE_ENTITY).entity("Message could not be processed: " + e.getMessage()).build();
		}
		
		System.out.println("<<<EXITED uploadTargetFile()");
		return response;
	}
}
