package com.btxtech.uiservice.effects;

import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.system.SimpleExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * 10.02.2017.
 */
@Singleton
public class TrailService {
    public static final long VISIBLE_WRECKAGE_MILLIS = 30 * 1000;
    private static final long CLEANUP_INTERVAL_MILLIS = 1000;
    final private Collection<WreckageItem> wreckageItems = new ArrayList<>();

    @Inject
    public TrailService(SimpleExecutorService simpleExecutorService, ItemTypeService itemTypeService) {
        simpleExecutorService.scheduleAtFixedRate(CLEANUP_INTERVAL_MILLIS, true, this::cleanup, SimpleExecutorService.Type.TRAIL_SERVICE);
    }

    public void clear() {
        wreckageItems.clear();
    }

    private void cleanup() {
        // TODO
    }
}
