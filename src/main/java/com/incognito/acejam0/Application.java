package com.incognito.acejam0;

import com.incognito.acejam0.utils.Builder;
import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.style.BaseStyles;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Application extends SimpleApplication {

    public static void main(String[] args) {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        Logger.getGlobal().setLevel(Level.FINEST);

        SimpleApplication app = new Application();
        app.setSettings(new Builder<>(new AppSettings(true))
                .with(AppSettings::setTitle, "Acerola Jam 0 - Aberration")
                .with(AppSettings::setResolution, 1440, 810)    // TODO: get from args or config file
                .build());
        app.setDisplayFps(false);
        app.setDisplayStatView(false);

        app.start();
    }

    @Override
    public void simpleInitApp() {
        GuiGlobals.initialize(this);
        GuiGlobals.getInstance().setCursorEventsEnabled(false);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");
    }
}
