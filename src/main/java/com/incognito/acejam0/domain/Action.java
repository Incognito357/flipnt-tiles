package com.incognito.acejam0.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Action {
    private final UUID uuid = UUID.randomUUID();
    private final List<ActionInfo> actions;

    @JsonCreator
    public Action(@JsonProperty("actions") List<ActionInfo> actions) {
        this.actions = actions;
    }

    @JsonIgnore
    public UUID getUuid() {
        return uuid;
    }

    public List<ActionInfo> getActions() {
        return actions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Action action = (Action) o;
        return Objects.equals(uuid, action.uuid) &&
                Objects.equals(actions, action.actions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, actions);
    }
}
