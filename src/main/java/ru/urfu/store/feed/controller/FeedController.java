package ru.urfu.store.feed.controller;

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
import ru.urfu.store.feed.model.dto.CommentRequest;
import ru.urfu.store.feed.model.dto.CreateFeedRequest;
import ru.urfu.store.feed.model.dto.FeedDto;
import ru.urfu.store.feed.model.dto.UpdateFeedRequest;
import ru.urfu.store.feed.service.FeedService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public FeedDto createFeed(@Valid @RequestBody CreateFeedRequest request) {
        return feedService.createFeed(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeedDto> getFeed(@PathVariable UUID id) {
        var feed = feedService.getFeed(id);
        return ResponseEntity.ok(feed);
    }

    @GetMapping
    public Page<FeedDto> getAllFeeds(
            @PageableDefault(size = 20, sort = "created", direction = Sort.Direction.DESC) Pageable pageable) {
        return feedService.getAllFeeds(pageable);
    }

    @PutMapping("/{id}")
    public FeedDto updateFeed(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFeedRequest request) {
        return feedService.updateFeed(id, request);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteFeed(@PathVariable UUID id) {
        feedService.deleteFeed(id);
    }

    @PostMapping("/{id}/like")
    public void likeFeed(
            @PathVariable UUID id,
            @RequestParam UUID userId) {
        feedService.likeFeed(id, userId);
    }

    @PostMapping("/{id}/unlike")
    public void unlikeFeed(
            @PathVariable UUID id,
            @RequestParam UUID userId) {
        feedService.unlikeFeed(id, userId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{id}/comment")
    public Comment addComment(
            @PathVariable UUID id,
            @Valid @RequestBody CommentRequest request) {
        if (!id.equals(request.getFeedId())) {
            throw new IllegalArgumentException("Feed ID in path must match feed ID in request");
        }

        return feedService.addComment(request);
    }
}