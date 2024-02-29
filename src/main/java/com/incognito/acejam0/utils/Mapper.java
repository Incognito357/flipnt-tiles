package com.incognito.acejam0.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.incognito.acejam0.utils.serializers.BitSetDeserializer;
import com.incognito.acejam0.utils.serializers.BitSetSerializer;

import java.util.BitSet;

public class Mapper {
    private Mapper() {}

    private static ObjectMapper objectMapper;

    public static ObjectMapper getMapper() {
        synchronized(Mapper.class) {
            if (objectMapper == null) {
                objectMapper = new ObjectMapper();
                objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
                objectMapper.registerModule(new Builder<>(new SimpleModule())
                        .with(SimpleModule::addSerializer, new BitSetSerializer())
                        .with(SimpleModule::addDeserializer, BitSet.class, new BitSetDeserializer())
                        .build());
            }
            return objectMapper;
        }
    }
}
