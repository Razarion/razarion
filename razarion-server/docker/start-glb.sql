-- Start-GLB (Builder, Factory, Harvester, Resource 1) als eigener GLTF-Eintrag. Idempotent.
-- Vorher: start.glb nach /tmp/start.glb im MariaDB-Container kopieren.
SET @g := (SELECT id FROM GLTF WHERE internalName = 'Start models');
INSERT INTO GLTF (internalName, glb, glbDigest) SELECT 'Start models', LOAD_FILE('/tmp/start.glb'), NULL FROM DUAL WHERE @g IS NULL;
SET @g := (SELECT id FROM GLTF WHERE internalName = 'Start models');
UPDATE GLTF SET glb = LOAD_FILE('/tmp/start.glb'), glbDigest = NULL WHERE id = @g AND LOAD_FILE('/tmp/start.glb') IS NOT NULL;
INSERT INTO GLTF_BABYLON_MATERIAL (gltfMaterialName, babylon_material_id, gltfEntity)
  SELECT m.gltfMaterialName, m.babylon_material_id, @g FROM GLTF_BABYLON_MATERIAL m
  WHERE m.gltfEntity = 1 AND m.gltfMaterialName NOT IN ('Cubes', 'Fence_point_01') AND NOT EXISTS (SELECT 1 FROM GLTF_BABYLON_MATERIAL x WHERE x.gltfEntity = @g AND x.gltfMaterialName = m.gltfMaterialName);
-- Nur Materialien, die in start.glb vorkommen (Cubes, Fence_point_01 nicht).
DELETE FROM GLTF_BABYLON_MATERIAL WHERE gltfEntity = @g AND gltfMaterialName IN ('Cubes', 'Fence_point_01');
UPDATE MODEL_3D SET gltfEntity_id = @g WHERE internalName IN ('Builder', 'Factory', 'Harvester', 'Resource 1') AND gltfName = internalName;
SELECT id, internalName, LENGTH(glb) AS bytes FROM GLTF;
SELECT id, internalName, gltfEntity_id FROM MODEL_3D WHERE gltfEntity_id = @g;
SELECT gltfEntity, COUNT(*) FROM GLTF_BABYLON_MATERIAL GROUP BY gltfEntity;
