package com.incognito.acejam0;

import com.incognito.acejam0.domain.Level;
import com.incognito.acejam0.states.BackgroundRendererState;
import com.incognito.acejam0.states.CameraControlsState;
import com.incognito.acejam0.states.MapEditorState;
import com.incognito.acejam0.states.MapRendererState;
import com.incognito.acejam0.utils.Builder;
import com.jme3.app.SimpleApplication;
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

    public static void main(String[] args) {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        Logger.getGlobal().setLevel(java.util.logging.Level.FINEST);

        Application app = new Application();
        app.setSettings(new Builder<>(new AppSettings(true))
                .with(AppSettings::setTitle, "Acerola Jam 0 - Aberration")
                .with(AppSettings::setResolution, 1440, 810)    // TODO: get from args or config file
                .build());
        app.getStateManager().attachAll(new BackgroundRendererState(), new AnimationState(), new CameraControlsState());
        app.setDisplayFps(false);
        app.setDisplayStatView(false);

        app.getStateManager().attachAll(new MapEditorState(), new MapRendererState(new Level("", 0, 0, List.of(), List.of(), new BitSet(), Map.of())));

        APP = app;

        app.start();
    }

    @Override
    public void simpleInitApp() {
        GuiGlobals.initialize(this);
        GuiGlobals.getInstance().setCursorEventsEnabled(false);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        flyCam.setEnabled(false);
        cam.setParallelProjection(true);
    }
}
