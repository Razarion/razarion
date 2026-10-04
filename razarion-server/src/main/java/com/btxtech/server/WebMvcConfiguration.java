package com.btxtech.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;
import com.btxtech.server.web.ContentEtagGenerator;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    /**
     * Jib stamps every file in the image with 1970-01-01 00:00:01, and Spring sent that as Last-Modified:
     * "changed since 1970?" was answered 304 after every deploy, for the sprite sheets as for
     * /game/index.html (PROD, 2026-10-02). So no handler here sends Last-Modified; the ones whose
     * file names do not change get an ETag from the content instead. The handlers Spring Boot
     * registers itself are switched off from Last-Modified in application.properties.
     */
    private final ContentEtagGenerator contentEtag = new ContentEtagGenerator();

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // The tracking pixel is NOT registered here. TrackingPixelController maps /t.gif for POST,
        // and a controller mapping claims its URL for every method - a resource handler for the same
        // path can never be reached, the GET is refused as a wrong method instead. The controller
        // serves the image itself.

        // Static assets from homepage folder (images, etc.) - cache for 7 days
        registry.addResourceHandler("/*.jpg", "/*.png", "/*.webp", "/*.ico")
                .addResourceLocations("classpath:/homepage/")
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                .setUseLastModified(false)
                .setEtagGenerator(contentEtag);

        // JS files - cache for 1 day
        registry.addResourceHandler("/*.js")
                .addResourceLocations("classpath:/static/", "classpath:/generated/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
                .setUseLastModified(false)
                .setEtagGenerator(contentEtag);

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
                .setCacheControl(versioned)
                .setUseLastModified(false);
        registry.addResourceHandler("/teavm-client/*.wasm", "/teavm-client/*.wasm-runtime.js")
                .addResourceLocations("classpath:/generated/teavm-client/")
                .setCacheControl(versioned)
                .setUseLastModified(false);

        // The bootstraps themselves, plus source maps and anything else in those folders. These are
        // small and carry the build stamp for everything else, so they must never go stale.
        registry.addResourceHandler("/teavm-worker/**")
                .addResourceLocations("classpath:/generated/teavm-worker/")
                .setCacheControl(CacheControl.noCache())
                .setUseLastModified(false)
                .setEtagGenerator(contentEtag);
        registry.addResourceHandler("/teavm-client/**")
                .addResourceLocations("classpath:/generated/teavm-client/")
                .setCacheControl(CacheControl.noCache())
                .setUseLastModified(false)
                .setEtagGenerator(contentEtag);

        // Angular apps. index.html points at the current hashes, so it must never be cached -
        // otherwise a deployed client keeps asking for chunks that no longer exist. Registered
        // before the asset handlers because the registry resolves in registration order.
        registry.addResourceHandler("/game/index.html", "/studio/index.html")
                .addResourceLocations("classpath:/generated/")
                .setCacheControl(CacheControl.noCache())
                .setUseLastModified(false)
                .setEtagGenerator(contentEtag);

        // Everything else the Angular build emits carries a content hash in its file name
        // (main-SMERQXAU.js, chunk-*.js, styles-*.css), so a new build is a new URL and the old
        // one can be kept forever. Without this the files went out with no Cache-Control at all
        // and every game start re-downloaded the full bundle.
        CacheControl immutable = CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable();
        //
        // The build also writes a brotli copy next to each of them (scripts/precompress.mjs), 19%
        // smaller than the gzip the server made on the fly - half a megabyte of the 2.5 MB a phone
        // waits for before the engine can run. EncodedResourceResolver hands it to a browser that
        // asks for br, the plain file to the rest, and sets Vary: Accept-Encoding so the CDN keeps
        // the two apart. Tomcat leaves a response alone that already carries a Content-Encoding.
        registry.addResourceHandler("/game/*.js", "/game/*.css")
                .addResourceLocations("classpath:/generated/game/")
                .setCacheControl(immutable)
                .setUseLastModified(false)
                .resourceChain(true)
                .addResolver(new EncodedResourceResolver())
                .addResolver(new PathResourceResolver());
        registry.addResourceHandler("/studio/*.js", "/studio/*.css")
                .addResourceLocations("classpath:/generated/studio/")
                .setCacheControl(immutable)
                .setUseLastModified(false)
                .resourceChain(true)
                .addResolver(new EncodedResourceResolver())
                .addResolver(new PathResourceResolver());

        // Everything else the Angular apps load under a fixed name: sprite sheets, the environment
        // map, images from public/. They change with a build and keep their name, so the browser has
        // to ask every time - and gets a 304 only when the content is the same. They went out with no
        // Cache-Control at all, which the browser answered with a guess from a Last-Modified of 1970:
        // years. The more specific patterns above still win for the hashed files and index.html.
        registry.addResourceHandler("/game/**")
                .addResourceLocations("classpath:/generated/game/")
                .setCacheControl(CacheControl.noCache())
                .setUseLastModified(false)
                .setEtagGenerator(contentEtag);
        registry.addResourceHandler("/studio/**")
                .addResourceLocations("classpath:/generated/studio/")
                .setCacheControl(CacheControl.noCache())
                .setUseLastModified(false)
                .setEtagGenerator(contentEtag);
    }
}
