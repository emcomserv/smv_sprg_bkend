package com.smartvehicle.payload.request;

import com.smartvehicle.entity.BaseEntity;
import com.smartvehicle.entity.Route;
import com.smartvehicle.entity.School;
import com.smartvehicle.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class AttenderUpdateReq {

    private Long id;
    private String schoolId;
    private String firstName;
    private String lastName;
    private String routeId;
    private String smAttenderId;
    private String email;
    private String countryCode;
    private String phone;

}
