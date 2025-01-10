package com.smartvehicle.mapper;

import com.smartvehicle.entity.School;
import com.smartvehicle.payload.response.SchoolResponseDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SchoolMapper {
    SchoolResponseDTO toResponseDTO(School school);
}

