package com.incognito.acejam0.states.game;

import com.fasterxml.jackson.core.type.TypeReference;
import com.incognito.acejam0.Application;
import com.incognito.acejam0.domain.Level;
import com.incognito.acejam0.states.common.BackgroundRendererState;
import com.incognito.acejam0.states.common.TypedBaseAppState;
import com.incognito.acejam0.states.menu.MainMenuState;
import com.incognito.acejam0.states.menu.MenuList;
import com.incognito.acejam0.states.menu.OptionsState;
import com.incognito.acejam0.utils.FileLoader;
import com.incognito.acejam0.utils.GlobalMaterials;
import com.incognito.acejam0.utils.GuiText;
import com.incognito.acejam0.utils.TweenUtil;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
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
import com.simsilica.lemur.anim.SpatialTweens;
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
    private BitmapText levelMessage;

    private boolean menuOpen = false;
    private List<String> levels;
    private static int currentLevel = 0;

    public static boolean inProgress() {
        return currentLevel != 0;
    }

    private final ActionListener menuListener = (name, isPressed, tpf) -> {
        if (isPressed) {
            toggleMenu();
        }
    };

    private final ActionListener nextLevelListener = (name, isPressed, tpf) -> {
        if (isPressed && !menuOpen) {
            appStateManager.detach(appStateManager.getState(MapRendererState.class));
            appStateManager.detach(appStateManager.getState(PlayerState.class));
            currentLevel++;
            if (currentLevel >= levels.size()) {
                appStateManager.detach(appStateManager.getState(GameState.class));
                appStateManager.attach(new MainMenuState(BackgroundRendererState.BgState.RAINBOW));
                currentLevel = 0;
            } else {
                startLevel();
            }
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

        TweenUtil.addAnimation(menuNode, () -> SpatialTweens.move(menuNode, null, menuNode.getLocalTranslation().add(offset), 1.0));
        TweenUtil.addAnimation(menuScreen, () -> new AbstractTween(0.5f) {
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
        });
    }

    @Override
    protected void onInitialize(Application app) {
        appStateManager = app.getStateManager();
        inputManager = app.getInputManager();
        AppSettings settings = app.getContext().getSettings();

        guiNode = app.getGuiNode();

        menuNode = new Node();
        BitmapText label = GuiText.makeText("PAUSED", ColorRGBA.LightGray, 50f);
        label.setLocalTranslation(30f, -10f, 0f);
        menuNode.attachChild(label);

        menu = new MenuList(app.getFontOutline(), ColorRGBA.LightGray, ColorRGBA.DarkGray, 35f, BitmapFont.Align.Left, List.of(
                Map.entry("OPTIONS", () -> {
                    menu.onDisable(inputManager);
                    inputManager.removeListener(menuListener);
                    appStateManager.attach(new OptionsState(() -> {
                        inputManager.addListener(menuListener, "menu");
                        menu.onEnable(inputManager);
                    }));
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

        menuNode.setLocalTranslation(-(menu.getWidth() + 60f), settings.getHeight(), 2f);

        guiNode.attachChild(menuNode);

        menuScreen = new Geometry("", new Quad(settings.getWidth(), settings.getHeight()));
        ColorRGBA screenColor = ColorRGBA.BlackNoAlpha;
        Material mat = GlobalMaterials.getDebugMaterial(screenColor);
        mat.setColor("Color", screenColor); //shared material, reset back to initial invisible color
        menuScreen.setMaterial(mat);
        guiNode.attachChild(menuScreen);

        inputManager.addMapping("menu", new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addListener(menuListener, "menu");
        inputManager.addMapping("nextLevel", new KeyTrigger(KeyInput.KEY_SPACE), new KeyTrigger(KeyInput.KEY_RETURN));

        levelMessage = GuiText.makeOutlineText("", ColorRGBA.LightGray, 35f);
        levelMessage.setBox(new Rectangle(0, levelMessage.getLineHeight() + 60f, settings.getWidth(), levelMessage.getLineHeight() + 60f));
        levelMessage.setAlignment(BitmapFont.Align.Center);
        levelMessage.setVerticalAlignment(BitmapFont.VAlign.Center);
        guiNode.attachChild(levelMessage);

        levels = FileLoader.readFile("levels.json", new TypeReference<>() {});
        if (levels == null || levels.isEmpty()) {
            logger.error("No levels found");
            return;
        }

        addResizeListener(s -> {
            menuScreen.setMesh(new Quad(s.getWidth(), s.getHeight()));
            menuNode.setLocalTranslation(menuNode.getLocalTranslation().x, s.getHeight(), 2f);
            menuBg.setMesh(new Quad(menu.getWidth() + 60f, s.getHeight()));
            menuBg.setLocalTranslation(0, -settings.getHeight(), -1f);
            levelMessage.setBox(new Rectangle(0, levelMessage.getLineHeight() + 60f, s.getWidth(), levelMessage.getLineHeight() + 60f));
        });

        startLevel();
    }

    private void startLevel() {
        inputManager.removeListener(nextLevelListener);
        Level level = Level.loadLevel(levels.get(currentLevel));
        if (level == null) {
            logger.error("Could not load level {} ({})", currentLevel, levels.get(currentLevel));
            appStateManager.detach(this);
            appStateManager.attach(new MainMenuState());
            return;
        }
        TweenUtil.tweenText(levelMessage, level.getMessage(), 1.5f, 0.5f);

        appStateManager.attachAll(new MapRendererState(level), new PlayerState(level));
        appStateManager.getState(PlayerState.class).addCompletedListener(() -> {
            inputManager.addListener(nextLevelListener, "nextLevel");
            TweenUtil.tweenText(levelMessage, "Press Space/Enter to continue", 1.0f, 0.25f);
        });
    }

    @Override
    protected void onCleanup(Application app) {
        guiNode.detachChild(menuNode);
        guiNode.detachChild(menuScreen);
        guiNode.detachChild(levelMessage);
        menu.cleanup(inputManager);
        inputManager.removeListener(menuListener);
        inputManager.deleteMapping("menu");
        inputManager.removeListener(nextLevelListener);
        inputManager.deleteMapping("nextLevel");

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
