CREATE TABLE IF NOT EXISTS coach_availabilities (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    coach_id uuid NOT NULL REFERENCES coaches(user_id) ON DELETE CASCADE,
    weekday int NOT NULL,
    start_time time NOT NULL,
    end_time time NOT NULL,
    CHECK (end_time > start_time)
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_coach_availability_slot
    ON coach_availabilities (coach_id, weekday, start_time, end_time);
