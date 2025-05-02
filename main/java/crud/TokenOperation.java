package crud;

import java.io.IOException;
import java.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import exception.InvalidException;
import helper.Helper;
import pojo.Client;
import pojo.UserSession;

public class TokenOperation {

	public static boolean refreshToken(int sessionId, int userId) throws InvalidException, JSONException, IOException
	{
		Client client= ClientOperation.getClient("OurAuth");
		UserSession userSession= UserOperation.getSession(sessionId, userId);
				
		StringBuilder tokenApi= new StringBuilder("http://localhost:8081/OurAuth");
		tokenApi.append("/token?")
		.append("grant_type=refresh_token")
		.append("&client_id=").append(client.getClientId())
		.append("&client_secret=").append(client.getClientSecret())
		.append("&refresh_token=").append(userSession.getRefreshToken());
		
		JSONObject response= Helper.connectionRequest(tokenApi.toString(), "POST", null);
		System.out.println("New refresh token: "+response.getString("refresh_token"));
		
		return UserOperation.updateSession(response.getString("access_token"), response.getString("refresh_token"), sessionId);
	}
	
	public static JSONObject introspectToken(int sessionId, int userId) throws InvalidException, JSONException, IOException
	{
		Client client= ClientOperation.getClient("OurAuth");
		UserSession userSession= UserOperation.getSession(sessionId, userId);
				
		StringBuilder tokenApi= new StringBuilder("http://localhost:8081/OurAuth");
		tokenApi.append("/introspect?")
//		.append("&client_id=").append(client.getClientId())
//		.append("&client_secret=").append(client.getClientSecret())
		.append("&token=").append(userSession.getAccessToken())
		.append("&token_type_hint=access_token");
		
		System.out.println("Token introspection req: "+tokenApi);
		String authToken= new String(Base64.getEncoder().encode((client.getClientId()+":"+client.getClientSecret()).getBytes()));
		JSONObject response= Helper.connectionRequest(tokenApi.toString(), "POST", "Basic "+authToken);
		System.out.println("Token meta-data: "+response.toString());
		
		return response;
	}
}
