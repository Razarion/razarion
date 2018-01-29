package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.InventoryInfo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Beat
 * on 17.09.2017.
 */
@Path(CommonUrl.INVENTORY_PROVIDER_PATH)
public interface InventoryProvider {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("loadInventory")
    InventoryInfo loadInventory();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("loadCrystals")
    int loadCrystals();
}
