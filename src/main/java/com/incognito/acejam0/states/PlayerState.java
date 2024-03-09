package com.incognito.acejam0.states;

import com.incognito.acejam0.Application;
import com.incognito.acejam0.domain.Action;
import com.incognito.acejam0.domain.ActionInfo;
import com.incognito.acejam0.domain.InputBinding;
import com.incognito.acejam0.domain.Level;
import com.incognito.acejam0.domain.Tile;
import com.incognito.acejam0.states.BackgroundRendererState.BgState;
import com.incognito.acejam0.utils.GlobalMaterials;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;
import com.jme3.input.JoyInput;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.JoyAxisTrigger;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.simsilica.lemur.anim.AnimationState;
import com.simsilica.lemur.anim.SpatialTweens;
import com.simsilica.lemur.anim.Tween;
import com.simsilica.lemur.anim.TweenAnimation;
import com.simsilica.lemur.anim.Tweens;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
    private final List<Map.Entry<Spatial, Vector2f>> players = new ArrayList<>();
    private final Map<InputBinding, AtomicInteger> actionStates = new HashMap<>();
    private final Set<TweenAnimation> currentTweens = new HashSet<>();
    boolean isFlipped = false;
    private Level level;
    private Node rootNode;
    private InputManager inputManager;
    private AppStateManager appStateManager;
    private final Map<InputBinding, ActionListener> listeners = Map.of(
            InputBinding.UP, (name, isPressed, tpf) -> {
                if (isPressed) {
                    Set<Vector2f> moved = move(0, -1);
                    if (!moved.isEmpty()) {
                        doAction(InputBinding.UP, moved);
                    }
                }
            },
            InputBinding.DOWN, (name, isPressed, tpf) -> {
                if (isPressed) {
                    Set<Vector2f> moved = move(0, 1);
                    if (!moved.isEmpty()) {
                        doAction(InputBinding.DOWN, moved);
                    }
                }
            },
            InputBinding.LEFT, (name, isPressed, tpf) -> {
                if (isPressed) {
                    Set<Vector2f> moved = move(-1, 0);
                    if (!moved.isEmpty()) {
                        doAction(InputBinding.LEFT, moved);
                    }
                }
            },
            InputBinding.RIGHT, (name, isPressed, tpf) -> {
                if (isPressed) {
                    Set<Vector2f> moved = move(1, 0);
                    if (!moved.isEmpty()) {
                        doAction(InputBinding.RIGHT, moved);
                    }
                }
            });

    private final ActionListener flipListener = (name, isPressed, tpf) -> {
        if (isPressed) {
            skipTweens();
            List<ActionInfo> flips = IntStream.range(0, level.getMap().size())
                    .mapToObj(i -> new ActionInfo(i % level.getWidth(), i / level.getWidth(), false, 2, null))
                    .toList();

            updateState(new Action(flips), Set.of());

            appStateManager.getState(BackgroundRendererState.class)
                    .setBackgroundState(isFlipped ? BgState.FRONT : BgState.BACK, 0.75f);
            isFlipped = !isFlipped;
        }
    };

    public PlayerState(Level level) {
        this.level = level;
    }

    private void doAction(InputBinding dir, Set<Vector2f> moved) {
        AtomicInteger state = actionStates.computeIfAbsent(dir, d -> new AtomicInteger(0));
        List<Action> actions = level.getActions().getOrDefault(dir.ordinal(), List.of());
        if (!actions.isEmpty() && state.get() < actions.size() && state.get() >= 0) {
            updateState(actions.get(state.getAndIncrement()), moved);

            if (state.get() >= actions.size()) {
                state.set(0);
            }
        }

        if (checkFinish()) {
            appStateManager.getState(BackgroundRendererState.class)
                    .setBackgroundState(BgState.COMPLETE, 1.5f);
        }
    }

    private boolean checkFinish() {
        // every player must be on an exit
        if (players.stream()
                .anyMatch(kvp -> (kvp.getKey().getLocalTranslation().z < 0 ?
                        level.getInactiveTile((int) kvp.getValue().x, (int) kvp.getValue().y) :
                        level.getActiveTile((int) kvp.getValue().x, (int) kvp.getValue().y)) != Tile.EXIT)) {
            return false;
        }

        // if more players than exits, some exits will need more than one player, but spread evenly
        // to prevent piling all players onto a single exit
        int maxPlayerPerExit = (int)FastMath.ceil(level.getNumStarts() / (float)level.getNumExits());

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
                        .filter(kvp -> kvp.getKey().getLocalTranslation().z > 0)
                        .filter(kvp -> (int) kvp.getValue().x == x && (int) kvp.getValue().getY() == y)
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
                        .filter(kvp -> kvp.getKey().getLocalTranslation().z < 0)
                        .filter(kvp -> (int) kvp.getValue().x == x && (int) kvp.getValue().getY() == y)
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

    private void updateState(Action action, Set<Vector2f> moved) {
        Map<Vector2f, Boolean> tiles = players.stream()
                .collect(Collectors.toMap(
                        Map.Entry::getValue,
                        kvp -> level.isTileFlipped((int) kvp.getValue().x, (int) kvp.getValue().y),
                        (a, b) -> a));

        List<ActionInfo> relativeActions = action.getActions().stream()
                .filter(ActionInfo::isRelative)
                .flatMap(a -> {
                    Vector2f offset = new Vector2f(a.getX(), a.getY());
                    return moved.stream()
                            .distinct()
                            .map(v -> v.add(offset))
                            .map(v -> new ActionInfo((int) v.x, (int) v.y, false, a.getStateChange(), a.getTileChange()));
                })
                .toList();
        if (!relativeActions.isEmpty()) {
            ArrayList<ActionInfo> newActions = new ArrayList<>(action.getActions().stream().filter(Predicate.not(ActionInfo::isRelative)).toList());
            newActions.addAll(relativeActions);
            action = new Action(newActions);
        }

        appStateManager.getState(MapRendererState.class).update(action);

        List<Tween> tweens = players.stream()
                .filter(kvp -> level.isTileFlipped((int) kvp.getValue().x, (int) kvp.getValue().y) != tiles.get(kvp.getValue()))
                .map(kvp -> {
                    logger.info("Tweening from {} to {}", kvp.getKey().getLocalTranslation(), kvp.getKey().getLocalTranslation().mult(1, 1, -1));
                    return SpatialTweens.move(
                            kvp.getKey(), null,
                            kvp.getKey().getLocalTranslation().mult(1, 1, -1),
                            7.5f);
                })
                .toList();
        logger.info("Tweening {} players", tweens.size());
        currentTweens.add(AnimationState.getDefaultInstance().add(Tweens.parallel(tweens.toArray(new Tween[0]))));
    }

    private Set<Vector2f> move(int dx, int dy) {
        Set<Vector2f> moved = new HashSet<>();
        for (Map.Entry<Spatial, Vector2f> kvp : players) {
            Vector2f p = kvp.getValue();
            int x = (int) p.x;
            int y = (int) p.y;

            int nx = x + dx;
            int ny = y + dy;

            if (nx < 0 || ny < 0 || nx >= level.getWidth() || ny >= level.getHeight()) {
                continue;
            }

            Tile tile = kvp.getKey().getLocalTranslation().z < 0 ? level.getInactiveTile(nx, ny) : level.getActiveTile(nx, ny);
            if (tile == Tile.WALL || tile == Tile.EMPTY) {
                continue;
            }

            skipTweens();

            p.x = nx;
            p.y = ny;
            kvp.getKey().setLocalTranslation(kvp.getKey().getLocalTranslation().add(dx, -dy, 0));
            moved.add(p);
        }
        return moved;
    }

    private void skipTweens() {
        currentTweens.forEach(t -> {
            t.fastForwardPercent(1.0);
        });
        currentTweens.clear();
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
        inputManager.addMapping("flip", new KeyTrigger(KeyInput.KEY_LSHIFT));

        addListener(inputManager, InputBinding.UP);
        addListener(inputManager, InputBinding.DOWN);
        addListener(inputManager, InputBinding.LEFT);
        addListener(inputManager, InputBinding.RIGHT);
        inputManager.addListener(flipListener, "flip");

        appStateManager.getState(BackgroundRendererState.class).setBackgroundState(BgState.FRONT, 0.5f);
    }

    private void createPlayer(int i, boolean flipped) {
        int x = i % level.getWidth();
        int y = i / level.getWidth();
        logger.info("Creating player at {}, {} ({})", x, y, flipped ? "back" : "front");
        Geometry g = new Geometry("", new Sphere(16, 2, 0.4f));
        g.setMaterial(GlobalMaterials.getShaderMaterial(
                flipped ? ColorRGBA.Magenta : ColorRGBA.Cyan,
                FastMath.nextRandomFloat() * 5f + 5f,
                FastMath.nextRandomFloat() + 0.45f,
                FastMath.nextRandomFloat() * 30f + 25f));
        g.rotate(FastMath.HALF_PI, 0, 0);
        g.setLocalTranslation(x, -y, flipped ? -1 : 1);
        players.add(Map.entry(g, new Vector2f(x, y)));
        playersNode.attachChild(g);
    }

    private void addListener(InputManager inputManager, InputBinding mapping) {
        inputManager.addListener(listeners.get(mapping), mapping.name());
    }

    private void removeListener(InputManager inputManager, InputBinding mapping) {
        inputManager.removeListener(listeners.get(mapping));
        inputManager.deleteMapping(mapping.name());
    }

    @Override
    protected void onCleanup(Application app) {
        rootNode.detachChild(playersNode);
        removeListener(inputManager, InputBinding.UP);
        removeListener(inputManager, InputBinding.DOWN);
        removeListener(inputManager, InputBinding.LEFT);
        removeListener(inputManager, InputBinding.RIGHT);
        inputManager.removeListener(flipListener);
        inputManager.deleteMapping("flip");
    }

    public void setLevel(Level level) {
        this.level = level;
    }
}
