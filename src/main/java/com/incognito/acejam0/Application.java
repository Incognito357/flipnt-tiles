package com.incognito.acejam0;

import com.incognito.acejam0.domain.Level;
import com.incognito.acejam0.states.common.BgmState;
import com.incognito.acejam0.states.common.BackgroundRendererState;
import com.incognito.acejam0.states.common.BackgroundRendererState.BgState;
import com.incognito.acejam0.states.common.CameraControlsState;
import com.incognito.acejam0.states.game.MapEditorState;
import com.incognito.acejam0.states.game.MapRendererState;
import com.incognito.acejam0.states.menu.MainMenuState;
import com.incognito.acejam0.utils.AudioUtil;
import com.incognito.acejam0.utils.Builder;
import com.jme3.app.DebugKeysAppState;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.anim.AnimationState;
import com.simsilica.lemur.style.BaseStyles;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.HyperlinkEvent;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

public class Application extends SimpleApplication {
    private static final Logger logger = LogManager.getLogger();
    public static Application APP;
    private BitmapFont font;
    private BitmapFont fontOutline;
    private static boolean EDIT_MODE;

    public static void main(String[] args) {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        java.util.logging.Logger.getGlobal().setLevel(java.util.logging.Level.FINEST);

        Application app = new Application();

        String resXProp = System.getProperty("res.x", "1440");
        String resYProp = System.getProperty("res.y", "810");
        String resFullProp = System.getProperty("res.full", "false");
        String editProp = System.getProperty("editor", "false");

        int resX;
        int resY;
        boolean resFull = Boolean.parseBoolean(resFullProp);
        EDIT_MODE = Boolean.parseBoolean(editProp);
        try {
            resX = Integer.parseUnsignedInt(resXProp);
            resY = Integer.parseUnsignedInt(resYProp);
        } catch (NumberFormatException e) {
            logger.error("Invalid resolution ({}, {})", resXProp, resYProp);
            resX = 1440;
            resY = 810;
        }

        app.setSettings(new Builder<>(new AppSettings(true))
                .with(AppSettings::setTitle, "Acerola Jam 0 - Aberration")
                .with(AppSettings::setResolution, resX, resY)
                .with(AppSettings::setFullscreen, resFull)
                .build());
        app.getStateManager().attachAll(new BackgroundRendererState(BgState.MENU), new AnimationState(), new CameraControlsState(), new BgmState());
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
        font = assetManager.loadFont("font/book_antiqua.fnt");
        fontOutline = assetManager.loadFont("font/book_antiqua_outline.fnt");

        GuiGlobals.initialize(this);
        GuiGlobals.getInstance().setCursorEventsEnabled(false);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        cam.setParallelProjection(true);

        stateManager.detach(stateManager.getState(FlyCamAppState.class));
        stateManager.detach(stateManager.getState(DebugKeysAppState.class));
        inputManager.deleteMapping(INPUT_MAPPING_EXIT);

        AudioUtil.initTracks();
    }

    @Override
    public void handleError(String errMsg, Throwable t) {
        logger.error(errMsg, t);
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));

        JEditorPane label = new JEditorPane("text/html", "<html><body><div align='left'>" +
                "Please consider submitting a bug report " +
                "<a href=\"https://github.com/Incognito357/acerola-jam-0/issues/new\">here</a>, and " +
                "add a brief description of what you were doing when this happened.</div>" +
                "<div>If a bug has already been submitted, consider adding additional details " +
                "in the comments, instead of opening a duplicate bug report.</div></body></html>");
        label.addHyperlinkListener(e -> {
            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED) && Desktop.isDesktopSupported()) {
                try {
                    URI uri = e.getURL().toURI();
                    String params = "title=Crash Report - " + t.getMessage() +
                            "&body=[Add description here]\n" +
                            "```\n" + sw + "```" +
                            "&labels=bug";
                    Desktop.getDesktop().browse(new URI(
                            uri.getScheme(),
                            uri.getAuthority(),
                            uri.getPath(),
                            params,
                            uri.getFragment()));
                } catch (IOException | URISyntaxException ex) {
                    logger.error("Could not open browser", ex);
                }
            }
        });
        label.setEditable(false);
        label.setBorder(null);
        label.setBackground(new JLabel().getBackground());
        JTextArea err = new JTextArea(errMsg + "\n" + sw);
        err.setEditable(false);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.add(label, BorderLayout.NORTH);
        panel.add(new JScrollPane(err), BorderLayout.CENTER);

        JOptionPane.showMessageDialog(null, panel, "Well, that's not supposed to happen...", JOptionPane.ERROR_MESSAGE);
    }

    public BitmapFont getGuiFont() {
        return font;
    }

    public BitmapFont getFontOutline() {
        return fontOutline;
    }
}
