package com.smartvehicle.mapper;

import com.smartvehicle.entity.Student;
import com.smartvehicle.payload.response.StudentResponseDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StudentMapper {
    StudentResponseDTO toResponseDTO(Student student);
}

