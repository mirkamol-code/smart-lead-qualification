package com.mirkamolcode.service;

import com.mirkamolcode.dto.AiTicketResponse;
import com.mirkamolcode.dto.CommentResponse;
import com.mirkamolcode.dto.CreateCommentRequest;
import com.mirkamolcode.entity.ApplicationUser;
import com.mirkamolcode.entity.Comment;
import com.mirkamolcode.entity.Ticket;
import com.mirkamolcode.entity.enums.Category;
import com.mirkamolcode.entity.enums.Priority;
import com.mirkamolcode.exception.ResourceNotFoundException;
import com.mirkamolcode.repository.ApplicationUserRepository;
import com.mirkamolcode.repository.CommentRepository;
import com.mirkamolcode.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private HuggingFaceService huggingFaceService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ApplicationUserRepository userRepository;

    @InjectMocks
    private CommentService underTest;

    private ApplicationUser mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new ApplicationUser("user123", "password", Set.of());
    }

    @Test
    void shouldSaveCommentAndCreateTicketWhenQualified() {
        // given
        CreateCommentRequest commentRequest = new CreateCommentRequest("atbc@gmail.com", "Payment system is broken");
        Comment commentAboutBilling = new Comment();
        commentAboutBilling.setUser(mockUser);
        commentAboutBilling.setSenderEmail(commentRequest.senderEmail());
        commentAboutBilling.setContent(commentRequest.content());

        AiTicketResponse aiResponse = new AiTicketResponse(
                true,
                "Payment Failure",
                Category.BILLING,
                Priority.HIGH,
                "Customer can't complete payment"
        );

        given(userRepository.findApplicationUserByUsername("user123")).willReturn(Optional.of(mockUser));
        given(commentRepository.save(any(Comment.class))).willReturn(commentAboutBilling);
        given(huggingFaceService.analyzeMessage(commentAboutBilling.getContent())).willReturn(aiResponse);

        // when
        CommentResponse actual = underTest.save(commentRequest, "user123");

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.senderEmail()).isEqualTo(commentRequest.senderEmail());
        assertThat(actual.content()).isEqualTo(commentRequest.content());
        verify(commentRepository).save(any(Comment.class));
        verify(huggingFaceService).analyzeMessage(commentAboutBilling.getContent());
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void shouldSaveCommentWithoutCreatingTicketWhenNotQualified() {
        // given
        CreateCommentRequest commentRequest = new CreateCommentRequest("atbc@gmail.com", "Hello, how are you?");
        Comment commentAboutBilling = new Comment();
        commentAboutBilling.setUser(mockUser);
        commentAboutBilling.setSenderEmail(commentRequest.senderEmail());
        commentAboutBilling.setContent(commentRequest.content());

        AiTicketResponse aiResponse = new AiTicketResponse(
                false,
                null,
                null,
                null,
                null
        );

        given(userRepository.findApplicationUserByUsername("user123")).willReturn(Optional.of(mockUser));
        given(commentRepository.save(any(Comment.class))).willReturn(commentAboutBilling);
        given(huggingFaceService.analyzeMessage(commentAboutBilling.getContent())).willReturn(aiResponse);

        // when
        CommentResponse actual = underTest.save(commentRequest, "user123");

        // then
        assertThat(actual).isNotNull();
        verify(commentRepository).save(any(Comment.class));
        verify(huggingFaceService).analyzeMessage(commentAboutBilling.getContent());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void shouldGetMyComments() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Comment comment1 = new Comment("atbc@gmail.com", "I paid my subscription twice a month");
        Comment comment2 = new Comment("bdc@gmail.com", "I have difficulty with login");
        Page<Comment> expectedPage = new PageImpl<>(List.of(comment1, comment2));

        given(commentRepository.findAllByUserUsername("user123", pageable)).willReturn(expectedPage);

        // when
        Page<CommentResponse> actual = underTest.getMyComments("user123", pageable);

        // then
        assertThat(actual.getContent()).hasSize(2);
        assertThat(actual.getContent().getFirst().senderEmail()).isEqualTo("atbc@gmail.com");
    }

    @Test
    void shouldGetAllCommentsForAdmin() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Comment comment1 = new Comment("atbc@gmail.com", "I paid my subscription twice a month");
        Comment comment2 = new Comment("bdc@gmail.com", "I have difficulty with login");
        Page<Comment> expectedPage = new PageImpl<>(List.of(comment1, comment2));

        given(commentRepository.findAll(pageable)).willReturn(expectedPage);

        // when
        Page<CommentResponse> actual = underTest.getAll(pageable);

        // then
        assertThat(actual.getContent()).hasSize(2);
    }

    @Test
    void shouldGetCommentByIdForOwner() {
        // given
        UUID commentId = UUID.randomUUID();
        Comment comment = new Comment("atbc@gmail.com", "my comment");
        UserDetails userDetails = new User("user123", "password", List.of(new SimpleGrantedAuthority("own comment:read")));

        given(commentRepository.findByIdAndUserUsername(commentId, "user123")).willReturn(Optional.of(comment));

        // when
        CommentResponse actual = underTest.getOwnCommentById(commentId, userDetails);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.senderEmail()).isEqualTo("atbc@gmail.com");
    }

    @Test
    void shouldGetCommentByIdForAdmin() {
        // given
        UUID commentId = UUID.randomUUID();
        Comment comment = new Comment("someone@gmail.com", "user comment");

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        CommentResponse actual = underTest.getAnyCommentById(commentId);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.senderEmail()).isEqualTo("someone@gmail.com");
    }

    @Test
    void shouldThrowWhenCommentNotFoundForUser() {
        // given
        UUID commentId = UUID.randomUUID();
        UserDetails userDetails = new User("user123", "password", List.of(new SimpleGrantedAuthority("own comment:read")));

        given(commentRepository.findByIdAndUserUsername(commentId, "user123")).willReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> underTest.getOwnCommentById(commentId, userDetails))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Comment not found");
    }

    @Test
    void shouldThrowWhenCommentNotFoundForAny() {
        // given
        UUID commentId = UUID.randomUUID();
        given(commentRepository.findById(commentId)).willReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> underTest.getAnyCommentById(commentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Comment not found");
    }
}