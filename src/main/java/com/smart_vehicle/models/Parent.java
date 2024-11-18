package com.smart_vehicle.models;

import jakarta.persistence.*;

@Entity
@Table(name = "smv_parent_reg")
public class Parent {

    @Column(name = "sch_uniq_id", nullable = false, length = 8)
    private String schoolUniqueId;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parent_id", nullable = false, length = 8)
    private String parentId;

    @Column(name = "parent_name", length = 20)
    private String parentName;

    @Column(name = "country_code", length = 3)
    private String countryCode;

    @Column(name = "contact_num", length = 12)
    private String contactNum;

    @Column(name = "last_role", length = 1)
    private String lastRole;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "reserve", length = 8)
    private String reserve;

//    @ManyToOne
//    @JoinColumn(name = "sch_uniq_id", referencedColumnName = "sch_id", foreignKey = @ForeignKey(name = "sch_uniq_id"))
//    private School school;
//
//    @ManyToOne
//    @JoinColumn(name = "user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_user_id"))
//    private User user;


    @Column(name = "user_id", nullable = false, length = 8)
    private String userId;

    // Getters and Setters

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

    public String getLastRole() {
        return lastRole;
    }

    public void setLastRole(String lastRole) {
        this.lastRole = lastRole;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getReserve() {
        return reserve;
    }

    public void setReserve(String reserve) {
        this.reserve = reserve;
    }

//    public School getSchool() {
//        return school;
//    }
//
//    public void setSchool(School school) {
//        this.school = school;
//    }
//
//    public User getUser() {
//        return user;
//    }
//
//    public void setUser(User user) {
//        this.user = user;
//    }

    public String getSchoolUniqueId() {
        return schoolUniqueId;
    }

    public void setSchoolUniqueId(String schoolUniqueId) {
        this.schoolUniqueId = schoolUniqueId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

