package com.smartvehicle.mapper;

import com.smartvehicle.entity.Parent;
import com.smartvehicle.payload.response.ParentResponseDTO;
import com.smartvehicle.payload.response.ParentResponseUrDTO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ParentMapper {
    @Mapping(target = "schId", ignore = true)
    ParentResponseDTO toResponseDTO(Parent parent);

    ParentResponseUrDTO toResponseUrDTO(Parent parent);
    List<ParentResponseDTO> toResponseDTO(List<Parent> parents);

    @AfterMapping
    default void setSchoolId(Parent parent, @MappingTarget ParentResponseDTO dto) {
        if (parent != null && parent.getSchool() != null) {
            dto.setSchId(parent.getSchool().getId());
        }
    }
}