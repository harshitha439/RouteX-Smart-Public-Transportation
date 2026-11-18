package com.routex;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private ResultActions postJson(String url, Object body, String token) throws Exception {
        MockHttpServletRequestBuilder req = post(url).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body));
        if (token != null) req.header("Authorization", "Bearer " + token);
        return mvc.perform(req);
    }

    private String tokenOf(ResultActions r) throws Exception {
        return json.readTree(r.andReturn().getResponse().getContentAsString()).get("token").asText();
    }

    private Map<String, String> reg(String email) {
        return Map.of("fullName", "Test User", "email", email, "phone", "9876543210", "password", "Passw0rd!");
    }

    @Test @Order(1)
    void registerPassenger_returnsTokenAndPassengerRole() throws Exception {
        postJson("/api/auth/register", reg("a@test.com"), null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("PASSENGER"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test @Order(2)
    void registerDuplicateEmail_returns409() throws Exception {
        postJson("/api/auth/register", reg("dup@test.com"), null).andExpect(status().isCreated());
        postJson("/api/auth/register", reg("dup@test.com"), null).andExpect(status().isConflict());
    }

    @Test @Order(3)
    void registerWithShortPassword_returns400() throws Exception {
        postJson("/api/auth/register", Map.of("fullName", "X", "email", "x@test.com", "password", "123"), null)
                .andExpect(status().isBadRequest());
    }

    @Test @Order(4)
    void loginValid_returnsToken() throws Exception {
        postJson("/api/auth/register", reg("login@test.com"), null);
        postJson("/api/auth/login", Map.of("email", "login@test.com", "password", "Passw0rd!"), null)
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test @Order(5)
    void loginInvalid_returns401() throws Exception {
        postJson("/api/auth/register", reg("bad@test.com"), null);
        postJson("/api/auth/login", Map.of("email", "bad@test.com", "password", "WRONG-pass"), null)
                .andExpect(status().isUnauthorized());
    }

    @Test @Order(6)
    void profileWithoutToken_returns401() throws Exception {
        mvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
    }

    @Test @Order(7)
    void profileWithToken_returnsOwnProfile() throws Exception {
        String t = tokenOf(postJson("/api/auth/register", reg("me@test.com"), null));
        mvc.perform(get("/api/users/me").header("Authorization", "Bearer " + t))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value("me@test.com"));
    }

    @Test @Order(8)
    void passengerCannotAccessAdminApi_returns403() throws Exception {
        String t = tokenOf(postJson("/api/auth/register", reg("p@test.com"), null));
        mvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + t))
                .andExpect(status().isForbidden());
    }

    @Test @Order(9)
    void adminCreatesDriver_andDriverCanLogin() throws Exception {
        String admin = tokenOf(postJson("/api/auth/login", Map.of("email", "admin@test.com", "password", "Admin@12345"), null));
        postJson("/api/admin/drivers", reg("driver@test.com"), admin)
                .andExpect(status().isCreated()).andExpect(jsonPath("$.role").value("DRIVER"));
        postJson("/api/auth/login", Map.of("email", "driver@test.com", "password", "Passw0rd!"), null)
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("DRIVER"));
    }

    @Test @Order(10)
    void changePassword_wrongCurrent_returns400_andCorrectWorks() throws Exception {
        String t = tokenOf(postJson("/api/auth/register", reg("pw@test.com"), null));
        mvc.perform(put("/api/users/me/password").header("Authorization", "Bearer " + t)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("currentPassword", "nope", "newPassword", "NewPassw0rd!"))))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/users/me/password").header("Authorization", "Bearer " + t)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("currentPassword", "Passw0rd!", "newPassword", "NewPassw0rd!"))))
                .andExpect(status().isOk());
        postJson("/api/auth/login", Map.of("email", "pw@test.com", "password", "NewPassw0rd!"), null).andExpect(status().isOk());
    }
}
