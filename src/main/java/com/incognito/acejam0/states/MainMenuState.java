package com.incognito.acejam0.states;

import com.incognito.acejam0.Application;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.IntStream;

public class MainMenuState extends TypedBaseAppState<Application> {

    private static final Logger logger = LogManager.getLogger();

    private InputManager inputManager;
    private BitmapFont guiFont;
    private Node guiNode;
    private final Node menuNode = new Node();

    private int selectedOption = 0;

    private List<BitmapText> options;

    private final ActionListener upListener = (name, isPressed, tpf) -> {
        if (isPressed) {
            options.get(selectedOption).setColor(ColorRGBA.DarkGray);
            selectedOption--;
            if (selectedOption < 0) {
                selectedOption = options.size() - 1;
            }
        }
    };

    private final ActionListener downListener = (name, isPressed, tpf) -> {
        if (isPressed) {
            options.get(selectedOption).setColor(ColorRGBA.DarkGray);
            selectedOption = (selectedOption + 1) % options.size();
        }
    };

    @Override
    protected void onInitialize(Application app) {
        inputManager = app.getInputManager();
        guiFont = app.getGuiFont();
        guiNode = app.getGuiNode();

        AppSettings settings = app.getContext().getSettings();

        options = List.of(
                makeText("START"),
                makeText("OPTIONS"),
                makeText("EXIT"));

        IntStream.range(0, options.size())
                .forEach(i -> {
                    options.get(i).setLocalTranslation(-options.get(i).getLineWidth() / 2f, -i * options.get(i).getLineHeight() + 5, 0);
                    menuNode.attachChild(options.get(i));
                });
        guiNode.setLocalTranslation(settings.getWidth() / 2f, settings.getHeight() / 2f + (options.size() * options.get(0).getLineHeight() + 5f) / 2f, 0);

        guiNode.attachChild(menuNode);

        inputManager.addMapping("menu-up", new KeyTrigger(KeyInput.KEY_UP), new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("menu-down", new KeyTrigger(KeyInput.KEY_DOWN), new KeyTrigger(KeyInput.KEY_S));
        inputManager.addListener(upListener, "menu-up");
        inputManager.addListener(downListener, "menu-down");
    }

    @Override
    protected void onCleanup(Application app) {
        guiNode.detachChild(menuNode);

        inputManager.removeListener(upListener);
        inputManager.removeListener(downListener);
        inputManager.deleteMapping("menu-up");
        inputManager.deleteMapping("menu-down");

    }

    @Override
    public void update(float tpf) {
        options.get(selectedOption).setColor(ColorRGBA.LightGray.mult(FastMath.interpolateLinear(
                (FastMath.sin(System.nanoTime() * 0.000000004f) + 1f) / 2f, 0.5f, 1f)));
    }

    private BitmapText makeText(String message) {
        BitmapText text = new BitmapText(guiFont);
        text.setSize(50f);
        text.setColor(ColorRGBA.DarkGray);
        text.setText(message);

        return text;
    }
}
