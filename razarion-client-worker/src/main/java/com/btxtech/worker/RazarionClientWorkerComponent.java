package com.btxtech.worker;

import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = RazarionClientWorkerModule.class)
public interface RazarionClientWorkerComponent {
    ClientGameEngineWorker clientGameEngineWorker();
}