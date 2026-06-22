alter table attraction_popularity_stats
    drop constraint if exists chk_attraction_popularity_stats_favorite_count;

alter table attraction_popularity_stats
    drop column if exists favorite_count;

drop table if exists attraction_favorites;
