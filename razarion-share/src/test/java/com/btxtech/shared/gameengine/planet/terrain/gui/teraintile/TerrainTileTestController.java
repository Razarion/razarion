package com.btxtech.shared.gameengine.planet.terrain.gui.teraintile;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.gui.AbstractTerrainTestController;
import com.btxtech.shared.gameengine.planet.terrain.gui.AbstractTerrainTestRenderer;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

/**
 * Created by Beat
 * 09.04.2017.
 */
public class TerrainTileTestController extends AbstractTerrainTestController {
    private final Collection<TerrainTile> expected;
    private final Collection<TerrainTile> actual;
    private TriangleContainer triangleContainer;

    public TerrainTileTestController(Collection<TerrainTile> expected, Collection<TerrainTile> actual) {
        this.expected = expected;
        this.actual = actual;
        triangleContainer = new TriangleContainer(expected, actual);
    }

    @Override
    protected AbstractTerrainTestRenderer setupRenderer() {
        return new TerrainTileTestRenderer(expected, actual, triangleContainer);
    }

    @Override
    protected void onMousePressedTerrain(DecimalPosition position) {
        triangleContainer.printTrianglesAt(position);
    }

}
