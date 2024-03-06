package com.incognito.acejam0.states;

import com.incognito.acejam0.Application;
import com.incognito.acejam0.utils.GlobalMaterials;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.CenterQuad;
import com.jme3.system.AppSettings;

public class BackgroundRendererState extends TypedBaseAppState<Application> {

    private Node rootNode;
    private Geometry background;

    @Override
    protected void onInitialize(Application app) {
        rootNode = app.getRootNode();

        AppSettings settings = app.getContext().getSettings();
        background = new Geometry("", new CenterQuad(settings.getWidth() / 2f, settings.getHeight() / 2f));
        background.setMaterial(GlobalMaterials.getBackgroundMaterial(ColorRGBA.Blue));
        background.setLocalTranslation(0, 0, -10f);

        rootNode.attachChild(background);
    }

    @Override
    protected void onCleanup(Application app) {
        rootNode.detachChild(background);
    }
}
