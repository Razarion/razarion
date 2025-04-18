package com.btxtech.server.service.engine;

public class NoTerrainShapeException extends RuntimeException {
    public NoTerrainShapeException(Integer planetId) {
        super("No planet for id: " + planetId);
    }
}
