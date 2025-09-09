package com.smartvehicle.service;

import com.smartvehicle.entity.*;
import com.smartvehicle.payload.request.AssignmentCreateReq;
import com.smartvehicle.payload.response.AssignmentResponse;
import com.smartvehicle.repository.*;
import com.smartvehicle.exception.ApplicationException;
import com.smartvehicle.payload.request.AssignmentUpdateReq;
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
            throw new ApplicationException("Assignment already exists for this school, route and date", HttpStatus.CONFLICT);
        }

        // Close previous active assignment for this driver or attender on or before this date
        if (driver != null) {
            assignmentRepository.findDriverOverlaps(driver.getId(), date, date)
                    .forEach(a -> { a.setEndDate(date.minusDays(1)); assignmentRepository.save(a); });
        }
        if (attender != null) {
            assignmentRepository.findAttenderOverlaps(attender.getId(), date, date)
                    .forEach(a -> { a.setEndDate(date.minusDays(1)); assignmentRepository.save(a); });
        }

        Assignment assignment = new Assignment();

        assignment.setSchool(school);
        assignment.setRoute(route);
        assignment.setDriver(driver);
        assignment.setAttender(attender);
        assignment.setAssignmentDate(date);
        assignment.setEndDate(null);

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

    public java.util.List<AssignmentResponse> getActiveBySchoolAndDate(String schoolId, LocalDate date) {
        java.util.List<Assignment> list = assignmentRepository.findActiveBySchoolAndDate(schoolId, date);
        java.util.List<AssignmentResponse> out = new java.util.ArrayList<>();
        for (Assignment a : list) {
            out.add(toResponse(a));
        }
        return out;
    }

    @Transactional
    public AssignmentResponse update(Long id, AssignmentUpdateReq req) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ApplicationException("Assignment not found", HttpStatus.NOT_FOUND));

        // Determine effective start date for the change: provided date or today
        LocalDate effectiveStart = req.getDate() != null ? req.getDate() : java.time.LocalDate.now();

        if (effectiveStart.isBefore(assignment.getAssignmentDate())) {
            throw new ApplicationException("Effective date cannot be before original assignment start", HttpStatus.BAD_REQUEST);
        }

        // Resolve new route/driver/attender if provided, else use current
        Route newRoute = assignment.getRoute();
        if (req.getRouteSmId() != null && !req.getRouteSmId().isEmpty()) {
            newRoute = routeRepository.findBySmRouteId(req.getRouteSmId())
                    .orElseThrow(() -> new ApplicationException("Route not found", HttpStatus.NOT_FOUND));
        }

        Driver newDriver = assignment.getDriver();
        if (req.getDriverSmId() != null && !req.getDriverSmId().isEmpty()) {
            newDriver = driverRepository.findBySmDriverId(req.getDriverSmId())
                    .orElseThrow(() -> new ApplicationException("Driver not found", HttpStatus.NOT_FOUND));
        }

        Attender newAttender = assignment.getAttender();
        if (req.getAttenderSmId() != null && !req.getAttenderSmId().isEmpty()) {
            newAttender = attenderRepository.findBySmAttenderId(req.getAttenderSmId())
                    .orElseThrow(() -> new ApplicationException("Attender not found", HttpStatus.NOT_FOUND));
        }

        // Validate overlaps at the effective start date
        if (newDriver != null) {
            assignmentRepository.findDriverOverlaps(newDriver.getId(), effectiveStart, effectiveStart)
                    .stream().filter(a -> !a.getId().equals(assignment.getId()))
                    .findAny().ifPresent(a -> { throw new ApplicationException("Driver already assigned for this date", HttpStatus.CONFLICT); });
        }
        if (newAttender != null) {
            assignmentRepository.findAttenderOverlaps(newAttender.getId(), effectiveStart, effectiveStart)
                    .stream().filter(a -> !a.getId().equals(assignment.getId()))
                    .findAny().ifPresent(a -> { throw new ApplicationException("Attender already assigned for this date", HttpStatus.CONFLICT); });
        }

        // If effectiveStart is after current assignment start, we should NOT edit history.
        // Close the current record at effectiveStart - 1, and create a new record from effectiveStart
        if (effectiveStart.isAfter(assignment.getAssignmentDate())) {
            LocalDate newEndForOld = effectiveStart.minusDays(1);
            if (assignment.getEndDate() == null || assignment.getEndDate().isAfter(newEndForOld)) {
                assignment.setEndDate(newEndForOld);
                assignmentRepository.save(assignment);
            }

            Assignment newAssignment = new Assignment();
            newAssignment.setSchool(assignment.getSchool());
            newAssignment.setRoute(newRoute);
            newAssignment.setDriver(newDriver);
            newAssignment.setAttender(newAttender);
            newAssignment.setAssignmentDate(effectiveStart);
            if (req.getEndDate() != null) {
                if (req.getEndDate().isBefore(effectiveStart)) {
                    throw new ApplicationException("endDate cannot be before effective start date", HttpStatus.BAD_REQUEST);
                }
                newAssignment.setEndDate(req.getEndDate());
            } else {
                newAssignment.setEndDate(null);
            }

            Assignment saved = assignmentRepository.save(newAssignment);

            // Sync pointers if the new assignment is active today
            LocalDate today = java.time.LocalDate.now();
            if ((saved.getEndDate() == null || !today.isAfter(saved.getEndDate())) && !today.isBefore(saved.getAssignmentDate())) {
                if (newDriver != null) {
                    if (newDriver.getRoute() == null || !newDriver.getRoute().getId().equals(newRoute.getId())) {
                        newDriver.setRoute(newRoute);
                        driverRepository.save(newDriver);
                    }
                }
                if (newAttender != null) {
                    if (newAttender.getRoute() == null || !newAttender.getRoute().getId().equals(newRoute.getId())) {
                        newAttender.setRoute(newRoute);
                        attenderRepository.save(newAttender);
                    }
                }
            }

            return toResponse(saved);
        }

        // effectiveStart equals current start: update in place
        if (req.getEndDate() != null) {
            if (req.getEndDate().isBefore(assignment.getAssignmentDate())) {
                throw new ApplicationException("endDate cannot be before start date", HttpStatus.BAD_REQUEST);
            }
            assignment.setEndDate(req.getEndDate());
        }
        assignment.setRoute(newRoute);
        assignment.setDriver(newDriver);
        assignment.setAttender(newAttender);
        Assignment saved = assignmentRepository.save(assignment);
        return toResponse(saved);
    }

    @Transactional
    public void deleteById(Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ApplicationException("Assignment not found", HttpStatus.NOT_FOUND));

        // Clear route pointers on driver and attender
        if (assignment.getDriver() != null) {
            Driver driver = assignment.getDriver();
            driver.setRoute(null); // sets smv_driver.route_id to NULL
            driverRepository.save(driver);
        }
        if (assignment.getAttender() != null) {
            Attender attender = assignment.getAttender();
            attender.setRoute(null); // sets smv_attender.route_id to NULL
            attenderRepository.save(attender);
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


