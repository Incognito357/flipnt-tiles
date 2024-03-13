package com.incognito.acejam0.states.menu;

import com.incognito.acejam0.Application;
import com.incognito.acejam0.states.common.TypedBaseAppState;
import com.incognito.acejam0.utils.AudioUtil;
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
import com.simsilica.lemur.Label;
import com.simsilica.lemur.Slider;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.core.VersionedObject;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class OptionsState extends TypedBaseAppState<Application> {

    private final Runnable onComplete;

    private Node guiNode;
    private Container options;

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

    private final Set<VersionListener<?, ?>> versionListeners = new HashSet<>();

    public OptionsState(Runnable onComplete) {
        this.onComplete = onComplete;
    }

    @Override
    protected void onInitialize(Application app) {
        guiNode = app.getGuiNode();

        AudioUtil.AudioState bgmState = AudioUtil.getBgmState();
        AudioUtil.AudioState pingState = AudioUtil.getPingState();

        Checkbox bgmMute = new Checkbox("Mute", new DefaultCheckboxModel(bgmState.isMuted()));
        Checkbox pingMute = new Checkbox("Mute", new DefaultCheckboxModel(pingState.isMuted()));
        Slider bgmVolume = new Slider(new DefaultRangedValueModel(0.0, 1.0, bgmState.getVolume()), Axis.X);
        Slider pingVolume = new Slider(new DefaultRangedValueModel(0.0, 1.0, pingState.getVolume()), Axis.X);
        bgmVolume.setDelta(0.1f);
        pingVolume.setDelta(0.1f);

        versionListeners.add(new VersionListener<>(bgmMute.getModel(), m -> bgmState.toggleMute()));
        versionListeners.add(new VersionListener<>(pingMute.getModel(), m -> pingState.toggleMute()));
        versionListeners.add(new VersionListener<>(bgmVolume.getModel(), m -> bgmState.setVolume((float) m.getValue())));
        versionListeners.add(new VersionListener<>(pingVolume.getModel(), m -> pingState.setVolume((float) m.getValue())));

        AppSettings settings = app.getContext().getSettings();

        options = new Container(new SpringGridLayout());
        Container musicPanel = new Container(new SpringGridLayout(Axis.X, Axis.Y, FillMode.Last, FillMode.Even));
        musicPanel.addChild(new Label("Music"));
        musicPanel.addChild(bgmMute);
        musicPanel.addChild(bgmVolume);

        Container pingPanel = new Container(new SpringGridLayout(Axis.X, Axis.Y, FillMode.Last, FillMode.Even));
        pingPanel.addChild(new Label("Sounds"));
        pingPanel.addChild(pingMute);
        pingPanel.addChild(pingVolume);

        Button btnBack = new Button("Back");
        btnBack.addClickCommand(btn -> app.getStateManager().detach(this));

        options.addChild(musicPanel);
        options.addChild(pingPanel);
        options.addChild(btnBack);

        Vector3f size = new Vector3f(settings.getWidth() / 2f, 100f, 0f);
        options.setPreferredSize(size);
        options.setLocalTranslation(settings.getWidth() / 2f - size.x / 2f, settings.getHeight() / 2f + size.y / 2f, 0);

        guiNode.attachChild(options);
        GuiGlobals.getInstance().requestCursorEnabled(options);
    }

    @Override
    protected void onCleanup(Application app) {
        guiNode.detachChild(options);
        GuiGlobals.getInstance().releaseCursorEnabled(options);
        onComplete.run();
    }

    @Override
    public void update(float tpf) {
        versionListeners.forEach(VersionListener::checkUpdate);
    }
}
