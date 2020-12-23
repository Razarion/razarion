package com.btxtech.server.util;

import com.btxtech.shared.datatypes.shape.config.Shape3DConfig;
import com.btxtech.shared.datatypes.shape.config.Shape3DElementConfig;
import com.btxtech.shared.dto.editor.GenericPropertyInfo;
import com.btxtech.shared.dto.editor.OpenApi3Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.NoSuchElementException;

public class GenericPropertyEditorGeneratorTest {
    public static class TestClass {
        @Schema(type="testType")
        private int imageId;
    }

    @Test
    public void generate() {
        GenericPropertyInfo genericPropertyInfo = GenericPropertyEditorGenerator.generate();
        // Verify listElementTypes
        Map<String, Map<String, String>> listElementTypes = genericPropertyInfo.getListElementTypes();
        Assert.assertEquals(Shape3DElementConfig.class.getName(), listElementTypes.get(Shape3DConfig.class.getName()).get("shape3DElementConfigs"));
        // Verify OpenApi3Schema
        OpenApi3Schema openApi3Schema = genericPropertyInfo.getOpenApi3Schemas().stream()
                .filter(op3s -> op3s.getJavaParentPropertyClass().equals(TestClass.class.getName()))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
        Assert.assertEquals(TestClass.class.getName(), openApi3Schema.getJavaParentPropertyClass());
        Assert.assertEquals("imageId", openApi3Schema.getJavaPropertyName());
        Assert.assertEquals("testType", openApi3Schema.getType());
    }

}