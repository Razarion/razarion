package com.btxtech.webglemulator;

import com.btxtech.persistence.JsonProviderEmulator;
import com.btxtech.scenariongui.InstanceStringGenerator;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.InventoryTypeService;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.system.debugtool.DebugHelperStatic;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.inventory.InventoryUiService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.mouse.TerrainMouseHandler;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.RenderService;
import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import com.btxtech.uiservice.tip.GameTipService;
import com.btxtech.webglemulator.razarion.RazarionEmulator;
import com.btxtech.webglemulator.razarion.WorkerEmulator;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Created by Beat
 * 22.05.2016.
 */
@Singleton
public class WebGlEmulatorController implements Initializable {
    @FXML
    private CheckBox showNormCheckBox;
    @FXML
    private AnchorPane centerPanel;
    @FXML
    private Slider fovSlider;
    @FXML
    private Label aspectRatioLabel;
    @FXML
    private TextField xTranslationField;
    @FXML
    private TextField yTranslationField;
    @FXML
    private TextField zTranslationField;
    @FXML
    private Slider cameraZRotationSlider;
    @FXML
    private Slider cameraXRotationSlider;
    @FXML
    private Slider shadowXRotationSlider;
    @FXML
    private Slider shadowYRotationSlider;
    @FXML
    private Canvas canvas;
    @FXML
    private CheckBox showRenderTimeCheckBox;
    @FXML
    private VBox itemCockpitPanel;
    @FXML
    private Label resourceLabel;
    @FXML
    private Label xpLabel;
    @FXML
    private Label levelLabel;
    @Inject
    private Instance<RenderService> renderServiceInstance;
    @Inject
    private RazarionEmulator razarionEmulator;
    @Inject
    private VisualUiService visualUiService;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    @Inject
    private ShadowUiService shadowUiService;
    @Inject
    private WebGlEmulatorShadowController shadowController;
    @Inject
    private WorkerViewController workerViewController;
    @Inject
    private ClientViewController clientViewController;
    @Inject
    private TerrainMouseHandler terrainMouseHandler;
    @Inject
    private JsonProviderEmulator jsonProviderEmulator;
    @Inject
    private InventoryTypeService inventoryTypeService;
    @Inject
    private InventoryUiService inventoryUiService;
    @Inject
    private PerfmonService perfmonService;
    @Inject
    private SelectionHandler selectionHandler;
    @Inject
    private GameTipService gameTipService;
    @Inject
    private GameEngineControl gameEngineControl;
    @Inject
    private WorkerEmulator workerEmulator;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        centerPanel.widthProperty().addListener((observableValue, oldSceneWidth, width) -> {
            try {
                canvas.setWidth(width.doubleValue());
                projectionTransformation.setAspectRatio(getAspectRatio());
                aspectRatioLabel.setText(Double.toString(projectionTransformation.getAspectRatio()));
            } catch (IllegalStateException | IllegalArgumentException e) {
                // Catch ugly initialization bug
            }
        });
        centerPanel.heightProperty().addListener((observableValue, oldSceneWidth, height) -> {
            try {
                canvas.setHeight(height.doubleValue());
                projectionTransformation.setAspectRatio(getAspectRatio());
                aspectRatioLabel.setText(Double.toString(projectionTransformation.getAspectRatio()));
            } catch (NullPointerException e) {
                // Catch ugly initialization bug
            }
        });
        canvas.setFocusTraversable(true);
        canvas.addEventFilter(MouseEvent.ANY, mouseEvent -> canvas.requestFocus());

        fovSlider.valueProperty().addListener((observableValue, number, newValue) -> {
            projectionTransformation.setFovYSave(Math.toRadians(fovSlider.getValue()));
        });
        cameraXRotationSlider.valueProperty().addListener((observableValue, number, newValue) -> {
            camera.setRotateX(Math.toRadians(cameraXRotationSlider.getValue()));
        });
        cameraZRotationSlider.valueProperty().addListener((observableValue, number, newValue) -> {
            camera.setRotateZ(Math.toRadians(cameraZRotationSlider.getValue()));
        });
        shadowXRotationSlider.valueProperty().addListener((observableValue, number, newValue) -> {
            visualUiService.getPlanetVisualConfig().setShadowRotationX(Math.toRadians(shadowXRotationSlider.getValue()));
            shadowUiService.setupMatrices();
            clientViewController.update();
        });
        shadowYRotationSlider.valueProperty().addListener((observableValue, number, newValue) -> {
            visualUiService.getPlanetVisualConfig().setShadowRotationY(Math.toRadians(shadowYRotationSlider.getValue()));
            shadowUiService.setupMatrices();
            clientViewController.update();
        });

