package com.smartvehicle.mapper;

import com.smartvehicle.entity.Driver;
import com.smartvehicle.payload.response.DriverResponseDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DriverMapper {
    DriverResponseDTO toResponseDTO(Driver driver);
}

