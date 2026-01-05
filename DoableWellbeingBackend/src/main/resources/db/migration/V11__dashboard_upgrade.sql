BEGIN;

-- =========================================================
-- 0) Extensions + helper trigger function
-- =========================================================
CREATE EXTENSION IF NOT EXISTS pgcrypto;


DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_proc WHERE proname = 'set_updated_at') THEN
    CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS trigger LANGUAGE plpgsql AS $fn$
BEGIN
      NEW.updated_at := now();
RETURN NEW;
END;
    $fn$;
END IF;
END $$;

-- =========================================================
-- 1) New dashboard tables
-- =========================================================
CREATE TABLE IF NOT EXISTS dashboards (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name text NOT NULL DEFAULT 'default',
    is_default boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    UNIQUE(user_id, name)
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_dashboards_default
    ON dashboards(user_id)
    WHERE is_default = true;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_dashboards_set_updated_at') THEN
CREATE TRIGGER trg_dashboards_set_updated_at
    BEFORE UPDATE ON dashboards
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
END IF;
END $$;




CREATE TABLE IF NOT EXISTS dashboard_widgets (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    dashboard_id uuid NOT NULL REFERENCES dashboards(id) ON DELETE CASCADE,


    module_id uuid NOT NULL REFERENCES modules(id),

    title text,
    settings jsonb NOT NULL DEFAULT '{}'::jsonb,
    settings_version int NOT NULL DEFAULT 1,

    is_active boolean NOT NULL DEFAULT true,


    legacy_user_widget_id uuid UNIQUE,

    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS ix_dashboard_widgets_dashboard
    ON dashboard_widgets(dashboard_id);

CREATE INDEX IF NOT EXISTS ix_dashboard_widgets_module
    ON dashboard_widgets(module_id);

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_dashboard_widgets_set_updated_at') THEN
CREATE TRIGGER trg_dashboard_widgets_set_updated_at
    BEFORE UPDATE ON dashboard_widgets
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
END IF;
END $$;


CREATE TABLE IF NOT EXISTS dashboard_widget_placements (
                                                           id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    widget_id uuid NOT NULL REFERENCES dashboard_widgets(id) ON DELETE CASCADE,

    breakpoint text NOT NULL,
    x int NOT NULL,
    y int NOT NULL,
    w int NOT NULL,
    h int NOT NULL,


    min_w int,
    min_h int,
    max_w int,
    max_h int,
    is_static boolean,

    UNIQUE(widget_id, breakpoint),
    CONSTRAINT ck_dashboard_breakpoint CHECK (breakpoint IN ('lg','md','sm','xs','xxs'))
    );

CREATE INDEX IF NOT EXISTS ix_widget_placements_widget
    ON dashboard_widget_placements(widget_id);

-- =========================================================
-- 2) Create dashboards for users that already have widgets/layouts
-- =========================================================
INSERT INTO dashboards(user_id, name, is_default)
SELECT DISTINCT uw.user_id, 'default', true
FROM user_widgets uw
    ON CONFLICT (user_id, name) DO NOTHING;


INSERT INTO dashboards(user_id, name, is_default)
SELECT DISTINCT uml.user_id, COALESCE(uml.name, 'default'), (COALESCE(uml.name,'default') = 'default')
FROM user_module_layouts uml
    ON CONFLICT (user_id, name) DO NOTHING;


UPDATE dashboards d
SET is_default = true
WHERE d.name = 'default'
  AND NOT EXISTS (
    SELECT 1 FROM dashboards d2
    WHERE d2.user_id = d.user_id AND d2.is_default = true
);

-- =========================================================
-- 3) Migrate widgets: user_widgets -> dashboard_widgets
-- =========================================================

INSERT INTO dashboard_widgets (
    dashboard_id, module_id, title, settings, is_active,
    legacy_user_widget_id, created_at, updated_at
)
SELECT
    d.id,
    m.id,
    uw.title,
    uw.settings,
    uw.is_active,
    uw.id,
    uw.created_at,
    uw.updated_at
