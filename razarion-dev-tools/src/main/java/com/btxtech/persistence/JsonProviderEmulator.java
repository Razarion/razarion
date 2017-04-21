package com.btxtech.persistence;

import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;
import com.btxtech.shared.dto.GameUiControlConfig;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.rest.RestUrl;
import com.btxtech.webglemulator.razarion.HttpConnectionEmu;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 07.07.2016.
 */
@Singleton
public class JsonProviderEmulator {
    private static final String DEV_TOOL_RESOURCE_DIR = "C:\\dev\\projects\\razarion\\code\\tmp";
    private static final String FILE_NAME_TUTORIAL = "GameUiControlConfigTutorial.json";
    private static final String FILE_NAME_MULTI_PLAYER = "GameUiControlConfigMultiplayer.json";
    private static final String VERTEX_CONTAINER_BUFFERS_FILE_NAME = "VertexContainerBuffers.json";
    private static final String TMP_FILE_NAME = "TmpGameUiControlConfig.json";
    private static final String URL = "http://localhost:8080/" + RestUrl.APPLICATION_PATH + "/" + RestUrl.GAME_UI_CONTROL_PATH;
    private static final String URL_VERTEX_CONTAINER_BUFFERS_FILE_NAME = "http://localhost:8080/" + RestUrl.APPLICATION_PATH + "/" + RestUrl.SHAPE_3D_PROVIDER + "/" + RestUrl.SHAPE_3D_PROVIDER_GET_VERTEX_BUFFER;
    private static final String FACEBOOK_USER_LOGIN_INFO_STRING_TUTORIAL = "{\"accessToken\": null, \"expiresIn\": null, \"signedRequest\": null, \"userId\": null}";
    private static final String FACEBOOK_USER_LOGIN_INFO_STRING_MULTI_PLAYER = "{\"accessToken\": null, \"expiresIn\": null, \"signedRequest\": null, \"userId\": 100003634094139}";

    public GameUiControlConfig readFromServer() {
        return ClientBuilder.newClient().target(URL).request(MediaType.APPLICATION_JSON).get(GameUiControlConfig.class);
    }

    public GameUiControlConfig readFromFile(boolean tutorial) {
        try {
            String string = new String(Files.readAllBytes(getFile(tutorial ? FILE_NAME_TUTORIAL : FILE_NAME_MULTI_PLAYER).toPath()));
            return new ObjectMapper().readValue(string, GameUiControlConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<VertexContainerBuffer> readVertexContainerBuffersFromFile() {
        try {
            String string = new String(Files.readAllBytes(getFile(VERTEX_CONTAINER_BUFFERS_FILE_NAME).toPath()));
            return new ObjectMapper().readValue(string, new TypeReference<List<VertexContainerBuffer>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public GameEngineConfig readGameEngineConfigFromFile(String filename) {
        try {
            String string = new String(Files.readAllBytes(new File(filename).toPath()));
            return new ObjectMapper().readValue(string, GameEngineConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void fromServerToFilePost(String fileName, String url, String fbLogin) {
        try {
            Client client = ClientBuilder.newClient();
            String text = client.target(url).request(MediaType.APPLICATION_JSON).post(Entity.entity(fbLogin, MediaType.APPLICATION_JSON_TYPE), String.class);
            Files.write(getFile(fileName).toPath(), text.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpConnectionEmu fromServer() {
        try {
            HttpConnectionEmu emu = new HttpConnectionEmu();
            Client client = ClientBuilder.newClient();
            client.register((ClientResponseFilter) (requestContext, responseContext) -> {
                for (Map.Entry<String, NewCookie> entry : responseContext.getCookies().entrySet()) {
                    if (entry.getKey().equals(HttpConnectionEmu.SESSION_KEY)) {
                        emu.setSessionCookie(entry.getValue());
                    }
                }
            });
            String string = client.target(URL).request(MediaType.APPLICATION_JSON).post(Entity.entity(FACEBOOK_USER_LOGIN_INFO_STRING_MULTI_PLAYER, MediaType.APPLICATION_JSON_TYPE), String.class);
            emu.setGameUiControlConfig(new ObjectMapper().readValue(string, GameUiControlConfig.class));
            return emu;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void fromServerToFileGet(String fileName, String url) {
        try {
            Client client = ClientBuilder.newClient();
            String text = client.target(url).request(MediaType.APPLICATION_JSON).get(String.class);
            Files.write(getFile(fileName).toPath(), text.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void gameUiControlConfigToFile(String fileName, Object object) {
        try {
            File file = getFile(fileName);
            new ObjectMapper().writeValue(file, object);
            System.out.println("Written config to: " + file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void gameUiControlConfigToTmpFile(Object object) {
        gameUiControlConfigToFile(TMP_FILE_NAME, object);
    }

    public void fromServerToFile() {
        fromServerToFilePost(FILE_NAME_MULTI_PLAYER, URL, FACEBOOK_USER_LOGIN_INFO_STRING_MULTI_PLAYER);
        fromServerToFilePost(FILE_NAME_TUTORIAL, URL, FACEBOOK_USER_LOGIN_INFO_STRING_TUTORIAL);
    }

    public void fromServerToFileVertexContainerBuffer() {
        fromServerToFileGet(VERTEX_CONTAINER_BUFFERS_FILE_NAME, URL_VERTEX_CONTAINER_BUFFERS_FILE_NAME);
    }

    private File getFile(String fileName) {
        return new File(DEV_TOOL_RESOURCE_DIR, fileName);
    }
}
