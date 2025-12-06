package ru.urfu.store.feed.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedDto {
    private UUID id;
    private String title;
    private String text;
    private Long likesCount;
    private Long watchCount;
    private Long commentsCount;
    private ZonedDateTime created;
    private ZonedDateTime updated;
}