package com.incognito.acejam0.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.incognito.acejam0.utils.Mapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Level {
    public static final int MAX_SIZE = 100;
    private static final Logger logger = LogManager.getLogger();

    private final String title;
    private final int width;
    private final int height;
    private final List<Tile> map;
    private final List<Tile> map2;
    private final BitSet state;
    private final Map<Integer, List<Action>> actions;
    private final int numStarts;
    private final int numExits;

    @JsonCreator
    public Level(
            @JsonProperty("title") String title,
            @JsonProperty("width") int width,
            @JsonProperty("height") int height,
            @JsonProperty("map") List<Tile> map,
            @JsonProperty("map2") List<Tile> map2,
            @JsonProperty("state") BitSet state,
            @JsonProperty("actions") Map<Integer, List<Action>> actions) {
        if (title == null || title.isBlank()) {
            title = UUID.randomUUID().toString();
        }

        this.title = title;
        this.width = width;
        this.height = height;

        if (width < 0 || width > MAX_SIZE) {
            throw new IllegalArgumentException("Invalid width: " + width);
        }

        if (height < 0 || height > MAX_SIZE) {
            throw new IllegalArgumentException("Invalid height: " + height);
        }

        int size = width * height;
        if (map == null || map.isEmpty()) {
            logger.warn("Missing map for level {}", title);
            this.map = IntStream.range(0, size)
                    .mapToObj(i -> Tile.EMPTY)
                    .toList();
        } else if (map.size() < size) {
            logger.warn("Map data for level {} missing {} tiles (expected {})", title, size - map.size(), size);
            this.map = new ArrayList<>(map);
            while (this.map.size() < size) {
                this.map.add(Tile.EMPTY);
            }
        } else {
            this.map = map;
        }

        if (map2 == null) {
            this.map2 = IntStream.range(0, size)
                    .mapToObj(i -> Tile.EMPTY)
                    .toList();
        } else if (map2.size() < size) {
            this.map2 = new ArrayList<>(map2);
            logger.warn("Map2 data for level {} missing {} tiles (expected {})", title, size - map2.size(), size);
            while (this.map2.size() < size) {
                this.map2.add(Tile.EMPTY);
            }
        } else {
            this.map2 = map2;
        }

        if (this.map.size() > size) {
            logger.warn("Map data for level {} is {} tiles larger than expected size {} (extra data is ignored)",
                    title, this.map.size() - size, size);
        }
        if (this.map2.size() > size) {
            logger.warn("Map2 data for level {} is {} tiles larger than expected size {} (extra data is ignored)",
                    title, this.map2.size() - size, size);
        }

        this.state = Objects.requireNonNullElseGet(state, () -> new BitSet(size));

        if (actions == null) {
            actions = new HashMap<>();
            for (InputBinding b : InputBinding.values()) {
                actions.put(b.ordinal(), new ArrayList<>());
            }
            this.actions = actions;
        } else {
            this.actions = new HashMap<>(actions);
            for (InputBinding b : InputBinding.values()) {
                this.actions.computeIfAbsent(b.ordinal(), i -> new ArrayList<>());
            }
        }

        Map<Tile, Long> counts = Stream.concat(this.map.stream(), this.map2.stream())
                .filter(t -> t == Tile.START || t == Tile.EXIT)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        this.numStarts = counts.getOrDefault(Tile.START, 0L).intValue();
        this.numExits = counts.getOrDefault(Tile.EXIT, 0L).intValue();
    }

    @JsonIgnore
    public Tile getTile(int x, int y) {
        return getTile(x, y, map);
    }

    @JsonIgnore
    public Tile getTile2(int x, int y) {
        return getTile(x, y, map2);
    }

    @JsonIgnore
    public Tile getActiveTile(int x, int y) {
        if (isTileFlipped(x, y)) {
            return getTile2(x, y);
        }
        return getTile(x, y);
    }

    @JsonIgnore
    public Tile getInactiveTile(int x, int y) {
        if (isTileFlipped(x, y)) {
            return getTile(x, y);
        }
        return getTile2(x, y);
    }

    private Tile getTile(int x, int y, List<Tile> data) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return null;
        }
        int i = y * width + x;
        if (i >= data.size()) {
            return null;
        }
        return data.get(i);
    }

    @JsonIgnore
    public boolean isTileFlipped(int x, int y) {
        return state.get(y * width + x);
    }

    public String getTitle() {
        return title;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<Tile> getMap() {
        return map;
    }

    public List<Tile> getMap2() {
        return map2;
    }

    public BitSet getState() {
        return state;
    }

    public Map<Integer, List<Action>> getActions() {
        return actions;
    }

    @JsonIgnore
    public int getNumStarts() {
        return numStarts;
    }

    @JsonIgnore
    public int getNumExits() {
        return numExits;
    }

    public void performActions(Action change) {
        for (ActionInfo a : change.getActions()) {
            int stateChange = a.getStateChange();
            int i = a.getY() * width + a.getX();

            Tile t = a.getTileChange();
            if (t != null) {
                if (state.get(i)) {
                    map2.set(i, t);
                } else {
                    map.set(i, t);
                }
            }

            if (stateChange == 2) {
                state.flip(i);
            } else if (stateChange == -1) {
                state.set(i);
            } else if (stateChange == 1) {
                state.clear(i);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Level level = (Level) o;
        return width == level.width &&
                height == level.height &&
                Objects.equals(title, level.title) &&
                Objects.equals(map, level.map) &&
                Objects.equals(map2, level.map2) &&
                Objects.equals(state, level.state) &&
                Objects.equals(actions, level.actions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, width, height, map, map2, state, actions);
    }

    public static Level loadLevel(String name) {
        try (InputStream in = new FileInputStream("levels/" + name + ".json")) {
            return Mapper.getMapper().readValue(in, Level.class);
        } catch (IOException e) {
            try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("levels/" + name + ".json")) {
                if (in == null) {
                    logger.error("Could not load level {}", name);
                    return null;
                }
                return Mapper.getMapper().readValue(in, Level.class);
            } catch (IOException e2) {
                logger.error("Could not load level {}", name, e2);
                return null;
            }
        }
    }
}
