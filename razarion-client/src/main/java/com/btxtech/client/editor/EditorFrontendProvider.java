package com.btxtech.client.editor;

import com.btxtech.client.editor.generic.GenericEditorFrontendProvider;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.shared.system.perfmon.PerfmonStatistic;
import com.btxtech.uiservice.control.GameEngineControl;
import elemental2.promise.Promise;
import jsinterop.annotations.JsType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@JsType
@ApplicationScoped
public class EditorFrontendProvider {
    @Inject
    private GenericEditorFrontendProvider genericEditorFrontendProvider;
    @Inject
    private PerfmonService perfmonService;
    @Inject
    private GameEngineControl gameEngineControl;
    @Inject
    private TerrainEditorService terrainEditorService;

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
