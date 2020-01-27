package com.btxtech.server.rest.exceptionmapper;

import com.btxtech.server.user.SecurityException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class SecurityExceptionMapper implements ExceptionMapper<SecurityException> {

    public Response toResponse(SecurityException e) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
}