package com.itm.space.backendresources.controller;
import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.service.UserService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "MODERATOR")

public class ResourcesTest extends BaseIntegrationTest {
    @Autowired
    private UserService userService;
    @Autowired
    private Keycloak keycloak;

    @Test
    @SneakyThrows
    void testCreateUser() {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
                post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "username": "User",
                                "firstName": "User",
                                "lastName": "User",
                                "email": "user@gmail.com",
                                "password": "12345"
                                }
                                """);
        mvc.perform(mockHttpServletRequestBuilder).andExpect(status().isOk());
    }

    @AfterEach
    @SneakyThrows
    void cleanBd() {
        List<UserRepresentation> userRepresentations
                = keycloak.realm("ITM").users().search("User");
        if (!(userRepresentations.isEmpty())) {
            UserRepresentation userRepresentation = userRepresentations.get(0);
            keycloak.realm("ITM").users().get(userRepresentation.getId()).remove();
        }
    }

    @Test
    @SneakyThrows
    void testCreateUserNegative() {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder
                = post("/api/users").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "username": "User",
                        "firstName": "User",
                        "lastName": "User",
                        "email": "usergmail.com",
                        "password": "12345"
                        }
                        """);
        mvc.perform(mockHttpServletRequestBuilder).andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void helloTest() {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder
                = get("/api/users/hello");
        mvc.perform(mockHttpServletRequestBuilder).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    @SneakyThrows
    void helloTestNegative() {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder
                = get("/api/users/hello");
        mvc.perform(mockHttpServletRequestBuilder).andExpect(status().is4xxClientError());

    }

    @Test
    @SneakyThrows
    void getUserById() {
        userService.createUser(new UserRequest("User", "user@gmail.com", "test", "User", "User"));
        String testUUID = keycloak.realm("ITM").users().search("User").get(0).getId();
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder
                = get("/api/users/" + testUUID);
        mvc.perform(mockHttpServletRequestBuilder)
                .andExpectAll(
                        status().isOk());
        keycloak.realm("ITM").users().get(testUUID).remove();
    }

    @Test
    @SneakyThrows
    void getUserByIdNegative() {
        String testUUID = String.valueOf(UUID.randomUUID());
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
                get("/api/users/" + testUUID);
        mvc.perform(mockHttpServletRequestBuilder).andExpect(status().is5xxServerError());
    }
}