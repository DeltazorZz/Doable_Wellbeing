BEGIN;

-- 1) Appointment resources (coach uploads metadata; storage link később)
CREATE TABLE IF NOT EXISTS appointment_resources (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id uuid NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,
    uploaded_by uuid NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    file_name varchar NOT NULL,
    size_bytes bigint,
    mime_type varchar,
    url varchar,
    created_at timestamptz NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS ix_appt_resources_appt_time ON appointment_resources(appointment_id, created_at DESC);

-- Appointment notes
CREATE TABLE IF NOT EXISTS appointment_notes (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id uuid NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,
    created_by uuid NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    note text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS ix_appt_notes_appt_time ON appointment_notes(appointment_id, created_at);

-- 2) Wheel of life scores
CREATE TABLE IF NOT EXISTS user_wheel_scores (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    area varchar NOT NULL,
    score int NOT NULL,
    scored_at timestamptz NOT NULL DEFAULT now(),
    CHECK (score BETWEEN 1 AND 10)
    );
CREATE INDEX IF NOT EXISTS ix_wheel_user_time ON user_wheel_scores(user_id, scored_at DESC);
CREATE INDEX IF NOT EXISTS ix_wheel_user_area_time ON user_wheel_scores(user_id, area, scored_at DESC);

-- 3) Quick check-ins (comfort/stretch/burnout gauge)
CREATE TABLE IF NOT EXISTS user_checkins (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    level varchar NOT NULL, -- comfort/stretch/burnout
    intensity int NOT NULL, -- 0..100 (a gauge-hez)
    note text,
    created_at timestamptz NOT NULL DEFAULT now(),
    CHECK (intensity BETWEEN 0 AND 100)
    );
CREATE INDEX IF NOT EXISTS ix_checkins_user_time ON user_checkins(user_id, created_at DESC);

-- 4) Micro-habit catalog
CREATE TABLE IF NOT EXISTS micro_habit_catalog (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    category varchar NOT NULL,
    title varchar NOT NULL,
    notes text,
    tags jsonb NOT NULL DEFAULT '[]'::jsonb,
    is_active boolean NOT NULL DEFAULT true
    );
CREATE INDEX IF NOT EXISTS ix_micro_habit_cat_active ON micro_habit_catalog(category, is_active);

-- 5) Mood-log
CREATE TABLE IF NOT EXISTS mood_logs (
                                         id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    mood_score int NOT NULL,
    note text,
    logged_at timestamptz NOT NULL DEFAULT now(),
    CHECK (mood_score BETWEEN 1 AND 10)
    );

CREATE INDEX IF NOT EXISTS ix_mood_user_time ON mood_logs(user_id, logged_at DESC);

-- 6) Habits
CREATE TABLE IF NOT EXISTS habits (
                                      id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title varchar NOT NULL,
    description text NOT NULL,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now()
    );
CREATE INDEX IF NOT EXISTS ix_habits_user_active ON habits(user_id, is_active);

-- 7) Habits-log
CREATE TABLE IF NOT EXISTS habit_logs (
                                          id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    habit_id uuid NOT NULL REFERENCES habits(id) ON DELETE CASCADE,
    logged_date date NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    UNIQUE (habit_id, logged_date)
    );
CREATE INDEX IF NOT EXISTS ix_habit_logs_habit_date ON habit_logs(habit_id, logged_date DESC);

COMMIT;
