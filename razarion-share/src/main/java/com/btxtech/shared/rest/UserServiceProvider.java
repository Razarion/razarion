package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.*;
import com.btxtech.shared.dto.EmailPasswordInfo;
import com.btxtech.shared.dto.RegisterResult;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by Beat
 * on 26.12.2017.
 */
@Path(CommonUrl.USER_SERVICE_PROVIDER_PATH)
public interface UserServiceProvider {
    @POST
    @Path("ingamefacebookregister")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    RegisterInfo inGameFacebookRegister(FbAuthResponse fbAuthResponse);

    @POST
    @Path("createunverifieduser")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    RegisterResult createUnverifiedUser(EmailPasswordInfo emailPasswordInfo);

    @POST
    @Path("setname/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    SetNameResult setName(@PathParam("name") String name);

    @GET
    @Path("verifySetName/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    SetNameResult verifySetName(@PathParam("name") String name); // Top level enum as return type not allowed

    @GET
    @Path("isemailfree/{email}")
    @Produces(MediaType.APPLICATION_JSON)
    boolean isEmailFree(@PathParam("email") String email);

    @GET
    @Path("useraccountinfo")
    @Produces(MediaType.APPLICATION_JSON)
    UserAccountInfo userAccountInfo();

    @POST
    @Path("useraccountinfo")
    @Consumes(MediaType.APPLICATION_JSON)
    void setRememberMe(boolean rememberMe);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("additionuserinfo")
    List<AdditionUserInfo> additionUserInfo();
}
