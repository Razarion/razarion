package com.btxtech.client;

import com.btxtech.client.gwtangular.GwtAngularService;
import com.btxtech.client.system.LifecycleService;
import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.DomGlobal;

public class RazarionClient implements EntryPoint {
    @Override
    public void onModuleLoad() {
        DomGlobal.console.error("RazarionClient init");

        RazarionClientComponent component = DaggerRazarionClientComponent.create();
        GwtAngularService gwtAngularService = component.gwtAngularService();
        LifecycleService lifecycleService = component.lifecycleService();

        DomGlobal.console.error("RazarionClient start");
        gwtAngularService.init();
        lifecycleService.startCold();
    }
}
