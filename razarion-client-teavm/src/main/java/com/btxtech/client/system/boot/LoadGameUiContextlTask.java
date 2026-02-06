package com.btxtech.client.system.boot;

import com.btxtech.client.jso.JsConsole;
import com.btxtech.client.jso.JsJson;
import com.btxtech.client.jso.JsObject;
import com.btxtech.client.JwtHelper;
import com.btxtech.client.TeaVMClientMarshaller;
import com.btxtech.client.rest.JsonDeserializer;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.BootContext;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

public class LoadGameUiContextlTask extends AbstractStartupTask {
    private final BootContext bootContext;

    public LoadGameUiContextlTask(BootContext bootContext) {
        this.bootContext = bootContext;
    }

    @Override
    protected void privateStart(final DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();

        String bearerToken = JwtHelper.getBearerTokenFromLocalStorage();

        StringCallback onSuccess = text -> {
            try {
                JsObject json = JsJson.parseObject(text);
                // Store raw JSON for worker forwarding (avoids re-serializing StaticGameConfig etc.)
                TeaVMClientMarshaller.storeRawColdContext(json);
                ColdGameUiContext ctx = JsonDeserializer.deserializeColdGameUiContext(json);
                bootContext.getGameUiControl().setColdGameUiContext(ctx);
                deferredStartup.finished();
            } catch (Throwable throwable) {
                JsConsole.error("LoadGameUiContextlTask failed: " + throwable.getMessage());
                deferredStartup.failed(throwable);
            }
        };

        StringCallback onError = errorMsg -> {
            JsConsole.error("LoadGameUiContextlTask fetch failed: " + errorMsg);
            deferredStartup.failed("LoadGameUiContextlTask fetch failed: " + errorMsg);
        };

        if (bearerToken != null) {
            fetchJson("/rest/game-ui-context-control/cold", bearerToken, onSuccess, onError);
        } else {
            fetchJsonNoAuth("/rest/game-ui-context-control/cold", onSuccess, onError);
        }
    }

    @JSFunctor
    public interface StringCallback extends JSObject {
        void call(String value);
    }

    @JSBody(params = {"url", "token", "onSuccess", "onError"}, script =
            "fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/json', 'Accept': 'application/json', 'Authorization': 'Bearer ' + token }, body: null })" +
            ".then(function(response) { if (response.ok) { return response.text(); } else { throw new Error('HTTP ' + response.status); } })" +
            ".then(function(text) { onSuccess(text); })" +
            ".catch(function(error) { onError('' + error); });")
    private static native void fetchJson(String url, String token, StringCallback onSuccess, StringCallback onError);

    @JSBody(params = {"url", "onSuccess", "onError"}, script =
            "fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' }, body: null })" +
            ".then(function(response) { if (response.ok) { return response.text(); } else { throw new Error('HTTP ' + response.status); } })" +
            ".then(function(text) { onSuccess(text); })" +
            ".catch(function(error) { onError('' + error); });")
    private static native void fetchJsonNoAuth(String url, StringCallback onSuccess, StringCallback onError);
}
