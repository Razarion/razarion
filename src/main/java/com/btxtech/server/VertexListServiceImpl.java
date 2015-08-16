package com.btxtech.server;

import com.btxtech.client.VertexListService;
import com.btxtech.client.terrain.VertexList;
import com.btxtech.server.collada.ColladaConverter;
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
    public VertexList getVertexList() {
        ColladaConverter colladaConverter = new ColladaConverter();
        try {
            return colladaConverter.read();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
