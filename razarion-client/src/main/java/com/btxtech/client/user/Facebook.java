package com.btxtech.client.user;

import com.google.gwt.core.client.JavaScriptObject;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 20.02.2017.
 */
public class Facebook {
    @JsType(isNative = true)
    public interface FB {
        void getLoginStatus(LoginStatusCallback loginStatusCallback);
    }

    @JsProperty(namespace = JsPackage.GLOBAL)
    public static native FB getFB();

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
        JavaScriptObject call(FbResponse response);
    }
}
