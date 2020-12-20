package com.btxtech.client.editor.terrain.renderer;

import com.btxtech.client.editor.terrain.EditorSlopeWrapper;
import com.btxtech.client.editor.terrain.TerrainEditorService;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.nativejs.NativeMatrixFactory;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.AbstractRenderTaskRunner;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 13.01.2017.
 */
@ApplicationScoped
public class TerrainEditorRenderTaskRunner extends AbstractRenderTaskRunner {
    @Inject
    private TerrainEditorService terrainEditor;
    @Inject
    private NativeMatrixFactory nativeMatrixFactory;
    private TerrainEditorCursorRenderTask cursorRendererTask;
    private Map<EditorSlopeWrapper, TerrainEditorSlopeRenderTask> slopeRenderers = new HashMap<>();
    private Map<EditorSlopeWrapper, TerrainEditorSlopeDrivewayRenderTask> slopeDrivewayRenderers = new HashMap<>();

    public void activate(Polygon2D cursor, Collection<EditorSlopeWrapper> modifiedSlopes) {
        destroyRenderAllTasks();
        setupCursor(cursor);
        setupModifiedSlopes(modifiedSlopes);
        setupModifiedTerrainObject();
    }

    public void deactivate() {
        cursorRendererTask = null;
        slopeRenderers.clear();
        slopeDrivewayRenderers.clear();
        destroyRenderAllTasks();
    }

    public void changeCursor(Polygon2D cursor) {
        cursorRendererTask.changeBuffers(cursor);
    }

    public void updateSlope(EditorSlopeWrapper modifiedSlope) {
        slopeRenderers.get(modifiedSlope).fillBuffers(modifiedSlope);
        slopeDrivewayRenderers.get(modifiedSlope).fillBuffers(modifiedSlope);
    }

    public void newSlope(EditorSlopeWrapper modifiedSlope) {
        setupModifiedSlope(modifiedSlope);
        setupModifiedSlopeDriveways(modifiedSlope);
    }

    public void removeSlope(EditorSlopeWrapper modifiedSlope) {
        destroyRenderTask(slopeRenderers.remove(modifiedSlope));
        destroyRenderTask(slopeDrivewayRenderers.remove(modifiedSlope));
    }

    private void setupCursor(Polygon2D cursor) {
        cursorRendererTask = createModelRenderTask(TerrainEditorCursorRenderTask.class,
                cursor,
                timeStamp -> Collections.singletonList(new ModelMatrices(nativeMatrixFactory.createFromColumnMajorArray(terrainEditor.getCursorModelMatrix().toWebGlArray()))),
                null,
                null,
                null);
    }

    private void setupModifiedSlopes(Collection<EditorSlopeWrapper> modifiedSlopes) {
        for (EditorSlopeWrapper modifiedSlope : modifiedSlopes) {
            setupModifiedSlope(modifiedSlope);
            setupModifiedSlopeDriveways(modifiedSlope);
        }
    }

    private void setupModifiedSlope(EditorSlopeWrapper modifiedSlope) {
        slopeRenderers.put(modifiedSlope,
                createRenderTask(TerrainEditorSlopeRenderTask.class, modifiedSlope));
    }

    private void setupModifiedSlopeDriveways(EditorSlopeWrapper modifiedSlope) {
        slopeDrivewayRenderers.put(modifiedSlope,
                createRenderTask(TerrainEditorSlopeDrivewayRenderTask.class, modifiedSlope));
    }

    private void setupModifiedTerrainObject() {
        createModelRenderTask(TerrainEditorTerrainObjectRendererTask.class,
                null,
                timeStamp -> terrainEditor.provideTerrainObjectModelMatrices(),
                null,
                null,
                null);
    }

}
