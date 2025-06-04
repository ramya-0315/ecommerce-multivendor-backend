package com.ramyastore.controller;

import com.ramyastore.exception.UserException;
import com.ramyastore.model.User;
import com.ramyastore.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;

    private final String AUTH_HEADER = "Bearer test.jwt.token";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getUserProfileHandler_returnsUserProfile() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setFullName("John Doe");
        // set other User fields if needed

        when(userService.findUserProfileByJwt(AUTH_HEADER)).thenReturn(user);

        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(userService, times(1)).findUserProfileByJwt(AUTH_HEADER);
    }
}
