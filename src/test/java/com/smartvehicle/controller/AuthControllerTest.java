package com.smartvehicle.controller;

import com.smartvehicle.entity.*;
import com.smartvehicle.payload.request.LoginRequest;
import com.smartvehicle.payload.request.ResetPasswordRequest;
import com.smartvehicle.payload.response.JwtResponse;
import com.smartvehicle.repository.*;
import com.smartvehicle.security.jwt.JwtUtils;
import com.smartvehicle.security.services.TwilioVerificationService;
import com.smartvehicle.security.services.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private TwilioVerificationService twilioVerificationService;

    @Mock
    private ParentRepository parentRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private AttenderRepository attenderRepository;

    @Mock
    private AdminRepository adminRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAuthenticateUser_Success() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserName("ashok");
        loginRequest.setPassword("password");

        User user = new User();
        user.setId(4L);
        user.setUsername("ashok");
        user.setPassword("encodedPassword");
        user.setTwoFactorEnabled(false);
        Role role=new Role();
        role.setId(1L);
        role.setName("PARENT");
        role.setSchId("SHC0001");
        user.setRoles(Collections.singleton(role));
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role.getName()));

        Parent parent =new Parent();
        parent.setId(2L);
        parent.setUser(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(user.getId(),
                user.getUsername(), user.getPassword(),authorities,user.getPhone());

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("ashok")).thenReturn(Optional.of(user));
        when(parentRepository.findByUser_Id(user.getId())).thenReturn(parent);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("jwtToken");

        // Act
        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof JwtResponse);

        JwtResponse jwtResponse = (JwtResponse) response.getBody();
        assertEquals("jwtToken", jwtResponse.getToken());
        assertEquals("ashok", jwtResponse.getUsername());

    }

    @Test
    void testAuthenticateUser_UserNotFound() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserName("nonexistentuser");
        loginRequest.setPassword("password");

        when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authController.authenticateUser(loginRequest));
    }

    @Test
    void testResetPassword_Success() {
        // Arrange
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setPassword("newPassword");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getDetails()).thenReturn(Collections.singletonMap("userId", 1L));

        User user = new User();
        user.setId(1L);
        user.setPassword("oldPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        // Act
        ResponseEntity<?> response = authController.getStudents(authentication, resetPasswordRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
        verify(userRepository, times(1)).save(user);
        assertEquals("encodedNewPassword", user.getPassword());
    }

    @Test
    void testResetPassword_Unauthorized() {
        // Arrange
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setPassword("newPassword");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getDetails()).thenReturn(null); // Simulate unauthorized user

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authController.getStudents(authentication, resetPasswordRequest));
    }

    @Test
    void testResetPassword_UserNotFound() {
        // Arrange
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setPassword("newPassword");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getDetails()).thenReturn(Collections.singletonMap("userId", 1L));

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authController.getStudents(authentication, resetPasswordRequest));
    }
}