package com.smartvehicle.security.jwt;

public class CustomRequestContextHolder {
    private static final ThreadLocal<String> authTokenThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> deviceTypeThreadLocal = new ThreadLocal<>();

    public static void setAuthToken(String token) {
        authTokenThreadLocal.set(token);
    }

    public static String getAuthToken() {
        return authTokenThreadLocal.get();
    }

    public static void setDeviceType(String deviceType) {
        deviceTypeThreadLocal.set(deviceType);
    }

    public static String getDeviceType() {
        return deviceTypeThreadLocal.get();
    }
    public static void clear() {
        authTokenThreadLocal.remove();
        deviceTypeThreadLocal.remove();
    }
}
