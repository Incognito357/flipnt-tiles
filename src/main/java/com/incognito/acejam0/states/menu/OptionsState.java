package com.incognito.acejam0.states.menu;

import com.incognito.acejam0.Application;
import com.incognito.acejam0.states.common.TypedBaseAppState;
import com.incognito.acejam0.utils.AudioUtil;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.Axis;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Checkbox;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.DefaultCheckboxModel;
import com.simsilica.lemur.DefaultRangedValueModel;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.Slider;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.VAlignment;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.core.VersionedObject;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class OptionsState extends TypedBaseAppState<Application> {

    private final Runnable onComplete;
    private final Set<VersionListener<?, ?>> versionListeners = new HashSet<>();

    private InputManager inputManager;
    private Node guiNode;
    private Container options;

    private final ActionListener closeListener = (name, isPressed, tpf) -> {
        if (isPressed) {
            getApplication().getStateManager().detach(this);
        }
    };

    private static class VersionListener<V, T extends VersionedObject<V>> {
        private long version;
        private final T model;
        private final Consumer<T> onChange;

        private VersionListener(T model, Consumer<T> onChange) {
            this.version = model.getVersion();
            this.model = model;
            this.onChange = onChange;
        }

        private void checkUpdate() {
            long cur = model.getVersion();
            if (cur != version) {
                onChange.accept(model);
                version = cur;
            }
        }
    }

    public OptionsState(Runnable onComplete) {
        this.onComplete = onComplete;
    }

    @Override
    protected void onInitialize(Application app) {
        inputManager = app.getInputManager();
        guiNode = app.getGuiNode();

        AudioUtil.AudioState bgmState = AudioUtil.getBgmState();
        AudioUtil.AudioState pingState = AudioUtil.getPingState();

        Label lblMusic = new Label("Music");
        lblMusic.setFontSize(20f);
        lblMusic.setTextVAlignment(VAlignment.Center);

        Label lblSounds = new Label("Sounds");
        lblSounds.setFontSize(20f);
        lblSounds.setTextVAlignment(VAlignment.Center);
        Checkbox bgmMute = new Checkbox("Mute", new DefaultCheckboxModel(bgmState.isMuted()));
        Checkbox pingMute = new Checkbox("Mute", new DefaultCheckboxModel(pingState.isMuted()));
        Slider bgmVolume = new Slider(new DefaultRangedValueModel(0.0, 1.0, bgmState.getVolume()), Axis.X);
        Slider pingVolume = new Slider(new DefaultRangedValueModel(0.0, 1.0, pingState.getVolume()), Axis.X);
        bgmVolume.setDelta(0.1f);
        pingVolume.setDelta(0.1f);
        pingVolume.getIncrementButton().addClickCommand(btn -> AudioUtil.playPing(Vector3f.ZERO));
        pingVolume.getDecrementButton().addClickCommand(btn -> AudioUtil.playPing(Vector3f.ZERO));
        pingVolume.getThumbButton().addClickCommand(btn -> AudioUtil.playPing(Vector3f.ZERO));

        versionListeners.add(new VersionListener<>(bgmMute.getModel(), m -> bgmState.toggleMute()));
        versionListeners.add(new VersionListener<>(pingMute.getModel(), m -> pingState.toggleMute()));
        versionListeners.add(new VersionListener<>(bgmVolume.getModel(), m -> bgmState.setVolume((float) m.getValue())));
        versionListeners.add(new VersionListener<>(pingVolume.getModel(), m -> pingState.setVolume((float) m.getValue())));

        options = new Container(new SpringGridLayout(Axis.Y, Axis.X, FillMode.Even, FillMode.Last));
        options.addChild(lblMusic);
        options.addChild(bgmMute, 1);
        options.addChild(bgmVolume, 2);

        options.addChild(lblSounds);
        options.addChild(pingMute, 1);
        options.addChild(pingVolume, 2);

        AppSettings settings = app.getContext().getSettings();
        Label lblRes = new Label("Resolution");
        lblRes.setFontSize(20f);
        Label lblX = new Label("X");
        lblX.setFontSize(20f);
        lblX.setTextVAlignment(VAlignment.Center);
        TextField txtX = new TextField(String.valueOf(settings.getWidth()));

        Label lblY = new Label("Y");
        lblY.setFontSize(20f);
        lblY.setTextVAlignment(VAlignment.Center);
        TextField txtY = new TextField(String.valueOf(settings.getHeight()));

        Checkbox chkFull = new Checkbox("Fullscreen");
        chkFull.setChecked(settings.isFullscreen());

        Button btnApply = new Button("Apply");
        btnApply.setFontSize(20f);
        btnApply.setTextHAlignment(HAlignment.Center);
        btnApply.setTextVAlignment(VAlignment.Center);
        btnApply.addClickCommand(btn -> {
            int x;
            int y;
            try {
                x = Integer.parseUnsignedInt(txtX.getText());
            } catch (NumberFormatException e) {
                txtX.setText(String.valueOf(settings.getWidth()));
                return;
            }
            try {
                y = Integer.parseUnsignedInt(txtY.getText());
            } catch (NumberFormatException e) {
                txtY.setText(String.valueOf(settings.getHeight()));
                return;
            }
            boolean f = chkFull.isChecked();
            if (settings.getWidth() != x || settings.getHeight() != y || settings.isFullscreen() != f) {
                settings.setResolution(x, y);
                settings.setWindowSize(x, y);
                settings.setFullscreen(f);
                app.restart();
                onResize(settings);
            }
        });

        options.addChild(lblRes);
        options.addChild(lblX);
        options.addChild(txtX, 1);
        options.addChild(lblY);
        options.addChild(txtY, 1);
        options.addChild(chkFull);
        options.addChild(btnApply);

        Button btnBack = new Button("Back");
        btnBack.setFontSize(20f);
        btnBack.setTextHAlignment(HAlignment.Center);
        btnBack.setTextVAlignment(VAlignment.Center);
        btnBack.addClickCommand(btn -> app.getStateManager().detach(this));
        options.addChild(btnBack);

        resizeMenu(settings);

        guiNode.attachChild(options);
        GuiGlobals.getInstance().requestCursorEnabled(options);

        inputManager.addMapping("options-close", new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addListener(closeListener, "options-close");

        addResizeListener(s -> {
            resizeMenu(s);
            txtX.setText(String.valueOf(s.getWidth()));
            txtY.setText(String.valueOf(s.getHeight()));
            chkFull.setChecked(s.isFullscreen());
        });
    }

    private void resizeMenu(AppSettings settings) {
        Vector3f size = new Vector3f(Math.max(settings.getWidth() / 2f, 100), Math.max(settings.getHeight() / 3f, 200), 0f);
        options.setPreferredSize(size);
        options.setLocalTranslation(settings.getWidth() / 2f - size.x / 2f, settings.getHeight() / 2f + size.y / 2f, 0);
    }

    @Override
    protected void onCleanup(Application app) {
        guiNode.detachChild(options);
        GuiGlobals.getInstance().releaseCursorEnabled(options);
        inputManager.removeListener(closeListener);
        inputManager.deleteMapping("options-close");

        onComplete.run();
    }

    @Override
    public void update(float tpf) {
        versionListeners.forEach(VersionListener::checkUpdate);
    }
}
