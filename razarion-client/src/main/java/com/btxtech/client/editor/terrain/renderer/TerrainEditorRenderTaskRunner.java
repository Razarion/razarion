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
    private TerrainEditorSlopeCursorRenderTask slopeCursorRendererTask;
    private Map<EditorSlopeWrapper, TerrainEditorSlopeRenderTask> slopeRenderers = new HashMap<>();
    private Map<EditorSlopeWrapper, TerrainEditorSlopeDrivewayRenderTask> slopeDrivewayRenderers = new HashMap<>();
    private TerrainEditorTerrainObjectRendererTask terrainObjectRenderer;

    public void activate(Polygon2D cursor, Collection<EditorSlopeWrapper> modifiedSlopes, boolean slopeMode) {
        destroyRenderAllTasks();
        setupSlopeCursor(cursor);
        setupModifiedSlopes(modifiedSlopes);
        setupModifiedTerrainObject();
        setSlopeMode(slopeMode);
    }

    public void deactivate() {
        slopeCursorRendererTask = null;
        slopeRenderers.clear();
        slopeDrivewayRenderers.clear();
        terrainObjectRenderer = null;
        destroyRenderAllTasks();
    }

    public void setSlopeMode(boolean slopeMode) {
        slopeCursorRendererTask.setActive(slopeMode);
        slopeRenderers.values().forEach(render -> render.setActive(slopeMode));
        slopeDrivewayRenderers.values().forEach(render -> render.setActive(slopeMode));
        terrainObjectRenderer.setActive(!slopeMode);
    }

    public void changeCursor(Polygon2D cursor) {
        slopeCursorRendererTask.changeBuffers(cursor);
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

    private void setupSlopeCursor(Polygon2D cursor) {
        slopeCursorRendererTask = createModelRenderTask(TerrainEditorSlopeCursorRenderTask.class,
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
        terrainObjectRenderer = createModelRenderTask(TerrainEditorTerrainObjectRendererTask.class,
                null,
                timeStamp -> terrainEditor.provideTerrainObjectModelMatrices(),
                null,
                null,
                null);
    }

}
