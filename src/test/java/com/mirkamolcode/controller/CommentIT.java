package com.mirkamolcode.controller;

import com.mirkamolcode.AbstractTestConfig;
import com.mirkamolcode.dto.AiTicketResponse;
import com.mirkamolcode.dto.ApiResponse;
import com.mirkamolcode.dto.CommentResponse;
import com.mirkamolcode.dto.CreateCommentRequest;
import com.mirkamolcode.service.HuggingFaceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

public class CommentIT extends AbstractTestConfig {
    private static final String PRODUCT_BASE_URL = "/messages";

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private HuggingFaceService huggingFaceService;

    @Test
    void canCreateComment() {
        given(huggingFaceService.analyzeMessage(anyString()))
                .willReturn(new AiTicketResponse(false, null, null, null, null));

        createComment(
                new CreateCommentRequest(
                        "test@gmail.com",
                        "I am having issue with payment"
                )
        );
    }

    @Test
    void canGetMyComments() {
        given(huggingFaceService.analyzeMessage(anyString()))
                .willReturn(new AiTicketResponse(false, null, null, null, null));

        CreateCommentRequest commentRequest = new CreateCommentRequest(
                "user123@gmail.com",
                "I am having issue with payment"
        );
        createComment(commentRequest);

        webTestClient.get()
                .uri(PRODUCT_BASE_URL + "/my")
                .headers(h -> h.setBasicAuth("user123", "password"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.body.content").isArray()
                .jsonPath("$.body.content.length()").isNotEmpty();
    }

    @Test
    void canGetAllCommentsAsAdmin() {
        given(huggingFaceService.analyzeMessage(anyString()))
                .willReturn(new AiTicketResponse(false, null, null, null, null));

        CreateCommentRequest commentRequest1 = new CreateCommentRequest(
                "test1@gmail.com",
                "I am having issue with payment"
        );
        createComment(commentRequest1);

        webTestClient.get()
                .uri(PRODUCT_BASE_URL)
                .headers(h -> h.setBasicAuth("admin123", "password"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.body.content").isArray();
    }

    @Test
    void canGetOwnCommentById() {
        given(huggingFaceService.analyzeMessage(anyString()))
                .willReturn(new AiTicketResponse(false, null, null, null, null));

        CommentResponse created = createComment(
                new CreateCommentRequest(
                        "user123@gmail.com",
                        "My specific comment for getById"
                )
        );

        webTestClient.get()
                .uri(PRODUCT_BASE_URL + "/{id}", created.id())
                .headers(h -> h.setBasicAuth("user123", "password"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.body.id").isEqualTo(created.id().toString())
                .jsonPath("$.body.content").isEqualTo(created.content());
    }

    @Test
    void canGetAnyCommentByIdAsAdmin() {
        given(huggingFaceService.analyzeMessage(anyString()))
                .willReturn(new AiTicketResponse(false, null, null, null, null));

        CommentResponse created = createComment(
                new CreateCommentRequest(
                        "user123@gmail.com",
                        "Comment to be fetched by admin"
                )
        );

        webTestClient.get()
                .uri(PRODUCT_BASE_URL + "/all/{id}", created.id())
                .headers(h -> h.setBasicAuth("admin123", "password"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.body.id").isEqualTo(created.id().toString())
                .jsonPath("$.body.content").isEqualTo(created.content());
    }

    @Test
    void regularUserCannotAccessAllEndpoint() {
        webTestClient.get()
                .uri(PRODUCT_BASE_URL + "/all/{id}", UUID.randomUUID())
                .headers(h -> h.setBasicAuth("user123", "password"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void shouldReturn404WhenCommentNotFound() {
        webTestClient.get()
                .uri(PRODUCT_BASE_URL + "/{id}", UUID.randomUUID())
                .headers(h -> h.setBasicAuth("user123", "password"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    private CommentResponse createComment(CreateCommentRequest request) {
        ApiResponse<CommentResponse> response = webTestClient.post()
                .uri(PRODUCT_BASE_URL)
                .headers(h -> h.setBasicAuth("user123", "password"))
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ApiResponse<CommentResponse>>() {
                })
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }
}
