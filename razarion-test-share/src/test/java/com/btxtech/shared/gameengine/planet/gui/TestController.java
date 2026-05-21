package com.btxtech.shared.gameengine.planet.gui;

import com.btxtech.shared.TestShareDagger;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.gui.userobject.InstanceStringGenerator;
import com.btxtech.shared.gameengine.planet.gui.userobject.MouseMoveCallback;
import com.btxtech.shared.gameengine.planet.gui.userobject.TestCaseGenerator;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static com.btxtech.shared.gameengine.planet.terrain.TerrainUtil.toNode;

/**
 * Controller for the JavaFX test display (formerly WeldTestController).
 * Drives the canvas, handles pan/zoom, and runs the planet service in
 * single-tick or auto-tick mode for visual debugging.
 */
public class TestController implements Initializable {
    private final DaggerTestRenderer renderer;
    private final PlanetService planetService;
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
    private CheckBox shapeTerrainTypeCheck;
    @FXML
    private CheckBox shapeTerrainHeightCheck;
    @FXML
    private CheckBox shapeTerrainObjectCheck;
    @FXML
    private CheckBox syncItemsCheck;
    @FXML
    private CheckBox trailCheck;
    @FXML
    private CheckBox aStarClosedListCheck;
    @FXML
    private ToggleButton autoTickToggle;
    @FXML
    private Slider autoTickSpeedSlider;
    @FXML
    private Label tickCountLabel;
    private DecimalPosition mousePosition;
    private Object[] userObjects;
    private MouseMoveCallback mouseMoveCallback;
    private TestCaseGenerator testCaseGenerator;
    private int tickCount;
    private List<DecimalPosition> polygon;
    private List<DecimalPosition> positions;
    private AnimationTimer autoTickTimer;
    private long lastAutoTickNanos;

    public TestController(Object[] userObjects, TestShareDagger testShareDagger) {
        renderer = testShareDagger.daggerTestRenderer();
        renderer.setup(this, userObjects);
        setUserObjects(userObjects);
        planetService = testShareDagger.planetService();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        renderer.setup(this, userObjects);
        renderer.init(canvas, 4.0);
        anchorPanel.widthProperty().addListener((obs, oldWidth, width) -> {
            canvas.setWidth(width.doubleValue());
            renderer.render();
        });
        anchorPanel.heightProperty().addListener((obs, oldHeight, height) -> {
            canvas.setHeight(height.doubleValue());
            renderer.render();
        });
        scaleField.setText(String.format("%.2f", renderer.getScale()));
        zoomSlider.setValue(renderer.getZoom());
        zoomSlider.valueProperty().addListener((obs, oldValue, newValue) -> setZoom(zoomSlider.getValue()));

        zMinField.setText(Double.toString(renderer.getZMin()));
        zMaxField.setText(Double.toString(renderer.getZMax()));

        shapeTerrainTypeCheck.setSelected(true);
        shapeTerrainObjectCheck.setSelected(true);
        syncItemsCheck.setSelected(true);
        trailCheck.setSelected(true);

        addRenderListener(shapeTerrainTypeCheck);
        addRenderListener(shapeTerrainHeightCheck);
        addRenderListener(shapeTerrainObjectCheck);
        addRenderListener(syncItemsCheck);
        addRenderListener(trailCheck);
        addRenderListener(aStarClosedListCheck);

        autoTickToggle.selectedProperty().addListener((obs, oldVal, selected) -> {
            if (selected) {
                startAutoTick();
            } else {
                stopAutoTick();
            }
        });
    }

    public void onZoomResetButton() {
        setZoom(1);
    }

    private void setZoom(double zoom) {
        renderer.setZoom(zoom);
        scaleField.setText(String.format("%.2f", renderer.getScale()));
        renderer.render();
    }

    public void onScroll(ScrollEvent scrollEvent) {
        if (scrollEvent.getDeltaY() > 0) {
            zoomSlider.setValue(zoomSlider.getValue() + 1);
        } else {
            zoomSlider.setValue(zoomSlider.getValue() - 1);
        }
    }

    public void onMouseDragged(Event event) {
        if (renderer.shifting(event)) {
            renderer.render();
        }
    }

    public void onMouseReleased() {
        renderer.stopShift();
    }

