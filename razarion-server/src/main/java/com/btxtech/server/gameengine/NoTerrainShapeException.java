package com.btxtech.server.gameengine;

public class NoTerrainShapeException extends RuntimeException {
    public NoTerrainShapeException(Integer planetId) {
        super("No planet for id: " + planetId);
    }
}
