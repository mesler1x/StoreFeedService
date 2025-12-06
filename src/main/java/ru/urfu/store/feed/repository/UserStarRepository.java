package ru.urfu.store.feed.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserStarRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Transactional
    public void save(UUID userId, UUID feedId) {
        var sql = """
            INSERT INTO user_star (user_id, feed_id)
            VALUES (:userId, :feedId)
            ON CONFLICT (user_id, feed_id) DO NOTHING
            """;

        var params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("feedId", feedId);

        jdbcTemplate.update(sql, params);
    }

    @Transactional
    public void delete(UUID userId, UUID feedId) {
        var sql = "DELETE FROM user_star WHERE user_id = :userId AND feed_id = :feedId";
        var params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("feedId", feedId);

        jdbcTemplate.update(sql, params);
    }

    public boolean existsByUserIdAndFeedId(UUID userId, UUID feedId) {
        var sql = """
            SELECT COUNT(*) FROM user_star 
            WHERE user_id = :userId AND feed_id = :feedId
            """;

        var params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("feedId", feedId);

        var count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    public List<UUID> findFeedIdsByUserId(UUID userId) {
        var sql = "SELECT feed_id FROM user_star WHERE user_id = :userId";
        var params = new MapSqlParameterSource().addValue("userId", userId);

        return jdbcTemplate.queryForList(sql, params, UUID.class);
    }
}