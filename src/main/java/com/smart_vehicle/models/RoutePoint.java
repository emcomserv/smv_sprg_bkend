package com.smart_vehicle.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "smv_route_point_reg")
public class RoutePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "route_point_name", length = 40)
    private String routePointName;

    @Column(name = "latitude", length = 16)
    private String latitude;

    @Column(name = "longitude", length = 16)
    private String longitude;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "reserve", length = 8)
    private String reserve;

    @Column(name = "content", length = 255)
    private String content;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "title", length = 255)
    private String title;

    @ManyToOne
    @JoinColumn(name = "route_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_route_id_route"))
    private Route route;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoutePointName() {
        return routePointName;
    }

    public void setRoutePointName(String routePointName) {
        this.routePointName = routePointName;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
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

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }
}
