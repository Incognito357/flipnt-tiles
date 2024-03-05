package com.incognito.acejam0.states;

import com.incognito.acejam0.Application;
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

public class MapEditorState extends TypedBaseAppState<Application> {

    private static final Logger logger = LogManager.getLogger();

    private AppStateManager appStateManager;
    private InputManager inputManager;
    private Camera camera;

    private Level level;
    private Node rootNode;
    private Node guiNode;
    private Container gui;
    private TextField txtLevelName;
    private Label lblWidth;
    private Label lblHeight;
    private Label lblMouse;
    private Label lblTile;

    private record TileInfo(Tile a, Tile b, boolean flipped) {}

    private final Map<Vector2f, TileInfo> tiles = new HashMap<>();
    private Vector2f boundsMin = Vector2f.ZERO.clone();
    private Vector2f boundsMax = Vector2f.ZERO.clone();
    private Vector2f mouseCell = Vector2f.ZERO.clone();

    private final Geometry cursor = new Geometry("", new WireBox(0.5f, 0.5f, 0f));
    private Tile selectedTile = Tile.FLOOR;

    private boolean leftClicking = false;
    private boolean rightClicking = false;
    private boolean firstLeftClicking = false;
    private boolean firstRightClicking = false;

    private Vector2f camOffset = Vector2f.ZERO.clone();

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

    private void clickTile(boolean rightClick) {
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
            //logger.info("Decreased x bounds from {} to {}", boundsMin.x, mouseCell.x);
            boundsMin.x = mouseCell.x;
            camOffset.x = -boundsMin.x;
        } else if (mouseCell.x > boundsMax.x) {
            //logger.info("Increased x bounds from {} to {}", boundsMax.x, mouseCell.x);
            boundsMax.x = mouseCell.x;
        }
        if (mouseCell.y < boundsMin.y) {
            //logger.info("Decreased y bounds from {} to {}", boundsMin.y, mouseCell.y);
            boundsMin.y = mouseCell.y;
            camOffset.y = boundsMin.y;
        } else if (mouseCell.y > boundsMax.y) {
            //logger.info("Increased y bounds from {} to {}", boundsMax.y, mouseCell.y);
            boundsMax.y = mouseCell.y;
        }
        updateEditorLevel();
    }

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
        Button loadLevel = new Button("Load");
        loadLevel.addClickCommand(btn -> {
            if (txtLevelName.getText() != null && !txtLevelName.getText().isBlank()) {
                loadLevel(txtLevelName.getText());
            }
        });
        gui.addChild(txtLevelName);
        gui.addChild(loadLevel, 1);

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

        cursor.setMaterial(GlobalMaterials.getDebugMaterial(ColorRGBA.Yellow));

        rootNode.attachChild(cursor);

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
    }

    @Override
    protected void onCleanup(Application app) {
        guiNode.detachChild(gui);
    }

    @Override
    protected void onEnable() {
        GuiGlobals.getInstance().requestCursorEnabled(gui);
    }

    @Override
    protected void onDisable() {
        GuiGlobals.getInstance().releaseCursorEnabled(gui);
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
                logger.info("Offsetting camera by {}", camOffset);
                camera.setLocation(oldCam.add(camOffset.x, camOffset.y, 0f));
            }
            camOffset = Vector2f.ZERO.clone();
            boundsMin = Vector2f.ZERO.clone();
            boundsMax = new Vector2f(level.getWidth() - 1f, level.getHeight() - 1f);
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
                    //logger.info("No tile data at {} in editor", editCell);
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

    private void saveLevel(String name) {
        try {
            Mapper.getMapper().writeValue(new File(name + ".json"), level);
        } catch (IOException e) {
            logger.error("Could not save level {}.json", name, e);
        }
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
}
