package com.smart_vehicle.security;


import com.smart_vehicle.security.services.TwilioVerificationService;
import com.twilio.Twilio;
import com.twilio.base.ResourceSet;
import com.twilio.rest.verify.v2.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.smart_vehicle.security.jwt.AuthEntryPointJwt;
import com.smart_vehicle.security.jwt.AuthTokenFilter;
import com.smart_vehicle.security.services.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
public class SecurityConfig  {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.verification.service.name}")
    private String serviceName;

    @Autowired
    UserDetailsServiceImpl userDetailsServiceImpl;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }


    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsServiceImpl);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                                auth.requestMatchers("/smartVehicle/api/auth/**").permitAll()
                                        .requestMatchers("/smartVehicle/parent/**").hasAuthority("PARENT") // Parent role
                                        .requestMatchers("/smartVehicle/admin/**").hasAuthority("ADMIN") // Admin role
                                        .anyRequest()
                                        .permitAll()
//				      .authenticated()
                );

        http.authenticationProvider(authenticationProvider());
        http.csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable())
                .headers(httpSecurityHeadersConfigurer -> {
                    httpSecurityHeadersConfigurer.frameOptions(frameOptionsConfig -> {
                        frameOptionsConfig.disable();
                    });
                });
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        // http.headers().frameOptions().disable();
        return http.build();

    }

    @Bean
    public Service twilioService() {

        Twilio.init(accountSid, authToken);
        ResourceSet<Service> services = Service.reader().read();
        for (Service service : services) {
            if (service.getFriendlyName().equals(serviceName)){
                System.out.println("Service found: " + serviceName + " with SID: " + service.getSid());
                return Service.fetcher(service.getSid()).fetch();
            }
        }


        // Create a new service if it doesn't exist
        Service newService = Service.creator(serviceName).create();
        String serviceSid = newService.getSid();
        System.out.println("Service created: " + serviceName + " with SID: " + serviceSid);
        return Service.fetcher(serviceSid).fetch();
    }

}