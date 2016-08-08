package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.SurfaceType;
import com.btxtech.shared.gameengine.datatypes.TerrainType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;
import com.btxtech.shared.gameengine.planet.PlanetActivationEvent;
import com.btxtech.shared.gameengine.planet.terrain.ground.GroundMesh;
import com.btxtech.shared.gameengine.planet.terrain.ground.GroundModeler;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.shared.gameengine.planet.terrain.slope.SlopeWater;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 13.07.2016.
 */
@Singleton
public class TerrainService {
    public static final int MESH_NODE_EDGE_LENGTH = 64;
    public static final int MESH_NODES = 64;
    private Logger logger = Logger.getLogger(TerrainService.class.getName());
    @Inject
    private TerrainTypeService terrainTypeService;
    private Water water = new Water(-7, -20); // Init here due to the editor
    private GroundMesh groundMesh;
    private Map<Integer, Slope> slopeMap = new HashMap<>();
    private Collection<TerrainSlopePosition> terrainSlopePositions;

    public void onPlanetActivation(@Observes PlanetActivationEvent planetActivationEvent) {
        logger.severe("Start setup surface");
        long time = System.currentTimeMillis();
        setupGround(MESH_NODES, MESH_NODES);
        water.clearAllTriangles();
        slopeMap.clear();

        terrainSlopePositions = planetActivationEvent.getPlanetConfig().getTerrainSlopePositions();
        if (terrainSlopePositions != null) {
            for (TerrainSlopePosition terrainSlopePosition : terrainSlopePositions) {
                setupPlateau(terrainSlopePosition);
            }
        }

        logger.severe("Setup surface took: " + (System.currentTimeMillis() - time));
    }

    private void setupPlateau(TerrainSlopePosition terrainSlopePosition) {
        SlopeSkeletonConfig slopeSkeletonConfig = terrainTypeService.getSlopeSkeleton(terrainSlopePosition.getSlopeId());
        Slope slope;
        if (slopeSkeletonConfig.getType() == SlopeSkeletonConfig.Type.WATER) {
            slope = new SlopeWater(water, slopeSkeletonConfig, terrainSlopePosition.getPolygon());
        } else if (slopeSkeletonConfig.getType() == SlopeSkeletonConfig.Type.LAND) {
            slope = new Slope(slopeSkeletonConfig, terrainSlopePosition.getPolygon());
            slope.wrap(groundMesh);
        } else {
            throw new IllegalStateException("Unknown enum type: " + slopeSkeletonConfig.getType());
        }
        slope.wrap(groundMesh);
        slope.setupGroundConnection(groundMesh);
        slopeMap.put(slopeMap.size(), slope);
    }

    private void setupGround(int xCount, int yCount) {
        groundMesh = GroundModeler.generateGroundMesh(terrainTypeService.getGroundSkeletonConfig(), xCount, yCount);
        groundMesh.setupNorms();
    }

    public Slope getSlope(int id) {
        return slopeMap.get(id);
    }

    public Collection<Integer> getSlopeIds() {
        return slopeMap.keySet();
    }

    public Collection<Slope> getSlopes() {
        return slopeMap.values();
    }

    public Water getWater() {
        return water;
    }

    public GroundMesh getGroundMesh() {
        return groundMesh;
    }

    public Vertex getVertexAt(Index position) {
        logger.severe("TerrainService.getVertexAt(): Faked position");
        return new Vertex(position.getX(), position.getY(), 0);
    }

    // -------------------------------------------------
    // TODO TerrainSettings getTerrainSettings();

    // TODO void addTerrainListener(TerrainListener terrainListener);

    // TODO boolean isFree(Index middlePoint, int radius, Collection<SurfaceType> allowedSurfaces, SurfaceType adjoinSurface, TerrainType builderTerrainType, Integer maxAdjoinDistance);

    public boolean isFree(Index middlePoint, ItemType itemType, TerrainType builderTerrainType, Integer builderMaxAdjoinDistance) {
        throw new UnsupportedOperationException();
    }

    public boolean hasSurfaceTypeInRegion(SurfaceType surfaceType, Rectangle absRectangle) {
        throw new UnsupportedOperationException();
    }

    // TODO SurfaceType getSurfaceType(Index tileIndex);

    // TODO SurfaceType getSurfaceTypeAbsolute(Index absoluteIndex);

    // TODO Index correctPosition(SyncItem syncItem, Index position);

    // TODO Index correctPosition(int radius, Index position);

    // TODO void createTerrainTileField(Collection<TerrainImagePosition> terrainImagePositions, Collection<SurfaceRect> surfaceRects);

    // TODO TerrainTile[][] getTerrainTileField();

    // TODO  void iteratorOverAllTerrainTiles(Rectangle tileRect, TerrainTileEvaluator terrainTileEvaluator);

}
