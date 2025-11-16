BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS modules (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code        varchar NOT NULL UNIQUE,
    name        varchar NOT NULL,
    description text,
    is_enabled  boolean NOT NULL DEFAULT true,
    created_at  timestamptz NOT NULL DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS user_widgets (
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type       varchar NOT NULL,
    title      varchar,
    settings   jsonb   NOT NULL DEFAULT '{}'::jsonb,
    is_active  boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS ix_user_widgets_user   ON user_widgets(user_id);
CREATE INDEX IF NOT EXISTS ix_user_widgets_active ON user_widgets(user_id, is_active);

CREATE TABLE IF NOT EXISTS user_module_layouts (
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name       varchar NOT NULL DEFAULT 'default',

    grid_lg    jsonb NOT NULL DEFAULT '[]'::jsonb,
    grid_md    jsonb NOT NULL DEFAULT '[]'::jsonb,
    grid_sm    jsonb NOT NULL DEFAULT '[]'::jsonb,
    grid_xs    jsonb NOT NULL DEFAULT '[]'::jsonb,
    grid_xxs   jsonb NOT NULL DEFAULT '[]'::jsonb,

    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    UNIQUE (user_id, name)
    );

CREATE INDEX IF NOT EXISTS ix_user_module_layouts_user ON user_module_layouts(user_id);

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN NEW.updated_at := now(); RETURN NEW; END;
$$;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_user_widgets_set_updated_at') THEN
CREATE TRIGGER trg_user_widgets_set_updated_at
    BEFORE UPDATE ON user_widgets
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_user_module_layouts_set_updated_at') THEN
CREATE TRIGGER trg_user_module_layouts_set_updated_at
    BEFORE UPDATE ON user_module_layouts
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
END IF;
END $$;

INSERT INTO modules(code,name,description) VALUES
    ('habit_tracker','Habit Tracker','Daily habits and micro-habits'),
    ('mood_chart','Mood Chart','Mood timeline'),
    ('goals_progress','Goals Progress','Objectives and milestones'),
    ('wheel_of_life','Wheel of Life','Self-scoring life areas')
ON CONFLICT (code) DO NOTHING;

COMMIT;
