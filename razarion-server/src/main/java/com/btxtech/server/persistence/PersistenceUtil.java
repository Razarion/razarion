package com.btxtech.server.persistence;

/**
 * Created by Beat
 * 21.10.2016.
 */
public interface PersistenceUtil {
    static Integer getImageIdSafe(ImageLibraryEntity imageLibraryEntity) {
        if (imageLibraryEntity != null) {
            return imageLibraryEntity.getId();
        } else {
            return null;
        }
    }
}
