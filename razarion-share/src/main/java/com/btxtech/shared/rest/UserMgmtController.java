package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path(CommonUrl.USER_MGMT_CONTROLLER_PATH)
public interface UserMgmtController {
    @POST
    @Path("set-level/{userId}/{levelId}")
    void setLevel(@PathParam("userId") int userId, @PathParam("levelId") int levelId);

    @POST
    @Path("set-crystals/{userId}/{crystals}")
    void setCrystals(@PathParam("userId") int userId, @PathParam("crystals") int crystals);

    @POST
    @Path("get-user-id-for-email/{email}")
    int getUserIdForEmail(@PathParam("email") String email);

}
