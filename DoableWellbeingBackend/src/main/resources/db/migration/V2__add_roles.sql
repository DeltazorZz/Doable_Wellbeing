INSERT INTO roles (name) VALUES
    ('admin'),
    ('coach'),
    ('client'),
    ('user')
ON CONFLICT (name) DO NOTHING;