    public void onMouseMoved(Event event) {
        DecimalPosition position = renderer.convertMouseToModel(event);
        mouseLabel.setText(String.format("%.2f:%.2f", position.getX(), position.getY()));
        mousePosition = position;
        if (mouseMoveCallback != null) {
            Object[] userObject = mouseMoveCallback.onMouseMove(position);
            if (userObject != null) {
                renderer.setMoveUserDataRenderer(userObject);
                renderer.render();
                renderer.setMoveUserDataRenderer(null);
            }
        }
    }

    public void onMousePressed(MouseEvent event) {
        DecimalPosition position = renderer.convertMouseToModel(event);
        if (polygon != null) {
            polygon.add(position);
        }
        if (positions != null) {
            positions.add(position);
        }
        renderer.render();
        System.out.println("position: " + position + " node: " + toNode(position));
    }

    public DecimalPosition getMousePosition() {
        return mousePosition;
    }

    private void addRenderListener(CheckBox checkBox) {
        checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> renderer.render());
    }

    public void setUserObjects(Object[] userObjects) {
        List<Object> userObjectsCopy = new ArrayList<>();
        for (Object userObject : userObjects) {
            if (userObject instanceof MouseMoveCallback) {
                mouseMoveCallback = (MouseMoveCallback) userObject;
            } else if (userObject instanceof TestCaseGenerator) {
                testCaseGenerator = (TestCaseGenerator) userObject;
            } else {
                userObjectsCopy.add(userObject);
            }
        }
        this.userObjects = userObjectsCopy.toArray();
    }

    public void onMinZChanged() {
        renderer.setZMin(Double.parseDouble(zMinField.getText()));
        renderer.render();
    }

    public void onMaxZChanged() {
        renderer.setZMax(Double.parseDouble(zMaxField.getText()));
        renderer.render();
    }

    public boolean renderShapeTerrainType() {
        return shapeTerrainTypeCheck.isSelected();
    }

    public boolean renderShapeTerrainHeight() {
        return shapeTerrainHeightCheck.isSelected();
    }

    public boolean renderShapeTerrainObject() {
        return shapeTerrainObjectCheck.isSelected();
    }

    public boolean renderSyncItems() {
        return syncItemsCheck.isSelected();
    }

    public boolean renderTrail() {
        return trailCheck.isSelected();
    }

    public boolean renderAStarClosedList() {
        return aStarClosedListCheck.isSelected();
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
        runOneTick();
    }

    private void runOneTick() {
        planetService.run();
        renderer.recordTrail();
        tickCount++;
        tickCountLabel.setText(Integer.toString(tickCount));
        renderer.render();
    }

    private void startAutoTick() {
        if (autoTickTimer != null) {
            return;
        }
        lastAutoTickNanos = 0;
        autoTickTimer = new AnimationTimer() {
            @Override
            public void handle(long nowNanos) {
                long intervalNanos = (long) (1_000_000_000.0 / Math.max(1, autoTickSpeedSlider.getValue()));
                if (nowNanos - lastAutoTickNanos < intervalNanos) {
                    return;
                }
                lastAutoTickNanos = nowNanos;
                runOneTick();
            }
        };
        autoTickTimer.start();
    }

    private void stopAutoTick() {
        if (autoTickTimer != null) {
            autoTickTimer.stop();
            autoTickTimer = null;
        }
    }

    public void onClearTrailButton() {
        renderer.clearTrail();
        renderer.render();
    }

    public void onTestGenerationButton() {
        if (testCaseGenerator != null) {
            if (getMousePosition() == null) {
                throw new IllegalStateException("No mouse position available. Move the mouse before pressing the test case generation button.");
            }
            testCaseGenerator.onTestGenerationButton(getMousePosition());
        }
    }

    public void onPolygonDumpCheck(ActionEvent actionEvent) {
        polygon = ((CheckBox) actionEvent.getSource()).isSelected() ? new ArrayList<>() : null;
    }

    public void onPolygonDumpButton() {
        if (polygon != null) {
            System.out.println("-------------------- Polygon --------------------");
            System.out.println(InstanceStringGenerator.generateSimpleDecimalPositionList(polygon));
        }
    }

    public void onPositionsDumpCheck(ActionEvent actionEvent) {
        positions = ((CheckBox) actionEvent.getSource()).isSelected() ? new ArrayList<>() : null;
        renderer.render();
    }

    public void onPositionsDumpButton() {
        if (positions != null) {
            System.out.println("-------------------- Positions --------------------");
            System.out.println(InstanceStringGenerator.generateSimpleDecimalPositionList(positions));
        }
        renderer.render();
    }
}
