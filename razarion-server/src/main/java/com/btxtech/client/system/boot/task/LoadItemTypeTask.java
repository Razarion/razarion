package com.btxtech.client.system.boot.task;

import com.btxtech.uiservice.units.ItemService;
import com.btxtech.shared.ItemTypeService;
import com.btxtech.shared.dto.ItemType;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 07.02.2016.
 */
@Dependent
public class LoadItemTypeTask extends AbstractStartupTask {
    @Inject
    private ItemService itemService;
    @Inject
    private Caller<ItemTypeService> itemTypeServiceCaller;
    private Logger logger = Logger.getLogger(LoadItemTypeTask.class.getName());

    @Override
    protected void privateStart(final DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        itemTypeServiceCaller.call(new RemoteCallback<Collection<ItemType>>() {
            @Override
            public void callback(Collection<ItemType> itemTypes) {
                itemService.setItemTypes(itemTypes);
                deferredStartup.finished();
            }
        }, new ErrorCallback() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "loadItemTypes failed: " + message, throwable);
                deferredStartup.failed(throwable);
                return false;
            }

        }).loadItemTypes();
    }
}
