package com.btxtech.server.persistence;

import com.btxtech.server.RestServerTestBase;
import com.btxtech.shared.dto.ImageGalleryItem;
import com.btxtech.shared.rest.ImageProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;

public class ImageProviderTest extends RestServerTestBase {
    private ImageProvider imageProvider;

    @Before
    public void setup() {
        imageProvider = setupRestAccess(ImageProvider.class);
    }

    @After
    public void cleanImages() {
        cleanTable(ImageLibraryEntity.class);
    }

    @Test
    public void crudAdmin() {
        login("admin@admin.com", "1234");

        Assert.assertTrue(imageProvider.getImageGalleryItems().isEmpty());

        // Create image
        imageProvider.uploadImage(IMG_1_DATA_URL);
        List<ImageGalleryItem> imageGalleryItems = imageProvider.getImageGalleryItems();
        Assert.assertEquals(1, imageGalleryItems.size());
        assertThat(imageGalleryItems, hasItem(allOf(
                hasProperty("size", equalTo(43)),
                hasProperty("type", equalTo("image/jpeg"))
        )));
        int imageId = imageGalleryItems.get(0).getId();
        ImageGalleryItem imageGalleryItem = imageProvider.getImageGalleryItem(imageId);
        assertThat(imageGalleryItem, allOf(
                hasProperty("size", equalTo(43)),
                hasProperty("type", equalTo("image/jpeg"))
        ));
        Response response = imageProvider.getImage(imageId);
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        assertThat(response.getLength(), equalTo(43));
        // TODO does not work assertThat(response.getMediaType(), equalTo(MediaType.valueOf("image/jpeg")));
        assertThat(response.readEntity(byte[].class), equalTo(IMG_1_BYTES));
        // Update image
        imageProvider.save(imageId, IMG_2_DATA_URL);
        imageGalleryItems = imageProvider.getImageGalleryItems();
        Assert.assertEquals(1, imageGalleryItems.size());
        assertThat(imageGalleryItems, hasItem(allOf(
                hasProperty("id", equalTo(imageId)),
                hasProperty("size", equalTo(56)),
                hasProperty("type", equalTo("image/gif"))
        )));
        imageGalleryItem = imageProvider.getImageGalleryItem(imageId);
        assertThat(imageGalleryItem, allOf(
                hasProperty("size", equalTo(56)),
                hasProperty("type", equalTo("image/gif"))
        ));
        response = imageProvider.getImage(imageId);
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        assertThat(response.getLength(), equalTo(56));
        // TODO does not assertThat(response.getMediaType(), equalTo(MediaType.valueOf("image/gif")));
        assertThat(response.readEntity(byte[].class), equalTo(IMG_2_BYTES));
        // Delete image
        // TODO delete image missing
    }

    @Test(expected = NotAuthorizedException.class)
    public void uploadImageReg() {
        login("user@user.com", "1234");
        imageProvider.uploadImage(IMG_1_DATA_URL);
    }

    @Test(expected = NotAuthorizedException.class)
    public void saveReg() {
        login("user@user.com", "1234");
        imageProvider.save(1, IMG_1_DATA_URL);
    }

    @Test(expected = NotAuthorizedException.class)
    public void uploadImageUnreg() {
        imageProvider.uploadImage(IMG_1_DATA_URL);
    }

    @Test(expected = NotAuthorizedException.class)
    public void saveUnreg() {
        imageProvider.save(1, IMG_1_DATA_URL);
    }
}
