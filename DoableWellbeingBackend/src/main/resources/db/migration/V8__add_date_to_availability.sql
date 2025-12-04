ALTER TABLE coach_availabilities
    ADD COLUMN date date,
    ADD COLUMN is_recurring boolean,
    ADD COLUMN series_id uuid,
    ADD COLUMN is_active boolean;

