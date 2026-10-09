-- Terrain objects outside planet 117: plants from place-asphalt-plants.mjs along the upper edge of
-- the top asphalt row (y = -1 .. -5). TerrainShapeManagerSetup drops their tile and logs
-- "Can not handle terrain objects in tile: x: 0/1 y: -1" on every start. Nothing of them was ever
-- shown. 35 rows locally on 2026-10-09. Warm restart afterwards.

-- Look first
SELECT id, x, y, internalName FROM TERRAIN_OBJECT_POSITION
WHERE planet = 117 AND (x < 0 OR y < 0) ORDER BY id;

DELETE FROM TERRAIN_OBJECT_POSITION
WHERE planet = 117 AND (x < 0 OR y < 0) AND internalName LIKE 'asphalt-edge@%';

-- Rollback: none needed, the rows were never rendered. The generator can recreate them.
