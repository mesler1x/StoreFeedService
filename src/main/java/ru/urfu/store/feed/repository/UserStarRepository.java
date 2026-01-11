package ru.urfu.store.feed.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserStarRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Transactional
    public void star(String userMail, UUID feedId) {
        var sql = """
                INSERT INTO user_star (user_mail, feed_id)
                VALUES (:userMail, :feedId)
                ON CONFLICT (user_mail, feed_id) DO NOTHING
                """;

        var params = new MapSqlParameterSource()
                .addValue("userMail", userMail)
                .addValue("feedId", feedId);

        jdbcTemplate.update(sql, params);
    }

    @Transactional
    public void unStar(String userMail, UUID feedId) {
        var sql = "DELETE FROM user_star WHERE user_mail = :userMail AND feed_id = :feedId";
        var params = new MapSqlParameterSource()
                .addValue("userMail", userMail)
                .addValue("feedId", feedId);

        jdbcTemplate.update(sql, params);
    }

    @Transactional
    public void delete(UUID feedId) {
        var sql = "DELETE FROM user_star WHERE feed_id = :feedId";
        var params = new MapSqlParameterSource()
                .addValue("feedId", feedId);

        jdbcTemplate.update(sql, params);
    }
}