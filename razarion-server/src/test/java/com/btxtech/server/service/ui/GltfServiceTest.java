package com.btxtech.server.service.ui;

import com.btxtech.server.model.ui.GltfEntity;
import com.btxtech.server.repository.ui.GltfRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The digest is what lets a browser keep the eleven-megabyte model file without ever being able to
 * keep it past an edit. That guarantee rests entirely on the digest moving whenever the bytes move
 * - a digest that lagged behind a replaced model would hand every holder of the old file a "still
 * current" answer, which is worse than the download it saves.
 */
class GltfServiceTest {
    private final GltfRepository gltfRepository = mock(GltfRepository.class);
    private final GltfService gltfService = new GltfService(gltfRepository, mock(BabylonMaterialService.class));

    private GltfEntity stored(byte[] glb, String digest) {
        GltfEntity entity = new GltfEntity();
        entity.setGlb(glb);
        entity.setGlbDigest(digest);
        when(gltfRepository.findById(1)).thenReturn(Optional.of(entity));
        return entity;
    }

    @Test
    void replacingTheModelReplacesItsDigest() {
        GltfEntity entity = stored(new byte[]{1, 2, 3}, null);
        gltfService.setGlb(1, new byte[]{1, 2, 3});
        String first = entity.getGlbDigest();

        gltfService.setGlb(1, new byte[]{9, 9, 9});

        assertNotNull(first);
        assertNotEquals(first, entity.getGlbDigest());
    }

    /** Same bytes, same tag - otherwise every upload of an unchanged file would cost every player a download. */
    @Test
    void thesameModelKeepsTheSameDigest() {
        GltfEntity entity = stored(null, null);
        gltfService.setGlb(1, new byte[]{4, 5, 6});
        String first = entity.getGlbDigest();
        gltfService.setGlb(1, new byte[]{4, 5, 6});

        assertEquals(first, entity.getGlbDigest());
    }

    /** Rows written before the column existed have no digest. It is computed once and kept. */
    @Test
    void aRowFromBeforeThisChangeIsBackfilledOnFirstUse() {
        GltfEntity entity = stored(new byte[]{7, 7}, null);

        String digest = gltfService.getGlbDigest(1);

        assertNotNull(digest);
        assertEquals(digest, entity.getGlbDigest());
        verify(gltfRepository).save(entity);
    }

    /**
     * Reading a digest that is already there must not write, and must not touch the blob. The blob
     * is lazily fetched and eleven megabytes; loading it to answer "unchanged" would give back
     * exactly what the entity tag was introduced to save.
     */
    @Test
    void anExistingDigestIsReadWithoutWritingAnything() {
        stored(new byte[]{7, 7}, "already-there");

        assertEquals("already-there", gltfService.getGlbDigest(1));
        verify(gltfRepository, never()).save(any());
    }

    /** No bytes, no tag - and no digest invented for emptiness. */
    @Test
    void aRowWithoutAModelHasNoDigest() {
        stored(null, null);

        assertNull(gltfService.getGlbDigest(1));
    }
}
