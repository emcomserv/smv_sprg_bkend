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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    // GPS Location constants
    private static final String GPS_LOCATION_PREFIX = "1005";
    private static final String GPS_LOCATION_SUFFIX = "GPSAAAA";

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
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    // State management for student list sending
    private final Map<String, StudentListState> deviceStates = new HashMap<>();

    // State management by school and route combination
    private final Map<String, StudentListState> schoolRouteStates = new HashMap<>();

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
                // Clean up device states for this connection
                cleanupDeviceStatesForConnection(socket);
                socket.close();
            } catch (IOException e) {
                System.err.println("❌ Error closing socket: " + e.getMessage());
            }
        }
    }

    private void cleanupDeviceStatesForConnection(Socket socket) {
        // This is a simplified cleanup - in a real implementation, you might want to
        // track which devices are associated with which connections
        System.out.println("🧹 Cleaning up device states for disconnected connection: " + socket.getInetAddress());
        // For now, we'll keep the device states as they might be needed for reconnections
        // You might want to implement a more sophisticated cleanup mechanism
        // Note: We're now using schoolRouteStates instead of deviceStates
    }

    private String extractCompleteMessage(String data) {
        System.out.println("🔍 extractCompleteMessage called with data: '" + data + "'");

        // Look for complete messages that end with expected suffixes
        if (data.contains("AUTHAAAA")) {
            int start = data.indexOf("1000");
            int end = data.indexOf("AUTHAAAA") + "AUTHAAAA".length();
            if (start != -1 && end > start) {
                String message = data.substring(start, end);
                System.out.println("🔍 Found AUTH message: '" + message + "'");
                return message;
            }
        }

        if (data.contains("StudentListAAAA")) {
            int start = data.indexOf("1002");
            int end = data.indexOf("StudentListAAAA") + "StudentListAAAA".length();
            if (start != -1 && end > start) {
                String message = data.substring(start, end);
                System.out.println("🔍 Found StudentList message: '" + message + "'");
                return message;
            }
        }

        if (data.contains("ACKAAAA")) {
            int start = data.indexOf("1002");
            int end = data.indexOf("ACKAAAA") + "ACKAAAA".length();
            if (start != -1 && end > start) {
                String message = data.substring(start, end);
                System.out.println("🔍 Found ACK message: '" + message + "'");
                return message;
            }
        }

        if (data.contains("GPSAAAA")) {
            int start = data.indexOf("1005");
            int end = data.indexOf("GPSAAAA") + "GPSAAAA".length();
            if (start != -1 && end > start) {
                String message = data.substring(start, end);
                System.out.println("🔍 Found GPS message: '" + message + "'");
                return message;
            }
        }

        if (data.contains("AAAA")) {
            // Check for swipe card requests (1004...AAAA)
            int start = data.indexOf("1004");
            if (start != -1) {
                int end = data.indexOf("AAAA", start);
                if (end != -1 && end > start) {
                    end += "AAAA".length();
                    String message = data.substring(start, end);
                    System.out.println("🔍 Found swipe card message: '" + message + "'");
                    return message;
                }
            }
        }

        System.out.println("🔍 No complete message found in data");
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
        } else if (isAcknowledgmentMessage(message)) {
            System.out.println("🔍 Processing acknowledgment message: " + message);
            processAcknowledgmentMessage(message, out);
        } else if (isGpsLocationMessage(message)) {
            System.out.println("🔍 Processing GPS location message: " + message);
            processGpsLocationMessage(message, out);
        } else {
            System.err.println("❌ Invalid request format received: " + message);
            System.err.println("❌ Message does not match any expected format");
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

    private boolean isAcknowledgmentMessage(String message) {
        boolean isAck = message != null && message.endsWith("ACKAAAA");
        System.out.println("🔍 Checking if message is acknowledgment: '" + message + "' -> " + isAck);
        return isAck;
    }

    private boolean isGpsLocationMessage(String message) {
        boolean isGps = message != null && message.startsWith(GPS_LOCATION_PREFIX) && message.endsWith(GPS_LOCATION_SUFFIX);
        System.out.println("🔍 Checking if message is GPS location: '" + message + "' -> " + isGps);
        return isGps;
    }

    private void processAcknowledgmentMessage(String message, PrintWriter out) {
        try {
            System.out.println("🔍 Parsing acknowledgment message: " + message);

            // Extract school ID, route ID, and student ID from acknowledgment format: 1002AC1F0002RT7F0001ST6F0001ACKAAAA
            String schoolId = extractSchoolIdFromAcknowledgment(message);
            String routeId = extractRouteIdFromAcknowledgment(message);
            String studentId = extractStudentIdFromAcknowledgment(message);

            System.out.println("✅ Received acknowledgment for - School ID: '" + schoolId +
                    "', Route ID: '" + routeId +
                    "', Student ID: '" + studentId + "'");

            if (schoolId == null || routeId == null || studentId == null) {
                System.err.println("❌ Failed to parse acknowledgment message: " + message);
                return;
            }

            System.out.println("🚀 About to process next student for school: " + schoolId + " and route: " + routeId);

            // Find the device that sent this student and process the next student
            processNextStudentForSchoolRoute(schoolId, routeId, studentId, out);

        } catch (Exception e) {
            System.err.println("❌ Error processing acknowledgment message: " + message + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String extractSchoolIdFromAcknowledgment(String message) {
        try {
            // Remove prefix (1002) and find school ID pattern
            // Acknowledgment format: 1002AC1F0002RT7F0001ST6F0001ACKAAAA
            String content = message.substring(4); // Remove "1002"

            System.out.println("🔍 Acknowledgment content after removing prefix: '" + content + "'");
            System.out.println("🔍 Acknowledgment content length: " + content.length());

            // School ID is first 8 characters after 1002
            if (content.length() >= 8) {
                String schoolId = content.substring(0, 8);
                System.out.println("🔍 Extracted school ID from acknowledgment: '" + schoolId + "'");
                return schoolId;
            }
        } catch (Exception e) {
            System.err.println("❌ Error extracting school ID from acknowledgment: " + e.getMessage());
        }
        return null;
    }

    private String extractRouteIdFromAcknowledgment(String message) {
        try {
            // Remove prefix (1002) and find route ID pattern
            // Acknowledgment format: 1002AC1F0002RT7F0001ST6F0001ACKAAAA
            String content = message.substring(4); // Remove "1002"

            // School ID is 8 characters, so Route ID starts at position 8
            if (content.length() >= 16) {
                // Route ID is 8 characters (positions 8-16)
                String routeId = content.substring(8, 16);
                System.out.println("🔍 Extracted route ID from acknowledgment: '" + routeId + "'");
                return routeId;
            }
        } catch (Exception e) {
            System.err.println("❌ Error extracting route ID from acknowledgment: " + e.getMessage());
        }
        return null;
    }

    private String extractStudentIdFromAcknowledgment(String message) {
        try {
            // Remove prefix (1002) and find student ID pattern
            // Acknowledgment format: 1002AC1F0002RT7F0001ST6F0001ACKAAAA
            String content = message.substring(4); // Remove "1002"

            // School ID is 8 characters, Route ID is 8 characters, so Student ID starts at position 16
            if (content.length() >= 24) {
                // Student ID is 8 characters (positions 16-24)
                String studentId = content.substring(16, 24);
                System.out.println("🔍 Extracted student ID from acknowledgment: '" + studentId + "'");
                return studentId;
            }
        } catch (Exception e) {
            System.err.println("❌ Error extracting student ID from acknowledgment: " + e.getMessage());
        }
        return null;
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

            // Start the acknowledgment-based student sending process
            startAcknowledgmentBasedStudentSending(deviceId, schoolId, routeId, students, out);

        } catch (Exception e) {
            System.err.println("❌ Error processing student list request: " + request + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startAcknowledgmentBasedStudentSending(String deviceId, String schoolId, String routeId,
                                                        List<Student> students, PrintWriter out) {
        if (students.isEmpty()) {
            // Send END marker immediately if no students
            String endResponse = "1002" + deviceId + schoolId + routeId + "ENDAAAA";
            out.println(endResponse);
            System.out.println("📤 Sent END marker (no students): " + endResponse);
            return;
        }

        // Initialize state for this school and route combination
        String schoolRouteKey = schoolId + "_" + routeId;
        System.out.println("🔑 Creating state with key: '" + schoolRouteKey + "' for device: " + deviceId);

        StudentListState state = new StudentListState(students, deviceId);
        schoolRouteStates.put(schoolRouteKey, state);

        System.out.println("🚀 Starting acknowledgment-based student sending for device " + deviceId +
                " with " + students.size() + " students");
        System.out.println("🔑 State stored with key: '" + schoolRouteKey + "'");
        System.out.println("🔑 Total states in map after storing: " + schoolRouteStates.size());
        System.out.println("🔑 Available keys after storing: " + schoolRouteStates.keySet());

        // Send first student immediately
        sendNextStudentForSchoolRoute(schoolId, routeId, students, state, out);

        // Schedule a timeout to clean up if no acknowledgment is received
        scheduleTimeoutForSchoolRoute(schoolId, routeId, students.size());
    }

    private void scheduleTimeoutForSchoolRoute(String schoolId, String routeId, int studentCount) {
        // Set a timeout based on the number of students (30 seconds per student + 10 seconds buffer)
        int timeoutSeconds = (studentCount * 30) + 10;

        scheduler.schedule(() -> {
            String schoolRouteKey = schoolId + "_" + routeId;
            StudentListState state = schoolRouteStates.get(schoolRouteKey);
            if (state != null) {
                System.err.println("⏰ Timeout reached for school: " + schoolId + " and route: " + routeId + " - cleaning up state");
                schoolRouteStates.remove(schoolRouteKey);
            }
        }, timeoutSeconds, TimeUnit.SECONDS);

        System.out.println("⏰ Scheduled timeout for school: " + schoolId + " and route: " + routeId +
                " in " + timeoutSeconds + " seconds");
    }

    private String extractDeviceIdFromStudentListRequest(String request) {
        try {
            // Remove prefix (1002) and suffix (StudentListAAAA)
            String content = request.substring(STUDENT_LIST_PREFIX.length(),
                    request.length() - STUDENT_LIST_SUFFIX.length());

            System.out.println("🔍 Raw content after removing prefix and suffix: '" + content + "'");
            System.out.println("🔍 Content length: " + content.length());

            // Extract device ID (first 10 characters after 1002)
            if (content.length() >= 10) {
                String deviceId = content.substring(0, 10);
                System.out.println("🔍 Extracted device ID: '" + deviceId + "'");
                return deviceId;
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

            // Device ID is 10 characters, so school ID starts at position 10
            if (content.length() >= 18) {
                // School ID is 8 characters (positions 10-18)
                String schoolId = content.substring(10, 18);
                System.out.println("🔍 Extracted school ID: '" + schoolId + "'");
                return schoolId;
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

            // Device ID is 10 characters, School ID is 8 characters, so Route ID starts at position 18
            if (content.length() >= 26) {
                // Route ID is 8 characters (positions 18-26)
                String routeId = content.substring(18, 26);
                System.out.println("🔍 Extracted route ID: '" + routeId + "'");
                return routeId;
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

            // For the current format, we're ignoring count
            // If you need count in the future, it would be after the route ID
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

            // 5. Get the latest GPS coordinates for this student from the swipe record
            Double latitude = parseDoubleSafely(latestSwipe.getLatitude());
            Double longitude = parseDoubleSafely(latestSwipe.getLongitude());

            if (latitude != null && longitude != null) {
                System.out.println("📍 Found GPS coordinates for student " + studentId +
                        " - Lat: " + latitude + ", Lon: " + longitude);

                // 6. Send GPS location data to frontend via WebSocket
                sendSwipeLocationToFrontend(schoolId, routeId, studentId, latitude, longitude, latestSwipe.getImageName());
            } else {
                System.out.println("⚠️ No GPS coordinates found for student " + studentId);
            }

            // 7. Construct FTP path: /upload/school_id/route_id/imagename.jpg
            String ftpPath = buildFtpPathFromSwipe(latestSwipe);
            if (ftpPath == null || ftpPath.isEmpty()) {
                System.err.println("❌ Failed to construct FTP path");
                return buildSwipeCardResponse(schoolId, routeId, studentId, "06");
            }

            // 8. Send to Python server and get response
            String pythonResponse = sendFtpPathToPythonServer(ftpPath, studentId, schoolId, routeId);
            if (pythonResponse == null) {
                System.err.println("❌ Python connection failed - sending error code 07");
                return buildSwipeCardResponse(schoolId, routeId, studentId, "07");
            }

            // 9. Map Python response to our format
            String responseCode = mapPythonResponseToCode(pythonResponse);
            return buildSwipeCardResponse(schoolId, routeId, studentId, responseCode);

        } catch (Exception e) {
            System.err.println("❌ Error processing swipe card request: " + request + " - " + e.getMessage());
            e.printStackTrace();
            return buildSwipeCardResponse(null, null, null, "06");
        }
    }

    private void sendSwipeLocationToFrontend(String schoolId, String routeId, String studentId,
                                             Double latitude, Double longitude, String imageName) {
        try {
            // Create swipe location data for frontend
            Map<String, Object> swipeData = new HashMap<>();
            swipeData.put("schoolId", schoolId);
            swipeData.put("routeId", routeId);
            swipeData.put("studentId", studentId);
            swipeData.put("latitude", latitude);
            swipeData.put("longitude", longitude);
            swipeData.put("imageName", imageName);
            swipeData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            swipeData.put("type", "student_swipe_location");

            // Convert to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonData = objectMapper.writeValueAsString(swipeData);

            // Send to general swipe topic
            simpMessagingTemplate.convertAndSend("/topic/student-swipes", jsonData);

            // Send to specific school and route topic
            String schoolRouteTopic = "/topic/swipes/" + schoolId + "/" + routeId;
            simpMessagingTemplate.convertAndSend(schoolRouteTopic, jsonData);

            System.out.println("📡 Sent swipe location to frontend - Student: " + studentId +
                    ", School: " + schoolId + ", Route: " + routeId +
                    ", Coordinates: " + latitude + ", " + longitude);
            System.out.println("📡 Topics: /topic/student-swipes and " + schoolRouteTopic);

        } catch (Exception e) {
            System.err.println("❌ Error sending swipe location to frontend: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Safely parse a String to Double, returning null if parsing fails
     */
    private Double parseDoubleSafely(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("❌ Error parsing coordinate value: '" + value + "' - " + e.getMessage());
            return null;
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
        if (swipe == null || swipe.getSchoolId() == null || swipe.getRouteId() == null || swipe.getImageName() == null) {
            return null;
        }
        // Format: /upload/{schoolId}/{routeId}/{imageName}
        return "/upload/" + swipe.getSchoolId() + "/" + swipe.getRouteId() + "/" + swipe.getImageName();
    }

    private String sendFtpPathToPythonServer(String ftpPath, String studentId, String schoolId, String routeId) {
        try {
            // Connect to Python server (adjust IP and port as needed)
            try (Socket pythonSocket = new Socket("68.178.203.99", 5006);
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

    // Helper class to track student list sending state for each device
    private static class StudentListState {
        private int currentIndex;
        private final List<Student> students;
        private final String deviceId;

        public StudentListState(List<Student> students, String deviceId) {
            this.students = students;
            this.deviceId = deviceId;
            this.currentIndex = 0;
        }

        public int getCurrentIndex() {
            return currentIndex;
        }

        public List<Student> getStudents() {
            return students;
        }

        public String getDeviceId() {
            return deviceId;
        }
    }

    private void processNextStudentForSchoolRoute(String schoolId, String routeId, String studentId, PrintWriter out) {
        try {
            // Create a key for school and route combination
            String schoolRouteKey = schoolId + "_" + routeId;

            System.out.println("🔍 Looking for state with key: '" + schoolRouteKey + "'");
            System.out.println("🔍 Available school-route keys: " + schoolRouteStates.keySet());
            System.out.println("🔍 Total states in map: " + schoolRouteStates.size());

            // Debug: Print each key-value pair
            for (Map.Entry<String, StudentListState> entry : schoolRouteStates.entrySet()) {
                System.out.println("🔍 State entry - Key: '" + entry.getKey() + "', Device: " + entry.getValue().getDeviceId() +
                        ", Index: " + entry.getValue().getCurrentIndex() + ", Students: " + entry.getValue().getStudents().size());
            }

            // Get the current state for this school and route
            StudentListState state = schoolRouteStates.get(schoolRouteKey);
            if (state == null) {
                System.err.println("❌ No state found for school: " + schoolId + " and route: " + routeId);
                System.err.println("❌ This means the acknowledgment was received but no student list request was processed first");
                return;
            }

            System.out.println("✅ Processing acknowledgment for student: " + studentId +
                    " in school: " + schoolId + " and route: " + routeId);
            System.out.println("✅ Current state - Index: " + state.currentIndex + ", Total students: " + state.students.size());

            // Find the route by sm_route_id
            Optional<Route> routeOpt = routeRepository.findBySmRouteId(routeId);
            if (routeOpt.isEmpty()) {
                System.err.println("❌ Route not found for acknowledgment: " + routeId);
                return;
            }

            Route route = routeOpt.get();
            List<Student> students = studentRepository.findAllByRoute_Id(route.getId());

            // Send the next student
            sendNextStudentForSchoolRoute(schoolId, routeId, students, state, out);

        } catch (Exception e) {
            System.err.println("❌ Error processing next student for school route: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendNextStudentForSchoolRoute(String schoolId, String routeId,
                                               List<Student> students, StudentListState state, PrintWriter out) {
        if (state.currentIndex >= students.size()) {
            // All students sent, send END marker
            String endResponse = "1002" + state.getDeviceId() + schoolId + routeId + "ENDAAAA";
            out.println(endResponse);
            System.out.println("📤 Sent END marker: " + endResponse);

            // Clean up the state
            String schoolRouteKey = schoolId + "_" + routeId;
            schoolRouteStates.remove(schoolRouteKey);
            System.out.println("✅ Completed student list sending for school: " + schoolId +
                    " and route: " + routeId + " - cleaned up state");
            return;
        }

        Student student = students.get(state.currentIndex);
        String studentResponse = "1002" + state.getDeviceId() + schoolId + routeId + student.getSmStudentId() + "AAAA";

        out.println(studentResponse);
        System.out.println("📤 Sent student " + (state.currentIndex + 1) + "/" + students.size() +
                " for school: " + schoolId + " and route: " + routeId + ": " + studentResponse);

        // Increment the index for next acknowledgment
        state.currentIndex++;

        System.out.println("⏳ Waiting for acknowledgment for next student: " +
                (state.currentIndex + 1) + "/" + students.size());
    }

    private void processGpsLocationMessage(String message, PrintWriter out) {
        try {
            System.out.println("📍 Processing GPS location message: " + message);

            // Extract GPS data from format: 1005BNG0000001AC1F0002RT7F0001LAT12.345678LON78.901234GPSAAAA
            String deviceId = extractDeviceIdFromGpsMessage(message);
            String schoolId = extractSchoolIdFromGpsMessage(message);
            String routeId = extractRouteIdFromGpsMessage(message);
            Double latitude = extractLatitudeFromGpsMessage(message);
            Double longitude = extractLongitudeFromGpsMessage(message);

            System.out.println("📍 Extracted GPS data - Device: " + deviceId +
                    ", School: " + schoolId +
                    ", Route: " + routeId +
                    ", Lat: " + latitude +
                    ", Lon: " + longitude);

            if (deviceId == null || schoolId == null || routeId == null ||
                    latitude == null || longitude == null) {
                System.err.println("❌ Invalid GPS location message format: " + message);
                return;
            }

            // Send GPS location to frontend via WebSocket
            sendGpsLocationToFrontend(deviceId, schoolId, routeId, latitude, longitude);

            // Send acknowledgment back to C++ client
            String ackResponse = "1005" + deviceId + schoolId + routeId + "ACKGPSAAAA";
            out.println(ackResponse);
            System.out.println("📤 Sent GPS acknowledgment: " + ackResponse);

        } catch (Exception e) {
            System.err.println("❌ Error processing GPS location message: " + message + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String extractDeviceIdFromGpsMessage(String message) {
        try {
            // Remove prefix (1005) and suffix (GPSAAAA)
            String content = message.substring(GPS_LOCATION_PREFIX.length(),
                    message.length() - GPS_LOCATION_SUFFIX.length());

            // Extract device ID (first 10 characters after 1005)
            if (content.length() >= 10) {
                String deviceId = content.substring(0, 10);
                System.out.println("🔍 Extracted device ID from GPS message: '" + deviceId + "'");
                return deviceId;
            }
        } catch (Exception e) {
            System.err.println("❌ Error extracting device ID from GPS message: " + e.getMessage());
        }
        return null;
    }

    private String extractSchoolIdFromGpsMessage(String message) {
        try {
            // Remove prefix (1005) and suffix (GPSAAAA)
            String content = message.substring(GPS_LOCATION_PREFIX.length(),
                    message.length() - GPS_LOCATION_SUFFIX.length());

            // Device ID is 10 characters, so school ID starts at position 10
            if (content.length() >= 18) {
                // School ID is 8 characters (positions 10-18)
                String schoolId = content.substring(10, 18);
                System.out.println("🔍 Extracted school ID from GPS message: '" + schoolId + "'");
                return schoolId;
            }
        } catch (Exception e) {
            System.err.println("❌ Error extracting school ID from GPS message: " + e.getMessage());
        }
        return null;
    }

    private String extractRouteIdFromGpsMessage(String message) {
        try {
            // Remove prefix (1005) and suffix (GPSAAAA)
            String content = message.substring(GPS_LOCATION_PREFIX.length(),
                    message.length() - GPS_LOCATION_SUFFIX.length());

            // Device ID is 10 characters, School ID is 8 characters, so Route ID starts at position 18
            if (content.length() >= 26) {
                // Route ID is 8 characters (positions 18-26)
                String routeId = content.substring(18, 26);
                System.out.println("🔍 Extracted route ID from GPS message: '" + routeId + "'");
                return routeId;
            }
        } catch (Exception e) {
            System.err.println("❌ Error extracting route ID from GPS message: " + e.getMessage());
        }
        return null;
    }

    private Double extractLatitudeFromGpsMessage(String message) {
        try {
            // Remove prefix (1005) and suffix (GPSAAAA)
            String content = message.substring(GPS_LOCATION_PREFIX.length(),
                    message.length() - GPS_LOCATION_SUFFIX.length());

            // Look for LAT pattern after route ID (positions 26+)
            if (content.length() >= 30) {
                int latStart = content.indexOf("LAT", 26);
                if (latStart != -1) {
                    int latEnd = content.indexOf("LON", latStart);
                    if (latEnd != -1) {
                        String latStr = content.substring(latStart + 3, latEnd);
                        Double latitude = Double.parseDouble(latStr);
                        System.out.println("🔍 Extracted latitude from GPS message: '" + latitude + "'");
                        return latitude;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error extracting latitude from GPS message: " + e.getMessage());
        }
        return null;
    }

    private Double extractLongitudeFromGpsMessage(String message) {
        try {
            // Remove prefix (1005) and suffix (GPSAAAA)
            String content = message.substring(GPS_LOCATION_PREFIX.length(),
                    message.length() - GPS_LOCATION_SUFFIX.length());

            // Look for LON pattern after latitude
            if (content.length() >= 30) {
                int lonStart = content.indexOf("LON");
                if (lonStart != -1) {
                    String lonStr = content.substring(lonStart + 3);
                    Double longitude = Double.parseDouble(lonStr);
                    System.out.println("🔍 Extracted longitude from GPS message: '" + longitude + "'");
                    return longitude;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error extracting longitude from GPS message: " + e.getMessage());
        }
        return null;
    }

    private void sendGpsLocationToFrontend(String deviceId, String schoolId, String routeId,
                                           Double latitude, Double longitude) {
        try {
            // Create GPS location data for frontend
            Map<String, Object> gpsData = new HashMap<>();
            gpsData.put("deviceId", deviceId);
            gpsData.put("schoolId", schoolId);
            gpsData.put("routeId", routeId);
            gpsData.put("latitude", latitude);
            gpsData.put("longitude", longitude);
            gpsData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            gpsData.put("type", "gps_location");

            // Convert to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonData = objectMapper.writeValueAsString(gpsData);

            // Send to general GPS topic
            simpMessagingTemplate.convertAndSend("/topic/gps-updates", jsonData);

            // Send to specific school and route topic
            String schoolRouteTopic = "/topic/gps/" + schoolId + "/" + routeId;
            simpMessagingTemplate.convertAndSend(schoolRouteTopic, jsonData);

            System.out.println("📡 Sent GPS location to frontend - Device: " + deviceId +
                    ", School: " + schoolId + ", Route: " + routeId +
                    ", Coordinates: " + latitude + ", " + longitude);
            System.out.println("📡 Topics: /topic/gps-updates and " + schoolRouteTopic);

        } catch (Exception e) {
            System.err.println("❌ Error sending GPS location to frontend: " + e.getMessage());
            e.printStackTrace();
        }
    }
}