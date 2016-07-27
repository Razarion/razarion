package com.btxtech.webglemulator;

import com.btxtech.scenariongui.InstanceStringGenerator;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Ray3d;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.btxtech.webglemulator.razarion.RazarionEmulator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Beat
 * 22.05.2016.
 */
@Singleton
public class WebGlEmulatorController implements Initializable {
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
    private Slider shadowZRotationSlider;
    @FXML
    private Canvas canvas;
    @Inject
    private RazarionEmulator razarionEmulator;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    @Inject
    private ShadowUiService shadowUiService;
    @Inject
    private WebGlEmulatorShadowController shadowController;
    @Inject
    private WebGlEmulatorSceneController sceneController;
    private DecimalPosition lastCanvasPosition;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        centerPanel.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number width) {
                canvas.setWidth(width.doubleValue());
                projectionTransformation.setAspectRatio(getAspectRatio());
                aspectRatioLabel.setText(Double.toString(projectionTransformation.getAspectRatio()));
            }
        });
        centerPanel.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number height) {
                canvas.setHeight(height.doubleValue());
                projectionTransformation.setAspectRatio(getAspectRatio());
                aspectRatioLabel.setText(Double.toString(projectionTransformation.getAspectRatio()));
            }
        });
        canvas.setFocusTraversable(true);
        canvas.addEventFilter(MouseEvent.ANY, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                canvas.requestFocus();
            }
        });
        ////////
