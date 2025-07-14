package com.smartvehicle.service;

import org.springframework.stereotype.Service;

@Service
public class RouteSchlStudentMappingService {

    private static final int SHIFT = 3; // Shift value for encoding
    private static final String SEPARATOR = "#";

    private static String encodeString(String input) {
        StringBuilder encoded = new StringBuilder();
        for (char c : input.toCharArray()) {
            encoded.append((char) (c + SHIFT));
        }
        return encoded.toString();
    }

    private static String decodeString(String input) {
        StringBuilder decoded = new StringBuilder();
        for (char c : input.toCharArray()) {
            decoded.append((char) (c - SHIFT));
        }
        return decoded.toString();
    }

    public static String encodeRoute(String routeId, String schoolId, String city, String studentId) {
        String encRoute = encodeString(routeId);
        String encSchool = encodeString(schoolId);
        String encCity = encodeString(city);
        String encStudent = encodeString(studentId);

        return String.join(SEPARATOR, encRoute, encSchool, encCity, encStudent);
    }

    public static String[] decodeRoute(String encoded) throws IllegalArgumentException {
        String[] parts = encoded.split(SEPARATOR);
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid encoded format: must contain 4 parts separated by " + SEPARATOR);
        }

        String routeId = decodeString(parts[0]);
        String schoolId = decodeString(parts[1]);
        String city = decodeString(parts[2]);
        String studentId = decodeString(parts[3]);

        return new String[]{routeId, schoolId, city, studentId};
    }
}
