package com.btxtech.unityconverter.unity.asset.type;

import com.btxtech.unityconverter.unity.asset.Meta;
import com.btxtech.unityconverter.unity.model.GameObject;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.nullValue;

class PrefabTest {

    @org.junit.jupiter.api.Test
    void readGameObjects() {
        Prefab prefab = new Prefab(new Meta().assetFile(new File("C:\\dev\\projects\\razarion\\code\\razarion\\razarion-unity-converter\\src\\test\\resources\\unity\\prefab\\separator.yaml")));
        GameObject gameObject = prefab.readGameObject();

        assertThat(gameObject,
                allOf(
                        hasProperty("m_Name", equalTo("TestNameGameObject")),
                        hasProperty("m_Component", containsInAnyOrder(
                                allOf(
                                        hasProperty("component",
                                                allOf(
                                                        hasProperty("fileID", equalTo("5678528082777578285")),
                                                        hasProperty("guid", nullValue()),
                                                        hasProperty("type", nullValue())
                                                )
                                        )
                                ),
                                allOf(
                                        hasProperty("component",
                                                allOf(
                                                        hasProperty("fileID", equalTo("5678528082779871501")),
                                                        hasProperty("guid", nullValue()),
                                                        hasProperty("type", nullValue())
                                                )
                                        )
                                ),
                                allOf(
                                        hasProperty("component",
                                                allOf(
                                                        hasProperty("fileID", equalTo("5678528082781002573")),
                                                        hasProperty("guid", nullValue()),
                                                        hasProperty("type", nullValue())
                                                )
                                        )
                                ))
                        )
                ));
    }
}