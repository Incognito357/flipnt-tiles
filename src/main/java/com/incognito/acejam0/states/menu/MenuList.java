package com.incognito.acejam0.states.menu;

import com.jme3.font.BitmapFont;
import com.jme3.font.Rectangle;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class MenuList extends Node {

    private final List<Map.Entry<MenuItem, Runnable>> items = new ArrayList<>();
    private int selected = 0;
    private final float width;
    private final float height;
    private final ActionListener selectListener = (name, isPressed, tpf) -> {
        if (isPressed) {
            items.get(selected).getValue().run();
        }
    };

    private final ActionListener upListener = (name, isPressed, tpf) -> {
        if (isPressed) {
            items.get(selected).getKey().setSelected(false);
            selected--;
            if (selected < 0) {
                selected = items.size() - 1;
            }
            items.get(selected).getKey().setSelected(true);
        }
    };
    private final ActionListener downListener = (name, isPressed, tpf) -> {
        if (isPressed) {
            items.get(selected).getKey().setSelected(false);
            selected = (selected + 1) % items.size();
            items.get(selected).getKey().setSelected(true);
        }
    };

    public MenuList(
            BitmapFont guiFont,
            ColorRGBA activeColor,
            ColorRGBA inactiveColor,
            float size,
            BitmapFont.Align align,
            List<Map.Entry<String, Runnable>> items) {

        AtomicReference<Float> maxWidth = new AtomicReference<>(0f);
        this.items.addAll(items.stream()
                .map(kvp -> {
                    MenuItem item = new MenuItem(guiFont, kvp.getKey(), activeColor, inactiveColor, size);
                    maxWidth.accumulateAndGet(item.getLineWidth(), Float::max);
                    attachChild(item);
                    return new AbstractMap.SimpleEntry<>(item, kvp.getValue());
                })
                .collect(Collectors.toList()));
        this.width = maxWidth.get();
        this.height = items.size() * (this.items.get(0).getKey().getLineHeight() + 5f);
        List<Map.Entry<MenuItem, Runnable>> entries = this.items;
        for (int i = 0; i < entries.size(); i++) {
            MenuItem item = entries.get(i).getKey();
            item.setBox(new Rectangle(0, -i * (item.getLineHeight() + 5f), this.width, item.getLineHeight()));
            item.setAlignment(align);
            if (i == selected) {
                item.setSelected(true);
            }
        }
    }

    public void initialize(InputManager inputManager) {
        inputManager.addMapping("menu-up", new KeyTrigger(KeyInput.KEY_UP), new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("menu-down", new KeyTrigger(KeyInput.KEY_DOWN), new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("menu-select", new KeyTrigger(KeyInput.KEY_RETURN), new KeyTrigger(KeyInput.KEY_SPACE));
    }

    public void cleanup(InputManager inputManager) {
        inputManager.removeListener(upListener);
        inputManager.removeListener(downListener);
        inputManager.removeListener(selectListener);
        inputManager.deleteMapping("menu-up");
        inputManager.deleteMapping("menu-down");
        inputManager.deleteMapping("menu-select");
    }

    public void onEnable(InputManager inputManager) {
        items.get(selected).getKey().setSelected(false);
        selected = 0;
        items.get(selected).getKey().setSelected(true);
        inputManager.addListener(upListener, "menu-up");
        inputManager.addListener(downListener, "menu-down");
        inputManager.addListener(selectListener, "menu-select");
    }

    public void onDisable(InputManager inputManager) {
        inputManager.removeListener(upListener);
        inputManager.removeListener(downListener);
        inputManager.removeListener(selectListener);
    }


    public void update() {
        items.get(selected).getKey().update();
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
