package com.btxtech.shared.mocks;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class TestFloat32ArraySerializer extends StdSerializer<TestFloat32Array> {

    public TestFloat32ArraySerializer() {
        super(TestFloat32Array.class);
    }

    @Override
    public void serialize(TestFloat32Array value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        String arrayString = "";
        if (value.getDoubles() != null) {
            arrayString = Arrays.stream(value.getDoubles()).mapToObj(Double::toString).collect(Collectors.joining(","));
        }

        jgen.writeRawValue("[" + arrayString + "]");
    }
}
