-- The Badger bot becomes "the Badger's den" (2026-11-17): one Badger on its hill, with a stash to
-- loot around it and a treasure box when it falls. Quest 423 "Destroy 1 Badger" counts only this bot.
--
-- Why: 423 is the weakest step of the whole funnel (33 %, 3 of 9 from 15.11. 20:56 on, PROD). The
-- quest counted any (Bot2) Badger, so the attack tip sent the player to the nearest one - often
-- Beach mountain (5 Badgers + 2 Teslas, region 3), where 9 Vipers die in seconds. One player lost 19
-- Vipers there and then destroyed the two harmless Messengers for 12 hours without progress.
--
-- 1. The bot on its hill (realm and ground plate stay, 50 m plateau at 112-232 / 992-1080): one
--    Badger in the middle, back after 60 s; no builder, factory and Vipers any more. Three Refineries
--    (15 hp, unarmed) stand around it as its stash: something to blow up, not counted by the quest.
-- 2. The Badger drops a box "Badger treasure" with 3 crystals: quest 424 "Pick up 1 box" follows
--    where 423 was won. The drop belongs to the item type, so every other bot with Badgers gets
--    noBoxDrop (new BOT_CONFIG column, needs the server from the same commit).
-- 3. Quest 423 counts only this bot (QUEST_COMPARISON_BOT); the attack tip only points here.
--
-- Looks everything up by internal names, so it runs the same locally and on PROD. Restart the bots
-- afterwards (POST /rest/planet-mgmt-controller/restartBots). Rollback at the end.

SET @bot = (SELECT id FROM BOT_CONFIG WHERE internalName = '(Bot2) Badger');
SET @state = (SELECT id FROM BOT_CONFIG_ENRAGEMENT_STATE_CONFIG WHERE botConfig = @bot);
SET @badger = (SELECT id FROM BASE_ITEM_TYPE WHERE internalName = '(Bot2) Badger');
SET @refinery = (SELECT id FROM BASE_ITEM_TYPE WHERE internalName = '(Bot1) Refinery');
SET @quest423 = (SELECT c.comparisonConfig_id FROM QUEST q JOIN QUEST_CONDITION c ON c.id = q.conditionConfigEntity_id WHERE q.id = 423);

-- 1. The den
DELETE FROM BOT_CONFIG_BOT_ITEM WHERE botEnragementStateConfig = @state;
UPDATE BOT_CONFIG SET name = 'Old Badger' WHERE id = @bot;

INSERT INTO PLACE_CONFIG (x, y, radius, internalName) VALUES (172, 1038, 1, 'badger-den');
SET @p = LAST_INSERT_ID();
INSERT INTO BOT_CONFIG_BOT_ITEM (angle, count, createDirectly, moveRealmIfIdle, noRebuild, noSpawn, rePopTime, baseItemTypeEntity_id, place_id, botEnragementStateConfig, internalName, placeNearCenter)
VALUES (0, 1, 1, 0, 0, 0, 60000, @badger, @p, @state, 'badger-den', 0);

INSERT INTO PLACE_CONFIG (x, y, radius, internalName) VALUES (152, 1028, 1, 'badger-den');
SET @p = LAST_INSERT_ID();
INSERT INTO BOT_CONFIG_BOT_ITEM (angle, count, createDirectly, moveRealmIfIdle, noRebuild, noSpawn, rePopTime, baseItemTypeEntity_id, place_id, botEnragementStateConfig, internalName, placeNearCenter)
VALUES (0.6, 1, 1, 0, 0, 0, 120000, @refinery, @p, @state, 'badger-den', 0);

INSERT INTO PLACE_CONFIG (x, y, radius, internalName) VALUES (168, 1018, 1, 'badger-den');
SET @p = LAST_INSERT_ID();
INSERT INTO BOT_CONFIG_BOT_ITEM (angle, count, createDirectly, moveRealmIfIdle, noRebuild, noSpawn, rePopTime, baseItemTypeEntity_id, place_id, botEnragementStateConfig, internalName, placeNearCenter)
VALUES (2.1, 1, 1, 0, 0, 0, 120000, @refinery, @p, @state, 'badger-den', 0);

