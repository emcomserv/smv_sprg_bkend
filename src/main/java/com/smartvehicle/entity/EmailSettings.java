package com.smartvehicle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "smv_email_settings")
public class EmailSettings extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "host", nullable = false, length = 255)
    private String host;

    @Column(name = "port", nullable = false)
    private Integer port;

    @Column(name = "username", nullable = false, length = 255)
    private String username; // medium email address

    @Column(name = "password", nullable = false, length = 255)
    private String password; // medium email password

    @Column(name = "recipient", nullable = false, length = 255)
    private String recipient; // receiver email address
}


