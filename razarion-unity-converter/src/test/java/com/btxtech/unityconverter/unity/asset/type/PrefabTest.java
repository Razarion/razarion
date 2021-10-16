package com.btxtech.unityconverter.unity.asset.type;

import com.btxtech.unityconverter.unity.asset.Meta;
import com.btxtech.unityconverter.unity.model.GameObject;
import org.junit.jupiter.api.Assertions;

import java.io.File;

class PrefabTest {

    @org.junit.jupiter.api.Test
    void readGameObjects() {
        Prefab prefab = new Prefab(new Meta().assetFile(new File("C:\\dev\\projects\\razarion\\code\\razarion\\razarion-unity-converter\\src\\test\\resources\\unity\\prefab\\separator.yaml")));
        GameObject gameObject = prefab.readGameObject();
        Assertions.assertEquals("TestNameGameObject", gameObject.getName());
        Assertions.assertEquals(3, gameObject.getComponents().size());
    }
}