        showRenderTimeCheckBox.setSelected(razarionEmulator.isShowRenderTime());
    }


    public void onEngineInitialized() {
        showNormCheckBox.setSelected(renderServiceInstance.get().isShowNorm());
        fovSlider.valueProperty().set(Math.toDegrees(projectionTransformation.getFovY()));
        cameraXRotationSlider.valueProperty().set(Math.toDegrees(camera.getRotateX()));
        cameraZRotationSlider.valueProperty().set(Math.toDegrees(camera.getRotateZ()));
        xTranslationField.setText(Double.toString(camera.getTranslateX()));
        yTranslationField.setText(Double.toString(camera.getTranslateY()));
        zTranslationField.setText(Double.toString(camera.getTranslateZ()));

        shadowXRotationSlider.valueProperty().set(Math.toDegrees(visualUiService.getPlanetVisualConfig().getShadowRotationX()));
        shadowYRotationSlider.valueProperty().set(Math.toDegrees(visualUiService.getPlanetVisualConfig().getShadowRotationY()));
    }

    private DecimalPosition toClipCoordinates(DecimalPosition canvasPosition) {
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        double xScale = canvasWidth / 2.0;
        double xOffset = canvasWidth / 2.0;
        double yScale = -canvasHeight / 2.0;
        double yOffset = canvasHeight / 2.0;

        return new DecimalPosition((canvasPosition.getX() - xOffset) / xScale, (canvasPosition.getY() - yOffset) / yScale);
    }

    public void onDumpProjectionTransformatioClicked() {
        System.out.println("-----------Projection Transformation-----------------");
        System.out.println(projectionTransformation);
        System.out.println("-----------------------------------------------------");
    }

    public void onDumpCameraClicked() {
        System.out.println("---------------------Camera--------------------------");
        System.out.println(camera);
        System.out.println("-----------------------------------------------------");
    }

    public void onTestCaseButtonClicked() {
        System.out.println("---------------------Test Case-----------------------");
        System.out.println(camera);
        System.out.println(projectionTransformation);
        System.out.println(InstanceStringGenerator.generate(projectionTransformation.getMatrix()));
        System.out.println("-----------------------------------------------------");
    }

    public void xTranslationFieldChanged() {
        camera.setTranslateX(Double.parseDouble(xTranslationField.getText()));
    }

    public void yTranslationFieldChanged() {
        camera.setTranslateY(Double.parseDouble(yTranslationField.getText()));
    }

    public void zTranslationFieldChanged() {
        camera.setTranslateZ(Double.parseDouble(zTranslationField.getText()));
    }

    public void onTickButtonClicked() {
        // TODO temUiService.tick();
    }

    public void onRestartButtonClicked() {
        // TODO baseItemUiService.setupItems();
    }

    public void onShadowButtonClicked() {
        if (shadowController.getCanvas() != null) {
            return;
        }
        try {
            final Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/webglemulation/WebGlEmulatorShadow.fxml"));
            loader.setControllerFactory(param -> shadowController);
            AnchorPane root = loader.load();
            stage.setTitle("Shadow");
            stage.setScene(new Scene(root));
//            stage.setX(-1288);
//            stage.setY(168);
            stage.setOnCloseRequest(we -> shadowController.setActive(false));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onWorkerViewButtonClicked() {
        if (workerViewController.isActive()) {
            workerViewController.update();
            return;
        }
        try {
            final Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/webglemulation/WorkerView.fxml"));
            loader.setControllerFactory(param -> workerViewController);
            Parent root = loader.load();
            stage.setTitle("Worker View");
            stage.setScene(new Scene(root));
//            stage.setX(-1288);
//            stage.setY(168);
            stage.show();
            stage.setOnCloseRequest(we -> workerViewController.close());

            workerViewController.update();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void onClientViewButtonClicked() {
        if (clientViewController.isActive()) {
            clientViewController.update();
            return;
        }
        try {
            final Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/webglemulation/ClientView.fxml"));
            loader.setControllerFactory(param -> clientViewController);
            BorderPane root = loader.load();
            stage.setTitle("Client View");
            stage.setScene(new Scene(root));
//            stage.setX(-1288);
//            stage.setY(168);
            stage.show();
            stage.setOnCloseRequest(we -> clientViewController.close());

            clientViewController.update();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.LEFT) {
            terrainScrollHandler.executeAutoScrollKey(TerrainScrollHandler.ScrollDirection.LEFT, null);
        }
        if (event.getCode() == KeyCode.RIGHT) {
            terrainScrollHandler.executeAutoScrollKey(TerrainScrollHandler.ScrollDirection.RIGHT, null);
        }
        if (event.getCode() == KeyCode.UP) {
            terrainScrollHandler.executeAutoScrollKey(null, TerrainScrollHandler.ScrollDirection.TOP);
        }
        if (event.getCode() == KeyCode.DOWN) {
            terrainScrollHandler.executeAutoScrollKey(null, TerrainScrollHandler.ScrollDirection.BOTTOM);
        }
        if (event.getCode() == KeyCode.ESCAPE) {
            selectionHandler.clearSelection(false);
        }
    }

    public void onKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.LEFT) {
            terrainScrollHandler.executeAutoScrollKey(TerrainScrollHandler.ScrollDirection.STOP, null);
        }
        if (event.getCode() == KeyCode.RIGHT) {
            terrainScrollHandler.executeAutoScrollKey(TerrainScrollHandler.ScrollDirection.STOP, null);
        }
        if (event.getCode() == KeyCode.UP) {
            terrainScrollHandler.executeAutoScrollKey(null, TerrainScrollHandler.ScrollDirection.STOP);
        }
        if (event.getCode() == KeyCode.DOWN) {
            terrainScrollHandler.executeAutoScrollKey(null, TerrainScrollHandler.ScrollDirection.STOP);
        }
    }

    public void onMouseMoved(MouseEvent event) {
        terrainMouseHandler.onMouseMove((int) event.getX(), (int) event.getY(), (int) canvas.getWidth(), (int) canvas.getHeight(), event.isPrimaryButtonDown());
    }

    public void onMouseDragged(MouseEvent event) {
        terrainMouseHandler.onMouseMove((int) event.getX(), (int) event.getY(), (int) canvas.getWidth(), (int) canvas.getHeight(), event.isPrimaryButtonDown());
    }

    public void onMouseOut() {
        terrainMouseHandler.onMouseOut();
    }

    public void onMousePressed(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            terrainMouseHandler.onMouseDown((int) event.getX(), (int) event.getY(), (int) canvas.getWidth(), (int) canvas.getHeight(), event.isShiftDown());
        }
    }

    public void onMouseReleased(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            terrainMouseHandler.onMouseUp((int) event.getX(), (int) event.getY(), (int) canvas.getWidth(), (int) canvas.getHeight());
        }
    }

    public Canvas getCanvas() {
        return canvas;
    }

    private double getAspectRatio() {
        return canvas.getWidth() / canvas.getHeight();
    }

    public void onShowRenderTimeCheckBox() {
        razarionEmulator.setShowRenderTime(showRenderTimeCheckBox.isSelected());
    }

    public void onShowNormCheckBox() {
        renderServiceInstance.get().setShowNorm(showNormCheckBox.isSelected());
    }

    public Pane getItemCockpitPanel() {
        return itemCockpitPanel;
    }

    public void onInventoryButtonClicked() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Inventory Dialog");
            alert.setHeaderText("Inventory Items");
            alert.setContentText("Choose your option.");

            inventoryUiService.provideInventoryInfo(inventoryInfo -> {
                if (inventoryInfo.getInventoryItemIds() != null) {
                    inventoryInfo.getInventoryItemIds().forEach(inventoryItemId -> alert.getButtonTypes().add(new ButtonType(Integer.toString(inventoryItemId))));
                }
            });

            Optional<ButtonType> result = alert.showAndWait();
            InventoryItem inventoryItem = inventoryTypeService.getInventoryItem(Integer.parseInt(result.get().getText()));
            System.out.println("pressed: " + inventoryItem);
            inventoryUiService.useItem(inventoryItem);

        });
        gameTipService.onInventoryDialogOpened();
    }

    public void onPerfomButtonClicked() {
        System.out.println("******************* TODO *******************");
    }

    public void displayResource(int resource) {
        resourceLabel.setText("Res: " + resource);
    }

    public void displayXp(int xp, int xp2LevelUp) {
        xpLabel.setText("XP: " + xp + " / " + xp2LevelUp);
    }

    public void displayLevel(int levelNumber) {
        levelLabel.setText("Level: " + levelNumber);
    }

    public void onSaveTickDataButton() {
        try {
            System.out.println("onSaveTickDataButton() start saving");
            new ObjectMapper().writeValue(new File(DebugHelperStatic.TICK_DATA_SLAVE), workerEmulator.getWorkerBean(PlanetService.class).getTickDatas());
            System.out.println("onSaveTickDataButton() saved to: " + DebugHelperStatic.TICK_DATA_SLAVE);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void onClearTickDataButton() {
        System.out.println("onClearTickDataButton()");
        DebugHelperStatic.clearTickDatas(workerEmulator.getWorkerBean(PlanetService.class).getTickDatas());
    }
}
