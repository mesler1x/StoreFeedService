package ru.urfu.store.feed.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.store.feed.model.Feed;
import ru.urfu.store.feed.model.dto.Paging;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
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
                INSERT INTO feed (title, text, created, updated)
                VALUES (:title, :text, :created, :updated)
                RETURNING id
                """;

        var params = new MapSqlParameterSource()
                .addValue("title", feed.getTitle())
                .addValue("text", feed.getText())
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
        var sql = """
                SELECT 
                    f.id,
                    f.title,
                    f.text,
                    f.created,
                    f.updated,
                    f.watch_count,
                    COALESCE(ul.likes_count, 0) AS likes_count,
                    COALESCE(c.comments_count, 0) AS comments_count
                FROM feed f
                LEFT JOIN (
                    SELECT feed_id, COUNT(*) AS likes_count 
                    FROM user_like 
                    GROUP BY feed_id
                ) ul ON f.id = ul.feed_id
                LEFT JOIN (
                    SELECT feed_id, COUNT(*) AS stars_count 
                    FROM user_star 
                    GROUP BY feed_id
                ) us ON f.id = us.feed_id
                LEFT JOIN (
                    SELECT feed_id, COUNT(*) AS comments_count 
                    FROM comment 
                    GROUP BY feed_id
                ) c ON f.id = c.feed_id
                WHERE f.id = :id
                """;

        var params = new MapSqlParameterSource().addValue("id", id);

        var feeds = jdbcTemplate.query(sql, params, this::mapRowWithCounts);

        return feeds.stream().findFirst();
    }

    public Paging<Feed> findAll(Integer limit, Integer offset) {
        var countSql = "SELECT COUNT(*) FROM feed";
        var total = jdbcTemplate.queryForObject(countSql, new MapSqlParameterSource(), Long.class);

        var sql = """
                SELECT 
                    f.*,
                    COALESCE(l.likes_count, 0) AS likes_count,
                    COALESCE(c.comments_count, 0) AS comments_count
                FROM feed f
                LEFT JOIN (
                    SELECT feed_id, COUNT(*) AS likes_count 
                    FROM user_like 
                    GROUP BY feed_id
                ) l ON f.id = l.feed_id
                LEFT JOIN (
                    SELECT feed_id, COUNT(*) AS stars_count 
                    FROM user_star 
                    GROUP BY feed_id
                ) s ON f.id = s.feed_id
                LEFT JOIN (
                    SELECT feed_id, COUNT(*) AS comments_count 
                    FROM comment 
                    GROUP BY feed_id
                ) c ON f.id = c.feed_id
                ORDER BY f.created DESC 
                LIMIT :limit OFFSET :offset
                """;

        var params = new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("offset", offset);

        var feeds = jdbcTemplate.query(sql, params, this::mapRowWithCounts);

        return new Paging<>(total, limit, offset, feeds);
    }

    private Feed mapRowWithCounts(ResultSet rs, int rowNum) throws SQLException {
        return Feed.builder()
                .id(rs.getObject("id", UUID.class))
                .title(rs.getString("title"))
                .text(rs.getString("text"))
                .likesCount(rs.getLong("likes_count"))
                .watchCount(rs.getLong("watch_count"))
                .commentsCount(rs.getLong("comments_count"))
                .created(rs.getObject("created", OffsetDateTime.class).toZonedDateTime())
                .updated(Optional.ofNullable(rs.getObject("updated", OffsetDateTime.class))
                        .map(OffsetDateTime::toZonedDateTime)
                        .orElse(null))
                .build();
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
    public void incrementLikesCount(UUID feedId, UUID userId) {
        var sql = """
                
                    INSERT INTO user_like(user_id, feed_id) VALUES (
                        :userId, :feedId                                                
                    ) ON CONFLICT DO NOTHING
                """;

        var params = new MapSqlParameterSource()
                .addValue("feedId", feedId)
                .addValue("userId", userId);
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
    public void decrementLikesCount(UUID feedId, UUID userId) {
        jdbcTemplate.update(
                """
                        DELETE FROM user_like WHERE user_id = :userId and feed_id = :feedId
                        """,
                new MapSqlParameterSource().addValue("userId", userId)
                        .addValue("feedId", feedId)

        );
    }

    @Transactional
    public void deleteLikes(UUID feedId) {
        jdbcTemplate.update(
                """
                        DELETE FROM user_like WHERE feed_id = :feedId
                        """,
                new MapSqlParameterSource().addValue("feedId", feedId)

        );
    }
}
