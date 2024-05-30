package com.btxtech.shared.gameengine.planet.gui;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.gui.scenarioplayback.ScenarioPlaybackController;
import com.btxtech.shared.gameengine.planet.gui.userobject.InstanceStringGenerator;
import com.btxtech.shared.gameengine.planet.gui.userobject.MouseMoveCallback;
import com.btxtech.shared.gameengine.planet.gui.userobject.ScenarioPlayback;
import com.btxtech.shared.gameengine.planet.gui.userobject.TestCaseGenerator;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeNode;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static com.btxtech.shared.gameengine.planet.terrain.TerrainUtil.toNode;

/**
 * Created by Beat
 * 09.04.2017.
 */
@Singleton
public class WeldTestController implements Initializable {
    @Inject
    private WeldTestRenderer weldTestRenderer;
    @Inject
    private PlanetService planetService;
    @Inject
    private TerrainService terrainService;
    @Inject
    private Instance<ScenarioPlaybackController> instance;
    @FXML
    private AnchorPane anchorPanel;
    @FXML
    private Canvas canvas;
    @FXML
    private Slider zoomSlider;
    @FXML
    private TextField scaleField;
    @FXML
    private TextField mouseLabel;
    @FXML
    private TextField zMinField;
    @FXML
    private TextField zMaxField;
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
    @FXML
    private CheckBox syncItemsCheck;
    @FXML
    private AnchorPane gameEnginePlaybackContainer;
    @FXML
    private Label tickCountLabel;
    private DecimalPosition mousePosition;
    private Object[] userObjects;
    private MouseMoveCallback mouseMoveCallback;
    private TestCaseGenerator testCaseGenerator;
    private ScenarioPlaybackController scenarioPlaybackController;
    private int tickCount;
    private List<DecimalPosition> polygon;
    private List<DecimalPosition> positions;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        weldTestRenderer.setup(this, userObjects);
        weldTestRenderer.init(canvas, 1.0);
        anchorPanel.widthProperty().addListener((observableValue, oldSceneWidth, width) -> {
            canvas.setWidth(width.doubleValue());
            weldTestRenderer.render(scenarioPlaybackController);
        });
        anchorPanel.heightProperty().addListener((observableValue, oldSceneWidth, height) -> {
            canvas.setHeight(height.doubleValue());
            weldTestRenderer.render(scenarioPlaybackController);
        });
        scaleField.setText(String.format("%.2f", weldTestRenderer.getScale()));
        zoomSlider.setValue(weldTestRenderer.getZoom());
        zoomSlider.valueProperty().addListener((observableValue, number, t1) -> setZoom(zoomSlider.getValue()));

        zMinField.setText(Double.toString(weldTestRenderer.getZMin()));
        zMaxField.setText(Double.toString(weldTestRenderer.getZMax()));

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
        addRenderListener(syncItemsCheck);

