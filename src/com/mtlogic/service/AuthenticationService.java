package com.mtlogic.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mtlogic.business.SecureHash;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class AuthenticationService {
	final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

	public static final String DUPLICATE_USER = "Duplicate entry: User already exists!";
	public static final String DUPLICATE_EMAIL = "Duplicate entry: E-mail already exists!";
	public static final String ADDED_USER = "Successfully added new user credentials!";
	public static final String INVALID_CREDENTIALS = "--INVALID CREDENTIALS--";
	public static final String RESET_PASSWORD = "--RESET REQUIRED--";
	public static final String UPDATED_PASSWORD = "--UPDATED SUCCESSFULLY--";
	public static final String PASSWORD_LENGTH = "--PASSWORD MUST BE AT LEAST 8 CHARACTERS--";
	public static final String PASSWORD_SAME = "--NEW PASSWORD MUST NOT BE SAME AS OLD PASSWORD--";
	public static final String CLIENT_DOES_NOT_EXIST = "--CLIENT DOES NOT EXIST--";
	public static final String USERNAME_REQUIRED = "--USERNAME IS REQUIRED!--";
	public static final String PASSWORD_REQUIRED = "--PASSWORD IS REQUIRED!--";
	public static final String NEWPASSWORD_REQUIRED = "--NEWPASSWORD IS REQUIRED!--";
	public static final String EMAIL_REQUIRED = "--EMAIL IS REQUIRED!--";
	public static final String CLIENT_NUMBER_REQUIRED = "--CLIENT NUMBER IS REQUIRED!--";

	public AuthenticationService() {}

	public String requestToken(String jsonMessage) {
		logger.info(">>>ENTERED requestToken()");
		final JSONObject obj = new JSONObject(jsonMessage);
		String user = obj.getString("username");
		String password = obj.getString("password");
		String token = null;
		StringBuilder sb = new StringBuilder();
		String clientId = verifyCredentials(user, password);

		if (INVALID_CREDENTIALS.equals(clientId) || RESET_PASSWORD.equals(clientId)) {
			sb.append("{ \"error\":\"");
			sb.append(clientId);
			sb.append("\" }");
		} else {
			token = generateToken(user, clientId);
			if (token != null) {
this.verifyToken(token);
				sb.append("{ \"token\":\"");
				sb.append(token);
				sb.append("\" }");
			}
		} 

		logger.info("<<<EXITED requestToken(" + sb.toString() + ")");
		return sb.toString();
	}

	public String addCredentials(String jsonMessage) {
		logger.info(">>>ENTERED addCredentials(");
		StringBuilder sb = new StringBuilder();
		//String status = ADDED_USER;
		Vector<String> errorList = new Vector<String>();
		final JSONObject obj = new JSONObject(jsonMessage);
		String user = null;
		try {
			user = obj.getString("username");
		} catch (Exception e) {
			errorList.add(USERNAME_REQUIRED);
		}
		String password = null;
		try {
			password = obj.getString("password");
		} catch (Exception e) {
			errorList.add(PASSWORD_REQUIRED);
		}
		String email = obj.optString("email");
		String clientNumber = null;
		try {
			clientNumber = obj.getString("clientnumber");
		} catch (Exception e) {
			errorList.add(CLIENT_NUMBER_REQUIRED);
		}

		if (errorList.isEmpty()) {
			Integer clientId = lookupClientId(clientNumber); 
			if (clientId != null) {		
				Context envContext = null;
				Connection con = null;
				PreparedStatement preparedStatement = null;
				String insertUserSQL = "insert into public.user (user_name, email_address, password, client_id) values(?, ?, ?, ?)";

				try {
					envContext = new InitialContext();
					Context initContext  = (Context)envContext.lookup("java:/comp/env");
					DataSource ds = null;
					ds = (DataSource)initContext.lookup("jdbc/admin");

					con = ds.getConnection();					
					preparedStatement = con.prepareStatement(insertUserSQL);
					preparedStatement.setString(1, user);
					preparedStatement.setString(2, email);
					preparedStatement.setString(3, SecureHash.generateStrongPasswordHash(password));
					preparedStatement.setInt(4, clientId);

					preparedStatement.executeUpdate();		
				} catch (SQLException e) {
					e.printStackTrace();
					if (e.getMessage().contains("user_user_name_key")) {
						errorList.addElement(DUPLICATE_USER);
					}
					if (e.getMessage().contains("user_email_address_key")) {
						errorList.addElement(DUPLICATE_EMAIL);
					}
				} catch (NamingException e) {
					e.printStackTrace();
					logger.error("ERROR!!! : " + e.getMessage());
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("ERROR!!! : " + e.getMessage());
				} finally {
				    try{preparedStatement.close();}catch(Exception e){};
				    try{con.close();}catch(Exception e){};
				}
			} else {
				errorList.addElement(CLIENT_DOES_NOT_EXIST);
			}
		}

		sb.append("{ \"status\":\"");
		if (errorList.isEmpty()) {
			sb.append(ADDED_USER);
		} else { 
			sb.append(errorList.toString());
		}
		sb.append("\" }");

		logger.info("<<<EXITED addCredentials(" + sb.toString() + ")");
		return sb.toString();
	}

	public String verifyCredentials(String user, String password) {
		logger.info(">>>ENTERED verifyCredentials()");
		Context envContext = null;
		Connection con = null;
		PreparedStatement preparedStatement = null;
		String clientId = null;
		Boolean resetPassword = false;

		if ((user != null && !user.isEmpty()) && (password != null && !password.isEmpty())) {
			String selectQuery = "select reset_password from public.user where user_name = ?";

			try {
				envContext = new InitialContext();
				Context initContext  = (Context)envContext.lookup("java:/comp/env");
				DataSource ds = null;
			    ds = (DataSource)initContext.lookup("jdbc/admin");
				con = ds.getConnection();

				preparedStatement = con.prepareStatement(selectQuery);
				preparedStatement.setString(1, user);

				ResultSet rs = preparedStatement.executeQuery();

				if (rs.next()) {
					resetPassword = rs.getBoolean("reset_password");
				}
				if (!resetPassword) {
					clientId = verifyPassword(user, password);
				} else {
					clientId = RESET_PASSWORD;
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error("ERROR!!! : " + e.getMessage());
			} catch (NamingException e) {
				e.printStackTrace();
				logger.error("ERROR!!! : " + e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("ERROR!!! : " + e.getMessage());
			} finally {
			    try{preparedStatement.close();}catch(Exception e){};
			    try{con.close();}catch(Exception e){};
			}
		}

		logger.info("<<<EXITED verifyCredentials(" + clientId + ")");
        return (clientId);
    }

	private String verifyPassword(String user, String password) {
		logger.info(">>>ENTERED verifyPassword()");
		boolean isValid = false;
		Context envContext = null;
		Connection con = null;
		PreparedStatement preparedStatement = null;
		String storedPassword = null;
		String clientNumber = null;

		if ((user != null && !user.isEmpty()) && (password != null && !password.isEmpty())) {
			String selectSQL = "select password, client_id from public.user where user_name = ?";

			try {
				envContext = new InitialContext();
				Context initContext  = (Context)envContext.lookup("java:/comp/env");
				DataSource ds = null;
			    ds = (DataSource)initContext.lookup("jdbc/admin");
				con = ds.getConnection();

				preparedStatement = con.prepareStatement(selectSQL);
				preparedStatement.setString(1, user);

				ResultSet rs = preparedStatement.executeQuery();

				if (rs.next()) {
					storedPassword = rs.getString("password");
					clientNumber = rs.getString("client_id");
				}
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error("ERROR!!! : " + e.getMessage());
			} catch (NamingException e) {
				e.printStackTrace();
				logger.error("ERROR!!! : " + e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("ERROR!!! : " + e.getMessage());
			} finally {
			    try{preparedStatement.close();}catch(Exception e){};
			    try{con.close();}catch(Exception e){};
			}

			try {
				Boolean valid = SecureHash.validatePassword(password, storedPassword);
		        if ((valid)) {
		        	isValid = true;
		        }
			} catch (Exception e) {
				isValid = false;
			}
		}

		logger.info("<<<EXITED verifyPassword(" + isValid + ")");
        return (isValid?clientNumber:INVALID_CREDENTIALS);
    }

	public String resetPassword(String jsonMessage) {
		logger.info(">>>ENTERED resetPassword()");
		List<String> statusList = new ArrayList<String>();
		final JSONObject obj = new JSONObject(jsonMessage);
		String user = null;
		try {
			user = obj.getString("username");
		} catch (Exception e) {
			statusList.add(USERNAME_REQUIRED);
		}
		String password = null; 
		try {
			password = obj.getString("password");
		} catch (Exception e) {
			statusList.add(PASSWORD_REQUIRED);
		}
		String newPassword = null;
		try {
			newPassword= obj.getString("newpassword");
		} catch (Exception e) {
			statusList.add(NEWPASSWORD_REQUIRED);
		}
		String email = null;
		try {
			email = obj.getString("email");
		} catch (Exception e) {
			statusList.add(EMAIL_REQUIRED);
		}

		//StringBuilder sb = new StringBuilder();
		if (statusList.isEmpty()) {
			Boolean initialized = verifyUserIsInitialized(user);

			String status = "SUCCESS";
			if (initialized) {
				status = verifyPassword(user, password);
			}

			if (!INVALID_CREDENTIALS.equals(status)) {
				//Removed the check below for now so that users can use their previous Alveo password
				//status = checkPasswordStrength(password, newPassword);
				status = null;

				if (status == null) {
					Context envContext = null;
					Connection con = null;
					PreparedStatement preparedStatement = null;
					String updateSQL = "update public.user set password = ?, email_address = ?, reset_password = false where user_name = ?";

					try {
						envContext = new InitialContext();
						Context initContext  = (Context)envContext.lookup("java:/comp/env");
						DataSource ds = null;
						ds = (DataSource)initContext.lookup("jdbc/admin");

						con = ds.getConnection();
						preparedStatement = con.prepareStatement(updateSQL);

						preparedStatement.setString(1, SecureHash.generateStrongPasswordHash(newPassword));
						preparedStatement.setString(2, email);
						preparedStatement.setString(3, user);

						preparedStatement.executeUpdate();
						status = UPDATED_PASSWORD;
						statusList.add(UPDATED_PASSWORD);
					} catch (SQLException e) {
						e.printStackTrace();
						logger.error("ERROR!!! - " + status);
					} catch (NamingException e) {
						e.printStackTrace();
						logger.error("ERROR!!! : " + e.getMessage());
					} catch (Exception e) {
						e.printStackTrace();
						logger.error("ERROR!!! : " + e.getMessage());
					} finally {
					    try{preparedStatement.close();}catch(Exception e){};
					    try{con.close();}catch(Exception e){};
					}
				} else {
					statusList.add(status);
				}
			} else {
				statusList.add(INVALID_CREDENTIALS);
			}
		}

//		sb.append("{ \"status\":\"");
//		sb.append(status);
//		sb.append("\" }");
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", statusList);

		logger.info("<<<EXITED resetPassword(" + jsonObject.toString() + ")");
		//return sb.toString();
		return jsonObject.toString();
	}

	private Boolean verifyUserIsInitialized(String user) {
		logger.info(">>>ENTERED verifyUserIsInitialized(" + user + ")");
		Context envContext = null;
		Connection con = null;
		PreparedStatement preparedStatement = null;
		Boolean initialized = Boolean.FALSE;

		String selectSQL = "select initialized from public.user where user_name = ?";

		try {
			envContext = new InitialContext();
			Context initContext  = (Context)envContext.lookup("java:/comp/env");
			DataSource ds = null;
		    ds = (DataSource)initContext.lookup("jdbc/admin");
			con = ds.getConnection();

			preparedStatement = con.prepareStatement(selectSQL);
			preparedStatement.setString(1, user);

			ResultSet rs = preparedStatement.executeQuery();

			if (rs.next()) {
				initialized = rs.getBoolean("initialized");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("ERROR!!! : " + e.getMessage());
		} catch (NamingException e) {
			e.printStackTrace();
			logger.error("ERROR!!! : " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("ERROR!!! : " + e.getMessage());
		} finally {
		    try{preparedStatement.close();}catch(Exception e){};
		    try{con.close();}catch(Exception e){};
		}

		logger.info("<<<EXITED verifyUserIsInitialized(" + initialized + ")");
		return initialized;
	}

	public String overwritePassword(String jsonMessage) {
		logger.info(">>>ENTERED overwritePassword()");
		final JSONObject obj = new JSONObject(jsonMessage);
		String user = obj.getString("username");
		String newPassword = obj.getString("newpassword");
		StringBuilder sb = new StringBuilder();

		String status = null;
		Context envContext = null;
		Connection con = null;
		PreparedStatement preparedStatement = null;
		String updateSQL = "update public.user set password = ?, reset_password = false where user_name = ?";

		try {
			envContext = new InitialContext();
			Context initContext  = (Context)envContext.lookup("java:/comp/env");
			DataSource ds = null;
			ds = (DataSource)initContext.lookup("jdbc/admin");

			con = ds.getConnection();
			preparedStatement = con.prepareStatement(updateSQL);

			preparedStatement.setString(1, SecureHash.generateStrongPasswordHash(newPassword));
			preparedStatement.setString(2, user);

			preparedStatement.executeUpdate();
			status = UPDATED_PASSWORD;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("ERROR!!! - " + status);
		} catch (NamingException e) {
			e.printStackTrace();
			logger.error("ERROR!!! : " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("ERROR!!! : " + e.getMessage());
		} finally {
		    try{preparedStatement.close();}catch(Exception e){};
		    try{con.close();}catch(Exception e){};
		} 

		sb.append("{ \"status\":\"");
		sb.append(status);
		sb.append("\" }");

		logger.info("<<<EXITED overwritePassword(" + status + ")");
		return sb.toString();
	}

	private String checkPasswordStrength(String oldPassword, String newPassword) {
		logger.info(">>>ENTERED checkPasswordStrength()");
		String status = null;
		// Need to determine password rules and add below
		//	for now just a few simple checks...
		if (oldPassword.equals(newPassword)) {
			status = PASSWORD_SAME;
		}
		if (newPassword.length() < 8) {
			status = PASSWORD_LENGTH;
		}
		logger.info("<<<EXITED checkPasswordStrength(" + status + ")");
		return status;
	}

	private Integer lookupClientId(String clientNumber) {
		logger.info(">>>ENTERED lookupClientId(" + clientNumber + ")");
		Context envContext = null;
		Connection con = null;
		PreparedStatement preparedStatement = null;
		Integer clientId = null;

		String selectSQL = "select client_id from public.client where client_number = ?";

		try {
			envContext = new InitialContext();
			Context initContext  = (Context)envContext.lookup("java:/comp/env");
			DataSource ds = null;
		    ds = (DataSource)initContext.lookup("jdbc/admin");
			con = ds.getConnection();

			preparedStatement = con.prepareStatement(selectSQL);
			preparedStatement.setString(1, clientNumber);

			ResultSet rs = preparedStatement.executeQuery();

			if (rs.next()) {
				clientId = rs.getInt("client_id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("ERROR!!! : " + e.getMessage());
		} catch (NamingException e) {
			e.printStackTrace();
			logger.error("ERROR!!! : " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("ERROR!!! : " + e.getMessage());
		} finally {
		    try{preparedStatement.close();}catch(Exception e){};
		    try{con.close();}catch(Exception e){};
		}

		logger.info("<<<EXITED lookupClientId(" + clientId + ")");
        return (clientId);
    }

	private String generateToken(String userName, String clientId) {
		logger.info(">>>ENTERED generateToken(" + userName + "' " + clientId + ")");
        String id = UUID.randomUUID().toString().replace("-", "");
        Date now = new Date();
        //Date exp = new Date(System.currentTimeMillis() + (1000 * 60)); // 60 seconds
        //Date exp = new Date(System.currentTimeMillis() + (1000 * 60)*60); // 60 minutes
        //Date exp = new Date(System.currentTimeMillis() + (1000 * 60)*60*24); // 24 hours
        //Date exp = new Date(System.currentTimeMillis() + (1000 * 60)*60*24*30); // 30 days
        Date exp = new Date(System.currentTimeMillis() + (1000 * 60)*60*24*265); // 265 days

        // Temporary for test will store secret in database
        //    maybe have one secret for each client?+
        //Key secret = MacProvider.generateKey(SignatureAlgorithm.HS256);
        //byte[] secretBytes = secret.getEncoded();
        //String base64SecretBytes = Base64.getEncoder().encodeToString(secretBytes);
        String base64SecretBytes = "JPLet+DhcjalDLlmhpH4Xi0ivIlzj/ZTZVJvGkMB7e8=";

        String token = Jwts.builder()
            .setId(id)
            .setIssuedAt(now)
            .setNotBefore(now)
            .setExpiration(exp)
            .setSubject(userName + ":" + clientId)
            .signWith(SignatureAlgorithm.HS256, base64SecretBytes)
            .compact();

        logger.info("<<<EXITED generateToken(" + token + ")");
        return token;
    }

	public String verifyToken(String token) {
		logger.info(">>>ENTERED verifyToken(" + token + ")");
		String base64SecretBytes = "JPLet+DhcjalDLlmhpH4Xi0ivIlzj/ZTZVJvGkMB7e8=";
		boolean isValid = false;
		Claims claims = null;
		try {
	        claims = Jwts.parser()
	            .setSigningKey(base64SecretBytes)
	            .parseClaimsJws(token).getBody();
	        if (claims != null) {
	        	Date now = new Date();
	        	Date exp = claims.getExpiration();
	        	if (exp.after(now)) {
	        		isValid = true;
	        	}
	        }
		} catch (Exception e) {
			logger.error("ERROR parsing JWToken!!! : " + e.getMessage());
			e.printStackTrace();
			isValid = false;
		}

		String[] bits = null;
		if (isValid) {
			bits = claims.getSubject().split(":");
			if (bits.length == 2) {
				if (bits[0] == null || bits[0].isEmpty()) {
					isValid = false;
				}
				if (bits[1] == null || bits[1].isEmpty()) {
					isValid = false;
				}
			} else {
				isValid = false;
			}
		}

		JSONObject obj = new JSONObject();
		obj.put("verified", isValid);
		if (isValid) {
			obj.put("username", bits[0]);
			obj.put("clientid", bits[1]);
		}

		logger.info("<<<EXITED verifyToken(" + obj.toString() + ")");
        return obj.toString();
    }

}