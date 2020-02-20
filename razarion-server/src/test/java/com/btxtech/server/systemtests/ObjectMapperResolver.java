package com.btxtech.server.systemtests;

import com.btxtech.shared.dto.ObjectNameIdProvider;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class ObjectMapperResolver implements ContextResolver<ObjectMapper> {
    private final ObjectMapper mapper;

    public ObjectMapperResolver(Class<? extends ObjectNameIdProvider> implClass) {
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ObjectNameIdProvider.class, new StdDeserializer<ObjectNameIdProvider>(ObjectNameIdProvider.class) {
            @Override
            public ObjectNameIdProvider deserialize(JsonParser p, DeserializationContext ctxt)
                    throws IOException {
                ObjectMapper mapper = (ObjectMapper) p.getCodec();
                ObjectNode obj = mapper.readTree(p);
                return mapper.treeToValue(obj, implClass);
            }
        });
        mapper.registerModule(module);
    }

    @Override
    public ObjectMapper getContext(Class<?> cls) {
        return mapper;
    }
}
