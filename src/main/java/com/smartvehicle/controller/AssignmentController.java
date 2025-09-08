package com.smartvehicle.controller;

import com.smartvehicle.payload.request.AssignmentCreateReq;
import com.smartvehicle.payload.response.AssignmentResponse;
import com.smartvehicle.service.AssignmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/assignments")
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @PostMapping
    public ResponseEntity<AssignmentResponse> create(@Valid @RequestBody AssignmentCreateReq req) {
        AssignmentResponse response = assignmentService.createOrUpdate(req);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<AssignmentResponse> get(
            @RequestParam String schoolId,
            @RequestParam String routeSmId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        AssignmentResponse response = assignmentService.getBySchoolRouteDate(schoolId, routeSmId, date);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assignmentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}