FROM user_widgets uw
         JOIN dashboards d
              ON d.user_id = uw.user_id AND d.name = 'default'
         JOIN modules m
              ON m.code = uw.type
    ON CONFLICT (legacy_user_widget_id) DO NOTHING;

-- =========================================================
-- 4) Migrate placements: user_module_layouts.grid_* -> placements
-- =========================================================
INSERT INTO dashboard_widget_placements(widget_id, breakpoint, x, y, w, h, min_w, min_h, max_w, max_h, is_static)
SELECT
    dw.id,
    'lg',
    COALESCE((it->>'x')::int, 0),
    COALESCE((it->>'y')::int, 0),
    COALESCE((it->>'w')::int, 1),
    COALESCE((it->>'h')::int, 1),
    NULLIF((it->>'minW')::int, 0),
    NULLIF((it->>'minH')::int, 0),
    NULLIF((it->>'maxW')::int, 0),
    NULLIF((it->>'maxH')::int, 0),
    CASE WHEN it ? 'static' THEN (it->>'static')::boolean ELSE NULL END
FROM user_module_layouts uml
         CROSS JOIN LATERAL jsonb_array_elements(uml.grid_lg) it
JOIN dashboards d
ON d.user_id = uml.user_id AND d.name = COALESCE(uml.name,'default')
    JOIN dashboard_widgets dw
    ON dw.dashboard_id = d.id
    AND dw.legacy_user_widget_id = (it->>'i')::uuid
    ON CONFLICT (widget_id, breakpoint) DO UPDATE
    SET x = EXCLUDED.x, y = EXCLUDED.y, w = EXCLUDED.w, h = EXCLUDED.h,
    min_w = EXCLUDED.min_w, min_h = EXCLUDED.min_h, max_w = EXCLUDED.max_w, max_h = EXCLUDED.max_h,
    is_static = EXCLUDED.is_static;


INSERT INTO dashboard_widget_placements(widget_id, breakpoint, x, y, w, h, min_w, min_h, max_w, max_h, is_static)
SELECT
    dw.id,
    'md',
    COALESCE((it->>'x')::int, 0),
    COALESCE((it->>'y')::int, 0),
    COALESCE((it->>'w')::int, 1),
    COALESCE((it->>'h')::int, 1),
    NULLIF((it->>'minW')::int, 0),
    NULLIF((it->>'minH')::int, 0),
    NULLIF((it->>'maxW')::int, 0),
    NULLIF((it->>'maxH')::int, 0),
    CASE WHEN it ? 'static' THEN (it->>'static')::boolean ELSE NULL END
FROM user_module_layouts uml
         CROSS JOIN LATERAL jsonb_array_elements(uml.grid_md) it
JOIN dashboards d
ON d.user_id = uml.user_id AND d.name = COALESCE(uml.name,'default')
    JOIN dashboard_widgets dw
    ON dw.dashboard_id = d.id
    AND dw.legacy_user_widget_id = (it->>'i')::uuid
    ON CONFLICT (widget_id, breakpoint) DO UPDATE
    SET x = EXCLUDED.x, y = EXCLUDED.y, w = EXCLUDED.w, h = EXCLUDED.h,
    min_w = EXCLUDED.min_w, min_h = EXCLUDED.min_h, max_w = EXCLUDED.max_w, max_h = EXCLUDED.max_h,
    is_static = EXCLUDED.is_static;

INSERT INTO dashboard_widget_placements(widget_id, breakpoint, x, y, w, h, min_w, min_h, max_w, max_h, is_static)
SELECT
    dw.id,
    'sm',
    COALESCE((it->>'x')::int, 0),
    COALESCE((it->>'y')::int, 0),
    COALESCE((it->>'w')::int, 1),
    COALESCE((it->>'h')::int, 1),
    NULLIF((it->>'minW')::int, 0),
    NULLIF((it->>'minH')::int, 0),
    NULLIF((it->>'maxW')::int, 0),
    NULLIF((it->>'maxH')::int, 0),
    CASE WHEN it ? 'static' THEN (it->>'static')::boolean ELSE NULL END
