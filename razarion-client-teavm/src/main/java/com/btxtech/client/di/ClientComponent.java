package com.btxtech.client.di;

import com.btxtech.client.TeaVMGwtAngularService;
import com.btxtech.client.TeaVMLifecycleService;
import dagger.Component;

import jakarta.inject.Singleton;

@Singleton
@Component(modules = ClientModule.class)
public interface ClientComponent {

    TeaVMGwtAngularService gwtAngularService();

    TeaVMLifecycleService lifecycleService();
}
