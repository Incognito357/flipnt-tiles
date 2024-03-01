package com.incognito.acejam0;

import com.incognito.acejam0.domain.Level;
import com.incognito.acejam0.states.MapRendererState;
import com.incognito.acejam0.utils.Builder;
import com.incognito.acejam0.utils.Mapper;
import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.anim.AnimationState;
import com.simsilica.lemur.style.BaseStyles;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

public class Application extends SimpleApplication {

    public static void main(String[] args) {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        Logger.getGlobal().setLevel(java.util.logging.Level.FINEST);

        SimpleApplication app = new Application();
        app.setSettings(new Builder<>(new AppSettings(true))
                .with(AppSettings::setTitle, "Acerola Jam 0 - Aberration")
                .with(AppSettings::setResolution, 1440, 810)    // TODO: get from args or config file
                .build());
        app.getStateManager().attach(new AnimationState());
        app.setDisplayFps(false);
        app.setDisplayStatView(false);

        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("levels/test.json")) {
            app.getStateManager().attach(new MapRendererState(Mapper.getMapper().readValue(in, Level.class)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        app.start();
    }

    @Override
    public void simpleInitApp() {
        GuiGlobals.initialize(this);
        GuiGlobals.getInstance().setCursorEventsEnabled(false);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        flyCam.setEnabled(false);
    }
}
