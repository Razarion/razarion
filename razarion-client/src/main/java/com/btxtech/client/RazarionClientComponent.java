package com.btxtech.client;

import com.btxtech.client.gwtangular.GwtAngularService;
import com.btxtech.client.system.LifecycleService;
import dagger.Component;

import jakarta.inject.Singleton;

@Singleton
@Component(modules = RazarionClientModule.class)
public interface RazarionClientComponent {
    LifecycleService lifecycleService();

    GwtAngularService gwtAngularService();
}