//https://rhea.sockette.net:5050/~kerch22m/WWW/proj1/

import GaFr.GFGame;
import GaFr.GFStamp;
import GaFr.Gfx;
import GaFr.GFU;
import GaFr.GFTexture;
import GaFr.GFKey;
import GaFr.GFSound;

import java.util.*;
import java.io.*;
import java.lang.Integer;
import java.lang.Math;

public class Game extends GFGame
{
  {
    Gfx.clearColor(Gfx.Color.BLACK);
  }

  // variables

    // constants
    static final int GRID_HEIGHT = 15;
    static final int GRID_WIDTH = 24;
    static final int TILE_SIZE = 32;
    static final int TEXTURES_QTY = 128;
    static final int EDGE_BUFFER = 24;

    // Edit mode variables
    static boolean editMode = false;
    static String editModePlaceType = "tile"; // can be "tile", "prop".
    static int editModePlaceIcon = 1; // can be any valid ID in textures.
    static int editModePlaceIconIndex = 0; // the index of the ID in editModeTiles
    static String editModeProps = "";
    static ArrayList<Prop> editModeGhostProps = new ArrayList<Prop>();
    static int[] editModeTiles; // all valid tiles, as cycled through in editmode
    // static variables and grids
    public static int grid[][] = new int[GRID_WIDTH][GRID_HEIGHT];
    

    // error screen
    static boolean beRightBack = false;
    static GFStamp cryBabyScreen = new GFStamp("assets/images/technical_difficulties.png");


    // Load graphics

    static GFStamp[][] tileImages = new GFTexture("assets/images/tileset.png").splitIntoTilesBySize2D(32,32);
    static GaFrHash<Integer, GFStamp> textures = new GaFrHash<Integer, GFStamp>();
    static GaFrHash<String, Integer> tileDict = new GaFrHash<String, Integer>();


  // Initialization
  {
    for (GFStamp s: new GFU.Iter2D<GFStamp>(tileImages)) {
      s.centerPin();
    }
    
    indexTextures("assets/data/tiles", tileImages); // initializes tileDict

    Sfx.BGM.play();

    Player.initPlayers();

    Animation.initializeAnimations(); // depends on tileDict being initialized
    
    getValidTiles(textures);

    Prop.initializeProps();
    
    loadLevel("assets/maps/hub");

    TextBox.initText();

  }


  // Gets a list of all valid tiles for edit mode.
  static void getValidTiles(GaFrHash<Integer, GFStamp> tileArray) {

    ArrayList<Integer> keyArr = tileArray.getKeyArray();
    editModeTiles = new int[keyArr.size()];
    for (int i = 0; i < keyArr.size(); i++) {
      editModeTiles[i] = keyArr.get(i);
    }
    
  }

  /* 
    Reads textures from tiles.txt into textures and tileDict. 
  */
  static void indexTextures(String tile_filename, GFStamp[][] imageMatrix) {
    String[] tilesArray;
    try {
      tilesArray = Reader.splitFileNewline(GFU.loadTextFile(tile_filename+".txt"));
    } catch (Exception e ){
      System.out.println("Error reading file "+tile_filename+".txt into textures.");
      errorScreen();
      tilesArray = new String[0];
    }
    
    String[] currentLineSplit;
	  int[] arrs;

    for (int i =0; i < tilesArray.length; i++) {
      currentLineSplit = Reader.splitLineStr(tilesArray[i].trim());

      if (Reader.assertArgs(currentLineSplit, 4)) {
        
        arrs = Reader.strToInt(currentLineSplit,1);
        addTile(arrs[1], currentLineSplit[0], imageMatrix[arrs[2]][arrs[3]]);

      }

    }

  }


  static void addTile(int id, String name, GFStamp stamp) {
    textures.put(id, stamp);
    tileDict.put(name, id);
  }
	
  // Translates a String tilename into an integer value.
  static int translate(String tile) {

    if (tileDict.containsKey(tile)) {
      return tileDict.get(tile);
    } else if (tileDict.size() == 0) {
      System.out.println("Error: tileDict is empty. Has it been initialized?");
    } else {
      System.out.println("Error: Could not find \""+tile+"\" in tileDict.");
    }
    
    return 69; // nice
  }
  static int translate(int tile) {
    return tile;
  }

  // Gets the stamp associated with an ID/name.
  static GFStamp getStamp(String tile) {
    return textures.get(translate(tile));
  }
  static GFStamp getStamp(int tile) {
    return textures.get(tile);
  }


