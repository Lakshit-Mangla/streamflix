-- Seed genres
INSERT IGNORE INTO genres (name) VALUES
('Action'), ('Comedy'), ('Drama'), ('Sci-Fi'), ('Horror'),
('Thriller'), ('Romance'), ('Documentary'), ('Animation'), ('Fantasy');

-- Seed a handful of sample movies
-- video_url values are free, public-domain sample clips (Blender Foundation open films,
-- Creative Commons licensed) hosted by Google for exactly this kind of testing purpose.
INSERT IGNORE INTO movies (id, title, description, release_year, duration_minutes, poster_url, video_url, content_type, average_rating, view_count, cast_members, director)
VALUES
(1, 'Neon Horizon', 'A hacker uncovers a conspiracy in a near-future megacity.', 2023, 128, NULL, 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', 'MOVIE', 0.0, 0, 'A. Rivera, J. Chen', 'M. Alvarez'),
(2, 'Quiet Static', 'A grieving father moves to a small town harboring a secret.', 2022, 104, NULL, NULL, 'MOVIE', 0.0, 0, 'T. Ford, L. Nakamura', 'S. Whitfield'),
(3, 'The Last Ember', 'Survivors of a collapsed civilization search for a mythical flame.', 2021, 137, NULL, 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 'MOVIE', 0.0, 0, 'K. Obi, R. Santos', 'D. Kim'),
(4, 'Laugh Track', 'A washed-up sitcom writer gets one more shot at a comeback.', 2024, 96, NULL, NULL, 'MOVIE', 0.0, 0, 'P. Grant, M. Osei', 'C. Bianchi'),
(5, 'Static Bloom', 'A botanist and an AI unravel the cause of a global bloom event.', 2023, 112, NULL, 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4', 'DOCUMENTARY', 0.0, 0, 'Narrated by E. Voss', 'H. Lindqvist');

INSERT IGNORE INTO movie_genres (movie_id, genre_id) VALUES
(1, 1), (1, 4), (1, 6),
(2, 3), (2, 6),
(3, 4), (3, 10),
(4, 2),
(5, 8), (5, 4);

-- If these movies already existed from a previous run (INSERT IGNORE skips them),
-- make sure they still get the video URLs added above.
UPDATE movies SET video_url = 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4' WHERE id = 1 AND (video_url IS NULL OR video_url = '');
UPDATE movies SET video_url = 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4' WHERE id = 3 AND (video_url IS NULL OR video_url = '');
UPDATE movies SET video_url = 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4' WHERE id = 5 AND (video_url IS NULL OR video_url = '');
