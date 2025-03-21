package crud;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import database.DatabaseConnection;
import exception.InvalidException;
import pojo.User;
import pojo.UserSession;

public class UserOperation {
	
	public static int getUserId(String email) throws InvalidException
	{
		String query= "Select user_id from User where email=?";
		
		try(Connection connection= DatabaseConnection.getConnection();
				PreparedStatement statement= connection.prepareStatement(query))
		{
			statement.setString(1, email);
			System.out.println(statement);
			
			try(ResultSet result = statement.executeQuery())
			{
				while(result.next())
				{
					return result.getInt(1);
				}
				return 0;
			}
		} catch (SQLException | ClassNotFoundException error) {
			System.out.println("SQL Exception: " + error.getMessage());
			throw new InvalidException("Error occurred while fetching from DB.", error);
		}
	}
	
	public static int addUser(User user) throws InvalidException
	{
		String query= "Insert into User(name, first_name, last_name, email, gender) values(?, ?, ?, ?, ?)";
		
		try(Connection connection= DatabaseConnection.getConnection();
				PreparedStatement statement= connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS))
		{
			statement.setString(1, user.getName());
			statement.setString(2, user.getFirstName());
			statement.setString(3, user.getLastName());
			statement.setString(4, user.getEmail());
			statement.setString(5, user.getGender());
			
			System.out.println(statement);
			int rowsAffected = statement.executeUpdate();

			if (rowsAffected != 0) {
				try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						return generatedKeys.getInt(1);
					}
				}
			}
			return 0;
		} catch (SQLException | ClassNotFoundException error) {
			System.out.println("SQL Exception: " + error.getMessage());
			throw new InvalidException("Error occurred while fetching from DB.", error);
		}
	}
	
	public static int addUserSession(UserSession uSession) throws InvalidException
	{
		String query= "Insert into UserSession(user_id, access_token, refresh_token) values(?, ?, ?)";
		
		try(Connection connection= DatabaseConnection.getConnection();
				PreparedStatement statement= connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS))
		{
			statement.setInt(1, uSession.getUserId());
			statement.setString(2, uSession.getAccessToken());
			statement.setString(3, uSession.getRefreshToken());
			
			System.out.println(statement);
			int rowsAffected = statement.executeUpdate();

			if (rowsAffected != 0) {
				try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						return generatedKeys.getInt(1);
					}
				}
			}
			return 0;
		} catch (SQLException | ClassNotFoundException error) {
			System.out.println("SQL Exception: " + error.getMessage());
			throw new InvalidException("Error occurred while fetching from DB.", error);
		}
	}
	
	public static User getUser(int userId) throws InvalidException
	{
		String query= "Select * from User where user_id=?";
		User user= new User();
		try(Connection connection= DatabaseConnection.getConnection();
				PreparedStatement statement= connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS))
		{
			statement.setInt(1, userId);
			
			System.out.println(statement);
			try(ResultSet result = statement.executeQuery())
			{
				while(result.next())
				{
					 user.setName(result.getString("name"));
					 user.setFirstName(result.getString("first_name"));
					 user.setLastName(result.getString("last_name"));
					 user.setEmail(result.getString("email"));
					 user.setGender(result.getString("gender"));
				}
			}
			return user;
		} catch (SQLException | ClassNotFoundException error) {
			System.out.println("SQL Exception: " + error.getMessage());
			throw new InvalidException("Error occurred while fetching from DB.", error);
		}
	}
}
