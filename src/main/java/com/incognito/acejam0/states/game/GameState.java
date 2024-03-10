package com.incognito.acejam0.states.game;

import com.fasterxml.jackson.core.type.TypeReference;
import com.incognito.acejam0.Application;
import com.incognito.acejam0.domain.Level;
import com.incognito.acejam0.states.common.TypedBaseAppState;
import com.incognito.acejam0.states.menu.MainMenuState;
import com.incognito.acejam0.states.menu.MenuList;
import com.incognito.acejam0.utils.FileLoader;
import com.incognito.acejam0.utils.GlobalMaterials;
import com.incognito.acejam0.utils.GuiText;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.anim.AbstractTween;
import com.simsilica.lemur.anim.AnimationState;
import com.simsilica.lemur.anim.SpatialTweens;
import com.simsilica.lemur.anim.Tween;
import com.simsilica.lemur.anim.TweenAnimation;
import com.simsilica.lemur.anim.Tweens;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class GameState extends TypedBaseAppState<Application> {

    private static final Logger logger = LogManager.getLogger();

    private AppStateManager appStateManager;
    private InputManager inputManager;
    private Node guiNode;
    private Node menuNode;
    private Geometry menuScreen;
    private MenuList menu;
    private TweenAnimation menuAnimation = null;

    private boolean menuOpen = false;
    private List<String> levels;

    private final ActionListener menuListener = (name, isPressed, tpf) -> {
        if (isPressed) {
            toggleMenu();
        }
    };

    private void toggleMenu() {
        Vector3f offset = new Vector3f(menu.getWidth() + 60f, 0f, 0f);
        if (menuOpen) {
            menu.onDisable(inputManager);
            appStateManager.getState(PlayerState.class).onEnable();
            offset.x *= -1;
        } else {
            menu.onEnable(inputManager);
            appStateManager.getState(PlayerState.class).onDisable();
        }
        menuOpen = !menuOpen;

        if (menuAnimation != null) {
            menuAnimation.fastForwardPercent(1.0);
            menuAnimation = null;
        }

        Tween move = SpatialTweens.move(menuNode, null, menuNode.getLocalTranslation().add(offset), 1.0);
        Tween bg = new AbstractTween(0.5f) {
            private final Material mat = menuScreen.getMaterial();
            private final ColorRGBA original = mat.getParamValue("Color");
            private final ColorRGBA target = menuOpen ? ColorRGBA.fromRGBA255(0, 0, 0, 128) : ColorRGBA.fromRGBA255(0, 0, 0, 0);

            @Override
            protected void doInterpolate(double t) {
                if (t >= 1.0) {
                    menuScreen.getMaterial().setColor("Color", target);
                }
                ColorRGBA lerped = new ColorRGBA().interpolateLocal(original, target, (float) t);
                mat.setColor("Color", lerped);
            }
        };
        menuAnimation = AnimationState.getDefaultInstance().add(Tweens.parallel(move, bg));
    }

    @Override
    protected void onInitialize(Application app) {
        appStateManager = app.getStateManager();
        inputManager = app.getInputManager();
        AppSettings settings = app.getContext().getSettings();

        BitmapFont guiFont = app.getGuiFont();
        guiNode = app.getGuiNode();

        menuNode = new Node();
        BitmapText label = GuiText.makeText("PAUSED", ColorRGBA.LightGray, 50f);
        label.setLocalTranslation(30f, -10f, 0f);
        menuNode.attachChild(label);

        menu = new MenuList(guiFont, ColorRGBA.LightGray, ColorRGBA.DarkGray, 35f, BitmapFont.Align.Left, List.of(
                Map.entry("OPTIONS", () -> {
                }),
                Map.entry("RESTART", () -> {
                    appStateManager.getState(PlayerState.class).restartLevel();
                    toggleMenu();
                }),
                Map.entry("MAIN MENU", () -> {
                    appStateManager.detach(this);
                    appStateManager.attach(new MainMenuState());
                }),
                Map.entry("EXIT GAME", app::stop)));
        menu.initialize(inputManager);

        menuNode.attachChild(menu);
        menu.setLocalTranslation(30f, -label.getLineHeight() - 30f, 0);

        Geometry menuBg = new Geometry("", new Quad(menu.getWidth() + 60f, settings.getHeight()));
        menuBg.setMaterial(GlobalMaterials.getDebugMaterial(ColorRGBA.fromRGBA255(0, 0, 0, 128)));
        menuBg.setLocalTranslation(0, -settings.getHeight(), -1f);
        menuNode.attachChild(menuBg);

        menuNode.setLocalTranslation(-(menu.getWidth() + 60f), app.getContext().getSettings().getHeight(), 2f);

        guiNode.attachChild(menuNode);

        menuScreen = new Geometry("", new Quad(settings.getWidth(), settings.getHeight()));
        menuScreen.setMaterial(GlobalMaterials.getDebugMaterial(ColorRGBA.fromRGBA255(0, 0, 0, 0)));
        guiNode.attachChild(menuScreen);

        inputManager.addMapping("menu", new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addListener(menuListener, "menu");

        levels = FileLoader.readFile("levels.json", new TypeReference<>() {});
        if (levels == null || levels.isEmpty()) {
            logger.error("No levels found");
            return;
        }
        Level level = Level.loadLevel(levels.get(0));
        appStateManager.attachAll(new MapRendererState(level), new PlayerState(level));
    }

    @Override
    protected void onCleanup(Application app) {
        toggleMenu();
        guiNode.detachChild(menuNode);
        guiNode.detachChild(menuScreen);
        menu.cleanup(inputManager);
        inputManager.removeListener(menuListener);
        inputManager.deleteMapping("menu");

        appStateManager.detach(appStateManager.getState(MapRendererState.class));
        appStateManager.detach(appStateManager.getState(PlayerState.class));
    }

    @Override
    public void update(float tpf) {
        if (menuOpen) {
            menu.update();
        }
    }
}
