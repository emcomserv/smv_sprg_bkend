package com.smartvehicle.controller;

import com.smartvehicle.payload.request.AttenderUpdateReq;
import com.smartvehicle.repository.UserRepository;
import com.smartvehicle.service.RouteService;
import com.smartvehicle.service.UserService;
import com.smartvehicle.entity.*;
import com.smartvehicle.mapper.AttenderMapper;
import com.smartvehicle.payload.request.AttenderSignupReq;
import com.smartvehicle.payload.response.AttenderResponseDTO;
import com.smartvehicle.payload.response.SignupResponse;
import com.smartvehicle.repository.AttenderRepository;
import com.smartvehicle.repository.SchoolRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import com.smartvehicle.repository.StudentRepository;
import com.smartvehicle.payload.response.StudentIdListResponse;
import com.smartvehicle.service.FTPClientService;


@RestController
@RequestMapping("/api/v1/attender")
public class AttenderController {
    @Autowired
    private AttenderRepository attenderRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private SchoolRepository schoolRepository;
    @Autowired
    private RouteService routeService;
    @Autowired
    private AttenderMapper attenderMapper;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private FTPClientService ftpClientService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody AttenderSignupReq request) throws Exception{
        // Check for duplicate smAttenderId
        if (attenderRepository.findBySmAttenderId(request.getSmAttenderId()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("smAttenderId already exists! Please use a unique value.");
        }
        School school = schoolRepository.findById(request.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Error: School not found with id  "+request.getSchoolId()));
        User user = userService.registerUser(request, UserType.ATTENDER.name(),false);
        Route route = routeService.getRouteById(request.getRouteId());
        Attender attender = new Attender();
        attender.setUser(user);
        attender.setFirstName(request.getFirstName());
        attender.setLastName(request.getLastName());
        attender.setSchool(school);
        attender.setSmAttenderId(request.getSmAttenderId());
        attenderRepository.save(attender);
        SignupResponse response = new SignupResponse(request.getUsername(), request.getPhone());
        return new ResponseEntity<SignupResponse>(response, HttpStatus.CREATED);
    }

    /**
     * Get all routes
     */
    @GetMapping
    public ResponseEntity<List<AttenderResponseDTO>> getAllAttenders() {
        List<Attender> attenders = attenderRepository.findAllWithRelationships();
        List<AttenderResponseDTO> attenderResponseDTOS = attenderMapper.toResponseDTO(attenders);
        return ResponseEntity.ok(attenderResponseDTOS);
    }

    @GetMapping("/school/{schoolId}")
    public ResponseEntity<List<AttenderResponseDTO>> getAttendersBySchool(@PathVariable String schoolId) {
        List<Attender> attenders = attenderRepository.findBySchool_Id(schoolId);
        List<AttenderResponseDTO> response = attenderMapper.toResponseDTO(attenders);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a route by ID
     */
    @GetMapping("/route/{id}")
    public ResponseEntity<AttenderResponseDTO> getByRouteId(@PathVariable Long id) {
        Attender route = attenderRepository.findByRoute_Id(id)
                .orElseThrow(() -> new RuntimeException("Error: Attender not found with route id  "+id));
        AttenderResponseDTO attenderResponseDTO = attenderMapper.toResponseDTO(route);
        return ResponseEntity.ok(attenderResponseDTO);
    }


    @GetMapping("/{id}")
    public ResponseEntity<AttenderResponseDTO> getById(@PathVariable Long id) {
        Attender attender = attenderRepository.findByIdWithRelationships(id)
                .orElseThrow(() -> new RuntimeException("Error: Attender not found with id  "+id));
        AttenderResponseDTO attenderResponseDTO = attenderMapper.toResponseDTO(attender);
        return ResponseEntity.ok(attenderResponseDTO);
    }

    @GetMapping("/by-attenderId")
    public ResponseEntity<AttenderResponseDTO> getBySmAttenderId(@RequestParam String smAttenderId) {
        Attender attender = attenderRepository.findBySmAttenderId(smAttenderId)
                .orElseThrow(() -> new RuntimeException("Error: Attender not found with smAttenderId  " + smAttenderId));
        AttenderResponseDTO attenderResponseDTO = attenderMapper.toResponseDTO(attender);
        return ResponseEntity.ok(attenderResponseDTO);
    }

    // Returns ["ST6F0001","ST6F0004",...]
    @GetMapping("/students")
    public ResponseEntity<StudentIdListResponse> getStudentsBySchoolAndSmRoute(
            @RequestParam("schoolId") String schoolId,
            @RequestParam("smRouteId") String smRouteId) {
        List<String> ids = studentRepository.findSmStudentIdsBySchoolIdAndSmRouteId(schoolId, smRouteId);
        String count = String.format("%02d", ids.size());

        java.util.Map<String, String> imagesBase64 = new java.util.HashMap<>();
        for (String studentId : ids) {
            try {
                String dir = schoolId + "/" + smRouteId + "/" + studentId + "/Default";
                List<String> files = ftpClientService.listFilesInDirectory(dir);
                // Pick the first jpg/jpeg/png file if present
                String selected = null;
                for (String f : files) {
                    String lower = f.toLowerCase();
                    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")) {
                        selected = f;
                        break;
                    }
                }
                if (selected != null) {
                    String relativePath = dir + "/" + selected;
                    byte[] data = ftpClientService.readFile(relativePath);
                    String b64 = java.util.Base64.getEncoder().encodeToString(data);
                    imagesBase64.put(studentId, b64);
                }
            } catch (Exception ignored) {
            }
        }
        return ResponseEntity.ok(new StudentIdListResponse(count, ids, imagesBase64));
    }

    @PutMapping("/update/{id}")
    @Transactional
    public ResponseEntity<?> updateAttenderInfo(@PathVariable String id, @Valid @RequestBody AttenderUpdateReq updateReq) {
        Optional<Attender> attenderOptional = attenderRepository.findBySmAttenderId(id);
        if (!attenderOptional.isPresent())
            return ResponseEntity.ofNullable("Attender is not found with Id " + id);

        Attender attender = attenderOptional.get();

        // Update user details
        Optional<User> userOptional = userRepository.findById(attender.getUser().getId());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (updateReq.getPhone() != null && !updateReq.getPhone().isEmpty())
                user.setPhone(updateReq.getPhone());
            if (updateReq.getEmail() != null && !updateReq.getEmail().isEmpty())
                user.setEmail(updateReq.getEmail());

            userRepository.save(user);
            attender.setUser(user);
        }

        if (updateReq.getFirstName() != null) {
            attender.setFirstName(updateReq.getFirstName());
        }
        if (updateReq.getLastName() != null) {
            attender.setLastName(updateReq.getLastName());
        }
        if (updateReq.getRouteId() != null && !updateReq.getRouteId().isEmpty()) {
            Route route = routeService.getRouteBySmId(updateReq.getRouteId());
            attender.setRoute(route);
        }
        if (updateReq.getSchoolId() != null && !updateReq.getSchoolId().isEmpty()) {
            Optional<School> school = schoolRepository.findById(updateReq.getSchoolId());
            school.ifPresent(attender::setSchool);
        }

        attenderRepository.save(attender);
        return ResponseEntity.ok("Attender updated successfully for ID " + id);
    }
}
