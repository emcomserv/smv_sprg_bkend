package com.smartvehicle.service;

import com.smartvehicle.entity.*;
import com.smartvehicle.payload.request.AssignmentCreateReq;
import com.smartvehicle.payload.response.AssignmentResponse;
import com.smartvehicle.repository.*;
import com.smartvehicle.exception.ApplicationException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.http.HttpStatus;

@Service
public class AssignmentService {

    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private SchoolRepository schoolRepository;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private AttenderRepository attenderRepository;

    @Transactional
    public AssignmentResponse createOrUpdate(AssignmentCreateReq req) {
        School school = schoolRepository.findById(req.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found: " + req.getSchoolId()));

        Route route = routeRepository.findBySmRouteId(req.getRouteSmId())
                .orElseThrow(() -> new RuntimeException("Route not found: " + req.getRouteSmId()));

        Driver driver = null;
        if (req.getDriverSmId() != null && !req.getDriverSmId().isEmpty()) {
            driver = driverRepository.findBySmDriverId(req.getDriverSmId())
                    .orElseThrow(() -> new RuntimeException("Driver not found: " + req.getDriverSmId()));
            // Block any assignment for this driver on the same date (regardless of route)
            assignmentRepository.findByDriver_IdAndAssignmentDate(driver.getId(), req.getDate())
                    .ifPresent(existing -> { throw new ApplicationException("Driver already assigned for this date", HttpStatus.CONFLICT); });
        }

        Attender attender = null;
        if (req.getAttenderSmId() != null && !req.getAttenderSmId().isEmpty()) {
            attender = attenderRepository.findBySmAttenderId(req.getAttenderSmId())
                    .orElseThrow(() -> new RuntimeException("Attender not found: " + req.getAttenderSmId()));
            // Block any assignment for this attender on the same date (regardless of route)
            assignmentRepository.findByAttender_IdAndAssignmentDate(attender.getId(), req.getDate())
                    .ifPresent(existing -> { throw new ApplicationException("Attender already assigned for this date", HttpStatus.CONFLICT); });
        }

        LocalDate date = req.getDate();
        Optional<Assignment> existingOpt = assignmentRepository.findBySchoolIdAndSmRouteIdAndDate(school.getId(), route.getSmRouteId(), date);
        if (existingOpt.isPresent()) {
            // Do not allow creating/updating for same school+route+date at all
            throw new ApplicationException("Assignment already exists for this school, route and date", HttpStatus.CONFLICT);
        }
        Assignment assignment = existingOpt.orElseGet(Assignment::new);

        assignment.setSchool(school);
        assignment.setRoute(route);
        assignment.setDriver(driver);
        assignment.setAttender(attender);
        assignment.setAssignmentDate(date);

        assignment = assignmentRepository.save(assignment);

        // Ensure driver and attender tables reflect the new route assignment
        if (driver != null) {
            if (driver.getRoute() == null || !driver.getRoute().getId().equals(route.getId())) {
                driver.setRoute(route);
                driverRepository.save(driver);
            }
        }
        if (attender != null) {
            if (attender.getRoute() == null || !attender.getRoute().getId().equals(route.getId())) {
                attender.setRoute(route);
                attenderRepository.save(attender);
            }
        }

        return toResponse(assignment);
    }

    public AssignmentResponse getBySchoolRouteDate(String schoolId, String routeSmId, LocalDate date) {
        Assignment assignment = assignmentRepository.findBySchoolIdAndSmRouteIdAndDate(schoolId, routeSmId, date)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        return toResponse(assignment);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!assignmentRepository.existsById(id)) {
            throw new ApplicationException("Assignment not found", HttpStatus.NOT_FOUND);
        }
        assignmentRepository.deleteById(id);
    }

    private AssignmentResponse toResponse(Assignment a) {
        return new AssignmentResponse(
                a.getId(),
                a.getSchool() != null ? a.getSchool().getId() : null,
                a.getRoute() != null ? a.getRoute().getSmRouteId() : null,
                a.getDriver() != null ? a.getDriver().getSmDriverId() : null,
                a.getAttender() != null ? a.getAttender().getSmAttenderId() : null,
                a.getAssignmentDate()
        );
    }
}


