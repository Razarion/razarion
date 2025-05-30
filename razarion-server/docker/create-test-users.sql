INSERT INTO USER (email, passwordHash, verificationDoneDate, admin, crystals, xp, level_id, userId)
VALUES ('admin@admin.com',
        '$2a$12$BmpbwogZZcxbt2rIjFULS.oBbjhuecmFQsLj3brjPP5m6eFrESwWy',
        '2020-01-27 20:00:00', true, 0, 0, 272,
        'dc0b3681-8d56-47f0-81a6-1b292f64717e');

-- Create user (user@user.com, pwd: 1234)
INSERT INTO USER (email, passwordHash, verificationDoneDate, admin, crystals, xp, level_id, userId)
VALUES ('user@user.com',
        '$2a$12$BmpbwogZZcxbt2rIjFULS.oBbjhuecmFQsLj3brjPP5m6eFrESwWy',
        '2020-01-27 20:00:00', false, 0, 0, 272,
        '5f5d9792-4efa-457e-8ba1-bfdb2dac12e2');
