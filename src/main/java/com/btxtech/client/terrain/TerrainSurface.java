package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.TerrainEditorService;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.client.renderer.model.Mesh;
import com.btxtech.client.terrain.slope.Plateau;
import com.btxtech.client.terrain.slope.Shape;
import com.btxtech.client.terrain.slope.ShapeTemplate;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Vertex;
import org.jboss.errai.common.client.api.Caller;

import javax.annotation.PostConstruct;
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
    public static final int MESH_SIZE = 1024;
    public static final int MESH_NODE_EDGE_LENGTH = 64;
    @Inject
    private Caller<TerrainEditorService> terrainEditorService;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private RenderService renderService;
    private Mesh mesh = new Mesh();
    private ImageDescriptor coverImageDescriptor = ImageDescriptor.GRASS_1;
    private ImageDescriptor blenderImageDescriptor = ImageDescriptor.BLEND_3;
    private ImageDescriptor groundImageDescriptor = ImageDescriptor.GROUND_5;
    private ImageDescriptor groundBmImageDescriptor = ImageDescriptor.GROUND_BM_5;
    private ImageDescriptor slopeImageDescriptor = ImageDescriptor.ROCK_5;
    private ImageDescriptor slopePumpMapImageDescriptor = ImageDescriptor.BUMP_MAP_04;
    private ImageDescriptor beachImageDescriptor = ImageDescriptor.BEACH_01;
    private ImageDescriptor beachPumpMapImageDescriptor = ImageDescriptor.BUMP_MAP_05;
    private double edgeDistance = 0.5;
    private double groundBumpMap = 2;
    private Plateau plateau;
    private Beach beach;
    private Logger logger = Logger.getLogger(TerrainSurface.class.getName());
    private final double highestPointInView = 101; // Should be calculated
    private final double lowestPointInView = -9; // Should be calculated

    @PostConstruct
    public void init() {
        setupGround();

        ShapeTemplate shapeTemplate = new ShapeTemplate(10, new Shape());
        shapeTemplate.sculpt(20, 1);
        plateau = new Plateau(shapeTemplate, 20, Arrays.asList(new DecimalPosition(200, 200), new DecimalPosition(600, 200), new DecimalPosition(600, 600)));
        plateau.wrap();
        beach = new Beach(mesh);
    }

    private void setupGround() {
        mesh.reset(MESH_NODE_EDGE_LENGTH, MESH_NODE_EDGE_LENGTH, MESH_SIZE, MESH_SIZE, 0);

        final FractalField heightField = FractalField.createSaveFractalField(MESH_SIZE, MESH_SIZE, 1.0, -10, 10);
        final FractalField grassGround = FractalField.createSaveFractalField(MESH_SIZE, MESH_SIZE, 1.0, 0, 1);
        mesh.iterate(new Mesh.VertexVisitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                mesh.getVertexDataSafe(index).setEdge(grassGround.getValue(index));
                // TODO mesh.getVertexDataSafe(index).add(new Vertex(0, 0, heightField.getValue(index)));
            }
        });

        mesh.generateAllTriangle();
        mesh.adjustNorm();
    }

    public boolean isFree(double absoluteX, double absoluteY) {
        return true; // TODO
    }

    public Plateau getPlateau() {
        return plateau;
    }

    public VertexList getVertexList() {
        return mesh.provideVertexList();
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
}