FROM user_module_layouts uml
         CROSS JOIN LATERAL jsonb_array_elements(uml.grid_sm) it
JOIN dashboards d
ON d.user_id = uml.user_id AND d.name = COALESCE(uml.name,'default')
    JOIN dashboard_widgets dw
    ON dw.dashboard_id = d.id
    AND dw.legacy_user_widget_id = (it->>'i')::uuid
    ON CONFLICT (widget_id, breakpoint) DO UPDATE
    SET x = EXCLUDED.x, y = EXCLUDED.y, w = EXCLUDED.w, h = EXCLUDED.h,
    min_w = EXCLUDED.min_w, min_h = EXCLUDED.min_h, max_w = EXCLUDED.max_w, max_h = EXCLUDED.max_h,
    is_static = EXCLUDED.is_static;


INSERT INTO dashboard_widget_placements(widget_id, breakpoint, x, y, w, h, min_w, min_h, max_w, max_h, is_static)
SELECT
    dw.id,
    'xs',
    COALESCE((it->>'x')::int, 0),
    COALESCE((it->>'y')::int, 0),
    COALESCE((it->>'w')::int, 1),
    COALESCE((it->>'h')::int, 1),
    NULLIF((it->>'minW')::int, 0),
    NULLIF((it->>'minH')::int, 0),
    NULLIF((it->>'maxW')::int, 0),
    NULLIF((it->>'maxH')::int, 0),
    CASE WHEN it ? 'static' THEN (it->>'static')::boolean ELSE NULL END
FROM user_module_layouts uml
         CROSS JOIN LATERAL jsonb_array_elements(uml.grid_xs) it
JOIN dashboards d
ON d.user_id = uml.user_id AND d.name = COALESCE(uml.name,'default')
    JOIN dashboard_widgets dw
    ON dw.dashboard_id = d.id
    AND dw.legacy_user_widget_id = (it->>'i')::uuid
    ON CONFLICT (widget_id, breakpoint) DO UPDATE
    SET x = EXCLUDED.x, y = EXCLUDED.y, w = EXCLUDED.w, h = EXCLUDED.h,
    min_w = EXCLUDED.min_w, min_h = EXCLUDED.min_h, max_w = EXCLUDED.max_w, max_h = EXCLUDED.max_h,
    is_static = EXCLUDED.is_static;


INSERT INTO dashboard_widget_placements(widget_id, breakpoint, x, y, w, h, min_w, min_h, max_w, max_h, is_static)
SELECT
    dw.id,
    'xxs',
    COALESCE((it->>'x')::int, 0),
    COALESCE((it->>'y')::int, 0),
    COALESCE((it->>'w')::int, 1),
    COALESCE((it->>'h')::int, 1),
    NULLIF((it->>'minW')::int, 0),
    NULLIF((it->>'minH')::int, 0),
    NULLIF((it->>'maxW')::int, 0),
    NULLIF((it->>'maxH')::int, 0),
    CASE WHEN it ? 'static' THEN (it->>'static')::boolean ELSE NULL END
FROM user_module_layouts uml
         CROSS JOIN LATERAL jsonb_array_elements(uml.grid_xxs) it
JOIN dashboards d
ON d.user_id = uml.user_id AND d.name = COALESCE(uml.name,'default')
    JOIN dashboard_widgets dw
    ON dw.dashboard_id = d.id
    AND dw.legacy_user_widget_id = (it->>'i')::uuid
    ON CONFLICT (widget_id, breakpoint) DO UPDATE
    SET x = EXCLUDED.x, y = EXCLUDED.y, w = EXCLUDED.w, h = EXCLUDED.h,
    min_w = EXCLUDED.min_w, min_h = EXCLUDED.min_h, max_w = EXCLUDED.max_w, max_h = EXCLUDED.max_h,
    is_static = EXCLUDED.is_static;

-- =========================================================
-- 5) Optional: indexes for JSON settings if you plan to query it
-- =========================================================
CREATE INDEX IF NOT EXISTS ix_dashboard_widgets_settings_gin
    ON dashboard_widgets USING gin (settings);

COMMIT;
