package com.smartvehicle.service;

import com.smartvehicle.entity.Route;
import com.smartvehicle.entity.Student;
import com.smartvehicle.repository.RouteRepository;
import com.smartvehicle.repository.StudentRepository;
import com.smartvehicle.security.jwt.JwtUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.smartvehicle.entity.SwipeStudentDevice;
import com.smartvehicle.repository.SwipeStudentDeviceRepository;
import java.util.Optional;

@Service
public class DeviceAuthTcpServerService {
    private static final int PORT = 5000;

    // Constants for authentication and assignment requests
    private static final String REQUEST_PREFIX = "#SMV";
    private static final String REQUEST_SUFFIX = "_AAAA";
    private static final String AUTH_REQUEST_PATTERN = "_1000_AUTH";
    private static final String AUTH_RESPONSE_PATTERN = "_1000_JWT:";
    private static final String ASSIGN_REQUEST_PATTERN = "_1001_";

    private static final String STUDENT_VALIDATION_PREFIX = "#SMVSTD_";
    private static final String STUDENT_VALIDATION_SUFFIX = "AAAA";
    private static final String STUDENT_VALIDATION_SUCCESS = "<00>";
    private static final String STUDENT_VALIDATION_FAILURE = "<01>";

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private SwipeStudentDeviceRepository swipeStudentDeviceRepository;

