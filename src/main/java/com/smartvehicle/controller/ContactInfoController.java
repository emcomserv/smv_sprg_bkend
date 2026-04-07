package com.smartvehicle.controller;

import com.smartvehicle.payload.request.ContactInfoCreateReq;
import com.smartvehicle.payload.response.ContactInfoResponse;
import com.smartvehicle.service.ContactInfoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/contact")
public class ContactInfoController {

    @Autowired
    private ContactInfoService contactInfoService;

    @PostMapping
    public ResponseEntity<ContactInfoResponse> create(@Valid @RequestBody ContactInfoCreateReq request) {
        ContactInfoResponse response = contactInfoService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}