  // Given a text file string containing the map layout, constructs the tiles. (Does not include interactibles.)
  static int[][] readInGameMap(String filename) {
    String mapString = "";
    try {
      mapString = GFU.loadTextFile(filename);
    } catch (Exception e ) {
      System.out.println("Could not load map "+filename);
    }

    // Obtain the width and height of the map.
    int mapHeight = mapString.split("\n").length;
    int mapWidth = mapString.split("\n")[0].split(",").length; // Not configured for nonrectangular maps. Do not make.

    if ((mapHeight > GRID_HEIGHT) || (mapWidth > GRID_WIDTH)) {  // map too big for grid
      System.out.println("Error: Dimension of map exceeds play area");
      errorScreen();
    }

    int mapGrid[][] = new int[GRID_WIDTH][GRID_HEIGHT];

    // Splits the map string into an array of rows
    // Each row is a string that is GRID_WIDTH long.
    String mapgridArray[] = new String[GRID_HEIGHT];
    mapgridArray = Reader.splitFileNewline(mapString); // TODO change to reader

    // Will contain a bunch of entries of numbers as strings
    String lineArray[] = new String[GRID_WIDTH];

    // Add each map value into the map grid, parsing them into integers from strings.
    for (int y = 0; y < mapHeight; y++) {

      lineArray = Reader.splitLineStr(mapgridArray[y]);

      for (int x = 0; x < mapWidth; x++) {

        mapGrid[x][y] = Integer.parseInt(lineArray[x]); // whitespace issue wasted 2 hours :(
        
      }
    }

    return mapGrid;
  }

  // Determine whether a block type is impassable.
  static boolean canPass(int tile) {
    if (editMode == true) {  // you can pass through everything in edit mode
      return true;
    }
    else if  ( Prop.impassableProps.contains(tile))  {
      return false;
    }
    return true;
  }

  // Determines whether a particular tile at x,y is passable.
  // Returns false if it is not passable, or if it is invalid.
  public static boolean isPassableXY(int x, int y) {
    if (isValidTile(x,y) && (canPass(grid[x][y]))) {
      if ( !Prop.isValid(x,y) ) {
        return true;
      } else {
        return Prop.propAt(x,y).canPass();
      }
    }
    return false;
  }





  // Loads a new level, looking for a spawn entry.
  public static void loadLevel(String filename) {
      
      loadLevelInternal(filename);

      // find the Spawn entry (usually but not always the first object) and spawn there
      Prop currentProp = Prop.getProp(0);
      int propIndex = 0;

      while ((!currentProp.metadata.containsKey("spawn"))) {
        propIndex++;
        currentProp = Prop.getProp(propIndex);
      }

      if (currentProp.metadata.containsKey("spawn") ) {
        spawnPlayer(currentProp.getX(), currentProp.getY());

      } else {
        System.out.println("Warning/error: no spawn given for file "+filename+".");
        spawnPlayer(0,0);
      }

  }

  public static void loadLevel(String filename, int spawnX, int spawnY) {

    loadLevelInternal(filename);
    spawnPlayer(spawnX, spawnY);

  }

  // Spawns the player in.
  public static void spawnPlayer(int x, int y) {

    Player.setPlayer("human");
    Player.cur.setCharX(x);
    Player.cur.setCharY(y);
    Fog.clearFog(Player.cur.x, Player.cur.y);

  }

  // Does the actual "loading of tiles" actions. 
  // Loads, in order: fog, tiles, props.
  public static void loadLevelInternal(String filename) {
      
      Fog.loadFog(filename);
      
      // load in maps
      System.out.println("Load ingame map");
      grid = readInGameMap(filename+".txt");
      System.out.println("Load props");
      Prop.readProps(filename+"_props.txt");

  }



  // Checks whether a given tile is valid
  public static boolean isValidTile(int x, int y) {
      if ( (x >= 0) && (y >= 0) && (x < GRID_WIDTH) && (y < GRID_HEIGHT) ) {
        return true;
      }
      return false;
  }


  public static String tilesToString() {
    String tilesString = "";
    for (int y=0; y < GRID_HEIGHT-1; y++) {
      for (int x=0; x < GRID_WIDTH-1; x++) {
        tilesString = tilesString + grid[x][y] + ",";
      }
      tilesString = tilesString.substring(0, tilesString.length()-2) + "\n";
    }
    return tilesString;
  }

