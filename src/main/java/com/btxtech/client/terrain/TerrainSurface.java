package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.TerrainEditorService;
import com.btxtech.client.renderer.model.Mesh;
import com.btxtech.shared.PlateauConfigEntity;
import com.btxtech.shared.VertexList;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.AfterInitialization;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 09.08.2015.
 */
@Singleton
public class TerrainSurface {
    public static final int MESH_EDGE_LENGTH = 8;
    @Inject
    private Caller<TerrainEditorService> terrainEditorService;
    private Mesh mesh = new Mesh();
    private ImageDescriptor groundImageDescriptor = ImageDescriptor.GRASS_IMAGE;
    private ImageDescriptor bottomImageDescriptor = ImageDescriptor.SAND_2;
    private ImageDescriptor slopeImageDescriptor = ImageDescriptor.ROCK_5;
    private ImageDescriptor slopePumpMapImageDescriptor = ImageDescriptor.BUMP_MAP_04;
    private double edgeDistance = 0.5;
    private double roughnessTop;
    private double roughnessHillside;
    private double roughnessGround;
    private Plateau plateau;
    private Logger logger = Logger.getLogger(TerrainSurface.class.getName());

    @PostConstruct
    public void init() {
        plateau = new Plateau(mesh);
        setup();
    }

    @AfterInitialization
    public void afterInitialization() {
        terrainEditorService.call(new RemoteCallback<PlateauConfigEntity>() {
            @Override
            public void callback(PlateauConfigEntity plateauConfigEntity) {
                plateau.setPlateauConfigEntity(plateauConfigEntity);
            }
        }, new ErrorCallback() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, TerrainSurface.class.getName() + ": Error calling terrainEditorService.read(). message:" + message, throwable);
                return false;
            }

        }).read();
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

    public void setup() {
        mesh.fill(1024, 1024, MESH_EDGE_LENGTH);

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

    public ImageDescriptor getSlopeImageDescriptor() {
        return slopeImageDescriptor;
    }

    public ImageDescriptor getBottomImageDescriptor() {
        return bottomImageDescriptor;
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

    public double getRoughnessTop() {
        return roughnessTop;
    }

    public void setRoughnessTop(double roughnessTop) {
        this.roughnessTop = roughnessTop;
    }

    public double getRoughnessHillside() {
        return roughnessHillside;
    }

    public void setRoughnessHillside(double roughnessHillside) {
        this.roughnessHillside = roughnessHillside;
    }

    public double getRoughnessGround() {
        return roughnessGround;
    }

    public void setRoughnessGround(double roughnessGround) {
        this.roughnessGround = roughnessGround;
    }

    public Plateau getPlateau() {
        return plateau;
    }
}
