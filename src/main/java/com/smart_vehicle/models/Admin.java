package com.smart_vehicle.models;

import jakarta.persistence.*;

@Entity
@Table(name = "smv_admin_reg")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id", length = 8, nullable = false)
    private String adminId;

    @Column(name = "sch_uniq_id", length = 8)
    private String schoolUniqueId;

    @Column(name = "admin_name", length = 20)
    private String adminName;

    @Column(name = "country_code", length = 3)
    private String countryCode;

    @Column(name = "contact_num", length = 12)
    private String contactNumber;

    @Column(name = "last_role", length = 1)
    private String lastRole;

    @Column(name = "status")
    private Byte status;

    @Column(name = "reserve", length = 8)
    private String reserve;

    @Column(name = "user_id")
    private String userId;

    // Relationships
//    @ManyToOne
//    @JoinColumn(name = "sch_uniq_id", referencedColumnName = "sch_id", foreignKey = @ForeignKey(name = "fk_sch_uniq_id_admin"), nullable = true)
//    private School school;

//    @ManyToOne
//    @JoinColumn(name = "user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_admin_user_id"), nullable = true)
//    private User user;

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getSchoolUniqueId() {
        return schoolUniqueId;
    }

    public void setSchoolUniqueId(String schoolUniqueId) {
        this.schoolUniqueId = schoolUniqueId;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getLastRole() {
        return lastRole;
    }

    public void setLastRole(String lastRole) {
        this.lastRole = lastRole;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public String getReserve() {
        return reserve;
    }

    public void setReserve(String reserve) {
        this.reserve = reserve;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
}
