-- Locks the quests of the beginners' island (levels 1-8, quest 358 up to 392, the crossing) against
-- switching: while one of them runs, the player gets no quest list and the server refuses a switch.
--
-- On PROD players left the guided quests through the quest list, which a tap on the phone's quest
-- line opened: 486 -> 392 six times, 0 passed; 75 switches by 30 players since 16.09. (2026-09-30).
-- The tip cannot lead a player who has jumped ahead in the chain.
--
-- Needs the server with QuestConfig.switchable. Hibernate adds the column on start; the ALTER only
-- makes the script runnable before that. Looks the quests up by level number, so it runs the same on
-- local and PROD. The flag is read at every quest activation - no warm restart needed; a player whose
-- locked quest is already active sees the change after a reload.
--
-- Rollback: UPDATE QUEST SET switchable = NULL;

ALTER TABLE QUEST ADD COLUMN IF NOT EXISTS switchable BIT(1) NULL;

UPDATE QUEST q
    JOIN SERVER_LEVEL_QUEST_ENTRY e ON e.quest_id = q.id
    JOIN SERVER_LEVEL_QUEST s ON e.serverLevelQuest = s.id
    JOIN LEVEL l ON s.minimalLevel_id = l.id
SET q.switchable = 0
WHERE l.number <= 8;

SELECT l.number AS level, q.id, q.internalName, q.switchable + 0 AS switchable
FROM QUEST q
         JOIN SERVER_LEVEL_QUEST_ENTRY e ON e.quest_id = q.id
         JOIN SERVER_LEVEL_QUEST s ON e.serverLevelQuest = s.id
         JOIN LEVEL l ON s.minimalLevel_id = l.id
WHERE l.number <= 9
ORDER BY l.number, e.orderColumn;
