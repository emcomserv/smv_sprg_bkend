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
            return ResponseEntity.badRequest().body("Invalid result filter. Use 'matched', 'mismatched', 'duplicate', or 00..07");
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
    // ... existing code ...

    // Modified: date-only variant with counts in response
    @GetMapping("/ids-by-date")
    public ResponseEntity<?> getDistinctStudentIdsByDate(
            @RequestParam String schoolId,
            @RequestParam(required = false) String routeId,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestParam(name = "result", required = false) String result,
            @RequestParam(name = "studentId", required = false) String studentId) {
        if (isBlank(schoolId)) {
            return ResponseEntity.badRequest().body("schoolId is required");
        }
        if (result != null && !result.isBlank() && !isValidResultFilter(result)) {
            return ResponseEntity.badRequest().body("Invalid result filter. Use 'matched', 'mismatched', 'duplicate', or 00..07");
        }
        List<SwipeStudentDevice> rows;
        if (startDate == null || startDate.isBlank() || endDate == null || endDate.isBlank()) {
            // No dates supplied: return all for the school (optionally route filtered), oldest first
            if (routeId != null && !routeId.isBlank()) {
                // Without start/end, repository signature requires between; fallback to min-max
                LocalDateTime startDt = LocalDateTime.of(1970, 1, 1, 0, 0);
                LocalDateTime endDt = LocalDateTime.now();
                rows = swipeStudentDeviceRepository
                        .findBySchoolIdAndRouteIdAndTimestampBetweenOrderByTimestampAsc(schoolId, routeId, startDt, endDt);
            } else {
                rows = swipeStudentDeviceRepository.findBySchoolIdOrderByTimestampAsc(schoolId);
            }
        } else {
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
            if (routeId != null && !routeId.isBlank()) {
                rows = swipeStudentDeviceRepository
                        .findBySchoolIdAndRouteIdAndTimestampBetweenOrderByTimestampAsc(schoolId, routeId, startDt, endDt);
            } else {
                rows = swipeStudentDeviceRepository
                        .findBySchoolIdAndTimestampBetweenOrderByTimestampAsc(schoolId, startDt, endDt);
            }
        }

        // Apply result filter
        if (result != null && !result.isBlank()) {
            rows = rows.stream().filter(r -> matchesResult(r.getReserv(), result)).collect(Collectors.toList());
        }

        // Apply studentId filter
        if (studentId != null && !studentId.isBlank()) {
            rows = rows.stream().filter(r -> studentId.equals(r.getStudentId())).collect(Collectors.toList());
        }

        // Calculate counts based on reserve field
        int matchedCount = 0;
        int mismatchedCount = 0;
        int duplicateCount = 0;

        for (SwipeStudentDevice swipe : rows) {
            String reserv = swipe.getReserv();
            if (reserv != null && reserv.length() >= 2) {
                String firstTwoChars = reserv.substring(0, 2);
                if ("00".equals(firstTwoChars)) {
                    matchedCount++;
                } else if ("AA".equals(firstTwoChars)) {
                    duplicateCount++;
                } else if (firstTwoChars.matches("0[1-7]")) { // 01, 02, 03, 04, 05, 06, 07
                    mismatchedCount++;
                }
            }
        }

        List<SwipeStudentSummaryDTO> resp = rows.stream().map(s -> new SwipeStudentSummaryDTO(
                s.getSchoolId(), s.getRouteId(), s.getStudentId(), s.getLatitude(), s.getLongitude(), s.getTimestamp(), s.getReserv()
        )).collect(Collectors.toList());
        if (resp.isEmpty()) {
            return ResponseEntity.status(404).body("No swipe records found for the given criteria");
        }

        // Enhanced response with counts
        Map<String, Object> response = new HashMap<>();
        response.put("dateRange", startDate + " to " + endDate);
        response.put("totalSwipes", resp.size());
        response.put("matchedCount", matchedCount);
        response.put("mismatchedCount", mismatchedCount);
        response.put("duplicateCount", duplicateCount);
        response.put("swipes", resp);

        return ResponseEntity.ok(response);
    }



    // Modified: Get evening swipes (after 12:00 PM) with date range support and counts
    @GetMapping("/evening-swipes")
    public ResponseEntity<?> getEveningSwipes(
            @RequestParam String schoolId,
            @RequestParam String routeId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(name = "result", required = false) String result,
            @RequestParam(name = "studentId", required = false) String studentId) {

        if (isBlank(schoolId) || isBlank(routeId) || isBlank(startDate) || isBlank(endDate)) {
            return ResponseEntity.badRequest().body("schoolId, routeId, startDate, and endDate are required");
        }

        if (result != null && !result.isBlank() && !isValidResultFilter(result)) {
            return ResponseEntity.badRequest().body("Invalid result filter. Use 'matched', 'mismatched', 'duplicate', or 00..07");
        }

        try {
            // Parse the date range
            java.time.LocalDate startD = java.time.LocalDate.parse(startDate);
            java.time.LocalDate endD = java.time.LocalDate.parse(endDate);

            if (endD.isBefore(startD)) {
                return ResponseEntity.badRequest().body("endDate must be on/after startDate");
            }

            // Create date-time range for evening swipes
            LocalDateTime startOfFirstDay = startD.atStartOfDay();
            LocalDateTime endOfLastDay = endD.plusDays(1).atStartOfDay().minusNanos(1);

            // Get all swipes in the date range
            List<SwipeStudentDevice> allSwipes = swipeStudentDeviceRepository
                    .findBySchoolIdAndRouteIdAndTimestampBetweenOrderByTimestampAsc(schoolId, routeId, startOfFirstDay, endOfLastDay);

            // Filter for evening swipes (after 12:00 PM)
            List<SwipeStudentDevice> eveningSwipes = allSwipes.stream()
                    .filter(swipe -> {
                        LocalDateTime swipeTime = swipe.getTimestamp();
                        LocalDateTime eveningStart = swipeTime.toLocalDate().atTime(12, 0, 0);
                        return swipeTime.isAfter(eveningStart) || swipeTime.isEqual(eveningStart);
                    })
                    .collect(Collectors.toList());

            // Apply result filter
            if (result != null && !result.isBlank()) {
                eveningSwipes = eveningSwipes.stream()
                        .filter(r -> matchesResult(r.getReserv(), result))
                        .collect(Collectors.toList());
            }

            // Apply studentId filter
            if (studentId != null && !studentId.isBlank()) {
                eveningSwipes = eveningSwipes.stream()
                        .filter(r -> studentId.equals(r.getStudentId()))
                        .collect(Collectors.toList());
            }

            // Calculate counts based on reserve field
            int matchedCount = 0;
            int mismatchedCount = 0;
            int duplicateCount = 0;

            for (SwipeStudentDevice swipe : eveningSwipes) {
                String reserv = swipe.getReserv();
                if (reserv != null && reserv.length() >= 2) {
                    String firstTwoChars = reserv.substring(0, 2);
                    if ("00".equals(firstTwoChars)) {
                        matchedCount++;
                    } else if ("AA".equals(firstTwoChars)) {
                        duplicateCount++;
                    } else if (firstTwoChars.matches("0[1-7]")) { // 01, 02, 03, 04, 05, 06, 07
                        mismatchedCount++;
                    }
                }
            }

            List<SwipeStudentSummaryDTO> resp = eveningSwipes.stream()
                    .map(s -> new SwipeStudentSummaryDTO(
                            s.getSchoolId(), s.getRouteId(), s.getStudentId(),
                            s.getLatitude(), s.getLongitude(), s.getTimestamp(), s.getReserv()
                    ))
                    .collect(Collectors.toList());

            if (resp.isEmpty()) {
                return ResponseEntity.status(404).body("No evening swipe records found for the given date range");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("swipeType", "evening");
            response.put("timeRange", "12:00:00 to 23:59:59");
            response.put("dateRange", startDate + " to " + endDate);
            response.put("totalSwipes", resp.size());
            response.put("matchedCount", matchedCount);
            response.put("mismatchedCount", mismatchedCount);
            response.put("duplicateCount", duplicateCount);
            response.put("swipes", resp);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving evening swipes: " + e.getMessage());
        }
    }
    // ... existing code ...

    // Modified: Get morning swipes (before 10:00 AM) with date range support and counts
    @GetMapping("/morning-swipes")
    public ResponseEntity<?> getMorningSwipes(
            @RequestParam String schoolId,
            @RequestParam String routeId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(name = "result", required = false) String result,
            @RequestParam(name = "studentId", required = false) String studentId) {

        if (isBlank(schoolId) || isBlank(routeId) || isBlank(startDate) || isBlank(endDate)) {
            return ResponseEntity.badRequest().body("schoolId, routeId, startDate, and endDate are required");
        }

        if (result != null && !result.isBlank() && !isValidResultFilter(result)) {
            return ResponseEntity.badRequest().body("Invalid result filter. Use 'matched', 'mismatched', 'duplicate', or 00..07");
        }

        try {
            // Parse the date range
            java.time.LocalDate startD = java.time.LocalDate.parse(startDate);
            java.time.LocalDate endD = java.time.LocalDate.parse(endDate);

            if (endD.isBefore(startD)) {
                return ResponseEntity.badRequest().body("endDate must be on/after startDate");
            }

            // Create date-time range for morning swipes
            LocalDateTime startOfFirstDay = startD.atStartOfDay();
            LocalDateTime endOfLastDay = endD.plusDays(1).atStartOfDay().minusNanos(1);

            // Get all swipes in the date range
            List<SwipeStudentDevice> allSwipes = swipeStudentDeviceRepository
                    .findBySchoolIdAndRouteIdAndTimestampBetweenOrderByTimestampAsc(schoolId, routeId, startOfFirstDay, endOfLastDay);

            // Filter for morning swipes (before 10:00 AM)
            List<SwipeStudentDevice> morningSwipes = allSwipes.stream()
                    .filter(swipe -> {
                        LocalDateTime swipeTime = swipe.getTimestamp();
                        LocalDateTime morningCutoff = swipeTime.toLocalDate().atTime(10, 0, 0);
                        return swipeTime.isBefore(morningCutoff);
                    })
                    .collect(Collectors.toList());

            // Apply result filter
            if (result != null && !result.isBlank()) {
                morningSwipes = morningSwipes.stream()
                        .filter(r -> matchesResult(r.getReserv(), result))
                        .collect(Collectors.toList());
            }

            // Apply studentId filter
            if (studentId != null && !studentId.isBlank()) {
                morningSwipes = morningSwipes.stream()
                        .filter(r -> studentId.equals(r.getStudentId()))
                        .collect(Collectors.toList());
            }

            // Calculate counts based on reserve field
            int matchedCount = 0;
            int mismatchedCount = 0;
            int duplicateCount = 0;

            for (SwipeStudentDevice swipe : morningSwipes) {
                String reserv = swipe.getReserv();
                if (reserv != null && reserv.length() >= 2) {
                    String firstTwoChars = reserv.substring(0, 2);
                    if ("00".equals(firstTwoChars)) {
                        matchedCount++;
                    } else if ("AA".equals(firstTwoChars)) {
                        duplicateCount++;
                    } else if (firstTwoChars.matches("0[1-7]")) { // 01, 02, 03, 04, 05, 06, 07
                        mismatchedCount++;
                    }
                }
            }

            List<SwipeStudentSummaryDTO> resp = morningSwipes.stream()
                    .map(s -> new SwipeStudentSummaryDTO(
                            s.getSchoolId(), s.getRouteId(), s.getStudentId(),
                            s.getLatitude(), s.getLongitude(), s.getTimestamp(), s.getReserv()
                    ))
                    .collect(Collectors.toList());

            if (resp.isEmpty()) {
                return ResponseEntity.status(404).body("No morning swipe records found for the given date range");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("swipeType", "morning");
            response.put("timeRange", "00:00:00 to 10:00:00");
            response.put("dateRange", startDate + " to " + endDate);
            response.put("totalSwipes", resp.size());
            response.put("matchedCount", matchedCount);
            response.put("mismatchedCount", mismatchedCount);
            response.put("duplicateCount", duplicateCount);
            response.put("swipes", resp);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving morning swipes: " + e.getMessage());
        }
    }

// ... existing code ...

// ... existing code ...


    @GetMapping("/by-school")
    public ResponseEntity<?> getSwipesBySchool(
            @RequestParam String schoolId,
            @RequestParam(name = "result", required = false) String result,
            @RequestParam(name = "studentId", required = false) String studentId) {
        if (isBlank(schoolId)) {
            return ResponseEntity.badRequest().body("schoolId is required");
        }
        if (result != null && !result.isBlank() && !isValidResultFilter(result)) {
            return ResponseEntity.badRequest().body("Invalid result filter. Use 'matched', 'mismatched', 'duplicate', or 00..07");
        }

        List<SwipeStudentDevice> rows = swipeStudentDeviceRepository.findBySchoolIdOrderByTimestampAsc(schoolId);

        if (result != null && !result.isBlank()) {
            rows = rows.stream().filter(r -> matchesResult(r.getReserv(), result)).collect(Collectors.toList());
        }

        if (studentId != null && !studentId.isBlank()) {
            rows = rows.stream().filter(r -> studentId.equals(r.getStudentId())).collect(Collectors.toList());
        }

        int matchedCount = 0;
        int mismatchedCount = 0;
        int duplicateCount = 0;
        for (SwipeStudentDevice swipe : rows) {
            String reserv = swipe.getReserv();
            if (reserv != null && reserv.length() >= 2) {
                String firstTwoChars = reserv.substring(0, 2);
                if ("00".equals(firstTwoChars)) {
                    matchedCount++;
                } else if ("AA".equals(firstTwoChars)) {
                    duplicateCount++;
                } else if (firstTwoChars.matches("0[1-7]")) { // 01, 02, 03, 04, 05, 06, 07
                    mismatchedCount++;
                }
            }
        }

        List<SwipeStudentSummaryDTO> resp = rows.stream().map(s -> new SwipeStudentSummaryDTO(
                s.getSchoolId(), s.getRouteId(), s.getStudentId(), s.getLatitude(), s.getLongitude(), s.getTimestamp(), s.getReserv()
        )).collect(Collectors.toList());

        if (resp.isEmpty()) {
            return ResponseEntity.status(404).body("No swipe records found for the given criteria");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalSwipes", resp.size());
        response.put("matchedCount", matchedCount);
        response.put("mismatchedCount", mismatchedCount);
        response.put("duplicateCount", duplicateCount);
        response.put("swipes", resp);

        return ResponseEntity.ok(response);
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
        boolean isDuplicate = code.equals("AA");
        if ("matched".equalsIgnoreCase(desired)) return isMatched;
        if ("mismatched".equalsIgnoreCase(desired)) return !isMatched && !isDuplicate;
        if ("duplicate".equalsIgnoreCase(desired)) return isDuplicate;
        // If specific code requested like "02", match exactly
        if (desired.length() == 2 && desired.chars().allMatch(Character::isDigit)) {
            return code.equals(desired);
        }
        return true;
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private boolean isValidResultFilter(String r) {
        if ("matched".equalsIgnoreCase(r) || "mismatched".equalsIgnoreCase(r) || "duplicate".equalsIgnoreCase(r)) return true;
        return r.length() == 2 && r.chars().allMatch(Character::isDigit) && r.charAt(0) == '0' && r.charAt(1) >= '0' && r.charAt(1) <= '7';
    }
}


