package com.smart_vehicle.payload.response;

public class JwtResponse {
  private String token;
  private String id;
  private String username;

  private Boolean twoFactorAuthentication;

  public JwtResponse(String id, String username, Boolean twoFactorAuthentication) {
    this.id = id;
    this.username = username;
    this.twoFactorAuthentication = twoFactorAuthentication;
  }

  public JwtResponse(String accessToken, String id, String username, Boolean twoFactorAuthentication) {
    this.token = accessToken;
    this.id = id;
    this.username = username;
    this.twoFactorAuthentication = twoFactorAuthentication;
  }

  public String getAccessToken() {
    return token;
  }

  public void setAccessToken(String accessToken) {
    this.token = accessToken;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Boolean getTwoFactorAuthentication() {
    return twoFactorAuthentication;
  }

  public void setTwoFactorAuthentication(Boolean twoFactorAuthentication) {
    this.twoFactorAuthentication = twoFactorAuthentication;
  }
}
