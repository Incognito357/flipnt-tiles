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
    private static final Map<String, ColorRGBA> colors = new Builder<>(new HashMap<String, ColorRGBA>())
            .with(Map::put, Tile.EMPTY.name(), ColorRGBA.BlackNoAlpha)
            .with(Map::put, Tile.WALL.name(), ColorRGBA.fromRGBA255(16, 16, 16, 255))
            .with(Map::put, Tile.FLOOR.name(), ColorRGBA.fromRGBA255(239, 239, 239, 255))
            .with(Map::put, Tile.START.name(), ColorRGBA.Cyan)
            .with(Map::put, Tile.EXIT.name(), ColorRGBA.Green)
            .with(Map::put, Tile.BUTTON.name(), ColorRGBA.Orange.mult(0.35f))
            .build();

    public static Material getTileMaterial(Tile tile) {
        return mats.computeIfAbsent(tile.name(), GlobalMaterials::createMaterial);
    }

    private static Material createMaterial(String name) {
        if (Tile.EMPTY.name().equals(name)) {
            return getBgMaterial();
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

    public static Material getBgMaterial() {
        return mats.get("bg");
    }

    public static Material getBgMaterial(ColorRGBA color, float speed, float scale, float strength) {
        return mats.computeIfAbsent("bg", n -> {
            Material mat = getShaderMaterial(color, speed, scale, strength);
            mat.setBoolean("ScreenSpace", true);
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
        mat.setBoolean("LocalSpace", false);
        return mat;
    }
}
