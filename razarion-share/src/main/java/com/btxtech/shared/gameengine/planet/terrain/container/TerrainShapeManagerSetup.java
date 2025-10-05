package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.planet.terrain.BabylonDecal;
import com.btxtech.shared.gameengine.planet.terrain.BotGround;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeBabylonDecal;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeBotGround;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeObjectList;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeObjectPosition;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeVertex;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.btxtech.shared.gameengine.datatypes.workerdto.NativeUtil.toNativeDecimalPositions;
import static com.btxtech.shared.gameengine.planet.terrain.TerrainUtil.BOT_BLOCK_LENGTH;
import static com.btxtech.shared.gameengine.planet.terrain.TerrainUtil.terrainPositionToTileIndex;

public class TerrainShapeManagerSetup {
    private final Logger logger = Logger.getLogger(TerrainShapeManagerSetup.class.getName());
    private final TerrainShapeManager terrainShape;
    private final TerrainTypeService terrainTypeService;
    private final AlarmService alarmService;

    public TerrainShapeManagerSetup(TerrainShapeManager terrainShape, TerrainTypeService terrainTypeService, AlarmService alarmService) {
        this.terrainShape = terrainShape;
        this.terrainTypeService = terrainTypeService;
        this.alarmService = alarmService;
    }

    public void processTerrainObject(List<TerrainObjectPosition> terrainObjectPositions) {
        if (terrainObjectPositions == null) {
            return;
        }
        long time = System.currentTimeMillis();
        Map<Index, MapList<Integer, TerrainObjectPosition>> renderTerrainObjects = new HashMap<>();
        for (TerrainObjectPosition objectPosition : terrainObjectPositions) {
            try {
                // Render engine
                MapList<Integer, TerrainObjectPosition> tileObjects = renderTerrainObjects.computeIfAbsent(TerrainUtil.toTile(objectPosition.getPosition()), k -> new MapList<>());
                tileObjects.put(objectPosition.getTerrainObjectConfigId(), objectPosition);
                // Game engine
                TerrainObjectConfig terrainObjectConfig = terrainTypeService.getTerrainObjectConfig(objectPosition.getTerrainObjectConfigId());
                if (terrainObjectConfig.getRadius() <= 0.0) {
                    continue;
                }
                // TODO
            } catch (Throwable t) {
                alarmService.riseAlarm(Alarm.Type.TERRAIN_SHAPE_FAILED_TERRAIN_OBJECT_POSITION, objectPosition.getId());
                logger.log(Level.WARNING, "Can not handle terrain object with id: " + objectPosition.getId(), t);
            }
        }
        fillInRenderTerrainObject(renderTerrainObjects);
        logger.info("Generate Terrain Objects: " + (System.currentTimeMillis() - time));
    }

    private double calculateScale(Vertex scale) {
        if (scale == null) {
            return 1;
        }
        return Math.max(scale.getX(), scale.getY());
    }

    private void fillInRenderTerrainObject(Map<Index, MapList<Integer, TerrainObjectPosition>> renderTerrainObjects) {
        renderTerrainObjects.forEach((tileIndex, terrainObjectGroup) -> {
            NativeTerrainShapeObjectList[] nativeTerrainShapeObjectLists = new NativeTerrainShapeObjectList[terrainObjectGroup.getMap().size()];
            int terrainObjectIdIndex = 0;
            for (Map.Entry<Integer, List<TerrainObjectPosition>> entry : terrainObjectGroup.getMap().entrySet()) {
                NativeTerrainShapeObjectList nativeTerrainShapeObjectList = new NativeTerrainShapeObjectList();
                nativeTerrainShapeObjectList.terrainObjectConfigId = entry.getKey();
                nativeTerrainShapeObjectList.terrainShapeObjectPositions = new NativeTerrainShapeObjectPosition[entry.getValue().size()];
                for (int positionIndex = 0; positionIndex < entry.getValue().size(); positionIndex++) {
                    TerrainObjectPosition terrainObjectPosition = entry.getValue().get(positionIndex);
                    NativeTerrainShapeObjectPosition nativeTerrainShapeObjectPosition = new NativeTerrainShapeObjectPosition();
                    nativeTerrainShapeObjectPosition.terrainObjectId = terrainObjectPosition.getId();
                    nativeTerrainShapeObjectPosition.x = terrainObjectPosition.getPosition().getX();
                    nativeTerrainShapeObjectPosition.y = terrainObjectPosition.getPosition().getY();
                    if (terrainObjectPosition.getScale() != null) {
                        nativeTerrainShapeObjectPosition.scale = new NativeVertex();
                        nativeTerrainShapeObjectPosition.scale.x = terrainObjectPosition.getScale().getX();
                        nativeTerrainShapeObjectPosition.scale.y = terrainObjectPosition.getScale().getY();
                        nativeTerrainShapeObjectPosition.scale.z = terrainObjectPosition.getScale().getZ();
                    }
                    if (terrainObjectPosition.getRotation() != null) {
                        nativeTerrainShapeObjectPosition.rotation = new NativeVertex();
                        nativeTerrainShapeObjectPosition.rotation.x = terrainObjectPosition.getRotation().getX();
                        nativeTerrainShapeObjectPosition.rotation.y = terrainObjectPosition.getRotation().getY();
                        nativeTerrainShapeObjectPosition.rotation.z = terrainObjectPosition.getRotation().getZ();
                    }
                    if (terrainObjectPosition.getOffset() != null) {
                        nativeTerrainShapeObjectPosition.offset = new NativeVertex();
                        nativeTerrainShapeObjectPosition.offset.x = terrainObjectPosition.getOffset().getX();
                        nativeTerrainShapeObjectPosition.offset.y = terrainObjectPosition.getOffset().getY();
                        nativeTerrainShapeObjectPosition.offset.z = terrainObjectPosition.getOffset().getZ();
                    }
                    nativeTerrainShapeObjectList.terrainShapeObjectPositions[positionIndex] = nativeTerrainShapeObjectPosition;
                }
                nativeTerrainShapeObjectLists[terrainObjectIdIndex++] = nativeTerrainShapeObjectList;
            }
            try {
                terrainShape.getOrCreateTerrainShapeTile(tileIndex).setNativeTerrainShapeObjectLists(nativeTerrainShapeObjectLists);
            } catch (Throwable t) {
                logger.log(Level.WARNING, "Can not handle terrain object with id: " + nativeTerrainShapeObjectLists[0].terrainObjectConfigId, t);
            }
        });
    }

