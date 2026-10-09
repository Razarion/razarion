-- A quest of its own for selling the old harvester, right after quest 393 (sell the factory).
--
-- Level 9 allows one harvester, and the one from the noob island fills it. Quest 400 wants a
-- harvester and six vipers in the Phase 2 region, so its FABRICATE tip turned into "go back and sell
-- the old one" halfway through a build quest. PROD 28.09.-09.10.: 400 passed 23 of 35, the highest
-- loss after the island; the players who left built their vipers, never the harvester, and hung in
-- AWAIT_SELL_CLICK / ITEM_PANEL_CLOSED / ACTOR_OUT_OF_VIEW on the noob island. A sale as a quest of its
-- own works: 393 (the factory, same island) passed 49 of 54, 401 (the dockyard) 35 of 37.
--
-- Next to 393 because that is when the noob island is cleared, the harvester is certainly still
-- there, and its money pays for the new factory. Its 10 XP come from 400 (20 -> 10): level 9 still
-- adds up to 80.
--
-- Players already past 393 in level 9 get the new quest credited with its 10 XP, or they would be
-- sent back to sell a harvester after 400 (see feedback_quest_split_migrate_active_players).
--
-- Looks up everything by quest id and internalName, so it runs the same on local and PROD. Run once.
-- Quests are read from the DB on every activation; no restart needed.

SET @factory_sale = 393;
SET @harvester = (SELECT id FROM BASE_ITEM_TYPE WHERE internalName = 'Harvester');
SET @level_group = (SELECT serverLevelQuest FROM SERVER_LEVEL_QUEST_ENTRY WHERE quest_id = @factory_sale);
SET @after = (SELECT orderColumn FROM SERVER_LEVEL_QUEST_ENTRY WHERE quest_id = @factory_sale);
SET @level9 = (SELECT minimalLevel_id FROM SERVER_LEVEL_QUEST WHERE id = @level_group);

INSERT INTO QUEST_COMPARISON (count, time, placeConfig_id, includeExisting, startRegionId)
VALUES (NULL, NULL, NULL, 0, NULL);
SET @cmp = LAST_INSERT_ID();
INSERT INTO QUEST_COMPARISON_BASE_ITEM (ComparisonConfigEntity_id, typeCount, baseItemTypeEntityId)
VALUES (@cmp, 1, @harvester);
INSERT INTO QUEST_CONDITION (conditionTrigger, comparisonConfig_id) VALUES ('SELL', @cmp);
SET @cond = LAST_INSERT_ID();
INSERT INTO QUEST (crystal, internalName, razarion, xp, conditionConfigEntity_id, tipString, tipActorItemType_id, switchable)
SELECT 0, 'Sell the Harvester', 0, 10, @cond, 'SELL', @harvester, switchable FROM QUEST WHERE id = @factory_sale;
SET @quest = LAST_INSERT_ID();

UPDATE QUEST SET xp = 10 WHERE id = 400;

UPDATE SERVER_LEVEL_QUEST_ENTRY SET orderColumn = orderColumn + 1
WHERE serverLevelQuest = @level_group AND orderColumn > @after;
INSERT INTO SERVER_LEVEL_QUEST_ENTRY (orderColumn, quest_id, serverLevelQuest)
VALUES (@after + 1, @quest, @level_group);

-- Migration: level 9, 393 already done
UPDATE RAZARION_USER u SET u.xp = u.xp + 10
WHERE u.level_id = @level9
  AND EXISTS (SELECT 1 FROM USER_COMPLETED_QUEST c WHERE c.`razarion-user` = u.id AND c.quest = @factory_sale);
INSERT INTO USER_COMPLETED_QUEST (`razarion-user`, quest)
SELECT u.id, @quest FROM RAZARION_USER u
WHERE u.level_id = @level9
  AND EXISTS (SELECT 1 FROM USER_COMPLETED_QUEST c WHERE c.`razarion-user` = u.id AND c.quest = @factory_sale);
SELECT ROW_COUNT() AS migrated_users;

SELECT e.orderColumn, q.id, q.internalName, q.xp, q.tipString, q.switchable
FROM SERVER_LEVEL_QUEST_ENTRY e JOIN QUEST q ON q.id = e.quest_id
WHERE e.serverLevelQuest = @level_group ORDER BY e.orderColumn;
