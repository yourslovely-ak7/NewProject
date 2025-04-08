package helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import exception.InvalidException;
import pojo.User;
import pojo.UserSession;

public class Helper {
	
	private static final String redirectURIZoho= "http://localhost:8080/NewProject/oauthredirect";
	private static final String redirectURIOurAuth= "http://localhost:8080/NewProject/oauthredirect/ourauth";
	private static final String refreshApi= "http://localhost:8080/NewProject/refresh";
	
public static String getRedirectURIZoho()
	{
		return redirectURIZoho;
	}
	
	public static String getRedirectURIOurAuth()
	{
		return redirectURIOurAuth;
	}

	public static String getRefreshApi()
	{
		return refreshApi;
	}
	
	public static void checkForNull(Object obj) throws InvalidException
	{
		if(obj==null)
		{
			throw new InvalidException("Value cannot be Null!");
		}
	}
	
	public static User buildUserFromJson(JSONObject data) throws JSONException
	{
		User user= new User();
		
		user.setName(data.getString("name"));
		user.setFirstName(data.getString("first_name"));
		user.setLastName(data.getString("last_name"));
		user.setEmail(data.getString("email"));
		user.setGender(data.getString("gender"));
		
		return user;
	}
	
	public static UserSession buildUserSession(String accessToken, String refreshToken, int userId)
	{
		UserSession uSession= new UserSession();
		uSession.setAccessToken(accessToken);
		uSession.setRefreshToken(refreshToken);
		uSession.setUserId(userId);
		
		return uSession;
	}

	public static JSONObject connectionRequest(String api, String reqType) throws JSONException, IOException
	{
		URL url= new URL(api);
		HttpURLConnection connection= (HttpURLConnection) url.openConnection();
		try
		{
			connection.setRequestMethod(reqType);
			connection.setDoOutput(true);
			connection.setRequestProperty("Accept", "application/json");
			
			return sendRequest(connection);
		}
		finally
		{
			connection.disconnect();
		}
	}
	
	public static int connectionStatusCode(String api, String reqType) throws JSONException, IOException
	{
		URL url= new URL(api);
		HttpURLConnection connection= (HttpURLConnection) url.openConnection();
		try
		{
			connection.setRequestMethod(reqType);
			connection.setDoOutput(true);
			connection.setRequestProperty("Accept", "application/json");
			
			return connection.getResponseCode();
		}
		finally
		{
			connection.disconnect();
		}
	}
	
	public static JSONObject connectionRequestWithToken(String api, String reqType, String aToken) throws JSONException, IOException
	{
		URL url= new URL(api);
		HttpURLConnection connection= (HttpURLConnection) url.openConnection();
		try
		{
			connection.setRequestMethod(reqType);
			connection.setDoOutput(true);
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Authorization", "Bearer "+aToken);
			
			return sendRequest(connection);
		}
		finally
		{
			connection.disconnect();
		}
	}
	
	private static JSONObject sendRequest(HttpURLConnection connection) throws IOException, JSONException
	{
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
		
		return new JSONObject(response.toString());
	}

	public static JSONObject validateToken(String idToken, String api) throws JSONException, IOException, InvalidException
	{
		try
		{
			String parts[]= idToken.split("\\.");
			String header= new String(Base64.getUrlDecoder().decode(parts[0]));
			JSONObject headerJson= new JSONObject(header);
			
			String kId= headerJson.getString("kid");
			PublicKey publicKey= Helper.getPublicKey(api, kId);
			
			String headerAndPayload= parts[0]+"."+parts[1];
			String signature= parts[2];
			
			Signature sign= Signature.getInstance("SHA256withRSA");
			sign.initVerify(publicKey);
			sign.update(headerAndPayload.getBytes(StandardCharsets.UTF_8));
			
			if(sign.verify(Base64.getUrlDecoder().decode(signature)))
			{
				System.out.println("Signature checked!");
				String payload=  new String(Base64.getUrlDecoder().decode(parts[1]));
				System.out.println("Header: "+header+"\nPayload: "+ payload);
				
				return new JSONObject(payload);
			}
			return null;
			
		}
		catch(JSONException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException error)
		{
			System.out.println(error.getMessage());
			throw new InvalidException("Error validating ID token.",error);
		}
	}

	public static PublicKey getPublicKey(String api, String kId) throws JSONException, IOException, NoSuchAlgorithmException, InvalidKeySpecException
	{
		JSONObject jwks= connectionRequest(api, "GET");
		JSONArray keys= jwks.getJSONArray("keys");
		
		int len= keys.length();
		BigInteger modulus= null,exponent=null;
		KeyFactory keyFactory=null;
		
		for(int i=0; i<len; i++)
		{
			JSONObject iter= keys.getJSONObject(i);
			
			if(iter.getString("kid").equals(kId))
			{
				modulus= new BigInteger(1, Base64.getUrlDecoder().decode(iter.getString("n")));
				exponent= new BigInteger(1, Base64.getUrlDecoder().decode(iter.getString("e")));
				keyFactory= KeyFactory.getInstance(iter.getString("kty"));
				break;
			}
		}
		
		RSAPublicKeySpec keySpec= new RSAPublicKeySpec(modulus, exponent);
		return keyFactory.generatePublic(keySpec);
	}
}