INSERT INTO PLACE_CONFIG (x, y, radius, internalName) VALUES (150, 1050, 1, 'badger-den');
SET @p = LAST_INSERT_ID();
INSERT INTO BOT_CONFIG_BOT_ITEM (angle, count, createDirectly, moveRealmIfIdle, noRebuild, noSpawn, rePopTime, baseItemTypeEntity_id, place_id, botEnragementStateConfig, internalName, placeNearCenter)
VALUES (4.0, 1, 1, 0, 0, 0, 120000, @refinery, @p, @state, 'badger-den', 0);

-- 2. The treasure, and no boxes from the other Badger bots
INSERT INTO BOX_ITEM_TYPE (internalName, fixVerticalNorm, radius, terrainType, ttl, i18nDescription_id, i18nName_id, thumbnail_id, model3DEntity_id, thumb_alpha, thumb_beta, thumb_diplomacy, thumb_radius, thumb_target_x, thumb_target_y, thumb_target_z)
SELECT 'Badger treasure', fixVerticalNorm, radius, terrainType, ttl, i18nDescription_id, i18nName_id, thumbnail_id, model3DEntity_id, thumb_alpha, thumb_beta, thumb_diplomacy, thumb_radius, thumb_target_x, thumb_target_y, thumb_target_z
FROM BOX_ITEM_TYPE WHERE internalName = 'Phase 2 region 1';
SET @box = LAST_INSERT_ID();
INSERT INTO BOX_ITEM_TYPE_POSSIBILITY (crystals, possibility, boxItemTypePossibilities_id) VALUES (3, 1, @box);
UPDATE BASE_ITEM_TYPE SET dropBoxItemTypeEntity_id = @box, dropBoxPossibility = 1 WHERE id = @badger;

UPDATE BOT_CONFIG c SET c.noBoxDrop = 1 WHERE c.id <> @bot AND c.id IN (
    SELECT e.botConfig FROM BOT_CONFIG_ENRAGEMENT_STATE_CONFIG e JOIN BOT_CONFIG_BOT_ITEM i ON i.botEnragementStateConfig = e.id
    WHERE i.baseItemTypeEntity_id = @badger);

-- 3. Quest 423 counts the den only
DELETE FROM QUEST_COMPARISON_BOT WHERE comparisonConfig = @quest423;
INSERT INTO QUEST_COMPARISON_BOT (comparisonConfig, botConfig) VALUES (@quest423, @bot);

SELECT c.internalName, c.name, t.internalName type, i.count, i.rePopTime, p.x, p.y
FROM BOT_CONFIG c JOIN BOT_CONFIG_ENRAGEMENT_STATE_CONFIG e ON e.botConfig = c.id
JOIN BOT_CONFIG_BOT_ITEM i ON i.botEnragementStateConfig = e.id JOIN BASE_ITEM_TYPE t ON t.id = i.baseItemTypeEntity_id
LEFT JOIN PLACE_CONFIG p ON p.id = i.place_id WHERE c.id = @bot;
SELECT id, internalName FROM BOT_CONFIG WHERE noBoxDrop = 1;

-- Rollback (values before 2026-11-17):
--   bot items of '(Bot2) Badger' (state 4510): (Bot1) Builder count 1 rePop 30000 createDirectly 1 place (145.10, 1054.10) r 20;
--   (Bot1) Factory count 1 rePop 240000 createDirectly 0 place (135.86, 1008.08) r 1; (Bot1) Viper count 3 rePop 20
--   createDirectly 0 spreadPlace 17664; (Bot2) Badger count 1 rePop 20000 createDirectly 0 spreadPlace 17665.
--   PLACE_CONFIG ids 17662/17663 and spread places 17664/17665 stay in the DB (orphaned), use them for the rollback.
--   BOT_CONFIG name '' ; BASE_ITEM_TYPE (Bot2) Badger: dropBoxItemTypeEntity_id NULL, dropBoxPossibility 0;
--   BOT_CONFIG noBoxDrop 0 everywhere; QUEST_COMPARISON_BOT: no rows for quest 423;
--   DELETE FROM BOX_ITEM_TYPE_POSSIBILITY / BOX_ITEM_TYPE for 'Badger treasure'; DELETE FROM PLACE_CONFIG WHERE internalName = 'badger-den'.
