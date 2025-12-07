package ru.urfu.store.feed.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.store.feed.model.Comment;
import ru.urfu.store.feed.model.Feed;
import ru.urfu.store.feed.model.dto.*;
import ru.urfu.store.feed.model.dto.exception.ResourceNotFoundException;
import ru.urfu.store.feed.repository.CommentRepository;
import ru.urfu.store.feed.repository.FeedRepository;
import ru.urfu.store.feed.repository.UserStarRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final CommentRepository commentRepository;
    private final UserStarRepository userStarRepository;

    public FeedDto createFeed(CreateFeedRequest request) {
        var feed = Feed.builder()
                .title(request.getTitle())
                .text(request.getText())
                .build();

        var savedFeed = feedRepository.save(feed);
        return mapToDto(savedFeed);
    }

    public FeedDto getFeed(UUID id) {
        incrementWatchCount(id);

        var feed = feedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feed not found with id: " + id));

        return mapToDto(feed);
    }

    public Paging<FeedDto> getAllFeeds(Integer limit, Integer offset) {
        var result = feedRepository.findAll(limit, offset);
        var dtoList = result.getCurrentValues().stream().map(this::mapToDto).toList();
        return new Paging<>(result.getTotalCount(), result.getLimit(), result.getOffset(), dtoList);
    }

    public FeedDto updateFeed(UUID id, UpdateFeedRequest request) {
        var feed = feedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feed not found with id: " + id));

        feed.setTitle(request.getTitle());
        feed.setText(request.getText());

        var updatedFeed = feedRepository.save(feed);
        return mapToDto(updatedFeed);
    }

    @Transactional
    public void deleteFeed(UUID id) {
        if (!feedRepository.existsById(id)) {
            throw new ResourceNotFoundException("Feed not found with id: " + id);
        }

        commentRepository.deleteByFeedId(id);
        feedRepository.deleteById(id);
    }

    @Transactional
    public void likeFeed(UUID feedId, UUID userId) {
        if (!feedRepository.existsById(feedId)) {
            throw new ResourceNotFoundException("Feed not found with id: " + feedId);
        }

        if (!userStarRepository.existsByUserIdAndFeedId(userId, feedId)) {
            userStarRepository.save(userId, feedId);
            feedRepository.incrementLikesCount(feedId);
        }
    }

    @Transactional
    public void unlikeFeed(UUID feedId, UUID userId) {
        if (!feedRepository.existsById(feedId)) {
            throw new ResourceNotFoundException("Feed not found with id: " + feedId);
        }

        if (userStarRepository.existsByUserIdAndFeedId(userId, feedId)) {
            userStarRepository.delete(userId, feedId);
            feedRepository.decrementLikesCount(feedId);
        }
    }

    @Transactional
    public void incrementWatchCount(UUID feedId) {
        feedRepository.incrementWatchCount(feedId);
    }

    @Transactional
    public Comment addComment(CommentRequest request) {
        if (!feedRepository.existsById(request.getFeedId())) {
            throw new ResourceNotFoundException("Feed not found with id: " + request.getFeedId());
        }

        var comment = Comment.builder()
                .text(request.getText())
                .userId(request.getUserId())
                .feedId(request.getFeedId())
                .build();

        var savedComment = commentRepository.save(comment);
        feedRepository.incrementCommentsCount(request.getFeedId());

        return savedComment;
    }

    private FeedDto mapToDto(Feed feed) {
        return FeedDto.builder()
                .id(feed.getId())
                .title(feed.getTitle())
                .text(feed.getText())
                .likesCount(feed.getLikesCount())
                .watchCount(feed.getWatchCount())
                .commentsCount(feed.getCommentsCount())
                .created(feed.getCreated())
                .updated(feed.getUpdated())
                .build();
    }
}