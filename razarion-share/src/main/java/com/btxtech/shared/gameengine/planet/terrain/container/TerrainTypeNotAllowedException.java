package com.btxtech.shared.gameengine.planet.terrain.container;

/**
 * Created by Beat
 * on 21.10.2017.
 */
public class TerrainTypeNotAllowedException extends RuntimeException {
    public TerrainTypeNotAllowedException(String message) {
        super(message);
    }
}
