package com.btxtech.server.systemtests.editors.itemtype;

import com.btxtech.server.persistence.I18nBundleEntity;
import com.btxtech.server.persistence.itemtype.ResourceItemTypeEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.rest.ResourceItemTypeEditorController;
import org.junit.After;
import org.junit.Before;

/**
 * Created by Beat
 * 19.05.2017.
 */
public class ResourceItemTypePersistenceTestRest extends AbstractCrudTest<ResourceItemTypeEditorController, ResourceItemType> {

    public ResourceItemTypePersistenceTestRest() {
        super(ResourceItemTypeEditorController.class, ResourceItemType.class);
    }


    @Before
    public void fillTables() {
        setupImages();
    }

    @After
    public void cleanTables() {
        cleanTable(ResourceItemTypeEntity.class);
        cleanTableNative("I18N_BUNDLE_STRING");
        cleanTable(I18nBundleEntity.class);
    }

    @Override
    protected void setupUpdate() {
        registerUpdate(resourceItemType -> resourceItemType
                .setAmount(54)
                .setRadius(31)
                .setFixVerticalNorm(false)
                .i18nName(i18nHelper("asdf"))
                .i18nDescription(i18nHelper("Codsfe gfrgg ms"))
                .thumbnail(IMAGE_1_ID)
        );
        registerUpdate(resourceItemType -> resourceItemType
                .setAmount(100000)
                .setRadius(3)
                .setFixVerticalNorm(true)
                .i18nName(i18nHelper("Razarion"))
                .i18nDescription(i18nHelper("Harvest Razarion from here"))
                .thumbnail(IMAGE_2_ID)
        );
    }
}