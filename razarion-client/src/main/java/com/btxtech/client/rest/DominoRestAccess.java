package com.btxtech.client.rest;

import com.btxtech.common.JwtHelper;
import com.btxtech.shared.RazarionSharedDominokitJsonRegistry;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.rest.GameUiContextAccess;
import com.btxtech.shared.rest.QuestAccess;
import org.dominokit.jackson.ObjectReader;
import org.dominokit.jackson.registration.TypeToken;
import org.dominokit.rest.shared.request.RequestMeta;
import org.dominokit.rest.shared.request.ServerRequest;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DominoRestAccess {
    private static final Logger logger = Logger.getLogger(DominoRestAccess.class.getName());

    public static QuestAccess getQuestAccess() {
        return () -> {
            ServerRequest<Void, Void> serverRequest = new ServerRequest<Void, Void>(new RequestMeta(QuestAccess.class,
                    "activateNextPossibleQuest",
                    Void.class,
                    void.class), null) {
            };
            setBearerTokenFromLocalStorage(serverRequest);
            serverRequest.setHttpMethod(HttpMethod.POST)
                    .setAccept(new String[]{MediaType.APPLICATION_JSON})
                    .setPath(QuestAccess.PATH + "/activateNextPossibleQuest")
                    .setServiceRoot("")
                    .setContentType(new String[]{MediaType.APPLICATION_JSON})
                    .onFailed(fail -> logger.log(Level.SEVERE, "QuestAccess.activateNextPossibleQuest() failed: " + fail.getStatusText(), fail.getThrowable()))
                    .send();
        };
    }

    public static ServerRequest<Void, ColdGameUiContext> loadColdGameUiContext() {
        ObjectReader<ColdGameUiContext> reader = RazarionSharedDominokitJsonRegistry
                .getInstance()
                .getReader(TypeToken.of(ColdGameUiContext.class));

        if (reader == null) {
            throw new RuntimeException("ColdGameUiContext not found in RazarionSharedDominokitJsonRegistry");
        }

        ServerRequest<Void, ColdGameUiContext> serverRequest = new ServerRequest<Void, ColdGameUiContext>(new RequestMeta(GameUiContextAccess.class,
                "loadColdGameUiContext",
                Void.class,
                ColdGameUiContext.class),
                null) {
        };

        setBearerTokenFromLocalStorage(serverRequest);
        serverRequest.setHttpMethod("POST")
                .setAccept(new String[]{"application/json"})
                .setPath(GameUiContextAccess.PATH + "/cold")
                .setServiceRoot("")
                .setContentType(new String[]{"application/json"})
                .setResponseReader(response -> reader.read(response.getBodyAsString()));


        return serverRequest;
    }

    private static <R, S> void setBearerTokenFromLocalStorage(ServerRequest<R, S> serverRequest) {
        String bearerToken = JwtHelper.getBearerTokenFromLocalStorage();
        if (bearerToken != null) {
            serverRequest.setHeader("Authorization", "Bearer " + bearerToken);
        }
    }
}
