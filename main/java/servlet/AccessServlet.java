package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;

import crud.TokenOperation;
import crud.UserOperation;
import exception.InvalidException;
import helper.Helper;
import pojo.UserSession;

@SuppressWarnings("serial")
public class AccessServlet extends HttpServlet
{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try
		{
			HttpSession session= req.getSession(false);
			int sessionId= (int) session.getAttribute("sessionId");
			int userId= (int) session.getAttribute("userId");
			UserSession sessionEntry= UserOperation.getSession(sessionId, userId);
			
			String resourceApi= "http://localhost:8081/OurAuth/resource";
			
			JSONObject response= Helper.connectionRequestWithToken(resourceApi, "GET", sessionEntry.getAccessToken());
			
			String error= response.optString("error", "no_error");
			System.out.println("Error: "+ error);
			
			if("token_expired".equals(error))
			{
				System.out.println("RT: "+sessionEntry.getRefreshToken()+"\nAT: "+sessionEntry.getAccessToken());

				if(TokenOperation.refreshToken(sessionId, userId))
				{
					System.out.println("Token refreshed successfully!");
					//initiate this request again...
					req.getRequestDispatcher("/access").forward(req, resp);
					return;
				}
				else
				{
					throw new InvalidException("Token cannot be refreshed!");
				}
			}
			
			System.out.println("Message from resource API: "+response.getString("message"));
			
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().write(response.toString());
		}
		catch(JSONException | InvalidException error)
		{
			error.printStackTrace();
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			resp.getWriter().write("{\"error\": \"" + "Request interrupted!" + "\"}");
		}
		catch(IOException error)
		{
			System.out.println("Error: "+ error.getMessage());
		}
	}
}
