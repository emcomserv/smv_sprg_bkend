package com.smartvehicle.mapper;

import com.smartvehicle.entity.Route;
import com.smartvehicle.payload.response.RouteResponseDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RouteMapper {
    RouteResponseDTO toResponseDTO(Route route);
}
