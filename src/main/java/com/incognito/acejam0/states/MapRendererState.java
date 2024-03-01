package com.incognito.acejam0.states;

import com.incognito.acejam0.Application;
import com.incognito.acejam0.domain.Action;
import com.incognito.acejam0.domain.ActionInfo;
import com.incognito.acejam0.domain.Level;
import com.incognito.acejam0.domain.Tile;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.simsilica.lemur.anim.AbstractTween;
import com.simsilica.lemur.anim.AnimationState;
import com.simsilica.lemur.anim.SpatialTweens;
import com.simsilica.lemur.anim.Tween;
import com.simsilica.lemur.anim.Tweens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapRendererState extends TypedBaseAppState<Application> {

    private Level level;

    private Node rootNode;
    private final Node tiles = new Node();
    private PlayerState playerState;

    private final Map<Tile, Material> tileMats = new HashMap<>();

    public MapRendererState(Level level) {
        this.level = level;
    }

    @Override
    protected void onInitialize(Application app) {
        rootNode = app.getRootNode();

        List<Tile> map = level.getMap();
        for (int i = 0; i < map.size(); i++) {
            Tile tile = map.get(i);
            int x = i % level.getWidth();
            int y = i / level.getWidth();
            Geometry g = new Geometry(String.format("x:%d,y:%d", x, y), new Quad(1, 1));
            g.setMaterial(tileMats.computeIfAbsent(tile, t -> {
                Material mat = new Material(app.getAssetManager(), Materials.UNSHADED);
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
            if (!level.isTileEnabled(x, y)) {
                g.rotate(0, FastMath.PI, 0);
            }

            tiles.attachChild(g);
        }
        tiles.setLocalTranslation(-level.getWidth() / 2.0f, level.getHeight() / 2.0f, 0);

        rootNode.attachChild(tiles);

        playerState = new PlayerState(level);
        app.getStateManager().attach(playerState);
    }

    @Override
    protected void onCleanup(Application app) {
        rootNode.detachChild(tiles);
        app.getStateManager().detach(playerState);
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public void update(Action action) {
        List<Tween> tweens = new ArrayList<>();
        for (ActionInfo change : action.getActions()) {
            int x = change.getX();
            int y = change.getY();
            int i = y * level.getWidth() + x;
            int state = change.getStateChange();
            Tile tile = change.getTileChange();
            Spatial node = rootNode.getChild(i);

            boolean oldState = level.isTileEnabled(x, y);
            Tile oldTile = level.getTile(x, y);
            if (state == 2 || (state == -1 && oldState) || state == 1 && !oldState) {
                tweens.add(SpatialTweens.rotate(
                        node, null,
                        oldState ? Quaternion.IDENTITY
                                : new Quaternion().fromAngleNormalAxis(FastMath.PI, Vector3f.UNIT_Y),
                        0.75));
            }

            if (tile != null && tile != oldTile) {
                Material origin = tileMats.get(oldTile);
                Material target = tileMats.get(tile);
                Material mat = origin.clone();
                ColorRGBA originCol = origin.getParamValue("Color");
                ColorRGBA targetCol = target.getParamValue("Color");
                node.setMaterial(mat);
                tweens.add(new AbstractTween(0.75f) {
                    @Override
                    protected void doInterpolate(double t) {
                        if (t == 1) {
                            node.setMaterial(target);
                            return;
                        }
                        ColorRGBA lerped = ColorRGBA.fromRGBA255(
                                (int) FastMath.interpolateLinear((float) t, originCol.r, targetCol.r),
                                (int) FastMath.interpolateLinear((float) t, originCol.g, targetCol.g),
                                (int) FastMath.interpolateLinear((float) t, originCol.b, targetCol.b),
                                (int) FastMath.interpolateLinear((float) t, originCol.a, targetCol.a));
                        mat.setColor("Color", lerped);
                    }
                });
            }

        }
        AnimationState.getDefaultInstance().add(Tweens.parallel(tweens.toArray(new Tween[0])));
    }
}
