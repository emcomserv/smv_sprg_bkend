//package com.smartvehicle.controller;
//
//import com.smartvehicle.entity.VehiclePosition;
//import com.smartvehicle.mapper.VehiclePositionMapper;
//import com.smartvehicle.payload.response.VehiclePositionResponseDTO;
//import com.smartvehicle.repository.VehiclePositionRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/v1/vehicle-positions")
//public class VehiclePositionController {
//
//    @Autowired
//    private VehiclePositionRepository vehiclePositionRepository;
//
//    @Autowired
//    private VehiclePositionMapper vehiclePositionMapper;
//
//    /**
//     * Get all vehicle positions
//     */
//    @GetMapping("/route/sm/{routeId}")
//    public ResponseEntity<List<VehiclePositionResponseDTO>> getAllVehiclePositionsByRouteId(@PathVariable String routeId) {
//        List<VehiclePosition> vehiclePositions = vehiclePositionRepository.getByRouteId(routeId);
//        List<VehiclePositionResponseDTO> response = vehiclePositionMapper.toResponseDTO(vehiclePositions);
//        return ResponseEntity.ok(response);
//    }
//
//
//
//}
