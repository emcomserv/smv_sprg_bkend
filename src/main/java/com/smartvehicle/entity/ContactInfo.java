package com.smartvehicle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "smv_contact_info")
public class ContactInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 20)
    private String fullName;

    @Column(name = "contact_number", nullable = false, length = 20)
    private String contactNumber;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "school_name", nullable = false, length = 20)
    private String schoolName;

    @Column(name = "message", nullable = false, length = 200)
    private String message;
}


