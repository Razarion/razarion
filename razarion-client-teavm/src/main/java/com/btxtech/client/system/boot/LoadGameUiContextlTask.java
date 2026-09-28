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

        fetchJson("/rest/game-ui-context-control/cold", bearerToken, onSuccess, onError);
    }

    @JSFunctor
    public interface StringCallback extends JSObject {
        void call(String value);
    }

    /**
     * Fetches the cold game context, and tries again where another try can succeed.
     * <p>
     * The first failure used to end the start: 58 sessions on PROD 2026-09-20..27 failed here with
     * "TypeError: Failed to fetch", 0.15 s into the request, and the game reported itself failed a
     * second later - not one of them reached the game. Same rule as the WASM download in
     * client-bootstrap.js: three attempts, 1 s and 3 s apart, for a dead connection or a server
     * error, and never for a 4xx, which the next attempt would only repeat. Every retry is logged
     * as a warning, which the console hook forwards to the server, so it can be counted.
     */
    @JSBody(params = {"url", "token", "onSuccess", "onError"}, script =
            "var headers = { 'Content-Type': 'application/json', 'Accept': 'application/json' };" +
            "if (token) { headers['Authorization'] = 'Bearer ' + token; }" +
            "var attempts = 3;" +
            "function attempt(i) {" +
            "  fetch(url, { method: 'POST', headers: headers, body: null })" +
            "    .then(function(response) { if (response.ok) { return response.text(); } var e = new Error('HTTP ' + response.status); e.status = response.status; throw e; })" +
            "    .then(function(text) {" +
            "      if (i > 1) { console.warn('LoadGameUiContextlTask recovered on attempt ' + i); }" +
            "      onSuccess(text);" +
            "    })" +
            "    .catch(function(error) {" +
            "      var retryable = error && error.status ? error.status >= 500 : true;" +
            "      if (i < attempts && retryable) {" +
            "        console.warn('LoadGameUiContextlTask attempt ' + i + ' failed, retrying: ' + error);" +
            "        setTimeout(function() { attempt(i + 1); }, i * 2000 - 1000);" +
            "      } else {" +
            "        onError((i > 1 ? 'after ' + i + ' attempts: ' : '') + error);" +
            "      }" +
            "    });" +
            "}" +
            "attempt(1);")
    private static native void fetchJson(String url, String token, StringCallback onSuccess, StringCallback onError);
}
