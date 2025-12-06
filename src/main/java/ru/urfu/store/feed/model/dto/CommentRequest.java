package ru.urfu.store.feed.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {

    @NotBlank(message = "Comment text is required")
    private String text;

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Feed ID is required")
    private UUID feedId;
}
