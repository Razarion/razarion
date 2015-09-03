package com.btxtech.server;

import com.btxtech.client.VertexListService;
import com.btxtech.shared.VertexList;
import com.btxtech.server.collada.ColladaConverter;
import org.jboss.errai.bus.server.annotations.Service;

import javax.enterprise.context.ApplicationScoped;
import java.io.FileInputStream;

/**
 * Created by Beat
 * 15.08.2015.
 */
@Service
@ApplicationScoped
public class VertexListServiceImpl implements VertexListService {
    @Override
    public VertexList getVertexList() {
        try {
            FileInputStream fileInputStream = new FileInputStream("C:\\dev\\projects\\razarion\\code\\experimental-webgl\\src\\main\\resources\\collada\\bush1.dae");
            return ColladaConverter.read(fileInputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
