INSERT INTO USER (email, passwordHash, verificationDoneDate, admin, crystals, xp, level_id)
VALUES ('admin@admin.com',
        'qKfYO+K4nrC4UZwdquWOMHoOYFw7qNPkhOBR9Df1iCbD+YcPX2ofbNg3H3zHJ+HzXz32oQwYQUC7/K/tP1nAvg==',
        '2020-01-27 20:00:00', true, 0, 0, 272);

-- Create user (user@user.com, pwd: 1234)
INSERT INTO USER (email, passwordHash, verificationDoneDate, admin, crystals, xp, level_id)
VALUES ('user@user.com',
        'qKfYO+K4nrC4UZwdquWOMHoOYFw7qNPkhOBR9Df1iCbD+YcPX2ofbNg3H3zHJ+HzXz32oQwYQUC7/K/tP1nAvg==',
        '2020-01-27 20:00:00', false, 0, 0, 272);