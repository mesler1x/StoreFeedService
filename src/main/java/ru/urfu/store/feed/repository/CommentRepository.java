package ru.urfu.store.feed.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.store.feed.model.Comment;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CommentRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private Comment mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Comment.builder()
                .id(rs.getObject("id", UUID.class))
                .text(rs.getString("text"))
                .userId(rs.getObject("user_id", UUID.class))
                .feedId(rs.getObject("feed_id", UUID.class))
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
    public Comment save(Comment comment) {
        if (comment.getId() == null) {
            return insert(comment);
        } else {
            return update(comment);
        }
    }

    private Comment insert(Comment comment) {
        var sql = """
                INSERT INTO comment (text, user_id, feed_id, created, updated)
                VALUES (:text, :userId, :feedId, :created, :updated)
                RETURNING id
                """;

        var params = new MapSqlParameterSource()
                .addValue("text", comment.getText())
                .addValue("userId", comment.getUserId())
                .addValue("feedId", comment.getFeedId())
                .addValue("created", convertToTimestamp(comment.getCreated()))
                .addValue("updated", convertToTimestamp(comment.getUpdated()));

        var generatedId = jdbcTemplate.queryForObject(sql, params, UUID.class);
        comment.setId(generatedId);
        return comment;
    }

    private Comment update(Comment comment) {
        var sql = """
                UPDATE comment 
                SET text = :text, 
                    updated = :updated
                WHERE id = :id
                """;

        comment.setUpdated(ZonedDateTime.now());

        var params = new MapSqlParameterSource()
                .addValue("id", comment.getId())
                .addValue("text", comment.getText())
                .addValue("updated", convertToTimestamp(comment.getUpdated()));

        jdbcTemplate.update(sql, params);
        return comment;
    }

    public List<Comment> findByFeedId(UUID feedId) {
        var sql = "SELECT * FROM comment WHERE feed_id = :feedId ORDER BY created DESC";
        var params = new MapSqlParameterSource().addValue("feedId", feedId);

        return jdbcTemplate.query(sql, params, this::mapRow);
    }

    @Transactional
    public void deleteByFeedId(UUID feedId) {
        var sql = "DELETE FROM comment WHERE feed_id = :feedId";
        var params = new MapSqlParameterSource().addValue("feedId", feedId);

        jdbcTemplate.update(sql, params);
    }

    public List<Comment> findByUserId(UUID userId) {
        var sql = "SELECT * FROM comment WHERE user_id = :userId ORDER BY created DESC";
        var params = new MapSqlParameterSource().addValue("userId", userId);

        return jdbcTemplate.query(sql, params, this::mapRow);
    }
}