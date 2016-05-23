package com.btxtech.webglemulator;

import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.webglemulator.razarion.RazarionEmulator;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 19.09.2015.
 */
@Singleton
public class FxCdiGui {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    private Label mousePosition;
    private Pane canvas;
    @Inject
    private Instance<Object> instance;
    @Inject
    private RazarionEmulator razarionEmulator;
    // @Inject
    // private ProjectionTransformation projectionTransformation;
    // @Inject
    // private Camera camera;
    // @Inject
    private TerrainSurface terrainSurface;
    private DecimalPosition startMouseMove;

    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/WebGlEmulator.fxml"));
        loader.setControllerFactory(new Callback<Class<?>, Object>() {
            @Override
            public Object call(Class<?> param) {
                return instance.select(param).get();
            }
        });
        Parent root = (Parent) loader.load();
        stage.setTitle("WebGL FX Emulator");
        stage.setScene(new Scene(root, WIDTH, HEIGHT));
        stage.setX(-867);
        stage.setY(387);
        stage.show();
        razarionEmulator.process();

//        canvas = new Pane();
//        canvas.addEventFilter(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
//            @Override
//            public void handle(MouseEvent mouseEvent) {
//                DecimalPosition endMouseMove = new DecimalPosition(mouseEvent.getX(), mouseEvent.getY());
//                DecimalPosition delta = endMouseMove.sub(startMouseMove);
//                startMouseMove = endMouseMove;
//                camera.setTranslateX(camera.getTranslateX() + delta.getX());
//                camera.setTranslateY(camera.getTranslateY() - delta.getY());
//                drawArrays();
//            }
//        });
//        canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
//            @Override
//            public void handle(MouseEvent mouseEvent) {
//                startMouseMove = new DecimalPosition(mouseEvent.getX(), mouseEvent.getY());
//            }
//        });
//
//        canvas.addEventFilter(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {
//            @Override
//            public void handle(MouseEvent mouseEvent) {
//                DecimalPosition mouseScreen = new DecimalPosition(mouseEvent.getX(), mouseEvent.getY());
//                DecimalPosition webGl = toWebGl(mouseScreen);
//                mousePosition.setText(webGl.getX() + ":" + webGl.getY());
//            }
//        });
//
//        mousePosition = new Label("?:?");
//        ToolBar toolBar = new ToolBar();
//        toolBar.getItems().add(mousePosition);
//
//        BorderPane pane = new BorderPane();
//        pane.setTop(toolBar);
//        pane.setCenter(canvas);
//
//        Scene scene = new Scene(pane, WIDTH, HEIGHT, Color.WHITE);
//        stage.setScene(scene);
//        stage.show();
//
//
//        fillBuffer(terrainSurface.getGroundVertexList().createPositionDoubles());
//
//        // fillBufferDouble(new Sphere(8000, 10, 10).provideVertexListPlain(Terrain.BUSH_1).createPositionDoubles());
//
//
//// TODO        drawArrays();
//        // VertexList vertexList = new VertexList();
//        // vertexList.add(new Triangle(new Vertex(-0.9, -0.9, 0), new Vertex(0.9, -0.9, 0), new Vertex(0.9, 0.9, 0)));
//        // drawArrays(vertexList.createPositionDoubles());
    }

    private DecimalPosition toWebGl(DecimalPosition screenPosition) {
        double webGlX = (2.0 / canvas.getWidth()) * screenPosition.getX() - 1;
        double webGlY = (-2.0 / canvas.getHeight()) * screenPosition.getY() + 1;
        return new DecimalPosition(webGlX, webGlY);
    }


}
