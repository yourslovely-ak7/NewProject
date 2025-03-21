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
public class OAuthCallbackZoho extends HttpServlet
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
		
		try
		{
		String authCode= req.getParameter("code");
//		String location= req.getParameter("location");
		String server= req.getParameter("accounts-server");
		String clientSecret="";
		
		Client client= Helper.getClient("Zoho");
		clientSecret= client.getClientSecret();
		Helper.checkForNull(clientSecret);
		
		StringBuilder tokenApi= new StringBuilder(server);
		tokenApi.append("/oauth/v2/token?")
		.append("client_id=").append(client.getClientId())
		.append("&client_secret=").append(clientSecret)
		.append("&grant_type=authorization_code")
		.append("&redirect_uri=").append(Helper.getRedirectURIZoho())
		.append("&code="+authCode);
		
		
		System.out.println("Token req: "+ tokenApi.toString());
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
			
			String accessToken= json.getString("access_token");
			String refreshToken= json.getString("refresh_token");
			String idToken= json.getString("id_token");
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
//			
//			req.setAttribute("email", jsonResp.getString("email"));
//			req.setAttribute("name", jsonResp.getString("name"));
//			req.setAttribute("first_name", jsonResp.getString("first_name"));
//			req.setAttribute("last_name", jsonResp.getString("last_name"));
//			req.setAttribute("gender", jsonResp.getString("gender"));
//			
//			req.getRequestDispatcher("dashboard.jsp").forward(req, resp);
		}
		catch(JSONException | InvalidException error)
		{
			System.out.println("Exception occurred: "+error.getMessage());
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
