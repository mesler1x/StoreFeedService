package ru.urfu.store.feed.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.store.feed.model.Feed;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FeedRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private Feed mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Feed.builder()
                .id(rs.getObject("id", UUID.class))
                .title(rs.getString("title"))
                .text(rs.getString("text"))
                .likesCount(rs.getLong("likes_count"))
                .watchCount(rs.getLong("watch_count"))
                .commentsCount(rs.getLong("comments_count"))
                .created(convertToZonedDateTime(rs.getTimestamp("created")))
                .updated(convertToZonedDateTime(rs.getTimestamp("updated")))
                .build();
    }

    private ZonedDateTime convertToZonedDateTime(Timestamp timestamp) {
        return timestamp != null ?
                ZonedDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault()) :
                null;
    }

    private Timestamp convertToTimestamp(ZonedDateTime zonedDateTime) {
        return zonedDateTime != null ?
                Timestamp.from(zonedDateTime.toInstant()) :
                null;
    }

    @Transactional
    public Feed save(Feed feed) {
        if (feed.getId() == null) {
            return insert(feed);
        } else {
            return update(feed);
        }
    }

    private Feed insert(Feed feed) {
        var sql = """
            INSERT INTO feed (title, text, likes_count, watch_count, comments_count, created, updated)
            VALUES (:title, :text, :likesCount, :watchCount, :commentsCount, :created, :updated)
            RETURNING id
            """;

        var params = new MapSqlParameterSource()
                .addValue("title", feed.getTitle())
                .addValue("text", feed.getText())
                .addValue("likesCount", feed.getLikesCount())
                .addValue("watchCount", feed.getWatchCount())
                .addValue("commentsCount", feed.getCommentsCount())
                .addValue("created", convertToTimestamp(feed.getCreated()))
                .addValue("updated", convertToTimestamp(feed.getUpdated()));

        var generatedId = jdbcTemplate.queryForObject(sql, params, UUID.class);
        feed.setId(generatedId);
        return feed;
    }

    private Feed update(Feed feed) {
        var sql = """
            UPDATE feed 
            SET title = :title, 
                text = :text, 
                updated = :updated
            WHERE id = :id
            """;

        feed.setUpdated(ZonedDateTime.now());

        var params = new MapSqlParameterSource()
                .addValue("id", feed.getId())
                .addValue("title", feed.getTitle())
                .addValue("text", feed.getText())
                .addValue("updated", convertToTimestamp(feed.getUpdated()));

        jdbcTemplate.update(sql, params);
        return feed;
    }

    public Optional<Feed> findById(UUID id) {
        var sql = "SELECT * FROM feed WHERE id = :id";

        var params = new MapSqlParameterSource().addValue("id", id);
        var feeds = jdbcTemplate.query(sql, params, this::mapRow);

        return feeds.stream().findFirst();
    }

    public Page<Feed> findAll(Pageable pageable) {
        var countSql = "SELECT COUNT(*) FROM feed";
        var total = jdbcTemplate.queryForObject(countSql, new MapSqlParameterSource(), Long.class);

        var sql = """
            SELECT * FROM feed 
            ORDER BY created DESC 
            LIMIT :limit OFFSET :offset
            """;

        var params = new MapSqlParameterSource()
                .addValue("limit", pageable.getPageSize())
                .addValue("offset", pageable.getOffset());

        var feeds = jdbcTemplate.query(sql, params, this::mapRow);

        return new PageImpl<>(feeds, pageable, total != null ? total : 0L);
    }

    @Transactional
    public void deleteById(UUID id) {
        var sql = "DELETE FROM feed WHERE id = :id";
        var params = new MapSqlParameterSource().addValue("id", id);
        jdbcTemplate.update(sql, params);
    }

    public boolean existsById(UUID id) {
        var sql = "SELECT COUNT(*) FROM feed WHERE id = :id";
        var params = new MapSqlParameterSource().addValue("id", id);
        var count = jdbcTemplate.queryForObject(sql, params, Integer.class);

        return count != null && count > 0;
    }

    @Transactional
    public void incrementLikesCount(UUID feedId) {
        var sql = """
            UPDATE feed 
            SET likes_count = likes_count + 1,
                updated = NOW()
            WHERE id = :id
            """;

        var params = new MapSqlParameterSource().addValue("id", feedId);
        jdbcTemplate.update(sql, params);
    }

    @Transactional
    public void incrementWatchCount(UUID feedId) {
        var sql = """
            UPDATE feed 
            SET watch_count = watch_count + 1,
                updated = NOW()
            WHERE id = :id
            """;

        var params = new MapSqlParameterSource().addValue("id", feedId);
        jdbcTemplate.update(sql, params);
    }

    @Transactional
    public void incrementCommentsCount(UUID feedId) {
        var sql = """
            UPDATE feed 
            SET comments_count = comments_count + 1,
                updated = NOW()
            WHERE id = :id
            """;

        var params = new MapSqlParameterSource().addValue("id", feedId);
        jdbcTemplate.update(sql, params);
    }

    @Transactional
    public void decrementLikesCount(UUID feedId) {
        var sql = """
            UPDATE feed 
            SET likes_count = GREATEST(likes_count - 1, 0),
                updated = NOW()
            WHERE id = :id
            """;

        var params = new MapSqlParameterSource().addValue("id", feedId);
        jdbcTemplate.update(sql, params);
    }
}
