package com.incognito.acejam0.utils;

import com.incognito.acejam0.Application;
import com.incognito.acejam0.domain.Tile;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;

import java.util.HashMap;
import java.util.Map;

public class GlobalMaterials {
    private GlobalMaterials() {}

    private static final Map<String, Material> mats = new HashMap<>();
    private static final Map<String, ColorRGBA> colors = Map.of(
            Tile.EMPTY.name(), ColorRGBA.fromRGBA255(0, 0, 0, 0),
            Tile.WALL.name(), ColorRGBA.fromRGBA255(16, 16, 16, 255),
            Tile.FLOOR.name(), ColorRGBA.fromRGBA255(239, 239, 239, 255),
            Tile.START.name(), ColorRGBA.Cyan,
            Tile.EXIT.name(), ColorRGBA.Green,
            "PLAYER", ColorRGBA.Cyan);

    public static Material getTileMaterial(Tile tile) {
        return mats.computeIfAbsent(tile.name(), GlobalMaterials::createMaterial);
    }

    public static Material getPlayerMaterial() {
        return mats.computeIfAbsent("PLAYER", GlobalMaterials::createMaterial);
    }

    private static Material createMaterial(String name) {
        if (Tile.EMPTY.name().equals(name)) {
            Material mat = new Material(Application.APP.getAssetManager(), Materials.UNSHADED);
            mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            mat.setColor("Color", colors.get(name));
            return mat;
        }
        return getShaderMaterial(colors.get(name), FastMath.nextRandomFloat() * 5f + 5f, FastMath.nextRandomFloat() + 0.45f, 30f);
    }

    public static Material getDebugMaterial(ColorRGBA color) {
        return mats.computeIfAbsent(color.toString(), c -> {
            Material mat = new Material(Application.APP.getAssetManager(), Materials.UNSHADED);
            mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            mat.setColor("Color", color);
            return mat;
        });
    }

    public static Material getShaderMaterial(ColorRGBA color, float speed, float scale, float strength) {
        Material mat = new Material(Application.APP.getAssetManager(), "shaders/background.j3md");
        mat.setColor("Color", color);
        mat.setInt("Seed", FastMath.nextRandomInt());
        mat.setFloat("Speed", speed);
        mat.setFloat("Scale", scale);
        mat.setFloat("Strength", strength);
        mat.setBoolean("ScreenSpace", false);
        return mat;
    }

    public static Material getShaderMaterial(ColorRGBA color) {
        return getShaderMaterial(color, 7.5f, 1.0f, 30f);
    }
}
