package com.btxtech.shared;

import com.btxtech.shared.dto.ItemType;
import org.jboss.errai.bus.server.annotations.Remote;

import java.util.Collection;

/**
 * Created by Beat
 * 15.08.2015.
 */
@Remote
public interface ItemTypeService {
    Collection<ItemType> loadItemTypes();
}
