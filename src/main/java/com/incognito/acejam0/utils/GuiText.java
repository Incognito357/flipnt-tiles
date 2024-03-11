package com.incognito.acejam0.utils;

import com.incognito.acejam0.Application;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;

public class GuiText {
    private GuiText() {}

    public static BitmapText makeText(String text, ColorRGBA color, float size) {
        return make(text, color, size, Application.APP.getGuiFont());
    }

    public static BitmapText makeOutlineText(String text, ColorRGBA color, float size) {
        return make(text, color, size, Application.APP.getFontOutline());
    }

    private static BitmapText make(String text, ColorRGBA color, float size, BitmapFont font) {
        BitmapText node = new BitmapText(font);
        node.setText(text);
        node.setColor(color);
        node.setSize(size);

        return node;
    }
}
