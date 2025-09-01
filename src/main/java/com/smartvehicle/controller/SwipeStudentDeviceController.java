package com.smartvehicle.controller;

import com.smartvehicle.entity.SwipeStudentDevice;
import com.smartvehicle.entity.RouteSchoolStudentMapping;
import com.smartvehicle.entity.RoutePoint;
import com.smartvehicle.repository.SwipeStudentDeviceRepository;
import com.smartvehicle.repository.RouteSchlStudentMappingRepo;
import com.smartvehicle.repository.RoutePointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import com.smartvehicle.payload.response.SwipeStudentSummaryDTO;

@RestController
@RequestMapping("/api/v1/swipe-students")
public class SwipeStudentDeviceController {

    @Autowired
    private SwipeStudentDeviceRepository swipeStudentDeviceRepository;
    
    @Autowired
    private RouteSchlStudentMappingRepo routeSchlStudentMappingRepo;
    
    @Autowired
    private RoutePointRepository routePointRepository;

    @GetMapping("/ids")
    public ResponseEntity<?> getDistinctStudentIds(
            @RequestParam String schoolId,
            @RequestParam String routeId,
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(name = "result", required = false) String result,
            @RequestParam(name = "studentId", required = false) String studentId) {
        if (isBlank(schoolId) || isBlank(routeId) || isBlank(start) || isBlank(end)) {
            return ResponseEntity.badRequest().body("schoolId, routeId, start, end are required");
        }
        LocalDateTime startDt;
        LocalDateTime endDt;
        try {
            startDt = LocalDateTime.parse(start);
            endDt = LocalDateTime.parse(end);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid datetime format. Use ISO yyyy-MM-ddTHH:mm:ss");
        }
        if (endDt.isBefore(startDt)) {
            return ResponseEntity.badRequest().body("end must be after start");
        }
        if (result != null && !result.isBlank() && !isValidResultFilter(result)) {
            return ResponseEntity.badRequest().body("Invalid result filter. Use 'matched', 'mismatched', or 00..07");
        }
        // Return all rows in range (including duplicate studentIds)
        List<SwipeStudentDevice> rows = swipeStudentDeviceRepository
                .findBySchoolIdAndRouteIdAndTimestampBetweenOrderByTimestampAsc(schoolId, routeId, startDt, endDt);
        
        // Apply result filter
        if (result != null && !result.isBlank()) {
            rows = rows.stream().filter(r -> matchesResult(r.getReserv(), result)).collect(Collectors.toList());
        }
        
        // Apply studentId filter
        if (studentId != null && !studentId.isBlank()) {
            rows = rows.stream().filter(r -> studentId.equals(r.getStudentId())).collect(Collectors.toList());
        }
        
        List<SwipeStudentSummaryDTO> resp = rows.stream().map(s -> new SwipeStudentSummaryDTO(
                s.getSchoolId(), s.getRouteId(), s.getStudentId(), s.getLatitude(), s.getLongitude(), s.getTimestamp(), s.getReserv()
        )).collect(Collectors.toList());
        if (resp.isEmpty()) {
            return ResponseEntity.status(404).body("No swipe records found for the given criteria");
        }
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/latest")
    public ResponseEntity<List<SwipeStudentSummaryDTO>> getLatestSwipesPerStudent(
            @RequestParam String schoolId,
            @RequestParam String routeId,
            @RequestParam String start,
            @RequestParam String end) {
        LocalDateTime startDt = LocalDateTime.parse(start);
        LocalDateTime endDt = LocalDateTime.parse(end);
        List<SwipeStudentDevice> rows = swipeStudentDeviceRepository
                .findLatestSwipesPerStudentBySchoolRouteAndDateRange(schoolId, routeId, startDt, endDt);
        List<SwipeStudentSummaryDTO> resp = rows.stream().map(s -> new SwipeStudentSummaryDTO(
                s.getSchoolId(), s.getRouteId(), s.getStudentId(), s.getLatitude(), s.getLongitude(), s.getTimestamp(), s.getReserv()
        )).collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }

    // New: date-only variant (no time in query). Same response format as /ids
    @GetMapping("/ids-by-date")
    public ResponseEntity<?> getDistinctStudentIdsByDate(
            @RequestParam String schoolId,
            @RequestParam String routeId,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam(name = "result", required = false) String result,
            @RequestParam(name = "studentId", required = false) String studentId) {
        if (isBlank(schoolId) || isBlank(routeId) || isBlank(startDate) || isBlank(endDate)) {
            return ResponseEntity.badRequest().body("schoolId, routeId, startDate, endDate are required");
        }
        if (result != null && !result.isBlank() && !isValidResultFilter(result)) {
            return ResponseEntity.badRequest().body("Invalid result filter. Use 'matched', 'mismatched', or 00..07");
        }
        java.time.LocalDate startD;
        java.time.LocalDate endD;
        try {
            startD = java.time.LocalDate.parse(startDate);
            endD = java.time.LocalDate.parse(endDate);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid date format. Use yyyy-MM-dd");
        }
        LocalDateTime startDt = startD.atStartOfDay();
        LocalDateTime endDt = endD.plusDays(1).atStartOfDay().minusNanos(1);
        if (endDt.isBefore(startDt)) {
            return ResponseEntity.badRequest().body("endDate must be on/after startDate");
        }
        List<SwipeStudentDevice> rows = swipeStudentDeviceRepository
                .findBySchoolIdAndRouteIdAndTimestampBetweenOrderByTimestampAsc(schoolId, routeId, startDt, endDt);
        
        // Apply result filter
        if (result != null && !result.isBlank()) {
            rows = rows.stream().filter(r -> matchesResult(r.getReserv(), result)).collect(Collectors.toList());
        }
        
        // Apply studentId filter
        if (studentId != null && !studentId.isBlank()) {
            rows = rows.stream().filter(r -> studentId.equals(r.getStudentId())).collect(Collectors.toList());
        }
        
        List<SwipeStudentSummaryDTO> resp = rows.stream().map(s -> new SwipeStudentSummaryDTO(
                s.getSchoolId(), s.getRouteId(), s.getStudentId(), s.getLatitude(), s.getLongitude(), s.getTimestamp(), s.getReserv()
        )).collect(Collectors.toList());
        if (resp.isEmpty()) {
            return ResponseEntity.status(404).body("No swipe records found for the given criteria");
        }
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/generate-swipe-records")
    public ResponseEntity<?> generateSwipeRecords(
            @RequestParam String schoolId,
            @RequestParam String date,
            @RequestParam(name = "time", required = false) String time,
            @RequestParam(name = "timeWindow", required = false, defaultValue = "30") Integer timeWindow) {
        
        if (isBlank(schoolId) || isBlank(date)) {
            return ResponseEntity.badRequest().body("schoolId and date are required");
        }
        
        try {
            // Parse the date
            java.time.LocalDate targetDate = java.time.LocalDate.parse(date);
            LocalDateTime startOfDay = targetDate.atStartOfDay();
            LocalDateTime endOfDay = targetDate.plusDays(1).atStartOfDay().minusNanos(1);
            
            // Get all route-student mappings for this school
            List<RouteSchoolStudentMapping> mappings = routeSchlStudentMappingRepo.findAll();
            List<RouteSchoolStudentMapping> schoolMappings = mappings.stream()
                    .filter(m -> m.getSchool().getId().equals(schoolId))
                    .collect(Collectors.toList());
            
            if (schoolMappings.isEmpty()) {
                return ResponseEntity.status(404).body("No routes found for school: " + schoolId);
            }
            
            int totalRecords = 0;
            int successRecords = 0;
            
            for (RouteSchoolStudentMapping mapping : schoolMappings) {
                String routeId = mapping.getRoute().getSmRouteId();
                String studentId = mapping.getSmStudentId();
                
                // Get route points for this route to pick coordinates
                List<RoutePoint> routePoints = routePointRepository.findByRoute_SmRouteId(routeId);
                
                if (!routePoints.isEmpty()) {
                    // Pick a random route point for coordinates
                    RoutePoint randomPoint = routePoints.get((int) (Math.random() * routePoints.size()));
                    
                    // Generate random reserve value
                    String reserve = generateRandomReserve();
                    
                    // Use manually provided time with random window or generate random time
                    LocalDateTime recordTimestamp;
                    if (time != null && !time.isBlank()) {
                        try {
                            // Parse the time in HH:mm:ss format
                            java.time.LocalTime parsedTime = java.time.LocalTime.parse(time);
                            LocalDateTime baseTime = startOfDay.plusHours(parsedTime.getHour())
                                                              .plusMinutes(parsedTime.getMinute())
                                                              .plusSeconds(parsedTime.getSecond());
                            
                            // Generate random time within the specified window (default 30 minutes)
                            // Window goes from (baseTime - timeWindow/2) to (baseTime + timeWindow/2)
                            int windowMinutes = timeWindow / 2; // Half window on each side
                            long randomMinutes = (long) (Math.random() * timeWindow) - windowMinutes;
                            recordTimestamp = baseTime.plusMinutes(randomMinutes);
                            
                        } catch (Exception e) {
                            return ResponseEntity.badRequest().body("Invalid time format. Use HH:mm:ss (e.g., 18:31:12)");
                        }
                    } else {
                        // Generate random timestamp if no time provided
                        recordTimestamp = startOfDay.plusMinutes((long) (Math.random() * 1440));
                    }
                    
                    // Generate image name: studentid_20250816-183112.jpg
                    String imageName = generateImageName(studentId, recordTimestamp);
                    
                    // Create swipe record
                    SwipeStudentDevice swipeRecord = new SwipeStudentDevice();
                    swipeRecord.setSchoolId(schoolId);
                    swipeRecord.setRouteId(routeId);
                    swipeRecord.setStudentId(studentId);
                    swipeRecord.setLatitude(randomPoint.getLatitude());
                    swipeRecord.setLongitude(randomPoint.getLongitude());
                    swipeRecord.setTimestamp(recordTimestamp);
                    swipeRecord.setImageName(imageName);
                    swipeRecord.setReserv(reserve);
                    
                    try {
                        swipeStudentDeviceRepository.save(swipeRecord);
                        successRecords++;
                    } catch (Exception e) {
                        System.err.println("Failed to save swipe record for student: " + studentId + ", Error: " + e.getMessage());
                    }
                }
                totalRecords++;
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Swipe records generation completed");
            response.put("schoolId", schoolId);
            response.put("date", date);
            if (time != null && !time.isBlank()) {
                response.put("baseTime", time);
                response.put("timeWindow", timeWindow + " minutes");
            }
            response.put("totalStudents", totalRecords);
            response.put("successfullyCreated", successRecords);
            response.put("failed", totalRecords - successRecords);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error generating swipe records: " + e.getMessage());
        }
    }

    private String generateImageName(String studentId, LocalDateTime timestamp) {
        // Format: studentid_20250816-183112.jpg
        String dateStr = timestamp.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timeStr = timestamp.format(java.time.format.DateTimeFormatter.ofPattern("HHmmss"));
        return studentId + "_" + dateStr + "-" + timeStr + ".jpg";
    }

    private String generateRandomReserve() {
        // Generate random reserve value with 85% probability for 00 (matched)
        double random = Math.random();
        
        if (random < 0.85) {
            // 85% chance for matched (00 + percentage 70-90 + AAA)
            int percentage = (int) (Math.random() * 21) + 70; // 70 to 90
            return String.format("00%02dAAA", percentage); // e.g., "0078AAA", "0085AAA", "0090AAA"
        } else {
            // 15% chance for mismatched
            int randomCode = (int) (Math.random() * 7) + 1; // 1 to 7
            String code = String.format("%02d", randomCode); // 01, 02, 03, etc.
            
            if (randomCode == 1 || randomCode == 3 || randomCode == 4) {
                // 01, 03, 04: percentage less than 65
                int percentage = (int) (Math.random() * 65); // 0 to 64
                return String.format("%s%02dAAA", code, percentage); // e.g., "0160AAA", "0355AAA", "0445AAA"
            } else {
                // 02, 05, 06, 07: percentage is 00
                return code + "00AAA"; // e.g., "0200AAA", "0500AAA", "0600AAA", "0700AAA"
            }
        }
    }

    private boolean matchesResult(String reserv, String desired) {
        if (reserv == null || reserv.length() < 2) return false;
        String code = reserv.substring(0, 2);
        boolean isMatched = code.equals("00");
        if ("matched".equalsIgnoreCase(desired)) return isMatched;
        if ("mismatched".equalsIgnoreCase(desired)) return !isMatched;
        // If specific code requested like "02", match exactly
        if (desired.length() == 2 && desired.chars().allMatch(Character::isDigit)) {
            return code.equals(desired);
        }
        return true;
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private boolean isValidResultFilter(String r) {
        if ("matched".equalsIgnoreCase(r) || "mismatched".equalsIgnoreCase(r)) return true;
        return r.length() == 2 && r.chars().allMatch(Character::isDigit) && r.charAt(0) == '0' && r.charAt(1) >= '0' && r.charAt(1) <= '7';
    }
}


