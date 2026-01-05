INSERT INTO micro_habit_catalog(category, title, notes, tags) VALUES
('mental','Take 3 intentional, deep breaths','Universal grounding technique','["grounding","mindfulness"]'),
('mental','Name 3 things you can see, hear, or feel right now','Grounding during anxiety','["grounding"]'),
('environment','Put one single thing away that is out of place','The "one thing" rule fights overwhelm','["declutter"]'),
('environment','Open a window or door for 5 minutes of fresh air','Changes sensory environment','["fresh_air"]'),
('social','Send one text/emoji to a friend or family member','Choose a safe person','["connection"]'),
('learning','Read one page of a book or article','One paragraph is a win','["learning"]')
ON CONFLICT DO NOTHING;
