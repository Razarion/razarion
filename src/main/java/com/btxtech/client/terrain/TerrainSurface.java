package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.client.renderer.model.GroundMesh;
import com.btxtech.client.terrain.slope.Shape;
import com.btxtech.client.terrain.slope.ShapeTemplate;
import com.btxtech.client.terrain.slope.Slope;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.SlopeConfigEntity;
import com.btxtech.shared.SlopeShapeEntity;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Vertex;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private ImageDescriptor slopeImageDescriptor = ImageDescriptor.ROCK_5;
    private ImageDescriptor slopePumpMapImageDescriptor = ImageDescriptor.BUMP_MAP_04;
    private ImageDescriptor beachImageDescriptor = ImageDescriptor.BEACH_01;
    private ImageDescriptor beachPumpMapImageDescriptor = ImageDescriptor.BUMP_MAP_05;
    private double edgeDistance = 0.5;
    private double groundBumpMap = 2;
    private GroundMesh groundMesh = new GroundMesh();
    private Slope plateau;
    private Slope beach;
    private GroundSlopeConnector groundPlateauConnector;
    private GroundSlopeConnector groundBeachConnector;
    private SlopeConfigEntity plateauConfigEntity;
    @Deprecated
    private Beach_OLD beach_old;
    private Logger logger = Logger.getLogger(TerrainSurface.class.getName());
    private final double highestPointInView = 101; // Should be calculated
    private final double lowestPointInView = -9; // Should be calculated

    public void init() {
        // setupPlateauConfigEntity();
        beach_old = new Beach_OLD(groundMesh);
        setupGround();
        setupPlateau();
        setupBeach();
    }

    public void setupPlateau() {
        ShapeTemplate plateauShapeTemplate = new ShapeTemplate(100, new Shape(plateauConfigEntity.getShape()));
//        logger.severe("---------------------------------");
//        List<ShapeEntryEntity> shape = plateauConfigEntity.getShape();
//        for (int i = 0; i < shape.size(); i++) {
//            ShapeEntryEntity shapeEntryEntity = shape.get(i);
//            logger.severe(i + ":" + shapeEntryEntity.getPosition().testString());
//        }
//        logger.severe("---------------------------------");

        plateauShapeTemplate.sculpt(plateauConfigEntity.getFractalShift(), plateauConfigEntity.getFractalShift());
        plateau = new Slope(plateauShapeTemplate, plateauConfigEntity.getVerticalSpace(), Arrays.asList(new DecimalPosition(580, 500), new DecimalPosition(1000, 500), new DecimalPosition(1000, 1120)));
        plateau.wrap(groundMesh);
        groundPlateauConnector = new GroundSlopeConnector(groundMesh, plateau);
        groundPlateauConnector.stampOut();
    }

    private void setupBeach() {
        SlopeConfigEntity beachSlopeConfigEntity = setupBeachConfigEntity();
        ShapeTemplate beachShapeTemplate = new ShapeTemplate(100, new Shape(beachSlopeConfigEntity.getShape()));
        beachShapeTemplate.sculpt(beachSlopeConfigEntity.getFractalShift(), beachSlopeConfigEntity.getFractalShift());

        beach = new Slope(beachShapeTemplate, beachSlopeConfigEntity.getVerticalSpace(), Arrays.asList(new DecimalPosition(2000, 1000), new DecimalPosition(3000, 1000), new DecimalPosition(3000, 1500), new DecimalPosition(2000, 1500)));
        beach.wrap(groundMesh);
        groundBeachConnector = new GroundSlopeConnector(groundMesh, beach);
        groundBeachConnector.stampOut();
    }

    private SlopeConfigEntity setupPlateauConfigEntity() {
        plateauConfigEntity = new SlopeConfigEntity();
        plateauConfigEntity.setBumpMapDepth(0.5);
        plateauConfigEntity.setFractalRoughness(0);
        plateauConfigEntity.setFractalShift(0);
        plateauConfigEntity.setSpecularHardness(0.2);
        plateauConfigEntity.setSpecularIntensity(1.0);
        plateauConfigEntity.setVerticalSpace(10);
        // plateauConfigEntity.setShape(Shape.SHAPE_1);
        return plateauConfigEntity;
    }


    private SlopeConfigEntity setupBeachConfigEntity() {
        SlopeConfigEntity beachSlopeConfigEntity = new SlopeConfigEntity();
        beachSlopeConfigEntity.setBumpMapDepth(0.5);
        beachSlopeConfigEntity.setFractalRoughness(0);
        beachSlopeConfigEntity.setFractalShift(0);
        beachSlopeConfigEntity.setSpecularHardness(0.2);
        beachSlopeConfigEntity.setSpecularIntensity(1.0);
        beachSlopeConfigEntity.setVerticalSpace(10);
        List<SlopeShapeEntity> shape = new ArrayList<>();
        shape.add(new SlopeShapeEntity(new Index(-8, 400), 1));
        shape.add(new SlopeShapeEntity(new Index(-7, 350), 1));
        shape.add(new SlopeShapeEntity(new Index(-6, 300), 1));
        shape.add(new SlopeShapeEntity(new Index(-5, 250), 1));
        shape.add(new SlopeShapeEntity(new Index(-4, 200), 1));
        shape.add(new SlopeShapeEntity(new Index(-3, 150), 1));
        shape.add(new SlopeShapeEntity(new Index(-2, 100), 1));
        shape.add(new SlopeShapeEntity(new Index(-1, 50), 0.5f));
        shape.add(new SlopeShapeEntity(new Index(0, 0), 0));
        beachSlopeConfigEntity.setShape(shape);
        return beachSlopeConfigEntity;
    }

    private void setupGround() {
        groundMesh.reset(MESH_NODE_EDGE_LENGTH, MESH_SIZE, MESH_SIZE, 0);

        final FractalField heightField = FractalField.createSaveFractalField(MESH_SIZE, MESH_SIZE, 1.0, -10, 10);
        final FractalField grassGround = FractalField.createSaveFractalField(MESH_SIZE, MESH_SIZE, 1.0, 0, 1);
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
                groundMesh.getVertexDataSafe(index).setEdge(grassGround.getValue(index));
                // TODO mesh.getVertexDataSafe(index).add(new Vertex(0, 0, heightField.getValue(index)));
            }
        });
        groundMesh.setupNorms();
    }

    public void sculpt() {
        setupPlateau();
        renderService.fillBuffers();
    }

    public boolean isFree(double absoluteX, double absoluteY) {
        return true; // TODO
    }

    public Slope getPlateau() {
        return plateau;
    }

    public SlopeConfigEntity getPlateauConfigEntity() {
        return plateauConfigEntity;
    }

    public VertexList getVertexList() {
        VertexList vertexList = groundMesh.provideVertexList();
        vertexList.append(groundPlateauConnector.getTopMesh().provideVertexList());
        vertexList.append(groundPlateauConnector.getConnectionVertexList());
        vertexList.append(groundPlateauConnector.getOuterConnectionVertexList());
        vertexList.append(groundBeachConnector.getOuterConnectionVertexList()); // TODO in beach no inner VertexList
        return vertexList;
    }

    public VertexList getWaterVertexList() {
        return beach_old.provideWaterVertexList();
    }

    public ImageDescriptor getGroundImageDescriptor() {
        return groundImageDescriptor;
    }

    public ImageDescriptor getGroundBmImageDescriptor() {
        return groundBmImageDescriptor;
    }

    public ImageDescriptor getSlopeImageDescriptor() {
        return slopeImageDescriptor;
    }

    public ImageDescriptor getCoverImageDescriptor() {
        return coverImageDescriptor;
    }

    public ImageDescriptor getBlenderImageDescriptor() {
        return blenderImageDescriptor;
    }

    public ImageDescriptor getSlopePumpMapImageDescriptor() {
        return slopePumpMapImageDescriptor;
    }

    public double getEdgeDistance() {
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

    public ImageDescriptor getBeachImageDescriptor() {
        return beachImageDescriptor;
    }

    public void setBeachImageDescriptor(ImageDescriptor beachImageDescriptor) {
        this.beachImageDescriptor = beachImageDescriptor;
    }

    public ImageDescriptor getBeachPumpMapImageDescriptor() {
        return beachPumpMapImageDescriptor;
    }

    public void setBeachPumpMapImageDescriptor(ImageDescriptor beachPumpMapImageDescriptor) {
        this.beachPumpMapImageDescriptor = beachPumpMapImageDescriptor;
    }

    public Beach_OLD getBeach() {
        return beach_old;
    }

    public double getHighestPointInView() {
        return highestPointInView;
    }

    public double getLowestPointInView() {
        return lowestPointInView;
    }

    public void setPlateauConfigEntity(SlopeConfigEntity plateauConfigEntity) {
        this.plateauConfigEntity = plateauConfigEntity;
    }
}
