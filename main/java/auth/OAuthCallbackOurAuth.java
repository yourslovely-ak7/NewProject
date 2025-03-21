package auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import crud.UserOperation;
import exception.InvalidException;
import helper.Helper;
import pojo.Client;
import pojo.User;
import pojo.UserSession;

@SuppressWarnings("serial")
public class OAuthCallbackOurAuth extends HttpServlet
{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String errorCode= req.getParameter("error");
		if(errorCode != null)
		{
			System.out.println("Error code received: "+errorCode);
			resp.sendRedirect("login.html");
			return;
		}
		
		System.out.println("Response received to the callback servlet.");
		try
		{
		String authCode= req.getParameter("code");
//		String location= req.getParameter("location");
		String server= req.getParameter("accounts-server");
		String clientSecret="";
		
		Client client= Helper.getClient("OurAuth");
		clientSecret= client.getClientSecret();
		Helper.checkForNull(clientSecret);
		
		StringBuilder tokenApi= new StringBuilder(server);
		tokenApi.append("/token?")
		.append("responseType=token")
		.append("&clientId=").append(client.getClientId())
		.append("&clientSecret=").append(clientSecret)
		.append("&redirectUrl=").append(Helper.getRedirectURIOurAuth())
		.append("&code="+authCode);
		
		System.out.println("Token req: "+tokenApi.toString());
		
		URL url= new URL(tokenApi.toString());
		HttpURLConnection connection= (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setRequestProperty("Accept", "application/json");
		
		StringBuilder response= new StringBuilder();
		try(BufferedReader reader= new BufferedReader(new InputStreamReader(connection.getInputStream())))
		{
			String line;
			while((line = reader.readLine()) != null)
			{
				response.append(line);
			}
		}
		System.out.println("Response: "+response);
		
			JSONObject json= new JSONObject(response.toString());
			
			try
			{
				String error= json.getString("error");
				System.out.println("Error code received: "+error);
				resp.sendRedirect("login.html");
				return;
			}
			catch(JSONException error)
			{
				System.out.println("Authorized successfully! Proceeding with data retrieval...");
			}
			
			String accessToken= json.getString("accessToken");
			String refreshToken= json.getString("refreshToken");
			String idToken= json.getString("idToken");
//			String apiDomain= json.getString("api_domain");
			
			System.out.println("Access Token: "+ accessToken);
			System.out.println("Refresh Token: "+ refreshToken);
			System.out.println("ID Token: "+ idToken);
//			System.out.println("Domain: "+ apiDomain);
			
			String parts[]= idToken.split("\\.");
			String header= new String(Base64.getUrlDecoder().decode(parts[0]));
			String payload=  new String(Base64.getUrlDecoder().decode(parts[1]));
			System.out.println("Header: "+header+"\nPayload: "+ payload);
			
			JSONObject jsonResp= new JSONObject(payload);
			
			User user= Helper.buildUserFromJson(jsonResp);
			int userId= UserOperation.getUserId(user.getEmail());
			
			if(userId == 0)
			{
				userId= UserOperation.addUser(user);
			}
			
			UserSession uSession= Helper.buildUserSession(accessToken, refreshToken, userId);
			int sessionId= UserOperation.addUserSession(uSession);
			
			Cookie userCookie= new Cookie("userId", userId+"");
			userCookie.setHttpOnly(true);
			Cookie sessionCookie= new Cookie("sessionId", sessionId+"");
			sessionCookie.setHttpOnly(true);
			
			resp.addCookie(userCookie);
			resp.addCookie(sessionCookie);
			resp.sendRedirect("dashboard.jsp");
		}
		catch(JSONException | InvalidException error)
		{
			System.out.println("Exception occurred: "+error.getMessage());
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
