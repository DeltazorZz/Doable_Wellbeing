-- ==========================================
-- Doable Wellbeing - Full PostgreSQL Schema
-- ==========================================

-- Extensions
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS btree_gin;

-- =====================
-- Enums
-- =====================
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'title_enum') THEN
CREATE TYPE title_enum AS ENUM ('Mr','Mrs','Ms','Miss','Dr','Other');
END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'gender_identity_enum') THEN
CREATE TYPE gender_identity_enum AS ENUM ('male','female','nonbinary','prefer_not_to_say','other');
END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'contact_type') THEN
CREATE TYPE contact_type AS ENUM ('mobile','home_phone','email');
END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'appointment_status') THEN
CREATE TYPE appointment_status AS ENUM ('scheduled','completed','cancelled','no_show');
END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'cadence_type') THEN
CREATE TYPE cadence_type AS ENUM ('daily','weekly','custom');
END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'goal_status') THEN
CREATE TYPE goal_status AS ENUM ('planned','in_progress','paused','completed','cancelled');
END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'reminder_kind') THEN
CREATE TYPE reminder_kind AS ENUM ('habit','appointment','goal');
END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'reminder_channel') THEN
CREATE TYPE reminder_channel AS ENUM ('push','email');
END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'notification_status') THEN
CREATE TYPE notification_status AS ENUM ('queued','sent','read','failed');
END IF;


  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'marital_status_enum') THEN
CREATE TYPE marital_status_enum AS ENUM ('single','married','cohabiting','separated','divorced','widowed','prefer_not_to_say');
END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'accommodation_type_enum') THEN
CREATE TYPE accommodation_type_enum AS ENUM ('owner','private_rent','social_housing','supported_accommodation','homeless','other');
END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'employment_status_enum') THEN
CREATE TYPE employment_status_enum AS ENUM ('employed','self_employed','unemployed','student','retired','unable_to_work','other');
END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'sexual_orientation_enum') THEN
CREATE TYPE sexual_orientation_enum AS ENUM ('heterosexual','gay','lesbian','bisexual','asexual','prefer_not_to_say','other');
END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'religion_enum') THEN
CREATE TYPE religion_enum AS ENUM ('christian','muslim','hindu','sikh','jewish','buddhist','none','prefer_not_to_say','other');
END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'ethnic_origin_enum') THEN
CREATE TYPE ethnic_origin_enum AS ENUM ('white','mixed','asian','black','other','prefer_not_to_say');
END IF;
END$$;

