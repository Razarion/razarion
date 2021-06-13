package com.btxtech.server.util;

import com.btxtech.shared.datatypes.CollectionReference;
import com.btxtech.shared.datatypes.CollectionReferenceType;
import com.btxtech.shared.datatypes.shape.config.Shape3DConfig;
import com.btxtech.shared.datatypes.shape.config.Shape3DElementConfig;
import com.btxtech.shared.dto.editor.GenericPropertyInfo;
import com.btxtech.shared.dto.editor.CollectionReferenceInfo;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.NoSuchElementException;

public class GenericPropertyEditorGeneratorTest {
    public static class TestClass {
        @CollectionReference(CollectionReferenceType.IMAGE)
        @SuppressWarnings("unused")
        private int imageId;
    }

    @Test
    public void generate() {
        GenericPropertyInfo genericPropertyInfo = GenericPropertyEditorGenerator.generate();
        // Verify listElementTypes
        Map<String, Map<String, String>> listElementTypes = genericPropertyInfo.getListElementTypes();
        Assert.assertEquals(Shape3DElementConfig.class.getName(), listElementTypes.get(Shape3DConfig.class.getName()).get("shape3DElementConfigs"));
        // Verify CollectionReference
        CollectionReferenceInfo collectionReferenceInfo = genericPropertyInfo.getCollectionReferenceInfos().stream()
                .filter(op3s -> op3s.getJavaParentPropertyClass().equals(TestClass.class.getName()))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
        Assert.assertEquals(TestClass.class.getName(), collectionReferenceInfo.getJavaParentPropertyClass());
        Assert.assertEquals("imageId", collectionReferenceInfo.getJavaPropertyName());
        Assert.assertEquals(CollectionReferenceType.IMAGE, collectionReferenceInfo.getType());
    }

}