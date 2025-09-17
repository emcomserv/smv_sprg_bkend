package com.smartvehicle.service;

import com.smartvehicle.entity.*;
import com.smartvehicle.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class SmIdGeneratorService {

    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private AttenderRepository attenderRepository;
    @Autowired
    private ParentRepository parentRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private RoutePointRepository routePointRepository;

    public String generateAdminId(String schoolId) {
        return generateNext("AD", schoolId, adminRepository.findBySchool_Id(schoolId).stream().map(Admin::getSmAdminId).toList());
    }

    public String generateDriverId(String schoolId) {
        List<Driver> list = driverRepository.findAllBySchool_Id(schoolId);
        return generateNext("DR", schoolId, list.stream().map(Driver::getSmDriverId).toList());
    }

    public String generateAttenderId(String schoolId) {
        List<Attender> list = attenderRepository.findBySchool_Id(schoolId);
        return generateNext("AT", schoolId, list.stream().map(Attender::getSmAttenderId).toList());
    }

    public String generateParentId(String schoolId) {
        List<Parent> list = parentRepository.findBySchool_Id(schoolId);
        return generateNext("PR", schoolId, list.stream().map(Parent::getSmParentId).toList());
    }

    public String generateStudentId(String schoolId) {
        List<Student> list = studentRepository.findBySchool_Id(schoolId);
        return generateNext("ST", schoolId, list.stream().map(Student::getSmStudentId).toList());
    }

    public String generateRouteId(String schoolId) {
        List<Route> list = routeRepository.findBySchool_Id(schoolId).orElse(java.util.List.of());
        return generateNext("RT", schoolId, list.stream().map(Route::getSmRouteId).toList());
    }

    public String generateRoutePointId(String schoolId) {
        List<RoutePoint> list = routePointRepository.findBySchId(schoolId);
        return generateNext("RP", schoolId, list.stream().map(RoutePoint::getSmRoutePointId).toList());
    }

    private String generateNext(String prefix, String schoolId, List<String> existingForSchool) {
        String schoolSuffix = extractSchoolSuffix(schoolId); // e.g., from SC1F0001 -> 1F0001
        String fixedPart = schoolSuffix.substring(0, schoolSuffix.length() - 4); // e.g., 1F

        int nextSeq = existingForSchool.stream()
                .filter(id -> id != null && id.startsWith(prefix + fixedPart))
                .map(this::extractSeq)
                .filter(java.util.Objects::nonNull)
                .max(Comparator.naturalOrder())
                .map(max -> max + 1)
                .orElse(1);

        return prefix + fixedPart + String.format("%04d", nextSeq);
    }

    private String extractSchoolSuffix(String schoolId) {
        if (schoolId == null || schoolId.length() < 7) {
            throw new IllegalArgumentException("Invalid schoolId format: " + schoolId);
        }
        // Expecting format like SC1F0001 -> return 1F0001
        return schoolId.substring(2);
    }

    private Integer extractSeq(String smId) {
        if (smId.length() < 4) return null;
        String last4 = smId.substring(smId.length() - 4);
        try {
            return Integer.parseInt(last4);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}


