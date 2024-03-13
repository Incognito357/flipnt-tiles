package com.incognito.acejam0.utils;

import com.incognito.acejam0.Application;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioUtil {
    private AudioUtil() {}

    private static Set<AudioNode> pings;
    private static AudioNode bgm;
    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    private static AudioState bgmState;
    private static AudioState pingState;
    private static long lastPing = -1;

    public static class AudioState {
        private float original;
        private final float scale;
        private boolean muted;
        private final Collection<AudioNode> nodes;

        private AudioState(float scale, Collection<AudioNode> nodes) {
            this.original = 0.5f;
            this.scale = scale;
            this.muted = false;
            this.nodes = nodes;
            setVolume(original);
        }

        public boolean isMuted() {
            return muted;
        }

        public void toggleMute() {
            muted = !muted;
            nodes.forEach(a -> a.setVolume(getActualVolume()));
        }

        public void setVolume(float volume) {
            original = volume;
            nodes.forEach(a -> a.setVolume(volume * scale));
        }

        public float getVolume() {
            return original;
        }

        private float getActualVolume() {
            if (muted) {
                return 0f;
            }
            return original * scale;
        }
    }

    private static AudioNode initAudio(AssetManager assetManager, String file) {
        AudioNode node = new AudioNode(assetManager, file, AudioData.DataType.Buffer);
        node.setReverbEnabled(true);
        return node;
    }

    public static void initTracks() {
        if (initialized.compareAndSet(false, true)) {
            AssetManager assetManager = Application.APP.getAssetManager();

            bgm = new AudioNode(assetManager, "audio/bgm.ogg", AudioData.DataType.Stream);
            bgm.setPositional(false);
            bgmState = new AudioState(1.0f, List.of(bgm));

            pings = Set.of(
                    initAudio(assetManager, "audio/pinga5.wav"),
                    initAudio(assetManager, "audio/pingb5.wav"),
                    initAudio(assetManager, "audio/pingc6.wav"),
                    initAudio(assetManager, "audio/pingd6.wav"),
                    initAudio(assetManager, "audio/pinge6.wav"));
            pingState = new AudioState(0.2f, pings);
        }
    }

    public static void playPing(Vector3f pos) {
        pings.stream()
                .skip(FastMath.nextRandomInt(0, pings.size() - 1))
                .findFirst()
                .ifPresent(a -> {
                    float rng = FastMath.nextRandomFloat();

                    long now = System.nanoTime();
                    float scaleBack = 1.0f;
                    if (Duration.ofNanos(now - lastPing).minusMillis(100).isNegative()) {
                        scaleBack = 0.5f;
                    }
                    if (rng < 0.65f){
                        a.setPitch(1.0f);
                        a.setVolume(pingState.getActualVolume() * scaleBack);
                    } else {
                        a.setPitch(2.0f);
                        a.setVolume(pingState.getActualVolume() * .5f * scaleBack);   //higher pitched ones seem a lot louder
                    }
                    a.setLocalTranslation(pos);
                    a.playInstance();
                    lastPing = System.nanoTime();
                });
    }

    public static AudioNode getBgm() {
        return bgm;
    }

    public static AudioState getBgmState() {
        return bgmState;
    }

    public static AudioState getPingState() {
        return pingState;
    }

}
