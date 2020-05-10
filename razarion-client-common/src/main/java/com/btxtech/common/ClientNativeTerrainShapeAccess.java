package com.btxtech.common;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeAccess;
import com.btxtech.shared.system.ExceptionHandler;
import elemental2.dom.XMLHttpRequest;
import jsinterop.base.Js;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 28.06.2017.
 */
@ApplicationScoped
public class ClientNativeTerrainShapeAccess implements NativeTerrainShapeAccess {
    @Inject
    private ExceptionHandler exceptionHandler;

    @Override
    public void load(int planetId, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback) {
        XMLHttpRequest xmlHttpRequest = new XMLHttpRequest();
        xmlHttpRequest.onload = progressEvent -> {
            try {
                if (xmlHttpRequest.readyState == 4 && xmlHttpRequest.status == 200) {
                    loadedCallback.accept(Js.uncheckedCast(xmlHttpRequest.response));
                } else if (xmlHttpRequest.status == 404) {
                    failCallback.accept("No terrain shape for PlanetId: " + planetId);
                } else {
                    failCallback.accept("TerrainShapeController onload error. Status: '" + xmlHttpRequest.status + "' StatusText: '" + xmlHttpRequest.statusText + "'");
                }
            } catch (Throwable throwable) {
                exceptionHandler.handleException(throwable);
                failCallback.accept(throwable.toString());
            }
        };
        xmlHttpRequest.addEventListener("error", evt -> {
            try {
                failCallback.accept("TerrainShapeController call error. Status: '" + xmlHttpRequest.status + "' StatusText: '" + xmlHttpRequest.statusText + "'");
            } catch (Throwable throwable) {
                exceptionHandler.handleException(throwable);
                failCallback.accept(throwable.toString());
            }
        });
        xmlHttpRequest.onabort = progressEvent -> {
            try {
                failCallback.accept("TerrainShapeController call abort. Status: '" + xmlHttpRequest.status + "' StatusText: '" + xmlHttpRequest.statusText + "'");
            } catch (Throwable throwable) {
                exceptionHandler.handleException(throwable);
                failCallback.accept(throwable.toString());
            }
        };
        xmlHttpRequest.open("GET", CommonUrl.terrainShapeController(planetId));
        xmlHttpRequest.responseType = "json";
        xmlHttpRequest.send();
    }
}
