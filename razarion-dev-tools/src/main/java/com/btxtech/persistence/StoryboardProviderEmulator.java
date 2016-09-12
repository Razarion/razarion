package com.btxtech.persistence;

import com.btxtech.shared.dto.StoryboardConfig;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 07.07.2016.
 */
@Singleton
public class StoryboardProviderEmulator {
    private static final String DEV_TOOL_RESOURCE_DIR = "C:\\dev\\projects\\razarion\\code\\tmp";
    private static final String FILE_NAME = "StoryboardConfig.json";
    private static final String URL = "http://localhost:8080/razarion-server/rest/storyboard/";
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private Logger logger;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;

    public StoryboardConfig readFromServer() {
        return ClientBuilder.newClient().target(URL).request(MediaType.APPLICATION_JSON).get(StoryboardConfig.class);
    }

    public void fromServerToFile(String fileName, String url) {
        try {
            Client client = ClientBuilder.newClient();
            String text = client.target(url).request(MediaType.APPLICATION_JSON).get(String.class);
            Files.write(getFile(fileName).toPath(), text.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void fromServerToFile() {
        fromServerToFile(FILE_NAME, URL);
    }

    private File getFile(String fileName) {
        return new File(DEV_TOOL_RESOURCE_DIR, fileName);
    }

    private FileWriter getFileWriter(String fileName) throws IOException {
        return new FileWriter(getFile(fileName));
    }

    private FileReader getFileReader(String fileName) throws IOException {
        return new FileReader(getFile(fileName));
    }
}
