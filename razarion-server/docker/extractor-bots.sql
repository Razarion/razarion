-- (Bot1) Extractor 1-12 on the noob island (2026-11-15), idempotent.
--
-- Vipers parked next to an extractor shot every rebuild at once; five kills and the bot raged.
-- The bot now rebuilds out of enemy weapon reach (BotItemContainer.getPosition), which needs room:
--   - extractor place radius 1 -> 18 (nearest-to-center spot out of reach; viper reach ~16.5 m)
--   - realm radius 14 -> 24 (covers the larger place; neighbours are >= 55 m apart, no overlap)
--   - no ground box any more: it was baked into the terrain at one spot. The plate now comes with
--     the building as a decal (BaseItemType.groundDecalRadius).
-- Needs a warm planet restart (terrain changes) and a new minimap afterwards.

ALTER TABLE BASE_ITEM_TYPE ADD COLUMN IF NOT EXISTS groundDecalRadius DOUBLE NULL;

UPDATE BASE_ITEM_TYPE SET groundDecalRadius = 3.5 WHERE internalName = '(Bot1) Extractor';
UPDATE BASE_ITEM_TYPE SET groundDecalRadius = 2.0 WHERE internalName = '(Bot1) Tesla';

UPDATE PLACE_CONFIG p
    JOIN BOT_CONFIG_BOT_ITEM i ON i.place_id = p.id
    JOIN BOT_CONFIG_ENRAGEMENT_STATE_CONFIG e ON e.id = i.botEnragementStateConfig
    JOIN BOT_CONFIG b ON b.id = e.botConfig
    JOIN BASE_ITEM_TYPE t ON t.id = i.baseItemTypeEntity_id
SET p.radius = 18
WHERE b.internalName LIKE '(Bot1) Extractor %' AND t.internalName = '(Bot1) Extractor';

UPDATE PLACE_CONFIG p
    JOIN BOT_CONFIG b ON b.realm_id = p.id
SET p.radius = 24
WHERE b.internalName LIKE '(Bot1) Extractor %';

-- The box positions stay (switching back on restores the old plates)
UPDATE BOT_CONFIG SET groundBoxEnabled = 0 WHERE internalName LIKE '(Bot1) Extractor %';
