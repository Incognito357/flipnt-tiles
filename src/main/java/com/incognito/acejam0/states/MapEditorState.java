package com.incognito.acejam0.states;

import com.incognito.acejam0.Application;
import com.incognito.acejam0.domain.Action;
import com.incognito.acejam0.domain.ActionInfo;
import com.incognito.acejam0.domain.Level;
import com.incognito.acejam0.domain.Tile;
import com.incognito.acejam0.utils.GlobalMaterials;
import com.incognito.acejam0.utils.Mapper;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.debug.WireBox;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.SpringGridLayout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class MapEditorState extends TypedBaseAppState<Application> {

    private static final Logger logger = LogManager.getLogger();

    private AppStateManager appStateManager;
    private InputManager inputManager;
    private Camera camera;

    private Level level;
    private Level prePlayLevel;
    private Node rootNode;
    private Node guiNode;
    private Container gui;
    private TextField txtLevelName;
    private Label lblWidth;
    private Label lblHeight;
    private Label lblMouse;
    private Label lblTile;

    private final Map<Vector2f, TileInfo> tiles = new HashMap<>();
    private Vector2f boundsMin = Vector2f.ZERO.clone();
    private Vector2f boundsMax = Vector2f.ZERO.clone();
    private Vector2f mouseCell = Vector2f.ZERO.clone();

    private final Geometry cursor = new Geometry("", new WireBox(0.5f, 0.5f, 0f));
    private final Geometry bounds = new Geometry("", new WireBox(0f, 0f, 0f));
    private Tile selectedTile = Tile.FLOOR;

    private boolean leftClicking = false;
    private boolean rightClicking = false;
    private boolean firstLeftClicking = false;
    private boolean firstRightClicking = false;
    private boolean playing = false;

    private Vector2f camOffset = Vector2f.ZERO.clone();

    private record TileInfo(Tile a, Tile b, boolean flipped) {
    }

    private final ActionListener clickListener = (name, isPressed, tpf) -> {
        if (!leftClicking && isPressed) {
            firstLeftClicking = true;
        }
        leftClicking = isPressed;
    };

    private final ActionListener rightClickListener = (name, isPressed, tpf) -> {
        if (!rightClicking && isPressed) {
            firstRightClicking = true;
        }
        rightClicking = isPressed;
    };

    private final ActionListener tilePicker = (name, isPressed, tpf) -> {
        selectedTile = Tile.values()[Integer.parseInt(name.split("-")[1])];
        lblTile.setText("Tile: " + selectedTile.name());
    };

    @Override
    protected void onInitialize(Application app) {
        appStateManager = app.getStateManager();
        inputManager = app.getInputManager();
        camera = app.getCamera();
        rootNode = app.getRootNode();
        guiNode = app.getGuiNode();

        AppSettings settings = app.getContext().getSettings();
        gui = new Container(new SpringGridLayout());
        gui.setLocalTranslation(10, settings.getHeight() - 10f, 0f);
        guiNode.attachChild(gui);

        txtLevelName = new TextField("");
        Button btnLoadLevel = new Button("Load");
        btnLoadLevel.addClickCommand(btn -> {
            if (txtLevelName.getText() != null && !txtLevelName.getText().isBlank()) {
                loadLevel(txtLevelName.getText());
            }
        });
        Button btnSaveLevel = new Button("Save");
        btnSaveLevel.addClickCommand(btn -> {
            if (txtLevelName.getText() != null && !txtLevelName.getText().isBlank()) {
                saveLevel(txtLevelName.getText());
            }
        });

        gui.addChild(txtLevelName);
        gui.addChild(btnLoadLevel, 1);
        gui.addChild(btnSaveLevel, 2);

        gui.addChild(new Label("Width"));
        lblWidth = new Label("");
        gui.addChild(lblWidth, 1);

        gui.addChild(new Label("Height"));
        lblHeight = new Label("");
        gui.addChild(lblHeight, 1);

        lblMouse = new Label("");
        gui.addChild(lblMouse);

        lblTile = new Label("Tile: " + selectedTile.name());
        gui.addChild(lblTile);

        Button btnPlay = new Button("Play");
        Button btnFlip = new Button("Flip Map");
        btnPlay.addClickCommand(btn -> {
            if (playing) {
                btn.setText("Play");
                appStateManager.detach(appStateManager.getState(PlayerState.class));
                btnFlip.setEnabled(true);
                level = prePlayLevel;
                syncLevel(true);
            } else {
                btn.setText("Editor");
                appStateManager.attach(new PlayerState(level));
                GuiGlobals.getInstance().releaseFocus(guiNode);
                btnFlip.setEnabled(false);
                prePlayLevel = new Level(
                        level.getTitle(),
                        level.getWidth(),
                        level.getHeight(),
                        level.getMap(),
                        level.getMap2(),
                        BitSet.valueOf(level.getState().toByteArray()),
                        level.getActions());
            }
            playing = !playing;
            MapRendererState renderer = appStateManager.getState(MapRendererState.class);
            renderer.setEditing(!playing);
            renderer.reloadLevel();
        });
        gui.addChild(btnPlay);

        btnFlip.addClickCommand(btn -> {
            List<ActionInfo> flips = IntStream.range(0, level.getMap().size())
                    .mapToObj(i -> new ActionInfo(i % level.getWidth(), i / level.getWidth(), 2, null))
                    .toList();
            appStateManager.getState(MapRendererState.class).update(new Action(flips));
        });
        gui.addChild(btnFlip);

        cursor.setMaterial(GlobalMaterials.getDebugMaterial(ColorRGBA.Yellow));
        bounds.setMaterial(GlobalMaterials.getDebugMaterial(new ColorRGBA(0, 0, 0.25f, 1)));
        rootNode.attachChild(cursor);
        rootNode.attachChild(bounds);

        Arrays.stream(Tile.values())
                .map(Tile::ordinal)
                .forEach(o -> {
                    String name = "editor-" + o;
                    inputManager.addMapping(name, new KeyTrigger(KeyInput.KEY_1 + o));
                    inputManager.addListener(tilePicker, name);
                });
        inputManager.addMapping("editor-click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("editor-rightclick", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addListener(clickListener, "editor-click");
        inputManager.addListener(rightClickListener, "editor-rightclick");

        appStateManager.getState(MapRendererState.class).setEditing(true);
    }

    @Override
    protected void onCleanup(Application app) {
        guiNode.detachChild(gui);
        rootNode.detachChild(cursor);
        rootNode.detachChild(bounds);
    }

    @Override
    protected void onEnable() {
        GuiGlobals.getInstance().requestCursorEnabled(gui);
    }

    @Override
    protected void onDisable() {
        GuiGlobals.getInstance().releaseCursorEnabled(gui);
    }

    @Override
    public void update(float tpf) {
        float depth = camera.getViewToProjectionZ(camera.getLocation().z);
        Vector2f mousePos = inputManager.getCursorPosition();
        Vector3f mouseWorldPos = camera.getWorldCoordinates(new Vector2f(mousePos.x, mousePos.y), depth);

        int x = (int) FastMath.floor(mouseWorldPos.x + 0.5f);
        int y = (int) FastMath.floor(mouseWorldPos.y + 0.5f);
        cursor.setLocalTranslation(x, y, 0.1f);

        Vector2f lastMouseCell = mouseCell.clone();

        mouseCell = new Vector2f(x, -y);
        mouseCell.x = FastMath.floor(mouseCell.x);
        mouseCell.y = FastMath.floor(mouseCell.y);

        lblMouse.setText(String.format("cell: (%d, %d)", (int) mouseCell.x, (int) mouseCell.y));

        if ((firstLeftClicking || firstRightClicking) || ((leftClicking || rightClicking) && !lastMouseCell.equals(mouseCell))) {
            if (leftClicking) {
                firstLeftClicking = false;
                clickTile(false);
            }
            if (rightClicking) {
                firstRightClicking = false;
                clickTile(true);
            }
        }
    }

    private void saveLevel(String name) {
        try {
            Mapper.getMapper().writeValue(new File("levels/" + name + ".json"), level);
        } catch (IOException e) {
            logger.error("Could not save level {}.json", name, e);
        }
    }

    private void loadLevel(String name) {
        level = Level.loadLevel(name);
        syncLevel(false);
    }

    private void syncLevel(boolean saveCam) {
        if (level != null) {
            Vector3f oldCam = camera.getLocation().clone();
            appStateManager.getState(MapRendererState.class).setLevel(level);
            if (saveCam) {
                camera.setLocation(oldCam.add(camOffset.x, camOffset.y, 0f));
            }
            camOffset = Vector2f.ZERO.clone();
            boundsMin = Vector2f.ZERO.clone();
            boundsMax = new Vector2f(level.getWidth() - 1f, level.getHeight() - 1f);
            bounds.setMesh(new WireBox(level.getWidth() / 2f, level.getHeight() / 2f, 0f));
            bounds.setLocalTranslation(level.getWidth() / 2f - 0.5f, -level.getHeight() / 2f + 0.5f, 0.1f);
            lblWidth.setText(String.valueOf(level.getWidth()));
            lblHeight.setText(String.valueOf(level.getHeight()));
            tiles.clear();
            for (int y = 0; y < level.getHeight(); y++) {
                for (int x = 0; x < level.getWidth(); x++) {
                    tiles.put(new Vector2f(x, y), new TileInfo(level.getTile(x, y), level.getTile2(x, y), level.isTileFlipped(x, y)));
                }
            }
        }
    }

    private void updateEditorLevel() {
        Vector2f size = boundsMax.subtract(boundsMin);
        List<Tile> map1 = new ArrayList<>();
        List<Tile> map2 = new ArrayList<>();
        BitSet state = new BitSet();
        int y = 0;
        for (int editY = (int) boundsMin.y; editY <= boundsMax.y; editY++) {
            int x = 0;
            for (int editX = (int) boundsMin.x; editX <= boundsMax.x; editX++) {
                Vector2f editCell = new Vector2f(editX, editY);
                TileInfo tileInfo = tiles.get(editCell);
                if (tileInfo == null) {
                    map1.add(Tile.EMPTY);
                    map2.add(Tile.EMPTY);
                } else {
                    map1.add(tileInfo.a);
                    map2.add(tileInfo.b);
                    state.set(y * ((int) size.x + 1) + x, tileInfo.flipped);
                }
                x++;
            }
            y++;
        }

        //todo: automatically map action coordinates

        level = new Level(
                txtLevelName.getText(),
                (int) size.x + 1,
                (int) size.y + 1,
                map1,
                map2,
                state,
                Map.of());
        syncLevel(true);
    }

    private void clickTile(boolean rightClick) {
        if (playing) {
            return;
        }
        logger.info("Adding {} to {} at {}", selectedTile, rightClick ? "map2" : "map", mouseCell);
        tiles.compute(mouseCell, (key, original) -> {
            if (original != null) {
                Tile a = rightClick ? original.a : selectedTile;
                Tile b = rightClick ? selectedTile : original.b;
                return new TileInfo(a, b, original.flipped);
            }
            Tile a = rightClick ? Tile.EMPTY : selectedTile;
            Tile b = rightClick ? selectedTile : Tile.EMPTY;
            return new TileInfo(a, b, false);
        });
        if (mouseCell.x < boundsMin.x) {
            boundsMin.x = mouseCell.x;
            camOffset.x = -boundsMin.x;
        } else if (mouseCell.x > boundsMax.x) {
            boundsMax.x = mouseCell.x;
        }
        if (mouseCell.y < boundsMin.y) {
            boundsMin.y = mouseCell.y;
            camOffset.y = boundsMin.y;
        } else if (mouseCell.y > boundsMax.y) {
            boundsMax.y = mouseCell.y;
        }
        updateEditorLevel();
    }
}
