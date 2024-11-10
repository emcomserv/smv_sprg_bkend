package com.smart_vehicle.payload.request;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

	@NotBlank
	private String userName;

	@NotBlank
	private String pssword;

	public String getUsername() {
		return userName;
	}

	public void setUsername(String username) {
		this.userName = username;
	}

	public String getPassword() {
		return pssword;
	}

	public void setPassword(String password) {
		this.pssword = password;
	}

	@Override
	public String toString() {
		return "LoginRequest [userName=" + userName + ", password=" + pssword + "]";
	}

}