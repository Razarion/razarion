package com.btxtech.unityconverter.unity.asset.type;

import com.btxtech.unityconverter.unity.asset.TestHelper;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.nullValue;

class UnityYamlScannerTest {
    @Test
    public void test() {
        assertThat(UnityYamlScanner.readAllYamlDocuments(new File(TestHelper.BASE_DIR + "prefab\\prefab.yaml")),
                contains(
                        allOf(
                                hasProperty("tag", equalTo("+++ROOT+++")),
                                hasProperty("objectId", equalTo("++ROOT+++")),
                                hasProperty("mayBeStripped", nullValue())
                        ),
                        allOf(
                                hasProperty("tag", equalTo("!u!1")),
                                hasProperty("objectId", equalTo("5678528082777868109")),
                                hasProperty("mayBeStripped", nullValue())
                        ),
                        allOf(
                                hasProperty("tag", equalTo("!u!4")),
                                hasProperty("objectId", equalTo("5678528082777578285")),
                                hasProperty("mayBeStripped", nullValue())
                        ),
                        allOf(
                                hasProperty("tag", equalTo("!u!33")),
                                hasProperty("objectId", equalTo("5678528082781002573")),
                                hasProperty("mayBeStripped", equalTo("stripped"))
                        ),
                        allOf(
                                hasProperty("tag", equalTo("!u!23")),
                                hasProperty("objectId", equalTo("5678528082779871501")),
                                hasProperty("mayBeStripped", nullValue())
                        )
                )
        );
    }
}