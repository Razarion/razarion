-- Puts the BUILD tip on the two rebuild quests of level 9: 395 the factory, 396 radar and powerplant.
--
-- Both passed at 97 % on PROD before the transport tips (31/32, 30/31, since 25.07.2026) - the
-- players who got that far had found everything themselves. The tips now carry players there who
-- were shown every step before, and 393 showed what the first quest without a tip does to them.
-- Both count what stands in the Phase 2 start region (122), and level 9 allows one radar and one
-- powerplant: the ones from level 3 on the noob island have to be sold first, and the tip says so.
-- Needs the client whose BUILD tip reads the region and the limit off the base (2026-09-25).
-- Looks up the builder by internalName, so it runs the same on local and PROD. The quest is read
-- from the DB on every activation: no restart needed.

SET @builder = (SELECT id FROM BASE_ITEM_TYPE WHERE internalName = 'Builder');

UPDATE QUEST SET tipString = 'BUILD', tipActorItemType_id = @builder WHERE id IN (395, 396);

SELECT id, tipString, tipActorItemType_id FROM QUEST WHERE id IN (395, 396);
