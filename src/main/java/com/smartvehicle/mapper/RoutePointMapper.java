package com.smartvehicle.mapper;

import com.smartvehicle.entity.RoutePoint;
import com.smartvehicle.payload.response.RoutePointResponseDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoutePointMapper {
    RoutePointResponseDTO toResponseDTO(RoutePoint routePoint);
}

