package com.btxtech.client.editor;

import com.btxtech.client.editor.generic.GenericEditorFrontendProvider;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.shared.system.perfmon.PerfmonStatistic;
import jsinterop.annotations.JsType;

import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;

@JsType
@ApplicationScoped
public class EditorFrontendProvider {
    @Inject
    private GenericEditorFrontendProvider genericEditorFrontendProvider;
    @Inject
    private PerfmonService perfmonService;

    @SuppressWarnings("unused") // Called by Angular
    public GenericEditorFrontendProvider getGenericEditorFrontendProvider() {
        return genericEditorFrontendProvider;
    }

    @SuppressWarnings("unused") // Called by Angular
    public PerfmonStatistic[] getPerfmonStatistics() {
        return perfmonService.peekClientPerfmonStatistics().toArray(new PerfmonStatistic[0]);
    }

}
