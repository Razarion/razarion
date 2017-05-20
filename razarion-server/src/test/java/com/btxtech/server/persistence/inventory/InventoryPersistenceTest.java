package com.btxtech.server.persistence.inventory;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 20.05.2017.
 */
public class InventoryPersistenceTest extends ArquillianBaseTest {
    @Inject
    private InventoryPersistence inventoryPersistence;
    private int baseItemTypeId;

    @Test
    public void testCrud() throws Exception {
        runInTransaction(em -> {
            BaseItemTypeEntity baseItemTypeEntity = new BaseItemTypeEntity();
            em.persist(baseItemTypeEntity);
            baseItemTypeId = baseItemTypeEntity.getId();
            em.createNativeQuery("INSERT INTO IMAGE_LIBRARY (id, size) VALUES(1, 0)").executeUpdate();
            em.createNativeQuery("INSERT INTO IMAGE_LIBRARY (id, size) VALUES(23, 0)").executeUpdate();
        });

        InventoryItem expectedInventoryItem = inventoryPersistence.createInventoryItem();
        expectedInventoryItem.setI18nName(i18nHelper("aiudhoais paisuhdpaisd")).setName("adesasd").setGold(999).setBaseItemTypeCount(3).setBaseItemTypeId(baseItemTypeId).setBaseItemTypeFreeRange(1234).setImageId(1);
        inventoryPersistence.updateInventoryItem(expectedInventoryItem);
        List<InventoryItem> actualInventoryItems = inventoryPersistence.readInventoryItems();
        ReflectionAssert.assertReflectionEquals(expectedInventoryItem, actualInventoryItems.get(0));

        expectedInventoryItem.setI18nName(i18nHelper("ddd www")).setName("rrr").setGold(1).setBaseItemTypeCount(0).setBaseItemTypeId(null).setBaseItemTypeFreeRange(0).setImageId(23);
        inventoryPersistence.updateInventoryItem(expectedInventoryItem);
        actualInventoryItems = inventoryPersistence.readInventoryItems();
        ReflectionAssert.assertReflectionEquals(expectedInventoryItem, actualInventoryItems.get(0));
        Assert.assertEquals(1, ((Number) getEntityManager().createQuery("SELECT COUNT(r) FROM BaseItemTypeEntity r").getSingleResult()).intValue());

        inventoryPersistence.deleteInventoryItem(expectedInventoryItem.getId());

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