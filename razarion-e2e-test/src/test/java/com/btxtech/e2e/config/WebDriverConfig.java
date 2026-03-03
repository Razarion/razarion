package com.btxtech.e2e.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

public class WebDriverConfig {

    private static final String DEFAULT_BASE_URL = "http://localhost:8080";

    public static WebDriver createDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        if (isHeadless()) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        ChromeDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

        return driver;
    }

    public static String getBaseUrl() {
        String url = System.getProperty("e2e.baseUrl", DEFAULT_BASE_URL);
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static boolean isHeadless() {
        return Boolean.parseBoolean(System.getProperty("e2e.headless", "true"));
    }

    public static boolean isRecording() {
        return Boolean.parseBoolean(System.getProperty("e2e.record", "false"));
    }
}
