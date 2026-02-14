package com.btxtech.client.jso.facade;

import com.btxtech.uiservice.cockpit.ChatCockpit;
import com.btxtech.uiservice.cockpit.MainCockpit;
import com.btxtech.uiservice.cockpit.QuestCockpit;
import com.btxtech.uiservice.cockpit.ScreenCover;
import com.btxtech.uiservice.dialog.ModelDialogPresenter;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerPresenter;
import com.btxtech.uiservice.renderer.BabylonRenderServiceAccess;
import com.btxtech.uiservice.system.boot.GwtAngularBoot;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * JSO interface to read window.gwtAngularFacade set by Angular
 */
public class JsGwtAngularFacade {

    private static JsGwtAngularFacade INSTANCE;

    private final JSObject facade;

    private JsGwtAngularFacade(JSObject facade) {
        this.facade = facade;
    }

    public static JsGwtAngularFacade get() {
        if (INSTANCE == null) {
            INSTANCE = new JsGwtAngularFacade(getWindowFacade());
        }
        return INSTANCE;
    }

    @JSBody(script = "return window.gwtAngularFacade;")
    private static native JSObject getWindowFacade();

    // --- Angular-provided objects (Angular -> Java direction) ---

    @JSBody(params = {"facade"}, script = "return facade.gwtAngularBoot;")
    private static native JSObject getGwtAngularBootJs(JSObject facade);

    @JSBody(params = {"facade"}, script = "return facade.screenCover;")
    private static native JSObject getScreenCoverJs(JSObject facade);

    @JSBody(params = {"facade"}, script = "return facade.mainCockpit;")
    private static native JSObject getMainCockpitJs(JSObject facade);

    @JSBody(params = {"facade"}, script = "return facade.questCockpit;")
    private static native JSObject getQuestCockpitJs(JSObject facade);

    @JSBody(params = {"facade"}, script = "return facade.chatCockpit;")
    private static native JSObject getChatCockpitJs(JSObject facade);

    @JSBody(params = {"facade"}, script = "return facade.baseItemPlacerPresenter;")
    private static native JSObject getBaseItemPlacerPresenterJs(JSObject facade);

    @JSBody(params = {"facade"}, script = "return facade.babylonRenderServiceAccess;")
    private static native JSObject getBabylonRenderServiceAccessJs(JSObject facade);

    @JSBody(params = {"facade"}, script = "return facade.modelDialogPresenter;")
    private static native JSObject getModelDialogPresenterJs(JSObject facade);

    // --- Java-provided objects (Java -> Angular direction) ---

    @JSBody(params = {"facade", "key", "value"}, script = "facade[key] = value;")
    private static native void setProperty(JSObject facade, String key, JSObject value);

    public void setJavaService(String name, JSObject proxy) {
        setProperty(facade, name, proxy);
    }

    @JSBody(params = {"facade"}, script = "facade.onCrash();")
    private static native void callOnCrash(JSObject facade);

    public void onCrash() {
        callOnCrash(facade);
    }

    // --- Adapter creation for Angular-provided interfaces ---

    @JSFunctor
    public interface VoidCallback extends JSObject {
        void call();
    }

    @JSFunctor
    public interface StringCallback extends JSObject {
        void accept(String value);
    }

    public GwtAngularBoot getGwtAngularBootAdapter() {
        JSObject boot = getGwtAngularBootJs(facade);
        return (onSuccess, onError) -> {
            VoidCallback successCb = onSuccess::run;
            StringCallback errorCb = onError::accept;
            callLoadThreeJsModels(boot, successCb, errorCb);
        };
    }

    @JSBody(params = {"boot", "onSuccess", "onError"}, script =
            "boot.loadThreeJsModels().then(function() { onSuccess.call(); }).catch(function(e) { onError.accept('' + e); });")
    private static native void callLoadThreeJsModels(JSObject boot, VoidCallback onSuccess, StringCallback onError);

    public ScreenCover getScreenCoverAdapter() {
        JSObject sc = getScreenCoverJs(facade);
        return new ScreenCover() {
            @Override public void showStoryCover(String html) { callMethod1S(sc, "showStoryCover", html); }
            @Override public void hideStoryCover() { callMethod0(sc, "hideStoryCover"); }
            @Override public void removeLoadingCover() { callMethod0(sc, "removeLoadingCover"); }
            @Override public void fadeInLoadingCover() { callMethod0(sc, "fadeInLoadingCover"); }
            @Override public void onStartupProgress(double percent) { callMethod1D(sc, "onStartupProgress", percent); }
        };
    }

    public MainCockpit getMainCockpitAdapter() {
        JSObject mc = getMainCockpitJs(facade);
        return new JsMainCockpit(mc);
    }

    public QuestCockpit getQuestCockpitAdapter() {
        JSObject qc = getQuestCockpitJs(facade);
        return new JsQuestCockpit(qc);
    }

    public ChatCockpit getChatCockpitAdapter() {
        JSObject cc = getChatCockpitJs(facade);
        return new JsChatCockpit(cc);
    }

    public BaseItemPlacerPresenter getBaseItemPlacerPresenterAdapter() {
        JSObject bp = getBaseItemPlacerPresenterJs(facade);
        return new JsBaseItemPlacerPresenter(bp);
    }

    public BabylonRenderServiceAccess getBabylonRenderServiceAccessAdapter() {
        JSObject brsa = getBabylonRenderServiceAccessJs(facade);
        return new JsBabylonRenderServiceAccess(brsa);
    }

    public ModelDialogPresenter getModelDialogPresenterAdapter() {
        JSObject mdp = getModelDialogPresenterJs(facade);
        return new JsModelDialogPresenter(mdp);
    }

    // --- Helper methods for calling JS methods ---

    @JSBody(params = {"obj", "method"}, script = "obj[method]();")
    static native void callMethod0(JSObject obj, String method);

    @JSBody(params = {"obj", "method", "arg"}, script = "obj[method](arg);")
    static native void callMethod1(JSObject obj, String method, JSObject arg);

    @JSBody(params = {"obj", "method", "arg"}, script = "obj[method](arg);")
    static native void callMethod1S(JSObject obj, String method, String arg);

    @JSBody(params = {"obj", "method", "arg"}, script = "obj[method](arg);")
    static native void callMethod1D(JSObject obj, String method, double arg);

    @JSBody(params = {"obj", "method", "arg"}, script = "obj[method](arg);")
    static native void callMethod1I(JSObject obj, String method, int arg);

    @JSBody(params = {"obj", "method", "arg"}, script = "obj[method](arg);")
    static native void callMethod1B(JSObject obj, String method, boolean arg);

    @JSBody(params = {"obj", "method", "a1", "a2"}, script = "obj[method](a1, a2);")
    static native void callMethod2(JSObject obj, String method, JSObject a1, JSObject a2);

    @JSBody(params = {"obj", "method", "a1", "a2"}, script = "obj[method](a1, a2);")
    static native void callMethod2D(JSObject obj, String method, double a1, double a2);

    @JSBody(params = {"obj", "method", "a1", "a2"}, script = "return obj[method](a1, a2);")
    static native JSObject callMethod2Return(JSObject obj, String method, JSObject a1, JSObject a2);

    @JSBody(params = {"obj", "method", "a1"}, script = "return obj[method](a1);")
    static native JSObject callMethod1Return(JSObject obj, String method, JSObject arg);

    @JSBody(params = {"obj", "method"}, script = "return obj[method]();")
    static native JSObject callMethod0Return(JSObject obj, String method);
}
