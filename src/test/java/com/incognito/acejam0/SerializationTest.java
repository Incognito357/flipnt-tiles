package com.incognito.acejam0;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.incognito.acejam0.domain.Action;
import com.incognito.acejam0.domain.ActionInfo;
import com.incognito.acejam0.domain.Level;
import com.incognito.acejam0.domain.Tile;
import com.incognito.acejam0.utils.Builder;
import com.incognito.acejam0.utils.Mapper;
import org.junit.jupiter.api.Test;

import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SerializationTest {

    @Test
    void testBitSetSerialization() throws JsonProcessingException {
        BitSet set = new BitSet(16);
        set.set(3);
        set.set(12);
        String s = Mapper.getMapper().writeValueAsString(set);
        assertNotNull(s);
        assertEquals("\"CBA=\"", s);

        BitSet deserialized = Mapper.getMapper().readValue(s, BitSet.class);

        assertNotNull(deserialized);
        assertEquals(set, deserialized);

        set.set(5);
        assertNotEquals(set, deserialized);
    }

    @Test
    void testLevelSerialization() throws JsonProcessingException {
        Level level = new Level("TITLE", 2, 3,
                List.of(Tile.START, Tile.FLOOR, Tile.WALL, Tile.EXIT, Tile.EMPTY, Tile.WALL),
                null,
                new Builder<>(new BitSet(6)).with(BitSet::set, 3).with(BitSet::set, 12).build(),
                new LinkedHashMap<>(Map.of(
                        0, List.of(new Action(List.of(new ActionInfo(0, 1, false, -1, null)))),
                        1, List.of(new Action(List.of(new ActionInfo(0, 1, true, 0, Tile.WALL), new ActionInfo(1, 0, false, 1, Tile.START))))
                )));
        String s = Mapper.getMapper().writeValueAsString(level);
        assertEquals("{" +
                "\"title\":\"TITLE\"," +
                "\"width\":2," +
                "\"height\":3," +
                "\"map\":[3,2,1,4,0,1]," +
                "\"map2\":[0,0,0,0,0,0]," +
                "\"state\":\"CBA=\"," +
                "\"actions\":{" +
                    "\"0\":[{" +
                        "\"actions\":[{" +
                            "\"x\":0," +
                            "\"y\":1," +
                            "\"relative\":false," +
                            "\"stateChange\":-1," +
                            "\"tileChange\":null" +
                        "}]" +
                    "}]," +
                    "\"1\":[{" +
                        "\"actions\":[{" +
                            "\"x\":0," +
                            "\"y\":1," +
                            "\"relative\":true," +
                            "\"stateChange\":0," +
                            "\"tileChange\":1" +
                        "},{" +
                            "\"x\":1," +
                            "\"y\":0," +
                            "\"relative\":false," +
                            "\"stateChange\":1," +
                            "\"tileChange\":3" +
                        "}]" +
                    "}]" +
                "}" +
            "}", s);

        Level deserialized = Mapper.getMapper().readValue(s, Level.class);
        assertEquals(level, deserialized);
    }

    @Test
    void testBitSetBase64() throws JsonProcessingException {
        BitSet set = new BitSet(9);
        set.set(0, 9, true);
        for (int i = 0; i < 9; i++) {
            set.clear(i);
            if (i > 0) {
                set.set(i - 1);
            }
            System.out.println((i + 1) + ": " + Mapper.getMapper().writeValueAsString(set));
        }
        System.out.println("All: " + Mapper.getMapper().writeValueAsString(set));
    }
}
