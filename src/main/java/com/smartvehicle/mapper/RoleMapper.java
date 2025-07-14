package com.smartvehicle.mapper;

import com.smartvehicle.entity.Role;
import com.smartvehicle.payload.response.RoleResponseDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleResponseDTO toResponseDTO(Role role);
}

