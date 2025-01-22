package com.smartvehicle.mapper;

import com.smartvehicle.entity.Route;
import com.smartvehicle.payload.response.RouteResponseDTO;
import com.smartvehicle.payload.response.RouteResponseLtDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {RoutePointMapper.class})
public interface RouteMapper {
    RouteResponseDTO toResponseDTO(Route route);

    List<RouteResponseDTO> toResponseDTO(List<Route> routes);


    RouteResponseLtDTO toResponseLtDTO(Route route);

    List<RouteResponseLtDTO> toResponseLtDTO(List<Route> routes);
}
