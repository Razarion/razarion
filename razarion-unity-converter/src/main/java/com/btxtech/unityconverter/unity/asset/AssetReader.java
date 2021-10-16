package com.btxtech.unityconverter.unity.asset;

import com.btxtech.unityconverter.unity.asset.type.AssetTypeFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.btxtech.unityconverter.unity.asset.type.AssetTypeFactory.META;


public class AssetReader {
    private static final Logger LOGGER = Logger.getLogger(AssetReader.class.getName());

    public static Asset read(String dirLocation) throws Exception {
        Asset asset = new Asset();
        processAssetFolder(new File(dirLocation), asset);
        return asset;
    }

    private static void processAssetFolder(File assetFolder, Asset asset) throws IOException {
        if (!assetFolder.isDirectory()) {
            throw new IllegalArgumentException("Is not a folder: " + assetFolder);
        }
        List<File> metaFiles = Files.list(Paths.get(assetFolder.getPath()))
                .map(Path::toFile)
                .filter(file -> findExtension(file.getName()).orElse("").endsWith(META))
                .collect(Collectors.toList());

        metaFiles.forEach(metaFile -> setupAssetType(metaFile, asset));
    }

    private static void setupAssetType(File metaFile, Asset asset) {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.findAndRegisterModules();
            Meta meta = mapper.readValue(metaFile, Meta.class);
            meta.setAssetFile(readAssetFile(metaFile));
            meta.setFileExtension(findExtension(meta.getAssetFile().getName()).orElse(null));
            if (meta.getFolderAsset() != null && meta.getFolderAsset()) {
                processAssetFolder(meta.getAssetFile(), asset);
            } else {
                asset.addAssetType(AssetTypeFactory.create(meta));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing meta: " + metaFile, e);
        }
    }

    private static Optional<String> findExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        if (lastIndex == -1) {
            return Optional.empty();
        }
        return Optional.of(fileName.substring(lastIndex + 1));
    }


    private static File readAssetFile(File metaFile) {
        int lastIndex = metaFile.getPath().lastIndexOf("." + META);
        if (lastIndex == -1) {
            throw new IllegalArgumentException("Invalid meta file: " + metaFile);
        }
        return new File(metaFile.getPath().substring(0, lastIndex));
    }


}
