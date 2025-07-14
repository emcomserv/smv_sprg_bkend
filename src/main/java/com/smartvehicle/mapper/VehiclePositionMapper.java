package com.smartvehicle.mapper;

import com.smartvehicle.entity.VehiclePosition;
import com.smartvehicle.payload.response.VehiclePositionResponseDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VehiclePositionMapper {
    VehiclePositionResponseDTO toResponseDTO(VehiclePosition vehiclePosition);
    List<VehiclePositionResponseDTO> toResponseDTO(List<VehiclePosition> vehiclePositions);
}
