package com.btxtech.server.systemtests.editors.itemtype;

import com.btxtech.server.persistence.inventory.InventoryItemEntity;
import com.btxtech.server.systemtests.framework.AbstractSystemTest;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemTypePossibility;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 19.05.2017.
 */
@Ignore
public class BoxItemTypePersistenceTestRest extends AbstractSystemTest {
    private int inventoryItemId1;
    private int inventoryItemId2;

    @Test
    public void testCrud() throws Exception {
        runInTransaction(em -> {
            InventoryItemEntity inventoryItemEntity1 = new InventoryItemEntity();
            em.persist(inventoryItemEntity1);
            inventoryItemId1 = inventoryItemEntity1.getId();
            InventoryItemEntity inventoryItemEntity2 = new InventoryItemEntity();
            em.persist(inventoryItemEntity2);
            inventoryItemId2 = inventoryItemEntity2.getId();
            em.createNativeQuery("INSERT INTO COLLADA (id) VALUES(17)").executeUpdate();
            em.createNativeQuery("INSERT INTO COLLADA (id) VALUES(18)").executeUpdate();
            em.createNativeQuery("INSERT INTO IMAGE_LIBRARY (id, size) VALUES(64, 0)").executeUpdate();
            em.createNativeQuery("INSERT INTO IMAGE_LIBRARY (id, size) VALUES(65, 0)").executeUpdate();
        });

//        BoxItemType boxExpected = itemTypePersistence.createBoxItemType();
//        finalizeSimpleBox1(boxExpected);
//        itemTypePersistence.updateBoxItemType(boxExpected);
//        List<BoxItemType> actualBoxes = itemTypePersistence.readBoxItemTypes();
//        Assert.assertEquals(1, actualBoxes.size());
//        ReflectionAssert.assertReflectionEquals(boxExpected, actualBoxes.get(0));
//
//        finalizeSimpleBox2(boxExpected);
//        itemTypePersistence.updateBoxItemType(boxExpected);
//        actualBoxes = itemTypePersistence.readBoxItemTypes();
//        Assert.assertEquals(1, actualBoxes.size());
//        ReflectionAssert.assertReflectionEquals(boxExpected, actualBoxes.get(0));
//
//        itemTypePersistence.deleteBoxItemType(boxExpected.getId());
//        Assert.assertTrue(itemTypePersistence.readBoxItemTypes().isEmpty());

        // Verify leftovers
        Assert.assertEquals(2, ((Number) getEntityManager().createQuery("SELECT COUNT(r) FROM InventoryItemEntity r").getSingleResult()).intValue());
        Assert.assertEquals(2, ((Number) getEntityManager().createQuery("SELECT COUNT(r) FROM ColladaEntity r").getSingleResult()).intValue());
        Assert.assertEquals(2, ((Number) getEntityManager().createQuery("SELECT COUNT(r) FROM ImageLibraryEntity r").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(r) FROM BoxItemTypeEntity r").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(r) FROM I18N_BUNDLE r").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createNativeQuery("SELECT COUNT(*) FROM I18N_BUNDLE_STRING").getSingleResult()).intValue());
    }

    @After
    public void clean() throws Exception {
        runInTransaction(em -> {
            em.createQuery("DELETE FROM ColladaEntity").executeUpdate();
            em.createQuery("DELETE FROM AudioLibraryEntity").executeUpdate();
            em.createQuery("DELETE FROM ImageLibraryEntity").executeUpdate();
            em.createQuery("DELETE FROM InventoryItemEntity ").executeUpdate();
        });
    }

    private void finalizeSimpleBox1(BoxItemType boxItemType) {
        boxItemType.i18nName(i18nHelper("Box")).i18nDescription(i18nHelper("Contains useful items")).thumbnail(64).threeJsModelPackConfigId(17);
        boxItemType.ttl(1500).radius(1.2).fixVerticalNorm(true);
        List<BoxItemTypePossibility> boxItemTypePossibilities = new ArrayList<>();
        boxItemTypePossibilities.add(new BoxItemTypePossibility().setPossibility(0.75).setInventoryItemId(inventoryItemId1));
        boxItemType.boxItemTypePossibilities(boxItemTypePossibilities);
    }

    private void finalizeSimpleBox2(BoxItemType boxItemType) {
        boxItemType.i18nName(i18nHelper("asdf")).i18nDescription(i18nHelper("Codsfe gfrgg ms")).thumbnail(65).threeJsModelPackConfigId(18);
        boxItemType.ttl(222).radius(331).fixVerticalNorm(false);
        List<BoxItemTypePossibility> boxItemTypePossibilities = new ArrayList<>();
        boxItemTypePossibilities.add(new BoxItemTypePossibility().setPossibility(0.4).setInventoryItemId(inventoryItemId2));
        boxItemType.boxItemTypePossibilities(boxItemTypePossibilities);
    }
}