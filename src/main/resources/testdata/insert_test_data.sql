INSERT INTO roles (id, name, description) VALUES (1, 'USER', 'user') ON CONFLICT DO NOTHING;

INSERT INTO users (name, email, password, role_id, private_profile, created_date)
VALUES ('Test User', 'test@gmail.com', '$2a$12$eb0Zm.l.fZhbCeME0BR3KOaGNPVDgXHYR1YGN8MPoFP0h5TVLBo0u', 1, false, now())
    ON CONFLICT DO NOTHING;

INSERT INTO users (name, email, password, role_id, private_profile, created_date)
VALUES ('Test User2', 'test2@gmail.com', '{noop}password2', 1, true, now())
ON CONFLICT DO NOTHING;

INSERT INTO images (id, name, type, file_size, deleted, user_id, image_date)
VALUES
    (1, 'image1.jpg', 'image/jpeg', 1024, FALSE, 1, NOW()),
    (2, 'image2.jpg', 'image/jpeg', 2048, TRUE, 1, NOW()),
    (3, 'image3.gif', 'image/gif', 512, FALSE, 1, NOW()),
    (4, 'image4.jpg', 'image/png', 4096, FALSE, 1, NOW()),
    (5, 'image5.jpg', 'image/jpeg', 8192, FALSE, 1, NOW())
ON CONFLICT DO NOTHING;