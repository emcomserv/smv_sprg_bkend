package com.smartvehicle.mapper;

import com.smartvehicle.entity.User;
import com.smartvehicle.payload.response.UserResponseDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDTO toResponseDTO(User user);
}

