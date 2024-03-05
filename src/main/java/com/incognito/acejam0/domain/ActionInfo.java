package com.incognito.acejam0.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ActionInfo {
    private final int x;
    private final int y;
    private final int stateChange;  //2 to flip, -1 to force flip down, 1 to force flip up, 0 to keep original state
    private final Tile tileChange;  //ignores if null

    @JsonCreator
    public ActionInfo(
            @JsonProperty("x") int x,
            @JsonProperty("y") int y,
            @JsonProperty("stateChange") int stateChange,
            @JsonProperty("tileChange") Tile tileChange) {
        this.x = x;
        this.y = y;
        this.stateChange = stateChange;
        this.tileChange = tileChange;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
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
        return x == action.x &&
                y == action.y &&
                stateChange == action.stateChange &&
                tileChange == action.tileChange;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, stateChange, tileChange);
    }
}
