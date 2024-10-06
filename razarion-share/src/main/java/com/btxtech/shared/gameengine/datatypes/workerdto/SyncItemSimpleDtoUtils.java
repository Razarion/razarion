package com.btxtech.shared.gameengine.datatypes.workerdto;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 08.01.2017.
 */
public class SyncItemSimpleDtoUtils {
    public static IdsDto toIds(Collection<? extends SyncItemSimpleDto> syncItemSimpleDtos) {
        return new IdsDto()
                .ids(syncItemSimpleDtos.stream().map(SyncItemSimpleDto::getId).collect(Collectors.toList()));
    }
}
