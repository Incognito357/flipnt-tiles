package com.incognito.acejam0.states;

import com.incognito.acejam0.Application;
import com.incognito.acejam0.domain.Level;
import com.incognito.acejam0.domain.Tile;
import com.incognito.acejam0.utils.Mapper;
import com.jme3.app.state.AppStateManager;
import com.jme3.scene.Node;
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
import java.util.List;

public class MapEditorState extends TypedBaseAppState<Application> {

    private static final Logger logger = LogManager.getLogger();

    private AppStateManager appStateManager;
    private Level level;
    private Node rootNode;
    private Node guiNode;
    private Container gui;
    private TextField txtLevelName;
    private TextField txtWidth;
    private TextField txtHeight;

    @Override
    protected void onInitialize(Application app) {
        appStateManager = app.getStateManager();
        rootNode = app.getRootNode();
        guiNode = app.getGuiNode();

        AppSettings settings = app.getContext().getSettings();
        gui = new Container(new SpringGridLayout());
        gui.setLocalTranslation(10, settings.getHeight() - 10f, 0f);
        //gui.setPreferredSize(new Vector3f(500, 600, 0));
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
        txtWidth = new TextField("");
        gui.addChild(txtWidth, 1);
        Button minusWidth = new Button("-");
        minusWidth.addClickCommand(btn -> {
            if (level.getWidth() > 1) {
                level = new Level(
                        level.getTitle(),
                        level.getWidth() - 1,
                        level.getHeight(),
                        shrinkWidth(level.getMap()),
                        shrinkWidth(level.getMap2()),
                        shrinkWidth(level.getState()),
                        level.getActions());
                appStateManager.getState(MapRendererState.class).setLevel(level);
                txtWidth.setText("" + level.getWidth());
            }
        });
        Button plusWidth = new Button("+");
        plusWidth.addClickCommand(btn -> {
            level = new Level(
                    level.getTitle(),
                    level.getWidth() + 1,
                    level.getHeight(),
                    increaseWidth(level.getMap()),
                    increaseWidth(level.getMap2()),
                    increaseWidth(level.getState()),
                    level.getActions());
            appStateManager.getState(MapRendererState.class).setLevel(level);
            txtWidth.setText("" + level.getWidth());
        });
        gui.addChild(minusWidth, 2);
        gui.addChild(plusWidth, 3);

        gui.addChild(new Label("Height"));
        txtHeight = new TextField("");
        gui.addChild(txtHeight, 1);

        Button minusHeight = new Button("-");
        minusHeight.addClickCommand(btn -> {
            if (level.getHeight() > 1) {
                shrinkHeight(level.getState());
                level = new Level(
                        level.getTitle(),
                        level.getWidth(),
                        level.getHeight() - 1,
                        shrinkHeight(level.getMap()),
                        shrinkHeight(level.getMap2()),
                        level.getState(),
                        level.getActions());
                appStateManager.getState(MapRendererState.class).setLevel(level);
                txtHeight.setText("" + level.getHeight());
            }
        });
        Button plusHeight = new Button("+");
        plusHeight.addClickCommand(btn -> {
            increaseHeight(level.getState());
            level = new Level(
                    level.getTitle(),
                    level.getWidth(),
                    level.getHeight() + 1,
                    increaseHeight(level.getMap()),
                    increaseHeight(level.getMap2()),
                    level.getState(),
                    level.getActions());
            appStateManager.getState(MapRendererState.class).setLevel(level);
            txtHeight.setText("" + level.getHeight());
        });
        gui.addChild(minusHeight, 2);
        gui.addChild(plusHeight, 3);
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
        if (level != null) {
            appStateManager.getState(MapRendererState.class).setLevel(level);
            txtWidth.setText("" + level.getWidth());
            txtHeight.setText("" + level.getHeight());
        }
    }

    private void saveLevel(String name) {
        try {
            Mapper.getMapper().writeValue(new File(name + ".json"), level);
        } catch (IOException e) {
            logger.error("Could not save level {}.json", name, e);
        }
    }

    private List<Tile> shrinkWidth(List<Tile> tiles) {
        List<Tile> newTiles = new ArrayList<>(tiles);
        for (int i = newTiles.size() - 1; i > 0; i -= level.getWidth()) {
            newTiles.remove(i);
        }
        return newTiles;
    }

    private List<Tile> increaseWidth(List<Tile> tiles) {
        List<Tile> newTiles = new ArrayList<>(tiles);
        for (int i = level.getWidth(); i < newTiles.size(); i += level.getWidth() + 1) {
            newTiles.add(i, Tile.FLOOR);
        }
        return newTiles;
    }

    private List<Tile> shrinkHeight(List<Tile> tiles) {
        List<Tile> newTiles = new ArrayList<>(tiles);
        newTiles.subList(newTiles.size() - level.getWidth() + 1, newTiles.size()).clear();
        return newTiles;
    }

    private List<Tile> increaseHeight(List<Tile> tiles) {
        List<Tile> newTiles = new ArrayList<>(tiles);
        for (int i = 0; i < level.getWidth(); i++) {
            newTiles.add(Tile.FLOOR);
        }
        return newTiles;
    }

    private BitSet shrinkWidth(BitSet set) {
        BitSet b = new BitSet();
        for (int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i+1)) {
            int x = i % level.getWidth();
            int y = i / level.getHeight();
            if (x < level.getWidth() - 1) {
                b.set(y * (level.getWidth() - 1) + x);
            }
            if (i == Integer.MAX_VALUE) {
                break; // or (i+1) would overflow
            }
        }
        return b;
    }

    private BitSet increaseWidth(BitSet set) {
        BitSet b = new BitSet();
        for (int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i+1)) {
            int x = i % level.getWidth();
            int y = i / level.getHeight();
            b.set(y * (level.getWidth() + 1) + x);
            if (i == Integer.MAX_VALUE) {
                break; // or (i+1) would overflow
            }
        }
        for (int y = 0; y < level.getHeight(); y++) {
            b.set(y * (level.getWidth() + 1) + level.getWidth() - 1);
        }
        return b;
    }

    private void shrinkHeight(BitSet set) {
        set.clear(level.getWidth() * (level.getHeight() - 1), level.getWidth() * level.getHeight());
    }

    private void increaseHeight(BitSet set) {
        set.set(level.getWidth() * level.getHeight(), level.getWidth() * (level.getHeight() + 1));
    }
}
