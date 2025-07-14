package com.smartvehicle.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartvehicle.config.FirebaseConfig;
import com.smartvehicle.entity.Role;
import com.smartvehicle.entity.School;
import com.smartvehicle.entity.User;
import com.smartvehicle.payload.request.ParentSignupReq;
import com.smartvehicle.repository.ParentRepository;
import com.smartvehicle.repository.RoleRepository;
import com.smartvehicle.repository.SchoolRepository;
import com.smartvehicle.repository.UserRepository;
import com.smartvehicle.security.jwt.CustomRequestContextHolder;
import com.smartvehicle.security.services.TwilioVerificationService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
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

    @MockBean
    private FirebaseConfig firebaseConfig;

    @BeforeEach
    void setUp() {
        // Create test school with all required fields
        School school = new School();
        school.setId("SCH001");
        school.setName("Test School");
        school.setCountryId("001");
        school.setProvId("001");
        school.setAreaId("001");
        school.setEntityId("001");
        school.setContactNum("1234567890");
        school.setContactName("Test Contact");
        school.setStatus(true);
        school.setCreatedAt(LocalDateTime.now());
        school.setUpdatedAt(LocalDateTime.now());
        schoolRepository.save(school);

        // Create test roles
        Role superAdminRole = new Role();
        superAdminRole.setName("SUPERADMIN");
        superAdminRole.setSchId("SCH001");
        roleRepository.save(superAdminRole);

        Role parentRole = new Role();
        parentRole.setName("PARENT");
        parentRole.setSchId("SCH001");
        roleRepository.save(parentRole);

        // Create test user
        User superAdminUser = new User();
        superAdminUser.setUsername("school_admin");
        superAdminUser.setEmail("test@test.com");
        superAdminUser.setPhone("123456789");
        superAdminUser.setTwoFactorEnabled(false);
        superAdminUser.setPassword(passwordEncoder.encode("password"));
        superAdminUser.setRoles(List.of(superAdminRole));
        userRepository.save(superAdminUser);
    }

    @Test
    void testRegisterParent_Success() throws Exception {
        try (MockedStatic<CustomRequestContextHolder> utilities = Mockito.mockStatic(CustomRequestContextHolder.class)) {
            utilities.when(CustomRequestContextHolder::getDeviceType).thenReturn("web");

            ParentSignupReq parentSignupReq = createParentSignupRequest();

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/parent/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer ")
                            .header("device-type", "web")
                            .content(new ObjectMapper().writeValueAsString(parentSignupReq)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.username").value("parent_user"))
                    .andExpect(jsonPath("$.data.phone").value("1234567890"));
        }
    }

    private ParentSignupReq createParentSignupRequest() {
        ParentSignupReq request = new ParentSignupReq();
        request.setUsername("parent_user");
        request.setPassword("password");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("parent_email@test.com");
        request.setPhone("1234567890");
        request.setCountryCode("+1");
        request.setSchoolId("SCH001");
        return request;
    }

    @Test
    void testSchoolSchema() {
        School school = schoolRepository.findById("SCH001").orElseThrow();
        assertNotNull(school);
        assertEquals("Test School", school.getName());
    }
}