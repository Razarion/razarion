package com.btxtech.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Tracking pixel - no caching
        registry.addResourceHandler("/t.gif")
                .addResourceLocations("classpath:/homepage/")
                .setCacheControl(CacheControl.noStore());

        // Static assets from homepage folder (images, etc.) - cache for 7 days
        registry.addResourceHandler("/*.jpg", "/*.png", "/*.webp", "/*.ico")
                .addResourceLocations("classpath:/homepage/")
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic());

        // JS files - cache for 1 day
        registry.addResourceHandler("/*.js")
                .addResourceLocations("classpath:/static/", "classpath:/generated/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic());

        // The WASM modules and the TeaVM runtime carry a build stamp in the query (?v=), appended by
        // client-bootstrap.js / worker-bootstrap.js, so a deploy is a new URL and the old one can be
        // kept forever. They used to be cached for an hour under fixed names, which meant a returning
        // player ran the freshly deployed TypeScript against the WASM from before the deploy - and a
        // developer's rebuild was invisible to anything short of a hard reload.
        // Registered before the catch-all handlers below, because the registry resolves in order.
        //
        // Every pattern needs a wildcard in its last segment. Spring resolves a request by taking
        // the part of the path that matched the wildcard and looking that up inside the location -
        // an exact pattern leaves nothing to look up, and the request 404s with a JSON error body
        // that the browser then refuses to execute as a script.
        CacheControl versioned = CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable();
        registry.addResourceHandler("/teavm-worker/*.wasm", "/teavm-worker/*.wasm-runtime.js")
                .addResourceLocations("classpath:/generated/teavm-worker/")
                .setCacheControl(versioned);
        registry.addResourceHandler("/teavm-client/*.wasm", "/teavm-client/*.wasm-runtime.js")
                .addResourceLocations("classpath:/generated/teavm-client/")
                .setCacheControl(versioned);

        // The bootstraps themselves, plus source maps and anything else in those folders. These are
        // small and carry the build stamp for everything else, so they must never go stale.
        registry.addResourceHandler("/teavm-worker/**")
                .addResourceLocations("classpath:/generated/teavm-worker/")
                .setCacheControl(CacheControl.noCache());
        registry.addResourceHandler("/teavm-client/**")
                .addResourceLocations("classpath:/generated/teavm-client/")
                .setCacheControl(CacheControl.noCache());

        // Angular apps. index.html points at the current hashes, so it must never be cached -
        // otherwise a deployed client keeps asking for chunks that no longer exist. Registered
        // before the asset handlers because the registry resolves in registration order.
        registry.addResourceHandler("/game/index.html", "/studio/index.html")
                .addResourceLocations("classpath:/generated/")
                .setCacheControl(CacheControl.noCache());

        // Everything else the Angular build emits carries a content hash in its file name
        // (main-SMERQXAU.js, chunk-*.js, styles-*.css), so a new build is a new URL and the old
        // one can be kept forever. Without this the files went out with no Cache-Control at all
        // and every game start re-downloaded the full bundle.
        CacheControl immutable = CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable();
        registry.addResourceHandler("/game/*.js", "/game/*.css")
                .addResourceLocations("classpath:/generated/game/")
                .setCacheControl(immutable);
        registry.addResourceHandler("/studio/*.js", "/studio/*.css")
                .addResourceLocations("classpath:/generated/studio/")
                .setCacheControl(immutable);
    }
}
