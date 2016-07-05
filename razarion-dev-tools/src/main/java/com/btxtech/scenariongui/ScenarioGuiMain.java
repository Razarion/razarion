package com.btxtech.scenariongui;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.Index;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Created by Beat
 * 24.01.2016.
 */
public class ScenarioGuiMain extends Application {
    private static final int CANVAS_WIDTH = 5000;
    private static final int CANVAS_HEIGHT = 5000;
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;
    private static final int GRID_SPACING = 100;
    private double scaleDouble = 1;


    public void start(final Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/ScenarioGui.fxml"));
        stage.setTitle("Scenario Gui");
        stage.setScene(new Scene(root));
        stage.setX(-1279);
        stage.setY(182);
        stage.setWidth(1277);
        stage.setHeight(1016);
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.out.println("Stage is closing. Windows position: " + stage.getX() + ":" + stage.getY() + " " + stage.getWidth() + ":" + stage.getHeight());
            }
        });
    }



    public void start2(Stage stage) {
        final Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawShapes(gc, new Index());

        ScrollPane scroll = new ScrollPane();
        scroll.setContent(canvas);
        scroll.vvalueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
                // System.out.println("vvalueProperty: " + newValue);
            }
        });
        scroll.hvalueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
                // System.out.println("hvalueProperty: " + newValue);
            }
        });

        ToolBar toolBar = new ToolBar();
        final Slider slider = new Slider();
        slider.setMin(-10);
        slider.setMax(10);
        slider.setValue(0);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(10);
        slider.setMinorTickCount(10);
        slider.setSnapToTicks(true);
        slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
                int sliderPosition = newValue.intValue();
                if (sliderPosition > 0) {
                    scaleDouble = sliderPosition;
                } else if (sliderPosition < 0) {
                    scaleDouble = -1.0 / sliderPosition;
                } else {
                    scaleDouble = 1.0;
                }
                drawShapes(canvas.getGraphicsContext2D(), new Index(0, 0));
            }
        });
        toolBar.getItems().add(slider);
        Button zoomButton = new Button("1:1");
        toolBar.getItems().add(zoomButton);
        zoomButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                slider.setValue(0);
            }
        });
        final Label mousePosition = new Label("?:?");
        toolBar.getItems().add(mousePosition);

        BorderPane pane = new BorderPane();
        pane.setCenter(scroll);
        pane.setTop(toolBar);

        final Scene scene = new Scene(pane, WIDTH, HEIGHT, Color.WHITE);
        stage.setScene(scene);

        canvas.addEventFilter(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mousePosition.setText(((int) mouseEvent.getX() - CANVAS_WIDTH / 2.0) / scaleDouble + ":" + ((CANVAS_HEIGHT - (int) mouseEvent.getY() - CANVAS_HEIGHT / 2.0)) / scaleDouble);
            }
        });

        canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                Index position = new Index((int) ((mouseEvent.getX() - CANVAS_WIDTH / 2.0) / scaleDouble), (int) (((CANVAS_HEIGHT - mouseEvent.getY() - CANVAS_HEIGHT / 2.0)) / scaleDouble));
                drawShapes(canvas.getGraphicsContext2D(), position);

            }
        });

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.out.println("Windows position: " + scene.getWindow().getX() + ":" + scene.getWindow().getY());
            }
        });

        scene.getWindow().setX(-1236);
        scene.getWindow().setY(229);
        stage.show();
        scroll.setHvalue(0.645);
        scroll.setVvalue(0.419);
    }

    private void drawShapes(GraphicsContext gc, Index mousePosition) {
        gc.translate(0, 0);
        gc.scale(1.0, 1.0);
        gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        gc.save();

        gc.translate(CANVAS_WIDTH / 2.0, CANVAS_HEIGHT / 2.0);
        gc.scale(scaleDouble, -scaleDouble);


        // draw grid
        gc.setLineWidth(1);

        gc.setStroke(Color.GRAY);
        int verticalGrid = (int) Math.ceil(CANVAS_WIDTH / 2.0 / GRID_SPACING) * GRID_SPACING;
        for (int x = -verticalGrid; x < verticalGrid; x += GRID_SPACING) {
            gc.strokeLine(x, -CANVAS_WIDTH / 2.0, x, CANVAS_WIDTH / 2.0);
        }
        int horizontalGrid = (int) Math.ceil(CANVAS_HEIGHT / 2.0 / GRID_SPACING) * GRID_SPACING;
        for (int y = -horizontalGrid; y < horizontalGrid; y += GRID_SPACING) {
            gc.strokeLine(-CANVAS_HEIGHT / 2.0, y, CANVAS_HEIGHT / 2.0, y);
        }
        gc.setStroke(Color.BLACK);
        gc.strokeLine(-CANVAS_WIDTH / 2.0, 0, CANVAS_WIDTH / 2.0, 0);
        gc.strokeLine(0, -CANVAS_HEIGHT / 2.0, 0, CANVAS_HEIGHT / 2.0);

        ExtendedGraphicsContext extendedGraphicsContext = new ExtendedGraphicsContext(gc);
        // TerrainScenario.drawPolygon2DCombine(extendedGraphicsContext, mousePosition);
        // TerrainScenario.drawFromFile(extendedGraphicsContext);

        gc.restore();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
