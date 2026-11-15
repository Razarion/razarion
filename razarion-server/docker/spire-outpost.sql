-- The Spire outposts ((Bot2) Spire 1 and 2) become a goal a beginner can reach (2026-11-13).
--
-- Why: in the phase-2 funnel the biggest drop is quest 412 "Destroy 3" (59 % pass). 7 of 12 player stories lost
-- their Vipers at the Spire base and 5 of them quit within 1-2 minutes. 412 had no target: the attack tip sent the
-- player to the nearest enemy, and the 3 bot Vipers (same stats as the player's) came back every 2 minutes.
--
-- 1. Fewer, slower guards: 2 instead of 3 Vipers per outpost, rebuilt after 3 instead of 2 minutes. A destroyed
--    Spire stays down for 60 s instead of 20 s. The player's 6 Vipers of quest 400 win if they attack together.
-- 2. A destroyed Spire drops a box (box type "Phase 2 region 1", 1 crystal): quest 404 "pick up a box" follows
--    right where 403 "destroy a Spire" was won. The Spire type is also used by "(Bot3) Isalnd" - harmless there.
-- 3. Quest 412 counts only the items of the two Spire outposts (QUEST_COMPARISON_BOT); the attack tip only
--    points at those (TipQuestSpec.targetBotIds, needs the client from the same commit).
-- 4. Spire 2 moves from the flat meadow at 945/470 to the plateau on the west shore of the pond, 640/495, 5-7 m
--    high, 30 m from the water, 707 m from the Badger bot (before 960 m). Shift -305/+25. The ground plate leaves
--    out the cells under Rock XL (626/501) and the east column (plants, Rock M at 669/523); at 6.2 m the
--    terrain shows through on 2 % of the plate (5.8: 34 %) and 81 % of it is reached on land from the south;
--    the west edge drops about 1 m to the slope like a terrace.
--
-- Looks everything up through the internal names, so it runs the same on local and PROD. Restart the bots (or
-- warm-restart the planet) afterwards. Rollback values at the end.

SET @bot1 = (SELECT id FROM BOT_CONFIG WHERE internalName = '(Bot2) Spire 1');
SET @bot = (SELECT id FROM BOT_CONFIG WHERE internalName = '(Bot2) Spire 2');
SET @spire = (SELECT id FROM BASE_ITEM_TYPE WHERE internalName = '(Bot2) Spire');
SET @viper = (SELECT id FROM BASE_ITEM_TYPE WHERE internalName = '(Bot1) Viper');
SET @box = (SELECT id FROM BOX_ITEM_TYPE WHERE internalName = 'Phase 2 region 1');
SET @quest412 = (SELECT qc.comparisonConfig_id FROM QUEST q JOIN QUEST_CONDITION qc ON qc.id = q.conditionConfigEntity_id
                 WHERE q.id = 412);

-- 1. Guards and Spire rebuild, both outposts
UPDATE BOT_CONFIG_BOT_ITEM i JOIN BOT_CONFIG_ENRAGEMENT_STATE_CONFIG e ON i.botEnragementStateConfig = e.id
SET i.count = 2, i.rePopTime = 180000
WHERE e.botConfig IN (@bot1, @bot) AND i.baseItemTypeEntity_id = @viper;

UPDATE BOT_CONFIG_BOT_ITEM i JOIN BOT_CONFIG_ENRAGEMENT_STATE_CONFIG e ON i.botEnragementStateConfig = e.id
SET i.rePopTime = 60000
WHERE e.botConfig IN (@bot1, @bot) AND i.baseItemTypeEntity_id = @spire;

-- 2. The Spire drops a box
UPDATE BASE_ITEM_TYPE SET dropBoxItemTypeEntity_id = @box, dropBoxPossibility = 1 WHERE id = @spire;

-- 3. Quest 412 counts the Spire outposts only
DELETE FROM QUEST_COMPARISON_BOT WHERE comparisonConfig = @quest412;
INSERT INTO QUEST_COMPARISON_BOT (comparisonConfig, botConfig) VALUES (@quest412, @bot1), (@quest412, @bot);

-- 4. Spire 2 to the pond
SET @realm = (SELECT realm_id FROM BOT_CONFIG WHERE id = @bot);
SET @spread = (SELECT i.spreadPlace_id FROM BOT_CONFIG_BOT_ITEM i JOIN BOT_CONFIG_ENRAGEMENT_STATE_CONFIG e ON i.botEnragementStateConfig = e.id
               WHERE e.botConfig = @bot AND i.baseItemTypeEntity_id = @viper LIMIT 1);

DELETE FROM PLACE_CONFIG_POSITION_POLYGON WHERE OWNER_ID = @realm;
INSERT INTO PLACE_CONFIG_POSITION_POLYGON (OWNER_ID, x, y, orderColumn)
VALUES (@realm, 660, 467, 0),
       (@realm, 660, 475, 1),
       (@realm, 668, 475, 2),
       (@realm, 668, 523, 3),
       (@realm, 612, 523, 4),
       (@realm, 612, 467, 5);

-- The guards wait between the factory and the Spires, off the rock
DELETE FROM PLACE_CONFIG_POSITION_POLYGON WHERE OWNER_ID = @spread;
INSERT INTO PLACE_CONFIG_POSITION_POLYGON (OWNER_ID, x, y, orderColumn)
VALUES (@spread, 636, 470.7, 0),
       (@spread, 650, 470.7, 1),
       (@spread, 650, 494, 2),
       (@spread, 636, 494, 3);

-- Factory and the two Spires (point places)
UPDATE PLACE_CONFIG p JOIN BOT_CONFIG_BOT_ITEM i ON i.place_id = p.id
    JOIN BOT_CONFIG_ENRAGEMENT_STATE_CONFIG e ON i.botEnragementStateConfig = e.id
SET p.x = p.x - 305, p.y = p.y + 25
WHERE e.botConfig = @bot AND p.x > 900;

UPDATE BOT_CONFIG SET groundBoxHeight = 6.2 WHERE id = @bot;

DELETE FROM BOT_CONFIG_GROUND_BOX_POSITIONS WHERE OWNER_ID = @bot;
INSERT INTO BOT_CONFIG_GROUND_BOX_POSITIONS (OWNER_ID, x, y, orderColumn)
VALUES
       (@bot, 616, 471, 0),
       (@bot, 616, 479, 1),
       (@bot, 616, 487, 2),
       (@bot, 616, 495, 3),
       (@bot, 616, 503, 4),
       (@bot, 616, 511, 5),
       (@bot, 616, 519, 6),
       (@bot, 624, 471, 7),
       (@bot, 624, 479, 8),
       (@bot, 624, 487, 9),
       (@bot, 624, 511, 10),
       (@bot, 624, 519, 11),
       (@bot, 632, 471, 12),
       (@bot, 632, 479, 13),
       (@bot, 632, 487, 14),
       (@bot, 632, 511, 15),
       (@bot, 632, 519, 16),
       (@bot, 640, 471, 17),
       (@bot, 640, 479, 18),
       (@bot, 640, 487, 19),
       (@bot, 640, 495, 20),
       (@bot, 640, 503, 21),
       (@bot, 640, 511, 22),
       (@bot, 640, 519, 23),
       (@bot, 648, 471, 24),
       (@bot, 648, 479, 25),
       (@bot, 648, 487, 26),
       (@bot, 648, 495, 27),
       (@bot, 648, 503, 28),
       (@bot, 648, 511, 29),
       (@bot, 648, 519, 30),
       (@bot, 656, 471, 31),
       (@bot, 656, 479, 32),
       (@bot, 656, 487, 33),
       (@bot, 656, 495, 34),
       (@bot, 656, 503, 35),
       (@bot, 656, 511, 36),
       (@bot, 656, 519, 37);

-- 5. Spire 1 on its new mesa (razarion-ai-content/scripts/west-spire-pond.mjs raises the ground under it to
--    5 m, builds table hills around it and digs a pond east of it): the plate 0.2 m above the plateau, as it was above the meadow, and the
--    plants that would now stand in the pond go.
UPDATE BOT_CONFIG SET groundBoxHeight = 5.2 WHERE id = @bot1;
DELETE FROM TERRAIN_OBJECT_POSITION
WHERE planet = 117 AND id IN (6801, 34787, 34788, 34789, 34790, 34791, 34792, 34793, 34794, 34795, 34796, 36108, 36112, 36115);
-- The plants on the ramps of the table hills and right above and below them (they block units): 101 plants.
DELETE FROM TERRAIN_OBJECT_POSITION
WHERE planet = 117 AND id IN (5566, 5567, 5568, 5569, 5570, 5571, 5572, 5573, 5574, 5575, 5576, 5577, 5722,
                              5723, 5724, 5725, 5726, 5727, 5728, 5729, 6712, 6726, 6783, 35087, 35089, 35092,
                              35133, 35135, 35137, 35199, 35209, 35286, 35290, 35293, 35294, 35299, 35300,
                              35356, 35358, 35361, 35362, 35363, 35364, 35365, 35366, 35367, 35369, 35370,
                              35371, 35372, 35373, 35374, 35375, 35376, 35377, 35378, 35379, 35380, 35381,
                              35492, 35493, 35494, 35496, 35498, 35499, 35528, 35529, 35532, 35544, 35545,
                              35550, 35556, 35558, 35559, 35627, 35628, 35629, 35630, 35644, 35647, 35649,
                              35650, 35676, 35678, 35679, 35681, 35922, 35927, 36043, 36295, 36296, 36297,
                              36298, 36299, 36300, 36301, 36302, 36303, 36304, 36305, 36306);
-- The Rock XXL at 66/631 stood half on the edge of the far north-west mesa (floating on its north side): to the foot
-- of its face, east of the north ramp (at 77/647 it closed a pocket between ramp, face and rock).
UPDATE TERRAIN_OBJECT_POSITION SET x = 92, y = 646 WHERE planet = 117 AND id = 7235;
-- Rocks like at the east pond: a Rock XL on the north-east shore, a Rock M on the south-west shore. The internal name
-- marks them for the rollback.
SET @rockM = (SELECT id FROM TERRAIN_OBJECT WHERE internalName = 'Rock M' AND radius = 4.1);
SET @rockXL = (SELECT id FROM TERRAIN_OBJECT WHERE internalName = 'Rock XL' AND radius = 4.35);   -- the one at the east pond
DELETE FROM TERRAIN_OBJECT_POSITION WHERE planet = 117 AND internalName = 'west-spire-rock';
INSERT INTO TERRAIN_OBJECT_POSITION (x, y, terrainObjectEntity_id, planet, rotationX, rotationY, rotationZ, scaleX, scaleY, scaleZ, internalName)
VALUES (411, 462, @rockXL, 117, 0, 0, 2.1, 1.0, 1.0, 1.0, 'west-spire-rock'),
       (375, 430, @rockM, 117, 0, 0, 4.0, 1.2, 1.2, 1.2, 'west-spire-rock');

-- Rollback (values before 2026-11-13):
--   Spire 1: groundBoxHeight 1.2; the 14 plants of step 5 only from a backup (and the heightmap backup the
--   script writes); the rocks: DELETE FROM TERRAIN_OBJECT_POSITION WHERE internalName = 'west-spire-rock';
--   Rock XXL 7235 back to x 65.83060470478742, y 631.0581524463215; the ramp plants only from a backup
--   Vipers count 3, rePopTime 120000; Spire rePopTime 20000 (both outposts)
--   BASE_ITEM_TYPE (Bot2) Spire: dropBoxItemTypeEntity_id NULL, dropBoxPossibility 0
--   QUEST_COMPARISON_BOT: no rows for quest 412
--   Spire 2: realm (965,442) (965,450) (973,450) (973,498) (917,498) (917,442); spread (933.75,445.68)
--   (952.70,445.77) (953.82,478.35) (934.17,478.63); factory 944.71/486.72, Spires 960.88/477.73 and
--   960.91/461.90; groundBoxHeight 0.7; ground plate = the cells above shifted by +305/-25, plus the column
--   x=969 (y 454-494) and the cells 929/937 x 470/478.
