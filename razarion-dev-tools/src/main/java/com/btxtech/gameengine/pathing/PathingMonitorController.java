package com.btxtech.gameengine.pathing;

import com.btxtech.scenariongui.InstanceStringGenerator;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;
import com.btxtech.shared.gameengine.planet.pathing.Unit;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Beat
 * 17.05.2016.
 */
public class PathingMonitorController implements Initializable {
    public Canvas canvas;
    public TextField stepField;
    public TextField scenarioField;
    public TextField mouseField;
    public Pane leftSidePanel;
    public Slider zoomSlider;
    public TextField scaleField;
    public AnchorPane anchorPanel;
    private PathingService pathingService;
    private int delay = PathingService.MILLI_S;
    private JavaFxGameRenderer renderer;
    private Scenario scenario;
    private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
    private ScheduledFuture scheduleAtFixedRate;
    private List<Collection<Unit>> backups = new ArrayList<>();
    private UnitSidePaneController hoverUnitSidePaneController;
    private boolean selected;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        zoomSlider.valueProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                setZoom(zoomSlider.getValue());
            }
        });

        anchorPanel.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number width) {
                canvas.setWidth(width.doubleValue());
            }
        });

        anchorPanel.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number height) {
                canvas.setHeight(height.doubleValue());
            }
        });


        scenario = new Scenario();
        pathingService = scenario.init(34);
        scenarioField.setText(Integer.toString(scenario.getNumber()));
        renderer = new JavaFxGameRenderer(canvas, 1.0);
        renderer.render(pathingService);

        onRun();
    }

    public void onRun() {
        if (scheduleAtFixedRate != null) {
            return;
        }
        scheduleAtFixedRate = scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    tick();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }

            }
        }, delay, delay, TimeUnit.MILLISECONDS);
    }

    public void onTurbo() {
        if (delay < PathingService.MILLI_S) {
            delay = PathingService.MILLI_S;
        } else {
            delay = 1;
        }
        onHold();
        onRun();
    }

    public void onHold() {
        if (scheduleAtFixedRate != null) {
            scheduleAtFixedRate.cancel(true);
            scheduleAtFixedRate = null;
        }
    }

    public void onStep() {
        onHold();
        scheduledThreadPool.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    tick();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }, 0, TimeUnit.MILLISECONDS);
    }

    public void onStepBackward() {
        onHold();
        if (backups.isEmpty()) {
            return;
        }
        pathingService.restore(backups.remove(backups.size() - 1), pathingService.getTickCount() - 1);
        renderer.render(pathingService);
        stepField.setText(Long.toString(pathingService.getTickCount()));
    }

    public void onNextScenario() {
        dispose();
        pathingService = scenario.initNext();
        scenarioField.setText(Integer.toString(scenario.getNumber()));
        backups.clear();
        onRun();
    }

    public void onRestartScenario() {
        dispose();
        pathingService = scenario.initCurrent();
        scenarioField.setText(Integer.toString(scenario.getNumber()));
        backups.clear();
        onRun();
    }

    public void onPrevScenario() {
        dispose();
        pathingService = scenario.initPrevious();
        scenarioField.setText(Integer.toString(scenario.getNumber()));
        backups.clear();
        onRun();
    }

    public void onMouseMove(Event event) {
        // Display mouse position
        DecimalPosition position = renderer.convertMouseToModel(event);
        mouseField.setText(String.format("%.2f:%.2f", position.getX(), position.getY()));
        // Show left side menu
        if (!selected) {
            Unit unit = pathingService.getUnit(position);
            if (unit != null) {
                if (hoverUnitSidePaneController == null || !hoverUnitSidePaneController.isSame(unit)) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/pathing/UnitSidePane.fxml"));
                        FlowPane unitSideBar = (FlowPane) loader.load();
                        hoverUnitSidePaneController = loader.getController();
                        hoverUnitSidePaneController.init(unit);
                        leftSidePanel.getChildren().setAll(unitSideBar);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if (hoverUnitSidePaneController != null) {
                    leftSidePanel.getChildren().clear();
                    hoverUnitSidePaneController = null;
                }
            }

        }
    }

    public void onCreateTestCase() {
        System.out.println("    @Test");
        System.out.println("    public void testCase() throws Exception {");
        System.out.println("        PathingService pathingService = new PathingService();");
        for (Unit unit : pathingService.getUnits()) {
            String unitParams = unit.getId() + ", " + unit.isCanMove() + ", " + unit.getRadius() + ", "
                    + InstanceStringGenerator.generate(unit.getPosition()) + ", " + InstanceStringGenerator.generate(unit.getVelocity()) + ", "
                    + InstanceStringGenerator.generate(unit.getDestination()) + ", " + InstanceStringGenerator.generate(unit.getDestination());
            System.out.println("        pathingService.createUnit(" + unitParams + ");");
        }
        for (Obstacle obstacle : pathingService.getObstacles()) {
            System.out.println("        pathingService.createObstacle(" + InstanceStringGenerator.generate(obstacle.getLine()) + ");");
        }
        System.out.println("        pathingService.tick(PathingService.FACTOR);");


        System.out.println("    }");
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
        scaleField.setText(String.format("%.2f", renderer.getScale()));
        renderer.render(pathingService);
    }

    public void onZoomResetButton() {
        setZoom(1);
    }

    private void tick() {
        backup();
        pathingService.tick();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    renderer.render(pathingService);
                    stepField.setText(Long.toString(pathingService.getTickCount()));
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });
    }

    private void dispose() {
        onHold();
        pathingService.stop();
        renderer.render(pathingService);
    }

    private void backup() {
        List<Unit> backupEntry = new ArrayList<>();
        backups.add(backupEntry);
        for (Unit unit : pathingService.getUnits()) {
            backupEntry.add(unit.getCopy());
        }
    }

    public void onMouseDragged(Event event) {
        renderer.shifting(event);
        renderer.render(pathingService);
    }

    public void onMouseReleased() {
        renderer.stopShift();
    }

    public void onMousePressed() {
        if (hoverUnitSidePaneController != null) {
            selected = true;
            hoverUnitSidePaneController.setSelected(new Runnable() {
                @Override
                public void run() {
                    selected = false;
                    leftSidePanel.getChildren().clear();
                    hoverUnitSidePaneController = null;
                }
            });
        }
    }
}
