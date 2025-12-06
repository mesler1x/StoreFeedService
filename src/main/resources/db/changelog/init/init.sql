CREATE TABLE feed(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    text TEXT NOT NULL,
    likes_count BIGINT NOT NULL DEFAULT 0,
    watch_count BIGINT NOT NULL DEFAULT 0,
    comments_count BIGINT NOT NULL DEFAULT 0,
    created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated TIMESTAMP WITH TIME ZONE
);

CREATE TABLE comment(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    text TEXT NOT NULL,
    user_id UUID NOT NULL,
    created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated TIMESTAMP WITH TIME ZONE
);

CREATE TABLE user_star(
    user_id UUID NOT NULL,
    feed_id UUID NOT NULL
);

