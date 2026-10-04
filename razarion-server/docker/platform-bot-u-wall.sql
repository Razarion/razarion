-- The RazCore Platform bot ((Bot1) Water) lives inside a U-shaped wall of land since 2026-10-04 (heightmap of
-- planet 117, opening to the south-west toward the beginners' island). Its realm - every foreign unit inside it is
-- attacked - shrinks to the inside of the U, a circle of radius 46 around 400/282, and its three Hydras patrol
-- deeper inside, at 408-428 / 252-272, 29 or more units from any way round the wall (their range is 15).
--
-- Why: transporters on quest 486 from the east coast steered for the nearest point of the target strip (536/400),
-- and the way there ran through the realm and the Hydra patrol; all 12 transporters lost in the week before
-- 2026-09-30 sank there. With the wall that way goes round the outside; the Hydras of quest 388 stay reachable
-- through the opening.
--
-- Looks the places up through the bot's internal name, so it runs the same on local and PROD. Restart the bots
-- (or warm-restart the planet) afterwards.

SET @realm = (SELECT realm_id FROM BOT_CONFIG WHERE internalName = '(Bot1) Water');
SET @hydra = (SELECT s.spreadPlace_id FROM BOT_CONFIG_BOT_ITEM s
                JOIN BOT_CONFIG_ENRAGEMENT_STATE_CONFIG e ON s.botEnragementStateConfig = e.id
                JOIN BOT_CONFIG b ON e.botConfig = b.id
                JOIN BASE_ITEM_TYPE t ON s.baseItemTypeEntity_id = t.id
              WHERE b.internalName = '(Bot1) Water' AND t.internalName = '(Bot1) Hydra' LIMIT 1);

DELETE FROM PLACE_CONFIG_POSITION_POLYGON WHERE OWNER_ID = @realm;
INSERT INTO PLACE_CONFIG_POSITION_POLYGON (OWNER_ID, x, y, orderColumn)
VALUES (@realm, 446, 282, 0),
       (@realm, 444.4, 293.9, 1),
       (@realm, 439.8, 305, 2),
       (@realm, 432.5, 314.5, 3),
       (@realm, 423, 321.8, 4),
       (@realm, 411.9, 326.4, 5),
       (@realm, 400, 328, 6),
       (@realm, 388.1, 326.4, 7),
       (@realm, 377, 321.8, 8),
       (@realm, 367.5, 314.5, 9),
       (@realm, 360.2, 305, 10),
       (@realm, 355.6, 293.9, 11),
       (@realm, 354, 282, 12),
       (@realm, 355.6, 270.1, 13),
       (@realm, 360.2, 259, 14),
       (@realm, 367.5, 249.5, 15),
       (@realm, 377, 242.2, 16),
       (@realm, 388.1, 237.6, 17),
       (@realm, 400, 236, 18),
       (@realm, 411.9, 237.6, 19),
       (@realm, 423, 242.2, 20),
       (@realm, 432.5, 249.5, 21),
       (@realm, 439.8, 259, 22),
       (@realm, 444.4, 270.1, 23);

DELETE FROM PLACE_CONFIG_POSITION_POLYGON WHERE OWNER_ID = @hydra;
INSERT INTO PLACE_CONFIG_POSITION_POLYGON (OWNER_ID, x, y, orderColumn)
VALUES (@hydra, 408, 252, 0),
       (@hydra, 428, 252, 1),
       (@hydra, 428, 272, 2),
       (@hydra, 408, 272, 3);

SELECT @realm AS realm, @hydra AS hydraSpread,
       (SELECT COUNT(*) FROM PLACE_CONFIG_POSITION_POLYGON WHERE OWNER_ID = @realm) AS realmCorners,
       (SELECT COUNT(*) FROM PLACE_CONFIG_POSITION_POLYGON WHERE OWNER_ID = @hydra) AS hydraCorners;

-- Rollback:
-- DELETE FROM PLACE_CONFIG_POSITION_POLYGON WHERE OWNER_ID IN (@realm, @hydra);
-- INSERT INTO PLACE_CONFIG_POSITION_POLYGON (OWNER_ID, x, y, orderColumn) VALUES
--        (@realm, 364, 247, 0),
--        (@realm, 404, 231, 1),
--        (@realm, 439, 249, 2),
--        (@realm, 437, 321, 3),
--        (@realm, 404, 321, 4),
--        (@realm, 367, 321, 5),
--        (@hydra, 397, 253, 0),
--        (@hydra, 427, 253, 1),
--        (@hydra, 429, 283, 2),
--        (@hydra, 396, 283, 3);
