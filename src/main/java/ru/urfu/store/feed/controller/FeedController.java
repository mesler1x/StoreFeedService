package ru.urfu.store.feed.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.urfu.store.feed.model.Comment;
import ru.urfu.store.feed.model.dto.*;
import ru.urfu.store.feed.service.FeedService;

import java.util.UUID;

@Tag(name = "Сервис взаимодействия с новостной лентов")
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
            @Parameter(name = "id новости")
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
            @Parameter(name = "id новости")
            @PathVariable(name = "feed_id") UUID id,
            @Valid @RequestBody UpdateFeedRequest request) {
        return feedService.updateFeed(id, request);
    }

    @Operation(summary = "Удалить новость")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{feed_id}")
    public void deleteFeed(
            @Parameter(name = "id новости")
            @PathVariable(name = "feed_id") UUID id
    ) {
        feedService.deleteFeed(id);
    }

    @Operation(summary = "Лайкнуть новость")
    @PostMapping("/{feed_id}/like")
    public void likeFeed(
            @Parameter(name = "id новости")
            @PathVariable(name = "feed_id") UUID id,
            @RequestParam UUID userId) {
        feedService.likeFeed(id, userId);
    }

    @Operation(summary = "Удалить лайк новости")
    @PostMapping("/{feed_id}/unlike")
    public void unlikeFeed(
            @Parameter(name = "id новости")
            @PathVariable(name = "feed_id") UUID id,
            @Parameter(name = "id пользователя")
            @RequestParam UUID userId) {
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