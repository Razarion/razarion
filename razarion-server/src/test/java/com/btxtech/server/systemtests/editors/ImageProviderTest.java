package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.systemtests.framework.AbstractSystemTest;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.ImageGalleryItem;
import com.btxtech.shared.rest.ImageProvider;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;

public class ImageProviderTest extends AbstractSystemTest {
    private ImageProvider imageProvider;
    private byte[] img1Array;
    private byte[] img2Array;

    @Before
    public void setup() throws IOException {
        imageProvider = setupRestAccess(ImageProvider.class);
        img1Array = IOUtils.toByteArray(Objects.requireNonNull(getClass().getResourceAsStream("Img1.png")));
        img2Array = IOUtils.toByteArray(Objects.requireNonNull(getClass().getResourceAsStream("Img2.jpg")));
    }

    @After
    public void cleanImages() {
        cleanTable(ImageLibraryEntity.class);
    }

    @Test
    public void crudAdmin() {
        getDefaultRestConnection().loginAdmin();

        Assert.assertTrue(imageProvider.getImageGalleryItems().isEmpty());

        // Upload image
        Response response = getDefaultRestConnection()
                .getTarget()
                .path(CommonUrl.IMAGE_SERVICE_PATH)
                .path("upload")
                .request()
                .post(Entity.entity(setupFormData("Img1.png"), MediaType.MULTIPART_FORM_DATA));
        Assert.assertEquals(204, response.getStatus());

        List<ImageGalleryItem> imageGalleryItems = imageProvider.getImageGalleryItems();
        Assert.assertEquals(1, imageGalleryItems.size());
        assertThat(imageGalleryItems, hasItem(allOf(
                hasProperty("size", equalTo(687)),
                hasProperty("type", equalTo("image/png"))
        )));
        int imageId = imageGalleryItems.get(0).getId();
        ImageGalleryItem imageGalleryItem = imageProvider.getImageGalleryItem(imageId);
        assertThat(imageGalleryItem, allOf(
                hasProperty("size", equalTo(687)),
                hasProperty("type", equalTo("image/png"))
        ));
        response = imageProvider.getImage(imageId);
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        assertThat(response.getLength(), equalTo(687));
        assertThat(response.getMediaType(), equalTo(MediaType.valueOf("image/png")));
        assertThat(response.readEntity(byte[].class), equalTo(img1Array));
        // Update image
        response = getDefaultRestConnection()
                .getTarget()
                .path(CommonUrl.IMAGE_SERVICE_PATH)
                .path("update/" + imageId)
                .request()
                .put(Entity.entity(setupFormData("Img2.jpg"), MediaType.MULTIPART_FORM_DATA));
        Assert.assertEquals(204, response.getStatus());

        imageGalleryItems = imageProvider.getImageGalleryItems();
        Assert.assertEquals(1, imageGalleryItems.size());
        assertThat(imageGalleryItems, hasItem(allOf(
                hasProperty("id", equalTo(imageId)),
                hasProperty("size", equalTo(1718)),
                hasProperty("type", equalTo("image/jpeg"))
        )));
        imageGalleryItem = imageProvider.getImageGalleryItem(imageId);
        assertThat(imageGalleryItem, allOf(
                hasProperty("size", equalTo(1718)),
                hasProperty("type", equalTo("image/jpeg"))
        ));
        response = imageProvider.getImage(imageId);
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        assertThat(response.getLength(), equalTo(1718));
        assertThat(response.getMediaType(), equalTo(MediaType.valueOf("image/jpeg")));
        assertThat(response.readEntity(byte[].class), equalTo(img2Array));
        // Delete image
        imageProvider.delete(imageId);
    }

    @Test
    public void uploadImageReg() {
        getDefaultRestConnection().loginUser();
        Response response = getDefaultRestConnection()
                .getTarget()
                .path(CommonUrl.IMAGE_SERVICE_PATH)
                .path("upload")
                .request()
                .post(Entity.entity(setupFormData("Img1.png"), MediaType.MULTIPART_FORM_DATA));
        Assert.assertEquals(401, response.getStatus());
    }

    @Test
    public void updateReg() {
        getDefaultRestConnection().loginUser();
        Response response = getDefaultRestConnection()
                .getTarget()
                .path(CommonUrl.IMAGE_SERVICE_PATH)
                .path("update/" + 1)
                .request()
                .put(Entity.entity(setupFormData("Img2.jpg"), MediaType.MULTIPART_FORM_DATA));
        Assert.assertEquals(401, response.getStatus());
    }

    @Test
    public void uploadImageUnreg() {
        getDefaultRestConnection().loginUser();
        Response response = getDefaultRestConnection()
                .getTarget()
                .path(CommonUrl.IMAGE_SERVICE_PATH)
                .path("upload")
                .request()
                .post(Entity.entity(setupFormData("Img1.png"), MediaType.MULTIPART_FORM_DATA));
        Assert.assertEquals(401, response.getStatus());
    }

    @Test
    public void updateUnreg() {
        Response response = getDefaultRestConnection()
                .getTarget()
                .path(CommonUrl.IMAGE_SERVICE_PATH)
                .path("update/" + 1)
                .request()
                .put(Entity.entity(setupFormData("Img2.jpg"), MediaType.MULTIPART_FORM_DATA));
        Assert.assertEquals(401, response.getStatus());
    }

    private MultipartFormDataOutput setupFormData(String image) {
        MultipartFormDataOutput multipartFormDataOutput = new MultipartFormDataOutput();
        multipartFormDataOutput.addFormData("ignore", getClass().getResourceAsStream(image), MediaType.APPLICATION_OCTET_STREAM_TYPE);
        return multipartFormDataOutput;
    }
}
