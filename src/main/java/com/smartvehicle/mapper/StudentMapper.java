package com.smartvehicle.mapper;

import com.smartvehicle.entity.Student;
import com.smartvehicle.payload.response.StudentResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class , RoutePointMapper.class})
public interface StudentMapper {

    @Mapping(target = "routeId", source = "route.id")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "schoolId", source = "school.id")
    @Mapping(target = "routeName", source = "route.routeName")
    @Mapping(target = "schoolName", source = "school.name")
    StudentResponseDTO toResponseDTO(Student student);

    List<StudentResponseDTO> toResponseDTO(List<Student> student);
}

