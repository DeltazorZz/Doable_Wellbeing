UPDATE coach_availabilities
SET is_active = COALESCE(is_active, true),
    is_recurring = COALESCE(is_recurring, false);

ALTER TABLE coach_availabilities
    ALTER COLUMN is_active SET NOT NULL,
    ALTER COLUMN is_recurring SET NOT NULL,
    ALTER COLUMN date SET NOT NULL;