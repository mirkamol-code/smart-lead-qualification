package com.mirkamolcode.event;

import java.util.UUID;

public record CommentCreatedEvent(
        UUID commentId,
        String content
) {
}
