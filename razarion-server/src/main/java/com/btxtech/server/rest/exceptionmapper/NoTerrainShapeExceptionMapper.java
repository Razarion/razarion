package com.btxtech.server.rest.exceptionmapper;

import com.btxtech.server.gameengine.NoTerrainShapeException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NoTerrainShapeExceptionMapper implements ExceptionMapper<NoTerrainShapeException> {

    public Response toResponse(NoTerrainShapeException e) {
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}