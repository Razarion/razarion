package com.btxtech.unityconverter.unity.asset.type;

import com.btxtech.unityconverter.unity.asset.meta.Meta;
import com.btxtech.unityconverter.unity.model.IgnoredAssetType;
import com.btxtech.unityconverter.unity.model.UnityObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AssetType {
    private final Meta meta;

    public AssetType(Meta meta) {
        this.meta = meta;
    }

    public Meta getMeta() {
        return meta;
    }

    public String getGuid() {
        return meta.getGuid();
    }

    public File getAssetFile() {
        return meta.getAssetFile();
    }

    public static class IgnoredAssetTypeHolder implements Holder<IgnoredAssetType> {
        public IgnoredAssetType ignoredAssetType;

        @Override
        public IgnoredAssetType getObject() {
            return ignoredAssetType;
        }

        @SuppressWarnings("unused")
        public void setIgnoredAssetType(IgnoredAssetType ignoredAssetType) {
            this.ignoredAssetType = ignoredAssetType;
        }

        @Override
        public String toString() {
            return "IgnoredAssetType{" +
                    "ignoredAssetType=" + ignoredAssetType +
                    '}';
        }
    }

    public interface Holder<T extends UnityObject> {
        T getObject();
    }

    protected InputStream removeUnityCrap(File assetFile) {
        try (Stream<String> stream = Files.lines(Paths.get(assetFile.toURI()))) {
            return new ByteArrayInputStream(stream.map(s -> {
                if (s.startsWith("---") && s.endsWith(" stripped")) {
                    return s.substring(0, s.length() - " stripped".length());
                } else {
                    return s;
                }
            }).collect(Collectors.joining("\n")).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Error processing: " + assetFile, e);
        }
    }

    protected String readObjectId(String snippet) {
        StringBuilder objectId = new StringBuilder();
        int objectIdStart = snippet.indexOf('&') + 1;
        for (int i = objectIdStart; i < snippet.length(); i++) {
            if (Character.isWhitespace(snippet.charAt(i))) {
                break;
            } else {
                objectId.append(snippet.charAt(i));
            }
        }
        return objectId.toString();
    }

}
