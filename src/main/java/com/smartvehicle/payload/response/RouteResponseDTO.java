package com.smartvehicle.payload.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RouteResponseDTO {
    private Long id;
    private String schId;
    private String routeName;
    private String title;
    private Boolean status;
    private Integer reserve;
    private String content;
}
