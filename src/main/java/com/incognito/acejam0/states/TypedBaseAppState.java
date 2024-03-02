package com.incognito.acejam0.states;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;

public abstract class TypedBaseAppState<T extends Application> extends BaseAppState {

    @Override
    protected final void initialize(Application app) {
        onInitialize((T) app);
    }

    @Override
    protected final void cleanup(Application app) {
        onCleanup((T) app);
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    protected abstract void onInitialize(T app);
    protected abstract void onCleanup(T app);
}
