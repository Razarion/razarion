package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.AudioItemConfig;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by Beat
 * 24.12.2016.
 */
@Path(CommonUrl.AUDIO_SERVICE_PATH)
public interface AudioProvider {

    @GET
    @Produces({"audio/mpeg"})
    @Path("{id}")
    Response getAudio(@PathParam("id") int id);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getAudioItemConfig/{id}")
    AudioItemConfig getAudioItemConfig(@PathParam("id") int id);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getAudioItemConfigs")
    List<AudioItemConfig> getAudioItemConfigs();

    @POST
    @Path("create")
    @Consumes(MediaType.TEXT_PLAIN)
    void createAudio(String dataUrl);

    @PUT
    @Path("save")
    @Consumes(MediaType.APPLICATION_JSON)
    void save(List<AudioItemConfig> audioItemConfigs);

}
