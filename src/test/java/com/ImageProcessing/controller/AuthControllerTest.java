package com.ImageProcessing.controller;

import com.ImageProcessing.dto.AuthenticationRequest;
import com.ImageProcessing.dto.RegisterRequest;
import com.ImageProcessing.dto.SearchRequest;
import com.ImageProcessing.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AuthControllerTest extends BaseIntegrationTest{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("[200] POST /api/v1/auth/register - Register User")
    void registerUser() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("mockUsername");
        registerRequest.setEmail("mockEmail@gmail.com");
        registerRequest.setPassword("mockPassword");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                        .content(objectMapper.writeValueAsString(registerRequest))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value(Matchers
                        .matchesPattern("^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$")))
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.access_token").isNotEmpty());
    }

    @Test
    @DisplayName("[400] POST /api/v1/auth/register - Register User Email Already Exists")
    void registerUserShouldReturnEmailAlreadyExists() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testUsername");
        registerRequest.setEmail("test@gmail.com");
        registerRequest.setPassword("testPassword");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                        .content(objectMapper.writeValueAsString(registerRequest))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", Matchers.is("Email already exists")));
    }

    @Test
    @DisplayName("[200] POST /api/v1/auth/authenticate - Authenticate User")
    void authenticateUser() throws Exception {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setEmail("test@gmail.com");
        authenticationRequest.setPassword("mypassword");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/authenticate")
                        .content(objectMapper.writeValueAsString(authenticationRequest))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value(Matchers
                        .matchesPattern("^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$")))
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.access_token").isNotEmpty());
    }

    @Test
    @DisplayName("[400] POST /api/v1/auth/authenticate - Authenticate User Password is Null")
    void authenticateUserShouldReturnPasswordNotNull() throws Exception {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setEmail("test@gmail.com");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/authenticate")
                        .content(objectMapper.writeValueAsString(authenticationRequest))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password", Matchers.is("must not be null")));

    }
}
