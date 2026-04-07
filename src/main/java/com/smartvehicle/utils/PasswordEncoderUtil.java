package com.smartvehicle.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderUtil {
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static String encode(String rawString) {
        return passwordEncoder.encode(rawString);
    }

    public static void main(String[] args) {
        System.out.println(PasswordEncoderUtil.encode("Admin@123"));
    }
}
