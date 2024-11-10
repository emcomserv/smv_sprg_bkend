package com.smart_vehicle.payload.response;

import java.util.List;

public class MessageResponse {
	private String message;

	private List<?> data;



	public MessageResponse(String message) {
		this.message = message;
	}

	public MessageResponse(List<?> data) {
		this.data = data;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<?> getData() {
		return data;
	}

	public void setData(List<?> data) {
		this.data = data;
	}

}
