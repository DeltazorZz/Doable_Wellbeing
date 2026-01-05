INSERT INTO modules (code, name, description) VALUES
('upcoming_meetings',  'Upcoming Meetings',  'Your next coaching sessions'),
('completed_meetings', 'Completed Meetings', 'Past sessions, coach notes, and shared resources'),
('habit_tracker',      'Habit Tracker',      'Daily habits and micro-habits'),
('mood_chart',         'Mood Chart',         'Mood timeline and trends'),
('goals_progress',     'Goals Progress',     'Objectives and milestones'),
('wheel_of_life',      'Wheel of Life',      'Self-scoring life areas')
ON CONFLICT (code) DO NOTHING;
