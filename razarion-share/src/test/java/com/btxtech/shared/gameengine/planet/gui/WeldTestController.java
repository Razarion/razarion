package com.btxtech.shared.gameengine.planet.gui;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.gui.userobject.MouseMoveCallback;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeNode;
import com.btxtech.shared.gameengine.planet.terrain.gui.AbstractTerrainTestController;
import com.btxtech.shared.gameengine.planet.terrain.gui.AbstractTerrainTestRenderer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by Beat
 * 09.04.2017.
 */
@Singleton
public class WeldTestController extends AbstractTerrainTestController {
    @Inject
    private WeldTestRenderer weldTestRenderer;
    @Inject
    private PlanetService planetService;
    @Inject
    private TerrainService terrainService;
    @FXML
    private TextField zMinField;
    @FXML
    private TextField zMaxField;
    @FXML
    private CheckBox terrainTileSplattingCheck;
    @FXML
    private CheckBox terrainTileWaterCheck;
    @FXML
    private CheckBox terrainTileGroundCheck;
    @FXML
    private CheckBox terrainTileSlopeCheck;
    @FXML
    private CheckBox terrainTileHeightCheck;
    @FXML
    private CheckBox terrainTileTerrainObjectCheck;
    @FXML
    private CheckBox terrainTileTerrainTypeCheck;
    @FXML
    private CheckBox shapeAccessCheck;
    @FXML
    private CheckBox shapeTerrainTypeCheck;
    @FXML
    private CheckBox shapeTerrainHeightCheck;
    @FXML
    private CheckBox shapeFractionalSlopeCheck;
    @FXML
    private CheckBox shapeObstaclesCheck;
    @FXML
    private CheckBox groundSlopeConnectionsCheck;
    @FXML
    private CheckBox shapeWaterCheck;
    @FXML
    private CheckBox shapeTerrainObjectCheck;
    private Object[] userObjects;
    private MouseMoveCallback mouseMoveCallback;

    @Override
    protected AbstractTerrainTestRenderer setupRenderer() {
        weldTestRenderer.setup(this, userObjects);
        return weldTestRenderer;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        zMinField.setText(Double.toString(weldTestRenderer.getZMin()));
        zMaxField.setText(Double.toString(weldTestRenderer.getZMax()));

        addRenderListener(terrainTileSplattingCheck);
        addRenderListener(terrainTileWaterCheck);
        addRenderListener(terrainTileGroundCheck);
        addRenderListener(terrainTileSlopeCheck);
        addRenderListener(terrainTileHeightCheck);
        addRenderListener(terrainTileTerrainObjectCheck);
        addRenderListener(terrainTileTerrainTypeCheck);
        addRenderListener(shapeAccessCheck);
        addRenderListener(shapeTerrainTypeCheck);
        addRenderListener(shapeTerrainHeightCheck);
        addRenderListener(shapeFractionalSlopeCheck);
        addRenderListener(shapeObstaclesCheck);
        addRenderListener(groundSlopeConnectionsCheck);
        addRenderListener(shapeWaterCheck);
        addRenderListener(shapeTerrainObjectCheck);
    }

    private void addRenderListener(CheckBox checkBox) {
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> getAbstractTerrainTestRenderer().render());
    }

    public void setUserObjects(Object[] userObjects) {
        List<Object> userObjectsCopy = new ArrayList<>();
        for (Object userObject : userObjects) {
            if (userObject instanceof MouseMoveCallback) {
                mouseMoveCallback = (MouseMoveCallback) userObject;
            } else {
                userObjectsCopy.add(userObject);
            }
        }
        this.userObjects = userObjectsCopy.toArray();
    }

    @Override
    public void onMouseMoved(DecimalPosition position) {
        if (mouseMoveCallback != null) {
            Object[] userObject = mouseMoveCallback.onMouseMove(position);
            if (userObject != null) {
                ((WeldTestRenderer) getAbstractTerrainTestRenderer()).setMoveUserDataRenderer(userObject);
                getAbstractTerrainTestRenderer().render();
                ((WeldTestRenderer) getAbstractTerrainTestRenderer()).setMoveUserDataRenderer(null);
            }
        }
    }

    protected void onMousePressedTerrain(DecimalPosition position) {
        System.out.println("-----------------------------------------------");
        System.out.println("position: " + position);
        TerrainShapeNode terrainShapeNode = terrainService.getPathingAccess().getTerrainShapeNode(TerrainUtil.toNode(position));
        System.out.println("SurfaceAccess: interpolated Z: " + terrainService.getSurfaceAccess().getInterpolatedZ(position));
        System.out.println("Interpolated norm: " + terrainService.getSurfaceAccess().getInterpolatedNorm(position));
        if (terrainShapeNode == null) {
            System.out.println("No terrain shape node at: " + position);
            return;
        }
        System.out.println("RenderEngineHeight: " + terrainShapeNode.getRenderEngineHeight());
        System.out.println("GameEngineHeight: " + terrainShapeNode.getGameEngineHeight());
//        if (terrainShapeNode.isFullDriveway()) {
//            System.out.println("getDrivewayHeightBL: " + terrainShapeNode.getDrivewayHeightBL());
//            System.out.println("getDrivewayHeightBR: " + terrainShapeNode.getDrivewayHeightBR());
//            System.out.println("getDrivewayHeightTR: " + terrainShapeNode.getDrivewayHeightTR());
//            System.out.println("getDrivewayHeightTL: " + terrainShapeNode.getDrivewayHeightTL());
//        }

    }

    public void onMinZChanged(ActionEvent inputMethodEvent) {
        weldTestRenderer.setZMin(Double.parseDouble(zMinField.getText()));
        getAbstractTerrainTestRenderer().render();
    }

    public void onMaxZChanged(ActionEvent inputMethodEvent) {
        weldTestRenderer.setZMax(Double.parseDouble(zMaxField.getText()));
        getAbstractTerrainTestRenderer().render();
    }

    public boolean renderTerrainTileSplattings() {
        return terrainTileSplattingCheck.isSelected();
    }

    public boolean renderTerrainTileWater() {
        return terrainTileWaterCheck.isSelected();
    }

    public boolean renderTerrainTileGround() {
        return terrainTileGroundCheck.isSelected();
    }

    public boolean renderTerrainTileSlope() {
        return terrainTileSlopeCheck.isSelected();
    }

    public boolean renderTerrainTileHeight() {
        return terrainTileHeightCheck.isSelected();
    }

    public boolean renderTerrainTileTerrainObject() {
        return terrainTileTerrainObjectCheck.isSelected();
    }

    public boolean renderTerrainTileTerrainType() {
        return terrainTileTerrainTypeCheck.isSelected();
    }

    public boolean renderShapeAccess() {
        return shapeAccessCheck.isSelected();
    }

    public boolean renderShapeTerrainType() {
        return shapeTerrainTypeCheck.isSelected();
    }

    public boolean renderShapeTerrainHeight() {
        return shapeTerrainHeightCheck.isSelected();
    }

    public boolean renderShapeFractionalSlope() {
        return shapeFractionalSlopeCheck.isSelected();
    }

    public boolean renderShapeObstacles() {
        return shapeObstaclesCheck.isSelected();
    }

    public boolean renderGroundSlopeConnections() {
        return groundSlopeConnectionsCheck.isSelected();
    }

    public boolean renderShapeWater() {
        return shapeWaterCheck.isSelected();
    }

    public boolean renderShapeTerrainObject() {
        return shapeTerrainObjectCheck.isSelected();
    }

    public void onTickButton(ActionEvent actionEvent) {
        planetService.run();
        getAbstractTerrainTestRenderer().render();
    }
}
