package com.btxtech.persistence;

import com.btxtech.shared.dto.GameUiControlConfig;
import com.btxtech.shared.rest.RestUrl;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by Beat
 * 07.07.2016.
 */
@Singleton
public class GameUiControlProviderEmulator {
    private static final String DEV_TOOL_RESOURCE_DIR = "C:\\dev\\projects\\razarion\\code\\tmp";
    private static final String FILE_NAME = "GameUiControlConfig.json";
    private static final String URL = "http://localhost:8080/razarion-server/" + RestUrl.APPLICATION_PATH + "/" + RestUrl.GAME_UI_CONTROL_PATH;

    public GameUiControlConfig readFromServer() {
        return ClientBuilder.newClient().target(URL).request(MediaType.APPLICATION_JSON).get(GameUiControlConfig.class);
    }

    public GameUiControlConfig readFromFile() {
        try {
            String string = new String(Files.readAllBytes(getFile(FILE_NAME).toPath()));
            return new ObjectMapper().readValue(string, GameUiControlConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
