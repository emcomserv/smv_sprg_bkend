package com.smartvehicle.controller;

import com.smartvehicle.entity.SwipeImage;
import com.smartvehicle.payload.request.SwapReportReq;
import com.smartvehicle.payload.response.SwapReportResponse;
import com.smartvehicle.service.FTPClientService;
import com.smartvehicle.service.SwipeImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/swipeimage")
public class SwipeImageController {
    @Autowired
    private SwipeImageService swipeImageService;

    @Autowired
    private FTPClientService ftpService;

    @GetMapping("/studentId")
    public ResponseEntity<List<SwipeImage>> getSwipeByStudentId(@RequestParam String studentId) {
        return ResponseEntity.ok(swipeImageService.getSwipesByStudentId(studentId));
    }

    @GetMapping("/byName")
    public ResponseEntity<byte[]> getImageByName(@RequestParam String imageName) {
        String[] split = imageName.split("/");
        if(split.length < 2 && split.length > 1)
            imageName = split[0] + "/" + split[1] + "/Default/"+ split[1] + "_Default.jpg";

        try {
            byte[] imageData = ftpService.readFile(imageName);
            return new ResponseEntity<>(imageData, HttpStatus.OK);
        } catch (IOException e) {
            log.debug(e.getMessage());
            throw new RuntimeException("Failed to read image: " + imageName, e);
        }
    }

    @GetMapping("/by-school-with-date-range")
    public ResponseEntity<?> getBySchoolAndDateRange(@RequestParam String schoolId, @RequestParam Date startDate, @RequestParam Date endDate){
        SwapReportReq request = new SwapReportReq();
        request.setSchoolId(schoolId);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        List<SwapReportResponse> responses = swipeImageService.getSwipesBySchoolAndDateRange(request);
        if(responses.size() > 0)
            return ResponseEntity.ok(responses);

        return new ResponseEntity<>("Data not found for requested criteria", HttpStatus.NO_CONTENT);
    }

    @GetMapping("/by-school-with-filters")
    public ResponseEntity<?> getBySchoolAndRouteAndDateRange(@RequestParam String schoolId, @RequestParam Date startDate, @RequestParam Date endDate, @RequestParam(required = false) String routeId, @RequestParam(required = false) String studentId){
        SwapReportReq request = new SwapReportReq();
        request.setSchoolId(schoolId);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        if(routeId != null)
            request.setRouteId(routeId);
        if(studentId != null)
            request.setStudentId(studentId);

        List<SwapReportResponse> responses = swipeImageService.getSwipesBySchoolAndRouteAndDateRange(request);
        if(responses.size() > 0)
            return ResponseEntity.ok(responses);

        return new ResponseEntity<>("Data not found for requested criteria", HttpStatus.NO_CONTENT);
    }

}