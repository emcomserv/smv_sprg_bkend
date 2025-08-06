package com.smartvehicle.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartvehicle.entity.*;
import com.smartvehicle.mapper.StudentMapper;
import com.smartvehicle.payload.request.StudentChangeRouteReq;
import com.smartvehicle.payload.request.StudentPickupPointReq;
import com.smartvehicle.payload.request.StudentSignupReq;
import com.smartvehicle.payload.response.StudentResponseDTO;
import com.smartvehicle.payload.response.StudentResponseLtDTO;
import com.smartvehicle.repository.*;
import com.smartvehicle.service.RouteService;
import com.smartvehicle.service.StudentService;
import com.smartvehicle.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/student")
public class StudentController {
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private SwipeReportMobileRepository swipeReportMobileRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private SchoolRepository schoolRepository;
    @Autowired
    private RouteService routeService;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private StudentService studentService;
    @Autowired
    private ParentRepository parentRepository;
    @Autowired
    private RouteSchlStudentMappingRepo routeSchlStudentMappingRepo;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private SimpUserRegistry simpUserRegistry;

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> registerUser(@Valid @RequestBody StudentSignupReq request) throws Exception {
        School school = schoolRepository.findById(request.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Error: School not found with id  " + request.getSchoolId()));
        Parent parent = parentRepository.findById(request.getParentId())
                .orElseThrow(() -> new RuntimeException("Error: Parent not found with id  " + request.getParentId()));
        Student student = new Student();
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setLongitude(request.getLongitude());
        student.setAge(request.getAge());
        student.setGender(request.getGender());
        student.setLatitude(request.getLatitude());
        student.setSchool(school);
        student.setParent(parent);
        student.setStatus(true);
        student.setSmStudentId(request.getSmStudentId());
        student.setDeviceId(request.getDeviceId());
        if (request.getRouteId() != null) {
            Route route = routeService.getRouteById(request.getRouteId());
            student.setRoute(route);
            if (request.getRoutePointId() != null) {
                RoutePoint routePoint = routeService.getRoutePointById(request.getRoutePointId());
                student.setRoutePoint(routePoint);
            }
        }

        Student student1 = studentRepository.save(student);

        // Saving data to mapping table
        RouteSchoolStudentMapping routeSchoolStudentMapping = new RouteSchoolStudentMapping();
        routeSchoolStudentMapping.setRoute(student1.getRoute());
        routeSchoolStudentMapping.setSmStudentId(student1.getSmStudentId());
        routeSchoolStudentMapping.setSchool(student1.getSchool());
        routeSchoolStudentMapping.setProvId(student1.getSchool().getProvId());

        routeSchlStudentMappingRepo.save(routeSchoolStudentMapping);

        StudentResponseDTO studentResponseDTO = studentMapper.toResponseDTO(student1);
        return new ResponseEntity<StudentResponseDTO>(studentResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<StudentResponseDTO>> getAllStudents() {
        List<Student> students = studentRepository.findAll();
        List<StudentResponseDTO> studentResponseDTOS = studentMapper.toResponseDTO(students);
        return ResponseEntity.ok(studentResponseDTOS);
    }

    @GetMapping("/route/{id}")
    public ResponseEntity<List<StudentResponseDTO>> getByRouteId(@PathVariable Long id) {
        List<Student> students = studentRepository.findAllByRoute_Id(id);
        List<StudentResponseDTO> studentResponseDTOS = studentMapper.toResponseDTO(students);
        return ResponseEntity.ok(studentResponseDTOS);
    }

    @GetMapping("/route-point/{id}")
    public ResponseEntity<List<StudentResponseLtDTO>> getAllStudentByRoutePointId(@PathVariable Long id) {
        List<Student> students = studentRepository.findAllByRoutePoint_Id(id);
        List<StudentResponseLtDTO> studentResponseDTOS = studentMapper.toResponseLtDTO(students);
        return ResponseEntity.ok(studentResponseDTOS);
    }

    @PostMapping("/{id}/change-routepoint")
    public ResponseEntity<StudentResponseDTO> changeRoutePoint(@PathVariable Long id,
                                                               @RequestBody StudentPickupPointReq req) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Student not found with id  " + id));
        student.setLatitude(req.getLatitude());
        student.setLongitude(req.getLongitude());
        studentRepository.save(student);
        StudentResponseDTO studentResponseDTO = studentMapper.toResponseDTO(student);
        return ResponseEntity.ok(studentResponseDTO);
    }

    @PostMapping("/{studentId}/change-route")
    public ResponseEntity<StudentResponseDTO> changeRoutePoint(@PathVariable Long studentId,
                                                               @RequestBody StudentChangeRouteReq req) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Error: Student not found with id  " + studentId));
        Route route = routeService.getRouteById(req.getRouteId());
        student.setRoute(route);
        studentRepository.save(student);
        StudentResponseDTO studentResponseDTO = studentMapper.toResponseDTO(student);
        return ResponseEntity.ok(studentResponseDTO);
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateAndStoreStudent(
            @RequestParam @NotBlank String input,
            @RequestParam @NotBlank String latitude,
            @RequestParam @NotBlank String longitude,
            @RequestParam @NotBlank String imageName) {

        Map<String, Object> response = new HashMap<>();

        // Validate input format
        if (!input.matches("^[A-Za-z0-9]+-[A-Za-z0-9]+-[A-Za-z0-9]+$")) {
            response.put("status", "Invalid");
            response.put("message", "Invalid input format. Expected format: SCHOOLID-ROUTEID-CITYCODESTUDENTID");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        // Parse and validate coordinates
        double lat, lon;
        try {
            lat = Double.parseDouble(latitude);
            lon = Double.parseDouble(longitude);
            if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                response.put("status", "Invalid");
                response.put("message", "Invalid coordinate values");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (NumberFormatException e) {
            response.put("status", "Invalid");
            response.put("message", "Invalid coordinate format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Validate image name format
        if (imageName.isEmpty()) {
            response.put("status", "Invalid");
            response.put("message", "Invalid image name format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Extract IDs
        String[] inputParts = input.split("-");
        String schoolId = inputParts[0].toUpperCase();
        String routeId = inputParts[1].toUpperCase();
        String studentIdWithCityCode = inputParts[2].toUpperCase();

        // Validate routeId is not empty or malformed
        if (routeId.isEmpty()) {
            response.put("status", "Invalid");
            response.put("message", "Invalid or missing route ID");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Extract sm_student_id and cityCode
        if (studentIdWithCityCode.length() <= 3) {
            response.put("status", "Invalid");
            response.put("message", "Invalid student ID format: Too short to contain a city code");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        String cityCode = studentIdWithCityCode.substring(0, 3);
        String studentId = studentIdWithCityCode.substring(3);

        // Validate ID formats
        if (schoolId.isEmpty()) {
            response.put("status", "Invalid");
            response.put("message", "Invalid school or student ID format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Validate school exists
        boolean schoolExists = schoolRepository.existsById(schoolId);
        System.out.println("School exists for ID " + schoolId + ": " + schoolExists);
        if (!schoolExists) {
            response.put("status", "Invalid");
            response.put("message", "School not found with ID: " + schoolId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Validate student exists and belongs to the school
        Student student = studentRepository.findBySmStudentId(studentId)
                .orElse(null);
        System.out.println("Student found for sm_student_id " + studentId + ": " + (student != null));
        if (student == null) {
            response.put("status", "Invalid");
            response.put("message", "Student not found with ID: " + studentId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        String studentSchoolId = student.getSchool() != null ? student.getSchool().getId() : "null";
        System.out.println("Student's school ID for " + studentId + ": " + studentSchoolId);
        if (!schoolId.equals(studentSchoolId)) {
            response.put("status", "Invalid");
            response.put("message", "Student does not belong to the specified school");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Validate route exists and belongs to the school
        Route route = routeRepository.findBySmRouteId(routeId)
                .orElse(null);
        System.out.println("Route found for sm_route_id " + routeId + ": " + (route != null));
        if (route == null) {
            response.put("status", "Invalid");
            response.put("message", "Route not found with ID: " + routeId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        String routeSchoolId = route.getSchool() != null ? route.getSchool().getId() : "null";
        System.out.println("Route's school ID for " + routeId + ": " + routeSchoolId);
        if (!schoolId.equals(routeSchoolId)) {
            response.put("status", "Invalid");
            response.put("message", "Route does not belong to the specified school");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Validate cityCode matches the route's cityCode
        String routeCityCode = route.getCityCode();
        System.out.println("Route's city code for " + routeId + ": " + routeCityCode + ", Input city code: " + cityCode);
        if (routeCityCode == null || !cityCode.equals(routeCityCode)) {
            response.put("status", "Invalid");
            response.put("message", "City code " + cityCode + " does not match the route's city code");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Construct the full FTP file path (including filename)
        String ftpPath = String.format("/upload/%s/%s/%s", schoolId, studentId, imageName);

        // Prepare payload for Python Analyzer
        Map<String, String> payload = new HashMap<>();
        payload.put("ftpPath", ftpPath);
        payload.put("schoolId", schoolId);
        payload.put("studentId", studentId);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            response.put("status", "Error");
            response.put("message", "Failed to serialize payload for analysis");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        // Send payload to Python Analyzer via socket communication
        String analyzerResponse;
        try (Socket socket = new Socket("68.178.203.99",5004);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(jsonPayload);
            analyzerResponse = in.readLine();
            if (analyzerResponse == null) {
                response.put("status", "Error");
                response.put("message", "No response from Python Analyzer");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (IOException e) {
            response.put("status", "Error");
            response.put("message", "Failed to communicate with Python Analyzer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        // Initialize response variables
        double confidence = 0.0;
        String schoolIdPython = schoolId;
        String studentIdPython = studentId;
        String imageId = "";
        String message = "Error";

        try {
            System.out.println("Analyzer Response: " + analyzerResponse);
            String[] parts = analyzerResponse.split(",");
            String code = parts[0].trim();

            // Parse key-value pairs
            for (int i = 1; i < parts.length; i++) {
                String part = parts[i].trim();
                if (part.contains("=")) {
                    String[] keyValue = part.split("=", 2);
                    String key = keyValue[0].trim();
                    String value = keyValue.length > 1 ? keyValue[1].trim() : "";
                    if (key.equals("school_id")) {
                        schoolIdPython = value;
                    } else if (key.equals("student_id")) {
                        studentIdPython = value;
                    } else if (key.equals("image_base64") || key.equals("image_bace64")) {
                        imageId = value;
                    }
                } else if (part.endsWith("%")) {
                    try {
                        confidence = Double.parseDouble(part.replace("%", "").trim());
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid confidence format: " + part);
                    }
                }
            }

            // Set message based on code
            switch (code) {
                case "00":
                    message = confidence > 70.0 ? "00" : "01";
                    break;
                case "01":
                    message = "01";
                    break;
                case "02":
                    message = "02";
                    break;
                case "03":
                    message = "03";
                    break;
                case "04/Clear":
                    message = "04";
                    break;
                case "05":
                    message = "05";
                    break;
                default:
                    message = "Error";
            }

        } catch (Exception e) {
            message = "Error";
            System.out.println("Error parsing analyzer response: " + e.getMessage());
        }

        // Build response
        response.put("message", message);
        response.put("schoolId", schoolIdPython);
        response.put("studentId", studentIdPython);
        response.put("base64", imageId);
        response.put("confidence", confidence);
        if (message.equals("00")) {
            Student studentForFeeStatus = studentRepository.findBySmStudentId(studentIdPython).orElse(null);
            if (studentForFeeStatus != null) {
                Boolean feeStatus = studentForFeeStatus.getStatus();
                response.put("feeStatus", (feeStatus != null && !feeStatus) ? "Fee Pending" : (feeStatus != null && feeStatus) ? "Fee Paid" : "Unknown");
            } else {
                response.put("feeStatus", "Unknown");
            }
        }

        // Send to WebSocket
        messagingTemplate.convertAndSend("/topic/validation", response);

        // Save swipe report if validation is successful
        if (message.equals("00")) {
            SwipeReportMobile swipeReportMobile = new SwipeReportMobile();
            swipeReportMobile.setRouteId(routeId);
            swipeReportMobile.setSchoolId(schoolId);
            swipeReportMobile.setStudentId(studentId);
            swipeReportMobile.setLatitude(latitude);
            swipeReportMobile.setLongitude(longitude);
            swipeReportMobile.setImageName(imageName);
            swipeReportMobileRepository.save(swipeReportMobile);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/school")
    public ResponseEntity<List<StudentResponseLtDTO>> getStudentsBySchoolId(@RequestParam String schoolId) {
        List<Student> students = studentService.findStudentsBySchoolId(schoolId);
        List<StudentResponseLtDTO> studentResponseDTOS = studentMapper.toResponseLtDTO(students);
        if (students.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(studentResponseDTOS);
    }
    @PutMapping("/update")
    public ResponseEntity<StudentResponseDTO> updateStudentBySmStudentId(
            @RequestParam String smStudentId,
            @RequestBody StudentSignupReq request) {
        Student student = studentRepository.findBySmStudentId(smStudentId)
                .orElseThrow(() -> new RuntimeException("Student not found with sm_student_id: " + smStudentId));

        if (request.getFirstName() != null) {
            student.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            student.setLastName(request.getLastName());
        }
        if (request.getAge() != null) {
            student.setAge(request.getAge());
        }
        if (request.getGender() != null) {
            student.setGender(request.getGender());
        }
        if (request.getLongitude() != null) {
            student.setLongitude(request.getLongitude());
        }
        if (request.getLatitude() != null) {
            student.setLatitude(request.getLatitude());
        }
        if (request.getStatus() != null) {
            student.setStatus(request.getStatus());
        }
        if (request.getSchoolId() != null) {
            School school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("School not found with ID: " + request.getSchoolId()));
            student.setSchool(school);
        }
        if (request.getParentId() != null) {
            Parent parent = parentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent not found with ID: " + request.getParentId()));
            student.setParent(parent);
        }
        if (request.getRouteId() != null) {
            Route route = routeService.getRouteById(request.getRouteId());
            student.setRoute(route);
            if (request.getRoutePointId() != null) {
                RoutePoint routePoint = routeService.getRoutePointById(request.getRoutePointId());
                student.setRoutePoint(routePoint);
            }
        }

        Student updatedStudent = studentRepository.save(student);
        StudentResponseDTO responseDTO = studentMapper.toResponseDTO(updatedStudent);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/test-websocket")
    public ResponseEntity<String> testWebSocket() {
        Map<String, Object> testResponse = new HashMap<>();
        testResponse.put("status", "Test");
        testResponse.put("message", "This is a test message");
        testResponse.put("isValid", true);
        testResponse.put("schoolId", "TEST_SCHOOL");
        testResponse.put("studentId", "TEST_STUDENT");
        messagingTemplate.convertAndSend("/topic/validation", testResponse);
        return ResponseEntity.ok("Test message sent to /topic/validation");
    }

    @GetMapping("/by-smStudentId")
    public ResponseEntity<StudentResponseDTO> getStudentBySmStudentId(
            @RequestParam String smStudentId) {
        Student student = studentRepository.findBySmStudentId(smStudentId)
                .orElseThrow(() -> new RuntimeException("Student not found with sm_student_id: " + smStudentId));
        StudentResponseDTO responseDTO = studentMapper.toResponseDTO(student);
        return ResponseEntity.ok(responseDTO);
    }

    @EventListener
    public void handleSubscription(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        if ("/topic/validation".equals(destination)) {
            Map<String, Object> subscribeResponse = new HashMap<>();
            subscribeResponse.put("status", "Subscribed");
            subscribeResponse.put("message", "Successfully subscribed to /topic/validation");
            subscribeResponse.put("destination", destination);
            messagingTemplate.convertAndSend("/topic/validation", subscribeResponse);
        }
    }
}