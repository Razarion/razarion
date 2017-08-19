package com.btxtech.shared.rest;

import com.btxtech.shared.dto.ImageGalleryItem;

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

import static com.btxtech.shared.rest.RestUrl.PLANET_MINI_MAP_PATH;

/**
 * Created by Beat
 * 17.06.2016.
 */
@Path(RestUrl.IMAGE_SERVICE_PATH)
public interface ImageProvider {

    @GET
    @Produces({"image/jpeg", "image/png", "image/gif"})
    @Path("{id}")
    Response getImage(@PathParam("id") int id);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("imagegallery")
    List<ImageGalleryItem> getImageGalleryItems();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("imagegallery/{id}")
    ImageGalleryItem getImageGalleryItem(@PathParam("id") int id);

    @POST
    @Path("upload")
    @Consumes(MediaType.TEXT_PLAIN)
    void uploadImage(String dataUrl);

    @PUT
    @Path("save/{id}")
    @Consumes(MediaType.TEXT_PLAIN)
    void save(@PathParam("id") int id, String dataUrl);

    @GET
    @Produces({"image/jpeg", "image/png", "image/gif"})
    @Path(PLANET_MINI_MAP_PATH + "/{planetId}")
    Response getMiniMapImage(@PathParam("planetId") int planetId);
}
