package com.btxtech.gameengine;

import com.btxtech.gameengine.scenarios.ScenarioService;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.shared.system.perfmon.StatisticEntry;
import com.btxtech.webglemulator.razarion.DevToolFutureControl;
import com.btxtech.webglemulator.razarion.DevToolsSimpleExecutorServiceImpl;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

/**
 * Created by Beat
 * 17.05.2016.
 */
@Singleton
public class GameEngineMonitorController implements Initializable {
    @FXML
    private Canvas canvas;
    @FXML
    private Canvas overlayCanvas;
    @FXML
    private TextField stepField;
    @FXML
    private TextField scenarioField;
    @FXML
    private TextField mouseField;
    @FXML
    private Pane leftSidePanel;
    @FXML
    private Slider zoomSlider;
    @FXML
    private TextField scaleField;
    @FXML
    private AnchorPane anchorPanel;
    @Inject
    private JavaFxGameEngineRenderer renderer;
    @Inject
    private OverlayGameEngineRenderer overlayRenderer;
    @Inject
    private ScenarioService scenarioService;
    @Inject
    private DevToolsSimpleExecutorServiceImpl devToolsSimpleExecutorService;
    @Inject
    private PlanetService planetService;
    @Inject
    private PerfmonService perfmonService;
    private int delay = PlanetService.TICK_TIME_MILLI_SECONDS;
    // private List<Collection<Unit>> backups = new ArrayList<>();
    private SyncItemSidePaneController hoverSyncItemSidePaneController;
    private boolean selected;
    private DevToolFutureControl gameEngineFutureControl;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        zoomSlider.valueProperty().addListener((observableValue, number, t1) -> {
            setZoom(zoomSlider.getValue());
        });

        anchorPanel.widthProperty().addListener((observableValue, oldSceneWidth, width) -> {
            overlayCanvas.setWidth(width.doubleValue());
            canvas.setWidth(width.doubleValue());
            renderer.render();
        });

        anchorPanel.heightProperty().addListener((observableValue, oldSceneWidth, height) -> {
            overlayCanvas.setHeight(height.doubleValue());
            canvas.setHeight(height.doubleValue());
            renderer.render();
        });

        gameEngineFutureControl = devToolsSimpleExecutorService.createDevToolFutureControl(SimpleExecutorService.Type.GAME_ENGINE);
        gameEngineFutureControl.setAfterExecutionCallback(this::tick);

        onRestartScenario();
        renderer.init(canvas, 4.0);
        overlayRenderer.init(overlayCanvas, 4.0);
        renderer.render();

