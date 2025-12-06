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
public class Comment {
    private UUID id;
    private String text;
    private UUID userId;
    private UUID feedId;

    @Builder.Default
    private ZonedDateTime created = ZonedDateTime.now();

    private ZonedDateTime updated;
}
