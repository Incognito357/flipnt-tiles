package com.incognito.acejam0.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.incognito.acejam0.utils.Mapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;

public class Level {
    private static final int MAX_SIZE = 100;
    private static final Logger logger = LogManager.getLogger();

    private final String title;
    private final int width;
    private final int height;
    private final List<Tile> map;
    private final List<Tile> map2;
    private final BitSet state;
    private final Map<Integer, List<Action>> actions;

    @JsonCreator
    public Level(
            @JsonProperty("title") String title,
            @JsonProperty("width") int width,
            @JsonProperty("height") int height,
            @JsonProperty("map") List<Tile> map,
            @JsonProperty("map2") List<Tile> map2,
            @JsonProperty("state") BitSet state,
            @JsonProperty("actions") Map<Integer, List<Action>> actions) {
        this.actions = actions;

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
    }

    @JsonIgnore
    public Tile getTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return null;
        }
        int i = y * width + x;
        if (i >= map.size()) {
            return null;
        }
        return map.get(i);
    }

    @JsonIgnore
    public boolean isTileEnabled(int x, int y) {
        return !state.get(y * width + x);
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
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("levels/" + name + ".json")) {
            if (in == null) {
                logger.error("Could not load level {}", name);
                return null;
            }
            return Mapper.getMapper().readValue(in, Level.class);
        } catch (IOException e) {
            logger.error("Could not load level {}", name, e);
            return null;
        }
    }
}
