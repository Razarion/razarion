-- One build time per kind of thing, and a short one (2026-10-04): every building 5 s except the
-- Tesla (7 s), Viper, Harvester and Hydra 4 s, Badger and Transporter 5 s, the Builder 6 s.
--
-- Before, Radar and Powerplant took 15 s, the other buildings 8 s, the Builder 40 s, the units
-- 5-8 s. Waiting for a building is not where players leave (PROD, 27.09.-04.10.2026: of 45 who
-- left quest 361 only 4 did while the radar site stood), but every second of watching a site grow
-- on the beginners' island is a second without anything to do. Builders and factories all build
-- with progress 1, so buildup is the build time in seconds; a factory adds 1 s intro and 2 s outro
-- on top, a builder 1 s each.
--
-- Only the player's types - the bots' "(Bot...)" types keep their times. Prices stay: a factory
-- withdraws the price in proportion to the progress, so a shorter build costs the same, faster.
-- Item types are cached by the server: warm-restart the planet afterwards.

UPDATE BASE_ITEM_TYPE SET buildup = 5 WHERE internalName IN ('Factory', 'Radar', 'Powerplant', 'Dockyard', 'House');
UPDATE BASE_ITEM_TYPE SET buildup = 7 WHERE internalName = 'Tesla';
UPDATE BASE_ITEM_TYPE SET buildup = 4 WHERE internalName IN ('Viper', 'Harvester', 'Hydra');
UPDATE BASE_ITEM_TYPE SET buildup = 5 WHERE internalName IN ('Badger', 'Transporter');
UPDATE BASE_ITEM_TYPE SET buildup = 6 WHERE internalName = 'Builder';

SELECT id, internalName, buildup FROM BASE_ITEM_TYPE WHERE internalName NOT LIKE '(Bot%' ORDER BY buildup, internalName;

-- Rollback (the values up to 2026-10-04):
-- UPDATE BASE_ITEM_TYPE SET buildup = 15 WHERE internalName IN ('Radar', 'Powerplant');
-- UPDATE BASE_ITEM_TYPE SET buildup = 8 WHERE internalName IN ('Factory', 'Dockyard', 'House', 'Badger');
-- UPDATE BASE_ITEM_TYPE SET buildup = 10 WHERE internalName = 'Tesla';
-- UPDATE BASE_ITEM_TYPE SET buildup = 7 WHERE internalName IN ('Harvester', 'Transporter');
-- UPDATE BASE_ITEM_TYPE SET buildup = 6 WHERE internalName = 'Hydra';
-- UPDATE BASE_ITEM_TYPE SET buildup = 5 WHERE internalName = 'Viper';
-- UPDATE BASE_ITEM_TYPE SET buildup = 40 WHERE internalName = 'Builder';
