-- Puts the coast edge of quest 392's region ("Unload the Builder", PlaceConfig 2063 on local and PROD)
-- onto the real shore.
--
-- The region was drawn coarsely: its coast edge ran up to 69 m out into the water, 18.3 % of it was
-- water, and the ship unloading at the Phase 2 coast lay inside it. Since the unload marker shows the
-- region cut to the ship's reach (commit 03d16ff1a), that made the marker the whole reach circle, water
-- included. Moved from the planet 117 heightmap (water = average node height < 0): 2.9 % water left,
-- no self-intersection. All 14 unload spots logged on PROD inside the old region since 2026-09-20
-- ([Container] unloaded) are inside the new one; the inland corners are unchanged.
--
-- Looks up the PlaceConfig through quest 392 and its internalName, so a different id on PROD is
-- harmless and a missing quest changes nothing. The quest is read from the DB on activation: players
-- who have 392 active already keep the old region until their next activation.
--
-- Rollback, the old corners in order: 149/654 241/582 304/569 357/539 411/506 427/465 465/439 516/399 546/350 576/313 725/154 866/330 703/674 246/919

SET @pc = (SELECT qc.placeConfig_id FROM QUEST q
           JOIN QUEST_CONDITION c ON c.id = q.conditionConfigEntity_id
           JOIN QUEST_COMPARISON qc ON qc.id = c.comparisonConfig_id
           WHERE q.id = 392 AND q.internalName = 'Unload the Builder');

SELECT @pc AS placeConfig, COUNT(*) AS cornersBefore FROM PLACE_CONFIG_POSITION_POLYGON WHERE OWNER_ID = @pc;

