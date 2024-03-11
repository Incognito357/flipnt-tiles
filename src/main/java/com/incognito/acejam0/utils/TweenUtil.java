package com.incognito.acejam0.utils;

import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.simsilica.lemur.anim.AbstractTween;
import com.simsilica.lemur.anim.AnimationState;
import com.simsilica.lemur.anim.Tween;
import com.simsilica.lemur.anim.TweenAnimation;
import com.simsilica.lemur.anim.Tweens;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class TweenUtil {
    private TweenUtil() {}

    private static final HashMap<Object, TweenAnimation> currentTweens = new HashMap<>();

    public static void addAnimation(Object key, Supplier<Tween> tween) {
        skip(key);
        currentTweens.put(key, AnimationState.getDefaultInstance().add(tween.get()));
    }

    public static <T extends Tween> void addLoop(Object key, Supplier<List<T>> tweens) {
        skip(key);
        currentTweens.put(key, AnimationState.getDefaultInstance()
                .add(new TweenAnimation(true, tweens.get().toArray(new Tween[]{}))));
    }

    public static void skip(Object key) {
        TweenAnimation t = currentTweens.remove(key);
        if (t != null) {
            t.fastForwardPercent(1.0);
            t.cancel();
        }
    }

    public static void clearAnimations() {
        currentTweens.forEach((k, v) -> v.fastForwardPercent(1.0));
        currentTweens.clear();
    }

    public static void tweenText(BitmapText text, String message, float length, float delay) {
        ColorRGBA origin = text.getColor();
        ColorRGBA target = text.getColor().clone().setAlpha(0);
        boolean skipFirst = text.getText() == null || text.getText().isBlank();
        boolean skipLast = message == null || message.isBlank();
        TweenUtil.addAnimation(text, () -> Tweens.sequence(
                new AbstractTween(skipFirst ? 0f : (skipLast ? length : length / 2.0f)) {
                    @Override
                    protected void doInterpolate(double t) {
                        text.setColor(new ColorRGBA().interpolateLocal(origin, target, (float) t));
                    }
                },
                new AbstractTween(delay) {
                    @Override
                    protected void doInterpolate(double t) {
                        text.setText(message);
                    }
                },
                new AbstractTween(skipLast ? 0f : (skipFirst ? length : length / 2.0f)) {
                    @Override
                    protected void doInterpolate(double t) {
                        text.setColor(new ColorRGBA().interpolateLocal(target, origin, (float) t));
                    }
                }));
    }
}
