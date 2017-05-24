package com.btxtech.server.web.marketing;

import com.btxtech.server.marketing.DetailedAdInterest;
import com.btxtech.server.marketing.Interest;
import com.btxtech.server.marketing.MarketingService;
import com.btxtech.server.marketing.facebook.CreationInput;
import com.btxtech.server.marketing.facebook.FbAdImage;
import com.btxtech.server.util.ServerUtil;
import com.btxtech.shared.system.ExceptionHandler;
import org.apache.commons.lang3.StringUtils;

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
    private CreationInput creationInput = new CreationInput();
    private List<FbAdImage> fbAdImages;
    private List<DetailedAdInterest> selectedAdInterest = new ArrayList<>();
    private List<DetailedAdInterest> availableAdInterest = new ArrayList<>();
    private String interestQuery;
    private String campaignCreationError;
    private String imageGalleryError;
    private Part uploadImageFile;

    @PostConstruct
    public void postConstruct() {
        try {
            fbAdImages = marketingService.queryFbAdImages();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public CreationInput getCreationInput() {
        return creationInput;
    }

    public String getTitle() {
        return creationInput.getTitle();
    }

    public void setTitle(String title) {
        creationInput.setTitle(title);
    }

    public String getBody() {
        return creationInput.getBody();
    }

    public void setBody(String body) {
        creationInput.setBody(body);
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
        if (creationInput.isLifeTime()) {
            if (creationInput.getScheduleStartTime() == null) {
                campaignCreationError = "If budget type is lifetime, Schedule Start Time must be set";
                return null;
            }
            if (creationInput.getScheduleEndTime() == null) {
                campaignCreationError = "If budget type is lifetime, Schedule End Time must be set";
                return null;
            }
            if (creationInput.getLifeTimeBudget() == null) {
                campaignCreationError = "If budget type is lifetime, Lifetime budget must be set";
                return null;
            }
        } else {
            if (creationInput.getDailyBudget() == null) {
                campaignCreationError = "If budget type is not lifetime, Daily budget must be set";
                return null;
            }
        }
        if (StringUtils.isEmpty(creationInput.getTitle())) {
            campaignCreationError = "Title not defined";
            return null;
        }
        if (StringUtils.isEmpty(creationInput.getBody())) {
            campaignCreationError = "Body not defined";
            return null;
        }
        if (creationInput.getFbAdImage() == null) {
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
                interest.setAudienceSize(selected.getAdInterest().getAudienceSize());
                interests.add(interest);
            }
            creationInput.setInterests(interests);
            creationInput.setUrlTagParam(ServerUtil.generateSimpleUuid());
            marketingService.startCampaign(creationInput);
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
        creationInput.setFbAdImage(image);
    }

    public void deleteImage(FbAdImage image) {
        try {
            marketingService.deleteFbAdImage(image);
            fbAdImages = marketingService.queryFbAdImages();
            if (creationInput.getFbAdImage() == image) {
                creationInput.setFbAdImage(null);
            }
            imageGalleryError = null;
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            imageGalleryError = e.getMessage();
        }
    }

    public FbAdImage getSelectedImage() {
        return creationInput.getFbAdImage();
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