        onRun();
    }

    public void onRun() {
        gameEngineFutureControl.start();
    }

    public void onTurbo() {
        if (delay < PlanetService.TICK_TIME_MILLI_SECONDS) {
            delay = PlanetService.TICK_TIME_MILLI_SECONDS;
        } else {
            delay = 1;
        }
        gameEngineFutureControl.modifyDelay(delay);
    }

    public void onHold() {
        gameEngineFutureControl.cancel();
    }

    public void onStep() {
        gameEngineFutureControl.singleEexecute();
    }

    public void onStepBackward() {
//        onHold();
//        if (backups.isEmpty()) {
//            return;
//        }
//        pathingService.restore(backups.remove(backups.size() - 1), pathingService.getTickCount() - 1);
//        renderer.render(pathingService);
//        stepField.setText(Long.toString(pathingService.getTickCount()));
    }

    public void onNextScenario() {
        scenarioService.startNextScenario();
        scenarioField.setText(scenarioService.getCurrentName());
        // backups.clear();
        onRun();
    }

    public void onRestartScenario() {
        scenarioService.restartCurrentScenario();
        scenarioField.setText(scenarioService.getCurrentName());
        // backups.clear();
        onRun();
    }

    public void onPrevScenario() {
        scenarioService.startPreviousScenario();
        scenarioField.setText(scenarioService.getCurrentName());
        // backups.clear();
        onRun();
    }

    public void onMouseMove(Event event) {
        // Display mouse position
        DecimalPosition position = renderer.convertMouseToModel(event);
        mouseField.setText(String.format("%.2f:%.2f", position.getX(), position.getY()));
        // Show left side menu
//        if (!selected) {
//            Unit unit = pathingService.getUnit(position);
//            if (unit != null) {
//                if (hoverSyncItemSidePaneController == null || !hoverSyncItemSidePaneController.isSame(unit)) {
//                    try {
//                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gameengine/SyncItemSidePane.fxml"));
//                        FlowPane unitSideBar = (FlowPane) loader.load();
//                        hoverSyncItemSidePaneController = loader.getController();
//                        hoverSyncItemSidePaneController.init(unit);
//                        leftSidePanel.getChildren().setAll(unitSideBar);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            } else {
//                if (hoverSyncItemSidePaneController != null) {
//                    leftSidePanel.getChildren().clear();
//                    hoverSyncItemSidePaneController = null;
//                }
//            }
//
//        }
    }

    public void onCreateTestCase() {
//        System.out.println("    @Test");
//        System.out.println("    public void testCase() throws Exception {");
//        System.out.println("        PathingService pathingService = new PathingService();");
//        for (Unit unit : pathingService.getUnits()) {
//            String unitParams = unit.getId() + ", " + unit.isCanMove() + ", " + unit.getRadius() + ", "
//                    + InstanceStringGenerator.generate(unit.getPosition3d()) + ", " + InstanceStringGenerator.generate(unit.getVelocity()) + ", "
//                    + InstanceStringGenerator.generate(unit.getDestination()) + ", " + InstanceStringGenerator.generate(unit.getDestination());
//            System.out.println("        pathingService.createUnit(" + unitParams + ");");
//        }
//        for (Obstacle obstacle : pathingService.getObstacles()) {
//            System.out.println("        pathingService.createObstacle(" + InstanceStringGenerator.generate(obstacle.getLine()) + ");");
//        }
//        System.out.println("        pathingService.tick(PathingService.FACTOR);");
//
//
//        System.out.println("    }");
    }

    public void onScroll(ScrollEvent scrollEvent) {
        if (scrollEvent.getDeltaY() > 0) {
            zoomSlider.setValue(zoomSlider.getValue() + 1);
        } else {
            zoomSlider.setValue(zoomSlider.getValue() - 1);
        }
    }

    private void setZoom(double zoom) {
        renderer.setZoom(zoom);
        overlayRenderer.setZoom(zoom);
        scaleField.setText(String.format("%.2f", renderer.getScale()));
        renderer.render();
    }

    public void onZoomResetButton() {
        setZoom(1);
    }

    private void tick() {
        backup();
        Platform.runLater(() -> {
            try {
                renderer.render();
                stepField.setText(Long.toString(planetService.getTickCount()));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    private void backup() {
//        List<Unit> backupEntry = new ArrayList<>();
//        backups.add(backupEntry);
//        for (Unit unit : pathingService.getUnits()) {
//            backupEntry.add(unit.getCopy());
//        }
    }

    public void onMouseDragged(Event event) {
        renderer.shifting(event);
        overlayRenderer.shifting(event);
        renderer.render();
    }

    public void onMouseReleased() {
        renderer.stopShift();
        overlayRenderer.stopShift();
    }

    public void onMousePressed() {
        if (hoverSyncItemSidePaneController != null) {
            selected = true;
            hoverSyncItemSidePaneController.setSelected(() -> {
                selected = false;
                leftSidePanel.getChildren().clear();
                hoverSyncItemSidePaneController = null;
            });
        }
    }

    public void onPerfmonButtonClicked() {
        Collection<StatisticEntry> statisticEntries = perfmonService.analyse();
        System.out.println("---------------------------------------------------------------------------------------------------------");
        for (StatisticEntry statisticEntry : statisticEntries) {
            System.out.println(statisticEntry.toInfoString());
        }
        System.out.println("---------------------------------------------------------------------------------------------------------");
    }
}
