package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by Beat
 * 24.08.2016.
 */
@Deprecated
@Path(CommonUrl.ITEM_TYPE_PROVIDER)
public interface ItemTypeProvider {
    @POST
    @Path("createResourceItemType")
    @Produces(MediaType.APPLICATION_JSON)
    ResourceItemType createResourceItemType();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("readResourceItemTypes")
    List<ResourceItemType> readResourceItemTypes();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("updateResourceItemType")
    void updateResourceItemType(ResourceItemType resourceItemType);

    @DELETE
    @Path("deleteResourceItemType/{id}")
    void deleteResourceItemType(@PathParam("id") int id);

    @POST
    @Path("createBoxItemType")
    @Produces(MediaType.APPLICATION_JSON)
    BoxItemType createBoxItemType();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("readBoxItemTypes")
    List<BoxItemType> readBoxItemTypes();

    @DELETE
    @Path("deleteBoxItemType/{id}")
    void deleteBoxItemType(@PathParam("id") int id);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("updateBoxItemType")
    void updateBoxItemType(BoxItemType boxItemType);
}
