package com.incognito.acejam0.states;

import com.incognito.acejam0.Application;
import com.incognito.acejam0.domain.Level;
import com.incognito.acejam0.domain.Tile;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class PlayerState extends TypedBaseAppState<Application> {

    private static final Logger logger = LogManager.getLogger();

    private Level level;
    private Node rootNode;
    private Node playersNode = new Node();
    private List<Spatial> players = new ArrayList<>();
    private Material mat;

    public PlayerState(Level level) {
        this.level = level;
    }

    @Override
    protected void onInitialize(Application app) {
        rootNode = app.getRootNode();

        mat = new Material(app.getAssetManager(), Materials.UNSHADED);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        mat.setColor("Color", ColorRGBA.Blue);

        List<Tile> map = level.getMap();
        for (int i = 0; i < map.size(); i++) {
            if (map.get(i) == Tile.START) {
                int x = i % level.getWidth();
                int y = i / level.getWidth();
                logger.info("Creating player at {}, {}", x, y);
                Geometry g = new Geometry(String.format("player-x:%d,y:%d", x, y), new Sphere(16, 2, 0.4f));
                g.setMaterial(mat);
                g.rotate(FastMath.HALF_PI, 0, 0);
                g.setLocalTranslation(x + 0.5f, -y - 0.5f, 0);
                players.add(g);
                playersNode.attachChild(g);
            }
        }
        playersNode.setLocalTranslation(-level.getWidth() / 2.0f, level.getHeight() / 2.0f, 0);

        rootNode.attachChild(playersNode);
    }

    @Override
    protected void onCleanup(Application app) {
        rootNode.detachChild(playersNode);
    }

    public void setLevel(Level level) {
        this.level = level;
    }
}
