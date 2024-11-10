package com.smart_vehicle.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "smv_parent", schema = "smartvehicle")
public class Parent implements Serializable {

    private static final long serialVersionUID = 1L;

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "sch_uniq_id", length = 8)
//    private String schUniqId; // Unique identifier


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parent_id")
    private String parentId; // Parent ID

    @NotNull
    @Column(name = "parent_name")
    private String parentName; // Parent Name

    @NotNull
    @Column(name = "user_name")
    private String userName; // User Name

    @NotNull
    @Column(name = "email")
    private String email;

    @NotNull
    @Column(name = "password")
    private String password;

    @Column(name = "country_code")
    private String countryCode; // Country Code

    @NotNull
    @Column(name = "contact_num")
    private String contactNum; // Contact Number

//    @Column(name = "last_role", length = 1)
//    private String lastRole; // Last Role (single character)
//
//    @Column(name = "status")
//    private boolean status; // Status (true/false)
//
//    @Column(name = "reserve", length = 8)
//    private byte[] reserve; // Reserved field (binary type, 8 bytes)

    // Default constructor
    public Parent() {
    }

    // Parametrized constructor
    public Parent(String parentName,String userName, String email, String password, String contactNum) {
        this.parentName = parentName;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.contactNum = contactNum;
    }

    // Getters and Setters
//    public String getSchUniqId() {
//        return schUniqId;
//    }
//
//    public void setSchUniqId(String schUniqId) {
//        this.schUniqId = schUniqId;
//    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getContactNum() {
        return contactNum;
    }

    public void setContactNum(String contactNum) {
        this.contactNum = contactNum;
    }

//    public String getLastRole() {
//        return lastRole;
//    }
//
//    public void setLastRole(String lastRole) {
//        this.lastRole = lastRole;
//    }
//
//    public boolean isStatus() {
//        return status;
//    }
//
//    public void setStatus(boolean status) {
//        this.status = status;
//    }
//
//    public byte[] getReserve() {
//        return reserve;
//    }
//
//    public void setReserve(byte[] reserve) {
//        this.reserve = reserve;
//    }

    @Override
    public String toString() {
        return "ParentEntity [ parentName=" + parentName
                + ", userName=" + userName + ", contactNum=" +contactNum +  ", email=" +email +  ", password=" +password +  "]";
    }
}
