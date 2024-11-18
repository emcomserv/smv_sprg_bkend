package com.smart_vehicle.models;

import jakarta.persistence.*;

@Entity
@Table(name = "smv_school_reg")
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sch_id", nullable = false, length = 8)
    private String schoolId;

    @Column(name = "sch_country_id", length = 3)
    private String countryId;

    @Column(name = "sch_prov_id", length = 3)
    private String provinceId;

    @Column(name = "sch_area_id", length = 3)
    private String areaId;

    @Column(name = "sch_entity_id", length = 3)
    private String entityId;

    @Column(name = "sch_name", length = 20)
    private String schoolName;

    @Column(name = "sch_contact_num", length = 10)
    private String contactNumber;

    @Column(name = "sch_contact_name", length = 20)
    private String contactName;

    @Column(name = "sch_status")
    private Boolean status;

    // Getters and Setters

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public String getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(String provinceId) {
        this.provinceId = provinceId;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
