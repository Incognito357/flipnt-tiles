package com.incognito.acejam0.states;

import com.incognito.acejam0.Application;
import com.incognito.acejam0.domain.Action;
import com.incognito.acejam0.domain.ActionInfo;
import com.incognito.acejam0.domain.Level;
import com.incognito.acejam0.domain.Tile;
import com.incognito.acejam0.utils.GlobalMaterials;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.CenterQuad;
import com.simsilica.lemur.anim.AbstractTween;
import com.simsilica.lemur.anim.AnimationState;
import com.simsilica.lemur.anim.SpatialTweens;
import com.simsilica.lemur.anim.Tween;
import com.simsilica.lemur.anim.TweenAnimation;
import com.simsilica.lemur.anim.Tweens;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class MapRendererState extends TypedBaseAppState<Application> {

    private static final Logger logger = LogManager.getLogger();

    private Level level;

    private Node rootNode;
    private final Node tiles1 = new Node();
    private final Node tiles2 = new Node();
    private boolean editing = false;
    private TweenAnimation currentTween;

    public MapRendererState(Level level) {
        this.level = level;
    }

    @Override
    protected void onInitialize(Application app) {
        rootNode = app.getRootNode();

        reloadLevel();
    }

    @Override
    protected void onCleanup(Application app) {
        rootNode.detachChild(tiles1);
    }

    public void setLevel(Level level) {
        this.level = level;
        reloadLevel();
    }

    public void reloadLevel() {
        rootNode.detachChild(tiles1);
        rootNode.detachChild(tiles2);
        tiles1.detachAllChildren();
        tiles2.detachAllChildren();

        List<Tile> map = level.getMap();
        List<Tile> map2 = level.getMap2();
        for (int i = 0; i < map.size(); i++) {
            Tile tile = map.get(i);
            Tile tile2 = map2.get(i);
            int x = i % level.getWidth();
            int y = i / level.getWidth();

            Geometry g = new Geometry("", new CenterQuad(1, 1));
            g.setMaterial(GlobalMaterials.getTileMaterial(!editing && tile == Tile.START ? Tile.FLOOR : tile));
            g.setLocalTranslation(x, -y, 0);

            Geometry g2 = new Geometry("", new CenterQuad(1, 1));
            g2.setMaterial(GlobalMaterials.getTileMaterial(!editing && tile2 == Tile.START ? Tile.FLOOR : tile2));
            g2.setLocalTranslation(x, -y, 0);

            if (level.isTileFlipped(x, y)) {
                g.rotate(0, FastMath.PI, 0);
            } else {
                g2.rotate(0, FastMath.PI, 0);
            }

            tiles1.attachChild(g);
            tiles2.attachChild(g2);
        }
        Camera camera = getApplication().getCamera();
        camera.setLocation(new Vector3f(level.getWidth() / 2.0f - 0.5f, -level.getHeight() / 2.0f + 0.5f, camera.getLocation().z));

        rootNode.attachChild(tiles1);
        rootNode.attachChild(tiles2);
    }

    public void update(Action action) {
        if (currentTween != null && currentTween.isRunning()) {
            currentTween.fastForwardPercent(1.0);
            currentTween = null;
        }
        List<Tween> tweens = new ArrayList<>();
        for (ActionInfo change : action.getActions()) {
            int x = change.getX();
            int y = change.getY();
            int i = y * level.getWidth() + x;
            int state = change.getStateChange();
            Tile tile = change.getTileChange();
            Spatial node1 = tiles1.getChild(i);
            Spatial node2 = tiles2.getChild(i);

            boolean oldState = level.isTileFlipped(x, y);
            Tile oldTile = level.getTile(x, y);
            Tile oldTile2 = level.getTile2(x, y);
            if (state == 2 || (state == -1 && !oldState) || (state == 1 && oldState)) {
                tweens.add(SpatialTweens.rotate(
                        node1, null,
                        oldState ? Quaternion.IDENTITY
                                : new Quaternion().fromAngleNormalAxis(FastMath.PI, Vector3f.UNIT_Y),
                        0.75));
                tweens.add(SpatialTweens.rotate(
                        node2, null,
                        !oldState ? Quaternion.IDENTITY
                                : new Quaternion().fromAngleNormalAxis(FastMath.PI, Vector3f.UNIT_Y),
                        0.75));
            }

            Tile tileToChange = oldState ? oldTile2 : oldTile;
            Spatial nodeToChange = oldState ? node2 : node1;
            if (tile != null && tile != tileToChange) {
                Material origin = GlobalMaterials.getTileMaterial(tileToChange);
                Material target = GlobalMaterials.getTileMaterial(tile);
                Material mat = origin.clone();
                ColorRGBA originCol = origin.getParamValue("Color");
                ColorRGBA targetCol = target.getParamValue("Color");
                nodeToChange.setMaterial(mat);
                tweens.add(new AbstractTween(0.75f) {
                    @Override
                    protected void doInterpolate(double t) {
                        if (t == 1) {
                            nodeToChange.setMaterial(target);
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

        level.performActions(action);

        logger.info("Adding {} tweens", tweens.size());
        currentTween = AnimationState.getDefaultInstance().add(Tweens.parallel(tweens.toArray(new Tween[0])));
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }
}
