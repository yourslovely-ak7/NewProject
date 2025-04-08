package crud;

import java.io.IOException;

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
		.append("responseType=refresh")
		.append("&clientId=").append(client.getClientId())
		.append("&clientSecret=").append(client.getClientSecret())
		.append("&refreshToken=").append(userSession.getRefreshToken());
		
		JSONObject response= Helper.connectionRequest(tokenApi.toString(), "POST");
		System.out.println("New refresh token: "+response.getString("refreshToken"));
		
		return UserOperation.updateSession(response.getString("accessToken"), response.getString("refreshToken"), sessionId);
	}
}
