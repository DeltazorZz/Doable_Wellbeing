-- db/migration/V2__add_supersedes_fk.sql

-- 1) oszlop UUID-ként
ALTER TABLE self_referrals
    ADD COLUMN IF NOT EXISTS supersedes_id UUID;

-- 2) (opcionális) index a gyors kereséshez
CREATE INDEX IF NOT EXISTS idx_self_referrals_supersedes_id
    ON self_referrals (supersedes_id);

-- 3) FK ugyanarra a táblára (önhivatkozó)
ALTER TABLE self_referrals
    ADD CONSTRAINT fk_self_referrals_supersedes
        FOREIGN KEY (supersedes_id) REFERENCES self_referrals (id)
            ON UPDATE CASCADE ON DELETE SET NULL;
