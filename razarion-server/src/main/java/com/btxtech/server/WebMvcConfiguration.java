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
    }
}
