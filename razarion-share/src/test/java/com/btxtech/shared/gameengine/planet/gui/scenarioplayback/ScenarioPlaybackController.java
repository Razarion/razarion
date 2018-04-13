package com.btxtech.shared.gameengine.planet.gui.scenarioplayback;

import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.gui.WeldTestRenderer;
import com.btxtech.shared.gameengine.planet.gui.userobject.ScenarioPlayback;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Beat
 * on 13.04.2018.
 */
public class ScenarioPlaybackController implements Initializable {
    @FXML
    private Label tickLabel;
    @FXML
    private TextField tickField;
    @FXML
    private Label timeLabel;
    @FXML
    private CheckBox animationCheck;
    @FXML
    private TableView<SyncItemProperty> syncItemPropertyTable;
    @FXML
    private TableColumn<SyncItemProperty, String> syncItemPropertyTableNameColumn;
    @FXML
    private TableColumn<SyncItemProperty, String> syncItemPropertyTableValueColumn;
    private ScenarioPlayback scenarioPlayback;
    private int tick;
    private List<SyncBaseItemInfo> current;
    private Integer currentSyncBaseItemId;
    private Runnable renderListener;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture simpleScheduledFuture;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        animationCheck.selectedProperty().addListener((observable, oldValue, newValue) -> animationTimer(newValue));
        syncItemPropertyTableNameColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getPropertyName()));
        syncItemPropertyTableValueColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getPropertyValue()));
        display();
    }

    public void setScenarioPlayback(ScenarioPlayback scenarioPlayback, Runnable renderListener) {
        this.scenarioPlayback = scenarioPlayback;
        this.renderListener = renderListener;
        setupCurrent();
    }

    public void onTickField() {
        try {
            if (tickField.getText().trim().isEmpty()) {
                tick = 0;
            } else {
                tick = Integer.parseInt(tickField.getText());
            }
            if (tick >= scenarioPlayback.getTickCount()) {
                tick = scenarioPlayback.getTickCount() - 1;
            }
            if (tick < 0) {
                tick = 0;
            }
            setupCurrent();
            display();
            renderListener.run();
            displayBaseItemInfo();
        } catch (NumberFormatException t) {
            tickField.setText(Integer.toString(tick));
            tickField.positionCaret(tickField.getLength());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void onNextTickButtonClicked() {
        tick++;
        if (tick >= scenarioPlayback.getTickCount()) {
            tick = scenarioPlayback.getTickCount() - 1;
            return;
        }
        setupCurrent();
        display();
        renderListener.run();
        displayBaseItemInfo();
    }

    public void onPrefTickButtonClicked() {
        tick--;
        if (tick < 0) {
            tick = 0;
            return;
        }
        setupCurrent();
        display();
        renderListener.run();
        displayBaseItemInfo();
    }

    private void setupCurrent() {
        current = scenarioPlayback.getSyncBaseItemInfo().get(tick);
    }

    private void display() {
        tickLabel.setText(Integer.toString(scenarioPlayback.getTickCount()));
        tickField.setText(Integer.toString(tick));
        tickField.positionCaret(tickField.getLength());
        timeLabel.setText(String.format("%.3fs", (double) tick * PlanetService.TICK_FACTOR));
    }

    public void render(WeldTestRenderer weldTestRenderer) {
        current.forEach(weldTestRenderer::drawSyncBaseItemInfo);
    }

    private void animationTimer(boolean start) {
        if (simpleScheduledFuture != null) {
            simpleScheduledFuture.cancel(true);
            simpleScheduledFuture = null;
        }

        if (start) {
            simpleScheduledFuture = scheduler.scheduleAtFixedRate(() -> {
                if (tick >= scenarioPlayback.getTickCount()) {
                    animationTimer(false);
                    return;
                }
                Platform.runLater(() -> {
                    try {
                        onNextTickButtonClicked();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                });
            }, PlanetService.TICK_TIME_MILLI_SECONDS, PlanetService.TICK_TIME_MILLI_SECONDS, TimeUnit.MILLISECONDS);
        }

        Platform.runLater(() -> {
            try {
                animationCheck.setSelected(start);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    public void onSyncItemTextChanged(KeyEvent keyEvent) {
        try {
            currentSyncBaseItemId = null;
            currentSyncBaseItemId = Integer.parseInt(((TextField) keyEvent.getSource()).getText());
            displayBaseItemInfo();
        } catch (NumberFormatException t) {
            // Ignore
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void displayBaseItemInfo() {
        syncItemPropertyTable.getItems().clear();
        if (currentSyncBaseItemId == null) {
            return;
        }
        try {
            SyncBaseItemInfo syncBaseItemInfo = current.stream().filter(info -> info.getId() == currentSyncBaseItemId).findFirst().orElse(null);
            if (syncBaseItemInfo != null) {
                syncItemPropertyTable.getItems().add(SyncItemProperty.create("Id", syncBaseItemInfo.getId()));
                syncItemPropertyTable.getItems().add(SyncItemProperty.create("Position", syncBaseItemInfo.getSyncPhysicalAreaInfo().getPosition()));
                syncItemPropertyTable.getItems().add(SyncItemProperty.createRad2Grad("Angle", syncBaseItemInfo.getSyncPhysicalAreaInfo().getAngle()));
                if (syncBaseItemInfo.getSyncPhysicalAreaInfo().getVelocity() != null) {
                    syncItemPropertyTable.getItems().add(SyncItemProperty.create("Velocity", syncBaseItemInfo.getSyncPhysicalAreaInfo().getVelocity()));
                }
            }
        } catch (NumberFormatException t) {
            // Ignore
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
