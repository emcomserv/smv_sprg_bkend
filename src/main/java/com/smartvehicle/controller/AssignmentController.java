package com.smartvehicle.controller;

import com.smartvehicle.payload.request.AssignmentCreateReq;
import com.smartvehicle.payload.request.AssignmentUpdateReq;
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
    public ResponseEntity<java.util.List<AssignmentResponse>> get(
            @RequestParam String schoolId,
            @RequestParam String routeSmId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(assignmentService.getBySchoolRouteDate(schoolId, routeSmId, date));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assignmentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActive(
            @RequestParam String schoolId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(assignmentService.getActiveBySchoolAndDate(schoolId, date));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssignmentResponse> update(@PathVariable Long id, @RequestBody AssignmentUpdateReq req) {
        return ResponseEntity.ok(assignmentService.update(id, req));
    }
}


