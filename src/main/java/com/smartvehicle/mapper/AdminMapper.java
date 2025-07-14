package com.smartvehicle.mapper;

import com.smartvehicle.entity.Admin;
import com.smartvehicle.payload.response.AdminResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class ,RouteMapper.class})
public interface AdminMapper {

    @Mapping(target = "schId", source = "school.id")
    @Mapping(target = "schoolName", source = "school.name")
    AdminResponseDTO toResponseDTO(Admin admin);
    List<AdminResponseDTO> toResponseDTO(List<Admin> admins);
}

