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
public class OAuthOurAuth extends HttpServlet 
{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try
		{
			StringBuilder authCodeApi= new StringBuilder();
			authCodeApi.append("http://localhost:8081/OurAuth/auth?")
			.append("responseType=code")
			.append("&clientId=").append(ClientOperation.getClient("OurAuth").getClientId())
			.append("&scope=").append("email ").append("profile ").append("RESOURCE.all")
			.append("&redirectUrl=").append(Helper.getRedirectURIOurAuth());

			System.out.println("authCodeReq: "+authCodeApi);
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.sendRedirect(authCodeApi.toString());
		}
		catch(InvalidException error)
		{
			System.out.println(error.getMessage());
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
}
