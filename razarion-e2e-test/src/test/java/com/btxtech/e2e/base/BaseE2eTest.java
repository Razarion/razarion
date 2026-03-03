package com.btxtech.e2e.base;

import com.btxtech.e2e.config.WebDriverConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;

@ExtendWith(E2eTestWatcher.class)
public abstract class BaseE2eTest {

    protected WebDriver driver;

    @BeforeEach
    void setUp() {
        driver = WebDriverConfig.createDriver();
    }

    protected void navigateTo(String path) {
        String url = WebDriverConfig.getBaseUrl() + (path.startsWith("/") ? path : "/" + path);
        driver.get(url);
    }
}
