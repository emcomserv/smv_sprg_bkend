package com.smartvehicle.service;

import com.smartvehicle.entity.Device;
import com.smartvehicle.entity.Route;
import com.smartvehicle.entity.Student;
import com.smartvehicle.entity.SwipeStudentDevice;
import com.smartvehicle.repository.DeviceRepository;
import com.smartvehicle.repository.RouteRepository;
import com.smartvehicle.repository.StudentRepository;
import com.smartvehicle.repository.SwipeStudentDeviceRepository;
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
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DeviceAuthTcpServerService {
    private static final int PORT = 8083;

    // Constants for new auth format
    private static final String AUTH_REQUEST_PREFIX = "1000";
    private static final String AUTH_REQUEST_SUFFIX = "AUTHAAAA";
    private static final String STUDENT_COUNT_PREFIX = "1001";
    private static final String STUDENT_COUNT_SUFFIX = "AAAA";
    private static final String STUDENT_LIST_PREFIX = "1002";
    private static final String STUDENT_LIST_SUFFIX = "StudentListAAAA";
    private static final String SWIPE_CARD_PREFIX = "1004";
    private static final String SWIPE_CARD_SUFFIX = "AAAA";

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private SwipeStudentDeviceRepository swipeStudentDeviceRepository;
    @Autowired
    private DeviceRepository deviceRepository;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

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
        try (InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream();
             PrintWriter out = new PrintWriter(outputStream, true)) {

            byte[] buffer = new byte[1024];
            StringBuilder dataBuffer = new StringBuilder();

            while (true) {
                int bytesRead = inputStream.read(buffer);
                if (bytesRead == -1) {
                    // Client disconnected
                    System.out.println("🔌 Client disconnected");
                    break;
                }

                // Convert bytes to string
                String receivedData = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                dataBuffer.append(receivedData);

                System.out.println("📥 Raw data received: '" + receivedData + "'");

                // Process the complete buffer
                String completeData = dataBuffer.toString();

                // Look for complete messages
                while (true) {
                    String message = extractCompleteMessage(completeData);
                    if (message == null) {
                        // No complete message found, keep remaining data in buffer
                        break;
                    }

                    // Process the complete message
                    processMessage(message, out);

                    // Remove the processed message from buffer
                    int messageEnd = completeData.indexOf(message) + message.length();
                    completeData = completeData.substring(messageEnd);
                    dataBuffer.setLength(0);
                    dataBuffer.append(completeData);
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

    private String extractCompleteMessage(String data) {
        // Look for complete messages that end with expected suffixes
        if (data.contains("AUTHAAAA")) {
            int start = data.indexOf("1000");
            int end = data.indexOf("AUTHAAAA") + "AUTHAAAA".length();
            if (start != -1 && end > start) {
                return data.substring(start, end);
            }
        }

        if (data.contains("StudentListAAAA")) {
            int start = data.indexOf("1002");
            int end = data.indexOf("StudentListAAAA") + "StudentListAAAA".length();
            if (start != -1 && end > start) {
                return data.substring(start, end);
            }
        }

        if (data.contains("AAAA")) {
            // Check for swipe card requests (1004...AAAA)
            int start = data.indexOf("1004");
            if (start != -1) {
                int end = data.indexOf("AAAA", start);
                if (end != -1 && end > start) {
                    end += "AAAA".length();
                    return data.substring(start, end);
                }
            }
        }

        return null; // No complete message found
    }

    private boolean isCompleteMessage(String message) {
        // Check if message ends with expected suffixes
        return message.endsWith("AUTHAAAA") ||
                message.endsWith("StudentListAAAA") ||
                message.endsWith("AAAA");
    }

    private void processMessage(String message, PrintWriter out) {
        System.out.println("🔍 Processing message: '" + message + "'");

        if (isAuthRequest(message)) {
            System.out.println("🔍 Processing auth request: " + message);
            String response = processAuthRequest(message, out);
            if (response != null) {
                out.println(response);
                System.out.println("📤 Sent auth response: " + response);
            } else {
                System.out.println("⚠️ No response generated for auth request: " + message);
            }
        } else if (isStudentListRequest(message)) {
            System.out.println("🔍 Processing student list request: " + message);
            processStudentListRequest(message, out);
        } else if (isSwipeCardRequest(message)) {
            System.out.println("🔍 Processing swipe card request: " + message);
            String response = processSwipeCardRequest(message);
            if (response != null) {
                out.println(response);
                System.out.println("📤 Sent swipe card response: " + response);
            } else {
                System.out.println("⚠️ No response generated for swipe card request: " + message);
            }
        } else {
            System.err.println("❌ Invalid request format received: " + message);
        }
    }

    private boolean isAuthRequest(String request) {
        return request != null &&
                request.startsWith(AUTH_REQUEST_PREFIX) &&
                request.endsWith(AUTH_REQUEST_SUFFIX);
    }

    private boolean isStudentListRequest(String request) {
        return request != null &&
                request.startsWith(STUDENT_LIST_PREFIX) &&
                request.endsWith(STUDENT_LIST_SUFFIX);
    }

    private boolean isSwipeCardRequest(String request) {
        return request != null &&
                request.startsWith(SWIPE_CARD_PREFIX) &&
                request.endsWith(SWIPE_CARD_SUFFIX);
    }

    private void processStudentListRequest(String request, PrintWriter out) {
        try {
            // Extract device ID, school ID, route ID, and count from format: 1002BNG0000001AC1F0002RT7F0001StudentListAAAA
            String deviceId = extractDeviceIdFromStudentListRequest(request);
            String schoolId = extractSchoolIdFromStudentListRequest(request);
            String routeId = extractRouteIdFromStudentListRequest(request);
            String count = extractCountFromStudentListRequest(request);

            System.out.println("🔍 Extracted - Device ID: " + deviceId + 
                    ", School ID: " + schoolId + 
                    ", Route ID: " + routeId + 
                    ", Count: " + (count != null ? count : "N/A"));

            if (deviceId == null || schoolId == null || routeId == null) {
                System.err.println("❌ Invalid student list request format: " + request);
                return;
            }

            // Find the route by sm_route_id
            Optional<Route> routeOpt = routeRepository.findBySmRouteId(routeId);
            if (routeOpt.isEmpty()) {
                System.err.println("❌ Route not found: " + routeId);
                return;
            }

            Route route = routeOpt.get();

            // Get all students for this route
            List<Student> students = studentRepository.findAllByRoute_Id(route.getId());
            System.out.println("📊 Found " + students.size() + " students for route: " + routeId);

            // Validate count for new format (optional)
            if (count != null) {
                try {
                    int expectedCount = Integer.parseInt(count);
                    if (students.size() != expectedCount) {
                        System.out.println("⚠️ Warning: Expected student count (" + expectedCount + 
                                ") does not match actual count (" + students.size() + ")");
                    }
                } catch (NumberFormatException e) {
                    System.err.println("❌ Invalid count format: " + count);
                }
            }

            // Send students one by one with 1-second delay
            for (int i = 0; i < students.size(); i++) {
                Student student = students.get(i);
                String studentResponse = "1002" + deviceId + schoolId + routeId + student.getSmStudentId() + "AAAA";

                // Schedule the response with delay
                final int index = i;
                scheduler.schedule(() -> {
                    try {
                        out.println(studentResponse);
                        System.out.println("📤 Sent student " + (index + 1) + "/" + students.size() + ": " + studentResponse);
                    } catch (Exception e) {
                        System.err.println("❌ Error sending student response: " + e.getMessage());
                    }
                }, i + 1, TimeUnit.SECONDS);
            }

            // Send END marker after all students
            String endResponse = "1002" + deviceId + schoolId + routeId + "ENDAAAA";
            scheduler.schedule(() -> {
                try {
                    out.println(endResponse);
                    System.out.println("📤 Sent END marker: " + endResponse);
                } catch (Exception e) {
                    System.err.println("❌ Error sending END marker: " + e.getMessage());
                }
            }, students.size() + 1, TimeUnit.SECONDS);

        } catch (Exception e) {
            System.err.println("❌ Error processing student list request: " + request + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String extractDeviceIdFromStudentListRequest(String request) {
        try {
            // Remove prefix (1002) and suffix (StudentListAAAA)
            String content = request.substring(STUDENT_LIST_PREFIX.length(),
                    request.length() - STUDENT_LIST_SUFFIX.length());

            // Extract device ID (first 10 characters after 1002)
            if (content.length() >= 10) {
                return content.substring(0, 10);
            }
        } catch (Exception e) {
            System.err.println("❌ Error extracting device ID from student list request: " + e.getMessage());
        }
        return null;
    }

    private String extractSchoolIdFromStudentListRequest(String request) {
        try {
            // Remove prefix (1002) and suffix (StudentListAAAA)
            String content = request.substring(STUDENT_LIST_PREFIX.length(),
                    request.length() - STUDENT_LIST_SUFFIX.length());

            // Check if this is new format (28 characters total) or old format (24 characters total)
            if (content.length() >= 28) {
                // New format: school ID is 8 characters (positions 10-18)
                return content.substring(10, 18);
            } else if (content.length() >= 24) {
                // Old format: school ID is 7 characters (positions 10-17)
                return content.substring(10, 17);
            }
        } catch (Exception e) {
            System.err.println("❌ Error extracting school ID from student list request: " + e.getMessage());
        }
        return null;
    }

    private String extractRouteIdFromStudentListRequest(String request) {
        try {
            // Remove prefix (1002) and suffix (StudentListAAAA)
            String content = request.substring(STUDENT_LIST_PREFIX.length(),
                    request.length() - STUDENT_LIST_SUFFIX.length());

            // Check if this is new format (28 characters total) or old format (24 characters total)
            if (content.length() >= 28) {
                // New format: route ID is 8 characters (positions 18-26)
                return content.substring(18, 26);
            } else if (content.length() >= 24) {
                // Old format: route ID is 7 characters (positions 17-24)
                return content.substring(17, 24);
            }
        } catch (Exception e) {
            System.err.println("❌ Error extracting route ID from student list request: " + e.getMessage());
        }
        return null;
    }

    private String extractCountFromStudentListRequest(String request) {
        try {
            // Remove prefix (1002) and suffix (StudentListAAAA)
            String content = request.substring(STUDENT_LIST_PREFIX.length(),
                    request.length() - STUDENT_LIST_SUFFIX.length());

            // Check if this is new format (28 characters total)
            if (content.length() >= 28) {
                // New format: count is 2 characters (positions 26-28)
                return content.substring(26, 28);
            }
            // Old format: no count field
            return null;
        } catch (Exception e) {
            System.err.println("❌ Error extracting count from student list request: " + e.getMessage());
        }
        return null;
    }

    private String processAuthRequest(String request, PrintWriter out) {
        try {
            // Extract device ID from format: 1000BNG0000001SSSSSSSSRRRRRRRRAUTHAAAA
            String deviceId = extractDeviceId(request);
            System.out.println("🔍 Extracted device ID: " + deviceId);

            if (deviceId == null || deviceId.isEmpty()) {
                System.err.println("❌ Invalid device ID in auth request: " + request);
                return null;
            }

            // Check if device exists in smv_device table and get school_id and route_id
            DeviceInfo deviceInfo = getDeviceInfo(deviceId);
            if (deviceInfo == null) {
                System.err.println("❌ Device not found: " + deviceId);
                return null;
            }

            // Generate JWT token for the device
            String jwtToken = generateJwtForDevice(deviceId);
            if (jwtToken == null) {
                System.err.println("❌ Failed to generate JWT for device: " + deviceId);
                return null;
            }

            // Build response: 1000BNG0000002AC1F0002RT7F0001authtokenAAAA
            String response = "1000" + deviceId + deviceInfo.getSchoolId() + deviceInfo.getRouteId() + jwtToken + "AAAA";

            // Schedule student count response after 3 seconds
            scheduleStudentCountResponse(deviceId, deviceInfo.getSchoolId(), deviceInfo.getRouteId(), out);

            System.out.println("📤 Sending auth response for device " + deviceId);
            return response;

        } catch (Exception e) {
            System.err.println("❌ Error processing auth request: " + request + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String processSwipeCardRequest(String request) {
        try {
            // Extract school ID, route ID, and student ID from format: 1004AC1F0002RT7F0001ST6F0003AAAA
            String schoolId = extractSchoolIdFromSwipeCardRequest(request);
            String routeId = extractRouteIdFromSwipeCardRequest(request);
            String studentId = extractStudentIdFromSwipeCardRequest(request);

            System.out.println("🔍 Extracted - School ID: " + schoolId + ", Route ID: " + routeId + ", Student ID: " + studentId);

            if (schoolId == null || routeId == null || studentId == null) {
                System.err.println("❌ Invalid swipe card request format: " + request);
                return buildSwipeCardResponse(schoolId, routeId, studentId, "06");
            }

            // 1. Query SwipeStudentDeviceRepository for the latest swipe record for the studentId
            SwipeStudentDevice latestSwipe = swipeStudentDeviceRepository.findTopByStudentIdOrderByTimestampDesc(studentId);
            if (latestSwipe == null) {
                System.err.println("❌ No swipe record found for student: " + studentId);
                return buildSwipeCardResponse(schoolId, routeId, studentId, "06");
            }

            // 2. Verify the student exists in StudentRepository
            Optional<Student> studentOpt = studentRepository.findBySmStudentId(studentId);
            if (studentOpt.isEmpty()) {
                System.err.println("❌ Student not found: " + studentId);
                return buildSwipeCardResponse(schoolId, routeId, studentId, "06");
            }
            Student student = studentOpt.get();

            // 3. Check if the student's school matches the swipe's school ID
            if (student.getSchool() == null || !student.getSchool().getId().equals(latestSwipe.getSchoolId())) {
                System.err.println("❌ Student's school doesn't match swipe's school ID");
                return buildSwipeCardResponse(schoolId, routeId, studentId, "06");
            }

            // 4. Verify the route exists in RouteRepository and is linked to the same school
            Optional<Route> routeOpt = routeRepository.findBySmRouteId(routeId);
            if (routeOpt.isEmpty()) {
                System.err.println("❌ Route not found: " + routeId);
                return buildSwipeCardResponse(schoolId, routeId, studentId, "06");
            }
            Route route = routeOpt.get();
            if (route.getSchool() == null || !route.getSchool().getId().equals(latestSwipe.getSchoolId())) {
                System.err.println("❌ Route's school doesn't match swipe's school ID");
                return buildSwipeCardResponse(schoolId, routeId, studentId, "06");
            }

            // 5. Construct FTP path: /upload/school_id/imagename.jpg
            String ftpPath = buildFtpPathFromSwipe(latestSwipe);
            if (ftpPath == null || ftpPath.isEmpty()) {
                System.err.println("❌ Failed to construct FTP path");
                return buildSwipeCardResponse(schoolId, routeId, studentId, "06");
            }

            // 6. Send to Python server and get response
            String pythonResponse = sendFtpPathToPythonServer(ftpPath, studentId, schoolId, routeId);
            if (pythonResponse == null) {
                System.err.println("❌ Python connection failed - sending error code 07");
                return buildSwipeCardResponse(schoolId, routeId, studentId, "07");
            }

            // 7. Map Python response to our format
            String responseCode = mapPythonResponseToCode(pythonResponse);
            return buildSwipeCardResponse(schoolId, routeId, studentId, responseCode);

        } catch (Exception e) {
            System.err.println("❌ Error processing swipe card request: " + request + " - " + e.getMessage());
            e.printStackTrace();
            return buildSwipeCardResponse(null, null, null, "06");
        }
    }

    private String buildSwipeCardResponse(String schoolId, String routeId, String studentId, String code) {
        if (schoolId == null || routeId == null || studentId == null) {
            return "1004" + "0000000" + "0000000" + "0000000" + code + "AAAA";
        }
        return "1004" + schoolId + routeId + studentId + code + "AAAA";
    }

    private String extractStudentIdFromSwipeCardRequest(String request) {
        try {
            // Remove prefix (1004) and suffix (AAAA)
            String content = request.substring(SWIPE_CARD_PREFIX.length(),
                    request.length() - SWIPE_CARD_SUFFIX.length());

            // Extract student ID (characters 16-23 after 1004)
            if (content.length() >= 24) {
                return content.substring(16, 24);
            }
        } catch (Exception e) {
            System.err.println("❌ Error extracting student ID from swipe card request: " + e.getMessage());
        }
        return null;
    }

    private String extractDeviceId(String request) {
        try {
            // Remove prefix (1000) and suffix (AUTHAAAA)
            String content = request.substring(AUTH_REQUEST_PREFIX.length(),
                    request.length() - AUTH_REQUEST_SUFFIX.length());

            // Extract device ID (first 10 characters after 1000)
            if (content.length() >= 10) {
                return content.substring(0, 10);
            }
        } catch (Exception e) {
            System.err.println("❌ Error extracting device ID: " + e.getMessage());
        }
        return null;
    }

    private String extractSchoolIdFromSwipeCardRequest(String request) {
        try {
            // Remove prefix (1004) and suffix (AAAA)
            String content = request.substring(SWIPE_CARD_PREFIX.length(),
                    request.length() - SWIPE_CARD_SUFFIX.length());

            // Extract school ID (first 8 characters after 1004)
            if (content.length() >= 8) {
                return content.substring(0, 8);
            }
        } catch (Exception e) {
            System.err.println("❌ Error extracting school ID from swipe card request: " + e.getMessage());
        }
        return null;
    }

    private String extractRouteIdFromSwipeCardRequest(String request) {
        try {
            // Remove prefix (1004) and suffix (AAAA)
            String content = request.substring(SWIPE_CARD_PREFIX.length(),
                    request.length() - SWIPE_CARD_SUFFIX.length());

            // Extract route ID (characters 8-15 after 1004)
            if (content.length() >= 16) {
                return content.substring(8, 16);
            }
        } catch (Exception e) {
            System.err.println("❌ Error extracting route ID from swipe card request: " + e.getMessage());
        }
        return null;
    }

    private DeviceInfo getDeviceInfo(String deviceId) {
        try {
            // Query smv_device table for device_id, school_id, route_id
            Optional<Device> deviceOpt = deviceRepository.findByDeviceId(deviceId);
            if (deviceOpt.isPresent()) {
                Device device = deviceOpt.get();
                return new DeviceInfo(device.getSchoolId(), device.getRouteId());
            }
            return null;
        } catch (Exception e) {
            System.err.println("❌ Error getting device info: " + e.getMessage());
            return null;
        }
    }

    private void scheduleStudentCountResponse(String deviceId, String schoolId, String routeId, PrintWriter out) {
        scheduler.schedule(() -> {
            try {
                int studentCount = getStudentCount(schoolId, routeId);
                String formattedStudentCount = String.format("%02d", studentCount);
                String studentCountResponse = "1001" + deviceId + schoolId + routeId + formattedStudentCount + "AAAA";
                out.println(studentCountResponse);
                System.out.println("📤 Sent student count response: " + studentCountResponse);
            } catch (Exception e) {
                System.err.println("❌ Error sending student count response: " + e.getMessage());
            }
        }, 3, TimeUnit.SECONDS);
    }

    private int getStudentCount(String schoolId, String routeId) {
        try {
            // Query for student count based on school_id and route_id
            // First find the route by sm_route_id
            Optional<Route> routeOpt = routeRepository.findBySmRouteId(routeId);
            if (routeOpt.isPresent()) {
                Route route = routeOpt.get();
                // Count students for this route
                List<Student> students = studentRepository.findAllByRoute_Id(route.getId());
                return students.size();
            }
            return 0;
        } catch (Exception e) {
            System.err.println("❌ Error getting student count: " + e.getMessage());
            return 0;
        }
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

    private String buildFtpPathFromSwipe(SwipeStudentDevice swipe) {
        if (swipe == null || swipe.getSchoolId() == null || swipe.getImageName() == null) {
            return null;
        }
        // Format: /upload/{schoolId}/{imageName}
        return "/upload/" + swipe.getSchoolId() + "/" + swipe.getImageName();
    }

    private String sendFtpPathToPythonServer(String ftpPath, String studentId, String schoolId, String routeId) {
        try {
            // Connect to Python server (adjust IP and port as needed)
            try (Socket pythonSocket = new Socket("68.178.203.99", 5005);
                 PrintWriter out = new PrintWriter(pythonSocket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(pythonSocket.getInputStream()))) {

                // Prepare payload for Python server
                Map<String, String> payload = new HashMap<>();
                payload.put("ftpPath", ftpPath);
                payload.put("studentId", studentId);
                payload.put("schoolId", schoolId);
                payload.put("routeId", routeId);
                payload.put("type", "swipe_card_validation");

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonPayload = objectMapper.writeValueAsString(payload);

                // Send to Python server
                out.println(jsonPayload);
                System.out.println("📤 Sent FTP path to Python server: " + ftpPath);

                // Read response from Python server (expects codes like 00,01,...)
                String pythonResponse = in.readLine();
                if (pythonResponse != null) {
                    System.out.println("📥 Python server response: " + pythonResponse);
                    // Extract leading code before comma if payload contains extra data
                    String code = pythonResponse.split(",")[0].trim();
                    return code;
                }
            } catch (IOException e) {
                System.err.println("❌ Failed to send FTP path to Python server: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ Error sending FTP path to Python server: " + e.getMessage());
        }
        return null;
    }

    private String mapPythonResponseToCode(String pythonResponse) {
        if (pythonResponse == null) {
            return "07"; // Python connection failed
        }

        String normalized = pythonResponse.trim();
        if (normalized.length() > 2) {
            normalized = normalized.substring(0, 2);
        }

        switch (normalized) {
            case "00": return "00"; // successful
            case "01": return "01"; // Image not matched confidence not enough
            case "02": return "02"; // error student id not found
            case "03": return "03"; // no face found
            case "04": return "04"; // image is blur
            case "05": return "05"; // no encodings found (student not trained)
            case "07": return "07"; // Python connection failed
            default: return "06";   // other failure response
        }
    }

    // Helper class to hold device information
    private static class DeviceInfo {
        private final String schoolId;
        private final String routeId;

        public DeviceInfo(String schoolId, String routeId) {
            this.schoolId = schoolId;
            this.routeId = routeId;
        }

        public String getSchoolId() {
            return schoolId;
        }

        public String getRouteId() {
            return routeId;
        }
    }
}