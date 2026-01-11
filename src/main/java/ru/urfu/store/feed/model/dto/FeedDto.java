package ru.urfu.store.feed.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.urfu.store.feed.model.Comment;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedDto {
    private UUID id;
    private String title;
    private String text;
    private List<Comment> comments;
    private Long likesCount;
    private Long watchCount;
    private Long commentsCount;
    private ZonedDateTime created;
    private ZonedDateTime updated;
}