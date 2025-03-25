package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.ImageGalleryItem;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static com.btxtech.shared.CommonUrl.PLANET_MINI_MAP_PATH;

/**
 * Created by Beat
 * 17.06.2016.
 */
@Path(CommonUrl.IMAGE_SERVICE_PATH)
public interface ImageProvider {

    @GET
    @Produces({"image/jpeg", "image/png", "image/gif"})
    @Path("{id}")
    Response getImage(@PathParam("id") int id);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("image-gallery")
    List<ImageGalleryItem> getImageGalleryItems();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("image-gallery/{id}")
    ImageGalleryItem getImageGalleryItem(@PathParam("id") int id);

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    void uploadImage(Map<String, InputStream> files);

    @PUT
    @Path("update/{id}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    void update(@PathParam("id") int id, Map<String, InputStream> files);

    @DELETE
    @Path("/delete/{id}")
    void delete(@PathParam("id") int id);

    @GET
    @Produces({"image/jpeg", "image/png", "image/gif"})
    @Path(PLANET_MINI_MAP_PATH + "/{planetId}")
    Response getMiniMapImage(@PathParam("planetId") int planetId);
}
