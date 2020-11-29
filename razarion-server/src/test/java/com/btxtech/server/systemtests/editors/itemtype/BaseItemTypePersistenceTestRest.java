package com.btxtech.server.systemtests.editors.itemtype;

import com.btxtech.server.persistence.itemtype.ItemContainerTypeEntity;
import com.btxtech.server.systemtests.framework.AbstractSystemTest;
import com.btxtech.server.systemtests.framework.ObjectMapperResolver;
import com.btxtech.server.systemtests.framework.RestConnection;
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
import com.btxtech.shared.rest.BaseItemTypeEditorController;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 19.05.2017.
 */
public class BaseItemTypePersistenceTestRest extends AbstractSystemTest {

    @Before
    public void fillTables() {
        setupAudios();
        setupImages();
        setupShape3dConfig();
    }

    @Test
    public void testNotAuthorized() {
        runUnauthorizedTest(BaseItemTypeEditorController.class, BaseItemTypeEditorController::create, RestConnection.TestUser.NONE, RestConnection.TestUser.USER);
        runUnauthorizedTest(BaseItemTypeEditorController.class, baseItemTypeEditorController -> baseItemTypeEditorController.update(new BaseItemType()), RestConnection.TestUser.NONE, RestConnection.TestUser.USER);
        runUnauthorizedTest(BaseItemTypeEditorController.class, baseItemTypeEditorController -> baseItemTypeEditorController.delete(1), RestConnection.TestUser.NONE, RestConnection.TestUser.USER);
    }

