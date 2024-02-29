package com.incognito.acejam0.utils.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.BitSet;

public class BitSetDeserializer extends StdDeserializer<BitSet> {

    public BitSetDeserializer() {
        super(BitSet.class);
    }

    @Override
    public BitSet deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return BitSet.valueOf(jsonParser.getBinaryValue());
    }
}
