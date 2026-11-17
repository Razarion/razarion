-- The harvest quests ask for less (2026-11-17). A harvest quest is there to show how mining
-- works, not to make the player wait: the harvester takes 1 razarion per second, and the quest
-- runs while the player only watches.
--
-- Why: of 219 new players (first base 15.11. 20:56 - 17.11. 09:10, PROD) 23 left while the
-- harvester was working on quest 363 "Harvest 5" (median 7 s into it), 8 more on 366 "Harvest 10"
-- (median 14 s). On the beginners' island nobody leaves stuck; they leave in the seconds in which
-- nothing is asked of them.
--
--   quest  level  before  after
--   363    L2       5       1   first harvest, island
--   366    L4      10       1   island
--   490    L9      20       1   first harvest on phase 2
--   402    L10     30       5
--   421    L13     30       5
--   437    L14     40      10
--   481            50      15
--
-- Quests have no texts of their own (title and progress come from the condition and its count).
-- The rewards stay (363 and 366 still pay 50 razarion). The server reads a quest from the DB when
-- it activates it, so no restart is needed; a quest that is already active keeps the old count
-- until the next restart. On PROD since 2026-11-17 16:05 UTC (402/421/437/481 at these values
-- since 16:10 UTC).

UPDATE QUEST_COMPARISON SET count = 1  WHERE id = 363;  -- quest 363
UPDATE QUEST_COMPARISON SET count = 1  WHERE id = 366;  -- quest 366
UPDATE QUEST_COMPARISON SET count = 1  WHERE id = 495;  -- quest 490
UPDATE QUEST_COMPARISON SET count = 5  WHERE id = 407;  -- quest 402
UPDATE QUEST_COMPARISON SET count = 5  WHERE id = 426;  -- quest 421
UPDATE QUEST_COMPARISON SET count = 10 WHERE id = 442;  -- quest 437
UPDATE QUEST_COMPARISON SET count = 15 WHERE id = 486;  -- quest 481

SELECT q.id quest, qc.id comparison, qc.count
FROM QUEST q JOIN QUEST_CONDITION c ON c.id = q.conditionConfigEntity_id
JOIN QUEST_COMPARISON qc ON qc.id = c.comparisonConfig_id
WHERE c.conditionTrigger = 'HARVEST' ORDER BY q.id;

-- Rollback:
-- UPDATE QUEST_COMPARISON SET count = 5  WHERE id = 363;
-- UPDATE QUEST_COMPARISON SET count = 10 WHERE id = 366;
-- UPDATE QUEST_COMPARISON SET count = 20 WHERE id = 495;
-- UPDATE QUEST_COMPARISON SET count = 30 WHERE id = 407;
-- UPDATE QUEST_COMPARISON SET count = 30 WHERE id = 426;
-- UPDATE QUEST_COMPARISON SET count = 40 WHERE id = 442;
-- UPDATE QUEST_COMPARISON SET count = 50 WHERE id = 486;
