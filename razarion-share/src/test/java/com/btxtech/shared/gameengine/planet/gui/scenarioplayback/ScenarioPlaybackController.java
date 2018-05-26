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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

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
    private Label testMethodLabel;
    @FXML
    private TextField syncItemText;
    @FXML
    private TableView<SyncItemProperty> syncItemPropertyTable;
    @FXML
    private TableColumn<SyncItemProperty, String> syncItemPropertyTableNameColumn;
    @FXML
    private TableColumn<SyncItemProperty, String> syncItemPropertyTableActualValueColumn;
    @FXML
    private TableColumn<SyncItemProperty, String> syncItemPropertyTableExpectedValueColumn;
    private ScenarioPlayback scenarioPlayback;
    private int tick;
    private List<SyncBaseItemInfo> currentActual;
    private List<SyncBaseItemInfo> currentExpected;
    private Integer currentSyncBaseItemId;
    private Runnable renderListener;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture simpleScheduledFuture;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        animationCheck.selectedProperty().addListener((observable, oldValue, newValue) -> animationTimer(newValue));
        syncItemPropertyTableNameColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getPropertyName()));
        syncItemPropertyTableNameColumn.setCellFactory(column -> new TableCell<SyncItemProperty, String>() {
            @Override
            public void updateIndex(int i) {
                super.updateIndex(i);
                if (i >= syncItemPropertyTable.getItems().size() || i < 0) {
                    setStyle(null);
                    setTextFill(Color.BLACK);
                    return;
                }
                SyncItemProperty syncItemProperty = syncItemPropertyTable.getItems().get(i);
                if (!syncItemProperty.isEquals()) {
                    setTextFill(Color.YELLOW);
                    setStyle("-fx-background-color: red");
                } else {
                    setStyle(null);
                    setTextFill(Color.BLACK);
                }
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item);
                }
            }
        });


        syncItemPropertyTableActualValueColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getPropertyActualValue()));
        syncItemPropertyTableActualValueColumn.setCellFactory(column -> {
            TableCell<SyncItemProperty, String> cell = new TableCell<SyncItemProperty, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(item);
                    }
                }
            };
            cell.setOnMouseClicked(event -> {
                System.out.println("To clipboard: " + cell.getItem());
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(cell.getItem());
                clipboard.setContent(content);
            });
            return cell;
        });

        syncItemPropertyTableExpectedValueColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getPropertyExpectedValue()));
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
        currentActual = scenarioPlayback.getActualSyncBaseItemInfo().get(tick);
        if (scenarioPlayback.getExpectedSyncBaseItemInfo() != null && tick < scenarioPlayback.getExpectedSyncBaseItemInfo().size()) {
            currentExpected = scenarioPlayback.getExpectedSyncBaseItemInfo().get(tick);
        } else {
            currentExpected = null;
        }
    }

    private void display() {
        tickLabel.setText(Integer.toString(scenarioPlayback.getTickCount()));
        tickField.setText(Integer.toString(tick));
        tickField.positionCaret(tickField.getLength());
        timeLabel.setText(String.format("%.3fs", (double) tick * PlanetService.TICK_FACTOR));
        testMethodLabel.setText(scenarioPlayback.getScenario().getFileName());
    }

    public void render(WeldTestRenderer weldTestRenderer) {
        currentActual.forEach(syncBaseItemInfo -> weldTestRenderer.drawSyncBaseItemInfo(syncBaseItemInfo, currentSyncBaseItemId != null && syncBaseItemInfo.getId() == currentSyncBaseItemId));
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
            renderListener.run();
            displayBaseItemInfo();
        } catch (NumberFormatException t) {
            // Ignore
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    public void onSyncItemIncreaseButton() {
        try {
            if (currentSyncBaseItemId != null) {
                currentSyncBaseItemId++;
            } else {
                currentSyncBaseItemId = 1;
            }
            syncItemText.setText(Integer.toString(currentSyncBaseItemId));
            renderListener.run();
            displayBaseItemInfo();
        } catch (NumberFormatException t) {
            // Ignore
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void onSyncItemDecreaseButton() {
        try {
            if (currentSyncBaseItemId != null) {
                currentSyncBaseItemId--;
            } else {
                currentSyncBaseItemId = 1;
            }
            syncItemText.setText(Integer.toString(currentSyncBaseItemId));
            renderListener.run();
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
            SyncBaseItemInfo actualSyncBaseItemInfo = currentActual.stream().filter(info -> info.getId() == currentSyncBaseItemId).findFirst().orElse(null);
            SyncBaseItemInfo expectedSyncBaseItemInfo = null;
            if (currentExpected != null) {
                expectedSyncBaseItemInfo = currentExpected.stream().filter(info -> info.getId() == currentSyncBaseItemId).findFirst().orElse(null);
            }
            if (actualSyncBaseItemInfo != null) {
                syncItemPropertyTable.getItems().add(SyncItemProperty.createInt("Id", actualSyncBaseItemInfo, expectedSyncBaseItemInfo, SyncBaseItemInfo::getId));
                syncItemPropertyTable.getItems().add(SyncItemProperty.createDecimalPosition("Position", actualSyncBaseItemInfo, expectedSyncBaseItemInfo, syncBaseItemInfo -> syncBaseItemInfo.getSyncPhysicalAreaInfo().getPosition()));
                syncItemPropertyTable.getItems().add(SyncItemProperty.createRad2Grad("Angle", actualSyncBaseItemInfo, expectedSyncBaseItemInfo, syncBaseItemInfo -> syncBaseItemInfo.getSyncPhysicalAreaInfo().getAngle()));
                if (actualSyncBaseItemInfo.getSyncPhysicalAreaInfo().getVelocity() != null) {
                    syncItemPropertyTable.getItems().add(SyncItemProperty.createDecimalPosition("Velocity", actualSyncBaseItemInfo, expectedSyncBaseItemInfo, syncBaseItemInfo -> syncBaseItemInfo.getSyncPhysicalAreaInfo().getVelocity()));
                    syncItemPropertyTable.getItems().add(SyncItemProperty.createDouble("Speed", actualSyncBaseItemInfo, expectedSyncBaseItemInfo, syncBaseItemInfo -> syncBaseItemInfo.getSyncPhysicalAreaInfo().getVelocity().magnitude()));
                }
                if (actualSyncBaseItemInfo.getSyncPhysicalAreaInfo().getWayPositions() != null) {
                    syncItemPropertyTable.getItems().add(SyncItemProperty.createDecimalPositionList("Way positions", actualSyncBaseItemInfo, expectedSyncBaseItemInfo, syncBaseItemInfo -> syncBaseItemInfo.getSyncPhysicalAreaInfo().getWayPositions()));
                    syncItemPropertyTable.getItems().add(SyncItemProperty.createInt("Current way pointIndex", actualSyncBaseItemInfo, expectedSyncBaseItemInfo, syncBaseItemInfo -> syncBaseItemInfo.getSyncPhysicalAreaInfo().getCurrentWayPointIndex()));
                }
            }
        } catch (NumberFormatException t) {
            // Ignore
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void onSaveButton() {
        scenarioPlayback.getScenario().onSave();
    }
}
