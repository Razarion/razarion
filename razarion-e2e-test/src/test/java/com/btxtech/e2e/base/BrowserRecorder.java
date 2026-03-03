package com.btxtech.e2e.base;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BrowserRecorder {

    private static final int INTERVAL_MS = 250;

    private final WebDriver driver;
    private final Path outputDir;
    private final ScheduledExecutorService scheduler;
    private final AtomicInteger frameCount = new AtomicInteger(0);

    public BrowserRecorder(WebDriver driver, Path outputDir) {
        this.driver = driver;
        this.outputDir = outputDir;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        try {
            Files.createDirectories(outputDir);
        } catch (Exception e) {
            System.err.println("Failed to create recording directory: " + e.getMessage());
            return;
        }
        scheduler.scheduleAtFixedRate(this::captureFrame, 0, INTERVAL_MS, TimeUnit.MILLISECONDS);
        System.out.println("Browser recording started: " + outputDir);
    }

    public void stop() {
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Browser recording stopped: " + frameCount.get() + " frames saved to " + outputDir);
    }

    private void captureFrame() {
        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Path file = outputDir.resolve(String.format("frame_%04d.png", frameCount.getAndIncrement()));
            Files.write(file, screenshot);
        } catch (Exception e) {
            // driver may be closing, ignore
        }
    }
}
