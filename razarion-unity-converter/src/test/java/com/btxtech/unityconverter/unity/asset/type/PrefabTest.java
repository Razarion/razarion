package com.btxtech.unityconverter.unity.asset.type;

import com.btxtech.unityconverter.unity.asset.meta.Meta;
import com.btxtech.unityconverter.unity.asset.TestHelper;
import com.btxtech.unityconverter.unity.model.MeshFilter;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.nullValue;

class PrefabTest {

    @Test
    void readGameObjects() {
        Prefab prefab = new Prefab(new Meta().assetFile(new File(TestHelper.BASE_DIR + "prefab\\prefab.yaml")));

        assertThat(prefab.getGameObject(),
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

    @Test
    void getMeshFilters() {
        Prefab prefab = new Prefab(new Meta().assetFile(new File(TestHelper.BASE_DIR + "prefab\\prefab.yaml")));
        prefab.getComponents(MeshFilter.class);
        assertThat(prefab.getComponents(MeshFilter.class),containsInAnyOrder(
                allOf(
                        hasProperty("m_Mesh",  allOf(
                                hasProperty("fileID", equalTo("4300180")),
                                hasProperty("guid", equalTo("e449f791897e9da408437dfc51ec9045")),
                                hasProperty("type", equalTo("3"))
                        ))
                )
        ));

    }
}