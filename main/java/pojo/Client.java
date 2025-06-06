package pojo;

public class Client {
	
	private String clientId;
	private String clientSecret;
	private String authType;
	private String jwksUrl;
	

	public String getJwksUrl() {
		return jwksUrl;
	}
	public void setJwksUrl(String jwksUrl) {
		this.jwksUrl = jwksUrl;
	}
	public String getClientId() {
		return clientId;
	}
	public String getClientSecret() {
		return clientSecret;
	}
	public String getAuthType() {
		return authType;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
	public void setAuthType(String authType) {
		this.authType = authType;
	}
}
