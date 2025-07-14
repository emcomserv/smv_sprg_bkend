package com.smartvehicle.mapper;

import com.smartvehicle.entity.User;
import com.smartvehicle.payload.response.UserResponseDTO;
import com.smartvehicle.payload.response.UserResponseLtDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDTO toResponseDTO(User user);

    UserResponseLtDTO toResponseLtDTO(User user);
}

