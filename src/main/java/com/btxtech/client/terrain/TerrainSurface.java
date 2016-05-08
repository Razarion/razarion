package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.client.renderer.model.GroundMesh;
import com.btxtech.client.terrain.slope.Slope;
import com.btxtech.client.terrain.slope.SlopeWater;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.TerrainSlopePositionEntity;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.dto.GroundSkeleton;
import com.btxtech.shared.dto.SlopeSkeleton;
import com.btxtech.shared.primitives.Ray3d;
import com.btxtech.shared.primitives.Vertex;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 09.08.2015.
 */
@Singleton
public class TerrainSurface {
    public static final int MESH_NODE_EDGE_LENGTH = 64;
    public static final int MESH_NODES = 64;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private RenderService renderService;
    @Inject
    private Event<TerrainInitializedEvent> terrainInitializedEvent;
    private ImageDescriptor coverImageDescriptor = ImageDescriptor.GRASS_1;
    private ImageDescriptor blenderImageDescriptor = ImageDescriptor.BLEND_3;
    // private ImageDescriptor blenderImageDescriptor = ImageDescriptor.GREY;
    private ImageDescriptor groundImageDescriptor = ImageDescriptor.GROUND_5;
    private ImageDescriptor groundBmImageDescriptor = ImageDescriptor.GROUND_BM_5;
    private GroundMesh groundMesh = new GroundMesh();
    private Water water = new Water(-7, -20); // Init here due to the editor
    private Logger logger = Logger.getLogger(TerrainSurface.class.getName());
    private final double highestPointInView = 101; // Should be calculated
    private final double lowestPointInView = -9; // Should be calculated
    private Map<Integer, SlopeSkeleton> slopeSkeletonMap = new HashMap<>();
    private Map<Integer, Slope> slopeMap = new HashMap<>();
    private Map<Integer, TerrainSlopePositionEntity> terrainSlopePositionEntities = new HashMap<>();
    private GroundSkeleton groundSkeleton;

    public TerrainSurface() {
        terrainSlopePositionEntities.put(1, new TerrainSlopePositionEntity(1, 2706, Arrays.asList(new Index(580, 500), new Index(1000, 500), new Index(1000, 1120))));
        terrainSlopePositionEntities.put(2, new TerrainSlopePositionEntity(2, 12517, Arrays.asList(new Index(2000, 1000), new Index(3000, 1000), new Index(2000, 1500))));
    }

    public void init() {
        logger.severe("Start setup surface");
        long time = System.currentTimeMillis();
        setupGround(MESH_NODES, MESH_NODES);
        water.clearAllTriangles();
        slopeMap.clear();

        for (TerrainSlopePositionEntity terrainSlopePositionEntity : terrainSlopePositionEntities.values()) {
            setupPlateau(terrainSlopePositionEntity);
        }

        terrainInitializedEvent.fire(new TerrainInitializedEvent());
        logger.severe("Setup surface took: " + (System.currentTimeMillis() - time));
    }

    public void setupPlateau(TerrainSlopePositionEntity terrainSlopePositionEntity) {
        SlopeSkeleton slopeSkeleton = getSlopeSkeleton(terrainSlopePositionEntity.getSlopeId());
        Slope slope;
        if (slopeSkeleton.getType() == SlopeSkeleton.Type.WATER) {
            slope = new SlopeWater(water, slopeSkeleton, terrainSlopePositionEntity.getPolygon());
            slope.setSlopeImageDescriptor(ImageDescriptor.BEACH_01);
            slope.setSlopeBumpImageDescriptor(ImageDescriptor.BUMP_MAP_05);
            slope.setSlopeGroundSplattingImageDescriptor(ImageDescriptor.BLEND_4);
        } else if (slopeSkeleton.getType() == SlopeSkeleton.Type.LAND) {
            slope = new Slope(slopeSkeleton, terrainSlopePositionEntity.getPolygon());
            slope.setSlopeImageDescriptor(ImageDescriptor.ROCK_5);
            slope.setSlopeBumpImageDescriptor(ImageDescriptor.BUMP_MAP_04);
            slope.setSlopeGroundSplattingImageDescriptor(ImageDescriptor.BLEND_4);
            slope.wrap(groundMesh);
        } else {
            throw new IllegalStateException("Unknown enum type: " + slopeSkeleton.getType());
        }
        slope.wrap(groundMesh);
        slope.setupGroundConnection(groundMesh);
        slopeMap.put(terrainSlopePositionEntity.getId(), slope);
    }

