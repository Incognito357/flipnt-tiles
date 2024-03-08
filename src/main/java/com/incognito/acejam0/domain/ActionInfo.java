package com.incognito.acejam0.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.UUID;

public class ActionInfo {
    private final UUID uuid = UUID.randomUUID();
    private final int x;
    private final int y;
    private final boolean relative;
    private final int stateChange;  //2 to flip, -1 to force flip down, 1 to force flip up, 0 to keep original state
    private final Tile tileChange;  //ignores if null

    @JsonCreator
    public ActionInfo(
            @JsonProperty("x") int x,
            @JsonProperty("y") int y,
            @JsonProperty("relative") boolean relative,
            @JsonProperty("stateChange") int stateChange,
            @JsonProperty("tileChange") Tile tileChange) {
        this.x = x;
        this.y = y;
        this.relative = relative;
        this.stateChange = stateChange;
        this.tileChange = tileChange;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isRelative() {
        return relative;
    }

    /**
     * -1 to disable, 1 to enable, 2 to flip state, 0 to keep original state
     */
    public int getStateChange() {
        return stateChange;
    }

    /**
     * null to keep original tile
     * @return
     */
    public Tile getTileChange() {
        return tileChange;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActionInfo action = (ActionInfo) o;
        return Objects.equals(uuid, action.uuid) &&
                x == action.x &&
                y == action.y &&
                relative == action.relative &&
                stateChange == action.stateChange &&
                tileChange == action.tileChange;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, x, y, relative, stateChange, tileChange);
    }
}
