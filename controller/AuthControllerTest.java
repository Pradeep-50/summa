//package com.nexbus.nexbus_backend.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.nexbus.nexbus_backend.dto.LoginRequest;
//import com.nexbus.nexbus_backend.dto.LoginResponse;
//import com.nexbus.nexbus_backend.dto.RegisterRequest;
//import com.nexbus.nexbus_backend.dto.UserDTO;
//import com.nexbus.nexbus_backend.service.AuthService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(AuthController.class)
//class AuthControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private AuthService authService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    private RegisterRequest registerRequest;
//    private LoginRequest loginRequest;
//    private UserDTO userDTO;
//    private LoginResponse loginResponse;
//
//    @BeforeEach
//    void setUp() {
//        registerRequest = new RegisterRequest();
//        registerRequest.setEmail("test@example.com");
//        registerRequest.setPassword("password");
//        registerRequest.setRoleName("CUSTOMER");
//
//        loginRequest = new LoginRequest();
//        loginRequest.setEmail("test@example.com");
//        loginRequest.setPassword("password");
//
//        userDTO = new UserDTO();
//        userDTO.setUserId(1);
//        userDTO.setEmail("test@example.com");
//        userDTO.setRoleName("CUSTOMER");
//
//        loginResponse = new LoginResponse();
//        loginResponse.setToken("jwt-token");
//        loginResponse.setUserId(1);
//    }
//
//    @Test
//    void register_success() throws Exception {
//        when(authService.register(any(RegisterRequest.class))).thenReturn(userDTO);
//
//        mockMvc.perform(post("/auth/register")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(registerRequest)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.userId").value(1))
//                .andExpect(jsonPath("$.email").value("test@example.com"));
//    }
//
//    @Test
//    void login_success() throws Exception {
//        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);
//
//        mockMvc.perform(post("/auth/login")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(loginRequest)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.token").value("jwt-token"));
//    }
//}