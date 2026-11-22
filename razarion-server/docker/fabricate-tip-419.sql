-- Puts the FABRICATE tip on quest 419 "L12 Build 9 Viper".
--
-- 419 was the only quest from level 9 on without a tip. Its comparison already counts the vipers the
-- player has (includeExisting), so a player with three left has to build six. Those come after the
-- house (35) and the six vipers of 400, with one harvester that often stands still by then, and the
-- harvest quest 421 only comes after 419. Funnel 2026-11-22: 419 passed 76 % -> 61 % (11/18) after
-- 19.11; the stoppers stay 4-14 min and build nothing (recorder: acf91a9d had 3 vipers, built none in
-- 9 min, harvester idle). The FABRICATE tip points at the factory's viper button, and with too little
-- Razarion it first sends a harvester out (tip-decision.ts moneyStep).
--
-- Looks up the factory by internalName, so it runs the same on local and PROD. The quest is read from
-- the DB on every activation: no restart needed; a quest already active keeps its old tip until it is
-- activated again.

SET @factory = (SELECT id FROM BASE_ITEM_TYPE WHERE internalName = 'Factory');

UPDATE QUEST SET tipString = 'FABRICATE', tipActorItemType_id = @factory, tipGroup = 0 WHERE id = 419;

SELECT id, tipString, tipActorItemType_id, tipGroup FROM QUEST WHERE id = 419;