    private SlopeSkeleton getSlopeSkeleton(int id) {
        SlopeSkeleton slopeSkeleton = slopeSkeletonMap.get(id);
        if (slopeSkeleton == null) {
            throw new IllegalArgumentException("No entry in integerSlopeSkeletonMap for id: " + id);
        }
        return slopeSkeleton;
    }

    public void setAllSlopeSkeletons(Collection<SlopeSkeleton> slopeSkeletons) {
        slopeSkeletonMap.clear();
        for (SlopeSkeleton slopeSkeleton : slopeSkeletons) {
            slopeSkeletonMap.put(slopeSkeleton.getId(), slopeSkeleton);
        }
    }

    public void setSlopeSkeleton(SlopeSkeleton slopeSkeleton) {
        slopeSkeletonMap.put(slopeSkeleton.getId(), slopeSkeleton);
        for (Slope slope : slopeMap.values()) {
            if (slope.getSlopeSkeleton().getId() == slopeSkeleton.getId()) {
                slope.updateSlopeSkeleton(slopeSkeleton);
                logger.severe("setSlopeSkeleton " + slopeSkeleton.getId() + " size: " + slopeSkeletonMap.size() + " SlopeFactorDistance: " + slopeSkeleton.getSlopeFactorDistance());
            }
        }
    }

    private void setupGround(int xCount, int yCount) {
        groundMesh = GroundModeler.generateGroundMesh(groundSkeleton, xCount, yCount);
        groundMesh.setupNorms();
    }

    public void fillBuffers() {
        init();
        renderService.fillBuffers();
    }

    public boolean isFree(double absoluteX, double absoluteY) {
        return true; // TODO
    }

    public VertexList getVertexList() {
        VertexList vertexList = groundMesh.provideVertexList();
        for (Slope slope : slopeMap.values()) {
            if (!slope.hasWater()) {
                vertexList.append(slope.getGroundPlateauConnector().getTopMesh().provideVertexList());
                vertexList.append(slope.getGroundPlateauConnector().getConnectionVertexList());
            }
            vertexList.append(slope.getGroundPlateauConnector().getOuterConnectionVertexList());
        }
        return vertexList;
    }

    public ImageDescriptor getGroundImageDescriptor() {
        return groundImageDescriptor;
    }

    public ImageDescriptor getGroundBmImageDescriptor() {
        return groundBmImageDescriptor;
    }

    public ImageDescriptor getCoverImageDescriptor() {
        return coverImageDescriptor;
    }

    public ImageDescriptor getBlenderImageDescriptor() {
        return blenderImageDescriptor;
    }

    public double getHighestPointInView() {
        return highestPointInView;
    }

    public double getLowestPointInView() {
        return lowestPointInView;
    }

    public Slope getSlope(int id) {
        return slopeMap.get(id);
    }

    public Collection<Integer> getSlopeIds() {
        return slopeMap.keySet();
    }

    public Water getWater() {
        return water;
    }

    public GroundSkeleton getGroundSkeleton() {
        return groundSkeleton;
    }

    public void setGroundSkeleton(GroundSkeleton groundSkeleton) {
        this.groundSkeleton = groundSkeleton;
    }

    public Collection<Integer> getTerrainSlopePositionIds() {
        return terrainSlopePositionEntities.keySet();
    }

    public TerrainSlopePositionEntity getTerrainSlopePositionEntity(int id) {
        return terrainSlopePositionEntities.get(id);
    }

    public Vertex calculatePositionOnTerrain(Ray3d worldPickRay) {
        // Find multiplier where the ray hits the ground (z = 0). start + m*direction -> z = 0
        double m = -worldPickRay.getStart().getZ() / worldPickRay.getDirection().getZ();
        return worldPickRay.getPoint(m);
//        logger.severe("Point On Ground: " + pointOnGround);
//        VertexData vertexData = originalGroundMesh.getVertexFromAbsoluteXY(pointOnGround.toXY());
//        if (vertexData != null) {
//            logger.severe("Ground VertexData: " + vertexData);
//        } else {
//            logger.severe("Position not on ground");
//        }
    }
}
