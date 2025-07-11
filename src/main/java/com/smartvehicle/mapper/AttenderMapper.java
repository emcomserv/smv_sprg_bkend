package com.smartvehicle.mapper;

import com.smartvehicle.entity.Attender;
import com.smartvehicle.payload.response.AttenderResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface AttenderMapper {
    @Mapping(target = "routeId", source = "route.smRouteId")
    @Mapping(target = "schoolId", source = "school.id")
    AttenderResponseDTO toResponseDTO(Attender attender);
    List<AttenderResponseDTO> toResponseDTO(List<Attender> attenders);
}

