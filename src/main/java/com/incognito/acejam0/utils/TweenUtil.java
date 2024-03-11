package com.incognito.acejam0.utils;

import com.simsilica.lemur.anim.AnimationState;
import com.simsilica.lemur.anim.Tween;
import com.simsilica.lemur.anim.TweenAnimation;

import java.util.HashMap;
import java.util.function.Supplier;

public class TweenUtil {
    private TweenUtil() {}

    private static final HashMap<Object, TweenAnimation> currentTweens = new HashMap<>();

    public static void addAnimation(Object key, Supplier<Tween> tween) {
        skip(key);
        currentTweens.put(key, AnimationState.getDefaultInstance().add(tween.get()));
    }

    public static void skip(Object key) {
        TweenAnimation t = currentTweens.remove(key);
        if (t != null) {
            t.fastForwardPercent(1.0);
        }
    }

    public static void clearAnimations() {
        currentTweens.forEach((k, v) -> v.fastForwardPercent(1.0));
        currentTweens.clear();
    }
}
