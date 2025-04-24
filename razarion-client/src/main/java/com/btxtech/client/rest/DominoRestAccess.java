package com.btxtech.client.rest;

import com.btxtech.shared.rest.QuestAccess;
import org.dominokit.rest.shared.request.RequestMeta;
import org.dominokit.rest.shared.request.ServerRequest;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.btxtech.shared.rest.QuestAccess.PATH;

public class DominoRestAccess {
    private static final Logger logger = Logger.getLogger(DominoRestAccess.class.getName());

    public static QuestAccess getQuestAccess() {
        return () -> {
            ServerRequest<Void, Void> serverRequest = new ServerRequest<Void, Void>(new RequestMeta(QuestAccess.class,
                    "activateNextPossibleQuest",
                    Void.class,
                    void.class), null) {
            };
            serverRequest.setHttpMethod(HttpMethod.POST);
            serverRequest.setAccept(new String[]{MediaType.APPLICATION_JSON});
            serverRequest.setPath(PATH + "/activateNextPossibleQuest");
            serverRequest.setServiceRoot("");
            serverRequest.setContentType(new String[]{MediaType.APPLICATION_JSON});
            serverRequest.onFailed(fail -> logger.log(Level.SEVERE, "QuestAccess.activateNextPossibleQuest() failed: " + fail.getStatusText(), fail.getThrowable()));
            serverRequest.send();
        };
    }
}