        setupGameEnginePlayback();
    }

    public void onZoomResetButton() {
        setZoom(1);
    }

    private void setZoom(double zoom) {
        weldTestRenderer.setZoom(zoom);
        scaleField.setText(String.format("%.2f", weldTestRenderer.getScale()));
        weldTestRenderer.render(scenarioPlaybackController);
    }

    public void onScroll(ScrollEvent scrollEvent) {
        if (scrollEvent.getDeltaY() > 0) {
            zoomSlider.setValue(zoomSlider.getValue() + 1);
        } else {
            zoomSlider.setValue(zoomSlider.getValue() - 1);
        }
    }

    public void onMouseDragged(Event event) {
        if (weldTestRenderer.shifting(event)) {
            weldTestRenderer.render(scenarioPlaybackController);
        }
    }

    public void onMouseReleased() {
        weldTestRenderer.stopShift();
    }

    public void onMouseMoved(Event event) {
        DecimalPosition position = weldTestRenderer.convertMouseToModel(event);
        mouseLabel.setText(String.format("%.2f:%.2f", position.getX(), position.getY()));
        mousePosition = position;
        if (mouseMoveCallback != null) {
            Object[] userObject = mouseMoveCallback.onMouseMove(position);
            if (userObject != null) {
                weldTestRenderer.setMoveUserDataRenderer(userObject);
                weldTestRenderer.render(scenarioPlaybackController);
                weldTestRenderer.setMoveUserDataRenderer(null);
            }
        }
    }

    public void onMousePressed(MouseEvent event) {
        DecimalPosition position = weldTestRenderer.convertMouseToModel(event);
        if (polygon != null) {
            polygon.add(position);
        }
        if (positions != null) {
            positions.add(position);
        }
        weldTestRenderer.render(scenarioPlaybackController);
        onMousePressedTerrain(position);
    }

    public DecimalPosition getMousePosition() {
        return mousePosition;
    }

    private void addRenderListener(CheckBox checkBox) {
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> weldTestRenderer.render(scenarioPlaybackController));
    }

    public void setUserObjects(Object[] userObjects) {
        List<Object> userObjectsCopy = new ArrayList<>();
        for (Object userObject : userObjects) {
            if (userObject instanceof MouseMoveCallback) {
                mouseMoveCallback = (MouseMoveCallback) userObject;
            } else if (userObject instanceof TestCaseGenerator) {
                testCaseGenerator = (TestCaseGenerator) userObject;
            } else if (userObject instanceof ScenarioPlayback) {
                scenarioPlaybackController = instance.get();
                scenarioPlaybackController.setScenarioPlayback((ScenarioPlayback) userObject, () -> weldTestRenderer.render(scenarioPlaybackController));
            } else {
                userObjectsCopy.add(userObject);
            }
        }
        this.userObjects = userObjectsCopy.toArray();
    }

    protected void onMousePressedTerrain(DecimalPosition position) {
        System.out.println("-----------------------------------------------");
        System.out.println("position: " + position);
        System.out.println("Node index: " + toNode(position));
        System.out.println("SurfaceAccess: interpolated Z: " + terrainService.getSurfaceAccess().getInterpolatedZ(position));
        System.out.println("Interpolated norm: " + terrainService.getSurfaceAccess().getInterpolatedNorm(position));
//        System.out.println("RenderEngineHeight: " + terrainShapeNode.getRenderEngineHeight());
//        if (terrainShapeNode.isFullDriveway()) {
//            System.out.println("getDrivewayHeightBL: " + terrainShapeNode.getDrivewayHeightBL());
//            System.out.println("getDrivewayHeightBR: " + terrainShapeNode.getDrivewayHeightBR());
//            System.out.println("getDrivewayHeightTR: " + terrainShapeNode.getDrivewayHeightTR());
//            System.out.println("getDrivewayHeightTL: " + terrainShapeNode.getDrivewayHeightTL());
//        }

    }

    public void onMinZChanged() {
        weldTestRenderer.setZMin(Double.parseDouble(zMinField.getText()));
        weldTestRenderer.render(scenarioPlaybackController);
    }

    public void onMaxZChanged() {
        weldTestRenderer.setZMax(Double.parseDouble(zMaxField.getText()));
        weldTestRenderer.render(scenarioPlaybackController);
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

    public boolean renderSyncItems() {
        return syncItemsCheck.isSelected();
    }

    public boolean renderPolygon() {
        return polygon != null && polygon.size() >= 3;
    }

    public boolean renderPositions() {
        return positions != null && !positions.isEmpty();
    }

    public List<DecimalPosition> getPolygon() {
        return polygon;
    }

    public List<DecimalPosition> getPositions() {
        return positions;
    }

    public void onTickButton() {
        planetService.run();
        tickCount++;
        tickCountLabel.setText(Integer.toString(tickCount));
        weldTestRenderer.render(scenarioPlaybackController);
    }

    public void onTestGenerationButton() {
        if (testCaseGenerator != null) {
            if (getMousePosition() == null) {
                throw new IllegalStateException("No mouse position available. Move the mouse before press the test case generation button.");
            }
            testCaseGenerator.onTestGenerationButton(getMousePosition());
        }
    }

    private void setupGameEnginePlayback() {
        gameEnginePlaybackContainer.getChildren().clear();
        if (scenarioPlaybackController == null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ScenarioPlayback.fxml"));
            loader.setControllerFactory(param -> scenarioPlaybackController);
            gameEnginePlaybackContainer.getChildren().add(loader.load());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void onPolygonDumpCheck(ActionEvent actionEvent) {
        if (((CheckBox) actionEvent.getSource()).isSelected()) {
            polygon = new ArrayList<>();
        } else {
            polygon = null;
        }
    }

    public void onPolygonDumpButton() {
        if (polygon != null) {
            System.out.println("-------------------- Slope --------------------------------");
            System.out.println(InstanceStringGenerator.generateSlope(polygon));
        }

    }

    public void onPositionsDumpCheck(ActionEvent actionEvent) {
        if (((CheckBox) actionEvent.getSource()).isSelected()) {
            positions = new ArrayList<>();
        } else {
            positions = null;
        }
        weldTestRenderer.render(scenarioPlaybackController);
    }

    public void onPositionsDumpButton() {
        if (positions != null) {
            System.out.println("-------------------- Positions --------------------------------");
            System.out.println(InstanceStringGenerator.generateSimpleDecimalPositionList(positions));
        }
        weldTestRenderer.render(scenarioPlaybackController);
    }
}
