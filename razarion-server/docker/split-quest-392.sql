-- Splits quest 392 ("builder into the Phase 2 region") into load, sail, unload.
--
-- On PROD 392 passed 90 % in August and 0 % from 18.09.2026 on, and in the day after the unload fix
-- not one load was ordered: nobody is told to put the builder into the transporter. Each of the
-- three quests now asks for one gesture and carries a tip for it (LOAD / SAIL / UNLOAD).
--
-- Needs the server build with the SYNC_ITEM_LOADED and LOADED_CONTAINER_POSITION triggers. Looks up
-- everything by quest id and internalName, so it runs the same on local and PROD. Run once; a warm
-- restart of the planet afterwards.
--
-- The sailing target is a strip of water along the Phase 2 coast, about 230 units long and 10 wide,
-- traced against the heightmap with the engine's own terrain rules (TerrainAnalyzer: water where the
-- mean of a node's corners is below 0, land where its slope is below 0.5). At every point of it the
-- transporter (radius 2) fits in water, and land of the Phase 2 region a builder (radius 1.45) can
-- stand on is at most 18.5 units away - inside the unload range of 20. Long rather than wide: the
-- shelf drops off steeply, and a crowd of players needs the room along the coast. Traced on the
-- LOCAL heightmap - PROD is modelled by hand and has to be traced again against its own.

SET @quest_unload = 392;
SET @builder = (SELECT id FROM BASE_ITEM_TYPE WHERE internalName = 'Builder');
SET @transporter = (SELECT id FROM BASE_ITEM_TYPE WHERE internalName = 'Transporter');
SET @level_group = (SELECT serverLevelQuest FROM SERVER_LEVEL_QUEST_ENTRY WHERE quest_id = @quest_unload);
SET @unload_order = (SELECT orderColumn FROM SERVER_LEVEL_QUEST_ENTRY WHERE quest_id = @quest_unload);

-- The conditionTrigger column may be older than the longest new value (see
-- feedback_hibernate_enum_column_length); ddl-auto=update never widens it.
ALTER TABLE QUEST_CONDITION MODIFY COLUMN conditionTrigger VARCHAR(255);

-- Q1: load the builder
INSERT INTO QUEST_COMPARISON (count, time, placeConfig_id, includeExisting, startRegionId)
VALUES (NULL, NULL, NULL, 0, NULL);
SET @cmp_load = LAST_INSERT_ID();
INSERT INTO QUEST_COMPARISON_BASE_ITEM (ComparisonConfigEntity_id, typeCount, baseItemTypeEntityId)
VALUES (@cmp_load, 1, @builder);
INSERT INTO QUEST_CONDITION (conditionTrigger, comparisonConfig_id) VALUES ('SYNC_ITEM_LOADED', @cmp_load);
SET @cond_load = LAST_INSERT_ID();
INSERT INTO QUEST (crystal, internalName, razarion, xp, conditionConfigEntity_id, tipString, tipActorItemType_id)
VALUES (0, 'Load the Builder', 0, 10, @cond_load, 'LOAD', @builder);
SET @quest_load = LAST_INSERT_ID();

-- Q2: sail the loaded transporter to the coast
INSERT INTO PLACE_CONFIG (x, y, radius, internalName) VALUES (NULL, NULL, NULL, 'Quest: water off the Phase 2 coast');
SET @place_sail = LAST_INSERT_ID();
INSERT INTO PLACE_CONFIG_POSITION_POLYGON (OWNER_ID, x, y, orderColumn) VALUES
  (@place_sail, 535.5, 400.5, 0),
  (@place_sail, 543.5, 407.5, 1),
  (@place_sail, 535.5, 415.5, 2),
  (@place_sail, 532.5, 428.5, 3),
  (@place_sail, 520.5, 439.5, 4),
  (@place_sail, 513.5, 456.5, 5),
  (@place_sail, 505.5, 458.5, 6),
  (@place_sail, 495.5, 466.5, 7),
  (@place_sail, 487.5, 468.5, 8),
  (@place_sail, 473.5, 482.5, 9),
  (@place_sail, 461.5, 501.5, 10),
  (@place_sail, 459.5, 515.5, 11),
  (@place_sail, 453.5, 526.5, 12),
  (@place_sail, 442.5, 538.5, 13),
  (@place_sail, 440.5, 544.5, 14),
  (@place_sail, 426.5, 546.5, 15),
  (@place_sail, 408.5, 563.5, 16),
  (@place_sail, 406.5, 568.5, 17),
  (@place_sail, 401.5, 573.5, 18),
  (@place_sail, 393.5, 566.5, 19),
  (@place_sail, 402.5, 554.5, 20),
  (@place_sail, 420.5, 538.5, 21),
  (@place_sail, 431.5, 536.5, 22),
  (@place_sail, 434.5, 533.5, 23),
  (@place_sail, 437.5, 527.5, 24),
  (@place_sail, 446.5, 518.5, 25),
  (@place_sail, 451.5, 508.5, 26),
  (@place_sail, 453.5, 495.5, 27),
  (@place_sail, 462.5, 479.5, 28),
  (@place_sail, 476.5, 463.5, 29),
  (@place_sail, 486.5, 459.5, 30),
  (@place_sail, 493.5, 453.5, 31),
  (@place_sail, 503.5, 449.5, 32),
  (@place_sail, 506.5, 446.5, 33),
  (@place_sail, 515.5, 428.5, 34),
  (@place_sail, 524.5, 420.5, 35),
  (@place_sail, 528.5, 407.5, 36);
INSERT INTO QUEST_COMPARISON (count, time, placeConfig_id, includeExisting, startRegionId)
VALUES (NULL, NULL, @place_sail, 0, NULL);
SET @cmp_sail = LAST_INSERT_ID();
INSERT INTO QUEST_COMPARISON_BASE_ITEM (ComparisonConfigEntity_id, typeCount, baseItemTypeEntityId)
VALUES (@cmp_sail, 1, @transporter);
INSERT INTO QUEST_CONDITION (conditionTrigger, comparisonConfig_id) VALUES ('LOADED_CONTAINER_POSITION', @cmp_sail);
SET @cond_sail = LAST_INSERT_ID();
INSERT INTO QUEST (crystal, internalName, razarion, xp, conditionConfigEntity_id, tipString, tipActorItemType_id)
VALUES (0, 'Sail to the coast', 0, 10, @cond_sail, 'SAIL', @builder);
SET @quest_sail = LAST_INSERT_ID();

-- Q3: the old 392 - builder in the Phase 2 region - now with the unload tip. Its 40 XP are split
-- 10/10/20 over the three.
UPDATE QUEST SET tipString = 'UNLOAD', tipActorItemType_id = @builder, xp = 20, internalName = 'Unload the Builder'
WHERE id = @quest_unload;

-- Order: ... 389, load, sail, 392, ...
UPDATE SERVER_LEVEL_QUEST_ENTRY SET orderColumn = orderColumn + 2
WHERE serverLevelQuest = @level_group AND orderColumn >= @unload_order;
INSERT INTO SERVER_LEVEL_QUEST_ENTRY (orderColumn, quest_id, serverLevelQuest)
VALUES (@unload_order, @quest_load, @level_group), (@unload_order + 1, @quest_sail, @level_group);

SELECT @quest_load AS quest_load, @quest_sail AS quest_sail, @quest_unload AS quest_unload, @place_sail AS place_sail;
