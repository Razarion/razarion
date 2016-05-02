package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.client.renderer.model.GroundMesh;
import com.btxtech.client.renderer.model.VertexData;
import com.btxtech.client.terrain.slope.Slope;
import com.btxtech.client.terrain.slope.SlopeWater;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.shared.GroundConfigEntity;
import com.btxtech.shared.GroundHeightEntry;
import com.btxtech.shared.GroundSkeletonEntity;
import com.btxtech.shared.GroundSplattingEntry;
import com.btxtech.shared.SlopeSkeletonEntity;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Ray3d;
import com.btxtech.shared.primitives.Vertex;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
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
    private Map<Integer, SlopeSkeletonEntity> slopeSkeletonMap = new HashMap<>();
    private Map<Integer, Slope> slopeMap = new HashMap<>();
    private GroundConfigEntity groundConfigEntity;
    private GroundSkeletonEntity groundSkeletonEntity;

    public TerrainSurface() {
        groundConfigEntity = new GroundConfigEntity();
        groundConfigEntity.setSplattingDistance(0.5);
        groundConfigEntity.setBumpMapDepth(2);
        groundConfigEntity.setSpecularHardness(5);
        groundConfigEntity.setSpecularIntensity(0.25);
        groundConfigEntity.setHeightXCount(20);
        groundConfigEntity.setHeightYCount(20);
        groundConfigEntity.setSplattingXCount(30);
        groundConfigEntity.setSplattingYCount(30);
        groundConfigEntity.setSplattingFractalMin(0);
        groundConfigEntity.setSplattingFractalMax(1);
    }

    public void init() {
        logger.severe("Start setup surface");
        long time = System.currentTimeMillis();
        setupGround(MESH_NODES, MESH_NODES);
        water.clearAllTriangles();
        slopeMap.clear();
        setupPlateau(2005, Arrays.asList(new DecimalPosition(580, 500), new DecimalPosition(1000, 500), new DecimalPosition(1000, 1120)));
        setupBeach(12514, Arrays.asList(new DecimalPosition(2000, 1000), new DecimalPosition(3000, 1000), new DecimalPosition(3000, 1500), new DecimalPosition(2000, 1500)));
        logger.severe("Setup surface took: " + (System.currentTimeMillis() - time));
    }

    public void setupPlateau(int id, List<DecimalPosition> corners) {
        SlopeSkeletonEntity slopeSkeletonEntity = getSlopeSkeleton(id);
        Slope plateau = new Slope(slopeSkeletonEntity, corners);
        plateau.setSlopeImageDescriptor(ImageDescriptor.ROCK_5);
        plateau.setSlopeBumpImageDescriptor(ImageDescriptor.BUMP_MAP_04);
        plateau.setSlopeGroundSplattingImageDescriptor(ImageDescriptor.BLEND_4);
        plateau.wrap(groundMesh);
        plateau.setupGroundConnection(groundMesh);
        slopeMap.put(slopeMap.size(), plateau);
    }

    public void setupBeach(int id, List<DecimalPosition> corners) {
        SlopeSkeletonEntity slopeSkeletonEntity = getSlopeSkeleton(id);
        SlopeWater beach = new SlopeWater(water, slopeSkeletonEntity, corners);
        beach.setSlopeImageDescriptor(ImageDescriptor.BEACH_01);
        beach.setSlopeBumpImageDescriptor(ImageDescriptor.BUMP_MAP_05);
        beach.setSlopeGroundSplattingImageDescriptor(ImageDescriptor.BLEND_4);
        beach.wrap(groundMesh);
        beach.setupGroundConnection(groundMesh);
        slopeMap.put(slopeMap.size(), beach);
    }

    private SlopeSkeletonEntity getSlopeSkeleton(int id) {
        SlopeSkeletonEntity slopeSkeletonEntity = slopeSkeletonMap.get(id);
        if (slopeSkeletonEntity == null) {
            throw new IllegalArgumentException("No entry in integerSlopeSkeletonMap for id: " + id);
        }
        return slopeSkeletonEntity;
    }

    public void setAllSlopeSkeletonEntities(Collection<SlopeSkeletonEntity> slopeSkeletonEntities) {
        slopeSkeletonMap.clear();
        for (SlopeSkeletonEntity slopeSkeletonEntity : slopeSkeletonEntities) {
            slopeSkeletonMap.put(slopeSkeletonEntity.getId().intValue(), slopeSkeletonEntity);
        }
    }

    public void setSlopeSkeletonEntity(SlopeSkeletonEntity slopeSkeletonEntity) {
        slopeSkeletonMap.put(slopeSkeletonEntity.getId().intValue(), slopeSkeletonEntity);
        for (Slope slope : slopeMap.values()) {
            if (slope.getSlopeSkeletonEntity().equals(slopeSkeletonEntity)) {
                slope.setSlopeSkeletonEntity(slopeSkeletonEntity);
            }
        }
        logger.severe("setSlopeSkeletonEntity " + slopeSkeletonEntity.getId() + " size: " + slopeSkeletonMap.size() + " SlopeFactorDistance: " + slopeSkeletonEntity.getSlopeFactorDistance());
    }

    private void setupGround(int xCount, int yCount) {
        groundSkeletonEntity = new GroundSkeletonEntity();
        FractalField heightField = FractalField.createSaveFractalField(groundConfigEntity.getHeightXCount(), groundConfigEntity.getHeightYCount(), groundConfigEntity.getHeightFractalRoughness(), -groundConfigEntity.getHeightFractalShift() / 2.0, groundConfigEntity.getHeightFractalShift());
        List<GroundHeightEntry> groundHeightEntries = new ArrayList<>();
        for (int x = 0; x < groundConfigEntity.getHeightXCount(); x++) {
            for (int y = 0; y < groundConfigEntity.getHeightYCount(); y++) {
                groundHeightEntries.add(new GroundHeightEntry(x, y, heightField.getValue(x, y)));
            }
        }
        FractalField splattingField = FractalField.createSaveFractalField(groundConfigEntity.getSplattingXCount(), groundConfigEntity.getSplattingYCount(), groundConfigEntity.getSplattingFractalRoughness(), groundConfigEntity.getSplattingFractalMin(), groundConfigEntity.getSplattingFractalMax());
        List<GroundSplattingEntry> groundSplattingEntries = new ArrayList<>();
        for (int x = 0; x < groundConfigEntity.getSplattingXCount(); x++) {
            for (int y = 0; y < groundConfigEntity.getSplattingYCount(); y++) {
                groundSplattingEntries.add(new GroundSplattingEntry(x, y, splattingField.getValue(x, y)));
            }
        }
        groundSkeletonEntity.setValues(groundSplattingEntries, groundHeightEntries, groundConfigEntity);
        groundConfigEntity.setGroundSkeletonEntity(groundSkeletonEntity);
        ///////////////
        groundMesh = groundSkeletonEntity.generateGroundMesh(xCount, yCount);
        groundMesh.setupNorms();
    }

    public GroundConfigEntity getGroundConfigEntity() {
        return groundConfigEntity;
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

    public GroundSkeletonEntity getGroundSkeletonEntity() {
        return groundSkeletonEntity;
    }

    public void handlePickRay(Ray3d worldPickRay) {
        // Find multiplier where the ray hits the ground (z = 0). start + m*direction -> z = 0
        double m = -worldPickRay.getStart().getZ() / worldPickRay.getDirection().getZ();
        Vertex pointOnGround = worldPickRay.getPoint(m);
        logger.severe("Point On Ground: " + pointOnGround);
        VertexData vertexData = groundMesh.getVertexFromAbsoluteXY(pointOnGround.toXY());
        if (vertexData != null) {
            logger.severe("Ground VertexData: " + vertexData);
        } else {
            logger.severe("Position not on ground");
        }
    }
}
