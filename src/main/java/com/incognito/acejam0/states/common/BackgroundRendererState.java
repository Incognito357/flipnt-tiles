package com.incognito.acejam0.states.common;

import com.incognito.acejam0.Application;
import com.incognito.acejam0.utils.GlobalMaterials;
import com.incognito.acejam0.utils.TweenUtil;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.CenterQuad;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.anim.AbstractTween;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BackgroundRendererState extends TypedBaseAppState<Application> {

    private Node rootNode;
    private Geometry background;
    private BgState currentState;

    public enum BgState {
        FRONT(7.5f, 1.0f, 10f, ColorRGBA.Blue),
        BACK(7.5f, 1.0f, 90.0f, ColorRGBA.Red.mult(0.5f)),
        COMPLETE(10.0f, 1.0f, 90.0f, ColorRGBA.Green),
        EDITOR(7.5f, 1.0f, 10f, ColorRGBA.Orange),
        MENU(3.5f, 1.0f, 60.0f, ColorRGBA.DarkGray.mult(0.5f)),
        RAINBOW(30.0f, 1.0f, 110.0f, ColorRGBA.Black);

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
        Material mat = GlobalMaterials.getBgMaterial(currentState.color, currentState.speed, currentState.scale, currentState.strength);
        background.setMaterial(mat);
        background.setLocalTranslation(0, 0, -10f);

        rootNode.attachChild(background);
    }

    @Override
    protected void onCleanup(Application app) {
        rootNode.detachChild(background);
    }

    public void setBackgroundState(BgState target, float length) {
        Material mat = background.getMaterial();
        ColorRGBA origin = mat.getParamValue("Color");
        float originSpeed = mat.getParamValue("Speed");
        float originScale = mat.getParamValue("Scale");
        float originStrength = mat.getParamValue("Strength");
        TweenUtil.addAnimation(background, () -> new AbstractTween(length) {

            @Override
            protected void doInterpolate(double t) {
                if (t == 1) {
                    mat.setColor("Color", target.color);
                    mat.setFloat("Speed", target.speed);
                    mat.setFloat("Scale", target.scale);
                    mat.setFloat("Strength", target.strength);
                    currentState = target;
                    return;
                }
                ColorRGBA mixed = new ColorRGBA().interpolateLocal(origin, target.color, (float) t);
                mat.setColor("Color", mixed);

                float speedInt = FastMath.interpolateLinear((float) t, originSpeed, target.speed);
                mat.setFloat("Speed", speedInt);

                float scaleInt = FastMath.interpolateLinear((float) t, originScale, target.scale);
                mat.setFloat("Scale", scaleInt);

                float strengthInt = FastMath.interpolateLinear((float) t, originStrength, target.strength);
                mat.setFloat("Strength", strengthInt);
            }
        });
        if (target == BgState.RAINBOW) {
            //green last, to blend nicer from level complete
            List<ColorRGBA> colors = Arrays.asList(ColorRGBA.Blue, ColorRGBA.Magenta, ColorRGBA.Red, ColorRGBA.Orange, ColorRGBA.Yellow, ColorRGBA.Green);
            TweenUtil.addLoop(background, () -> IntStream.range(0, colors.size())
                    .mapToObj(i -> new AbstractTween(2.0f) {
                        private final ColorRGBA origin = i == 0 ? colors.get(colors.size() - 1) : colors.get(i - 1);
                        private final ColorRGBA target = colors.get(i);

                        @Override
                        protected void doInterpolate(double t) {
                            mat.setColor("Color", new ColorRGBA().interpolateLocal(origin, target, (float) t));
                        }
                    })
                    .collect(Collectors.toList()));
        }
    }
}
