//package com.smart_vehicle.payload.request;
//
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotNull;
//
//public class AddHomeRequest {
//
//	@NotBlank
//	private String userName;
//
//	@NotBlank
//	private String country;
//
//	@NotBlank
//	private int pinCode;
//
//	private String houseName;
//
//	private int houseNumber;
//
//	private int floorNumber;
//
//	private int flatNumber;
//
//	@NotNull
//	private int rooms;
//
//	public String getUserName() {
//		return userName;
//	}
//
//	public void setUserName(String userName) {
//		this.userName = userName;
//	}
//
//	public String getCountry() {
//		return country;
//	}
//
//	public void setCountry(String country) {
//		this.country = country;
//	}
//
//	public int getPinCode() {
//		return pinCode;
//	}
//
//	public void setPinCode(int pinCode) {
//		this.pinCode = pinCode;
//	}
//
//	public String getHouseName() {
//		return houseName;
//	}
//
//	public void setHouseName(String houseName) {
//		this.houseName = houseName;
//	}
//
//	public int getHouseNumber() {
//		return houseNumber;
//	}
//
//	public void setHouseNumber(int houseNumber) {
//		this.houseNumber = houseNumber;
//	}
//
//	public int getFloorNumber() {
//		return floorNumber;
//	}
//
//	public void setFloorNumber(int floorNumber) {
//		this.floorNumber = floorNumber;
//	}
//
//	public int getFlatNumber() {
//		return flatNumber;
//	}
//
//	public void setFlatNumber(int flatNumber) {
//		this.flatNumber = flatNumber;
//	}
//
//	public int getRooms() {
//		return rooms;
//	}
//
//	public void setRooms(int rooms) {
//		this.rooms = rooms;
//	}
//
//	@Override
//	public String toString() {
//		return "AddHomeRequest [userName=" + userName + ", country=" + country + ", pinCode=" + pinCode + ", houseName="
//				+ houseName + ", houseNumber=" + houseNumber + ", floorNumber=" + floorNumber + ", flatNumber="
//				+ flatNumber + ", rooms=" + rooms + "]";
//	}
//
//}
