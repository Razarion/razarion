package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.client.renderer.model.GroundMesh;
import com.btxtech.client.terrain.slope.Slope;
import com.btxtech.client.terrain.slope.SlopeWater;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.dto.GroundSkeleton;
import com.btxtech.shared.dto.SlopeSkeleton;
import com.btxtech.shared.primitives.Ray3d;
import com.btxtech.shared.primitives.Vertex;

import javax.inject.Inject;
import javax.inject.Singleton;
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
    private static final double HIGHEST_POINT_IN_VIEW = 101; // Should be calculated
    private static final double LOWEST_POINT_IN_VIEW = -9; // Should be calculated
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private RenderService renderService;
    private ImageDescriptor coverImageDescriptor = ImageDescriptor.GRASS_1;
    private ImageDescriptor blenderImageDescriptor = ImageDescriptor.BLEND_3;
    // private ImageDescriptor blenderImageDescriptor = ImageDescriptor.GREY;
    private ImageDescriptor groundImageDescriptor = ImageDescriptor.GROUND_5;
    private ImageDescriptor groundBmImageDescriptor = ImageDescriptor.GROUND_BM_5;
    private GroundMesh groundMesh = new GroundMesh();
    private Water water = new Water(-7, -20); // Init here due to the editor
    private Logger logger = Logger.getLogger(TerrainSurface.class.getName());
    private Map<Integer, SlopeSkeleton> slopeSkeletonMap = new HashMap<>();
    private Map<Integer, Slope> slopeMap = new HashMap<>();
    private Collection<TerrainSlopePosition> terrainSlopePositions;
    private GroundSkeleton groundSkeleton;

    public void setAllSlopeSkeletons(Collection<SlopeSkeleton> slopeSkeletons) {
        slopeSkeletonMap.clear();
        for (SlopeSkeleton slopeSkeleton : slopeSkeletons) {
            slopeSkeletonMap.put(slopeSkeleton.getId(), slopeSkeleton);
        }
    }

    public void setGroundSkeleton(GroundSkeleton groundSkeleton) {
        this.groundSkeleton = groundSkeleton;
    }

    public void setTerrainSlopePositions(Collection<TerrainSlopePosition> terrainSlopePositions) {
        this.terrainSlopePositions = terrainSlopePositions;
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

    public void init() {
        logger.severe("Start setup surface");
        long time = System.currentTimeMillis();
        setupGround(MESH_NODES, MESH_NODES);
        water.clearAllTriangles();
        slopeMap.clear();

        for (TerrainSlopePosition terrainSlopePosition : terrainSlopePositions) {
            setupPlateau(terrainSlopePosition);
        }

        logger.severe("Setup surface took: " + (System.currentTimeMillis() - time));
    }

    public void fillBuffers() {
        init();
        renderService.fillBuffers();
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
        return HIGHEST_POINT_IN_VIEW;
    }

    public double getLowestPointInView() {
        return LOWEST_POINT_IN_VIEW;
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

    private void setupPlateau(TerrainSlopePosition terrainSlopePosition) {
        SlopeSkeleton slopeSkeleton = getSlopeSkeleton(terrainSlopePosition.getSlopeId());
        Slope slope;
        if (slopeSkeleton.getType() == SlopeSkeleton.Type.WATER) {
            slope = new SlopeWater(water, slopeSkeleton, terrainSlopePosition.getPolygon());
            slope.setSlopeImageDescriptor(ImageDescriptor.BEACH_01);
            slope.setSlopeBumpImageDescriptor(ImageDescriptor.BUMP_MAP_05);
            slope.setSlopeGroundSplattingImageDescriptor(ImageDescriptor.BLEND_4);
        } else if (slopeSkeleton.getType() == SlopeSkeleton.Type.LAND) {
            slope = new Slope(slopeSkeleton, terrainSlopePosition.getPolygon());
            slope.setSlopeImageDescriptor(ImageDescriptor.ROCK_5);
            slope.setSlopeBumpImageDescriptor(ImageDescriptor.BUMP_MAP_04);
            slope.setSlopeGroundSplattingImageDescriptor(ImageDescriptor.BLEND_4);
            slope.wrap(groundMesh);
        } else {
            throw new IllegalStateException("Unknown enum type: " + slopeSkeleton.getType());
        }
        slope.wrap(groundMesh);
        slope.setupGroundConnection(groundMesh);
        slopeMap.put(slopeMap.size(), slope);
    }

    private SlopeSkeleton getSlopeSkeleton(int id) {
        SlopeSkeleton slopeSkeleton = slopeSkeletonMap.get(id);
        if (slopeSkeleton == null) {
            throw new IllegalArgumentException("No entry in integerSlopeSkeletonMap for id: " + id);
        }
        return slopeSkeleton;
    }

    private void setupGround(int xCount, int yCount) {
        groundMesh = GroundModeler.generateGroundMesh(groundSkeleton, xCount, yCount);
        groundMesh.setupNorms();
    }

    public VertexList getGroundVertexList() {
        VertexList vertexList = groundMesh.provideVertexList();
        for (Slope slope : slopeMap.values()) {
            if (!slope.hasWater()) {
                vertexList.append(slope.getGroundPlateauConnector().getTopMesh().provideVertexList());
                vertexList.append(slope.getGroundPlateauConnector().getInnerConnectionVertexList());
            }
            vertexList.append(slope.getGroundPlateauConnector().getOuterConnectionVertexList());
        }
        return vertexList;
    }
}
