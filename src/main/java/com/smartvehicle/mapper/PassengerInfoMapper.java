package com.smartvehicle.mapper;

import com.smartvehicle.entity.PassengerInfo;
import com.smartvehicle.payload.response.PassengerInfoDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PassengerInfoMapper {
    PassengerInfoDTO toDTO(PassengerInfo passengerInfo);

    List<PassengerInfoDTO> toResponseDTO(List<PassengerInfoDTO> passengerInfos);
}

