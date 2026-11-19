-- The later harvest quests get the same tip as the first ones (2026-11-19).
--
-- Why: 363, 366 and 490 point the player at a harvester and then at the nearest Razarion field;
-- 402 (L10), 421, 437 and 481 had no tip config at all. On the Phase 2 mainland the 100 fields of
-- "Phase 2 region 1" are scattered over 1260 x 850 m, so the nearest one is usually off screen -
-- a phone test found 402 to be a break: nothing said where to go (2026-11-17).
-- Read on quest activation, no restart needed. Rollback: set the three columns back to NULL.

UPDATE QUEST SET tipString = 'HARVEST', tipActorItemType_id = 2, tipGroup = 0
WHERE id IN (402, 421, 437, 481) AND tipString IS NULL;

SELECT id, tipString, tipActorItemType_id, tipGroup FROM QUEST WHERE id IN (363, 366, 490, 402, 421, 437, 481);