    @Test
    public void testCrud() {
        RestConnection restConnection = new RestConnection(new ObjectMapperResolver(() -> BaseItemType.class));
        restConnection.loginAdmin();
        BaseItemTypeEditorController systemUnderTest = restConnection.proxy(BaseItemTypeEditorController.class);

        // Setup factory
        BaseItemType factoryExpected = systemUnderTest.create();
        // Setup builder
        BaseItemType builderExpected = systemUnderTest.create();
        finalizeBuilder(builderExpected, factoryExpected.getId());
        systemUnderTest.update(builderExpected);
        // Verify
        ReflectionAssert.assertReflectionEquals(builderExpected, systemUnderTest.read(builderExpected.getId()));
        // Setup harvester
        BaseItemType harvesterExpected = systemUnderTest.create();
        finalizeHarvester(harvesterExpected);
        systemUnderTest.update(harvesterExpected);
        // Verify
        ReflectionAssert.assertReflectionEquals(harvesterExpected, systemUnderTest.read(harvesterExpected.getId()));
        // Setup attacker
        BaseItemType attackerExpected = systemUnderTest.create();
        finalizeAttacker(attackerExpected);
        systemUnderTest.update(attackerExpected);
        // Verify
        ReflectionAssert.assertReflectionEquals(attackerExpected, systemUnderTest.read(attackerExpected.getId()));
        // Setup tower
        BaseItemType towerExpected = systemUnderTest.create();
        finalizeTower(towerExpected);
        systemUnderTest.update(towerExpected);
        // Verify
        ReflectionAssert.assertReflectionEquals(towerExpected, systemUnderTest.read(towerExpected.getId()));
        // Setup transporter
        BaseItemType transporterExpected = systemUnderTest.create();
        finalizeTransporter(transporterExpected, attackerExpected.getId(), builderExpected.getId());
        systemUnderTest.update(transporterExpected);
        // Verify
        ReflectionAssert.assertReflectionEquals(transporterExpected, systemUnderTest.read(transporterExpected.getId()), ReflectionComparatorMode.LENIENT_ORDER);
        // Finalize
        finalizeFactory(factoryExpected, builderExpected.getId(), attackerExpected.getId(), harvesterExpected.getId());
        factoryExpected.getFactoryType().getAbleToBuildIds().sort(Integer::compareTo);
        systemUnderTest.update(factoryExpected);
        // Verify
        assertFactory(factoryExpected, systemUnderTest);
        Assert.assertEquals(6, systemUnderTest.read().size());
        // Try to delete builder, but should fail
        try {
            systemUnderTest.delete(builderExpected.getId());
            Assert.fail("Exception expected. Can not delete builder while factory references it (ableToBuild)");
        } catch (Exception e) {
            // Expected
        }
        // Try to delete attacker, but should fail
        try {
            systemUnderTest.delete(attackerExpected.getId());
            Assert.fail("Exception expected. Can not delete builder while transporter references it (ableToContain)");
        } catch (Exception e) {
            // Expected
        }
        Assert.assertEquals(6, systemUnderTest.read().size());
        // Remove attacker from transporter
        finalizeTransporter(transporterExpected, builderExpected.getId());
        transporterExpected.getItemContainerType().setRange(20).setMaxCount(15);
        systemUnderTest.update(transporterExpected);
        // Verify
        Assert.assertEquals(6, systemUnderTest.read().size());
        ReflectionAssert.assertReflectionEquals(transporterExpected, systemUnderTest.read(transporterExpected.getId()), ReflectionComparatorMode.LENIENT_ORDER);
        ReflectionAssert.assertReflectionEquals(builderExpected, systemUnderTest.read(builderExpected.getId()));
        ReflectionAssert.assertReflectionEquals(harvesterExpected, systemUnderTest.read(harvesterExpected.getId()));
        ReflectionAssert.assertReflectionEquals(attackerExpected, systemUnderTest.read(attackerExpected.getId()));
        ReflectionAssert.assertReflectionEquals(towerExpected, systemUnderTest.read(towerExpected.getId()));
        assertFactory(factoryExpected, systemUnderTest);
        // Delete transporter
        systemUnderTest.delete(transporterExpected.getId());
        // Verify
        Assert.assertEquals(5, systemUnderTest.read().size());
        ReflectionAssert.assertReflectionEquals(builderExpected, systemUnderTest.read(builderExpected.getId()));
        ReflectionAssert.assertReflectionEquals(harvesterExpected, systemUnderTest.read(harvesterExpected.getId()));
        ReflectionAssert.assertReflectionEquals(attackerExpected, systemUnderTest.read(attackerExpected.getId()));
        ReflectionAssert.assertReflectionEquals(towerExpected, systemUnderTest.read(towerExpected.getId()));
        assertFactory(factoryExpected, systemUnderTest);
        // Remove builder from factory
        finalizeFactory(factoryExpected, attackerExpected.getId(), harvesterExpected.getId());
        factoryExpected.getFactoryType().getAbleToBuildIds().sort(Integer::compareTo);
        systemUnderTest.update(factoryExpected);
        Assert.assertEquals(5, systemUnderTest.read().size());
        ReflectionAssert.assertReflectionEquals(builderExpected, systemUnderTest.read(builderExpected.getId()));
        ReflectionAssert.assertReflectionEquals(harvesterExpected, systemUnderTest.read(harvesterExpected.getId()));
        ReflectionAssert.assertReflectionEquals(attackerExpected, systemUnderTest.read(attackerExpected.getId()));
        ReflectionAssert.assertReflectionEquals(towerExpected, systemUnderTest.read(towerExpected.getId()));
        assertFactory(factoryExpected, systemUnderTest);
        // Delete builder
        systemUnderTest.delete(builderExpected.getId());
        Assert.assertEquals(4, systemUnderTest.read().size());
        ReflectionAssert.assertReflectionEquals(harvesterExpected, systemUnderTest.read(harvesterExpected.getId()));
        ReflectionAssert.assertReflectionEquals(attackerExpected, systemUnderTest.read(attackerExpected.getId()));
        ReflectionAssert.assertReflectionEquals(towerExpected, systemUnderTest.read(towerExpected.getId()));
        assertFactory(factoryExpected, systemUnderTest);
        // Delete factory
        systemUnderTest.delete(factoryExpected.getId());
        Assert.assertEquals(3, systemUnderTest.read().size());
        ReflectionAssert.assertReflectionEquals(harvesterExpected, systemUnderTest.read(harvesterExpected.getId()));
        ReflectionAssert.assertReflectionEquals(attackerExpected, systemUnderTest.read(attackerExpected.getId()));
        ReflectionAssert.assertReflectionEquals(towerExpected, systemUnderTest.read(towerExpected.getId()));
        // Delete tower
        systemUnderTest.delete(towerExpected.getId());
        Assert.assertEquals(2, systemUnderTest.read().size());
        ReflectionAssert.assertReflectionEquals(harvesterExpected, systemUnderTest.read(harvesterExpected.getId()));
        ReflectionAssert.assertReflectionEquals(attackerExpected, systemUnderTest.read(attackerExpected.getId()));
        // Delete attacker
        systemUnderTest.delete(attackerExpected.getId());
        Assert.assertEquals(1, systemUnderTest.read().size());
        ReflectionAssert.assertReflectionEquals(harvesterExpected, systemUnderTest.read(harvesterExpected.getId()));
        // Delete harvester
        systemUnderTest.delete(harvesterExpected.getId());
        Assert.assertEquals(0, systemUnderTest.read().size());

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

    private void assertFactory(BaseItemType factoryExpected, BaseItemTypeEditorController baseItemTypeEditorController) {
        BaseItemType factoryActual = baseItemTypeEditorController.read(factoryExpected.getId());
        factoryActual.getFactoryType().getAbleToBuildIds().sort(Integer::compareTo);
        ReflectionAssert.assertReflectionEquals(factoryExpected, factoryActual);
    }

    private void finalizeBuilder(BaseItemType builder, Integer... ableToBuild) {
        builder.setHealth(5).setSpawnDurationMillis(3000).setSpawnShape3DId(SHAPE_3D_1_ID).setSpawnAudioId(AUDIO_1_ID).setBuildupTextureId(IMAGE_1_ID).setDemolitionImageId(IMAGE_2_ID).setThumbnail(IMAGE_3_ID).setShape3DId(SHAPE_3D_1_ID);
        builder.setI18nName(i18nHelper("Builder"));
        builder.setI18nDescription(i18nHelper("Builds buildings"));
        builder.getPhysicalAreaConfig().setRadius(3).setAcceleration(40.0).setSpeed(10.0).setAngularVelocity(Math.toRadians(60));
        BuilderType builderType = new BuilderType().setProgress(1).setRange(10).setAnimationShape3dId(SHAPE_3D_2_ID).setAnimationOrigin(new Vertex(1.63196, 0, 3.04829));
        if (ableToBuild.length > 0) {
            builderType.setAbleToBuildIds(Arrays.asList(ableToBuild));
        }
        builder.setBuilderType(builderType);
        builder.setBoxPickupRange(2).setExplosionParticleConfigId(2).setBuildup(30);
        builder.setPrice(100).setWreckageShape3DId(SHAPE_3D_1_ID);
    }

    private void finalizeHarvester(BaseItemType harvester) {
        harvester.setHealth(5).setSpawnDurationMillis(3000).setSpawnShape3DId(SHAPE_3D_1_ID).setSpawnAudioId(AUDIO_2_ID).setBuildupTextureId(IMAGE_1_ID).setDemolitionImageId(IMAGE_3_ID).setThumbnail(IMAGE_1_ID).setShape3DId(SHAPE_3D_1_ID);
        harvester.setI18nName(i18nHelper("Harvester"));
        harvester.setI18nDescription(i18nHelper("Collects resources"));
        harvester.getPhysicalAreaConfig().setRadius(3).setAcceleration(5.0).setSpeed(15.0).setAngularVelocity(Math.toRadians(60));
        harvester.setHarvesterType(new HarvesterType().setProgress(10).setRange(3).setAnimationShape3dId(SHAPE_3D_3_ID).setAnimationOrigin(new Vertex(2.5, 0, 1.25)));
        harvester.setBoxPickupRange(2).setExplosionParticleConfigId(2).setBuildup(20);
        harvester.setPrice(100).setWreckageShape3DId(SHAPE_3D_2_ID);
    }

    private void finalizeAttacker(BaseItemType attacker) {
        attacker.setHealth(5).setSpawnDurationMillis(3000).setSpawnShape3DId(SHAPE_3D_2_ID).setSpawnAudioId(AUDIO_3_ID).setBuildupTextureId(IMAGE_1_ID).setDemolitionImageId(IMAGE_1_ID).setThumbnail(IMAGE_2_ID).setShape3DId(SHAPE_3D_3_ID);
        attacker.setI18nName(i18nHelper("Attacker"));
        attacker.setI18nDescription(i18nHelper("Attacks other units"));
        attacker.getPhysicalAreaConfig().setRadius(2).setAcceleration(5.0).setSpeed(17.0).setAngularVelocity(Math.toRadians(60));
        attacker.setWeaponType(new WeaponType().setRange(10).setDamage(1).setReloadTime(3).setDetonationRadius(1).setProjectileSpeed(17.0).setProjectileShape3DId(SHAPE_3D_1_ID).setMuzzleFlashParticleConfigId(4).setDetonationParticleConfigId(3).setTurretType(new TurretType().setAngleVelocity(Math.toRadians(120)).setTurretCenter(new Vertex(-0.25, 0, 2)).setMuzzlePosition(new Vertex(1.3, 0, 0)).setShape3dMaterialId("Turret-material")));
        attacker.setBoxPickupRange(2).setExplosionParticleConfigId(2).setBuildup(15);
        attacker.setPrice(100).setWreckageShape3DId(SHAPE_3D_3_ID);
    }

    private void finalizeTower(BaseItemType tower) {
        tower.setHealth(5).setSpawnDurationMillis(3000).setSpawnShape3DId(SHAPE_3D_1_ID).setSpawnAudioId(AUDIO_1_ID).setBuildupTextureId(IMAGE_2_ID).setDemolitionImageId(IMAGE_3_ID).setThumbnail(IMAGE_3_ID).setShape3DId(SHAPE_3D_3_ID);
        tower.setI18nName(i18nHelper("Tower"));
        tower.setI18nDescription(i18nHelper("Defense tower"));
        tower.getPhysicalAreaConfig().setRadius(4).setFixVerticalNorm(true);
        tower.setWeaponType(new WeaponType().setRange(20).setDamage(1).setReloadTime(3).setDetonationRadius(1).setProjectileSpeed(40.0).setProjectileShape3DId(SHAPE_3D_2_ID).setMuzzleFlashParticleConfigId(4).setDetonationParticleConfigId(3).setTurretType(new TurretType().setAngleVelocity(Math.toRadians(120)).setTurretCenter(new Vertex(0, 0, 0.98)).setMuzzlePosition(new Vertex(5.2, 0, 5.4)).setShape3dMaterialId("turret_001-material")));
        tower.setExplosionParticleConfigId(2).setWreckageShape3DId(SHAPE_3D_1_ID).setBuildup(45);
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
        factory.setHealth(5).setSpawnDurationMillis(3000).setSpawnShape3DId(SHAPE_3D_1_ID).setSpawnAudioId(AUDIO_2_ID).setBuildupTextureId(IMAGE_2_ID).setDemolitionImageId(IMAGE_1_ID).setThumbnail(IMAGE_1_ID).setShape3DId(SHAPE_3D_2_ID);
        factory.setI18nName(i18nHelper("Factory"));
        factory.setI18nDescription(i18nHelper("Creates units"));
        factory.setExplosionParticleConfigId(2).setBuildup(30);
        factory.getPhysicalAreaConfig().setRadius(6).setFixVerticalNorm(true);
        FactoryType factoryType = new FactoryType().setProgress(1.0).setAbleToBuildIds(Arrays.asList(ableToBuild));
        if (ableToBuild.length > 0) {
            factoryType.setAbleToBuildIds(Arrays.asList(ableToBuild));
        }
        factory.setFactoryType(factoryType);
        factory.setPrice(200).setWreckageShape3DId(SHAPE_3D_2_ID);
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
        transporter.setHealth(19).setSpawnDurationMillis(2000).setSpawnShape3DId(SHAPE_3D_1_ID).setSpawnAudioId(AUDIO_1_ID).setBuildupTextureId(IMAGE_1_ID).setDemolitionImageId(IMAGE_3_ID).setThumbnail(IMAGE_2_ID).setShape3DId(SHAPE_3D_2_ID);
        transporter.setI18nName(i18nHelper("Transporter"));
        transporter.setI18nDescription(i18nHelper("Transports units"));
        transporter.setExplosionParticleConfigId(5).setBuildup(35);
        transporter.getPhysicalAreaConfig().setRadius(4).setAcceleration(2.0).setSpeed(10.0).setAngularVelocity(Math.toRadians(40));
        transporter.setBoxPickupRange(4).setExplosionParticleConfigId(7).setBuildup(25);
        transporter.setPrice(200).setWreckageShape3DId(SHAPE_3D_3_ID);
        // Container
        ItemContainerType itemContainerType = new ItemContainerType().setMaxCount(4).setRange(11.5);
        if (ableToBuild.length > 0) {
            itemContainerType.setAbleToContain(Arrays.asList(ableToBuild));
        }
        transporter.setItemContainerType(itemContainerType);
    }
}