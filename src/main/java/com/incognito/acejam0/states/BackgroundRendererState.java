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
import com.simsilica.lemur.anim.Tween;

public class BackgroundRendererState extends TypedBaseAppState<Application> {

    private Node rootNode;
    private Geometry background;

    private static final float speed = 7.5f;
    private static final float scale = 1.0f;
    private static final float strength = 30f;

    @Override
    protected void onInitialize(Application app) {
        rootNode = app.getRootNode();

        AppSettings settings = app.getContext().getSettings();
        background = new Geometry("", new CenterQuad(settings.getWidth() / 2f, settings.getHeight() / 2f));
        Material mat = GlobalMaterials.getShaderMaterial(ColorRGBA.Blue, speed, scale, strength);
        mat.setBoolean("ScreenSpace", true);
        background.setMaterial(mat);
        background.setLocalTranslation(0, 0, -10f);

        rootNode.attachChild(background);
    }

    @Override
    protected void onCleanup(Application app) {
        rootNode.detachChild(background);
    }

    public Tween createTween(ColorRGBA target, float length) {
        Material mat = background.getMaterial();
        ColorRGBA origin = mat.getParamValue("Color");
        //float originSpeed = mat.getParamValue("Speed");
        float originStrength = mat.getParamValue("Strength");
        //float targetSpeed = originSpeed > speed ? speed : originSpeed * 1.5f;
        float targetStrength = originStrength > strength ? strength : originStrength * 3f;
        return new AbstractTween(length) {

            @Override
            protected void doInterpolate(double t) {
                if (t == 1) {
                    mat.setColor("Color", target);
                    //mat.setFloat("Speed", targetSpeed);
                    mat.setFloat("Strength", targetStrength);
                    return;
                }
                ColorRGBA mixed = new ColorRGBA();
                mixed.interpolateLocal(origin, target, (float)t);
                mat.setColor("Color", mixed);

//                float speedInt = FastMath.interpolateLinear((float)t, originSpeed, targetSpeed);
//                mat.setFloat("Speed", speedInt);

                float strengthInt = FastMath.interpolateLinear((float)t, originStrength, targetStrength);
                mat.setFloat("Strength", strengthInt);
            }
        };
    }
}
