package com.smart_vehicle.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "smv_user", schema = "smartvehicle")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private String userId;


    @NotNull
    @Column(name = "username")
    private String username;

    @NotNull
    @Column(name = "password")
    private String password;

    @NotNull
    @Column(name = "phone")
    private String phone;

    // Default constructor
    public User() {
    }

    // Parametrized constructor
    public User(String username, String password, String phone) {
        this.username = username;
        this.password = password;
        this.phone = phone;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getContactNum() {
        return phone;
    }

    public void setContactNum(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "UserEntity [  userName=" + username + ", phone=" +phone + ", password=" +password +  "]";
    }
}
