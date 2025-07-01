package com.smartvehicle.mapper;

import com.smartvehicle.entity.Parent;
import com.smartvehicle.payload.response.ParentResponseDTO;
import com.smartvehicle.payload.response.ParentResponseUrDTO;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ParentMapper {
    ParentResponseDTO toResponseDTO(Parent parent);

    ParentResponseUrDTO toResponseUrDTO(Parent parent);
    List<ParentResponseDTO> toResponseDTO(List<Parent> parents);
}

