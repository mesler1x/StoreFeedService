package ru.urfu.store.feed.model;

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
public class Feed {
    private UUID id;
    private String title;
    private String text;

    @Builder.Default
    private Long likesCount = 0L;

    @Builder.Default
    private Long watchCount = 0L;

    @Builder.Default
    private Long commentsCount = 0L;

    @Builder.Default
    private ZonedDateTime created = ZonedDateTime.now();

    private ZonedDateTime updated;
}