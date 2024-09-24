package com.btxtech.server.persistence.inventory;

import com.btxtech.server.IgnoreOldArquillianTest;
import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Inject;

/**
 * Created by Beat
 * 20.05.2017.
 */
@Ignore
public class InventoryPersistenceTestRest extends IgnoreOldArquillianTest {

    private InventoryItemCrudPersistence inventoryPersistence;
    private int baseItemTypeId;

    @Inject
    public InventoryPersistenceTestRest(InventoryItemCrudPersistence inventoryPersistence) {
        this.inventoryPersistence = inventoryPersistence;
    }

    @Test
    public void testCrud() throws Exception {
        Assert.fail("---- TODO ----");
        runInTransaction(em -> {
            BaseItemTypeEntity baseItemTypeEntity = new BaseItemTypeEntity();
            em.persist(baseItemTypeEntity);
            baseItemTypeId = baseItemTypeEntity.getId();
            em.createNativeQuery("INSERT INTO IMAGE_LIBRARY (id, size) VALUES(1, 0)").executeUpdate();
            em.createNativeQuery("INSERT INTO IMAGE_LIBRARY (id, size) VALUES(23, 0)").executeUpdate();
        });

//        InventoryItem expectedInventoryItem = inventoryPersistence.createInventoryItem();
//        expectedInventoryItem.i18nName(i18nHelper("aiudhoais paisuhdpaisd")).setInternalName("adesasd").setRazarion(999).setBaseItemTypeCount(3).setBaseItemTypeId(baseItemTypeId).setBaseItemTypeFreeRange(1234).setImageId(1);
//        inventoryPersistence.updateInventoryItem(expectedInventoryItem);
//        List<InventoryItem> actualInventoryItems = inventoryPersistence.readInventoryItems();
//        Assert.assertEquals(1, actualInventoryItems.size());
//        ReflectionAssert.assertReflectionEquals(expectedInventoryItem, actualInventoryItems.get(0));
//
//        expectedInventoryItem.i18nName(i18nHelper("ddd www")).setInternalName("rrr").setRazarion(1).setBaseItemTypeCount(0).setBaseItemTypeId(null).setBaseItemTypeFreeRange(0).setImageId(23);
//        inventoryPersistence.updateInventoryItem(expectedInventoryItem);
//        actualInventoryItems = inventoryPersistence.readInventoryItems();
//        Assert.assertEquals(1, actualInventoryItems.size());
//        ReflectionAssert.assertReflectionEquals(expectedInventoryItem, actualInventoryItems.get(0));
//        Assert.assertEquals(1, ((Number) getEntityManager().createQuery("SELECT COUNT(r) FROM BaseItemTypeEntity r").getSingleResult()).intValue());
//
//        inventoryPersistence.deleteInventoryItem(expectedInventoryItem.getId());

        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(r) FROM InventoryItemEntity r").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(r) FROM I18N_BUNDLE r").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createNativeQuery("SELECT COUNT(*) FROM I18N_BUNDLE_STRING").getSingleResult()).intValue());
    }

    @After
    public void clean() throws Exception {
        runInTransaction(em -> {
            em.createQuery("DELETE FROM ImageLibraryEntity").executeUpdate();
            em.createQuery("DELETE FROM BaseItemTypeEntity").executeUpdate();
        });
    }

}