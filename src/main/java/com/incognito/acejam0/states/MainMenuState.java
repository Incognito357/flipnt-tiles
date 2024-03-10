package com.incognito.acejam0.states;

import com.incognito.acejam0.Application;
import com.incognito.acejam0.states.BackgroundRendererState.BgState;
import com.incognito.acejam0.states.menu.MenuList;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.input.InputManager;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;

import java.util.List;
import java.util.Map;

public class MainMenuState extends TypedBaseAppState<Application> {

    private InputManager inputManager;
    private Node guiNode;
    private MenuList menu;

    @Override
    protected void onInitialize(Application app) {
        BitmapFont guiFont;
        inputManager = app.getInputManager();
        guiFont = app.getGuiFont();
        guiNode = app.getGuiNode();

        AppSettings settings = app.getContext().getSettings();
        AppStateManager appStateManager = app.getStateManager();

        appStateManager.getState(BackgroundRendererState.class).setBackgroundState(BgState.MENU, 1.0f);

        menu = new MenuList(guiFont, ColorRGBA.LightGray, ColorRGBA.DarkGray, 50f, BitmapFont.Align.Center, List.of(
                Map.entry("START", () -> {
                    appStateManager.detach(this);
                    appStateManager.attach(new GameState());
                }),
                Map.entry("OPTIONS", () -> {}),
                Map.entry("EXIT", app::stop)));

        menu.setLocalTranslation(settings.getWidth() / 2f - menu.getWidth() / 2f, settings.getHeight() / 2f + menu.getHeight() / 2f, 0);
        menu.initialize(inputManager);
        guiNode.attachChild(menu);
    }

    @Override
    protected void onCleanup(Application app) {
        guiNode.detachChild(menu);
        menu.cleanup(inputManager);
    }

    @Override
    public void update(float tpf) {
        menu.update();
    }

    @Override
    protected void onEnable() {
        menu.onEnable(inputManager);
    }

    @Override
    protected void onDisable() {
        menu.onDisable(inputManager);
    }
}
