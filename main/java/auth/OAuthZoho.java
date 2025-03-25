package auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import crud.ClientOperation;
import exception.InvalidException;
import helper.Helper;

@SuppressWarnings("serial")
public class OAuthZoho extends HttpServlet 
{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try
		{
			StringBuilder authCodeApi= new StringBuilder();
			authCodeApi.append("https://accounts.zoho.com/oauth/v2/auth?")
			.append("response_type=code")
			.append("&client_id=").append(ClientOperation.getClient("Zoho").getClientId())
			.append("&scope=").append("email ").append("profile")
			.append("&redirect_uri=").append(Helper.getRedirectURIZoho())
			.append("&access_type=offline")
			.append("&prompt=consent");
			
			System.out.println("authCodeReq: "+authCodeApi);
			resp.sendRedirect(authCodeApi.toString());			
		}
		catch(InvalidException error)
		{
			System.out.println(error.getMessage());
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
}
