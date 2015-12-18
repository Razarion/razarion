package com.btxtech.server;

import com.btxtech.client.VertexListService;
import com.btxtech.server.collada.ColladaConverter;
import com.btxtech.shared.VertexList;
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
public class VertexListServiceImpl implements VertexListService {
    @Inject
    private ExceptionHandler exceptionHandler;

    @Override
    public List<VertexList> getVertexList() {
        try {
            // FileInputStream fileInputStream = new FileInputStream("C:\\dev\\projects\\razarion\\code\\experimental-webgl\\src\\main\\resources\\collada\\bush1.dae");
            FileInputStream fileInputStream = new FileInputStream("C:\\dev\\projects\\razarion\\code\\experimental-webgl\\src\\main\\resources\\collada\\tree03.dae");
            return ColladaConverter.read(fileInputStream);
        } catch (Exception e) {
            exceptionHandler.handleException(e);
            return null;
        }
    }
}
