package com.smartvehicle.Service;


import com.smartvehicle.entity.ERole;
import com.smartvehicle.entity.Role;
import com.smartvehicle.entity.User;
import com.smartvehicle.payload.request.SignupRequest;
import com.smartvehicle.payload.response.MessageResponse;
import com.smartvehicle.repository.RoleRepository;
import com.smartvehicle.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder encoder;

    @Autowired
    RoleRepository roleRepository;

   @Transactional
    public User registerUser(SignupRequest request, String roleName , boolean isOtp){
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Error: Username is already taken!");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setTwoFactorEnabled(isOtp);
        user.setStatus(true);
        Set<Role> roles = new HashSet<>();

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Error: No role find with  "+roleName));
        roles.add(role);
        user.setRoles(roles);
        return userRepository.save(user);
    }
}
