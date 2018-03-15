package com.btxtech.client.user;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 20.02.2017.
 */
public class Facebook {
    public static final String CONNECTED = "connected";

    @JsProperty(namespace = JsPackage.GLOBAL)
    public static native FB getFB();

    @JsProperty(namespace = JsPackage.GLOBAL, name = "window")
    public static native AppStartLogin getAppStartLogin();

    @JsType(isNative = true)
    public interface FB {
        void getLoginStatus(LoginStatusCallback loginStatusCallback);

        @JsProperty
        XFBML getXFBML();

        @JsProperty(name = "Event")
        Event getEvent();
    }

    @JsType(isNative = true)
    public interface XFBML {
        void parse();
    }

    @JsType(isNative = true)
    public interface Event {
        void subscribe(String event, LoginStatusCallback callback);

        void unsubscribe(String event, LoginStatusCallback callback);
    }

    @JsType(isNative = true)
    public static class FbResponse {
        public String status;
        public FbAuthResponse authResponse;
    }

    @JsType(isNative = true)
    public static class FbAuthResponse {
        public String accessToken;
        public int expiresIn;
        public String signedRequest;
        public String userID;
    }

    @JsFunction
    public interface LoginStatusCallback {
        void call(FbResponse response);
    }

    public static com.btxtech.shared.datatypes.FbAuthResponse toFbAuthResponse(FbAuthResponse authResponse) {
        return new com.btxtech.shared.datatypes.FbAuthResponse().setUserID(authResponse.userID).setAccessToken(authResponse.accessToken).setSignedRequest(authResponse.signedRequest).setExpiresIn(authResponse.expiresIn);
    }

    @JsType(isNative = true)
    public interface AppStartLogin {
        @JsProperty(name = "RAZ_inGameFbAuthResponse")
        FbResponse getFbAuthResponse();

        @JsProperty(name = "RAZ_inGameFbAuthResponse")
        void setFbAuthResponse(FbResponse fbAuthResponse);

        @JsProperty(name = "RAZ_inGameFbAuthResponseCallback")
        void setFbAuthResponseCallback(LoginStatusCallback loginStatusCallback);
    }
}
