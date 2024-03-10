package com.incognito.acejam0.states;

import com.incognito.acejam0.Application;
import com.incognito.acejam0.utils.GlobalMaterials;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.CenterQuad;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.anim.AbstractTween;
import com.simsilica.lemur.anim.AnimationState;
import com.simsilica.lemur.anim.TweenAnimation;

public class BackgroundRendererState extends TypedBaseAppState<Application> {

    private Node rootNode;
    private Geometry background;
    private TweenAnimation currentTween;
    private BgState currentState;

    public enum BgState {
        FRONT(7.5f, 1.0f, 1.0f, ColorRGBA.Blue),
        BACK(7.5f, 1.0f, 90.0f, ColorRGBA.Red.mult(0.5f)),
        COMPLETE(10.0f, 1.0f, 90.0f, ColorRGBA.Green),
        EDITOR(7.5f, 1.0f, 1.0f, ColorRGBA.Orange),
        MENU(3.5f, 1.0f, 60.0f, ColorRGBA.DarkGray.mult(0.5f));

        final float speed;
        final float scale;
        final float strength;
        final ColorRGBA color;

        BgState(float speed, float scale, float strength, ColorRGBA color) {
            this.speed = speed;
            this.scale = scale;
            this.strength = strength;
            this.color = color;
        }
    }

    public BackgroundRendererState(BgState initialState) {
        currentState = initialState;
    }

    @Override
    protected void onInitialize(Application app) {
        rootNode = app.getRootNode();

        AppSettings settings = app.getContext().getSettings();
        background = new Geometry("", new CenterQuad(settings.getWidth() / 2f, settings.getHeight() / 2f));
        Material mat = GlobalMaterials.getShaderMaterial(currentState.color, currentState.speed, currentState.scale, currentState.strength);
        mat.setBoolean("ScreenSpace", true);
        background.setMaterial(mat);
        background.setLocalTranslation(0, 0, -10f);

        rootNode.attachChild(background);
    }

    @Override
    protected void onCleanup(Application app) {
        rootNode.detachChild(background);
    }

    public void setBackgroundState(BgState target, float length) {
        if (currentTween != null) {
            currentTween.fastForwardPercent(1.0);
        }
        Material mat = background.getMaterial();
        ColorRGBA origin = mat.getParamValue("Color");
        float originSpeed = mat.getParamValue("Speed");
        float originScale = mat.getParamValue("Scale");
        float originStrength = mat.getParamValue("Strength");
        currentTween = AnimationState.getDefaultInstance().add(new AbstractTween(length) {

            @Override
            protected void doInterpolate(double t) {
                if (t == 1) {
                    mat.setColor("Color", target.color);
                    mat.setFloat("Speed", target.speed);
                    mat.setFloat("Scale", target.scale);
                    mat.setFloat("Strength", target.strength);
                    return;
                }
                ColorRGBA mixed = new ColorRGBA().interpolateLocal(origin, target.color, (float)t);
                mat.setColor("Color", mixed);

                float speedInt = FastMath.interpolateLinear((float)t, originSpeed, target.speed);
                mat.setFloat("Speed", speedInt);

                float scaleInt = FastMath.interpolateLinear((float)t, originScale, target.scale);
                mat.setFloat("Scale", scaleInt);

                float strengthInt = FastMath.interpolateLinear((float)t, originStrength, target.strength);
                mat.setFloat("Strength", strengthInt);
            }
        });
    }
}
