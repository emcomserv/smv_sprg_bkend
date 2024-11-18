package com.smart_vehicle.models;

import jakarta.persistence.*;

@Entity
@Table(name = "smv_student_reg")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id", nullable = false, length = 8)
    private String studentId;

    @Column(name = "student_name", length = 50)
    private String studentName;

    @Column(name = "student_age", length = 100)
    private String studentAge;

    @Column(name = "student_sex", length = 1)
    private String studentSex;

    @Column(name = "student_status")
    private Boolean studentStatus;

    @Column(name = "reserve", length = 8)
    private String reserve;

    @ManyToOne
    @JoinColumn(name = "sch_uniq_id", referencedColumnName = "sch_id", foreignKey = @ForeignKey(name = "fk_sch_uniq_id_student"))
    private School school;

    @ManyToOne
    @JoinColumn(name = "route_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_route_id_student"))
    private Route route;

    @Column(name = "parent_name", length = 20)
    private String parentName;

    @Column(name = "parent_contact", length = 12)
    private String parentContact;

    @Column(name = "parent_id", length = 8)
    private String parentId;

    // Getters and Setters

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentAge() {
        return studentAge;
    }

    public void setStudentAge(String studentAge) {
        this.studentAge = studentAge;
    }

    public String getStudentSex() {
        return studentSex;
    }

    public void setStudentSex(String studentSex) {
        this.studentSex = studentSex;
    }

    public Boolean getStudentStatus() {
        return studentStatus;
    }

    public void setStudentStatus(Boolean studentStatus) {
        this.studentStatus = studentStatus;
    }

    public String getReserve() {
        return reserve;
    }

    public void setReserve(String reserve) {
        this.reserve = reserve;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getParentContact() {
        return parentContact;
    }

    public void setParentContact(String parentContact) {
        this.parentContact = parentContact;
    }

    public School getSchool() {
        return school;
    }

    public void setSchool(School school) {
        this.school = school;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
