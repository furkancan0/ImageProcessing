INSERT INTO roles (id, name, description) VALUES (1, 'USER', 'user') ON CONFLICT DO NOTHING;

INSERT INTO users (name, email, password, role_id, private_profile, created_date)
VALUES ('Test User', 'test@gmail.com', '$2a$12$eb0Zm.l.fZhbCeME0BR3KOaGNPVDgXHYR1YGN8MPoFP0h5TVLBo0u', 1, false, now())
    ON CONFLICT DO NOTHING;

INSERT INTO users (name, email, password, role_id, private_profile, created_date)
VALUES ('Test User2', 'test2@gmail.com', '{noop}password2', 1, true, now())
ON CONFLICT DO NOTHING;

INSERT INTO images (id, name, type,description,file_size, image_data, thumbnail, user_id, date)
VALUES
    (1, 'image1.jpg', 'image/jpeg','desc', 1024,null,null, 1, NOW()),
    (2, 'image2.jpg', 'image/jpeg','desc', 2048,null,null, 1, NOW()),
    (3, 'image3.gif', 'image/gif','desc', 512,null,null, 1, NOW()),
    (4, 'image4.jpg', 'image/png','desc', 4096,null,null,  1, NOW()),
    (5, 'image5.jpg', 'image/jpeg','desc', 8192,null,null,  1, NOW())
ON CONFLICT DO NOTHING;