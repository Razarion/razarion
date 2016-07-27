package com.btxtech.client.editor.menu;

import com.btxtech.client.renderer.engine.ClientRenderServiceImpl;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 07.11.2015.
 */
@Templated("DebugMenu.html#menu-debug")
public class DebugMenu extends Composite {
    @Inject
    private ClientRenderServiceImpl renderService;
    @Inject
    @DataField
    private CheckBox showMonitor;
    @Inject
    @DataField
    private CheckBox showDeepMap;
    @Inject
    @DataField
    private CheckBox wireMode;
    @Inject
    @DataField
    private CheckBox showNorm;

    @PostConstruct
    public void init() {
        showMonitor.setValue(renderService.isShowMonitor());
        showDeepMap.setValue(renderService.isShowDeep());
        wireMode.setValue(renderService.isWire());
        showNorm.setValue(renderService.isShowNorm());
    }

    @EventHandler("showMonitor")
    public void showMonitorChanged(ChangeEvent e) {
        renderService.setShowMonitor(showMonitor.getValue());
    }

    @EventHandler("showDeepMap")
    public void showDeepMapChanged(ChangeEvent e) {
        renderService.setShowDeep(showDeepMap.getValue());
    }

    @EventHandler("wireMode")
    public void wireModeChanged(ChangeEvent e) {
        // TODO renderService.showWire(wireMode.getValue());
    }

    @EventHandler("showNorm")
    public void showNormChanged(ChangeEvent e) {
        renderService.setShowNorm(showNorm.getValue());
    }

}
