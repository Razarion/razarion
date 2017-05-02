package com.btxtech.server.web.marketing;

import com.btxtech.server.marketing.DetailedAdInterest;
import com.btxtech.server.marketing.Interest;
import com.btxtech.server.marketing.MarketingService;
import com.btxtech.server.marketing.facebook.FbAdImage;
import com.btxtech.shared.system.ExceptionHandler;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 27.03.2017.
 */
@Named
@SessionScoped
public class CreateCampaignBean implements Serializable {
    @Inject
    private MarketingService marketingService;
    @Inject
    transient private ExceptionHandler exceptionHandler;
    private String title;
    private String body;
    private List<FbAdImage> fbAdImages;
    private List<DetailedAdInterest> selectedAdInterest = new ArrayList<>();
    private List<DetailedAdInterest> availableAdInterest = new ArrayList<>();
    private String interestQuery;
    private String campaignCreationError;
    private String imageGalleryError;
    private FbAdImage selectedImage;
    private Part uploadImageFile;

    @PostConstruct
    public void postConstruct() {
        try {
            fbAdImages = marketingService.queryFbAdImages();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<DetailedAdInterest> getSelectedAdInterest() {
        return selectedAdInterest;
    }

    public void setSelectedAdInterest(List<DetailedAdInterest> selectedAdInterest) {
        this.selectedAdInterest = selectedAdInterest;
    }

    public String getInterestQuery() {
        return interestQuery;
    }

    public void setInterestQuery(String interestQuery) {
        this.interestQuery = interestQuery;
    }

    public List<DetailedAdInterest> getAvailableAdInterest() {
        return availableAdInterest;
    }

    public Object queryInterest() {
        try {
            availableAdInterest = marketingService.queryAdInterest(interestQuery);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
        return null;
    }

    public void addInterest(DetailedAdInterest adInterest) {
        if (!selectedAdInterest.contains(adInterest)) {
            selectedAdInterest.add(adInterest);
        }
    }

    public void removeInterest(DetailedAdInterest adInterest) {
        selectedAdInterest.remove(adInterest);
    }

    public Object deepQueryInterest() {
        try {
            List<DetailedAdInterest> deepInterests = new ArrayList<>(availableAdInterest);
            for (DetailedAdInterest adInterest : availableAdInterest) {
                for (DetailedAdInterest interest : marketingService.queryAdInterest(adInterest.getAdInterest().getName())) {
                    if (!deepInterests.contains(interest)) {
                        deepInterests.add(interest);
                    }
                }
            }
            availableAdInterest = deepInterests;
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
        return null;
    }


    public Object createCampaign() {
        if (title == null || title.isEmpty()) {
            campaignCreationError = "Title not defined";
            return null;
        }
        if (body == null || body.isEmpty()) {
            campaignCreationError = "Body not defined";
            return null;
        }
        if (selectedImage == null) {
            campaignCreationError = "No Image selected";
            return null;
        }
        if (selectedAdInterest.isEmpty()) {
            campaignCreationError = "No interests";
            return null;
        }

        try {
            List<Interest> interests = new ArrayList<>();
            for (DetailedAdInterest selected : selectedAdInterest) {
                Interest interest = new Interest();
                interest.setName(selected.getAdInterest().getName());
                interest.setFbId(selected.getAdInterest().getId());
                interests.add(interest);
            }
            marketingService.startCampaign(title, body, selectedImage, interests);
            return "marketing";
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            campaignCreationError = t.getMessage();
            return null;
        }
    }

    public String getCampaignCreationError() {
        return campaignCreationError;
    }

    public List<FbAdImage> getFbAdImages() {
        return fbAdImages;
    }

    public void selectImage(FbAdImage image) {
        selectedImage = image;
    }

    public void deleteImage(FbAdImage image) {
        try {
            marketingService.deleteFbAdImage(image);
            fbAdImages = marketingService.queryFbAdImages();
            if (selectedImage == image) {
                selectedImage = null;
            }
            imageGalleryError = null;
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            imageGalleryError = e.getMessage();
        }
    }

    public FbAdImage getSelectedImage() {
        return selectedImage;
    }

    public void setUploadImageFile(Part uploadImageFile) {
        this.uploadImageFile = uploadImageFile;
    }

    public Part getUploadImageFile() {
        return null;
    }

    public Object uploadImage() {
        if (uploadImageFile == null) {
            uploadImageFile = null;
            imageGalleryError = "No file spe";
            return null;
        }
        try {
            byte[] bytes = new byte[uploadImageFile.getInputStream().available()];
            int bytesRead = uploadImageFile.getInputStream().read(bytes);
            if (bytesRead < 0) {
                throw new IllegalStateException("bytesRead < 0");
            }
            marketingService.uploadImageFile(bytes);
            fbAdImages = marketingService.queryFbAdImages();
            uploadImageFile = null;
            imageGalleryError = null;
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            imageGalleryError = e.getMessage();
        }
        return null;
    }

    public String getImageGalleryError() {
        return imageGalleryError;
    }
}
