package com.btxtech.server.persistence;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.DbPropertyKey;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;

/**
 * Created by Beat
 * 15.05.2017.
 */
public class DbPropertiesServiceTest extends ArquillianBaseTest {
    @Inject
    private DbPropertiesService dbPropertiesService;

    @Test
    public void crud() throws Exception {
        // Verify empty
        Assert.assertEquals(0, dbPropertiesService.getIntProperty(DbPropertyKey.TIP_CORNER_MOVE_DURATION));
        Assert.assertEquals(0.0, dbPropertiesService.getDoubleProperty(DbPropertyKey.TIP_CORNER_MOVE_DISTANCE), 0.0001);
        Assert.assertNull(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_BASE_LOST));
        Assert.assertNull(dbPropertiesService.getImageIdProperty(DbPropertyKey.TIP_SCROLL_IMAGE));
        Assert.assertNull(dbPropertiesService.getShape3DIdProperty(DbPropertyKey.TIP_BASE_ITEM_PLACER_SHAPE3D));
        Assert.assertNull(dbPropertiesService.getColorProperty(DbPropertyKey.TIP_ATTACK_COMMAND_CORNER_COLOR));
        // Test int value
        dbPropertiesService.setIntProperty(11, DbPropertyKey.TIP_CORNER_MOVE_DURATION);
        Assert.assertEquals(11, dbPropertiesService.getIntProperty(DbPropertyKey.TIP_CORNER_MOVE_DURATION));
        dbPropertiesService.setIntProperty(22, DbPropertyKey.TIP_CORNER_MOVE_DURATION);
        Assert.assertEquals(22, dbPropertiesService.getIntProperty(DbPropertyKey.TIP_CORNER_MOVE_DURATION));
        dbPropertiesService.setIntProperty(null, DbPropertyKey.TIP_CORNER_MOVE_DURATION);
        Assert.assertEquals(0, dbPropertiesService.getIntProperty(DbPropertyKey.TIP_CORNER_MOVE_DURATION));
        // Test double value
        dbPropertiesService.setDoubleProperty(123.987, DbPropertyKey.TIP_CORNER_MOVE_DISTANCE);
        Assert.assertEquals(123.987, dbPropertiesService.getDoubleProperty(DbPropertyKey.TIP_CORNER_MOVE_DISTANCE), 0.0001);
        dbPropertiesService.setDoubleProperty(-1000.5, DbPropertyKey.TIP_CORNER_MOVE_DISTANCE);
        Assert.assertEquals(-1000.5, dbPropertiesService.getDoubleProperty(DbPropertyKey.TIP_CORNER_MOVE_DISTANCE), 0.0001);
        dbPropertiesService.setDoubleProperty(null, DbPropertyKey.TIP_CORNER_MOVE_DISTANCE);
        Assert.assertEquals(0, dbPropertiesService.getDoubleProperty(DbPropertyKey.TIP_CORNER_MOVE_DISTANCE), 0.0001);
        // Test audio value
        AudioLibraryEntity audio1 = persistInTransaction(new AudioLibraryEntity());
        AudioLibraryEntity audio2 = persistInTransaction(new AudioLibraryEntity());
        dbPropertiesService.setAudioIdProperty(audio1.getId(), DbPropertyKey.AUDIO_BASE_LOST);
        Assert.assertEquals(audio1.getId(), dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_BASE_LOST));
        dbPropertiesService.setAudioIdProperty(audio2.getId(), DbPropertyKey.AUDIO_BASE_LOST);
        Assert.assertEquals(audio2.getId(), dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_BASE_LOST));
        dbPropertiesService.setAudioIdProperty(null, DbPropertyKey.AUDIO_BASE_LOST);
        Assert.assertNull(dbPropertiesService.getAudioIdProperty(DbPropertyKey.AUDIO_BASE_LOST));
        // Test image value
        ImageLibraryEntity image1 = persistInTransaction(new ImageLibraryEntity());
        ImageLibraryEntity image2 = persistInTransaction(new ImageLibraryEntity());
        dbPropertiesService.setImageIdProperty(image1.getId(), DbPropertyKey.TIP_SCROLL_IMAGE);
        Assert.assertEquals(image1.getId(), dbPropertiesService.getImageIdProperty(DbPropertyKey.TIP_SCROLL_IMAGE));
        dbPropertiesService.setImageIdProperty(image2.getId(), DbPropertyKey.TIP_SCROLL_IMAGE);
        Assert.assertEquals(image2.getId(), dbPropertiesService.getImageIdProperty(DbPropertyKey.TIP_SCROLL_IMAGE));
        dbPropertiesService.setImageIdProperty(null, DbPropertyKey.TIP_SCROLL_IMAGE);
        Assert.assertNull(dbPropertiesService.getImageIdProperty(DbPropertyKey.TIP_SCROLL_IMAGE));
        // Test shape3DId value
        ColladaEntity shape3D1 = persistInTransaction(new ColladaEntity());
        ColladaEntity shape3D2 = persistInTransaction(new ColladaEntity());
        dbPropertiesService.setshape3DIdProperty(shape3D1.getId(), DbPropertyKey.TIP_BASE_ITEM_PLACER_SHAPE3D);
        Assert.assertEquals(shape3D1.getId(), dbPropertiesService.getShape3DIdProperty(DbPropertyKey.TIP_BASE_ITEM_PLACER_SHAPE3D));
        dbPropertiesService.setshape3DIdProperty(shape3D2.getId(), DbPropertyKey.TIP_BASE_ITEM_PLACER_SHAPE3D);
        Assert.assertEquals(shape3D2.getId(), dbPropertiesService.getShape3DIdProperty(DbPropertyKey.TIP_BASE_ITEM_PLACER_SHAPE3D));
        dbPropertiesService.setshape3DIdProperty(null, DbPropertyKey.TIP_BASE_ITEM_PLACER_SHAPE3D);
        Assert.assertNull(dbPropertiesService.getShape3DIdProperty(DbPropertyKey.TIP_BASE_ITEM_PLACER_SHAPE3D));
        // Test color value
        dbPropertiesService.setColorProperty(new Color(0.1, 0.3, 0.5, 0.7), DbPropertyKey.TIP_ATTACK_COMMAND_CORNER_COLOR);
        assertColor(new Color(0.1, 0.3, 0.5, 0.7), dbPropertiesService.getColorProperty(DbPropertyKey.TIP_ATTACK_COMMAND_CORNER_COLOR));
        dbPropertiesService.setColorProperty(new Color(0.2, 0.4, 0.6, 0.9), DbPropertyKey.TIP_ATTACK_COMMAND_CORNER_COLOR);
        assertColor(new Color(0.2, 0.4, 0.6, 0.9), dbPropertiesService.getColorProperty(DbPropertyKey.TIP_ATTACK_COMMAND_CORNER_COLOR));
        dbPropertiesService.setColorProperty(null, DbPropertyKey.TIP_ATTACK_COMMAND_CORNER_COLOR);
        Assert.assertNull(dbPropertiesService.getColorProperty(DbPropertyKey.TIP_ATTACK_COMMAND_CORNER_COLOR));

        // Cleanup
        runInTransaction(entityManager -> {
            entityManager.createQuery("DELETE FROM DbPropertiesEntity").executeUpdate();
            entityManager.createQuery("DELETE FROM ImageLibraryEntity").executeUpdate();
            entityManager.createQuery("DELETE FROM AudioLibraryEntity").executeUpdate();
            entityManager.createQuery("DELETE FROM ColladaEntity").executeUpdate();
        });
    }

    private void assertColor(Color expected, Color actual) {
        Assert.assertEquals("R value of color is not the same", expected.getR(), actual.getR(), 0.0001);
        Assert.assertEquals("G value of color is not the same", expected.getG(), actual.getG(), 0.0001);
        Assert.assertEquals("B value of color is not the same", expected.getB(), actual.getB(), 0.0001);
        Assert.assertEquals("A value of color is not the same", expected.getA(), actual.getA(), 0.0001);
    }

}