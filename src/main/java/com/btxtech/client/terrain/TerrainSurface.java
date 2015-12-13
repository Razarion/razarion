package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.TerrainEditorService;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.client.renderer.model.Mesh;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.PlateauConfigEntity;
import com.btxtech.shared.TerrainMeshVertex;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Vertex;
import com.google.gwt.core.client.GWT;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.AfterInitialization;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 09.08.2015.
 */
@Singleton
public class TerrainSurface {
    public static final int MESH_EDGE_SIZE = 1024;
    public static final int MESH_NODE_EDGE_LENGTH = 8;
    @Inject
    private Caller<TerrainEditorService> terrainEditorService;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private RenderService renderService;
    private Mesh mesh = new Mesh();
    private ImageDescriptor coverImageDescriptor = ImageDescriptor.GRASS_2;
    private ImageDescriptor blenderImageDescriptor = ImageDescriptor.BLEND_3;
    private ImageDescriptor groundImageDescriptor = ImageDescriptor.GROUND_2;
    private ImageDescriptor groundBmImageDescriptor = ImageDescriptor.BUMP_MAP_GROUND_1;
    private ImageDescriptor slopeImageDescriptor = ImageDescriptor.ROCK_5;
    private ImageDescriptor slopePumpMapImageDescriptor = ImageDescriptor.BUMP_MAP_04;
    private double edgeDistance = 0.5;
    private double groundBumpMap = 2;
    private Plateau plateau;
    private Logger logger = Logger.getLogger(TerrainSurface.class.getName());
    private boolean plateauConfigRead = false;
    private boolean meshRead = false;

    @PostConstruct
    public void init() {
        plateau = new Plateau(mesh);
    }

    @AfterInitialization
    public void afterInitialization() {
        readPlateauConfig();
        readTerrain();
    }

    private void readPlateauConfig() {
        terrainEditorService.call(new RemoteCallback<PlateauConfigEntity>() {
            @Override
            public void callback(PlateauConfigEntity plateauConfigEntity) {
                plateau.setPlateauConfigEntity(plateauConfigEntity);
                plateauConfigRead = true;
                checkState();
            }
        }, new ErrorCallback() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                GWT.log("Error calling terrainEditorService.read(): " + message + " " + throwable);
                logger.log(Level.SEVERE, TerrainSurface.class.getName() + ": Error calling terrainEditorService.readTerrainMeshVertices(). message:" + message, throwable);
                return false;
            }

        }).read();
    }

    private void readTerrain() {
        terrainEditorService.call(new RemoteCallback<Collection<TerrainMeshVertex>>() {
            @Override
            public void callback(Collection<TerrainMeshVertex> terrainMeshVertexes) {
                try {
                    logger.severe("terrainMeshVertexes: " + terrainMeshVertexes.size());
                    mesh.fill(terrainMeshVertexes);
                    mesh.generateAllTriangle();
                    mesh.adjustNorm();
                    meshRead = true;
                    checkState();
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "readTerrain failed", t);
                }
            }
        }, new ErrorCallback() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                GWT.log("Error calling terrainEditorService.readTerrainMeshVertices(): " + message + " " + throwable);
                logger.log(Level.SEVERE, TerrainSurface.class.getName() + ": Error calling terrainEditorService.read(). message:" + message, throwable);
                return false;
            }

        }).readTerrainMeshVertices();
    }

    private void checkState() {
        if (plateauConfigRead && meshRead) {
            logger.severe("gameCanvas.startRenderLoop()");
            renderService.fillBuffers();
            gameCanvas.startRenderLoop();
        }
    }

    public void savePlateauConfigEntity() {
        terrainEditorService.call(new RemoteCallback<Void>() {
            @Override
            public void callback(Void ignore) {
            }
        }, new ErrorCallback() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "message: " + message, throwable);
                return false;
            }

        }).save(plateau.getPlateauConfigEntity());
    }


    public void saveTerrain() {
        terrainEditorService.call(new RemoteCallback<Void>() {
            @Override
            public void callback(Void ignore) {
            }
        }, new ErrorCallback() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "message: " + message, throwable);
                return false;
            }

        }).saveTerrainMeshVertices(mesh.createTerrainMeshVertices());
    }

    public void sculpt() {
        mesh.reset(MESH_NODE_EDGE_LENGTH, MESH_EDGE_SIZE, MESH_EDGE_SIZE, 0);

        final FractalField fractalField = FractalField.createSaveFractalField(MESH_EDGE_SIZE, MESH_EDGE_SIZE, 2.0, -0.5, 0.9);
        mesh.iterate(new Mesh.VertexVisitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                double value = fractalField.get(index);
                mesh.getVertexDataSafe(index).setEdge(value);
            }

        });


        plateau.sculpt();
        mesh.generateAllTriangle();
        mesh.adjustNorm();

    }

    public VertexList getVertexList() {
        return mesh.provideVertexList(groundImageDescriptor);
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

    public Plateau getPlateau() {
        return plateau;
    }
}
