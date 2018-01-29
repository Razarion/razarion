package com.btxtech.common;

import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShapeAccess;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShape;
import com.btxtech.shared.CommonUrl;
import com.google.gwt.xhr.client.XMLHttpRequest;
import elemental.js.util.Xhr;
import elemental.json.Json;
import elemental.json.JsonObject;

import javax.enterprise.context.ApplicationScoped;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 28.06.2017.
 */
@ApplicationScoped
public class ClientNativeTerrainShapeAccess implements NativeTerrainShapeAccess {
    @Override
    public void load(int planetId, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback) {
        Xhr.get(CommonUrl.terrainShapeProvider(planetId), new Xhr.Callback() {
            @Override
            public void onFail(XMLHttpRequest xhr) {
                failCallback.accept("TerrainShapeProvider call failed: " + xhr.getStatusText() + " Status: " + xhr.getStatus());
            }

            @Override
            public void onSuccess(XMLHttpRequest xhr) {
                loadedCallback.accept(cast(Json.parse(xhr.getResponseText())));
            }
        });
    }

    private native NativeTerrainShape cast(JsonObject jsonObject) /*-{
        return jsonObject;
    }-*/;
}
