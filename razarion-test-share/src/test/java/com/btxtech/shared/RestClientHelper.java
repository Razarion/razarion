package com.btxtech.shared;

import com.btxtech.shared.dto.ColdGameUiContext;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * Created by Beat
 * 07.07.2016.
 */
@Singleton
public class RestClientHelper {
    // public static final String HOST_PORT = "192.168.99.100:32778/test";
    // public static final String HOST_PORT = "www.razarion.com";
    public static final String HOST_PORT = "localhost:8080";
    public static final String GAME_UI_CONTEXT_CONTROL_PATH = "game-ui-context-control";
    public static final String HTTP_LOCALHOST_8080 = "http://" + HOST_PORT;
    private static final String URL_GAME_UI_CONTROL = HTTP_LOCALHOST_8080 + CommonUrl.APPLICATION_PATH + "/" + GAME_UI_CONTEXT_CONTROL_PATH + "/" + CommonUrl.COLD;
    private static final String GAME_UI_CONTROL_INPUT = "{\"playbackGameSessionUuid\": null, \"playbackSessionUuid\": null}";

    public static ColdGameUiContext readColdGameUiContext(int planetId) {
        try {
            Client client = ClientBuilder.newClient();
            String string = client.target(URL_GAME_UI_CONTROL).request(MediaType.APPLICATION_JSON).post(Entity.entity(GAME_UI_CONTROL_INPUT, MediaType.APPLICATION_JSON_TYPE), String.class);
            return new ObjectMapper().readValue(string, ColdGameUiContext.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
