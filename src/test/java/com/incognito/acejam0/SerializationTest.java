package com.incognito.acejam0;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.incognito.acejam0.domain.Action;
import com.incognito.acejam0.domain.ActionInfo;
import com.incognito.acejam0.domain.Level;
import com.incognito.acejam0.domain.Tile;
import com.incognito.acejam0.utils.Builder;
import com.incognito.acejam0.utils.Mapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
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
                Arrays.asList(Tile.START, Tile.FLOOR, Tile.WALL, Tile.EXIT, Tile.EMPTY, Tile.BUTTON),
                null,
                new Builder<>(new BitSet(6)).with(BitSet::set, 3).with(BitSet::set, 12).build(),
                new Builder<>(new LinkedHashMap<Integer, List<Action>>())
                        .with(Map::put, 0, Collections.singletonList(new Action(Collections.singletonList(new ActionInfo(0, 1, false, -1, null, 0)))))
                        .with(Map::put, 1, Collections.singletonList(new Action(Arrays.asList(
                                new ActionInfo(0, 1, true, 0, Tile.WALL, 1),
                                new ActionInfo(1, 0, false, 1, Tile.START, 2)))))
                        .build()
                ,
                Collections.singletonMap(0, new Action(Collections.singletonList(new ActionInfo(5, 4, true, 1, Tile.WALL, -1)))));
        String s = Mapper.getMapper().writeValueAsString(level);
        assertEquals("{" +
                "\"message\":\"TITLE\"," +
                "\"width\":2," +
                "\"height\":3," +               // todo: remove width/height, calculate from max map/map2 size
                "\"map\":[3,2,1,4,0,5]," +
                "\"map2\":[0,0,0,0,0,0]," +     // missing tiles filled with empty
                "\"state\":\"CBA=\"," +
                "\"actions\":{" +
                    "\"0\":[{" +
                        "\"actions\":[{" +
                            "\"x\":0," +
                            "\"y\":1," +
                            "\"relative\":false," +
                            "\"stateChange\":-1," +
                            "\"tileChange\":null," +
                            "\"tileChangeSide\":0" +
                        "}]" +
                    "}]," +
                    "\"1\":[{" +
                        "\"actions\":[{" +
                            "\"x\":0," +
                            "\"y\":1," +
                            "\"relative\":true," +
                            "\"stateChange\":0," +
                            "\"tileChange\":1," +
                            "\"tileChangeSide\":1" +
                        "},{" +
                            "\"x\":1," +
                            "\"y\":0," +
                            "\"relative\":false," +
                            "\"stateChange\":1," +
                            "\"tileChange\":3," +
                            "\"tileChangeSide\":2" +
                        "}]" +
                    "}]," +
                    "\"2\":[]," +                       //missing directions filled in automatically
                    "\"3\":[]" +
                "}," +
                "\"switchActions\":{" +
                    "\"0\":{" +                         // no validation that index is in map, or that it's a button
                        "\"actions\":[{" +
                            "\"x\":5," +
                            "\"y\":4," +
                            "\"relative\":true," +
                            "\"stateChange\":1," +
                            "\"tileChange\":1," +
                            "\"tileChangeSide\":-1" +
                        "}]" +
                    "}" +
                "}" +
            "}", s);

        Level deserialized = Mapper.getMapper().readValue(s, Level.class);
        assertEquals(level.getWidth(), deserialized.getWidth());
        assertEquals(level.getHeight(), deserialized.getHeight());
        assertEquals(level.getMap(), deserialized.getMap());
        assertEquals(level.getMap2(), deserialized.getMap2());
        assertEquals(level.getNumStarts(), deserialized.getNumStarts());
        assertEquals(level.getNumExits(), deserialized.getNumExits());
        assertEquals(level.getState(), deserialized.getState());
        assertEquals(level.getMessage(), deserialized.getMessage());
        assertEquals(level.getActions().size(), deserialized.getActions().size());
        //UUIDs needed to make duplicate actions distinct in editor panel, but breaks equality here
        for (int i = 0; i < level.getActions().size(); i++) {
            List<Action> a = level.getActions().get(i);
            List<Action> d = deserialized.getActions().get(i);
            assertEquals(a.size(), d.size());
            for (int j = 0; j < a.size(); j++) {
                List<ActionInfo> ai = a.get(j).getActions();
                List<ActionInfo> di = d.get(j).getActions();
                assertEquals(ai.size(), di.size());
                for (int k = 0; k < ai.size(); k++) {
                    ActionInfo ai1 = ai.get(k);
                    ActionInfo di1 = di.get(k);
                    assertEquals(ai1.getX(), di1.getX());
                    assertEquals(ai1.getY(), di1.getY());
                    assertEquals(ai1.getStateChange(), di1.getStateChange());
                    assertEquals(ai1.getTileChange(), di1.getTileChange());
                    assertEquals(ai1.getTileChangeSide(), di1.getTileChangeSide());
                }
            }
        }
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
