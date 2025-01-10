package com.smartvehicle.mapper;

import com.smartvehicle.entity.Admin;
import com.smartvehicle.payload.response.AdminResponseDTO;
import org.mapstruct.Mapper;

@Mapper
public interface AdminMapper {
    AdminResponseDTO toResponseDTO(Admin admin);
}

