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
    @JsProperty(namespace = JsPackage.GLOBAL)
    public static native FB getFB();

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
}
