package com.smartvehicle.mapper;

import com.smartvehicle.entity.Parent;
import com.smartvehicle.payload.response.ParentResponseDTO;
import com.smartvehicle.payload.response.ParentResponseUrDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ParentMapper {
    ParentResponseDTO toResponseDTO(Parent parent);

    ParentResponseUrDTO toResponseUrDTO(Parent parent);
}

