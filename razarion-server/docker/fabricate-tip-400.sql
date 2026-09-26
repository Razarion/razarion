-- Puts the FABRICATE tip on quest 400: a harvester and six vipers in the Phase 2 start region.
--
-- The last quest of level 9 without a tip. Level 9 allows one harvester and six vipers, and the
-- old ones on the noob island fill that limit and cannot cross; the tip sends the player back to
-- sell them. Needs the client whose FABRICATE tip reads the region and the limit off the base
-- (2026-09-25). Looks up the factory by internalName, so it runs the same on local and PROD. The
-- quest is read from the DB on every activation: no restart needed.

SET @factory = (SELECT id FROM BASE_ITEM_TYPE WHERE internalName = 'Factory');

UPDATE QUEST SET tipString = 'FABRICATE', tipActorItemType_id = @factory WHERE id = 400;

SELECT id, tipString, tipActorItemType_id FROM QUEST WHERE id = 400;
