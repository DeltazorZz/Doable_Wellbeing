BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Ensure set_updated_at exists (same helper as V2)
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
-- 1) Appointments: make it "pro"
-- =========================================================

-- Audit columns
ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS created_at timestamptz NOT NULL DEFAULT now(),
    ADD COLUMN IF NOT EXISTS updated_at timestamptz NOT NULL DEFAULT now();

-- Session archive fields
ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS completed_at timestamptz,
    ADD COLUMN IF NOT EXISTS coach_summary text,
    ADD COLUMN IF NOT EXISTS client_reflection text;

-- updated_at trigger
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_appointments_set_updated_at') THEN
CREATE TRIGGER trg_appointments_set_updated_at
    BEFORE UPDATE ON appointments
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
END IF;
END $$;

-- Helpful indexes
CREATE INDEX IF NOT EXISTS ix_appt_client_status_start
    ON appointments (client_id, status, starts_at);

CREATE INDEX IF NOT EXISTS ix_appt_coach_status_start
    ON appointments (coach_id, status, starts_at);


-- =========================================================
-- 2) Appointment files (coach uploads for completed sessions)
-- =========================================================
CREATE TABLE IF NOT EXISTS appointment_files (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id uuid NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,

    uploaded_by uuid NOT NULL REFERENCES users(id) ON DELETE RESTRICT,

    file_name text NOT NULL,
    mime_type text,
    file_size bigint,
    storage_key text NOT NULL,
    description text,
    is_visible_to_client boolean NOT NULL DEFAULT true,

    created_at timestamptz NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS ix_appt_files_appt
    ON appointment_files(appointment_id);

CREATE INDEX IF NOT EXISTS ix_appt_files_uploader
    ON appointment_files(uploaded_by);

CREATE INDEX IF NOT EXISTS ix_appt_files_visible
    ON appointment_files(is_visible_to_client);


-- =========================================================
-- 3) Coach <-> Client relation (recommended for reporting & permissions)
-- =========================================================
CREATE TABLE IF NOT EXISTS coach_clients (
    coach_id uuid NOT NULL REFERENCES coaches(user_id) ON DELETE CASCADE,
    client_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (coach_id, client_id)
    );

CREATE INDEX IF NOT EXISTS ix_coach_clients_client
    ON coach_clients(client_id);

COMMIT;
