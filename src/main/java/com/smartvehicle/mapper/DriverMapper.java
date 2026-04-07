package com.smartvehicle.mapper;

import com.smartvehicle.entity.Driver;
import com.smartvehicle.payload.response.DriverResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface DriverMapper {
    @Mapping(target = "routeId", source = "route.id")
    @Mapping(target = "routeName", source = "route.routeName")
    @Mapping(target = "schoolId", source = "school.id")
    @Mapping(target = "schoolName", source = "school.name")
    DriverResponseDTO toResponseDTO(Driver driver);
    List<DriverResponseDTO> toResponseDTO(List<Driver> drivers);
}

