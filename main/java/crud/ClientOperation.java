package crud;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.DatabaseConnection;
import exception.InvalidException;
import pojo.Client;

public class ClientOperation {
	
	public static Client getClient(String authType) throws InvalidException {
		String query = "Select client_id, client_secret, jwks_url from ClientDetails where authType=?";
		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(query)) 
		{
			statement.setString(1, authType);
			System.out.println(statement);
			try(ResultSet result = statement.executeQuery())
			{
				Client client = new Client();
				while (result.next()) {
					client.setClientId(result.getString("client_id"));
					client.setClientSecret(result.getString("client_secret"));
					client.setJwksUrl(result.getString("jwks_url"));
					client.setAuthType(authType);
				}
				return client;
			}
		} catch (SQLException | ClassNotFoundException error) {
			System.out.println("SQL Exception: " + error.getMessage());
			throw new InvalidException("Error occurred while fetching from DB.", error);
		}
	}

}
