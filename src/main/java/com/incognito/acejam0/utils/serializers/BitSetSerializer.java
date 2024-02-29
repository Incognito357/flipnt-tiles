package com.incognito.acejam0.utils.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.BitSet;

public class BitSetSerializer extends StdSerializer<BitSet> {
    public BitSetSerializer() {
        super(BitSet.class);
    }

    @Override
    public void serialize(BitSet bitSet, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeBinary(bitSet.toByteArray());
    }
}
