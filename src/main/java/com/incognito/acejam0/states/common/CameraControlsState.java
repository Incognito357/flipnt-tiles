package com.incognito.acejam0.states.common;

import com.incognito.acejam0.Application;
import com.incognito.acejam0.states.game.MapEditorState;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.renderer.Camera;

public class CameraControlsState extends TypedBaseAppState<Application> {

    private Camera camera;
    private InputManager inputManager;
    private float scale = 10f;
    private Vector4f frustum;
    private final AnalogListener zoomIn = (name, value, tpf) -> {
        if (value == tpf) {
            zoom(tpf * -50f);
        } else {
            zoom(-value);
        }
    };
    private final AnalogListener zoomOut = (name, value, tpf) -> {
        if (value == tpf) {
            zoom(tpf * 50f);
        } else {
            zoom(value);
        }
    };
    private boolean panning = false;
    private boolean altPressed = false;
    private Vector2f cursor = Vector2f.ZERO.clone();
    private Vector3f camOrigin = Vector3f.ZERO.clone();
    private final ActionListener panDrag = (name, isPressed, tpf) -> {
        boolean drag = false;
        if ("panDrag".equals(name)) {
            drag = isPressed;
        } else if ("panDragRightClickAlt".equals(name)) {
            altPressed = isPressed;
        } else if ("panDragRightClick".equals(name)) {
            drag = isPressed && altPressed;
        }
        if (!panning && drag) {
            cursor = inputManager.getCursorPosition().clone();
            camOrigin = camera.getLocation().clone();
        } else if (panning && !drag) {
            cursor = Vector2f.ZERO.clone();
            camOrigin = Vector3f.ZERO.clone();
        }
        panning = drag;

        MapEditorState state = getApplication().getStateManager().getState(MapEditorState.class);
        if (state != null) {
            state.setEnabled(!panning);
        }
    };

    private final AnalogListener pan = (name, value, tpf) -> {
        if (panning) {
            float depth = camera.getViewToProjectionZ(camera.getLocation().z);
            Vector2f currentCursor = inputManager.getCursorPosition();
            Vector3f originWorld = camera.getWorldCoordinates(cursor, depth);
            Vector3f currentWorld = camera.getWorldCoordinates(currentCursor, depth);
            Vector3f dif = originWorld.subtract(currentWorld);
            camera.setLocation(camOrigin.add(new Vector3f(dif.x, dif.y, 0)));
        }
    };

    private void zoom(float value) {
        scale += value;
        if (scale < 5) {
            scale = 5;
        } else if (scale > 100) {
            scale = 100;
        }
        updateFov();
    }

    @Override
    protected void onInitialize(Application app) {
        camera = app.getCamera();
        inputManager = app.getInputManager();

        inputManager.addMapping("zoomIn", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false),
                new KeyTrigger(KeyInput.KEY_EQUALS), new KeyTrigger(KeyInput.KEY_ADD));
        inputManager.addMapping("zoomOut", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true),
                new KeyTrigger(KeyInput.KEY_MINUS), new KeyTrigger(KeyInput.KEY_SUBTRACT));
        inputManager.addMapping("panDrag", new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));
        inputManager.addMapping("pan",
                new MouseAxisTrigger(MouseInput.AXIS_X, true),
                new MouseAxisTrigger(MouseInput.AXIS_X, false),
                new MouseAxisTrigger(MouseInput.AXIS_Y, true),
                new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addMapping("panDragRightClick", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addMapping("panDragRightClickAlt", new KeyTrigger(KeyInput.KEY_LMENU));

        inputManager.addListener(zoomIn, "zoomIn");
        inputManager.addListener(zoomOut, "zoomOut");
        inputManager.addListener(panDrag, "panDrag");
        inputManager.addListener(pan, "pan");
        inputManager.addListener(panDrag, "panDragRightClick");
        inputManager.addListener(panDrag, "panDragRightClickAlt");

        frustum = new Vector4f(camera.getFrustumLeft(), camera.getFrustumTop(), camera.getFrustumRight(), camera.getFrustumBottom());

        updateFov();
    }

    private void updateFov() {
        camera.setFrustumLeft(frustum.x * scale);
        camera.setFrustumTop(frustum.y * scale);
        camera.setFrustumRight(frustum.z * scale);
        camera.setFrustumBottom(frustum.w * scale);
    }

    @Override
    protected void onCleanup(Application app) {

    }
}
