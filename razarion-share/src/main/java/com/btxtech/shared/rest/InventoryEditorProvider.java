package com.btxtech.shared.rest;

import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;

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
 * on 19.09.2017.
 */
@Path(RestUrl.INVENTORY_EDITOR_PROVIDER_PATH)
public interface InventoryEditorProvider {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("readInventoryItemObjectNameIds")
    List<ObjectNameId> readInventoryItemObjectNameIds();

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("createInventoryItem")
    InventoryItem createInventoryItem();

    @DELETE
    @Path("deleteInventoryItem/{id}")
    void deleteInventoryItem(@PathParam("id") int id);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("updateInventoryItem")
    void updateInventoryItem(InventoryItem inventoryItem);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("readInventoryItem/{id}")
    InventoryItem readInventoryItem(@PathParam("id") int id);
}
