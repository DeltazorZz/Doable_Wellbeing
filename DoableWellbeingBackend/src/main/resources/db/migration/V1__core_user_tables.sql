-- ==========================================
-- Doable Wellbeing - PostgreSQL Schema
-- ==========================================


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
    id uuid PRIMARY KEY ,
    email varchar NOT NULL,
    password_hash varchar NOT NULL,
    password_salt bytea NOT NULL,
    first_name varchar NOT NULL,
    last_name varchar NOT NULL,
    date_of_birth date,
    nhs_number varchar,
    is_active boolean NOT NULL DEFAULT true,
    is_deleted boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz,

    CONSTRAINT users_name_min CHECK (length(trim(first_name)) > 0 AND length(trim(last_name)) > 0));

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_nhs_number ON users (nhs_number);
CREATE INDEX IF NOT EXISTS ix_users_name ON users (last_name, first_name);
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email_lower ON users (lower(email));

ALTER TABLE users
    ADD CONSTRAINT users_deleted_flags_consistent
        CHECK (
            (is_deleted AND deleted_at IS NOT NULL)
                OR (NOT is_deleted AND deleted_at IS NULL)
            );

-- Címek
CREATE TABLE IF NOT EXISTS user_addresses (
    id uuid PRIMARY KEY,
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

CREATE INDEX IF NOT EXISTS ix_user_roles_role ON user_roles(role_id);
-- =========================================
-- Self-referral form snapshot
-- =========================================
CREATE TABLE IF NOT EXISTS self_referrals (
    -- Id
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    supersedes_id UUID,

    -- Contacts
    mobile_phone varchar,
    home_phone varchar,


    -- Presenting problem
    presenting_problem text,
    heard_about_us varchar,


    -- About You
    title title_enum,
    gender_identity gender_identity_enum,
    is_gender_same_as_assigned_at_birth boolean,
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

    --Timestamp
    created_at timestamptz NOT NULL DEFAULT now()
    );

ALTER TABLE self_referrals
    ADD CONSTRAINT self_referrals_supersedes_fk
        FOREIGN KEY (supersedes_id) REFERENCES self_referrals(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS ix_self_referrals_supersedes ON self_referrals(supersedes_id);
CREATE INDEX IF NOT EXISTS ix_self_referrals_user_created ON self_referrals (user_id, created_at);

-- ==========================
-- Coaching & Appointments
-- ==========================
CREATE TABLE IF NOT EXISTS coaches (
    user_id uuid PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    bio text,
    expertise text,
    timezone varchar NOT NULL
    );


CREATE TABLE IF NOT EXISTS appointments (
    id uuid PRIMARY KEY,
    coach_id uuid NOT NULL REFERENCES coaches(user_id) ON DELETE RESTRICT,
    client_id uuid NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    starts_at timestamptz NOT NULL,
    ends_at timestamptz NOT NULL,
    status appointment_status NOT NULL DEFAULT 'scheduled',
    notes text,
    CHECK (ends_at > starts_at)
    );

CREATE INDEX IF NOT EXISTS ix_appt_coach_start ON appointments (coach_id, starts_at);
CREATE INDEX IF NOT EXISTS ix_appt_client_start ON appointments (client_id, starts_at);
