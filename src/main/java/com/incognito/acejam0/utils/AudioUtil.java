package com.incognito.acejam0.utils;

import com.incognito.acejam0.Application;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

import java.util.Set;

public class AudioUtil {
    private AudioUtil() {}

    private static Set<AudioNode> pings;

    private static AudioNode initAudio(AssetManager assetManager, String file) {
        AudioNode node = new AudioNode(assetManager, file, AudioData.DataType.Buffer);
        node.setReverbEnabled(true);
        node.setVolume(0.25f);
        return node;
    }


    public static void initTracks() {
        if (pings == null) {
            AssetManager assetManager = Application.APP.getAssetManager();
            pings = Set.of(
                    initAudio(assetManager, "audio/pinga5.wav"),
                    initAudio(assetManager, "audio/pingb5.wav"),
                    initAudio(assetManager, "audio/pingc6.wav"),
                    initAudio(assetManager, "audio/pingd6.wav"),
                    initAudio(assetManager, "audio/pinge6.wav"));
        }
    }

    public static void playPing(Vector3f pos) {
        pings.stream()
                .skip(FastMath.nextRandomInt(0, pings.size() - 1))
                .findFirst()
                .ifPresent(a -> {
                    float rng = FastMath.nextRandomFloat();
                    if (rng < 0.65f){
                        a.setPitch(1.0f);
                    } else {
                        a.setPitch(2.0f);
                    }
                    a.setLocalTranslation(pos);
                    a.playInstance();
                });
    }
}
