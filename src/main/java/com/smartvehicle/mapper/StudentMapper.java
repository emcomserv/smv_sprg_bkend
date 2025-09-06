package com.smartvehicle.mapper;

import com.smartvehicle.entity.Student;
import com.smartvehicle.payload.response.StudentResponseDTO;
import com.smartvehicle.payload.response.StudentResponseLtDTO;
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
    @Mapping(target = "parentFirstName", source = "parent.firstName")
    @Mapping(target = "parentLastName", source = "parent.lastName")
    @Mapping(target = "parentUsername", source = "parent.user.username")
    @Mapping(target = "smParentId", source = "parent.smParentId")
    @Mapping(target = "routePointId", source = "routePoint.id")
    StudentResponseDTO toResponseDTO(Student student);

    @Mapping(target = "routeId", source = "route.id")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "schoolId", source = "school.id")
    @Mapping(target = "routeName", source = "route.routeName")
    @Mapping(target = "schoolName", source = "school.name")
    @Mapping(target = "parentFirstName", source = "parent.firstName")
    @Mapping(target = "parentLastName", source = "parent.lastName")
    @Mapping(target = "parentUsername", source = "parent.user.username")
    @Mapping(target = "smParentId", source = "parent.smParentId")
    StudentResponseLtDTO toResponseLtDTO(Student student);

    List<StudentResponseDTO> toResponseDTO(List<Student> student);
    List<StudentResponseLtDTO> toResponseLtDTO(List<Student> student);
}

