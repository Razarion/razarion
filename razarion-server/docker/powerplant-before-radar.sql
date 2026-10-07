-- Level 3 asks for the Powerplant first and the Radar second (2026-10-04), the other way round
-- before.
--
-- Why: a Radar without power stands at NO_POWER, and since the map is there from level 1
-- (2026-09-23) nothing visible changes when it is finished - the quest is passed and the game
-- looks the same. With the Powerplant first, the Radar starts working the moment it is done and
-- the others appear on the map: the reward lands on the quest pass, where most players who leave
-- the beginners' island leave (PROD, 27.09.-04.10.2026).
--
-- Only the order of the two entries changes. Quests have no texts of their own (titles come from
-- the condition), and the next quest is the first one of the level not yet passed, so a player
-- who is on 361 (Radar) right now gets 362 (Powerplant) after it as before. Takes effect for the
-- next quest activation, no restart needed for that - a warm restart does no harm.

UPDATE SERVER_LEVEL_QUEST_ENTRY SET orderColumn = 1 WHERE quest_id = 361;
UPDATE SERVER_LEVEL_QUEST_ENTRY SET orderColumn = 0 WHERE quest_id = 362;

SELECT e.serverLevelQuest, e.orderColumn, e.quest_id, q.tipString
FROM SERVER_LEVEL_QUEST_ENTRY e JOIN QUEST q ON q.id = e.quest_id
WHERE e.quest_id IN (361, 362) ORDER BY e.orderColumn;

-- Rollback:
-- UPDATE SERVER_LEVEL_QUEST_ENTRY SET orderColumn = 0 WHERE quest_id = 361;
-- UPDATE SERVER_LEVEL_QUEST_ENTRY SET orderColumn = 1 WHERE quest_id = 362;
