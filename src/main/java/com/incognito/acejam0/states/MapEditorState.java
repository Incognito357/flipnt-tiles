package com.incognito.acejam0.states;

import com.incognito.acejam0.Application;
import com.incognito.acejam0.domain.Level;
import com.incognito.acejam0.domain.Tile;
import com.incognito.acejam0.utils.GlobalMaterials;
import com.incognito.acejam0.utils.Mapper;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
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
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapEditorState extends TypedBaseAppState<Application> {

    private static final Logger logger = LogManager.getLogger();

    private AppStateManager appStateManager;
    private InputManager inputManager;

    private Level level;
    private Node rootNode;
    private Node guiNode;
    private Container gui;
    private TextField txtLevelName;
    private Label lblWidth;
    private Label lblHeight;
    private Label lblMouse;

    private record TileInfo(Tile a, Tile b, boolean enabled) {}

    private final Map<Vector2f, TileInfo> tiles = new HashMap<>();
    private Vector2f boundsMin = Vector2f.ZERO.clone();
    private Vector2f boundsMax = Vector2f.ZERO.clone();
    private Vector2f mouseCell = Vector2f.ZERO.clone();

    private final Geometry cursor = new Geometry("", new WireBox(0.5f, 0.5f, 0f));

    private final ActionListener mouseListener = (name, isPressed, tpf) -> {
        if (isPressed) {
            logger.info("Adding tile to {}", mouseCell);
            tiles.put(mouseCell, new TileInfo(Tile.FLOOR, Tile.FLOOR, false));
            if (mouseCell.x < boundsMin.x) {
                logger.info("Decreased x bounds from {} to {}", boundsMin.x, mouseCell.x);
                boundsMin.x = mouseCell.x;
            } else if (mouseCell.x > boundsMax.x) {
                logger.info("Increased x bounds from {} to {}", boundsMax.x, mouseCell.x);
                boundsMax.x = mouseCell.x;
            }
            if (mouseCell.y < boundsMin.y) {
                logger.info("Decreased y bounds from {} to {}", boundsMin.y, mouseCell.y);
                boundsMin.y = mouseCell.y;
            } else if (mouseCell.y > boundsMax.y) {
                logger.info("Increased y bounds from {} to {}", boundsMax.y, mouseCell.y);
                boundsMax.y = mouseCell.y;
            }
            updateEditorLevel();
        }
    };

    @Override
    protected void onInitialize(Application app) {
        appStateManager = app.getStateManager();
        inputManager = app.getInputManager();
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

        //Node separator = new Node();
        //separator.attachChild(new Geometry("", new Line(Vector3f.ZERO, Vector3f.UNIT_X.mult(10f))));
        //gui.addChild(separator);

        gui.addChild(new Label("Width"));
        lblWidth = new Label("");
        gui.addChild(lblWidth, 1);

        gui.addChild(new Label("Height"));
        lblHeight = new Label("");
        gui.addChild(lblHeight, 1);

        cursor.setMaterial(GlobalMaterials.getDebugMaterial(ColorRGBA.Yellow));

        rootNode.attachChild(cursor);

        inputManager.addMapping("editor-tile", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        inputManager.addListener(mouseListener, "editor-tile");
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
        syncLevel();
    }

    private void syncLevel() {
        if (level != null) {
            appStateManager.getState(MapRendererState.class).setLevel(level);
            boundsMin = Vector2f.ZERO.clone();
            boundsMax = new Vector2f(level.getWidth() - 1, level.getHeight() - 1);
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
        for (int editY = (int)boundsMin.y; editY <= boundsMax.y; editY++) {
            int x = 0;
            for (int editX = (int)boundsMin.x; editX <= boundsMax.x; editX++) {
                Vector2f editCell = new Vector2f(editX, editY);
                TileInfo tileInfo = tiles.get(editCell);
                logger.info("Adding editor {} to map ({}, {})", editCell, x, y);
                if (tileInfo == null) {
                    logger.info("No tile data at {} in editor", editCell);
                    map1.add(Tile.EMPTY);
                    map2.add(Tile.EMPTY);
                } else {
                    map1.add(tileInfo.a);
                    map2.add(tileInfo.b);
                    state.set(y * ((int)size.x + 1) + x, tileInfo.enabled);
                }
                x++;
            }
            y++;
        }
        level = new Level(
                txtLevelName.getText(),
                (int)size.x + 1,
                (int)size.y + 1,
                map1,
                map2,
                state,
                Map.of());
        syncLevel();
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
        Camera cam = getApplication().getCamera();
        float depth = cam.getViewToProjectionZ(cam.getLocation().z);
        Vector2f click2d = inputManager.getCursorPosition();
        Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), depth);
        Vector2f size = boundsMax.subtract(boundsMin);
        int x = (int)FastMath.floor(click3d.x + 0.5f);
        int y = (int)FastMath.floor(click3d.y + 0.5f);
        cursor.setLocalTranslation(x, y, 0.01f);

        mouseCell = new Vector2f(x, -y).addLocal(size.divide(2));
        mouseCell.x = FastMath.floor(mouseCell.x);
        mouseCell.y = FastMath.floor(mouseCell.y);
    }
}
