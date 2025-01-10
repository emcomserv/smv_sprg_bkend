package com.smartvehicle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "smv_school")
@Getter
@Setter
public class School extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(name = "name", length = 20)
    private String name;

    @Column(name = "country_id", length = 3)
    private String countryId;

    @Column(name = "prov_id", length = 3)
    private String provId;

    @Column(name = "area_id", length = 3)
    private String areaId;

    @Column(name = "entity_id", length = 3)
    private String entityId;

    @Column(name = "contact_num", length = 10)
    private String contactNum;

    @Column(name = "contact_name", length = 20) // Updated annotation
    private String contactName;

    @Column(name = "status")
    private Boolean status;


}
