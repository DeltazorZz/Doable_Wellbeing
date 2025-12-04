CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE coach_availabilities
    ADD CONSTRAINT coach_availabilities_weekday_valid
        CHECK (weekday BETWEEN 1 AND 7);

ALTER TABLE coach_availabilities
    ADD CONSTRAINT coach_availabilities_no_overlap
    EXCLUDE USING gist (
        coach_id WITH =,
        weekday WITH =,
        tsrange(
            (TIMESTAMP '2000-01-01' + start_time),
            (TIMESTAMP '2000-01-01' + end_time)
        ) WITH &&
    );
