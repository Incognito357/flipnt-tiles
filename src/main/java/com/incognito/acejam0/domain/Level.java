package com.incognito.acejam0.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private final BitSet state;
    private final Map<Integer, List<Action>> actions;

    @JsonCreator
    public Level(
            @JsonProperty("title") String title,
            @JsonProperty("width") int width,
            @JsonProperty("height") int height,
            @JsonProperty("map") List<Tile> map,
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
            logger.warn("Map data for level {} missing {} tiles (expected {})", title, map.size() - size, size);
            this.map = new ArrayList<>(map);
            while (this.map.size() < size) {
                this.map.add(Tile.EMPTY);
            }
        } else {
            this.map = map;
        }

        this.state = Objects.requireNonNullElseGet(state, () -> new BitSet(size));
    }

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

    public BitSet getState() {
        return state;
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
                Objects.equals(state, level.state) &&
                Objects.equals(actions, level.actions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, width, height, map, state, actions);
    }
}
