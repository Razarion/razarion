package com.btxtech.server;

import com.btxtech.client.VertexListService;
import org.jboss.errai.bus.server.annotations.Service;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 15.08.2015.
 */
@Service
@ApplicationScoped
public class VertexListServiceImpl implements VertexListService {
    @Override
    public String getVertexList() {
        return "Hallo";
    }
}
