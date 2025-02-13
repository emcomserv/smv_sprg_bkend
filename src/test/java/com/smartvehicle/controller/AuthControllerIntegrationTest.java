package com.smartvehicle.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartvehicle.entity.*;
import com.smartvehicle.payload.request.LoginRequest;
import com.smartvehicle.payload.request.ParentSignupReq;
import com.smartvehicle.payload.request.ResetPasswordRequest;
import com.smartvehicle.payload.response.JwtResponse;
import com.smartvehicle.repository.*;
import com.smartvehicle.security.services.TwilioVerificationService;
import com.smartvehicle.security.services.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ParentRepository parentRepository;
    @Autowired
    private SchoolRepository schoolRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private TwilioVerificationService twilioVerificationService;
    @Autowired
    private AuthController authController;

    private String superAdminToken;
    @BeforeEach
    void setUp() {
        // Clear the database before each test
        schoolRepository.deleteAll();
        parentRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        School school = new School();
        school.setId("SCH001");
        school.setName("Test School 1");
        schoolRepository.save(school);

        // Create a role for testing
        Role superAdminRole = new Role();
        superAdminRole.setName("SUPERADMIN");
        superAdminRole.setSchId("SCH001");
        superAdminRole.setId(1L);
        roleRepository.save(superAdminRole);

        User superAdminUser = new User();
        superAdminUser.setUsername("school_admin");
        superAdminUser.setEmail("test@test.com");
        superAdminUser.setPhone("123456789");
        superAdminUser.setTwoFactorEnabled(false);
        superAdminUser.setPassword(passwordEncoder.encode("password"));
        superAdminUser.setRoles(List.of(roleRepository.findByName("SUPERADMIN").get()));
        userRepository.save(superAdminUser);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserName("school_admin");
        loginRequest.setPassword("password");
        ResponseEntity<?> response = authController.authenticateUser(loginRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof JwtResponse);
        JwtResponse jwtResponse = (JwtResponse) response.getBody();
        superAdminToken = jwtResponse.getToken();

        Role parentRole = new Role();
        parentRole.setName("PARENT");
        parentRole.setSchId("SCH001");
        parentRole.setId(2L);
        roleRepository.save(parentRole);
    }

    @Test
    void testRegisterParent_Success() throws Exception {
        // Arrange
        ParentSignupReq parentSignupReq = createParentSignupRequest();
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/parent/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + superAdminToken)
                        .content(new ObjectMapper().writeValueAsString(parentSignupReq)))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.username").value("parent_user"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.phone").value("1234567890"));
        // Verify the parent was saved in the database
        Optional<User> savedUser = userRepository.findByUsername("parent_user");
        assertTrue(savedUser.isPresent());
        Parent savedParent = parentRepository.findByUser_Id(savedUser.get().getId());
//        assertTrue(savedParent.isPresent());
        assertEquals("John", savedParent.getFirstName());
        assertEquals("Doe", savedParent.getLastName());
        assertEquals("SCH001", savedParent.getSchool().getId());
    }

    @Test
    void testAuthenticateUser_Success() throws Exception {
        // Arrange
        ParentSignupReq parentSignupReq = createParentSignupRequest();
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserName("parent_user");
        loginRequest.setPassword("password");

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + superAdminToken) // Add JWT token to the header
                        .content(new ObjectMapper().writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("parent_user"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists());
    }

    @Test
    void testAuthenticateUser_InvalidCredentials() throws Exception {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRoles(List.of(roleRepository.findByName("PARENT").get()));
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserName("testuser");
        loginRequest.setPassword("wrongpassword");

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + superAdminToken)
                        .content(new ObjectMapper().writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testResetPassword_Success() throws Exception {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("oldPassword"));
        user.setRoles(List.of(roleRepository.findByName("ROLE_USER").get()));
        userRepository.save(user);

        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setPassword("newPassword");

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/resetpassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(resetPasswordRequest))
                        .with(request -> {
                            request.setAttribute("userId", user.getId());
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("true"));

        // Verify the password was updated
        Optional<User> updatedUser = userRepository.findById(user.getId());
        assertTrue(updatedUser.isPresent());
        assertTrue(passwordEncoder.matches("newPassword", updatedUser.get().getPassword()));
    }

    @Test
    void testResetPassword_UserNotFound() throws Exception {
        // Arrange
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setPassword("newPassword");

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/resetpassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(resetPasswordRequest))
                        .with(request -> {
                            request.setAttribute("userId", 999L); // Non-existent user ID
                            return request;
                        }))
                .andExpect(status().isBadRequest());
    }

    private ParentSignupReq createParentSignupRequest() {
        ParentSignupReq request = new ParentSignupReq();
        request.setUsername("parent_user");
        request.setPassword(passwordEncoder.encode("password"));
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("parent_email@test.com");
        request.setPhone("1234567890");
        request.setCountryCode("+1");
        request.setSchoolId("SCH001");
        return request;
    }
}
