package com.btxtech.uiservice.effects;

import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.nativejs.NativeMatrixFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Beat
 * 10.02.2017.
 */
@ApplicationScoped
public class TrailService {
    private static final long CLEANUP_INTERVAL_MILLIS = 1000;
    public static final long VISIBLE_WRECKAGE_MILLIS = 30 * 1000;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private SimpleExecutorService simpleExecutorService;
    @Inject
    private NativeMatrixFactory nativeMatrixFactory;
    private MapList<BaseItemType, ModelMatrices> wreckageModelMatrices = new MapList<>();
    private Collection<WreckageItem> wreckageItems = new ArrayList<>();

    @PostConstruct
    public void postConstruct() {
        simpleExecutorService.scheduleAtFixedRate(CLEANUP_INTERVAL_MILLIS, true, this::cleanup, SimpleExecutorService.Type.UNSPECIFIED);
    }

    public void addWreckage(SyncBaseItemSimpleDto syncBaseItem) {
        BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
        if (baseItemType.getWreckageShape3DId() == null) {
            return;
        }
        WreckageItem wreckageItem = new WreckageItem(baseItemType, syncBaseItem.getPosition3d(), nativeMatrixFactory);
        wreckageItems.add(wreckageItem);
        wreckageModelMatrices.put(baseItemType, wreckageItem.getModelMatrices());
    }

    public List<ModelMatrices> provideWreckageModelMatrices(BaseItemType baseItemType) {
        return wreckageModelMatrices.get(baseItemType);
    }

    private void cleanup() {
        for (Iterator<WreckageItem> iterator = wreckageItems.iterator(); iterator.hasNext(); ) {
            WreckageItem wreckageItem = iterator.next();
            if (wreckageItem.isExpired()) {
                iterator.remove();
                wreckageModelMatrices.remove(wreckageItem.getBaseItemType(), wreckageItem.getModelMatrices());
            }
        }
    }

}
