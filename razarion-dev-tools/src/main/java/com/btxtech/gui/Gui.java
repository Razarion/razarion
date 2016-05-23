package com.btxtech.gui;

import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.gui.scenario.AnimationScenario;
import com.btxtech.gui.scenario.Scenario;
import com.btxtech.gui.scenario.Triangle2dScenario;
import com.btxtech.gui.scenario.TriangulationManualScenario;
import com.btxtech.gui.scenario.TriangulationScenario;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

/**
 * Created by Beat
 * 16.07.2015.
 */
public class Gui extends Application {
    public static final int WIDTH = 1000;
    public static final int HEIGHT = 600;
    private Pane canvas;
    private Label mousePosition;
    private Scenario scenario = new TriangulationScenario();

    @Override
    public void start(Stage stage) throws Exception {
        canvas = new Pane();
        // canvas.setSnapToPixel(true);

        scenario.setCanvas(canvas);
        scenario.setup();

        mousePosition = new Label("?:?");
        ToolBar toolBar = new ToolBar();
        toolBar.getItems().add(mousePosition);
        Button generateButton = new Button("Generate");
        generateButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                scenario.onGenerate();
            }
        });
        toolBar.getItems().add(generateButton);

        BorderPane pane = new BorderPane();
        pane.setTop(toolBar);
        pane.setCenter(canvas);

        Scene scene = new Scene(pane, WIDTH, HEIGHT, Color.WHITE);
        stage.setScene(scene);

        canvas.addEventFilter(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mousePosition.setText((int) mouseEvent.getX() - WIDTH / 2 + ":" + (HEIGHT - (int) mouseEvent.getY() - HEIGHT / 2));
            }
        });

        canvas.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                Circle circle = new Circle(mouseEvent.getX(), mouseEvent.getY(), 1);
                circle.setFill(Color.BLACK);
                canvas.getChildren().add(circle);

                Index position = new Index((int) mouseEvent.getX(), HEIGHT - (int) mouseEvent.getY());

                scenario.onMouseDown(position);
            }
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
