package com.incognito.acejam0.utils;

import com.incognito.acejam0.Application;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;

public class GuiText {
    private GuiText() {}

    public static BitmapText makeText(String text, ColorRGBA color, float size) {
        BitmapText node = new BitmapText(Application.APP.getGuiFont());
        node.setText(text);
        node.setColor(color);
        node.setSize(size);

        return node;
    }
}
