package br.ifsp.demo.controller;

import br.ifsp.demo.security.auth.AuthRequest;
import br.ifsp.demo.security.auth.AuthResponse;
import br.ifsp.demo.security.user.JpaUserRepository;
import br.ifsp.demo.security.user.User;
import br.ifsp.demo.shared.EntityBuilder;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

import static io.restassured.RestAssured.baseURI;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseApiIntegrationTest {

    @LocalServerPort protected int port = 8080;
    @Autowired
    protected JpaUserRepository userRepository;

    @BeforeEach
    void generalSetup() {
        RestAssured.baseURI = "http://localhost:8080";
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    protected User registerUser(String password) {
        final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        User user = EntityBuilder.createRandomUser(encoder.encode(password));
        userRepository.save(user);
        return user;
    }

    protected String authenticate(String username, String password) {
        RestTemplate restTemplate = new RestTemplate();
        AuthRequest authRequest = new AuthRequest(username, password);
        final AuthResponse response = restTemplate.postForObject(baseURI + "/api/v1/authenticate", authRequest, AuthResponse.class);
        return response.token();
    }
}