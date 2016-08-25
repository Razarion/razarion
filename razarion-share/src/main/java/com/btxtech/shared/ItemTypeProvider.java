package com.btxtech.shared;

import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;

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
@Path(RestUrl.BASE_ITEM_TYPE_PROVIDER)
public interface ItemTypeProvider {
    @POST
    @Path("createBaseItemType")
    @Produces(MediaType.APPLICATION_JSON)
    BaseItemType createBaseItemType();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("read")
    List<ItemType> read();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("updateBaseItemType")
    void updateBaseItemType(BaseItemType baseItemType);

    @DELETE
    @Path("deleteBaseItemType/{id}")
    void deleteBaseItemType(@PathParam("id") int id);
}
