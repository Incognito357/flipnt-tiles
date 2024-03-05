package com.incognito.acejam0.states;

import com.incognito.acejam0.Application;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.renderer.Camera;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CameraControlsState extends TypedBaseAppState<Application> {
    private static final Logger logger = LogManager.getLogger();

    private Camera camera;
    private InputManager inputManager;
    private float scale = 10f;
    private Vector4f frustum;
    private boolean panning = false;
    private Vector2f cursor = Vector2f.ZERO.clone();
    private Vector3f camOrigin = Vector3f.ZERO.clone();

    private final AnalogListener zoomIn = (name, value, tpf) -> zoom(-value);

    private final AnalogListener zoomOut = (name, value, tpf) -> zoom(value);

    private final ActionListener panDrag = (name, isPressed, tpf) -> {
        if (!panning && isPressed) {
            cursor = inputManager.getCursorPosition().clone();
            camOrigin = camera.getLocation().clone();
        } else if (panning && !isPressed) {
            cursor = Vector2f.ZERO.clone();
            camOrigin = Vector3f.ZERO.clone();
        }
        panning = isPressed;
        //inputManager.setCursorVisible(!panning);
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

        inputManager.addMapping("zoomIn", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping("zoomOut", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        inputManager.addMapping("panDrag", new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));
        inputManager.addMapping("pan",
                new MouseAxisTrigger(MouseInput.AXIS_X, true),
                new MouseAxisTrigger(MouseInput.AXIS_X, false),
                new MouseAxisTrigger(MouseInput.AXIS_Y, true),
                new MouseAxisTrigger(MouseInput.AXIS_Y, false));

        inputManager.addListener(zoomIn, "zoomIn");
        inputManager.addListener(zoomOut, "zoomOut");
        inputManager.addListener(panDrag, "panDrag");
        inputManager.addListener(pan, "pan");

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
