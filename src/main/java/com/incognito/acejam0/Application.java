package com.incognito.acejam0;

import com.incognito.acejam0.domain.Level;
import com.incognito.acejam0.states.common.BackgroundRendererState;
import com.incognito.acejam0.states.common.BackgroundRendererState.BgState;
import com.incognito.acejam0.states.common.CameraControlsState;
import com.incognito.acejam0.states.game.MapEditorState;
import com.incognito.acejam0.states.game.MapRendererState;
import com.incognito.acejam0.states.menu.MainMenuState;
import com.incognito.acejam0.utils.Builder;
import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.anim.AnimationState;
import com.simsilica.lemur.style.BaseStyles;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Application extends SimpleApplication {
    public static Application APP;

    private static final boolean EDIT_MODE = true;

    private BitmapFont font;

    public static void main(String[] args) {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        Logger.getGlobal().setLevel(java.util.logging.Level.FINEST);

        Application app = new Application();

        app.setSettings(new Builder<>(new AppSettings(true))
                .with(AppSettings::setTitle, "Acerola Jam 0 - Aberration")
                .with(AppSettings::setResolution, 1440, 810)    // TODO: get from args or config file
                .build());
        app.getStateManager().attachAll(new BackgroundRendererState(BgState.MENU), new AnimationState(), new CameraControlsState());
        app.setDisplayFps(false);
        app.setDisplayStatView(false);

        if (!EDIT_MODE) {
            app.getStateManager().attach(new MainMenuState());
        } else {
            app.getStateManager().attachAll(
                    new MapEditorState(),
                    new MapRendererState(new Level("", 0, 0, List.of(), List.of(), new BitSet(), Map.of(), Map.of())));
        }

        APP = app;

        app.start();
    }

    @Override
    public void simpleInitApp() {
        font = getAssetManager().loadFont("font/book_antiqua.fnt");

        GuiGlobals.initialize(this);
        GuiGlobals.getInstance().setCursorEventsEnabled(false);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        flyCam.setEnabled(false);
        cam.setParallelProjection(true);

        if (!EDIT_MODE) {
            getInputManager().deleteMapping(INPUT_MAPPING_EXIT);
        }
    }

    public BitmapFont getGuiFont() {
        return font;
    }
}
