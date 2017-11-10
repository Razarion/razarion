package com.btxtech.shared.gameengine.planet.gui;

import com.btxtech.shared.datatypes.DecimalPosition;
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
    private TerrainService terrainService;
    @FXML
    private TextField zMinField;
    @FXML
    private TextField zMaxField;
    @FXML
    private CheckBox terrainSplattingCheck;
    @FXML
    private CheckBox shapeAccessCheck;
    private Object[] userObjects;

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
    }

    public void setUserObjects(Object[] userObjects) {
        this.userObjects = userObjects;
    }

    protected void onMousePressedTerrain(DecimalPosition position) {
        System.out.println("-----------------------------------------------");
        System.out.println("position: " + position);
        TerrainShapeNode terrainShapeNode = terrainService.getPathingAccess().getTerrainShapeNode(TerrainUtil.toNode(position));
        System.out.println("Interpolated Z: " + terrainService.getSurfaceAccess().getInterpolatedZ(position));
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

    public boolean renderTerrainSplattings() {
        return terrainSplattingCheck.isSelected();
    }

    public boolean renderShapeAccess() {
        return shapeAccessCheck.isSelected();
    }
}
