-- Every sell quest counts what the player has: with nothing of the type left to sell, it passes at once.
--
-- BaseItemTypeComparison already does this for SELL (init(..., isSellMode): "can only sell what you
-- have"), but only when the comparison has includeExisting set. 401 (sell the dockyard) had it; 393
-- (sell the factory) and "Sell the Harvester" (PROD 487, local 488) did not. A player who had sold or
-- lost the factory on the noob island - here: to free a builder walled in between radar, powerplant
-- and factory at quest 386 (phone test, 2026-10-14) - got 393 with nothing to sell, a silent tip
-- (ACTOR_NOT_FOUND) and no way on.
--
-- Looks the quests up by their trigger, so it runs the same on local and PROD. Safe to run again.
-- Quests are read from the DB on every activation; no restart needed. A quest already active keeps
-- its old progress until it is activated again.

UPDATE QUEST_COMPARISON cmp
  JOIN QUEST_CONDITION c ON c.comparisonConfig_id = cmp.id
SET cmp.includeExisting = 1
WHERE c.conditionTrigger = 'SELL';

SELECT q.id, q.internalName, cmp.includeExisting
FROM QUEST q
  JOIN QUEST_CONDITION c ON c.id = q.conditionConfigEntity_id
  JOIN QUEST_COMPARISON cmp ON cmp.id = c.comparisonConfig_id
WHERE c.conditionTrigger = 'SELL';
