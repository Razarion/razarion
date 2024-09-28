package com.btxtech.client;

import com.btxtech.client.gwtangular.GwtAngularService;
import com.btxtech.client.system.LifecycleService;
import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.DomGlobal;
import org.dominokit.rest.DominoRestConfig;

public class RazarionClient implements EntryPoint {
    @Override
    public void onModuleLoad() {
        DominoRestConfig.initDefaults().setDefaultResourceRootPath("/rest");

        RazarionClientComponent component = DaggerRazarionClientComponent.create();
        GwtAngularService gwtAngularService = component.gwtAngularService();
        LifecycleService lifecycleService = component.lifecycleService();

        gwtAngularService.init();
        lifecycleService.startCold();
    }
}
