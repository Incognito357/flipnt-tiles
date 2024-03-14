package com.incognito.acejam0.states.menu;

import com.incognito.acejam0.Application;
import com.incognito.acejam0.domain.Action;
import com.incognito.acejam0.domain.ActionInfo;
import com.incognito.acejam0.domain.Level;
import com.incognito.acejam0.states.common.BackgroundRendererState;
import com.incognito.acejam0.states.common.BackgroundRendererState.BgState;
import com.incognito.acejam0.states.common.CameraControlsState;
import com.incognito.acejam0.states.common.TypedBaseAppState;
import com.incognito.acejam0.states.game.GameState;
import com.incognito.acejam0.states.game.MapRendererState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.input.InputManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;

import java.time.Duration;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainMenuState extends TypedBaseAppState<Application> {

    private InputManager inputManager;
    private Node guiNode;
    private MenuList menu;
    private long titleAnim = 0;
    private boolean titlePlaying = false;
    private int titlePos = 0;
    private final List<Action> titleActions = new ArrayList<>();

    private final BgState initialState;

    public MainMenuState() {
        this(BgState.MENU);
    }

    public MainMenuState(BgState initialState) {
        this.initialState = initialState;
    }

    @Override
    protected void onInitialize(Application app) {
        inputManager = app.getInputManager();
        guiNode = app.getGuiNode();

        AppSettings settings = app.getContext().getSettings();
        AppStateManager appStateManager = app.getStateManager();

        appStateManager.getState(BackgroundRendererState.class).setBackgroundState(initialState, 1.0f);

        menu = new MenuList(app.getFontOutline(), ColorRGBA.LightGray, ColorRGBA.DarkGray, 50f, BitmapFont.Align.Center, Arrays.asList(
                new AbstractMap.SimpleEntry<>(GameState.inProgress() ? "CONTINUE" : "START", () -> {
                    appStateManager.detach(this);
                    appStateManager.attach(new GameState());
                }),
                new AbstractMap.SimpleEntry<>("OPTIONS", () -> {
                    appStateManager.detach(this);
                    appStateManager.attach(new OptionsState(() -> {
                        appStateManager.attach(new MainMenuState(initialState));
                    }));
                }),
                new AbstractMap.SimpleEntry<>("EXIT", app::stop)));

        menu.setLocalTranslation(settings.getWidth() / 2f - menu.getWidth() / 2f, settings.getHeight() / 2f + menu.getHeight() / 2f, 0);
        menu.initialize(inputManager);
        guiNode.attachChild(menu);

        Level title = Level.loadLevel("title");
        appStateManager.attach(new MapRendererState(title, false));
        appStateManager.getState(CameraControlsState.class).setZoom(60f);

        Camera cam = app.getCamera();
        cam.setLocation(new Vector3f(title.getWidth() / 2.0f - 0.5f, -title.getHeight(), cam.getLocation().z));

        for (int i = 0; i < title.getWidth(); i++) {
            List<ActionInfo> column = new ArrayList<>();
            for (int y = 10; y <= 20; y++) {
                column.add(new ActionInfo(i, y, false, 2, null, 0));
            }
            titleActions.add(new Action(column));
        }
        titleAnim = System.nanoTime();
    }

    @Override
    protected void onCleanup(Application app) {
        guiNode.detachChild(menu);
        menu.cleanup(inputManager);
        AppStateManager stateManager = app.getStateManager();
        stateManager.detach(stateManager.getState(MapRendererState.class));
        stateManager.getState(CameraControlsState.class).setZoom(10f);
    }

    @Override
    public void update(float tpf) {
        menu.update();

        long now = System.nanoTime();
        Duration timeSince = Duration.ofNanos(now - titleAnim);
        if (!titlePlaying && !timeSince.minusSeconds(3).isNegative()) {
            titlePlaying = true;
            titleAnim = System.nanoTime();
        } else if (titlePlaying && !timeSince.minusMillis(75).isNegative()) {
            getApplication().getStateManager().getState(MapRendererState.class).update(titleActions.get(titlePos));
            titleAnim = System.nanoTime();
            titlePos++;
            if (titlePos >= titleActions.size()) {
                titlePlaying = false;
                titlePos = 0;
            }
        }
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
