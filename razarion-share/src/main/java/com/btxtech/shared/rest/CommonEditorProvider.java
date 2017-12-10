package com.btxtech.shared.rest;

import com.btxtech.shared.datatypes.I18nStringEditor;
import com.btxtech.shared.dto.ObjectNameId;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * on 10.09.2017.
 */
@Path(RestUrl.COMMON_EDITOR_PROVIDER_PATH)
public interface CommonEditorProvider {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("loadalli18nentries")
    List<I18nStringEditor> loadAllI18NEntries();

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("savei8nentries")
    void saveI8NEntries(List<I18nStringEditor> i18nStringEditors);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getallbotsfromplanet/{planetId}")
    List<ObjectNameId> getAllBotsFromPlanet(@PathParam("planetId") int planetId);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("allBotFromPlanet/{botId}")
    String getInternalNameBot(@PathParam("botId") int botId);

}
