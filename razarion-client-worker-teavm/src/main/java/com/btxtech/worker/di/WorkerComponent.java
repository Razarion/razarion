package com.btxtech.worker.di;

import com.btxtech.worker.TeaVMClientGameEngineWorker;
import dagger.Component;

import jakarta.inject.Singleton;

/**
 * Dagger component for the TeaVM Web Worker
 * This is the entry point for dependency injection
 */
@Singleton
@Component(modules = WorkerModule.class)
public interface WorkerComponent {

    /**
     * Returns the main game engine worker instance
     * This is the primary entry point for the worker functionality
     */
    TeaVMClientGameEngineWorker clientGameEngineWorker();
}
