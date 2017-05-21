package com.btxtech.server.persistence.itemtype;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 19.05.2017.
 */
public class ResourceItemTypePersistenceTest extends ArquillianBaseTest {
    @Inject
    private ItemTypePersistence itemTypePersistence;

    @Test
    public void testCrud() throws Exception {
        runInTransaction(em -> {
            em.createNativeQuery("INSERT INTO COLLADA (id) VALUES(4)").executeUpdate();
            em.createNativeQuery("INSERT INTO COLLADA (id) VALUES(5)").executeUpdate();
            em.createNativeQuery("INSERT INTO IMAGE_LIBRARY (id, size) VALUES(64, 0)").executeUpdate();
            em.createNativeQuery("INSERT INTO IMAGE_LIBRARY (id, size) VALUES(65, 0)").executeUpdate();
        });

        ResourceItemType resourceItemType = itemTypePersistence.createResourceItemType();
        finalizeResourceItemType1(resourceItemType);
        itemTypePersistence.updateResourceItemType(resourceItemType);
        List<ResourceItemType> resourceItemTypes = itemTypePersistence.readResourceItemTypes();
        Assert.assertEquals(1, resourceItemTypes.size());
        ReflectionAssert.assertReflectionEquals(resourceItemType, resourceItemTypes.get(0));

        finalizeResourceItemType2(resourceItemType);
        itemTypePersistence.updateResourceItemType(resourceItemType);
        resourceItemTypes = itemTypePersistence.readResourceItemTypes();
        Assert.assertEquals(1, resourceItemTypes.size());
        ReflectionAssert.assertReflectionEquals(resourceItemType, resourceItemTypes.get(0));

        itemTypePersistence.deleteResourceItemType(resourceItemType.getId());
        Assert.assertTrue(itemTypePersistence.readResourceItemTypes().isEmpty());

        // Verify leftovers
        Assert.assertEquals(2, ((Number) getEntityManager().createQuery("SELECT COUNT(r) FROM ColladaEntity r").getSingleResult()).intValue());
        Assert.assertEquals(2, ((Number) getEntityManager().createQuery("SELECT COUNT(r) FROM ImageLibraryEntity r").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(r) FROM ResourceItemTypeEntity r").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(r) FROM I18N_BUNDLE r").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createNativeQuery("SELECT COUNT(*) FROM I18N_BUNDLE_STRING").getSingleResult()).intValue());
    }

    @After
    public void clean() throws Exception {
        runInTransaction(em -> {
            em.createQuery("DELETE FROM ColladaEntity").executeUpdate();
            em.createQuery("DELETE FROM ImageLibraryEntity").executeUpdate();
            em.createQuery("DELETE FROM InventoryItemEntity ").executeUpdate();
        });
    }

    private void finalizeResourceItemType1(ResourceItemType resourceItemType) {
        resourceItemType.setI18nName(i18nHelper("asdf")).setI18nDescription(i18nHelper("Codsfe gfrgg ms")).setThumbnail(65).setShape3DId(5);
        resourceItemType.setAmount(54).setRadius(31).setFixVerticalNorm(false);
    }

    private void finalizeResourceItemType2(ResourceItemType resourceItemType) {
        resourceItemType.setI18nName(i18nHelper("Razarion")).setI18nDescription(i18nHelper("Harvest Razarion from here")).setThumbnail(64).setShape3DId(4);
        resourceItemType.setAmount(100000).setRadius(3).setFixVerticalNorm(true);
    }
}