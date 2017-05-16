
# INSERT INTO razarion.LEVEL (id, number, xp2LevelUp) VALUES (1, 1, 2);
# INSERT INTO razarion.LEVEL_LIMITATION (LevelEntity_id, itemTypeLimitation, baseItemTypeEntityId) VALUES (1, 1, 180807);
# INSERT INTO razarion.LEVEL (id, number, xp2LevelUp) VALUES (2, 2, 13);
# INSERT INTO razarion.LEVEL_LIMITATION (LevelEntity_id, itemTypeLimitation, baseItemTypeEntityId) VALUES (2, 1, 180807);
# INSERT INTO razarion.LEVEL_LIMITATION (LevelEntity_id, itemTypeLimitation, baseItemTypeEntityId) VALUES (2, 3, 180832);
# INSERT INTO razarion.LEVEL (id, number, xp2LevelUp) VALUES (3, 3, 30);
# INSERT INTO razarion.LEVEL_LIMITATION (LevelEntity_id, itemTypeLimitation, baseItemTypeEntityId) VALUES (3, 1, 180807);
# INSERT INTO razarion.LEVEL_LIMITATION (LevelEntity_id, itemTypeLimitation, baseItemTypeEntityId) VALUES (3, 3, 180832);
# INSERT INTO razarion.LEVEL_LIMITATION (LevelEntity_id, itemTypeLimitation, baseItemTypeEntityId) VALUES (3, 1, 180830);
# INSERT INTO razarion.LEVEL_LIMITATION (LevelEntity_id, itemTypeLimitation, baseItemTypeEntityId) VALUES (3, 1, 272490);
# INSERT INTO razarion.LEVEL (id, number, xp2LevelUp) VALUES (4, 4, 50);
# INSERT INTO razarion.LEVEL_LIMITATION (LevelEntity_id, itemTypeLimitation, baseItemTypeEntityId) VALUES (4, 1, 180807);
# INSERT INTO razarion.LEVEL_LIMITATION (LevelEntity_id, itemTypeLimitation, baseItemTypeEntityId) VALUES (4, 5, 180832);
# INSERT INTO razarion.LEVEL_LIMITATION (LevelEntity_id, itemTypeLimitation, baseItemTypeEntityId) VALUES (4, 1, 180830);
# INSERT INTO razarion.LEVEL_LIMITATION (LevelEntity_id, itemTypeLimitation, baseItemTypeEntityId) VALUES (4, 1, 272490);
# INSERT INTO razarion.LEVEL (id, number, xp2LevelUp) VALUES (5, 5, 75);
# INSERT INTO razarion.LEVEL_LIMITATION (LevelEntity_id, itemTypeLimitation, baseItemTypeEntityId) VALUES (5, 1, 180807);
# INSERT INTO razarion.LEVEL_LIMITATION (LevelEntity_id, itemTypeLimitation, baseItemTypeEntityId) VALUES (5, 5, 180832);
# INSERT INTO razarion.LEVEL_LIMITATION (LevelEntity_id, itemTypeLimitation, baseItemTypeEntityId) VALUES (5, 1, 180830);
# INSERT INTO razarion.LEVEL_LIMITATION (LevelEntity_id, itemTypeLimitation, baseItemTypeEntityId) VALUES (5, 1, 272490);

# UPDATE PLANET
# SET groundMeshDimensionEndX = 64, groundMeshDimensionEndY = 64, groundMeshDimensionStartX = 0, groundMeshDimensionStartY = 0, houseSpace = 10, playGroundEndX = 360, playGroundEndY = 360,
#   playGroundStartX          = 50, playGroundStartY = 40, shadowAlpha = 0.2, shadowRotationX = 0.471239, shadowRotationY = 0, shape3DLightRotateX = 1.047198, shape3DLightRotateZ = 4.537856, startRazarion = 550,
#   startBaseItemType_id      = 180807
# WHERE id = 1;
# UPDATE PLANET
# SET groundMeshDimensionEndX = 2500, groundMeshDimensionEndY = 2500, groundMeshDimensionStartX = 0, groundMeshDimensionStartY = 0, houseSpace = 10, playGroundEndX = 19900, playGroundEndY = 19920,
#   playGroundStartX          = 50, playGroundStartY = 50, shadowAlpha = 0.2, shadowRotationX = 0.471239, shadowRotationY = 0, shape3DLightRotateX = 1.047198, shape3DLightRotateZ = 4.537856,
#   startRazarion             = 550, startBaseItemType_id = 180807
# WHERE id = 2;

# INSERT INTO SERVER_GAME_ENGINE_CONFIG (id, planetEntity_id) VALUES (1, 2);

# INSERT INTO WATER_CONFIG (	`id`,	`bmDepth`,	`bmScale`,	`groundLevel`,	`ambientA`,	`ambientB`,	`ambientG`,	`ambientR`,	`diffuseA`,	`diffuseB`,	`diffuseG`,	`diffuseR`,
# 	`specularHardness`,	`specularIntensity`,	`xRotation`,	`yRotation`,	`waterLevel`,	`waterTransparency`,	`bmId_id`)
# 	 VALUES ('5', '2', '0.02', '-2', '1', '0.38', '0.38', '0.38', '1', '1', '1', '1', '30',  '0.75', '-0.575959', '0', '-0.7', '0.5', '272480');

# ALTER TABLE TRACKER_PAGE DROP id;
# ALTER TABLE TRACKER_PAGE ADD id INT NOT NULL AUTO_INCREMENT FIRST, ADD PRIMARY KEY (id), AUTO_INCREMENT=1

ALTER TABLE TRACKER_GAME_UI_CONTROL DROP id;
ALTER TABLE TRACKER_GAME_UI_CONTROL ADD id INT NOT NULL AUTO_INCREMENT FIRST, ADD PRIMARY KEY (id), AUTO_INCREMENT=1

