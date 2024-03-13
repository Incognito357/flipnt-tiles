package com.incognito.acejam0.states.common;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.renderer.RenderManager;
import com.jme3.system.AppSettings;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class TypedBaseAppState<T extends Application> extends BaseAppState {
    private static final Map<TypedBaseAppState<?>, Consumer<AppSettings>> resizeListeners = new HashMap<>();

    @Override
    protected final void initialize(Application app) {
        onInitialize((T) app);
    }

    @Override
    protected final void cleanup(Application app) {
        onCleanup((T) app);
        resizeListeners.remove(this);
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    protected abstract void onInitialize(T app);
    protected abstract void onCleanup(T app);

    protected final void addResizeListener(Consumer<AppSettings> listener) {
        resizeListeners.put(this, listener);
    }

    public static void onResize(AppSettings settings) {
        resizeListeners.values().forEach(l -> l.accept(settings));
    }

}
