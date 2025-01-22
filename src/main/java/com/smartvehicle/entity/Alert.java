package com.smartvehicle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "smv_alert")
public class Alert extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "body", nullable = false)
    private String body;

    @Column(name = "alert_type", nullable = false)
    private String alertType; // e.g., CRITICAL, WARNING, INFO

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

}