-- ===========
-- Core Users
-- ===========
CREATE TABLE IF NOT EXISTS users (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    email varchar NOT NULL UNIQUE,
    password_hash varchar NOT NULL,


    title title_enum,
    first_name varchar NOT NULL,
    last_name varchar NOT NULL,
    date_of_birth date,
    gender_identity gender_identity_enum,
    is_gender_same_as_assigned_at_birth boolean,
    nhs_number varchar,

    is_active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz,

    CONSTRAINT users_name_min CHECK (length(trim(first_name)) > 0 AND length(trim(last_name)) > 0)
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_nhs_number ON users (nhs_number);
CREATE INDEX IF NOT EXISTS ix_users_name ON users (last_name, first_name);

-- Címek
CREATE TABLE IF NOT EXISTS user_addresses (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    line1 varchar NOT NULL,
    line2 varchar,
    line3 varchar,
    city varchar NOT NULL,
    county varchar,
    postcode varchar NOT NULL,
    is_primary boolean NOT NULL DEFAULT true,
    validated boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL DEFAULT now()
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_user_addresses_primary
    ON user_addresses (user_id)
    WHERE is_primary = true;

CREATE INDEX IF NOT EXISTS ix_user_addresses_postcode ON user_addresses (postcode);

-- Contacts
CREATE TABLE IF NOT EXISTS user_contacts (
                                             id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type contact_type NOT NULL,
    value varchar NOT NULL,

    consent_contact boolean NOT NULL DEFAULT false,
    consent_sms boolean NOT NULL DEFAULT false,
    consent_voicemail boolean NOT NULL DEFAULT false,
    is_primary boolean NOT NULL DEFAULT false,

    created_at timestamptz NOT NULL DEFAULT now(),

    CONSTRAINT uq_user_contacts_value UNIQUE (user_id, type, value)
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_user_contacts_primary_per_type
    ON user_contacts (user_id, type)
    WHERE is_primary = true;

CREATE INDEX IF NOT EXISTS ix_user_contacts_type ON user_contacts (user_id, type);

-- ==========
-- RBAC
-- ==========
CREATE TABLE IF NOT EXISTS roles (
    id int PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name varchar NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id int NOT NULL REFERENCES roles(id) ON DELETE RESTRICT,
    created_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, role_id)
    );

-- ==========
-- Consents
-- ==========
CREATE TABLE IF NOT EXISTS consents (
                                        id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type varchar NOT NULL,
    granted_at timestamptz NOT NULL DEFAULT now(),
    revoked_at timestamptz,
    CHECK (revoked_at IS NULL OR revoked_at >= granted_at)
    );

CREATE INDEX IF NOT EXISTS ix_consents_user_type ON consents (user_id, type, granted_at);

-- =========================================
-- Self-referral form snapshot
-- =========================================
CREATE TABLE IF NOT EXISTS self_referrals (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,


    title title_enum,
    first_name varchar,
    last_name varchar,
    date_of_birth date,
    gender_identity gender_identity_enum,
    is_gender_same_as_assigned_at_birth boolean,
    nhs_number varchar,

    address_line1 varchar,
    address_line2 varchar,
    address_line3 varchar,
    town_city varchar,
    county varchar,
    postcode varchar,

    mobile_phone varchar,
    home_phone varchar,
    email_address varchar,
    consent_mobile_contact boolean,
    consent_sms_reminders boolean,
    consent_mobile_voicemail boolean,
    consent_home_voicemail boolean,
    consent_email_contact boolean,

    -- Presenting problem
    presenting_problem text,
    heard_about_us varchar,

    -- Service enhancements consents
    consent_trainee_shadow boolean NOT NULL DEFAULT false,
    consent_recording boolean NOT NULL DEFAULT false,

    -- About You
    marital_status marital_status_enum,
    accommodation_type accommodation_type_enum,
    employment_status employment_status_enum,
    sexual_orientation sexual_orientation_enum,
    ethnic_origin ethnic_origin_enum,
    religion religion_enum,
    first_language varchar,
    requires_interpreter boolean,
    english_difficulty boolean,
    english_support_details varchar,
    has_disability boolean,
    has_long_term_conditions boolean,
    has_armed_forces_affiliation boolean,
    expecting_or_child_under_24m boolean,

    accepted_privacy_policy boolean NOT NULL DEFAULT false,

    created_at timestamptz NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS ix_self_referrals_user_created ON self_referrals (user_id, created_at);
CREATE INDEX IF NOT EXISTS ix_self_referrals_postcode ON self_referrals (postcode);

-- ==========================
-- Coaching & Appointments
-- ==========================
CREATE TABLE IF NOT EXISTS coaches (
    user_id uuid PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    bio text,
    expertise text,
--  hourly_rate int,
    timezone varchar NOT NULL
    );

CREATE TABLE IF NOT EXISTS clients (
    user_id uuid PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE
    );

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

CREATE TABLE IF NOT EXISTS appointments (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    coach_id uuid NOT NULL REFERENCES coaches(user_id) ON DELETE RESTRICT,
    client_id uuid NOT NULL REFERENCES clients(user_id) ON DELETE RESTRICT,
    starts_at timestamptz NOT NULL,
    ends_at timestamptz NOT NULL,
    status appointment_status NOT NULL DEFAULT 'scheduled',
    notes text,
    CHECK (ends_at > starts_at)
    );

CREATE INDEX IF NOT EXISTS ix_appt_coach_start ON appointments (coach_id, starts_at);
CREATE INDEX IF NOT EXISTS ix_appt_client_start ON appointments (client_id, starts_at);

CREATE TABLE IF NOT EXISTS session_notes (
                                             id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id uuid NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,
    coach_id uuid NOT NULL REFERENCES coaches(user_id) ON DELETE RESTRICT,
    note text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS ix_session_notes_appt_created ON session_notes (appointment_id, created_at);

-- ==================
-- Habits & Tracking
-- ==================
CREATE TABLE IF NOT EXISTS habits (
                                      id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name varchar NOT NULL,
    cadence cadence_type NOT NULL DEFAULT 'daily',
    target int,
    unit varchar,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS ix_habits_user_active ON habits (user_id, is_active);

CREATE TABLE IF NOT EXISTS habit_logs (
                                          id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    habit_id uuid NOT NULL REFERENCES habits(id) ON DELETE CASCADE,
    logged_at date NOT NULL,
    value numeric,
    note text,
    created_at timestamptz NOT NULL DEFAULT now()
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_habit_logs_per_day ON habit_logs (habit_id, logged_at);

-- =========
-- Mood log
-- =========
CREATE TABLE IF NOT EXISTS mood_logs (
                                         id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    logged_at timestamptz NOT NULL DEFAULT now(),
    mood_score int NOT NULL,
    tags text,
    note text,
    CHECK (mood_score BETWEEN 1 AND 10)
    );

CREATE INDEX IF NOT EXISTS ix_mood_logs_user_time ON mood_logs (user_id, logged_at);

-- =====
-- Goals
-- =====
CREATE TABLE IF NOT EXISTS goals (
                                     id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title varchar NOT NULL,
    description text,
    start_date date,
    target_date date,
    status goal_status NOT NULL DEFAULT 'planned',
    CHECK (target_date IS NULL OR start_date IS NULL OR target_date >= start_date)
    );

CREATE INDEX IF NOT EXISTS ix_goals_user_status ON goals (user_id, status);
CREATE INDEX IF NOT EXISTS ix_goals_user_target ON goals (user_id, target_date);

CREATE TABLE IF NOT EXISTS goal_progress (
                                             id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    goal_id uuid NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    logged_at timestamptz NOT NULL DEFAULT now(),
    progress_percent int NOT NULL,
    note text,
    CHECK (progress_percent BETWEEN 0 AND 100)
    );

CREATE INDEX IF NOT EXISTS ix_goal_progress_time ON goal_progress (goal_id, logged_at);

-- =======
-- Badges
-- =======
CREATE TABLE IF NOT EXISTS badges (
                                      id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code varchar NOT NULL UNIQUE,
    name varchar NOT NULL,
    description text,
    is_active boolean NOT NULL DEFAULT true
    );

CREATE TABLE IF NOT EXISTS user_badges (
                                           user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    badge_id uuid NOT NULL REFERENCES badges(id) ON DELETE RESTRICT,
    awarded_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, badge_id)
    );

CREATE INDEX IF NOT EXISTS ix_user_badges_awarded ON user_badges (badge_id, awarded_at);

-- ===============
-- Notifications
-- ===============
CREATE TABLE IF NOT EXISTS notifications (
                                             id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type varchar NOT NULL,
    payload jsonb NOT NULL DEFAULT '{}',
    status notification_status NOT NULL DEFAULT 'queued',
    created_at timestamptz NOT NULL DEFAULT now(),
    read_at timestamptz
    );

CREATE INDEX IF NOT EXISTS ix_notifications_user_status ON notifications (user_id, status, created_at);
CREATE INDEX IF NOT EXISTS ix_notifications_payload_gin ON notifications USING gin (payload);

-- ==========
-- Reminders
-- ==========
CREATE TABLE IF NOT EXISTS reminders (
                                         id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    kind reminder_kind NOT NULL,
    ref_id uuid,
    channel reminder_channel NOT NULL DEFAULT 'push',
    schedule text NOT NULL,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS ix_reminders_user_kind ON reminders (user_id, kind, is_active);

-- =========
-- Articles
-- =========
CREATE TABLE IF NOT EXISTS articles (
                                        id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    slug varchar NOT NULL UNIQUE,
    title varchar NOT NULL,
    body text NOT NULL,
    author_id uuid REFERENCES users(id) ON DELETE SET NULL,
    is_published boolean NOT NULL DEFAULT false,
    published_at timestamptz
    );

CREATE INDEX IF NOT EXISTS ix_articles_pub ON articles (is_published, published_at);
CREATE INDEX IF NOT EXISTS ix_articles_author_pub ON articles (author_id, published_at);

CREATE TABLE IF NOT EXISTS tags (
                                    id int PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                                    name varchar NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS article_tags (
                                            article_id uuid NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    tag_id int NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (article_id, tag_id)
    );

CREATE INDEX IF NOT EXISTS ix_article_tags_tag ON article_tags (tag_id);

-- ===========
-- Audit log
-- ===========
CREATE TABLE IF NOT EXISTS audit_logs (
                                          id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid REFERENCES users(id) ON DELETE SET NULL,
    action varchar NOT NULL,     -- create/update/delete/login/...
    entity varchar NOT NULL,
    entity_id uuid,
    meta jsonb NOT NULL DEFAULT '{}', -- diff, IP, user-agent, stb.
    created_at timestamptz NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS ix_audit_entity ON audit_logs (entity, entity_id, created_at);
CREATE INDEX IF NOT EXISTS ix_audit_user ON audit_logs (user_id, created_at);
CREATE INDEX IF NOT EXISTS ix_audit_meta_gin ON audit_logs USING gin (meta);

-- =============
-- Data exports
-- =============
CREATE TABLE IF NOT EXISTS data_exports (
                                            id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    requested_at timestamptz NOT NULL DEFAULT now(),
    status varchar NOT NULL,  -- pending/processing/ready/failed
    delivered_at timestamptz,
    location varchar
    );

CREATE INDEX IF NOT EXISTS ix_exports_user_time ON data_exports (user_id, requested_at);
CREATE INDEX IF NOT EXISTS ix_exports_status ON data_exports (status);


