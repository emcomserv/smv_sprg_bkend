package com.smart_vehicle.security.jwt;

public class AuthTokenHolder {
    private static final ThreadLocal<String> authTokenThreadLocal = new ThreadLocal<>();

    public static void setAuthToken(String token) {
        authTokenThreadLocal.set(token);
    }

    public static String getAuthToken() {
        return authTokenThreadLocal.get();
    }

    public static void clear() {
        authTokenThreadLocal.remove();
    }
}
