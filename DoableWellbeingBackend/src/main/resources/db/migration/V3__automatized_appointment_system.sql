ALTER TYPE appointment_status
    ADD VALUE IF NOT EXISTS 'requested';
ALTER TYPE appointment_status
    ADD VALUE IF NOT EXISTS 'declined';



CREATE INDEX IF NOT EXISTS ix_appt_coach_status_start
    ON appointments (coach_id, status, starts_at);

ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS external_calendar_id varchar,
    ADD COLUMN IF NOT EXISTS external_calendar_provider varchar,
    ADD COLUMN IF NOT EXISTS meeting_url varchar,
    ADD COLUMN IF NOT EXISTS confirmed_at timestamptz;