//        camera.setTranslateX(970);
//        camera.setTranslateY(-80);
//        camera.setTranslateZ(0);
//        camera.setRotateX(Math.toRadians(90));
//        camera.setRotateZ(Math.toRadians(0));
//        projectionTransformation.setFovY(Math.toRadians(110));
        ////////

        fovSlider.valueProperty().set(Math.toDegrees(projectionTransformation.getFovY()));
        fovSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number newValue) {
                projectionTransformation.setFovY(Math.toRadians(fovSlider.getValue()));
                System.out.println("Camera direction: " + camera.getDirection());
            }
        });
        cameraXRotationSlider.valueProperty().set(Math.toDegrees(camera.getRotateX()));
        cameraXRotationSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number newValue) {
                camera.setRotateX(Math.toRadians(cameraXRotationSlider.getValue()));
                System.out.println("X rot: " + cameraXRotationSlider.getValue());
                System.out.println("Z rot: " + cameraZRotationSlider.getValue());
                System.out.println("Camera direction: " + camera.getDirection());
                System.out.println("zNear: " + projectionTransformation.calculateZNear());
                System.out.println("zFar: " + projectionTransformation.calculateZFar());
            }
        });
        cameraZRotationSlider.valueProperty().set(Math.toDegrees(camera.getRotateZ()));
        cameraZRotationSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number newValue) {
                camera.setRotateZ(Math.toRadians(cameraZRotationSlider.getValue()));
                System.out.println("X rot: " + cameraXRotationSlider.getValue());
                System.out.println("Z rot: " + cameraZRotationSlider.getValue());
                System.out.println("Camera direction: " + camera.getDirection());
                System.out.println("zNear: " + projectionTransformation.calculateZNear());
                System.out.println("zFar: " + projectionTransformation.calculateZFar());
            }
        });
        xTranslationField.setText(Double.toString(camera.getTranslateX()));
        yTranslationField.setText(Double.toString(camera.getTranslateY()));
        zTranslationField.setText(Double.toString(camera.getTranslateZ()));

        shadowXRotationSlider.valueProperty().set(Math.toDegrees(shadowUiService.getRotateX()));
        shadowXRotationSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number newValue) {
                shadowUiService.setRotateX(Math.toRadians(shadowXRotationSlider.getValue()));
                System.out.println("Shadow X rot: " + shadowXRotationSlider.getValue());
                System.out.println("Shadow Y rot: " + shadowZRotationSlider.getValue());
                System.out.println("Shadow direction: " + shadowUiService.getLightDirection());
                shadowUiService.calculateViewField();
            }
        });
        shadowZRotationSlider.valueProperty().set(Math.toDegrees(shadowUiService.getRotateZ()));
        shadowZRotationSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number newValue) {
                shadowUiService.setRotateZ(Math.toRadians(shadowZRotationSlider.getValue()));
                System.out.println("Shadow X rot: " + shadowXRotationSlider.getValue());
                System.out.println("Shadow Y rot: " + shadowZRotationSlider.getValue());
                System.out.println("Shadow direction: " + shadowUiService.getLightDirection());
                shadowUiService.calculateViewField();
            }
        });
    }

    public void onMouseDragged(Event event) {
        MouseEvent mouseEvent = (MouseEvent) event;
        DecimalPosition canvasPosition = new DecimalPosition(mouseEvent.getX(), mouseEvent.getY());

        if (lastCanvasPosition != null) {
            DecimalPosition terrainOld = getTerrainPosition(lastCanvasPosition).toXY();
            DecimalPosition terrainNew = getTerrainPosition(canvasPosition).toXY();
            DecimalPosition deltaTerrain = terrainNew.sub(terrainOld);
            camera.setTranslateX(camera.getTranslateX() - deltaTerrain.getX());
            camera.setTranslateY(camera.getTranslateY() - deltaTerrain.getY());
            xTranslationField.setText(Double.toString(camera.getTranslateX()));
            yTranslationField.setText(Double.toString(camera.getTranslateY()));
        }
        lastCanvasPosition = canvasPosition;
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

    public void onMousePressed(Event event) {
        MouseEvent mouseEvent = (MouseEvent) event;
        DecimalPosition canvasPosition = new DecimalPosition(mouseEvent.getX(), mouseEvent.getY());
        DecimalPosition clipXY = toClipCoordinates(canvasPosition);
        Ray3d pickRay = projectionTransformation.createPickRay(clipXY);
        Ray3d worldPickRay = camera.toWorld(pickRay);
        Vertex groundMeshPosition = terrainUiService.calculatePositionGroundMesh(worldPickRay);
        System.out.println("Ground Mesh position: " + groundMeshPosition);
    }

    private Vertex getTerrainPosition(DecimalPosition canvasPosition) {
        DecimalPosition clipXY = toClipCoordinates(canvasPosition);
        Ray3d pickRay = projectionTransformation.createPickRay(clipXY);
        Ray3d worldPickRay = camera.toWorld(pickRay);
        return terrainUiService.calculatePositionOnZeroLevel(worldPickRay);
    }

    public void onMouseReleased() {
        lastCanvasPosition = null;
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
        System.out.println(InstanceStringGenerator.generate(projectionTransformation.createMatrix()));
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
            loader.setControllerFactory(new Callback<Class<?>, Object>() {
                @Override
                public Object call(Class<?> param) {
                    return shadowController;
                }
            });
            AnchorPane root = (AnchorPane) loader.load();
            stage.setTitle("Shadow");
            stage.setScene(new Scene(root));
            stage.setX(-1288);
            stage.setY(168);
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we) {
                    shadowController.setActive(false);
                }
            });
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onSceneButtonClicked() {
        if (sceneController.getCanvas() != null) {
            sceneController.update();
            return;
        }
        try {
            final Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/webglemulation/WebGlEmulatorScene.fxml"));
            loader.setControllerFactory(new Callback<Class<?>, Object>() {
                @Override
                public Object call(Class<?> param) {
                    return sceneController;
                }
            });
            AnchorPane root = (AnchorPane) loader.load();
            stage.setTitle("Scene");
            stage.setScene(new Scene(root));
            stage.setX(-1288);
            stage.setY(168);
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we) {
                    sceneController = null;
                }
            });
            stage.show();
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we) {
                    sceneController.setCanvas(null);
                }
            });

            sceneController.update();
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
        // terrainScrollHandler.handleMouseMoveScroll((int)event.getX(), (int)event.getY(), (int)canvas.getWidth(), (int)canvas.getHeight());
    }

    public Canvas getCanvas() {
        return canvas;
    }

    private double getAspectRatio() {
        return canvas.getWidth() / canvas.getHeight();
    }

}
