package com.btxtech.server.util;

import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceInfo;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
import com.btxtech.shared.dto.editor.CustomEditor;
import com.btxtech.shared.dto.editor.CustomEditorInfo;
import com.btxtech.shared.dto.editor.CustomEditorType;
import com.btxtech.shared.dto.editor.GenericPropertyInfo;
import org.junit.Assert;
import org.junit.Test;

import java.util.NoSuchElementException;

public class GenericPropertyEditorGeneratorTest {
    @Test
    public void generate() {
        GenericPropertyInfo genericPropertyInfo = GenericPropertyEditorGenerator.generate();
        // Verify CollectionReference
        CollectionReferenceInfo collectionReferenceInfo = genericPropertyInfo.getCollectionReferenceInfos().stream()
                .filter(cri -> cri.getJavaParentPropertyClass().equals(CollectionReferenceTestClass.class.getName()))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
        Assert.assertEquals(CollectionReferenceTestClass.class.getName(), collectionReferenceInfo.getJavaParentPropertyClass());
        Assert.assertEquals("imageId", collectionReferenceInfo.getJavaPropertyName());
        Assert.assertEquals(CollectionReferenceType.IMAGE, collectionReferenceInfo.getType());
        // Verify CustomEditor
        CustomEditorInfo customEditorInfo = genericPropertyInfo.getCustomEditorInfos().stream()
                .filter(cei -> cei.getJavaParentPropertyClass().equals(CustomEditorTestClass.class.getName()))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
        Assert.assertEquals(CustomEditorTestClass.class.getName(), customEditorInfo.getJavaParentPropertyClass());
        Assert.assertEquals("colladaString", customEditorInfo.getJavaPropertyName());
        Assert.assertEquals(CustomEditorType.COLLADA, customEditorInfo.getType());
    }

    public static class CollectionReferenceTestClass {
        @CollectionReference(CollectionReferenceType.IMAGE)
        @SuppressWarnings("unused")
        private int imageId;
    }

    public static class CustomEditorTestClass {
        @CustomEditor(CustomEditorType.COLLADA)
        @SuppressWarnings("unused")
        private int colladaString;
    }

}