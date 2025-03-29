package com.smartvehicle.controller;

import com.smartvehicle.repository.RouteSchlStudentMappingRepo;
import com.smartvehicle.repository.SwipeReportMobileRepository;

import com.smartvehicle.service.RouteService;
import com.smartvehicle.service.StudentService;
import com.smartvehicle.service.UserService;
import com.smartvehicle.entity.*;
import com.smartvehicle.mapper.StudentMapper;
import com.smartvehicle.payload.request.StudentChangeRouteReq;
import com.smartvehicle.payload.request.StudentPickupPointReq;
import com.smartvehicle.payload.request.StudentSignupReq;
import com.smartvehicle.payload.response.StudentResponseDTO;
import com.smartvehicle.payload.response.StudentResponseLtDTO;
import com.smartvehicle.repository.ParentRepository;
import com.smartvehicle.repository.SchoolRepository;
import com.smartvehicle.repository.StudentRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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
    @Autowired private RouteSchlStudentMappingRepo routeSchlStudentMappingRepo;
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> registerUser(@Valid @RequestBody StudentSignupReq request) throws Exception{
        School school = schoolRepository.findById(request.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Error: School not found with id  "+request.getSchoolId()));
        User user = userService.registerUser(request, UserType.STUDENT.name(),false);
        Parent parent = parentRepository.findById(request.getParentId())
                .orElseThrow(() -> new RuntimeException("Error: Parent not found with id  "+request.getParentId()));
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
        if(request.getRouteId()!=null){
            Route route = routeService.getRouteById(request.getRouteId());
            student.setRoute(route);
            if(request.getRoutePointId()!=null){
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

//            SignupResponse response = new SignupResponse(request.getUsername(), request.getPhone());
        StudentResponseDTO studentResponseDTO = studentMapper.toResponseDTO(student);
        return new ResponseEntity<StudentResponseDTO>(studentResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<StudentResponseDTO>> getAllStudents() {
        List<Student> students = studentRepository.findAll();
        List<StudentResponseDTO> studentResponseDTOS = studentMapper.toResponseDTO(students);
        return ResponseEntity.ok(studentResponseDTOS);
    }

    /**
     * Get a route by ID
     */
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
                .orElseThrow(() -> new RuntimeException("Error: Student not found with id  "+id));
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
                .orElseThrow(() -> new RuntimeException("Error: Student not found with id  "+studentId));
        Route route = routeService.getRouteById(req.getRouteId());
        student.setRoute(route);
        studentRepository.save(student);
        StudentResponseDTO studentResponseDTO = studentMapper.toResponseDTO(student);
        return ResponseEntity.ok(studentResponseDTO);
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateAndStoreStudent(
            @RequestParam String input,
            @RequestParam String latitude,
            @RequestParam String longitude,
            @RequestParam String imageName) {

        // Extract route_id, school_id, and student_id from input
        String[] inputParts = input.split("-");
        String routeId = inputParts[0].toUpperCase();  // "TBC"
        String schoolId = inputParts[1].toUpperCase(); // "TEST"
        String studentId = inputParts[2].toUpperCase(); // "BNG00ABEF01"
        studentId=studentId.substring(3);


        // Validate student in the other table (students)
        boolean isValid = studentService.isValidStudent(input);

        Map<String, Object> response = new HashMap<>();
        response.put("status", isValid ? "Valid" : "Invalid");
        response.put("isValid", isValid);

        if(isValid){
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
