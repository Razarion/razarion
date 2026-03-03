package com.btxtech.e2e.base;

import com.btxtech.e2e.config.WebDriverConfig;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.nio.file.Files;
import java.nio.file.Path;

public class E2eTestWatcher implements BeforeTestExecutionCallback, AfterEachCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(E2eTestWatcher.class);

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        if (WebDriverConfig.isRecording()) {
            BaseE2eTest test = (BaseE2eTest) context.getRequiredTestInstance();
            if (test.driver != null) {
                String testName = context.getRequiredTestClass().getSimpleName()
                        + "_" + context.getRequiredTestMethod().getName();
                Path dir = Path.of("target", "e2e-reports", "recordings", testName);
                BrowserRecorder recorder = new BrowserRecorder(test.driver, dir);
                context.getStore(NAMESPACE).put("recorder", recorder);
                recorder.start();
            }
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        BaseE2eTest test = (BaseE2eTest) context.getRequiredTestInstance();
        try {
            if (context.getExecutionException().isPresent() && test.driver != null) {
                saveScreenshot(test.driver, context);
            }
        } finally {
            try {
                BrowserRecorder recorder = context.getStore(NAMESPACE).remove("recorder", BrowserRecorder.class);
                if (recorder != null) {
                    recorder.stop();
                }
            } finally {
                if (test.driver != null) {
                    test.driver.quit();
                }
            }
        }
    }

    private void saveScreenshot(WebDriver driver, ExtensionContext context) {
        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            String testName = context.getRequiredTestClass().getSimpleName()
                    + "_" + context.getRequiredTestMethod().getName();
            Path dir = Path.of("target", "e2e-reports", "screenshots");
            Files.createDirectories(dir);
            Files.write(dir.resolve(testName + ".png"), screenshot);
            System.out.println("Screenshot saved: " + dir.resolve(testName + ".png"));
        } catch (Exception e) {
            System.err.println("Failed to save screenshot: " + e.getMessage());
        }
    }
}
