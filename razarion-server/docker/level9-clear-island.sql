-- Level 9 rebuilt around leaving the noob island (2026-10-14):
--   1 clear the noob island (new REGION_CLEARED, one button sells everything)   20 XP
--   2 factory in region 122            (395)                                    15 XP
--   3 a harvester                       (new)                                   10 XP
--   4 harvest 20                        (new)                                   10 XP
--   5 radar and powerplant in region 122 (396)                                  15 XP
--   6 six vipers in region 122          (400, without the harvester)            10 XP
-- 80 XP as before. 393 (sell the factory), Sell the Harvester and 401 (sell the dockyard) leave the
-- level; the quests themselves stay for the history.
--
-- Why: the old level sold the island piece by piece between the first buildings on the new one, and
-- 396 lost the players with no money left (PROD 28.09.-14.10.: 57/76; 4 of the 19 who left hung on
-- NO_MONEY, the rest left right after 395). Selling everything first pays for the new base; the
-- harvester and harvesting come before radar and powerplant.
--
-- Needs the server build with REGION_CLEARED. Looks everything up by quest id and internalName, so it
-- runs the same on local and PROD. Run once, then a warm restart of the planet.

SET @factory_sale = 393;
SET @dock_sale = 401;
SET @harvester_sale = (SELECT id FROM QUEST WHERE internalName = 'Sell the Harvester');
SET @factory_quest = 395;
SET @radar_quest = 396;
SET @viper_quest = 400;
SET @level_group = (SELECT serverLevelQuest FROM SERVER_LEVEL_QUEST_ENTRY WHERE quest_id = @factory_quest);
SET @level9 = (SELECT minimalLevel_id FROM SERVER_LEVEL_QUEST WHERE id = @level_group);
SET @factory = (SELECT id FROM BASE_ITEM_TYPE WHERE internalName = 'Factory');
SET @harvester = (SELECT id FROM BASE_ITEM_TYPE WHERE internalName = 'Harvester');

-- The noob island with the water around it, where the dockyard and the hydras lie: x and y 0-340.
-- The platform island starts at x 355, the Phase 2 coast is about 200 further out, and the builder and
-- transporter are already across (~466/485) when level 9 starts.
INSERT INTO PLACE_CONFIG (x, y, radius, internalName) VALUES (NULL, NULL, NULL, 'Quest: noob island and its water');
SET @place_island = LAST_INSERT_ID();
INSERT INTO PLACE_CONFIG_POSITION_POLYGON (OWNER_ID, x, y, orderColumn) VALUES
  (@place_island, 0, 0, 0),
  (@place_island, 340, 0, 1),
  (@place_island, 340, 340, 2),
  (@place_island, 0, 340, 3);

-- 1 clear the island
INSERT INTO QUEST_COMPARISON (count, time, placeConfig_id, includeExisting, startRegionId)
VALUES (NULL, NULL, @place_island, 0, NULL);
SET @cmp_clear = LAST_INSERT_ID();
INSERT INTO QUEST_CONDITION (conditionTrigger, comparisonConfig_id) VALUES ('REGION_CLEARED', @cmp_clear);
SET @cond_clear = LAST_INSERT_ID();
INSERT INTO QUEST (crystal, internalName, razarion, xp, conditionConfigEntity_id, tipString, tipActorItemType_id, switchable)
SELECT 0, 'Clear the Noob Island', 0, 20, @cond_clear, 'CLEAR', @factory, switchable FROM QUEST WHERE id = @factory_quest;
SET @quest_clear = LAST_INSERT_ID();

-- 3 a harvester; one already standing counts
INSERT INTO QUEST_COMPARISON (count, time, placeConfig_id, includeExisting, startRegionId)
VALUES (NULL, NULL, NULL, 1, NULL);
SET @cmp_harvester = LAST_INSERT_ID();
INSERT INTO QUEST_COMPARISON_BASE_ITEM (ComparisonConfigEntity_id, typeCount, baseItemTypeEntityId)
VALUES (@cmp_harvester, 1, @harvester);
INSERT INTO QUEST_CONDITION (conditionTrigger, comparisonConfig_id) VALUES ('SYNC_ITEM_CREATED', @cmp_harvester);
SET @cond_harvester = LAST_INSERT_ID();
INSERT INTO QUEST (crystal, internalName, razarion, xp, conditionConfigEntity_id, tipString, tipActorItemType_id, switchable)
SELECT 0, 'Build a Harvester', 0, 10, @cond_harvester, 'FABRICATE', @factory, switchable FROM QUEST WHERE id = @factory_quest;
SET @quest_harvester = LAST_INSERT_ID();

