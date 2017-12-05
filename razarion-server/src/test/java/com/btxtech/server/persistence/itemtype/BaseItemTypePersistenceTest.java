package com.btxtech.server.persistence.itemtype;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BuilderType;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionParticleConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionStepEffect;
import com.btxtech.shared.gameengine.datatypes.itemtype.FactoryType;
import com.btxtech.shared.gameengine.datatypes.itemtype.HarvesterType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemContainerType;
import com.btxtech.shared.gameengine.datatypes.itemtype.TurretType;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 19.05.2017.
 */
public class BaseItemTypePersistenceTest extends ArquillianBaseTest {
    @Inject
    private ItemTypePersistence itemTypePersistence;

    @Test
    public void testCrud() throws Exception {
        runInTransaction(em -> {
            em.createNativeQuery("INSERT INTO COLLADA (id) VALUES(2)").executeUpdate();
            em.createNativeQuery("INSERT INTO COLLADA (id) VALUES(3)").executeUpdate();
            em.createNativeQuery("INSERT INTO COLLADA (id) VALUES(6)").executeUpdate();
            em.createNativeQuery("INSERT INTO COLLADA (id) VALUES(9)").executeUpdate();
            em.createNativeQuery("INSERT INTO COLLADA (id) VALUES(11)").executeUpdate();
            em.createNativeQuery("INSERT INTO COLLADA (id) VALUES(10)").executeUpdate();
            em.createNativeQuery("INSERT INTO COLLADA (id) VALUES(15)").executeUpdate();
            em.createNativeQuery("INSERT INTO COLLADA (id) VALUES(16)").executeUpdate();
            em.createNativeQuery("INSERT INTO COLLADA (id) VALUES(17)").executeUpdate();
            em.createNativeQuery("INSERT INTO COLLADA (id) VALUES(18)").executeUpdate();
            em.createNativeQuery("INSERT INTO AUDIO_LIBRARY (id, size) VALUES(7, 0)").executeUpdate();
            em.createNativeQuery("INSERT INTO IMAGE_LIBRARY (id, size) VALUES(17, 0)").executeUpdate();
            em.createNativeQuery("INSERT INTO IMAGE_LIBRARY (id, size) VALUES(27, 0)").executeUpdate();
            em.createNativeQuery("INSERT INTO IMAGE_LIBRARY (id, size) VALUES(45, 0)").executeUpdate();
            em.createNativeQuery("INSERT INTO IMAGE_LIBRARY (id, size) VALUES(46, 0)").executeUpdate();
            em.createNativeQuery("INSERT INTO IMAGE_LIBRARY (id, size) VALUES(63, 0)").executeUpdate();
            em.createNativeQuery("INSERT INTO IMAGE_LIBRARY (id, size) VALUES(64, 0)").executeUpdate();
            em.createNativeQuery("INSERT INTO IMAGE_LIBRARY (id, size) VALUES(65, 0)").executeUpdate();
        });

        // Setup factory
        BaseItemType factoryExpected = itemTypePersistence.createBaseItemType();
        // Setup builder
        BaseItemType builderExpected = itemTypePersistence.createBaseItemType();
        finalizeBuilder(builderExpected, factoryExpected.getId());
        itemTypePersistence.updateBaseItemType(builderExpected);
        // Verify
        ReflectionAssert.assertReflectionEquals(builderExpected, getBaseItemType(builderExpected.getId()));
        // Setup harvester
        BaseItemType harvesterExpected = itemTypePersistence.createBaseItemType();
        finalizeHarvester(harvesterExpected);
        itemTypePersistence.updateBaseItemType(harvesterExpected);
        // Verify
        ReflectionAssert.assertReflectionEquals(harvesterExpected, getBaseItemType(harvesterExpected.getId()));
        // Setup attacker
        BaseItemType attackerExpected = itemTypePersistence.createBaseItemType();
        finalizeAttacker(attackerExpected);
        itemTypePersistence.updateBaseItemType(attackerExpected);
        // Verify
        ReflectionAssert.assertReflectionEquals(attackerExpected, getBaseItemType(attackerExpected.getId()));
        // Setup tower
        BaseItemType towerExpected = itemTypePersistence.createBaseItemType();
        finalizeTower(towerExpected);
        itemTypePersistence.updateBaseItemType(towerExpected);
        // Verify
        ReflectionAssert.assertReflectionEquals(towerExpected, getBaseItemType(towerExpected.getId()));
        // Setup transporter
        BaseItemType transporterExpected = itemTypePersistence.createBaseItemType();
        finalizeTransporter(transporterExpected, attackerExpected.getId(), builderExpected.getId());
        itemTypePersistence.updateBaseItemType(transporterExpected);
        // Verify
        ReflectionAssert.assertReflectionEquals(transporterExpected, getBaseItemType(transporterExpected.getId()), ReflectionComparatorMode.LENIENT_ORDER);
        // Finalize
        finalizeFactory(factoryExpected, builderExpected.getId(), attackerExpected.getId(), harvesterExpected.getId());
        factoryExpected.getFactoryType().getAbleToBuildIds().sort(Integer::compareTo);
        itemTypePersistence.updateBaseItemType(factoryExpected);
        // Verify
        assertFactory(factoryExpected);
        Assert.assertEquals(6, itemTypePersistence.readBaseItemTypes().size());
        // Try to delete builder, but should fail
        try {
            itemTypePersistence.deleteBaseItemType(builderExpected.getId());
            Assert.fail("Exception expected. Can not delete builder while factory references it (ableToBuild)");
        } catch (Exception e) {
            // Expected
        }
        // Try to delete attacker, but should fail
        try {
            itemTypePersistence.deleteBaseItemType(attackerExpected.getId());
            Assert.fail("Exception expected. Can not delete builder while transporter references it (ableToContain)");
        } catch (Exception e) {
            // Expected
        }
        Assert.assertEquals(6, itemTypePersistence.readBaseItemTypes().size());
        // Remove attacker from transporter
        finalizeTransporter(transporterExpected, builderExpected.getId());
        transporterExpected.getItemContainerType().setRange(20).setMaxCount(15);
        itemTypePersistence.updateBaseItemType(transporterExpected);
        // Verify
        Assert.assertEquals(6, itemTypePersistence.readBaseItemTypes().size());
        ReflectionAssert.assertReflectionEquals(transporterExpected, getBaseItemType(transporterExpected.getId()), ReflectionComparatorMode.LENIENT_ORDER);
        ReflectionAssert.assertReflectionEquals(builderExpected, getBaseItemType(builderExpected.getId()));
        ReflectionAssert.assertReflectionEquals(harvesterExpected, getBaseItemType(harvesterExpected.getId()));
        ReflectionAssert.assertReflectionEquals(attackerExpected, getBaseItemType(attackerExpected.getId()));
        ReflectionAssert.assertReflectionEquals(towerExpected, getBaseItemType(towerExpected.getId()));
        assertFactory(factoryExpected);
        // Delete transporter
        itemTypePersistence.deleteBaseItemType(transporterExpected.getId());
        // Verify
        Assert.assertEquals(5, itemTypePersistence.readBaseItemTypes().size());
        ReflectionAssert.assertReflectionEquals(builderExpected, getBaseItemType(builderExpected.getId()));
        ReflectionAssert.assertReflectionEquals(harvesterExpected, getBaseItemType(harvesterExpected.getId()));
        ReflectionAssert.assertReflectionEquals(attackerExpected, getBaseItemType(attackerExpected.getId()));
        ReflectionAssert.assertReflectionEquals(towerExpected, getBaseItemType(towerExpected.getId()));
        assertFactory(factoryExpected);
        // Remove builder from factory
        finalizeFactory(factoryExpected, attackerExpected.getId(), harvesterExpected.getId());
        factoryExpected.getFactoryType().getAbleToBuildIds().sort(Integer::compareTo);
        itemTypePersistence.updateBaseItemType(factoryExpected);
        Assert.assertEquals(5, itemTypePersistence.readBaseItemTypes().size());
        ReflectionAssert.assertReflectionEquals(builderExpected, getBaseItemType(builderExpected.getId()));
        ReflectionAssert.assertReflectionEquals(harvesterExpected, getBaseItemType(harvesterExpected.getId()));
        ReflectionAssert.assertReflectionEquals(attackerExpected, getBaseItemType(attackerExpected.getId()));
        ReflectionAssert.assertReflectionEquals(towerExpected, getBaseItemType(towerExpected.getId()));
        assertFactory(factoryExpected);
        // Delete builder
        itemTypePersistence.deleteBaseItemType(builderExpected.getId());
        Assert.assertEquals(4, itemTypePersistence.readBaseItemTypes().size());
        ReflectionAssert.assertReflectionEquals(harvesterExpected, getBaseItemType(harvesterExpected.getId()));
        ReflectionAssert.assertReflectionEquals(attackerExpected, getBaseItemType(attackerExpected.getId()));
        ReflectionAssert.assertReflectionEquals(towerExpected, getBaseItemType(towerExpected.getId()));
        assertFactory(factoryExpected);
        // Delete factory
        itemTypePersistence.deleteBaseItemType(factoryExpected.getId());
        Assert.assertEquals(3, itemTypePersistence.readBaseItemTypes().size());
        ReflectionAssert.assertReflectionEquals(harvesterExpected, getBaseItemType(harvesterExpected.getId()));
        ReflectionAssert.assertReflectionEquals(attackerExpected, getBaseItemType(attackerExpected.getId()));
        ReflectionAssert.assertReflectionEquals(towerExpected, getBaseItemType(towerExpected.getId()));
        // Delete tower
        itemTypePersistence.deleteBaseItemType(towerExpected.getId());
        Assert.assertEquals(2, itemTypePersistence.readBaseItemTypes().size());
        ReflectionAssert.assertReflectionEquals(harvesterExpected, getBaseItemType(harvesterExpected.getId()));
        ReflectionAssert.assertReflectionEquals(attackerExpected, getBaseItemType(attackerExpected.getId()));
        // Delete attacker
        itemTypePersistence.deleteBaseItemType(attackerExpected.getId());
        Assert.assertEquals(1, itemTypePersistence.readBaseItemTypes().size());
        ReflectionAssert.assertReflectionEquals(harvesterExpected, getBaseItemType(harvesterExpected.getId()));
        // Delete harvester
        itemTypePersistence.deleteBaseItemType(harvesterExpected.getId());
        Assert.assertEquals(0, itemTypePersistence.readBaseItemTypes().size());

        // Verify leftovers
        Assert.assertEquals(0, ((Number) getEntityManager().createNativeQuery("SELECT COUNT(*) FROM BASE_ITEM_FACTORY_TYPE_ABLE_TO_BUILD").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createNativeQuery("SELECT COUNT(*) FROM BASE_ITEM_BUILDER_TYPE_ABLE_TO_BUILD").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createNativeQuery("SELECT COUNT(*) FROM BASE_ITEM_WEAPON_TYPE_DISALLOWED_ITEM_TYPES").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(r) FROM DemolitionStepEffectParticleEntity r").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(r) FROM DemolitionStepEffectEntity r").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(r) FROM BaseItemTypeEntity r").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(r) FROM BuilderTypeEntity r").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(r) FROM FactoryTypeEntity r").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(r) FROM HarvesterTypeEntity r").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(r) FROM WeaponTypeEntity r").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(r) FROM TurretTypeEntity r").getSingleResult()).intValue());
        assertEmptyCountNative("BASE_ITEM_ITEM_CONTAINER_TYPE_ABLE_TO_CONTAIN");
        assertEmptyCount(ItemContainerTypeEntity.class);
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(r) FROM I18N_BUNDLE r").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createNativeQuery("SELECT COUNT(*) FROM I18N_BUNDLE_STRING").getSingleResult()).intValue());
    }

    private void assertFactory(BaseItemType factoryExpected) {
        BaseItemType factoryActual = getBaseItemType(factoryExpected.getId());
        factoryActual.getFactoryType().getAbleToBuildIds().sort(Integer::compareTo);
        ReflectionAssert.assertReflectionEquals(factoryExpected, factoryActual);
    }

    @After
    public void clean() throws Exception {
        runInTransaction(em -> {
            em.createQuery("DELETE FROM ColladaEntity").executeUpdate();
            em.createQuery("DELETE FROM AudioLibraryEntity").executeUpdate();
            em.createQuery("DELETE FROM ImageLibraryEntity").executeUpdate();
        });
    }

    private void finalizeBuilder(BaseItemType builder, Integer... ableToBuild) {
        builder.setHealth(5).setSpawnDurationMillis(3000).setSpawnShape3DId(2).setSpawnAudioId(7).setBuildupTextureId(17).setDemolitionImageId(27).setThumbnail(45).setShape3DId(3);
        builder.setI18nName(i18nHelper("Builder"));
        builder.setI18nDescription(i18nHelper("Builds buildings"));
        builder.getPhysicalAreaConfig().setRadius(3).setAcceleration(40.0).setSpeed(10.0).setAngularVelocity(Math.toRadians(60));
        BuilderType builderType = new BuilderType().setProgress(1).setRange(10).setAnimationShape3dId(9).setAnimationOrigin(new Vertex(1.63196, 0, 3.04829));
        if (ableToBuild.length > 0) {
            builderType.setAbleToBuildIds(Arrays.asList(ableToBuild));
        }
        builder.setBuilderType(builderType);
        builder.setBoxPickupRange(2).setExplosionParticleConfigId(2).setBuildup(30);
        builder.setPrice(100).setWreckageShape3DId(16);
    }

    private void finalizeHarvester(BaseItemType harvester) {
        harvester.setHealth(5).setSpawnDurationMillis(3000).setSpawnShape3DId(2).setSpawnAudioId(7).setBuildupTextureId(17).setDemolitionImageId(27).setThumbnail(64).setShape3DId(17);
        harvester.setI18nName(i18nHelper("Harvester"));
        harvester.setI18nDescription(i18nHelper("Collects resources"));
        harvester.getPhysicalAreaConfig().setRadius(3).setAcceleration(5.0).setSpeed(15.0).setAngularVelocity(Math.toRadians(60));
        harvester.setHarvesterType(new HarvesterType().setProgress(10).setRange(3).setAnimationShape3dId(18).setAnimationOrigin(new Vertex(2.5, 0, 1.25)));
        harvester.setBoxPickupRange(2).setExplosionParticleConfigId(2).setBuildup(20);
        harvester.setPrice(100).setWreckageShape3DId(16);
    }

    private void finalizeAttacker(BaseItemType attacker) {
        attacker.setHealth(5).setSpawnDurationMillis(3000).setSpawnShape3DId(2).setSpawnAudioId(7).setBuildupTextureId(17).setDemolitionImageId(27).setThumbnail(63).setShape3DId(11);
        attacker.setI18nName(i18nHelper("Attacker"));
        attacker.setI18nDescription(i18nHelper("Attacks other units"));
        attacker.getPhysicalAreaConfig().setRadius(2).setAcceleration(5.0).setSpeed(17.0).setAngularVelocity(Math.toRadians(60));
        attacker.setWeaponType(new WeaponType().setRange(10).setDamage(1).setReloadTime(3).setDetonationRadius(1).setProjectileSpeed(17.0).setProjectileShape3DId(6).setMuzzleFlashParticleConfigId(4).setDetonationParticleConfigId(3).setTurretType(new TurretType().setAngleVelocity(Math.toRadians(120)).setTorrentCenter(new Vertex(-0.25, 0, 2)).setMuzzlePosition(new Vertex(1.3, 0, 0)).setShape3dMaterialId("Turret-material")));
        attacker.setBoxPickupRange(2).setExplosionParticleConfigId(2).setBuildup(15);
        attacker.setPrice(100).setWreckageShape3DId(16);
    }

    private void finalizeTower(BaseItemType tower) {
        tower.setHealth(5).setSpawnDurationMillis(3000).setSpawnShape3DId(2).setSpawnAudioId(7).setBuildupTextureId(17).setDemolitionImageId(27).setThumbnail(65).setShape3DId(11);
        tower.setI18nName(i18nHelper("Tower"));
        tower.setI18nDescription(i18nHelper("Defense tower"));
        tower.getPhysicalAreaConfig().setRadius(4).setFixVerticalNorm(true);
        tower.setWeaponType(new WeaponType().setRange(20).setDamage(1).setReloadTime(3).setDetonationRadius(1).setProjectileSpeed(40.0).setProjectileShape3DId(6).setMuzzleFlashParticleConfigId(4).setDetonationParticleConfigId(3).setTurretType(new TurretType().setAngleVelocity(Math.toRadians(120)).setTorrentCenter(new Vertex(0, 0, 0.98)).setMuzzlePosition(new Vertex(5.2, 0, 5.4)).setShape3dMaterialId("turret_001-material")));
        tower.setExplosionParticleConfigId(2).setWreckageShape3DId(15).setBuildup(45);
        List<DemolitionStepEffect> demolitionStepEffects = new ArrayList<>();
        // Demolition 1
        List<DemolitionParticleConfig> demolitionShape3Ds1 = new ArrayList<>();
        demolitionShape3Ds1.add(new DemolitionParticleConfig().setParticleConfigId(1).setPosition(new Vertex(0, 0, 3)));
        demolitionStepEffects.add(new DemolitionStepEffect().setDemolitionParticleConfigs(demolitionShape3Ds1));
        // Demolition 2
        List<DemolitionParticleConfig> demolitionParticleConfig2s = new ArrayList<>();
        demolitionParticleConfig2s.add(new DemolitionParticleConfig().setParticleConfigId(1).setPosition(new Vertex(2, 2, 2)));
        demolitionParticleConfig2s.add(new DemolitionParticleConfig().setParticleConfigId(1).setPosition(new Vertex(-2, -2, 2)));
        demolitionStepEffects.add(new DemolitionStepEffect().setDemolitionParticleConfigs(demolitionParticleConfig2s));
        // Demolition 3
        List<DemolitionParticleConfig> demolitionShape3D3s = new ArrayList<>();
        demolitionShape3D3s.add(new DemolitionParticleConfig().setParticleConfigId(1).setPosition(new Vertex(3, 0, 1)));
        demolitionShape3D3s.add(new DemolitionParticleConfig().setParticleConfigId(1).setPosition(new Vertex(0, 3, 1)));
        demolitionShape3D3s.add(new DemolitionParticleConfig().setParticleConfigId(1).setPosition(new Vertex(3, 3, 1)));
        demolitionStepEffects.add(new DemolitionStepEffect().setDemolitionParticleConfigs(demolitionShape3D3s));
        tower.setDemolitionStepEffects(demolitionStepEffects);
    }

    private void finalizeFactory(BaseItemType factory, Integer... ableToBuild) {
        factory.setHealth(5).setSpawnDurationMillis(3000).setSpawnShape3DId(2).setSpawnAudioId(7).setBuildupTextureId(17).setDemolitionImageId(27).setThumbnail(46).setShape3DId(10);
        factory.setI18nName(i18nHelper("Factory"));
        factory.setI18nDescription(i18nHelper("Creates units"));
        factory.setExplosionParticleConfigId(2).setBuildup(30);
        factory.getPhysicalAreaConfig().setRadius(6).setFixVerticalNorm(true);
        FactoryType factoryType = new FactoryType().setProgress(1.0).setAbleToBuildIds(Arrays.asList(ableToBuild));
        if (ableToBuild.length > 0) {
            factoryType.setAbleToBuildIds(Arrays.asList(ableToBuild));
        }
        factory.setFactoryType(factoryType);
        factory.setPrice(200).setWreckageShape3DId(15);
        List<DemolitionStepEffect> demolitionStepEffects = new ArrayList<>();
        // Demolition 1
        List<DemolitionParticleConfig> demolitionShape3Ds1 = new ArrayList<>();
        demolitionShape3Ds1.add(new DemolitionParticleConfig().setParticleConfigId(5).setPosition(new Vertex(-2.1, 2.0, 3.4)));
        demolitionStepEffects.add(new DemolitionStepEffect().setDemolitionParticleConfigs(demolitionShape3Ds1));
        // Demolition 2
        List<DemolitionParticleConfig> demolitionParticleConfig2s = new ArrayList<>();
        demolitionParticleConfig2s.add(new DemolitionParticleConfig().setParticleConfigId(1).setPosition(new Vertex(-2.1, 2.0, 3.4)));
        demolitionParticleConfig2s.add(new DemolitionParticleConfig().setParticleConfigId(5).setPosition(new Vertex(3, 0.47, 3)));
        demolitionStepEffects.add(new DemolitionStepEffect().setDemolitionParticleConfigs(demolitionParticleConfig2s));
        // Demolition 3
        List<DemolitionParticleConfig> demolitionShape3D3s = new ArrayList<>();
        demolitionShape3D3s.add(new DemolitionParticleConfig().setParticleConfigId(1).setPosition(new Vertex(-2.1, 2.0, 3.4)));
        demolitionShape3D3s.add(new DemolitionParticleConfig().setParticleConfigId(1).setPosition(new Vertex(3, 0.47, 3)));
        demolitionStepEffects.add(new DemolitionStepEffect().setDemolitionParticleConfigs(demolitionShape3D3s));
        factory.setDemolitionStepEffects(demolitionStepEffects);
    }

    private void finalizeTransporter(BaseItemType transporter, Integer... ableToBuild) {
        transporter.setHealth(19).setSpawnDurationMillis(2000).setSpawnShape3DId(2).setSpawnAudioId(7).setBuildupTextureId(65).setDemolitionImageId(27).setThumbnail(46).setShape3DId(10);
        transporter.setI18nName(i18nHelper("Transporter"));
        transporter.setI18nDescription(i18nHelper("Transports units"));
        transporter.setExplosionParticleConfigId(5).setBuildup(35);
        transporter.getPhysicalAreaConfig().setRadius(4).setAcceleration(2.0).setSpeed(10.0).setAngularVelocity(Math.toRadians(40));
        transporter.setBoxPickupRange(4).setExplosionParticleConfigId(7).setBuildup(25);
        transporter.setPrice(200).setWreckageShape3DId(16);
        // Container
        ItemContainerType itemContainerType = new ItemContainerType().setMaxCount(4).setRange(11.5);
        if (ableToBuild.length > 0) {
            itemContainerType.setAbleToContain(Arrays.asList(ableToBuild));
        }
        transporter.setItemContainerType(itemContainerType);
    }


    private BaseItemType getBaseItemType(int id) {
        for (BaseItemType baseItemType : itemTypePersistence.readBaseItemTypes()) {
            if (baseItemType.getId() == id) {
                return baseItemType;
            }
        }
        throw new IllegalArgumentException("No BaseItemType for id: " + id);
    }

}