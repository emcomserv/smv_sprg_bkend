package com.smartvehicle.controller;

import com.smartvehicle.repository.RouteSchlStudentMappingRepo;
import com.smartvehicle.repository.SwipeReportMobileRepository;


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
import com.smartvehicle.repository.ParentRepository;
import com.smartvehicle.repository.SchoolRepository;
import com.smartvehicle.repository.StudentRepository;
import com.smartvehicle.service.RouteService;
import com.smartvehicle.service.StudentService;
import com.smartvehicle.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> registerUser(@Valid @RequestBody StudentSignupReq request) throws Exception {
        School school = schoolRepository.findById(request.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Error: School not found with id  " + request.getSchoolId()));
        User user = userService.registerUser(request, UserType.STUDENT.name(), false);
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
        if (request.getRouteId() != null) {
            Route route = routeService.getRouteById(request.getRouteId());
            student.setRoute(route);
            if (request.getRoutePointId() != null) {
                RoutePoint routePoint = routeService.getRoutePointById(request.getRoutePointId());
                student.setRoutePoint(routePoint);
            }
        }

        Student student1 = studentRepository.save(student);

        //Saving data to mapping table
        RouteSchoolStudentMapping routeSchoolStudentMapping = new RouteSchoolStudentMapping();
        routeSchoolStudentMapping.setRoute(student1.getRoute());
        routeSchoolStudentMapping.setSmStudentId(student1.getSmStudentId());
        routeSchoolStudentMapping.setSchool(student1.getSchool());
        routeSchoolStudentMapping.setProvId(student1.getSchool().getProvId());

        routeSchlStudentMappingRepo.save(routeSchoolStudentMapping);

        StudentResponseDTO studentResponseDTO = studentMapper.toResponseDTO(student);
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
            response.put("message", "Invalid input format. Expected format: ROUTE-SCHOOL-STUDENT");
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
        String routeId = inputParts[0].toUpperCase();
        String schoolId = inputParts[1].toUpperCase();
        String studentIdWithCityCode = inputParts[2].toUpperCase();

        // Validate routeId is not empty or malformed
        if (routeId.isEmpty()) {
            response.put("status", "Invalid");
            response.put("message", "Invalid or missing route ID");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Extract sm_student_id by removing the first 3 characters (city code)
        if (studentIdWithCityCode.length() <= 3) {
            response.put("status", "Invalid");
            response.put("message", "Invalid student ID format: Too short to contain a city code");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
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

        // Construct the full FTP file path (including filename)
        String ftpPath = String.format("/upload/%s/%s/Default/%s", schoolId, studentId,imageName);

        // Prepare payload for Python Analyzer
        Map<String, String> payload = new HashMap<>();
        payload.put("ftpPath", ftpPath); // Send full file path
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
        try (Socket socket = new Socket("127.0.0.1", 5004);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send JSON payload to Python Analyzer
            out.println(jsonPayload);

            // Receive response from Python Analyzer
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

        // Parse Python Analyzer's response (expected format: "student ID=found, image=matched, 85%")
        boolean studentIdFound = false;
        boolean imageMatched = false;
        double confidence = 0.0;
        try {
            String[] parts = analyzerResponse.split(",");
            for (String part : parts) {
                part = part.trim();
                if (part.startsWith("student ID=")) {
                    studentIdFound = part.split("=")[1].equals("found");
                } else if (part.startsWith("image=")) {
                    imageMatched = part.split("=")[1].equals("matched");
                } else if (part.endsWith("%")) {
                    confidence = Double.parseDouble(part.replace("%", ""));
                }
            }
        } catch (Exception e) {
            response.put("status", "Error");
            response.put("message", "Invalid response format from Python Analyzer: " + analyzerResponse);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        // Validate analyzer response
        if (!studentIdFound || !imageMatched) {
            response.put("status", "Invalid");
            response.put("message", "Student verification failed: ID or image not matched");
            response.put("isValid", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Check confidence threshold (e.g., 70%)
        double confidenceThreshold = 70.0;
        if (confidence < confidenceThreshold) {
            response.put("status", "Invalid");
            response.put("message", "Student verification failed: Confidence (" + confidence + "%) below threshold (" + confidenceThreshold + "%)");
            response.put("isValid", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // All checks passed, student entry is accepted
        response.put("status", "Valid");
        response.put("isValid", true);
        response.put("confidence", confidence);
        response.put("message", "Student Entry = Accepted");

        SwipeReportMobile swipeReportMobile = new SwipeReportMobile();
        swipeReportMobile.setRouteId(routeId);
        swipeReportMobile.setSchoolId(schoolId);
        swipeReportMobile.setStudentId(studentId);
        swipeReportMobile.setLatitude(latitude);
        swipeReportMobile.setLongitude(longitude);
        swipeReportMobile.setImageName(imageName); // Not used since image is uploaded directly to FTP
        swipeReportMobileRepository.save(swipeReportMobile);

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

        // Find the student by sm_student_id
        Student student = studentRepository.findBySmStudentId(smStudentId)
                .orElseThrow(() -> new RuntimeException("Student not found with sm_student_id: " + smStudentId));

        // Update fields if present in request
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

        // Update School if provided
        if (request.getSchoolId() != null) {
            School school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("School not found with ID: " + request.getSchoolId()));
            student.setSchool(school);
        }

        // Update Parent if provided
        if (request.getParentId() != null) {
            Parent parent = parentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent not found with ID: " + request.getParentId()));
            student.setParent(parent);
        }

        // Update Route and RoutePoint if provided
        if (request.getRouteId() != null) {
            Route route = routeService.getRouteById(request.getRouteId());
            student.setRoute(route);

            if (request.getRoutePointId() != null) {
                RoutePoint routePoint = routeService.getRoutePointById(request.getRoutePointId());
                student.setRoutePoint(routePoint);
            }
        }

        // Save the updated student
        Student updatedStudent = studentRepository.save(student);

        // Map the updated student to StudentResponseDTO
        StudentResponseDTO responseDTO = studentMapper.toResponseDTO(updatedStudent);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/by-smStudentId")
    public ResponseEntity<StudentResponseDTO> getStudentBySmStudentId(
            @RequestParam String smStudentId) {

        // Fetch student by sm_student_id
        Student student = studentRepository.findBySmStudentId(smStudentId)
                .orElseThrow(() -> new RuntimeException("Student not found with sm_student_id: " + smStudentId));

        // Map the student entity to StudentResponseDTO using MapStruct
        StudentResponseDTO responseDTO = studentMapper.toResponseDTO(student);

        return ResponseEntity.ok(responseDTO);
    }
}