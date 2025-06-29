package com.btxtech.server.service.ui;

import com.btxtech.server.model.ui.Image;
import com.btxtech.server.model.ui.ImageGalleryItem;
import com.btxtech.server.model.ui.ImageLibraryEntity;
import com.btxtech.server.repository.ui.ImageRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImagePersistence {
    private final ImageRepository imageRepository;

    public ImagePersistence(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Transactional
    public List<ImageGalleryItem> getImageGalleryItems() {
        return imageRepository.findAll().stream()
                .map(ImageLibraryEntity::toImageGalleryItem)
                .toList();
    }

    @Transactional
    public void createImage(byte[] imageData, String type) {
        ImageLibraryEntity imageLibraryEntity = new ImageLibraryEntity();
        imageLibraryEntity.setType(type);
        imageLibraryEntity.setData(imageData);
        imageLibraryEntity.setSize(imageData.length);
        imageRepository.save(imageLibraryEntity);
    }

    @Transactional
    public void save(int id, byte[] imageData, String type) {
        ImageLibraryEntity imageLibraryEntity = imageRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Image not found with id " + id));
        imageLibraryEntity.setType(type);
        imageLibraryEntity.setData(imageData);
        imageLibraryEntity.setSize(imageData.length);
        imageRepository.save(imageLibraryEntity);
    }

    @Transactional
    public void delete(int id) {
        ImageLibraryEntity imageLibraryEntity = imageRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Image not found with id " + id));
        imageRepository.delete(imageLibraryEntity);
    }

    @Transactional
    public Image getImage(int id) {
        return imageRepository.findImageRaw(id).stream()
                .map(result -> new Image((byte[]) result[0], (String) result[1]))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Image not found with id " + id));
    }

    @Transactional
    public ImageLibraryEntity getImageLibraryEntity(Integer id) {
        if (id == null) {
            return null;
        }
        return imageRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Image not found with id " + id));
    }

}