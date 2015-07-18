package com.btxtech.client.editor;

import com.btxtech.client.Webgl;
import com.btxtech.client.terrain.Terrain;
import com.btxtech.game.jsre.client.common.Index;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 12.05.2015.
 */
public class PlateauEditor extends SvgEditor {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 800;

    private PlateauEditor() {
        super(WIDTH, HEIGHT, false);
    }

    public static void showEditor() {
        PlateauEditor plateauEditor = new PlateauEditor();
        plateauEditor.show();
        plateauEditor.setFocus();
    }

    @Override
    protected List<Index> getIndexes() {
        return Terrain.getInstance().getCorners();
    }

    @Override
    protected void setIndexes(List<Index> indexes) {
        Terrain.getInstance().setCorners(indexes);
        Webgl.instance.fillBuffers();
    }

    @Override
    protected void onDump(List<Index> indexes, Logger logger) {
        String string = "public static List<Index> corners = Arrays.asList(";
        for (int i = 0; i < indexes.size(); i++) {
            Index index = indexes.get(i);
            string += index.testString();
            if(i + 1 < indexes.size()) {
                string += ", ";
            }
        }
        string += ");";
        logger.severe(string);
    }
}
