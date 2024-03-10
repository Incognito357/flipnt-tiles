package com.incognito.acejam0.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.NUMBER)
public enum Tile {
    EMPTY,
    WALL,
    FLOOR,
    START,
    EXIT,
    BUTTON
}
