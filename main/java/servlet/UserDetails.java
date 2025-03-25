package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;

import crud.UserOperation;
import exception.InvalidException;
import pojo.User;

@SuppressWarnings("serial")
public class UserDetails extends HttpServlet{
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		HttpSession session= req.getSession(false);
		int userId=(int) session.getAttribute("userId");
		
		try
		{
			User user= UserOperation.getUser(userId);
			JSONObject json= new JSONObject();
			json.put("email", user.getEmail());
			json.put("name", user.getName());
			json.put("firstName", user.getFirstName());
			json.put("lastName", user.getLastName());
			json.put("gender", user.getGender());
			
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().write(json.toString());
		}
		catch(InvalidException | JSONException error)
		{
			error.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		HttpSession session= req.getSession(false);
		session.invalidate();
		resp.setStatus(HttpServletResponse.SC_OK);
	}
	
}
