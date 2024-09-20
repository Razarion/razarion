package com.btxtech.server.systemtests.framework;

import com.btxtech.shared.dto.Config;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.function.Supplier;

@Provider
public class ObjectMapperResolver implements ContextResolver<ObjectMapper> {
    private final ObjectMapper mapper;

    public ObjectMapperResolver(Supplier<Class<? extends Config>> implClassSupplier) {
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Config.class, new StdDeserializer<Config>(Config.class) {
            @Override
            public Config deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                ObjectMapper mapper = (ObjectMapper) p.getCodec();
                ObjectNode obj = mapper.readTree(p);
                return mapper.treeToValue(obj, implClassSupplier.get());
            }
        });
        mapper.registerModule(module);
    }

    @Override
    public ObjectMapper getContext(Class<?> cls) {
        return mapper;
    }
}
