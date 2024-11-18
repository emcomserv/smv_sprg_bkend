package com.smart_vehicle.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "smv_route_reg")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "sch_uniq_id", length = 8)
    private String schoolUniqueId;

    @Column(name = "route_name", length = 12)
    private String routeName;

    @Column(name = "route_points_count")
    private Integer routePointsCount;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "reserve")
    private Integer reserve;

    @Column(name = "content", length = 255)
    private String content;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "title", length = 255)
    private String title;

//    @ManyToOne
//    @JoinColumn(name = "sch_uniq_id", referencedColumnName = "sch_id", foreignKey = @ForeignKey(name = "fk_sch_uniq_id_route"))
//    private School school;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSchoolUniqueId() {
        return schoolUniqueId;
    }

    public void setSchoolUniqueId(String schoolUniqueId) {
        this.schoolUniqueId = schoolUniqueId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public Integer getRoutePointsCount() {
        return routePointsCount;
    }

    public void setRoutePointsCount(Integer routePointsCount) {
        this.routePointsCount = routePointsCount;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Integer getReserve() {
        return reserve;
    }

    public void setReserve(Integer reserve) {
        this.reserve = reserve;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

//    public School getSchool() {
//        return school;
//    }
//
//    public void setSchool(School school) {
//        this.school = school;
//    }
}
