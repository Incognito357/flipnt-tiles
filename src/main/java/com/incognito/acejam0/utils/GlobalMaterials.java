package com.incognito.acejam0.utils;

import com.incognito.acejam0.Application;
import com.incognito.acejam0.domain.Tile;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;

import java.util.HashMap;
import java.util.Map;

public class GlobalMaterials {
    private GlobalMaterials() {}

    private static final Map<String, Material> mats = new HashMap<>();
    private static final Map<String, ColorRGBA> colors = Map.of(
            Tile.EMPTY.name(), ColorRGBA.fromRGBA255(0, 0, 0, 0),
            Tile.WALL.name(), ColorRGBA.DarkGray,
            Tile.FLOOR.name(), ColorRGBA.LightGray,
            Tile.START.name(), ColorRGBA.Cyan,
            Tile.EXIT.name(), ColorRGBA.Green,
            "PLAYER", ColorRGBA.Blue);

    public static Material getTileMaterial(Tile tile) {
        return mats.computeIfAbsent(tile.name(), GlobalMaterials::createMaterial);
    }

    public static Material getPlayerMaterial() {
        return mats.computeIfAbsent("PLAYER", GlobalMaterials::createMaterial);
    }

    private static Material createMaterial(String name) {
        Material mat = new Material(Application.APP.getAssetManager(), Materials.UNSHADED);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        mat.setColor("Color", colors.get(name));
        return mat;
    }
}
