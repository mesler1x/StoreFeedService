package ru.urfu.store.feed.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.urfu.store.feed.model.Comment;
import ru.urfu.store.feed.model.dto.*;
import ru.urfu.store.feed.service.FeedService;

import java.util.UUID;

@Tag(name = "Сервис взаимодействия с новостной лентой")
@RestController
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @Operation(summary = "Метод создание новостной публикации")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public FeedDto createFeed(@Valid @RequestBody CreateFeedRequest request) {
        return feedService.createFeed(request);
    }

    @Operation(summary = "Получение информации о новости по id")
    @GetMapping("/{feed_id}")
    public FeedDto getFeed(
            @PathVariable(name = "feed_id") UUID id
    ) {
        return feedService.getFeed(id);
    }

    @Operation(summary = "Получение всех новостей")
    @GetMapping
    public Paging<FeedDto> getAllFeeds(
            @RequestParam(name = "limit", required = false, defaultValue = "100")
            Integer limit,
            @RequestParam(name = "offset", required = false, defaultValue = "0")
            Integer offset) {
        return feedService.getAllFeeds(limit, offset);
    }

    @Operation(summary = "Обновить новость")
    @PutMapping("/{feed_id}")
    public FeedDto updateFeed(
            @PathVariable(name = "feed_id") UUID id,
            @Valid @RequestBody UpdateFeedRequest request) {
        return feedService.updateFeed(id, request);
    }

    @Operation(summary = "Удалить новость")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{feed_id}")
    public void deleteFeed(
            @PathVariable(name = "feed_id") UUID id
    ) {
        feedService.deleteFeed(id);
    }

    @Operation(summary = "Лайкнуть новость")
    @PostMapping("/{feed_id}/like")
    public void likeFeed(
            @PathVariable(name = "feed_id") UUID id,
            @RequestParam(name = "user_id") UUID userId) {
        feedService.likeFeed(id, userId);
    }

    @Operation(summary = "Удалить лайк новости")
    @PostMapping("/{feed_id}/unlike")
    public void unlikeFeed(
            @PathVariable(name = "feed_id") UUID id,
            @RequestParam(name = "user_id") UUID userId) {
        feedService.unlikeFeed(id, userId);
    }

    @Operation(summary = "Оставить комментарий к новости")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/comment")
    public Comment addComment(
            @Valid @RequestBody CommentRequest request) {
        return feedService.addComment(request);
    }
}