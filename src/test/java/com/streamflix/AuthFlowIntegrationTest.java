package com.streamflix;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamflix.dto.request.LoginRequest;
import com.streamflix.dto.request.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerThenLogin_returnsValidTokens() throws Exception {
        RegisterRequest register = new RegisterRequest("Ada Lovelace", "ada@example.com", "supersecret1");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.email").value("ada@example.com"));

        LoginRequest login = new LoginRequest("ada@example.com", "supersecret1");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void register_rejectsDuplicateEmail() throws Exception {
        RegisterRequest register = new RegisterRequest("Grace Hopper", "grace@example.com", "supersecret1");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isConflict());
    }

    @Test
    void protectedEndpoint_rejectsMissingToken() throws Exception {
        mockMvc.perform(get("/api/watchlist"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicMovieSearch_isAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/movies/search").param("query", "ember"))
                .andExpect(status().isOk());
    }
}
