-- Puts the SELL tip on the two sell quests of level 9: 393 sells the factory, 401 the dockyard.
--
-- On PROD 393 fell from 30/36 to 1/7 once the transport tips (485/486/392) brought players there
-- who had been shown every step before; none of the six who failed sold anything (2026-09-25).
-- Needs the client with the SELL tip. Looks up the actors by internalName, so it runs the same on
-- local and PROD. A warm restart of the planet afterwards.

SET @factory = (SELECT id FROM BASE_ITEM_TYPE WHERE internalName = 'Factory');
SET @dockyard = (SELECT id FROM BASE_ITEM_TYPE WHERE internalName = 'Dockyard');

UPDATE QUEST SET tipString = 'SELL', tipActorItemType_id = @factory WHERE id = 393;
UPDATE QUEST SET tipString = 'SELL', tipActorItemType_id = @dockyard WHERE id = 401;

SELECT id, internalName, tipString, tipActorItemType_id FROM QUEST WHERE id IN (393, 401);