START TRANSACTION;
DELETE FROM PLACE_CONFIG_POSITION_POLYGON WHERE OWNER_ID = @pc AND @pc IS NOT NULL;
INSERT INTO PLACE_CONFIG_POSITION_POLYGON (OWNER_ID, x, y, orderColumn)
SELECT @pc, v.x, v.y, v.o FROM (
    SELECT 183.5 AS x, 698.1 AS y, 0 AS o
    UNION ALL SELECT 178.1 AS x, 678.2 AS y, 1 AS o
    UNION ALL SELECT 180 AS x, 674.2 AS y, 2 AS o
    UNION ALL SELECT 197.1 AS x, 657 AS y, 3 AS o
    UNION ALL SELECT 207 AS x, 663.2 AS y, 4 AS o
    UNION ALL SELECT 219.8 AS x, 634.1 AS y, 5 AS o
    UNION ALL SELECT 227.3 AS x, 630.8 AS y, 6 AS o
    UNION ALL SELECT 244.7 AS x, 646.4 AS y, 7 AS o
    UNION ALL SELECT 248.4 AS x, 644.8 AS y, 8 AS o
    UNION ALL SELECT 249.1 AS x, 639.1 AS y, 9 AS o
    UNION ALL SELECT 254.3 AS x, 635 AS y, 10 AS o
    UNION ALL SELECT 255.6 AS x, 633.1 AS y, 11 AS o
    UNION ALL SELECT 271.1 AS x, 628.9 AS y, 12 AS o
    UNION ALL SELECT 276.1 AS x, 613.6 AS y, 13 AS o
    UNION ALL SELECT 291.8 AS x, 610.3 AS y, 14 AS o
    UNION ALL SELECT 300.4 AS x, 612.6 AS y, 15 AS o
    UNION ALL SELECT 310.3 AS x, 620.8 AS y, 16 AS o
    UNION ALL SELECT 333.6 AS x, 621.2 AS y, 17 AS o
    UNION ALL SELECT 341 AS x, 618.1 AS y, 18 AS o
    UNION ALL SELECT 351.5 AS x, 604.2 AS y, 19 AS o
    UNION ALL SELECT 358.4 AS x, 600.2 AS y, 20 AS o
    UNION ALL SELECT 382.8 AS x, 594.5 AS y, 21 AS o
    UNION ALL SELECT 388.3 AS x, 590.2 AS y, 22 AS o
    UNION ALL SELECT 390.1 AS x, 585.6 AS y, 23 AS o
    UNION ALL SELECT 402.5 AS x, 582.7 AS y, 24 AS o
    UNION ALL SELECT 414.3 AS x, 563.7 AS y, 25 AS o
    UNION ALL SELECT 429.8 AS x, 550.8 AS y, 26 AS o
    UNION ALL SELECT 434.3 AS x, 550.4 AS y, 27 AS o
    UNION ALL SELECT 459.4 AS x, 524.9 AS y, 28 AS o
    UNION ALL SELECT 470.1 AS x, 494.7 AS y, 29 AS o
    UNION ALL SELECT 474.1 AS x, 489.9 AS y, 30 AS o
    UNION ALL SELECT 478.3 AS x, 483.2 AS y, 31 AS o
    UNION ALL SELECT 482.1 AS x, 481.8 AS y, 32 AS o
    UNION ALL SELECT 487 AS x, 474.8 AS y, 33 AS o
    UNION ALL SELECT 499.1 AS x, 469.5 AS y, 34 AS o
    UNION ALL SELECT 501.6 AS x, 466.2 AS y, 35 AS o
    UNION ALL SELECT 517.3 AS x, 460.3 AS y, 36 AS o
    UNION ALL SELECT 526.2 AS x, 439.3 AS y, 37 AS o
    UNION ALL SELECT 536.3 AS x, 432.7 AS y, 38 AS o
    UNION ALL SELECT 542.4 AS x, 415.2 AS y, 39 AS o
    UNION ALL SELECT 545.4 AS x, 412.3 AS y, 40 AS o
    UNION ALL SELECT 551.7 AS x, 411.5 AS y, 41 AS o
    UNION ALL SELECT 561.2 AS x, 384.5 AS y, 42 AS o
    UNION ALL SELECT 569.2 AS x, 375.3 AS y, 43 AS o
    UNION ALL SELECT 588.5 AS x, 369 AS y, 44 AS o
    UNION ALL SELECT 591 AS x, 365.9 AS y, 45 AS o
    UNION ALL SELECT 592.2 AS x, 356.5 AS y, 46 AS o
    UNION ALL SELECT 610 AS x, 350.4 AS y, 47 AS o
    UNION ALL SELECT 614.3 AS x, 337.9 AS y, 48 AS o
    UNION ALL SELECT 625.6 AS x, 337.6 AS y, 49 AS o
    UNION ALL SELECT 632.5 AS x, 333.1 AS y, 50 AS o
    UNION ALL SELECT 644.8 AS x, 317.1 AS y, 51 AS o
    UNION ALL SELECT 653.1 AS x, 314 AS y, 52 AS o
    UNION ALL SELECT 656.4 AS x, 306.1 AS y, 53 AS o
    UNION ALL SELECT 673.9 AS x, 300.6 AS y, 54 AS o
    UNION ALL SELECT 683.6 AS x, 293.2 AS y, 55 AS o
    UNION ALL SELECT 686.9 AS x, 285.3 AS y, 56 AS o
    UNION ALL SELECT 700.2 AS x, 281.4 AS y, 57 AS o
    UNION ALL SELECT 705.5 AS x, 269.9 AS y, 58 AS o
    UNION ALL SELECT 705.7 AS x, 253.6 AS y, 59 AS o
    UNION ALL SELECT 707.7 AS x, 250 AS y, 60 AS o
    UNION ALL SELECT 718.1 AS x, 243.3 AS y, 61 AS o
    UNION ALL SELECT 723.3 AS x, 231.8 AS y, 62 AS o
    UNION ALL SELECT 729.5 AS x, 226.7 AS y, 63 AS o
    UNION ALL SELECT 732.1 AS x, 218.1 AS y, 64 AS o
    UNION ALL SELECT 753.6 AS x, 205.4 AS y, 65 AS o
    UNION ALL SELECT 756.2 AS x, 196.8 AS y, 66 AS o
    UNION ALL SELECT 758.6 AS x, 195.9 AS y, 67 AS o
    UNION ALL SELECT 866 AS x, 330 AS y, 68 AS o
    UNION ALL SELECT 703 AS x, 674 AS y, 69 AS o
    UNION ALL SELECT 246 AS x, 919 AS y, 70 AS o
) v WHERE @pc IS NOT NULL;
COMMIT;

SELECT @pc AS placeConfig, COUNT(*) AS cornersAfter FROM PLACE_CONFIG_POSITION_POLYGON WHERE OWNER_ID = @pc;