    public void processBotDecals(List<BabylonDecal> babylonDecals) {
        MapList<Index, NativeBabylonDecal> tileDecals = new MapList<>();
        babylonDecals.forEach(babylonDecal -> {
            Set<Index> tileIndices = new HashSet<>();
            tileIndices.add(terrainPositionToTileIndex(new DecimalPosition(babylonDecal.xPos, babylonDecal.yPos)));
            tileIndices.add(terrainPositionToTileIndex(new DecimalPosition(babylonDecal.xPos + babylonDecal.xSize, babylonDecal.yPos)));
            tileIndices.add(terrainPositionToTileIndex(new DecimalPosition(babylonDecal.xPos + babylonDecal.xSize, babylonDecal.yPos + babylonDecal.ySize)));
            tileIndices.add(terrainPositionToTileIndex(new DecimalPosition(babylonDecal.xPos, babylonDecal.yPos + babylonDecal.ySize)));

            NativeBabylonDecal nativeBabylonDecal = new NativeBabylonDecal();
            nativeBabylonDecal.babylonMaterialId = babylonDecal.babylonMaterialId;
            nativeBabylonDecal.xPos = babylonDecal.xPos;
            nativeBabylonDecal.yPos = babylonDecal.yPos;
            nativeBabylonDecal.xSize = babylonDecal.xSize;
            nativeBabylonDecal.ySize = babylonDecal.ySize;
            tileIndices.forEach(tileIndex -> tileDecals.put(tileIndex, nativeBabylonDecal));
        });

        tileDecals.getMap().forEach((tileIndex, nativeBabylonDecals) -> {
            try {
                terrainShape.getOrCreateTerrainShapeTile(tileIndex).setNativeBabylonDecals(nativeBabylonDecals.toArray(new NativeBabylonDecal[0]));
            } catch (Throwable t) {
                logger.log(Level.WARNING, "Can not handle BotDecals in tile index: " + tileIndex, t);
            }
        });
    }

    public void processBotGrounds(List<BotGround> botGrounds) {
        MapList<Index, NativeBotGround> tileBotGrounds = new MapList<>();
        botGrounds.forEach(botGround -> {
            Set<Index> tileIndices = new HashSet<>();

            Arrays.stream(botGround.positions).forEach(position -> {
                tileIndices.add(terrainPositionToTileIndex(position));
                tileIndices.add(terrainPositionToTileIndex(position.add(0, BOT_BLOCK_LENGTH)));
                tileIndices.add(terrainPositionToTileIndex(position.add(BOT_BLOCK_LENGTH, 0)));
                tileIndices.add(terrainPositionToTileIndex(position.add(BOT_BLOCK_LENGTH, BOT_BLOCK_LENGTH)));
            });

            NativeBotGround nativeBotGround = new NativeBotGround();
            nativeBotGround.model3DId = botGround.model3DId;
            nativeBotGround.height = botGround.height;
            nativeBotGround.positions = toNativeDecimalPositions(botGround.positions);
            tileIndices.forEach(tileIndex -> tileBotGrounds.put(tileIndex, nativeBotGround));
        });

        tileBotGrounds.getMap().forEach((tileIndex, nativeBotGround) -> {
            try {
                terrainShape.getOrCreateTerrainShapeTile(tileIndex).setNativeBotGrounds(nativeBotGround.toArray(new NativeBotGround[0]));
            } catch (Throwable t) {
                logger.log(Level.WARNING, "Can not handle BotGround in tile index: " + tileIndex, t);
            }
        });
    }
}
