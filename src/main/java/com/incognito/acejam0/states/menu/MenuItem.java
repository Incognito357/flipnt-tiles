package com.incognito.acejam0.states.menu;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;

public class MenuItem extends BitmapText {

    private boolean selected = false;
    private final ColorRGBA activeColor;
    private final ColorRGBA inactiveColor;
    private long selectionStart = 0;

    public MenuItem(BitmapFont font, String text, ColorRGBA activeColor, ColorRGBA inactiveColor, float size) {
        super(font);
        this.activeColor = activeColor;
        this.inactiveColor = inactiveColor;
        setColor(inactiveColor);
        setText(text);
        setSize(size);
    }

    public void update() {
        if (selected) {
            float v = FastMath.interpolateLinear(
                    (FastMath.cos((System.nanoTime() - selectionStart) * 0.000000004f) + 1f) / 2f,
                    0.5f,
                    1f);
            setColor(activeColor.mult(v).setAlpha(1.0f));
        }
    }

    public void setSelected(boolean selected) {
        if (this.selected != selected) {
            this.selected = selected;
            setColor(selected ? activeColor : inactiveColor);
            selectionStart = System.nanoTime();
        }
    }
}
