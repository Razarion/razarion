package com.btxtech.client.errai.jackson;

import org.jboss.errai.enterprise.client.jaxrs.JacksonTransformer;
import org.junit.Test;

/**
 * Created by Beat
 * on 08.08.2017.
 */
public class TestJacksonTransformer {
    private static final String ERRAI_JSON = "\"{\"^EncodedType\":\"com.btxtech.shared.gameengine.datatypes.config.QuestConfig\",\"^ObjectID\":\"1\",\"conditionConfig\":{\"^EncodedType\":\"com.btxtech.shared.gameengine.datatypes.config.ConditionConfig\",\"^ObjectID\":\"2\",\"conditionTrigger\":null,\"comparisonConfig\":{\"^EncodedType\":\"com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig\",\"^ObjectID\":\"3\",\"count\":null,\"typeCount\":{\"^EncodedType\":\"java.util.HashMap\",\"^ObjectID\":\"4\",\"^Value\":{\"^${$JSON$}$::{\\\"^EncodedType\\\":\\\"java.lang.Integer\\\", \\\"^ObjectID\\\": \\\"-1\\\",\\\"^NumVal\\\":2}\":{\"^EncodedType\":\"java.lang.Integer\", \"^ObjectID\": \"-1\",\"^NumVal\":2},\"^${$JSON$}$::{\\\"^EncodedType\\\":\\\"java.lang.Integer\\\", \\\"^ObjectID\\\": \\\"-1\\\",\\\"^NumVal\\\":4}\":{\"^EncodedType\":\"java.lang.Integer\", \"^ObjectID\": \"-1\",\"^NumVal\":4}}},\"time\":null,\"addExisting\":null,\"placeConfig\":null}},\"id\":14,\"internalName\":\"xxx\",\"title\":\"ssssss\",\"description\":\"wwwwwww\",\"xp\":0,\"money\":0,\"cristal\":0,\"passedMessage\":null,\"hidePassedDialog\":false}\"";

    @Test
    public void toJacksonTest() {
        String jackson = JacksonTransformer.toJackson(ERRAI_JSON);
        System.out.println(jackson);
    }
}
