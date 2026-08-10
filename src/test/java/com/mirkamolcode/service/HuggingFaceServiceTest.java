package com.mirkamolcode.service;

import com.mirkamolcode.dto.AiTicketResponse;
import com.mirkamolcode.entity.enums.Category;
import com.mirkamolcode.entity.enums.Priority;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class HuggingFaceServiceTest {
    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec responseSpec;


    @InjectMocks
    private HuggingFaceService underTest;

    @Test
    void shouldReturnAiTicketResponseWhenMessageIsAnalyzedSuccessfully() {
        // given
        String content = "Our payment was charged twice";

        AiTicketResponse expectedResponse = new AiTicketResponse(
                true,
                "Duplicate payment detected",
                Category.BILLING,
                Priority.HIGH,
                "Customer reported duplicate billing charges."
        );

        given(chatClient.prompt())
                .willReturn(requestSpec);

        given(requestSpec.user(content))
                .willReturn(requestSpec);

        given(requestSpec.call())
                .willReturn(responseSpec);

        given(responseSpec.entity(AiTicketResponse.class))
                .willReturn(expectedResponse);

        // when
        AiTicketResponse result = underTest.analyzeMessage(content);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedResponse);
        assertThat(result.qualified()).isTrue();
        assertThat(result.category()).isEqualTo(Category.BILLING);
        assertThat(result.priority()).isEqualTo(Priority.HIGH);

        then(chatClient).should().prompt();
        then(requestSpec).should().user(content);
        then(requestSpec).should().call();
        then(responseSpec).should().entity(AiTicketResponse.class);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenAiClientFails() {
        // given
        String content = "API is down";

        given(chatClient.prompt())
                .willThrow(new RuntimeException("API failure"));


        // then
        assertThatThrownBy(() -> underTest.analyzeMessage(content))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to analyze Ai response")
                .hasCauseInstanceOf(RuntimeException.class);

        then(chatClient).should().prompt();
    }
}