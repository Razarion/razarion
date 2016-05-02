package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.client.renderer.model.GroundMesh;
import com.btxtech.client.renderer.model.VertexData;
import com.btxtech.client.terrain.slope.MeshEntry;
import com.btxtech.client.terrain.slope.Slope;
import com.btxtech.client.terrain.slope.SlopeWater;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.SlopeSkeletonEntity;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Ray3d;
import com.btxtech.shared.primitives.Vertex;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 09.08.2015.
 */
@Singleton
public class TerrainSurface {
    public static final int MESH_SIZE = 4048;
    public static final int MESH_NODE_EDGE_LENGTH = 64;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private RenderService renderService;
    private ImageDescriptor coverImageDescriptor = ImageDescriptor.GRASS_1;
    private ImageDescriptor blenderImageDescriptor = ImageDescriptor.BLEND_3;
    // private ImageDescriptor blenderImageDescriptor = ImageDescriptor.GREY;
    private ImageDescriptor groundImageDescriptor = ImageDescriptor.GROUND_5;
    private ImageDescriptor groundBmImageDescriptor = ImageDescriptor.GROUND_BM_5;
    private double edgeDistance = 0.5;
    private double groundBumpMap = 2;
    private double groundSpecularHardness = 5;
    private double groundSpecularIntensity = 0.255;
    private GroundMesh groundMesh = new GroundMesh();
    private SlopeWater beach;
    private Water water = new Water(-7, -20); // Init here due to the editor
    private Logger logger = Logger.getLogger(TerrainSurface.class.getName());
    private final double highestPointInView = 101; // Should be calculated
    private final double lowestPointInView = -9; // Should be calculated
    private Map<Integer, SlopeSkeletonEntity> slopeSkeletonMap = new HashMap<>();
    private Map<Integer, Slope> slopeMap = new HashMap<>();

