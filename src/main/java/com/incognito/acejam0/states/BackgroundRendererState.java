package com.incognito.acejam0.states;

import com.incognito.acejam0.Application;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.CenterQuad;
import com.jme3.system.AppSettings;

public class BackgroundRendererState extends TypedBaseAppState<Application> {

    private Node rootNode;
    private Camera camera;

    private Material mat;
    private Geometry background;
    private Geometry border;

    @Override
    protected void onInitialize(Application app) {
        rootNode = app.getRootNode();
        camera = app.getCamera();

        mat = new Material(app.getAssetManager(), "assets/Shaders/background.j3md");

        AppSettings settings = app.getContext().getSettings();
        background = new Geometry("", new CenterQuad(settings.getWidth() / 2f, settings.getHeight() / 2f));
        mat.setColor("Color", ColorRGBA.Blue);
        background.setMaterial(mat);
        //background.setLocalTranslation(camera.getLocation().x, camera.getLocation().y, -10);
        //border = new Geometry("", new WireBox(2.5f, 2.5f, 0));
        //border.setMaterial(GlobalMaterials.getDebugMaterial(ColorRGBA.Green));

        rootNode.attachChild(background);
        //rootNode.attachChild(border);
    }

    @Override
    protected void onCleanup(Application app) {
        rootNode.detachChild(background);
    }

    @Override
    public void update(float tpf) {
        //background.setLocalTranslation(camera.getLocation().x, camera.getLocation().y, -10);
    }
}
