package com.incognito.acejam0.states.game;

import com.incognito.acejam0.Application;
import com.incognito.acejam0.domain.Action;
import com.incognito.acejam0.domain.ActionInfo;
import com.incognito.acejam0.domain.InputBinding;
import com.incognito.acejam0.domain.Level;
import com.incognito.acejam0.domain.Tile;
import com.incognito.acejam0.states.common.BackgroundRendererState;
import com.incognito.acejam0.states.common.BackgroundRendererState.BgState;
import com.incognito.acejam0.states.common.TypedBaseAppState;
import com.incognito.acejam0.utils.AudioUtil;
import com.incognito.acejam0.utils.GlobalMaterials;
import com.incognito.acejam0.utils.TweenUtil;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;
import com.jme3.input.JoyInput;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.JoyAxisTrigger;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.simsilica.lemur.anim.SpatialTweens;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PlayerState extends TypedBaseAppState<Application> {

    private static final Logger logger = LogManager.getLogger();
    private final Node playersNode = new Node();
    private final List<Map.Entry<Spatial, Vector3f>> players = new ArrayList<>();
    private final Map<InputBinding, AtomicInteger> actionStates = new HashMap<>();
    boolean isFlipped = false;
    private Level level;
    private final Level originalLevel;
    private Node rootNode;
    private InputManager inputManager;
    private AppStateManager appStateManager;
    private final List<Runnable> completedListeners = new ArrayList<>();
    private boolean completed = false;

    private final Map<InputBinding, ActionListener> listeners = Map.of(
            InputBinding.UP, (name, isPressed, tpf) -> {
                if (isPressed) {
                    Map<Spatial, Vector3f> moved = move(0, -1);
                    if (!moved.isEmpty()) {
                        doAction(InputBinding.UP, moved);
                    }
                }
            },
            InputBinding.DOWN, (name, isPressed, tpf) -> {
                if (isPressed) {
                    Map<Spatial, Vector3f> moved = move(0, 1);
                    if (!moved.isEmpty()) {
                        doAction(InputBinding.DOWN, moved);
                    }
                }
            },
            InputBinding.LEFT, (name, isPressed, tpf) -> {
                if (isPressed) {
                    Map<Spatial, Vector3f> moved = move(-1, 0);
                    if (!moved.isEmpty()) {
                        doAction(InputBinding.LEFT, moved);
                    }
                }
            },
            InputBinding.RIGHT, (name, isPressed, tpf) -> {
                if (isPressed) {
                    Map<Spatial, Vector3f> moved = move(1, 0);
                    if (!moved.isEmpty()) {
                        doAction(InputBinding.RIGHT, moved);
                    }
                }
            });

    private final ActionListener flipListener = (name, isPressed, tpf) -> {
        if (isPressed) {
            List<ActionInfo> flips = IntStream.range(0, level.getMap().size())
                    .mapToObj(i -> new ActionInfo(i % level.getWidth(), i / level.getWidth(), false, 2, null, 0))
                    .toList();

            updateState(new Action(flips), Set.of());

            if (!completed) {
                appStateManager.getState(BackgroundRendererState.class)
                        .setBackgroundState(isFlipped ? BgState.FRONT : BgState.BACK, 0.75f);
            }
            isFlipped = !isFlipped;
        }
    };

    private final ActionListener resetListener = (name, isPressed, tpf) -> {
        if (isPressed) {
            restartLevel();
        }
    };

    public PlayerState(Level level) {
        this.level = level;
        this.originalLevel = Level.copy(level);
    }

    public void restartLevel() {
        TweenUtil.clearAnimations();
        appStateManager.detach(this);
        appStateManager.attach(new PlayerState(originalLevel));
        PlayerState p = appStateManager.getState(PlayerState.class);
        completedListeners.forEach(p::addCompletedListener);
        appStateManager.getState(MapRendererState.class).setLevel(originalLevel);
    }

    private void doAction(InputBinding dir, Map<Spatial, Vector3f> moved) {
        AtomicInteger state = actionStates.computeIfAbsent(dir, d -> new AtomicInteger(0));
        List<Action> actions = level.getActions().getOrDefault(dir.ordinal(), List.of());
        if (!actions.isEmpty() && state.get() < actions.size() && state.get() >= 0) {
            updateState(actions.get(state.getAndIncrement()), moved.values());

            if (state.get() >= actions.size()) {
                state.set(0);
            }
        }

        Map<Integer, Action> switchActions = level.getSwitchActions();
        moved.forEach((key, value) -> {
            int x = (int) value.x;
            int y = (int) value.y;
            boolean inactive = value.z < 0;
            Tile t = inactive ? level.getInactiveTile(x, y) : level.getActiveTile(x, y);
            if (t == Tile.BUTTON) {
                int i = y * level.getWidth() + x;
                if (inactive) {
                    i *= -1;
                }
                Action action = switchActions.get(i);
                if (action.getActions().stream().anyMatch(a -> a.getTileChangeSide() > 0)) {
                    Action newAction = new Action(action.getActions().stream()
                            .map(a -> {
                                int side = a.getTileChangeSide();
                                if (side <= 0) {
                                    return a;
                                } else if (side == 1) {
                                    side = inactive ? -1 : 0;
                                } else if (side == 2) {
                                    side = inactive ? 0 : 1;
                                }
                                return new ActionInfo(a.getX(), a.getY(), a.isRelative(), a.getStateChange(), a.getTileChange(), side);
                            })
                            .toList());
                    updateState(newAction, List.of(value));
                } else {
                    updateState(action, List.of(value));
                }
            }
        });

        if (checkFinish()) {
            completed = true;
            appStateManager.getState(BackgroundRendererState.class)
                    .setBackgroundState(BgState.COMPLETE, 1.0f);
            onDisable();
            inputManager.addListener(flipListener, "flip");
            onCompleted();
        }
    }

    private boolean checkFinish() {
        // every player must be on an exit
        if (players.stream()
                .anyMatch(kvp -> (kvp.getValue().z < 0 ?
                        level.getInactiveTile((int) kvp.getValue().x, (int) kvp.getValue().y) :
                        level.getActiveTile((int) kvp.getValue().x, (int) kvp.getValue().y)) != Tile.EXIT)) {
            return false;
        }

        // if more players than exits, some exits will need more than one player, but spread evenly
        // to prevent piling all players onto a single exit
        int maxPlayerPerExit = (int) FastMath.ceil(level.getNumStarts() / (float) level.getNumExits());

        // if more exits than players, some exits will be empty, so stop checking early once all
        // players are accounted for
        int total = 0;
        List<Tile> map = level.getMap();
        List<Tile> map2 = level.getMap2();
        for (int i = 0; i < map.size(); i++) {
            int x = i % level.getWidth();
            int y = i / level.getWidth();
            if (map.get(i) == Tile.EXIT) {
                long count = players.stream()
                        .filter(kvp -> level.isTileFlipped((int) kvp.getValue().x, (int) kvp.getValue().y) ?
                                (kvp.getValue().z < 0) : (kvp.getValue().z > 0))
                        .filter(kvp -> (int) kvp.getValue().x == x && (int) kvp.getValue().y == y)
                        .count();
                if (count > maxPlayerPerExit) {
                    return false;
                }
                total += count;
                if (total == level.getNumStarts()) {
                    return true;
                }
            }
            if (map2.get(i) == Tile.EXIT) {
                long count = players.stream()
                        .filter(kvp -> level.isTileFlipped((int) kvp.getValue().x, (int) kvp.getValue().y) ?
                                (kvp.getValue().z > 0) : (kvp.getValue().z < 0))
                        .filter(kvp -> (int) kvp.getValue().x == x && (int) kvp.getValue().y == y)
                        .count();
                if (count > maxPlayerPerExit) {
                    return false;
                }
                total += count;
                if (total == level.getNumStarts()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void updateState(Action action, Collection<Vector3f> moved) {
        Map<Vector2f, Boolean> tiles = players.stream()
                .collect(Collectors.toMap(
                        kvp -> new Vector2f(kvp.getValue().x, kvp.getValue().y),
                        kvp -> level.isTileFlipped((int) kvp.getValue().x, (int) kvp.getValue().y),
                        (a, b) -> a));

        List<ActionInfo> relativeActions = action.getActions().stream()
                .filter(ActionInfo::isRelative)
                .flatMap(a -> {
                    Vector2f offset = new Vector2f(a.getX(), a.getY());
                    return moved.stream()
                            .distinct()
                            .map(v -> v.add(offset.x, offset.y, 0f))
                            .map(v -> new ActionInfo((int) v.x, (int) v.y, false, a.getStateChange(), a.getTileChange(), a.getTileChangeSide()));
                })
                .toList();
        if (!relativeActions.isEmpty()) {
            ArrayList<ActionInfo> newActions = new ArrayList<>(action.getActions().stream().filter(Predicate.not(ActionInfo::isRelative)).toList());
            newActions.addAll(relativeActions.stream()
                    .filter(a -> a.getX() >= 0 && a.getY() >= 0 && a.getX() < level.getWidth() && a.getY() < level.getHeight())
                    .toList());
            action = new Action(newActions);
        }

        appStateManager.getState(MapRendererState.class).update(action);

        players.stream()
                .filter(kvp -> level.isTileFlipped((int) kvp.getValue().x, (int) kvp.getValue().y) != tiles.get(new Vector2f(kvp.getValue().x, kvp.getValue().y)))
                .forEach(kvp -> {
                    logger.info("Tweening from {} to {}", kvp.getKey().getLocalTranslation(), kvp.getKey().getLocalTranslation().mult(1, 1, -1));
                    kvp.getValue().setZ(kvp.getValue().z * -1);
                    TweenUtil.addAnimation(kvp.getKey(), () -> SpatialTweens.move(
                            kvp.getKey(), null,
                            kvp.getKey().getLocalTranslation().mult(1, 1, -1),
                            7.5f));
                });
    }

    private Map<Spatial, Vector3f> move(int dx, int dy) {
        Map<Spatial, Vector3f> moved = new HashMap<>();
        for (Map.Entry<Spatial, Vector3f> kvp : players) {
            Vector3f p = kvp.getValue();
            int x = (int) p.x;
            int y = (int) p.y;

            int nx = x + dx;
            int ny = y + dy;

            if (nx < 0 || ny < 0 || nx >= level.getWidth() || ny >= level.getHeight()) {
                continue;
            }

            Tile tile = p.z < 0 ? level.getInactiveTile(nx, ny) : level.getActiveTile(nx, ny);
            if (tile == Tile.WALL || tile == Tile.EMPTY) {
                continue;
            }

            TweenUtil.skip(kvp.getKey());

            p.x = nx;
            p.y = ny;
            TweenUtil.addAnimation(kvp.getKey(), () -> SpatialTweens.move(
                    kvp.getKey(), null, kvp.getKey().getLocalTranslation().add(dx, -dy, 0), 0.25f));
            moved.put(kvp.getKey(), p);

            AudioUtil.playPing(p);
        }
        return moved;
    }

    @Override
    protected void onInitialize(Application app) {
        rootNode = app.getRootNode();
        inputManager = app.getInputManager();
        appStateManager = app.getStateManager();

        List<Tile> map = level.getMap();
        List<Tile> map2 = level.getMap2();
        for (int i = 0; i < map.size(); i++) {
            if (map.get(i) == Tile.START) {
                createPlayer(i, false);
            }
            if (map2.get(i) == Tile.START) {
                createPlayer(i, true);
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
        inputManager.addMapping("flip", new KeyTrigger(KeyInput.KEY_LSHIFT), new KeyTrigger(KeyInput.KEY_RSHIFT));
        inputManager.addMapping("reset", new KeyTrigger(KeyInput.KEY_R));

        for (InputBinding i : InputBinding.values()) {
            addListener(inputManager, i);
        }
        inputManager.addListener(flipListener, "flip");
        inputManager.addListener(resetListener, "reset");

        appStateManager.getState(BackgroundRendererState.class).setBackgroundState(BgState.FRONT, 1.0f);
    }

    @Override
    protected void onCleanup(Application app) {
        rootNode.detachChild(playersNode);
        for (InputBinding i : InputBinding.values()) {
            removeListener(inputManager, i);
        }
        inputManager.removeListener(flipListener);
        inputManager.deleteMapping("flip");
        inputManager.removeListener(resetListener);
        inputManager.deleteMapping("reset");
    }

    @Override
    protected void onEnable() {
        if (inputManager != null) {
            for (InputBinding i : InputBinding.values()) {
                addListener(inputManager, i);
            }
            inputManager.addListener(flipListener, "flip");
            inputManager.addListener(resetListener, "reset");
        }
    }

    @Override
    protected void onDisable() {
        if (inputManager != null) {
            for (InputBinding i : InputBinding.values()) {
                inputManager.removeListener(listeners.get(i));
            }
            inputManager.removeListener(flipListener);
            inputManager.removeListener(resetListener);
        }
    }

    private void createPlayer(int i, boolean flipped) {
        int x = i % level.getWidth();
        int y = i / level.getWidth();
        int z = flipped ? -1 : 1;
        logger.info("Creating player at {}, {} ({})", x, y, flipped ? "back" : "front");
        Geometry g = new Geometry("", new Sphere(16, 2, 0.4f));
        Material mat = GlobalMaterials.getShaderMaterial(
                flipped ? ColorRGBA.Magenta : ColorRGBA.Cyan,
                FastMath.nextRandomFloat() * 5f + 5f,
                FastMath.nextRandomFloat() + 0.45f,
                FastMath.nextRandomFloat() * 30f + 25f);
        mat.setBoolean("LocalSpace", true);
        g.setMaterial(mat);
        g.rotate(FastMath.HALF_PI, 0, 0);
        g.setLocalTranslation(x, -y, z);
        players.add(Map.entry(g, new Vector3f(x, y, z)));
        playersNode.attachChild(g);
    }

    private void addListener(InputManager inputManager, InputBinding mapping) {
        inputManager.addListener(listeners.get(mapping), mapping.name());
    }

    private void removeListener(InputManager inputManager, InputBinding mapping) {
        inputManager.removeListener(listeners.get(mapping));
        inputManager.deleteMapping(mapping.name());
    }

    public void addCompletedListener(Runnable listener) {
        completedListeners.add(listener);
    }

    private void onCompleted() {
        completedListeners.forEach(Runnable::run);
    }
}
