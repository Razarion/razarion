package com.btxtech.client.editor.terrain.renderer;

import com.btxtech.client.editor.terrain.ModifiedSlope;
import com.btxtech.client.editor.terrain.TerrainEditorService;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.RenderUnitControl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 13.01.2017.
 */
@ApplicationScoped
public class TerrainEditorRenderTask extends AbstractRenderTask<Void> {
    @Inject
    private TerrainEditorService terrainEditor;
    private boolean active;
    private TerrainEditorCursorRenderUnit cursorRenderer;
    private Map<ModifiedSlope, TerrainEditorSlopeRenderUnit> slopeRenderers = new HashMap<>();
    private Map<ModifiedSlope, TerrainEditorSlopeDrivewayRenderUnit> slopeDrivewayRenderers = new HashMap<>();

    @Override
    protected boolean isActive() {
        return active;
    }

    public void activate(Polygon2D cursor, Collection<ModifiedSlope> modifiedSlopes) {
        clear();
        setupCursor(cursor);
        setupModifiedSlopes(modifiedSlopes);
        setupModifiedTerrainObject();
        active = true;
    }

    public void deactivate() {
        active = false;
        cursorRenderer = null;
        slopeRenderers.clear();
        slopeDrivewayRenderers.clear();
        clear();
    }

    public void changeCursor(Polygon2D cursor) {
        cursorRenderer.fillBuffers(cursor);
    }

    public void updateSlope(ModifiedSlope modifiedSlope) {
        slopeRenderers.get(modifiedSlope).update();
        slopeDrivewayRenderers.get(modifiedSlope).update();
    }

    public void newSlope(ModifiedSlope modifiedSlope) {
        setupModifiedSlope(modifiedSlope);
        setupModifiedSlopeDriveways(modifiedSlope);
    }

    public void removeSlope(ModifiedSlope modifiedSlope) {
        remove(slopeRenderers.remove(modifiedSlope).getRenderComposite().getModelRenderer());
        remove(slopeDrivewayRenderers.remove(modifiedSlope).getRenderComposite().getModelRenderer());
    }

    private void setupCursor(Polygon2D cursor) {
        ModelRenderer<Polygon2D> modelRenderer = create();
        CommonRenderComposite<TerrainEditorCursorRenderUnit, Polygon2D> renderComposite = modelRenderer.create();
        renderComposite.init(cursor);
        cursorRenderer = renderComposite.setRenderUnit(TerrainEditorCursorRenderUnit.class);
        modelRenderer.add(RenderUnitControl.SELECTION_FRAME, renderComposite);
        add(modelRenderer);
        renderComposite.fillBuffers();
    }

    private void setupModifiedSlopes(Collection<ModifiedSlope> modifiedSlopes) {
        for (ModifiedSlope modifiedSlope : modifiedSlopes) {
            setupModifiedSlope(modifiedSlope);
            setupModifiedSlopeDriveways(modifiedSlope);
        }
    }

    private void setupModifiedSlope(ModifiedSlope modifiedSlope) {
        ModelRenderer<ModifiedSlope> modelRenderer = create();
        CommonRenderComposite<TerrainEditorSlopeRenderUnit, ModifiedSlope> renderComposite = modelRenderer.create();
        renderComposite.init(modifiedSlope);
        TerrainEditorSlopeRenderUnit slopeRenderer = renderComposite.setRenderUnit(TerrainEditorSlopeRenderUnit.class);
        slopeRenderers.put(modifiedSlope, slopeRenderer);
        modelRenderer.add(RenderUnitControl.SELECTION_FRAME, renderComposite);
        add(modelRenderer);
        renderComposite.fillBuffers();
    }

    private void setupModifiedSlopeDriveways(ModifiedSlope modifiedSlope) {
        ModelRenderer<ModifiedSlope> modelRenderer = create();
        CommonRenderComposite<TerrainEditorSlopeDrivewayRenderUnit, ModifiedSlope> renderComposite = modelRenderer.create();
        renderComposite.init(modifiedSlope);
        TerrainEditorSlopeDrivewayRenderUnit drivewayRenderer = renderComposite.setRenderUnit(TerrainEditorSlopeDrivewayRenderUnit.class);
        slopeDrivewayRenderers.put(modifiedSlope, drivewayRenderer);
        modelRenderer.add(RenderUnitControl.START_POINT_CIRCLE, renderComposite);
        add(modelRenderer);
        renderComposite.fillBuffers();
    }

    private void setupModifiedTerrainObject() {
        ModelRenderer<Void> modelRenderer = create();
        modelRenderer.init(null, timeStamp -> terrainEditor.provideTerrainObjectModelMatrices());
        CommonRenderComposite<TerrainEditorTerrainObjectRendererUnit, Void> renderComposite = modelRenderer.create();
        renderComposite.init(null);
        renderComposite.setRenderUnit(TerrainEditorTerrainObjectRendererUnit.class);
        modelRenderer.add(RenderUnitControl.SELECTION_FRAME, renderComposite);
        add(modelRenderer);
        renderComposite.fillBuffers();
    }

}
