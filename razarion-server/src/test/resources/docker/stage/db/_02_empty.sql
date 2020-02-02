-- Create simple level
INSERT INTO LEVEL (id, number, xp2LevelUp)
VALUES (1, 1, 1000);

-- Create admin user (admin@admin.com, pwd: 1234)
INSERT INTO HUMAN_PLAYER_ENTITY (id, sessionId)
VALUES (1, 'XXX');
INSERT INTO USER (id, humanPlayerIdEntity_id, email, passwordHash, verificationDoneDate, admin, level_id, crystals, xp)
VALUES (1, 1, 'admin@admin.com',
        'qKfYO+K4nrC4UZwdquWOMHoOYFw7qNPkhOBR9Df1iCbD+YcPX2ofbNg3H3zHJ+HzXz32oQwYQUC7/K/tP1nAvg==',
        '2020-01-27 20:00:00', true, 1, 0, 0);

-- Create user (user@user.com, pwd: 1234)
INSERT INTO HUMAN_PLAYER_ENTITY (id, sessionId)
VALUES (2, 'YYY');
INSERT INTO USER (id, humanPlayerIdEntity_id, email, passwordHash, verificationDoneDate, admin, level_id, crystals, xp)
VALUES (2, 2, 'user@user.com',
        'qKfYO+K4nrC4UZwdquWOMHoOYFw7qNPkhOBR9Df1iCbD+YcPX2ofbNg3H3zHJ+HzXz32oQwYQUC7/K/tP1nAvg==',
        '2020-01-27 20:00:00', false, 1, 0, 0);
