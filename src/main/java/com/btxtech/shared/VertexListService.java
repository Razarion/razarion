package com.btxtech.shared;

import com.btxtech.shared.VertexList;
import org.jboss.errai.bus.server.annotations.Remote;

import java.util.List;

/**
 * Created by Beat
 * 15.08.2015.
 */
@Remote
@Deprecated
public interface VertexListService {
    List<VertexList> getUnit();
}