  // I/O FUNCTIONS

  public void onKeyDown(String key, int code, int flags) {
  switch(code) {
      case GFKey.A:
      case GFKey.ArrowLeft: {
        Player.isPressed = "keyLeft";
        break;
      }

      case GFKey.D:
      case GFKey.ArrowRight: {
        Player.isPressed = "keyRight";
        break;
      }  
      
      case GFKey.W:
      case GFKey.ArrowUp: {
        Player.isPressed = "keyUp";
        break;
      }

      case GFKey.S:
      case GFKey.ArrowDown: 
      {
        Player.isPressed = "keyDown";
        break;
      }  

      case GFKey.P: {
        System.out.println("current position: "+Player.cur.x+","+Player.cur.y);
        break;
      }  

      case GFKey.E: {
        // E is for Edit! 
         if (editMode) {
          System.out.println("Props:"+"\n\n"+editModeProps+"\nNOTHING,3,3,{spawn:true}"+"\n\n"+"Tiles:"+"\n\n"+tilesToString()+"\n\n"+"Exiting edit mode.");
          Gfx.clearColor(Gfx.Color.BLACK);
          editModeGhostProps.clear();
          editMode = false;

         } else {
          System.out.println("Entering edit mode.\n\nX: swap between tile [turquoise] and prop [fuschia] mode. \nN: clear map. \nEnter: place a tile or prop.\n[]: change tile.");
          
          Gfx.clearColor(0xffbd9b20);
          editModePlaceType = "tile";
          editMode = true;

         }
        break;
      }  

      case GFKey.N: {
        // Clears the level and makes a new blank one for editing, IF in edit mode.
        if (editMode) {
          loadLevel("assets/maps/newmap", 1, 1);
          editModeProps = "";
          editModeGhostProps.clear();
          
        }
        break;
      }  

      case GFKey.K: {
        if (Player.cur == Player.human) {
          Player.setPlayer("robot");
        }
        else {
          Player.setPlayer("human");
        }
        break;
      }

      case GFKey.Space:
      case GFKey.Enter: {
        
        if (editMode) {
          // place an object
          editModePlaceTile();
        } else {
          
          if (!TextBox.isDialogue) {
            Player.cur.touchAction(); // call a touch action
          } 
          
        }

        TextBox.dialogueBox.onInteract(); 
        // placement is important. if this statement is placed
        // before the freeze check, examine quotes become impassable.
        break;
      }

      case GFKey.BracketRight: {
        editModeChangeIcon(1);
        break;
      }

      case GFKey.BracketLeft: {
        editModeChangeIcon(-1);
        break;
      }

      case GFKey.X: {
        if (editMode) {
          if (editModePlaceType.equals("tile") ) {
            editModePlaceType = "prop";
            Gfx.clearColor(0xffeb34eb);

          } else { 
            editModePlaceType = "tile";
            Gfx.clearColor(0xffbd9b20);
            
            }
        }
        break;
      }
    }
  }

  @Override
  public void onKeyUp(String key, int code, int flags) {
    switch(code) {
      case GFKey.A:
      case GFKey.ArrowLeft: {
        if (Player.isPressed.equals("keyLeft")) {
          Player.isPressed = "none";
          Player.keyTimeout = Player.INITIAL_TIMEOUT;
        }
        break;
      }

      case GFKey.D:
      case GFKey.ArrowRight: {
        if (Player.isPressed.equals("keyRight")) {
          Player.isPressed = "none";
          Player.keyTimeout = Player.INITIAL_TIMEOUT;
        }
        break;
      }

      case GFKey.W:
      case GFKey.ArrowUp: {
        if (Player.isPressed.equals("keyUp")) {
          Player.isPressed = "none";
          Player.keyTimeout = Player.INITIAL_TIMEOUT;
        }
        break;
      }

      case GFKey.S:
      case GFKey.ArrowDown: {
        if (Player.isPressed.equals("keyDown")) {
          Player.isPressed = "none";
          Player.keyTimeout = Player.INITIAL_TIMEOUT;
        }
        break;
      }

    }
  }

  // Places a tile OR prop.
  public static void editModePlaceTile() {
    if (editModePlaceType.equals("tile") ) {
      grid[Player.cur.x][Player.cur.y] = editModePlaceIcon;
    } else {
      editModeProps = editModeProps + "\n"+ tileDict.getKey(editModePlaceIcon) + ","+ Player.cur.x +","+ Player.cur.y +",{}";
      editModeGhostProps.add(new Prop(1,editModePlaceIcon,Player.cur.x,Player.cur.y));
    }
  }

