package auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;

import crud.TokenOperation;
import exception.InvalidException;

@SuppressWarnings("serial")
public class RefreshOurAuth extends HttpServlet
{
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{	
		try
		{
			HttpSession session= req.getSession(false);
			int sessionId= (int) session.getAttribute("sessionId");
			int userId= (int) session.getAttribute("userId");
			
			if(TokenOperation.refreshToken(sessionId, userId))
			{
				resp.setStatus(HttpServletResponse.SC_OK);
				resp.getWriter().write("{\"message\": \"" + "Token refreshed!" + "\"}");
			}
		}
		catch(InvalidException | JSONException error)
		{
			error.printStackTrace();
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.getWriter().write("{\"error\": \"" + "Refresh Interrupted!" + "\"}");
		}
	}
}
