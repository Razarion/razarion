package com.btxtech.server;

import com.btxtech.server.collada.ColladaConverter;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.VertexListService;
import org.jboss.errai.bus.server.annotations.Service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.FileInputStream;
import java.util.List;

/**
 * Created by Beat
 * 15.08.2015.
 */
@Service
@ApplicationScoped
@Deprecated
public class VertexListServiceImpl implements VertexListService {
    @Inject
    private ExceptionHandler exceptionHandler;

    @Override
    public List<VertexList> getUnit() {
//        try {
//            FileInputStream fileInputStream = new FileInputStream("C:\\dev\\projects\\razarion\\code\\experimental-webgl\\src\\main\\resources\\collada\\ViperBeat1.dae");
//            return ColladaConverter.convertToTerrainObject(fileInputStream);
//        } catch (Exception e) {
//            exceptionHandler.handleException(e);
            return null;
 //       }
    }
}
