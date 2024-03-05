package com.incognito.acejam0.states;

import com.incognito.acejam0.Application;
import com.incognito.acejam0.domain.Action;
import com.incognito.acejam0.domain.InputBinding;
import com.incognito.acejam0.domain.Level;
import com.incognito.acejam0.domain.Tile;
import com.incognito.acejam0.utils.GlobalMaterials;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;
import com.jme3.input.JoyInput;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.JoyAxisTrigger;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerState extends TypedBaseAppState<Application> {

    private static final Logger logger = LogManager.getLogger();

    private Level level;
    private Node rootNode;
    private final Node playersNode = new Node();
    private final List<Map.Entry<Spatial, Vector2f>> players = new ArrayList<>();
    private final Map<InputBinding, AtomicInteger> actionStates = new HashMap<>();

    private InputManager inputManager;
    private AppStateManager appStateManager;

    private final Map<InputBinding, ActionListener> listeners = Map.of(
            InputBinding.UP, (name, isPressed, tpf) -> {
                if (isPressed) {
                    move(0, -1);
                    doAction(InputBinding.UP);
                }
            },
            InputBinding.DOWN, (name, isPressed, tpf) -> {
                if (isPressed) {
                    move(0, 1);
                    doAction(InputBinding.DOWN);
                }
            },
            InputBinding.LEFT, (name, isPressed, tpf) -> {
                if (isPressed) {
                    move(-1, 0);
                    doAction(InputBinding.LEFT);
                }
            },
            InputBinding.RIGHT, (name, isPressed, tpf) -> {
                if (isPressed) {
                    move(1, 0);
                    doAction(InputBinding.RIGHT);
                }
            });

    private void doAction(InputBinding dir) {
        AtomicInteger state = actionStates.computeIfAbsent(dir, d -> new AtomicInteger(0));
        List<Action> actions = level.getActions().getOrDefault(dir.ordinal(), List.of());
        if (!actions.isEmpty() && state.get() < actions.size() && state.get() >= 0) {
            appStateManager.getState(MapRendererState.class).update(actions.get(state.getAndIncrement()));
        }
    }

    private void move(int dx, int dy) {
        for (Map.Entry<Spatial, Vector2f> kvp : players) {
            Vector2f p = kvp.getValue();
            int x = (int) p.x;
            int y = (int) p.y;

            int nx = x + dx;
            int ny = y + dy;

            if (nx < 0 || ny < 0 || nx >= level.getWidth() || ny >= level.getHeight()) {
                continue;
            }

            Tile tile = level.getTile(nx, ny);
            if (tile == Tile.WALL || tile == Tile.EMPTY) {
                continue;
            }

            p.x = nx;
            p.y = ny;
            kvp.getKey().setLocalTranslation(kvp.getKey().getLocalTranslation().add(dx, -dy, 0));
        }
    }

    public PlayerState(Level level) {
        this.level = level;
    }

    @Override
    protected void onInitialize(Application app) {
        rootNode = app.getRootNode();
        inputManager = app.getInputManager();
        appStateManager = app.getStateManager();

        List<Tile> map = level.getMap();
        for (int i = 0; i < map.size(); i++) {
            if (map.get(i) == Tile.START) {
                int x = i % level.getWidth();
                int y = i / level.getWidth();
                logger.info("Creating player at {}, {}", x, y);
                Geometry g = new Geometry(String.format("player-x:%d,y:%d", x, y), new Sphere(16, 2, 0.4f));
                g.setMaterial(GlobalMaterials.getPlayerMaterial());
                g.rotate(FastMath.HALF_PI, 0, 0);
                g.setLocalTranslation(x, -y, 0);
                players.add(Map.entry(g, new Vector2f(x, y)));
                playersNode.attachChild(g);
            }
        }

        rootNode.attachChild(playersNode);

        inputManager.addMapping(InputBinding.UP.name(),
                new KeyTrigger(KeyInput.KEY_W),
                new KeyTrigger(KeyInput.KEY_UP),
                new JoyAxisTrigger(0, JoyInput.AXIS_POV_Y, true));
        inputManager.addMapping(InputBinding.DOWN.name(),
                new KeyTrigger(KeyInput.KEY_S),
                new KeyTrigger(KeyInput.KEY_DOWN),
                new JoyAxisTrigger(0, JoyInput.AXIS_POV_Y, false));
        inputManager.addMapping(InputBinding.LEFT.name(),
                new KeyTrigger(KeyInput.KEY_A),
                new KeyTrigger(KeyInput.KEY_LEFT),
                new JoyAxisTrigger(0, JoyInput.AXIS_POV_X, false));
        inputManager.addMapping(InputBinding.RIGHT.name(),
                new KeyTrigger(KeyInput.KEY_D),
                new KeyTrigger(KeyInput.KEY_RIGHT),
                new JoyAxisTrigger(0, JoyInput.AXIS_POV_X, true));

        addListener(inputManager, InputBinding.UP);
        addListener(inputManager, InputBinding.DOWN);
        addListener(inputManager, InputBinding.LEFT);
        addListener(inputManager, InputBinding.RIGHT);
    }

    private void addListener(InputManager inputManager, InputBinding mapping) {
        inputManager.addListener(listeners.get(mapping), mapping.name());
    }

    private void removeListener(InputManager inputManager, InputBinding mapping) {
        inputManager.removeListener(listeners.get(mapping));
    }

    @Override
    protected void onCleanup(Application app) {
        rootNode.detachChild(playersNode);
        removeListener(inputManager, InputBinding.UP);
        removeListener(inputManager, InputBinding.DOWN);
        removeListener(inputManager, InputBinding.LEFT);
        removeListener(inputManager, InputBinding.RIGHT);
    }

    public void setLevel(Level level) {
        this.level = level;
    }
}
