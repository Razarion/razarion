package com.btxtech.client.editor;

import com.btxtech.client.editor.generic.GenericEditorFrontendProvider;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.shared.system.perfmon.PerfmonStatistic;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.renderer.BabylonTerrainTile;
import com.btxtech.uiservice.terrain.TerrainUiService;
import elemental2.promise.Promise;
import jsinterop.annotations.JsType;

import javax.inject.Singleton;
import javax.inject.Inject;
import java.util.stream.Collectors;

@JsType
@Singleton
public class EditorFrontendProvider {

    private GenericEditorFrontendProvider genericEditorFrontendProvider;

    private PerfmonService perfmonService;

    private GameEngineControl gameEngineControl;

    private TerrainEditorService terrainEditorService;

    private TerrainUiService terrainUiService;

    @Inject
    public EditorFrontendProvider(TerrainUiService terrainUiService, TerrainEditorService terrainEditorService, GameEngineControl gameEngineControl, PerfmonService perfmonService, GenericEditorFrontendProvider genericEditorFrontendProvider) {
        this.terrainUiService = terrainUiService;
        this.terrainEditorService = terrainEditorService;
        this.gameEngineControl = gameEngineControl;
        this.perfmonService = perfmonService;
        this.genericEditorFrontendProvider = genericEditorFrontendProvider;
    }

    @SuppressWarnings("unused") // Called by Angular
    public GenericEditorFrontendProvider getGenericEditorFrontendProvider() {
        return genericEditorFrontendProvider;
    }

    @SuppressWarnings("unused") // Called by Angular
    public PerfmonStatistic[] getClientPerfmonStatistics() {
        return perfmonService.peekClientPerfmonStatistics().toArray(new PerfmonStatistic[0]);
    }

    @SuppressWarnings("unused") // Called by Angular
    public Promise<PerfmonStatistic[]> getWorkerPerfmonStatistics() {
        return new Promise<>((resolve, reject) -> gameEngineControl.perfmonRequest(perfmonStatistics -> resolve.onInvoke(perfmonStatistics.toArray(new PerfmonStatistic[0]))));
    }

    @SuppressWarnings("unused") // Called by Angular
    public TerrainEditorService getTerrainEditorService() {
        return terrainEditorService;
    }
}
