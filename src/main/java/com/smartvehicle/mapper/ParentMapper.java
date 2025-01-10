package com.smartvehicle.mapper;

import com.smartvehicle.entity.Parent;
import com.smartvehicle.payload.response.ParentResponseDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ParentMapper {
    ParentResponseDTO toResponseDTO(Parent parent);
}

