package com.incognito.acejam0.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.UUID;

public class ActionInfo {
    private final UUID uuid = UUID.randomUUID();
    private final int x;
    private final int y;
    private final boolean relative;
    private final int stateChange;      // 2 to flip, -1 to force flip down, 1 to force flip up, 0 to keep original state
    private final Tile tileChange;      // ignores if null
    private final int tileChangeSide;   // -1 to change down tile, 0 to change up tile, 1 to change same side as trigger, 2 to change opposite side of trigger

    @JsonCreator
    public ActionInfo(
            @JsonProperty("x") int x,
            @JsonProperty("y") int y,
            @JsonProperty("relative") boolean relative,
            @JsonProperty("stateChange") int stateChange,
            @JsonProperty("tileChange") Tile tileChange,
            @JsonProperty("tileChangeSide") int tileChangeSide) {
        this.x = x;
        this.y = y;
        this.relative = relative;
        this.stateChange = stateChange;
        this.tileChange = tileChange;
        this.tileChangeSide = tileChangeSide;
    }

    @JsonIgnore
    public UUID getUuid() {
        return uuid;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /**
     * Coordinates are interpreted as relative to players. Will result in
     * transformed actions getting generated during gameplay, with fixed
     * coordinates, and this flag set to false.
     * @return
     */
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
     * Tile to change, or null for no change
     */
    public Tile getTileChange() {
        return tileChange;
    }

    /**
     * -1 to change inactive tile, 0 to change active tile,
     * 1 to change same side as trigger, 2 to change opposite side.
     * Similar to "isRelative", if the value is 1 or 2, new actions will be
     * generated based on the current map state, with this value transformed
     * to either -1 or 0.
     */
    public int getTileChangeSide() {
        return tileChangeSide;
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
                tileChange == action.tileChange &&
                tileChangeSide == action.tileChangeSide;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, x, y, relative, stateChange, tileChange, tileChangeSide);
    }
}
