package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.client.renderer.model.GroundMesh;
import com.btxtech.client.terrain.slope.Plateau;
import com.btxtech.client.terrain.slope.Shape;
import com.btxtech.client.terrain.slope.ShapeTemplate;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.PlateauConfigEntity;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Vertex;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 09.08.2015.
 */
@Singleton
public class TerrainSurface {
    public static final int MESH_SIZE = 2048;
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
    private Plateau plateau;
    private GroundSlopeConnector groundSlopeConnector;
    private PlateauConfigEntity plateauConfigEntity;
    private Beach beach;
    private Logger logger = Logger.getLogger(TerrainSurface.class.getName());
    private final double highestPointInView = 101; // Should be calculated
    private final double lowestPointInView = -9; // Should be calculated

    public void init() {
        // setupPlateauConfigEntity();
        beach = new Beach(groundMesh);
        setupGround();
        setupPlateau();
    }

    public void setupPlateau() {
        ShapeTemplate shapeTemplate = new ShapeTemplate(100, new Shape(plateauConfigEntity.getShape()));
//        logger.severe("---------------------------------");
//        List<ShapeEntryEntity> shape = plateauConfigEntity.getShape();
//        for (int i = 0; i < shape.size(); i++) {
//            ShapeEntryEntity shapeEntryEntity = shape.get(i);
//            logger.severe(i + ":" + shapeEntryEntity.getPosition().testString());
//        }
//        logger.severe("---------------------------------");

        shapeTemplate.sculpt(plateauConfigEntity.getFractalShift(), plateauConfigEntity.getFractalShift());
        plateau = new Plateau(shapeTemplate, plateauConfigEntity.getVerticalSpace(), Arrays.asList(new DecimalPosition(580, 500), new DecimalPosition(1000, 500), new DecimalPosition(1000, 1120)));
        plateau.wrap(groundMesh);

        groundSlopeConnector = new GroundSlopeConnector(groundMesh, plateau);
        groundSlopeConnector.stampOut();
    }

    private PlateauConfigEntity setupPlateauConfigEntity() {
        plateauConfigEntity = new PlateauConfigEntity();
        plateauConfigEntity.setBumpMapDepth(0.5);
        plateauConfigEntity.setFractalRoughness(0);
        plateauConfigEntity.setFractalShift(0);
        plateauConfigEntity.setSpecularHardness(0.2);
        plateauConfigEntity.setSpecularIntensity(1.0);
        plateauConfigEntity.setVerticalSpace(10);
        // plateauConfigEntity.setShape(Shape.SHAPE_1);
        return plateauConfigEntity;
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

    public Plateau getPlateau() {
        return plateau;
    }

    public PlateauConfigEntity getPlateauConfigEntity() {
        return plateauConfigEntity;
    }

    public VertexList getVertexList() {
        VertexList vertexList = groundMesh.provideVertexList();
        vertexList.append(groundSlopeConnector.getTopMesh().provideVertexList());
        vertexList.append(groundSlopeConnector.getConnectionVertexList());
        vertexList.append(groundSlopeConnector.getOuterConnectionVertexList());
        return vertexList;
    }

    public VertexList getWaterVertexList() {
        return beach.provideWaterVertexList();
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

    public Beach getBeach() {
        return beach;
    }

    public double getHighestPointInView() {
        return highestPointInView;
    }

    public double getLowestPointInView() {
        return lowestPointInView;
    }

    public void setPlateauConfigEntity(PlateauConfigEntity plateauConfigEntity) {
        this.plateauConfigEntity = plateauConfigEntity;
    }
}