    public void init() {
        logger.severe("Start setup surface");
        long time = System.currentTimeMillis();
        water.clearAllTriangles();
        setupGround();
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

//    private void setupBeach() {
//        SlopeSkeletonFactory beachSlopeSkeletonFactory = new SlopeSkeletonFactory(100, new Shape(beachSlopeConfigEntity.getShape()));
//        SlopeSkeleton slopeSkeleton = beachSlopeSkeletonFactory.sculpt(beachSlopeConfigEntity.getFractalShift(), beachSlopeConfigEntity.getFractalRoughness());
//
//        beach = new SlopeWater(water, slopeSkeleton, beachSlopeConfigEntity.getVerticalSpace(), Arrays.asList(new DecimalPosition(2000, 1000), new DecimalPosition(3000, 1000), new DecimalPosition(3000, 1500), new DecimalPosition(2000, 1500)), beachSlopeConfigEntity);
//        beach.setSlopeImageDescriptor(ImageDescriptor.BEACH_01);
//        beach.setSlopeBumpImageDescriptor(ImageDescriptor.BUMP_MAP_05);
//        beach.setSlopeGroundSplattingImageDescriptor(ImageDescriptor.BLEND_4);
//        beach.wrap(groundMesh);
//        groundBeachConnector = new GroundSlopeConnector(groundMesh, beach);
//        groundBeachConnector.stampOut();
//    }

//    private SlopeConfigEntity setupPlateauConfigEntity() {
//        plateauConfigEntity = new SlopeConfigEntity();
//        plateauConfigEntity.setBumpMapDepth(0.5);
//        plateauConfigEntity.setFractalRoughness(0);
//        plateauConfigEntity.setFractalShift(0);
//        plateauConfigEntity.setSpecularHardness(0.2);
//        plateauConfigEntity.setSpecularIntensity(1.0);
//        plateauConfigEntity.setVerticalSpace(10);
//        // plateauConfigEntity.setShape(Shape.SHAPE_1);
//        return plateauConfigEntity;
//    }
//
//
//    private void setupBeachConfigEntity() {
//        beachSlopeConfigEntity = new SlopeConfigEntity();
//        beachSlopeConfigEntity.setBumpMapDepth(2);
//        beachSlopeConfigEntity.setFractalRoughness(0.6);
//        beachSlopeConfigEntity.setFractalShift(16);
//        beachSlopeConfigEntity.setSpecularHardness(6);
//        beachSlopeConfigEntity.setSpecularIntensity(0.1);
//        beachSlopeConfigEntity.setVerticalSpace(30);
//        beachSlopeConfigEntity.setSlopeFactorDistance(0.4);
//        List<SlopeShapeEntity> shape = new ArrayList<>();
//        shape.add(new SlopeShapeEntity(new Index(400, -15), 1));
//        shape.add(new SlopeShapeEntity(new Index(350, -13), 1));
//        shape.add(new SlopeShapeEntity(new Index(300, -11), 1));
//        shape.add(new SlopeShapeEntity(new Index(250, -9), 1));
//        shape.add(new SlopeShapeEntity(new Index(200, -7), 1));
//        shape.add(new SlopeShapeEntity(new Index(150, -5), 1));
//        shape.add(new SlopeShapeEntity(new Index(100, -3), 1));
//        shape.add(new SlopeShapeEntity(new Index(50, -1), 0.5f));
//        shape.add(new SlopeShapeEntity(new Index(0, 0), 0));
//        beachSlopeConfigEntity.setShape(shape);
//    }

    public void setAllSlopeSkeletonEntities(Collection<SlopeSkeletonEntity> slopeSkeletonEntities) {
        slopeSkeletonMap.clear();
        for (SlopeSkeletonEntity slopeSkeletonEntity : slopeSkeletonEntities) {
            slopeSkeletonMap.put(slopeSkeletonEntity.getId().intValue(), slopeSkeletonEntity);
        }
    }

    public void setSlopeSkeletonEntity(SlopeSkeletonEntity slopeSkeletonEntity) {
        slopeSkeletonMap.put(slopeSkeletonEntity.getId().intValue(), slopeSkeletonEntity);
    }

    private void setupGround() {
        groundMesh.reset(MESH_NODE_EDGE_LENGTH, MESH_SIZE, MESH_SIZE, 0);

        // TODO final FractalField heightField = FractalField.createSaveFractalField(MESH_SIZE, MESH_SIZE, 1.0, -10, 10);
        //final FractalField grassGround = FractalField.createSaveFractalField(MESH_SIZE, MESH_SIZE, 1.0, 0, 1);
        groundMesh.iterate(new GroundMesh.VertexVisitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                double splatting;
                if (index.getX() % 2 == 0) {
                    if (index.getY() % 2 == 0) {
                        splatting = 0;
                    } else {
                        splatting = 1;
                    }
                } else {
                    if (index.getY() % 2 == 0) {
                        splatting = 1;
                    } else {
                        splatting = 0;
                    }
                }
                // groundMesh.getVertexDataSafe(index).setEdge(splatting);
                // groundMesh.getVertexDataSafe(index).setEdge(grassGround.getValue(index));
                // TODO mesh.getVertexDataSafe(index).add(new Vertex(0, 0, heightField.getValue(index)));
            }
        });
        groundMesh.setupNorms();
    }

    public void sculpt() {
        init();
        renderService.fillBuffers();
    }

    public boolean isFree(double absoluteX, double absoluteY) {
        return true; // TODO
    }

    public VertexList getVertexList() {
        VertexList vertexList = groundMesh.provideVertexList();
        for (Slope slope : slopeMap.values()) {
            if(!slope.hasWater()) {
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

    public double getSplattingBlur() {
        return edgeDistance;
    }

    public void setEdgeDistance(double edgeDistance) {
        this.edgeDistance = edgeDistance;
    }

    public double getGroundBumpMap() {
        return groundBumpMap;
    }

    public void setGroundBumpMap(double groundBumpMap) {
        this.groundBumpMap = groundBumpMap;
    }

    public double getGroundSpecularHardness() {
        return groundSpecularHardness;
    }

    public void setGroundSpecularHardness(double groundSpecularHardness) {
        this.groundSpecularHardness = groundSpecularHardness;
    }

    public double getGroundSpecularIntensity() {
        return groundSpecularIntensity;
    }

    public void setGroundSpecularIntensity(double groundSpecularIntensity) {
        this.groundSpecularIntensity = groundSpecularIntensity;
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

    public void handlePickRay(Ray3d worldPickRay) {
        // Find multiplier where the ray hits the ground (z = 0). start + m*direction -> z = 0
        double m = -worldPickRay.getStart().getZ() / worldPickRay.getDirection().getZ();
        Vertex pointOnGround = worldPickRay.getPoint(m);
        logger.severe("Point On Ground: " + pointOnGround);
        VertexData vertexData = groundMesh.getVertexFromAbsoluteXY(pointOnGround.toXY());
        if (vertexData != null) {
            logger.severe("Ground VertexData: " + vertexData);
            return;
        }
        MeshEntry meshEntry = beach.pick(pointOnGround);
        logger.severe("beach MeshEntry: " + meshEntry);
    }
}
