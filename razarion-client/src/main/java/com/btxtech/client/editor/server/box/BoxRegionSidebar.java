package com.btxtech.client.editor.server.box;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.client.editor.framework.CrudEditor;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.BoxRegionConfig;
import com.btxtech.shared.rest.ServerGameEngineControlProvider;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * on 28.07.2017.
 */
@Templated("../../framework/AbstractCrudeParentSidebar.html#abstract-crud-parent")
public class BoxRegionSidebar extends AbstractCrudeParentSidebar<BoxRegionConfig, BoxRegionPropertyPanel> {
    // private Logger logger = Logger.getLogger(BoxRegionSidebar.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private Caller<ServerGameEngineControlProvider> provider;
    @Inject
    private Instance<BoxRegionPropertyPanel> propertyPanelInstance;
    @Inject
    private BoxRegionCrudEditor boxRegionCrudEditor;

    @Override
    public void onConfigureDialog() {
        super.onConfigureDialog();
        getEditorPanel().addButton("Restart", () -> provider.call(ignore -> {
        }, exceptionHandler.restErrorHandler("Calling ServerGameEngineControlProvider.restartBoxRegions() failed: ")).restartBoxRegions());
    }

    @Override
    protected CrudEditor<BoxRegionConfig> getCrudEditor() {
        return boxRegionCrudEditor;
    }

    @Override
    protected BoxRegionPropertyPanel createPropertyPanel() {
        return propertyPanelInstance.get();
    }
}
