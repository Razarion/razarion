INSERT INTO GROUND_CONFIG (id, internalName) VALUE (1, 'Minimal');

INSERT INTO PLANET (id,
                    groundMeshDimensionStartX, groundMeshDimensionStartY, groundMeshDimensionEndX,
                    groundMeshDimensionEndY,
                    playGroundStartX, playGroundStartY, playGroundEndX, playGroundEndY,
                    startRazarion, houseSpace, shadowAlpha) VALUE (1, 0, 0, 3, 3, 20, 20, 20, 300, 300, 10, 0.8);

INSERT INTO PLANET (id,
                    groundMeshDimensionStartX, groundMeshDimensionStartY, groundMeshDimensionEndX,
                    groundMeshDimensionEndY,
                    playGroundStartX, playGroundStartY, playGroundEndX, playGroundEndY,
                    startRazarion, houseSpace, shadowAlpha) VALUE (2, 0, 0, 3, 3, 20, 20, 300, 300, 20, 10, 0.8);

INSERT INTO GAME_UI_CONTROL_CONFIG(id, detailedTracking, gameEngineMode, minimalLevel_id, planetEntity_id)
VALUES (1, false, 'MASTER', 1, 1);

INSERT INTO SERVER_GAME_ENGINE_CONFIG(id, planetEntity_id)
VALUES (1, 2);

INSERT INTO SCENE(id, orderColumn, gameUiControlConfigEntityId, removeLoadingCover, wait4LevelUpDialog)
VALUES (1, 0, 1, TRUE, TRUE)