    @PostConstruct
    public void startServer() {
        Thread serverThread = new Thread(this::runServer, "DeviceAuthTcpServer-Thread");
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private void runServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("🔐 Device Auth TCP Server listening on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("🔌 Auth client connected: " + clientSocket.getInetAddress());
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("❌ Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleClient(Socket socket) {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("📥 Auth received raw data: '" + line + "'");

                // Student validation logic
                if (isStudentValidationRequest(line)) {
                    System.out.println("🔍 Processing student validation request: " + line);
                    String response = processStudentValidationRequest(line);
                    if (response != null) {
                        out.println(response);
                        System.out.println("📤 Sent student validation response: " + response);
                    } else {
                        System.out.println("⚠️ No response generated for student validation request: " + line);
                    }
                    continue;
                }

                if (isAssignRequest(line)) {
                    System.out.println("🔍 Processing assign request: " + line);
                    String response = processAssignRequest(line);
                    if (response != null) {
                        out.println(response);
                        System.out.println("📤 Sent assign response: " + response);
                    } else {
                        System.out.println("⚠️ No response generated for assign request: " + line);
                    }
                    continue;
                }

                if (isAuthRequest(line)) {
                    System.out.println("🔍 Processing auth request: " + line);
                    String response = processAuthRequest(line);
                    if (response != null) {
                        out.println(response);
                        System.out.println("📤 Sent auth response: " + response);
                    } else {
                        System.out.println("⚠️ No response generated for auth request: " + line);
                    }
                } else {
                    System.err.println("❌ Invalid auth format received: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("❗ Error handling auth client: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("❌ Error closing socket: " + e.getMessage());
            }
        }
    }

    private boolean isAuthRequest(String request) {
        return request != null &&
                request.startsWith(REQUEST_PREFIX) &&
                request.endsWith(REQUEST_SUFFIX) &&
                request.contains(AUTH_REQUEST_PATTERN);
    }

    private boolean isAssignRequest(String request) {
        return request != null &&
                request.startsWith(REQUEST_PREFIX) &&
                request.endsWith(REQUEST_SUFFIX) &&
                request.contains(ASSIGN_REQUEST_PATTERN);
    }

    private boolean isStudentValidationRequest(String request) {
        return request != null &&
                request.startsWith(STUDENT_VALIDATION_PREFIX) &&
                request.endsWith(STUDENT_VALIDATION_SUFFIX);
    }

    private String processAssignRequest(String request) {
        try {
            String deviceId = extractDeviceIdForAssign(request);
            if (deviceId == null || deviceId.isEmpty()) {
                System.err.println("❌ Invalid device ID in assign request: " + request);
                return null;
            }

            // Extract school ID from request
            String schoolId = extractSchoolIdForAssign(request);
            if (schoolId == null || schoolId.isEmpty()) {
                System.err.println("❌ Invalid school ID in assign request: " + request);
                return null;
            }

            // Check if this device already has an assigned route
            Route alreadyAssigned = routeRepository.findByDeviceIdAndAssignedTrue(deviceId);
            if (alreadyAssigned != null) {
                // Respond with the same assignment
                String response = "#SMV_" + deviceId + "_1001_" +
                    alreadyAssigned.getSchool().getId() + "_" + alreadyAssigned.getSmRouteId() + "_AAAA";
                
                // Fetch all students for the already assigned route
                List<Student> students = studentRepository.findAllByRoute_Id(alreadyAssigned.getId());
                if (!students.isEmpty()) {
                    StringBuilder studentList = new StringBuilder();
                    for (Student student : students) {
                        // Format: #SMV_DEV1_1002_RT0F0002_ST0F0002_AAAA
                        studentList.append("#SMV_").append(deviceId).append("_1002_").append(alreadyAssigned.getSmRouteId()).append("_").append(student.getSmStudentId()).append("_AAAA");
                        studentList.append("\n");
                    }
                    // Add END marker: #SMV_DEV1_1002_END_AAAA
                    studentList.append("#SMV_").append(deviceId).append("_1002_END_AAAA");
                    response += "\n" + studentList.toString();
                }
                
                return response;
            }

            // Find first unassigned route for the specific school
            Route route = routeRepository.findFirstBySchool_IdAndAssignedFalseOrAssignedIsNull(schoolId);
            if (route == null) {
                System.out.println("⚠️ No unassigned route available for school: " + schoolId);
                return "#SMV_" + deviceId + "_1001_" + schoolId + "_NO_ROUTE_AVAILABLE_AAAA";
            }

            // Assign the route
            route.setAssigned(true);
            route.setDeviceId(deviceId);
            routeRepository.save(route);

            // Build response: #SMV_DEV1_1001_AC0F0001_RT1F0001_AAAA
            String response = "#SMV_" + deviceId + "_1001_" +
                route.getSchool().getId() + "_" + route.getSmRouteId() + "_AAAA";

                            // Fetch all students for the assigned route
                List<Student> students = studentRepository.findAllByRoute_Id(route.getId());
                if (!students.isEmpty()) {
                    StringBuilder studentList = new StringBuilder();
                    for (Student student : students) {
                        // Format: #SMV_DEV1_1002_RT0F0002_ST0F0002_AAAA
                        studentList.append("#SMV_").append(deviceId).append("_1002_").append(route.getSmRouteId()).append("_").append(student.getSmStudentId()).append("_AAAA");
                        studentList.append("\n");
                    }
                    // Add END marker: #SMV_DEV1_1002_END_AAAA
                    studentList.append("#SMV_").append(deviceId).append("_1002_END_AAAA");
                    response += "\n" + studentList.toString();
                }

            return response;
        } catch (Exception e) {
            System.err.println("❌ Error processing assign request: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String processStudentValidationRequest(String request) {
        try {
            // Extract student ID from format #SMVSTD_ST0F0002AAAA
            String studentId = request.substring(STUDENT_VALIDATION_PREFIX.length(), request.indexOf(STUDENT_VALIDATION_SUFFIX));
            System.out.println("🔍 Extracted student ID: " + studentId);
            System.out.println("📝 Processing student validation request: " + request);

            // Get the latest swipe for the student
            SwipeStudentDevice latestSwipe = swipeStudentDeviceRepository.findTopByStudentIdOrderByTimestampDesc(studentId);

            if (latestSwipe == null) {
                // No swipe found, send failure
                return buildStudentValidationResponse(studentId, STUDENT_VALIDATION_FAILURE);
            }

            // 1. Check if student exists
            Optional<Student> studentOpt = studentRepository.findBySmStudentId(latestSwipe.getStudentId());
            if (studentOpt.isEmpty()) {
                // Student not found
                return buildStudentValidationResponse(studentId, STUDENT_VALIDATION_FAILURE);
            }
            Student student = studentOpt.get();

            // 2. Check if student is linked to the school
            if (!student.getSchool().getId().equals(latestSwipe.getSchoolId())) {
                // Student not linked to this school
                return buildStudentValidationResponse(studentId, STUDENT_VALIDATION_FAILURE);
            }

            // 3. Check if route exists and is linked to the same school
            Optional<Route> routeOpt = routeRepository.findBySmRouteId(latestSwipe.getRouteId());
            if (routeOpt.isEmpty()) {
                // Route not found
                return buildStudentValidationResponse(studentId, STUDENT_VALIDATION_FAILURE);
            }
            Route route = routeOpt.get();
            if (!route.getSchool().getId().equals(latestSwipe.getSchoolId())) {
                // Route not linked to this school
                return buildStudentValidationResponse(studentId, STUDENT_VALIDATION_FAILURE);
            }

            // 4. All checks passed, send success
            return buildStudentValidationResponse(studentId, STUDENT_VALIDATION_SUCCESS);

        } catch (Exception e) {
            System.err.println("❌ Error processing student validation request: " + request + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String buildStudentValidationResponse(String studentId, String status) {
        return STUDENT_VALIDATION_PREFIX + studentId + status + STUDENT_VALIDATION_SUFFIX;
    }

    private String extractDeviceIdForAssign(String request) {
        try {
            // Remove prefix and suffix
            String content = request.substring(REQUEST_PREFIX.length(), request.length() - REQUEST_SUFFIX.length());
            // content: _DEV1_1001_School-Route Required
            int start = content.indexOf("_") + 1;
            int end = content.indexOf(ASSIGN_REQUEST_PATTERN);
            if (start >= 0 && end > start) {
                return content.substring(start, end);
            }
        } catch (Exception e) {
            System.err.println("❌ Error extracting device ID for assign: " + e.getMessage());
        }
        return null;
    }

    private String extractSchoolIdForAssign(String request) {
        try {
            // Remove prefix and suffix
            String content = request.substring(REQUEST_PREFIX.length(), request.length() - REQUEST_SUFFIX.length());
            // content: _DEV1_1001_AC0F0001_Route Required
            
            // Find the school ID between _1001_ and _Route Required
            int start = content.indexOf(ASSIGN_REQUEST_PATTERN) + ASSIGN_REQUEST_PATTERN.length();
            int end = content.indexOf("_Route Required");
            if (start >= 0 && end > start) {
                return content.substring(start, end);
            }
        } catch (Exception e) {
            System.err.println("❌ Error extracting school ID for assign: " + e.getMessage());
        }
        return null;
    }

    private String processAuthRequest(String request) {
        try {
            // Extract device ID from format #SMV_1000_AUTH_AAAA
            String deviceId = extractDeviceId(request);
            System.out.println("🔍 Extracted device ID: " + deviceId);

            if (deviceId == null || deviceId.isEmpty()) {
                System.err.println("❌ Invalid device ID in auth request: " + request);
                return null;
            }

            // Generate JWT token for the device
            String jwtToken = generateJwtForDevice(deviceId);
            if (jwtToken == null) {
                System.err.println("❌ Failed to generate JWT for device: " + deviceId);
                return null;
            }

            // Build response: #SMV_1000_JWT:"token"_AAAA
            String response = REQUEST_PREFIX + AUTH_RESPONSE_PATTERN + "\"" + jwtToken + "\"" + REQUEST_SUFFIX;

            System.out.println("📤 Sending auth response for device " + deviceId);
            return response;

        } catch (Exception e) {
            System.err.println("❌ Error processing auth request: " + request + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String extractDeviceId(String request) {
        try {
            System.out.println("🔍 DEBUG: Processing request: '" + request + "'");

            // Remove prefix and suffix
            String content = request.substring(REQUEST_PREFIX.length(),
                    request.length() - REQUEST_SUFFIX.length());
            System.out.println("🔍 DEBUG: After removing prefix/suffix: '" + content + "'");

            // Check if it's an auth request
            if (content.contains(AUTH_REQUEST_PATTERN)) {
                System.out.println("🔍 DEBUG: Contains AUTH_REQUEST_PATTERN: '" + AUTH_REQUEST_PATTERN + "'");

                // Extract device ID (everything before _1000_AUTH_)
                int authIndex = content.indexOf(AUTH_REQUEST_PATTERN);
                System.out.println("🔍 DEBUG: AUTH_REQUEST_PATTERN found at index: " + authIndex);

                if (authIndex > 0) {
                    String deviceId = content.substring(0, authIndex);
                    System.out.println("🔍 DEBUG: Extracted device ID (before cleanup): '" + deviceId + "'");

                    // Remove leading underscore if present
                    if (deviceId.startsWith("_")) {
                        deviceId = deviceId.substring(1);
                        System.out.println("🔍 DEBUG: Removed leading underscore: '" + deviceId + "'");
                    }

                    System.out.println("🔍 DEBUG: Final device ID: '" + deviceId + "'");
                    return deviceId;
                } else {
                    System.out.println("🔍 DEBUG: authIndex is not > 0, it's: " + authIndex);
                }
            } else {
                System.out.println("🔍 DEBUG: Does NOT contain AUTH_REQUEST_PATTERN: '" + AUTH_REQUEST_PATTERN + "'");
            }
        } catch (Exception e) {
            System.err.println("❌ Error extracting device ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private String generateJwtForDevice(String deviceId) {
        try {
            // Create claims for the device
            Map<String, Object> claims = new HashMap<>();
            claims.put("deviceId", deviceId);
            claims.put("type", "DEVICE");

            // Generate JWT token (valid for 24 hours)
            String jwt = jwtUtils.generateJwtTokenForDevice(deviceId, claims);
            System.out.println("🔑 Generated JWT for device " + deviceId + ": " + jwt.substring(0, Math.min(50, jwt.length())) + "...");

            return jwt;
        } catch (Exception e) {
            System.err.println("❌ Error generating JWT: " + e.getMessage());
            return null;
        }
    }
}