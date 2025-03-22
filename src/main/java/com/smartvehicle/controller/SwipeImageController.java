package com.smartvehicle.controller;

import com.smartvehicle.entity.SwipeImage;
import com.smartvehicle.service.FTPClientService;
import com.smartvehicle.service.SwipeImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/swipeimage")
public class SwipeImageController {
    @Autowired
    private SwipeImageService swipeImageService;

    @GetMapping("/studentId")
    public ResponseEntity<List<SwipeImage>> getSwipeByStudentId(@RequestParam String studentId) {
        return ResponseEntity.ok(swipeImageService.getSwipesByStudentId(studentId));
    }

    @Autowired
    private FTPClientService ftpService;
    @GetMapping("/byName")
    public ResponseEntity<byte[]> getImageByName(@RequestParam String imageName) {
        try {
            byte[] imageData = ftpService.readFile(imageName);
            return new ResponseEntity<>(imageData, HttpStatus.OK);
        } catch (IOException e) {
            log.debug(e.getMessage());
            throw new RuntimeException("Failed to read image: " + imageName, e);
        }
    }
}