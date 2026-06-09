create table attraction_popularity_stats (
    attraction_id bigint not null,
    favorite_count integer not null default 0,
    rating_count integer not null default 0,
    average_rating numeric(3, 2) not null default 0,
    view_count integer not null default 0,
    course_add_count integer not null default 0,
    popularity_score numeric(12, 4) not null default 0,
    updated_at timestamp(6) not null default current_timestamp,
    primary key (attraction_id),
    constraint fk_attraction_popularity_stats_attraction foreign key (attraction_id) references attractions (id) on delete cascade,
    constraint chk_attraction_popularity_stats_favorite_count check (favorite_count >= 0),
    constraint chk_attraction_popularity_stats_rating_count check (rating_count >= 0),
    constraint chk_attraction_popularity_stats_average_rating check (average_rating between 0 and 5),
    constraint chk_attraction_popularity_stats_view_count check (view_count >= 0),
    constraint chk_attraction_popularity_stats_course_add_count check (course_add_count >= 0),
    constraint chk_attraction_popularity_stats_popularity_score check (popularity_score >= 0)
);

create index idx_attraction_popularity_stats_score on attraction_popularity_stats (popularity_score desc, attraction_id);