  // Cycle through the icons. 
  public static void editModeChangeIcon(int delta) {
    
        editModePlaceIconIndex += delta;
        if (editModePlaceIconIndex >= editModeTiles.length) {
          editModePlaceIconIndex = 0;
        }
        else if (editModePlaceIconIndex < 0) {
          editModePlaceIconIndex = editModeTiles.length-1;
        }
        editModePlaceIcon = editModeTiles[editModePlaceIconIndex];
  }




  // DRAW FUNCTIONS

  static void drawGrid()
  {
    GFStamp s;
    for (int y = 0; y < GRID_HEIGHT; y++) {
      for (int x = 0; x < GRID_WIDTH; x++) {
        if ((editMode) || (Fog.fogOfWar[x][y] == 1)) {
          s = textures.get(grid[x][y]);
          s.moveTo(EDGE_BUFFER + x * TILE_SIZE, EDGE_BUFFER + y * TILE_SIZE);
          s.stamp();
        }
      }
    }
  }

  // Draws all ghost props. 
  static void drawGhostProps() {
    if (editMode) {
      GFStamp s;
      for (int i = 0; i < editModeGhostProps.size(); i++) {
        s = textures.get(editModeGhostProps.get(i).getImg());
        s.moveTo(EDGE_BUFFER+editModeGhostProps.get(i).getX()*TILE_SIZE, EDGE_BUFFER+editModeGhostProps.get(i).getY()*TILE_SIZE);
        s.stamp();
      }
    }
  }


  static void drawPlayer() {

    GFStamp player;
    
    if (editMode) {
      // draw the selected tile
      player = textures.get(editModePlaceIcon);
      player.moveTo(EDGE_BUFFER+Player.human.x*TILE_SIZE, EDGE_BUFFER+Player.human.y*TILE_SIZE);
      player.stamp();

      // draw the white outline
      player = textures.get(translate("EDITMODE_CURSOR"));
      player.moveTo(EDGE_BUFFER+Player.human.x*TILE_SIZE, EDGE_BUFFER+Player.human.y*TILE_SIZE);
      player.stamp();

    } else {
      // draw the human
      player = Player.human.getImg();
      player.moveTo(EDGE_BUFFER+Player.human.x*TILE_SIZE, EDGE_BUFFER+Player.human.y*TILE_SIZE);
      player.stamp();
      if (Player.robot.show && Player.robot_unlocked) {
        player = Player.robot.getImg();
        player.moveTo(EDGE_BUFFER+Player.robot.x*TILE_SIZE, EDGE_BUFFER+Player.robot.y*TILE_SIZE);
        player.stamp();
      }
    }


  }


  // Draws all preexisting props. 
  public static void drawProps() {
    GFStamp s;
    for (int i = 0; i < Prop.props.length; i++) {
      // checks that the prop a) exists and b) should be shown
      Prop current = Prop.props[i];
      if ( (current.exists) && ( (editMode) || (Fog.fogOfWar[current.getX()][current.getY()] == 1)) ) {

        s = textures.get(current.getImg());
        s.moveTo(EDGE_BUFFER+current.getX()*TILE_SIZE, EDGE_BUFFER+current.getY()*TILE_SIZE);
        s.stamp();
        
      }
    }
  }

  public static void drawUI() {

    if (editMode) {
      TextBox.englishFont.draw(128+16,20, "selected: "+tileDict.getKey(editModePlaceIcon).toLowerCase());
    }
    
  }

  
  static void drawText(){
    TextBox.dialogueBox.drawBox();
  }

  @Override
  public void onUpdate() {
    Player.cur.pollMove();
  }

  @Override
  public void onDraw (int frameCount)
  {
    
    if ( ((frameCount) % (Sfx.bgmDuration*60)) == 0) {
      Sfx.BGM.play();
    }
    if (beRightBack) {
      cryBabyScreen.moveTo(0,0);
      cryBabyScreen.stamp();
    } else {
      drawGrid();
      if (editMode) {
        drawGhostProps();
      } 
      Animation.pollAnimations();
      drawProps();
      drawPlayer();
      drawText();
      drawUI();
      if (frameCount%2 == 0) {
        TextBox.dialogueBox.displayOneCharacter();
      }
    }

  }


  public static void errorScreen() {
    beRightBack = true;
  }

}