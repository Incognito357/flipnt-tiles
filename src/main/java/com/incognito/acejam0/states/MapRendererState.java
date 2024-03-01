package com.incognito.acejam0.states;

import com.incognito.acejam0.Application;
import com.incognito.acejam0.domain.Level;
import com.incognito.acejam0.domain.Tile;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapRendererState extends TypedBaseAppState<Application> {

    private final Level level;

    private Node rootNode;
    private final Node tiles = new Node();

    private final Map<Tile, Material> tileMats = new HashMap<>();

    public MapRendererState(Level level) {
        this.level = level;
    }

    @Override
    protected void onInitialize(Application app) {
        AssetManager assetManager = app.getAssetManager();
        rootNode = app.getRootNode();

        List<Tile> map = level.getMap();
        for (int i = 0; i < map.size(); i++) {
            Tile tile = map.get(i);
            int x = i % level.getWidth();
            int y = i / level.getWidth();
            Geometry g = new Geometry(String.format("x:%d,y:%d", x, y), new Quad(1, 1));
            g.setMaterial(tileMats.computeIfAbsent(tile, t -> {
                Material mat = new Material(assetManager, Materials.UNSHADED);
                mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
                mat.setColor("Color", switch (t) {
                    case EMPTY -> ColorRGBA.fromRGBA255(0, 0, 0, 0);
                    case WALL -> ColorRGBA.DarkGray;
                    case FLOOR -> ColorRGBA.LightGray;
                    case START -> ColorRGBA.Cyan;
                    case EXIT -> ColorRGBA.Green;
                });
                return mat;
            }));
            g.setLocalTranslation(x, -y - 1f, 0);
            tiles.attachChild(g);
        }
        tiles.setLocalTranslation(-level.getWidth() / 2.0f, level.getHeight() / 2.0f, 0);

        rootNode.attachChild(tiles);
    }

    @Override
    protected void onCleanup(Application app) {
        rootNode.detachChild(tiles);
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }
}
