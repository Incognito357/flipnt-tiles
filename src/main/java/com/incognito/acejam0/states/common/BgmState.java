package com.incognito.acejam0.states.common;

import com.incognito.acejam0.Application;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.audio.Environment;

import java.time.Duration;

public class BgmState extends TypedBaseAppState<Application> {

    private AudioNode bgm;
    private long stoppedAt = -1;

    @Override
    protected void onInitialize(Application app) {
        bgm = new AudioNode(app.getAssetManager(), "audio/bgm.ogg", AudioData.DataType.Stream);
        bgm.setPositional(false);
        bgm.play();

        // Ice Palace Large Room
        app.getAudioRenderer().setEnvironment(new Environment(new float[]{
                26, 2.9f, 0.810f, -1000, -500, -700, 3.14f, 1.53f, 0.32f, -1200, 0.039f, 0f, 0f, 0f, 000, 0.027f, 0f, 0f, 0f, 0.214f, 0.110f, 0.250f, 0f, -5f, 12428.5f, 99.6f, 0f, 0x20
        }));
    }

    @Override
    protected void onCleanup(Application app) {
        bgm.stop();
    }

    @Override
    public void update(float tpf) {
        if (bgm != null && bgm.getStatus() == AudioSource.Status.Stopped) {
            long now = System.nanoTime();
            if (stoppedAt == -1) {
                stoppedAt = now;
            } else if (Duration.ofNanos(now - stoppedAt).minusSeconds(30).isPositive()) {
                stoppedAt = -1;
                bgm.play();
            }
        }
    }
}