-- 4 harvest 20
INSERT INTO QUEST_COMPARISON (count, time, placeConfig_id, includeExisting, startRegionId)
VALUES (20, NULL, NULL, 0, NULL);
SET @cmp_harvest = LAST_INSERT_ID();
INSERT INTO QUEST_CONDITION (conditionTrigger, comparisonConfig_id) VALUES ('HARVEST', @cmp_harvest);
SET @cond_harvest = LAST_INSERT_ID();
INSERT INTO QUEST (crystal, internalName, razarion, xp, conditionConfigEntity_id, tipString, tipActorItemType_id, switchable)
SELECT 0, 'Harvest on Phase 2', 0, 10, @cond_harvest, 'HARVEST', @harvester, switchable FROM QUEST WHERE id = @factory_quest;
SET @quest_harvest = LAST_INSERT_ID();

-- 6 vipers only: the harvester has its own quest now
DELETE FROM QUEST_COMPARISON_BASE_ITEM
WHERE baseItemTypeEntityId = @harvester
  AND ComparisonConfigEntity_id = (SELECT c.comparisonConfig_id FROM QUEST q JOIN QUEST_CONDITION c ON c.id = q.conditionConfigEntity_id WHERE q.id = @viper_quest);

UPDATE QUEST SET xp = 15 WHERE id IN (@factory_quest, @radar_quest);
UPDATE QUEST SET xp = 10 WHERE id = @viper_quest;

-- The level's order
DELETE FROM SERVER_LEVEL_QUEST_ENTRY WHERE serverLevelQuest = @level_group;
INSERT INTO SERVER_LEVEL_QUEST_ENTRY (orderColumn, quest_id, serverLevelQuest) VALUES
  (0, @quest_clear, @level_group),
  (1, @factory_quest, @level_group),
  (2, @quest_harvester, @level_group),
  (3, @quest_harvest, @level_group),
  (4, @radar_quest, @level_group),
  (5, @viper_quest, @level_group);

-- Migration of the players in level 9 (feedback_quest_split_migrate_active_players):
-- whoever passed 400 had a harvester and harvested: the two new ones count as done.
INSERT INTO USER_COMPLETED_QUEST (`razarion-user`, quest)
SELECT u.id, q.id FROM RAZARION_USER u JOIN QUEST q ON q.id IN (@quest_harvester, @quest_harvest)
WHERE u.level_id = @level9
  AND EXISTS (SELECT 1 FROM USER_COMPLETED_QUEST c WHERE c.`razarion-user` = u.id AND c.quest = @viper_quest);
-- A removed quest active: none, the server activates the first open one of the level.
UPDATE RAZARION_USER SET activeQuest_id = NULL
WHERE level_id = @level9 AND activeQuest_id IN (@factory_sale, @harvester_sale, @dock_sale);
-- XP in level 9 = what the new quests they have done are worth, so nobody ends above or below the 80.
UPDATE RAZARION_USER u SET u.xp = (
  SELECT COALESCE(SUM(q.xp), 0) FROM USER_COMPLETED_QUEST c JOIN QUEST q ON q.id = c.quest
  WHERE c.`razarion-user` = u.id
    AND q.id IN (@quest_clear, @factory_quest, @quest_harvester, @quest_harvest, @radar_quest, @viper_quest))
WHERE u.level_id = @level9;
SELECT ROW_COUNT() AS level9_users;

SELECT e.orderColumn, q.id, q.internalName, q.xp, q.tipString, c.conditionTrigger
FROM SERVER_LEVEL_QUEST_ENTRY e JOIN QUEST q ON q.id = e.quest_id JOIN QUEST_CONDITION c ON c.id = q.conditionConfigEntity_id
WHERE e.serverLevelQuest = @level_group ORDER BY e.orderColumn;
