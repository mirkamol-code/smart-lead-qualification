package com.mirkamolcode;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.profiles.active=local",
                "app.admin.username=admin123",
                "app.admin.password=password"
        })
@ActiveProfiles("local")
@AutoConfigureWebTestClient
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractTestConfig {
    @Container
    @ServiceConnection
    private static final SharedPostgresContainer POSTGRES =
            SharedPostgresContainer.getInstance();
}
