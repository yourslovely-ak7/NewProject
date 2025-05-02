package auth;

import java.io.IOException;
import java.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;

import crud.ClientOperation;
import crud.TokenOperation;
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
			String server= req.getParameter("api_domain");
			String clientSecret="";
			
			Client client= ClientOperation.getClient("OurAuth");
			clientSecret= client.getClientSecret();
			Helper.checkForNull(clientSecret);
			
			StringBuilder tokenApi= new StringBuilder(server);
			tokenApi.append("/token?")
			.append("grant_type=authorization_code")
//			.append("&client_id=").append(client.getClientId())
//			.append("&client_secret=").append(clientSecret)
			.append("&redirect_uri=").append(Helper.getRedirectURIOurAuth())
			.append("&code="+authCode);
			
			System.out.println("Token req: "+tokenApi.toString());
			
			String authToken= new String(Base64.getEncoder().encode((client.getClientId()+":"+clientSecret).getBytes()));
			JSONObject json= Helper.connectionRequest(tokenApi.toString(), "POST", "Basic "+authToken);
			
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
			
			JSONObject jsonResp= Helper.validateToken(idToken, client.getJwksUrl());
			Helper.checkForNull(jsonResp);
			
			if(jsonResp.getLong("exp") <= System.currentTimeMillis()/1000)
			{
				throw new InvalidException("Token Expired!");
			}

			User user= Helper.buildUserFromJson(jsonResp);
			int userId= UserOperation.getUserId(user.getEmail());
			
			if(userId == 0)
			{
				userId= UserOperation.addUser(user);
			}
			
			UserSession uSession= Helper.buildUserSession(accessToken, refreshToken, userId);
			int sessionId= UserOperation.addUserSession(uSession);
			
			HttpSession session= req.getSession(true);
			session.setAttribute("userId", userId);
			session.setAttribute("sessionId", sessionId);
			
			TokenOperation.introspectToken(sessionId, userId);
			
			resp.sendRedirect("../dashboard.jsp");
		}
		catch(JSONException | InvalidException error)
		{
			System.out.println("Exception occurred: "+error.getMessage());
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
//	public JSONObject connectionRequest(String api, String reqType) throws JSONException, IOException
//	{
//		URL url= new URL(api);
//		HttpURLConnection connection= (HttpURLConnection) url.openConnection();
//		connection.setRequestMethod(reqType);
//		connection.setDoOutput(true);
//		connection.setRequestProperty("Accept", "application/json");
//		
//		StringBuilder response= new StringBuilder();
//		try(BufferedReader reader= new BufferedReader(new InputStreamReader(connection.getInputStream())))
//		{
//			String line;
//			while((line = reader.readLine()) != null)
//			{
//				response.append(line);
//			}
//		}
//		finally
//		{
//			connection.disconnect();
//		}
//		System.out.println("Response: "+response);
//		
//		return new JSONObject(response.toString());
//	}
}
