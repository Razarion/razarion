package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Ray3d;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.GameUiControlConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.VertexList;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.Water;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.utils.MathHelper;
import com.btxtech.uiservice.control.GameUiControlInitEvent;
import com.btxtech.uiservice.renderer.RenderServiceInitEvent;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 09.08.2015.
 */
@Singleton
public class TerrainUiService {
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private ExceptionHandler exceptionHandler;
    private TerrainService terrainService;
    private static final double HIGHEST_POINT_IN_VIEW = 20;
    private static final double LOWEST_POINT_IN_VIEW = -2;
    private double highestPointInView; // Should be calculated
    private double lowestPointInView; // Should be calculated
    private MapCollection<TerrainObjectConfig, ModelMatrices> terrainObjectConfigModelMatrices;

    public TerrainUiService() {
        highestPointInView = HIGHEST_POINT_IN_VIEW;
        lowestPointInView = LOWEST_POINT_IN_VIEW;
        terrainService = new TerrainService();
    }

    public void onGameUiControlInitEvent(@Observes GameUiControlInitEvent gameUiControlInitEvent) {
        init(gameUiControlInitEvent.getGameUiControlConfig());
    }

    public void init(GameUiControlConfig gameUiControlConfig) {
        terrainService.init(gameUiControlConfig.getGameEngineConfig().getPlanetConfig(), terrainTypeService);
    }

    public void onRenderServiceInitEvent(@Observes RenderServiceInitEvent renderServiceInitEvent) {
        terrainObjectConfigModelMatrices = new MapCollection<>();
        for (Map.Entry<TerrainObjectConfig, Collection<TerrainObjectPosition>> entry : terrainService.getTerrainObjectPositions().getMap().entrySet()) {
            for (TerrainObjectPosition objectPosition : entry.getValue()) {
                try {
                    int z = (int) terrainService.getInterpolatedTerrainTriangle(new DecimalPosition(objectPosition.getPosition())).getHeight();
                    Matrix4 model = objectPosition.createModelMatrix(z);
                    terrainObjectConfigModelMatrices.put(entry.getKey(), new ModelMatrices(model));
                } catch (Throwable t) {
                    exceptionHandler.handleException("Placing terrain object failed", t);
                }
            }
        }
    }

    public double getHighestPointInView() {
        return highestPointInView;
    }

    public double getLowestPointInView() {
        return lowestPointInView;
    }

    public VertexList getGroundVertexList() {
        VertexList vertexList;
        if (terrainService.getGroundMesh() != null) {
            vertexList = terrainService.getGroundMesh().provideVertexList();
        } else {
            vertexList = new VertexList();
        }
        for (Slope slope : terrainService.getSlopes()) {
            if (!slope.hasWater()) {
                vertexList.append(slope.getGroundPlateauConnector().getTopMesh().provideVertexList());
                vertexList.append(slope.getGroundPlateauConnector().getInnerConnectionVertexList());
            }
            vertexList.append(slope.getGroundPlateauConnector().getOuterConnectionVertexList());
        }
        return vertexList;
    }

    public Water getWater() {
        return terrainService.getWater();
    }

    public double getWaterAnimation() {
        return getWaterAnimation(System.currentTimeMillis(), 2000, 0);
    }

    public double getWaterAnimation2() {
        return getWaterAnimation(System.currentTimeMillis(), 2000, 500);
    }

    private double getWaterAnimation(long millis, int durationMs, int offsetMs) {
        return Math.sin(((millis % durationMs) / (double) durationMs + ((double) offsetMs / (double) durationMs)) * MathHelper.ONE_RADIANT);
    }

    public List<ModelMatrices> provideTerrainObjectModelMatrices(TerrainObjectConfig terrainObjectConfig) {
        Collection<ModelMatrices> modelMatrices = terrainObjectConfigModelMatrices.get(terrainObjectConfig);
        if (modelMatrices != null) {
            return new ArrayList<>(modelMatrices);
        } else {
            return Collections.emptyList();
        }
    }

    public double calculateLandWaterProportion(Rectangle2D viewField) {
        if (getWater().isValid()) {
            return 1.0 - getWater().calculateAabb().coverRatio(viewField);
        } else {
            return 0;
        }
    }

    public Collection<Slope> getSlopes() {
        return terrainService.getSlopes();
    }

    public MapCollection<TerrainObjectConfig, TerrainObjectPosition> getTerrainObjectPositions() {
        return terrainService.getTerrainObjectPositions();
    }

    public boolean overlap(DecimalPosition position) {
        return terrainService.overlap(position);
    }

    public boolean overlap(Collection<DecimalPosition> positions, BaseItemType baseItemType) {
        return terrainService.overlap(positions, baseItemType);
    }

    public Vertex getPosition3d(DecimalPosition absoluteXY) {
        return new Vertex(absoluteXY, terrainService.getInterpolatedTerrainTriangle(absoluteXY).getHeight());
    }

    public Vertex calculatePositionGroundMesh(Ray3d worldPickRay) {
        DecimalPosition zeroLevel = terrainService.calculatePositionOnZeroLevel(worldPickRay).toXY();
        double height = terrainService.getInterpolatedTerrainTriangle(zeroLevel).getHeight();
        return new Vertex(zeroLevel, height);
    }

}
