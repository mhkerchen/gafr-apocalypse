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

    // Edit mode variables
    static boolean editMode = false;
    static String editModePlaceType = "tile"; // can be "tile", "prop".
    static int editModePlaceIcon = 1; // can be any valid index in textures.
    static int editModePlaceIconIndex = 0; // so sorry. i promise five words is the max for my variable names
    static String editModeProps = "";
    static ArrayList<Prop> editModeGhostProps = new ArrayList<Prop>();
    static int[] editModeTiles;

    public static int totalMaps = 10; // used for fog of war
    public static int edgeBuffer = 16;

    // static variables and grids
    public static int grid[][] = new int[GRID_WIDTH][GRID_HEIGHT];
    public static int[][] propsMap; // used for checking collisions. Allows accessing ID by position.
    
    // for fog of war saving and loading
    public static String currentMapFilename = "assets/maps/dungeon_map";

    // error screen
    static boolean beRightBack = false;
    static GFStamp cryBabyScreen = new GFStamp("assets/images/technical_difficulties.png");


    // Load graphics

    static GFStamp[][] tileImages = new GFTexture("assets/images/tileset.png").splitIntoTilesBySize2D(32,32);
    static GFStamp[] textures;// = new GFStamp[TEXTURES_QTY];
    static GaFrHash<String, Integer> tileDict = new GaFrHash<String, Integer>();

    static Animation lightBlink;


    // soundt
/*
  static void initSounds() {
    // all sounds are free from pixabay except the bgm
    sounds.put("start_dialogue", new GFSound("assets/sounds/dialogue_go.mp3"));
    sounds.put("door_open", new GFSound("assets/sounds/doornoise.mp3"));
    sounds.put("lever_pull", new GFSound("assets/sounds/lever.mp3"));
    sounds.put("footstep", new GFSound("assets/sounds/step.mp3"));
    // bgm from CoAg on youtube
    sounds.put("bgm", new GFSound("assets/sounds/letting_go_coag.mp3"));
  }

  static void playSound(String name) {
    sounds.get(name).play();
  }*/

  // Initialization
  {
    for (GFStamp s: new GFU.Iter2D<GFStamp>(tileImages)) {
      s.centerPin();
    }
    
    Player.p = new Player();
    Fog.initFog();
    Sfx.BGM.play();

    try { // initializes textures and tileDict
      textures = indexTextures("assets/image_indexes/tiles", tileImages, TEXTURES_QTY);
    } catch (Exception e ) {
      System.out.println("Failure to load tile textures :(");
      errorScreen();
    }
    try {
      Player.p.playerImgs = indexTextures("assets/image_indexes/character", tileImages, 16);
    } catch (Exception e ) {
      System.out.println("Failure to load player textures :(");
      errorScreen();
    }

    try { // must run after tileDict is initialized
      Animation.initializeAnimations();
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    editModeTiles = getValidTiles(textures);
    Prop.readImpassableProps();
    Prop.initializeProps();
  // Loads the level.
  try {
    loadLevel("assets/maps/dungeon_map");
  } catch (Exception e) {
    System.out.println("Level load failed.");
    errorScreen();
  }

  TextBox.initFonts();
    
  TextBox.dialogueBox = new TextBox(0, 400, 800-16, 100-16, TextBox.englishFont);
  }



  static int[] getValidTiles(GFStamp[] tileArray) {

    ArrayList<Integer> validIndexes = new ArrayList<Integer>();
    for (int i = 0; i < tileArray.length; i++) {
      if (tileArray[i] != null) {
        validIndexes.add(i);
      }
    }
    // there is a better way to do this but I Will Not Learn
    int[] returnArray = new int[validIndexes.size()];
    for (int i = 0; i < validIndexes.size(); i++) {
      returnArray[i] = validIndexes.get(i);
    }
    return returnArray;
  }

  
  static GFStamp[] indexTextures(String tile_filename, GFStamp[][] imageMatrix, int destArraySize) throws FileNotFoundException {

    String[] tilesArray = Readers.splitFileNewline(GFU.loadTextFile(tile_filename+".txt"));
    String[] currentLineSplit;
	  int[] arr;
    String currentLine;
    GFStamp[] texturesArray = new GFStamp[destArraySize];

    for (int i =0; i < tilesArray.length; i++) {
      currentLine = tilesArray[i].trim();

      if (Readers.lineValid(currentLine)) {
        currentLineSplit = Readers.splitLineStr(currentLine);
		    arr = Readers.strToInt(currentLineSplit,1);
        
        texturesArray[arr[1]] = imageMatrix[arr[2]][arr[3]];
        System.out.println(currentLineSplit[0]);
        tileDict.put(currentLineSplit[0].trim(), arr[1]);
      }
    }

    return texturesArray;
  }
	
  // Translates a String tilename into an integer value.
  static int translate(String tile) {
    if (tileDict.size() == 0) {
      System.out.println("Error: tileDict is empty. Has it been initialized?");
    } else {
      if (tileDict.containsKey(tile)) {
        return tileDict.get(tile);
      } else {
        System.out.println("Error: Could not find \""+tile+"\" in tileDict.");
      }
    }
    return 0;
  }

  // Overload -- this one's much simpler
  static int translate(int tile) {
    return tile;
  }

  // Given a text file string containing the map layout, constructs the tiles. (Does not include interactibles.)
  static int[][] readInGameMap(String mapString) {
    
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
    mapgridArray = mapString.split("\n");

    // Will contain a bunch of entries of numbers as strings
    String lineArray[] = new String[GRID_WIDTH];

    // Add each map value into the map grid, parsing them into integers from strings.
    for (int y = 0; y < mapHeight; y++) {

      lineArray = mapgridArray[y].split(",");

      for (int x = 0; x < mapWidth; x++) {

        mapGrid[x][y] = Integer.parseInt(lineArray[x].trim()); // whitespace issue wasted 2 hours :(
        
      }
    }

    return mapGrid;
  }

  // Determine whether a block is impassable.
  static boolean canPass(int tile) {
    if (editMode == true) {  // you can pass through everything in edit mode
      return true;
    }
    else if  ( (tile == tileDict.get("WALL")) || (tile == tileDict.get("WALL2")))  {  // the list right now is very short.
      return false;
    }
    return true;
  }




  // Loads a new level, looking for a spawn entry.
  public static void loadLevel(String filename) throws FileNotFoundException {
      
      loadLevelInternal(filename);

      // find the Spawn entry (usually but not always the first object) and spawn there
      Prop currentProp = Prop.getProp(0);
      int propIndex = 0;
      // TODO: error if there's no such prop
      while ((!currentProp.metadata.containsKey("spawn"))) {
        propIndex++;
        currentProp = Prop.getProp(propIndex);
      }
      Player.p.setCharX(currentProp.getX());
      Player.p.setCharY(currentProp.getY());
      Fog.clearFog(Player.p.x, Player.p.y); // reveal the immediate area, for ~vibes~

  }

  public static void loadLevel(String filename, int spawnX, int spawnY) throws FileNotFoundException {

    loadLevelInternal(filename);

    Player.p.setCharX(spawnX);
    Player.p.setCharY(spawnY);
    Fog.clearFog(Player.p.x, Player.p.y);

  }

  public static void loadLevelInternal(String filename) throws FileNotFoundException {
      
      Fog.loadFog(filename);
    
      propsMap = new int[GRID_WIDTH][GRID_HEIGHT];
      
      currentMapFilename = filename;

      // load in maps
      System.out.println("Load ingame map");
      grid = readInGameMap(GFU.loadTextFile(filename+".txt"));
      System.out.println("Load prop map");
      Prop.props = Prop.readProps(GFU.loadTextFile(filename+"_props.txt"));
  }

  // Determines whether a tile at x,y is passable.
  // Returns false if it is not passable, or if it is invalid.
  public static boolean isPassableXY(int x, int y) {
    if (isValidTile(x,y)) {
      if (canPass(grid[x][y])) {
        if (propsMap[x][y] == 0 ) {
          return true;
        } else {
          return Prop.props[propsMap[x][y]].canPass();
        }
      }
    }
    return false;
  }




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
      tilesString = tilesString + "\n";
    }
    return tilesString;
  }

  // I/O FUNCTIONS

  public void onKeyDown(String key, int code, int flags) {
  switch(code) {
      case GFKey.A:
      case GFKey.ArrowLeft: {
        Player.p.goDir("left");
        break;
      }

      case GFKey.D:
      case GFKey.ArrowRight: {
        Player.p.goDir("right");
        break;
      }  
      
      case GFKey.W:
      case GFKey.ArrowUp: {
        Player.p.goDir("up");
        break;
      }

      case GFKey.S:
      case GFKey.ArrowDown: {
        Player.p.goDir("down");
        break;
      }  

      case GFKey.P: {
        System.out.println("current position: "+Player.p.x+","+Player.p.y);
        break;
      }  

      case GFKey.E: {
        // E is for Edit! 
         if (editMode) {
          System.out.println("Props:");
          System.out.println(editModeProps);
          System.out.println("Tiles:");
          System.out.println(tilesToString());
          System.out.println("Exiting edit mode.");
          
          Gfx.clearColor(Gfx.Color.BLACK);
          editMode = false;

         } else {
          System.out.println("Entering edit mode.\n\nX: swap between tile [turquoise] and prop [fuschia] mode. \nN: clear map. \nEnter: place a tile or prop.\n[]: change tile.");
          
          editModeGhostProps.clear();
          Gfx.clearColor(0xffbd9b20);
          editModePlaceType = "tile";
          editMode = true;
         }
        break;
      }  

      case GFKey.N: {
        // Clears the level and makes a new blank one for editing, IF in edit mode.
        if (editMode) {
          try {
            loadLevel("assets/maps/newmap", 1, 1);
            editModeProps = "";
            editModeGhostProps.clear();
          } catch (Exception e) {
            System.out.println("Failed to clear map.");
          }
          
          
        }
        break;
      }  

      case GFKey.Space:
      case GFKey.Enter: {
        if (editMode) {
          if (editModePlaceType.equals("tile") ) {
            grid[Player.p.x][Player.p.y] = editModePlaceIcon;
          } else {
            editModeProps = editModeProps + "\n"+ editModePlaceIcon + ","+ Player.p.x +","+ Player.p.y +",{}";
            System.out.println(editModePlaceIcon + ","+ Player.p.x +","+ Player.p.y +",{}");
            propsMap[Player.p.x][Player.p.y] = editModePlaceIcon;
            editModeGhostProps.add(new Prop(1,editModePlaceIcon,Player.p.x,Player.p.y));
          }
        } else {
          // call a touch action
          if (!TextBox.isDialogue) {
            Player.p.touchAction();
          } 
          
        }

        TextBox.dialogueBox.onInteract(); 
        // placement is important. if this statement is placed
        // before the freeze check, examine quotes become impassible.
        

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

  public static void editModePlaceTile() {
    if (editModePlaceType.equals("tile") ) {
      grid[Player.p.x][Player.p.y] = editModePlaceIcon;
    } else {
      editModeProps = editModeProps + "\n"+ editModePlaceIcon + ","+ Player.p.x +","+ Player.p.y +",{}";
      System.out.println(editModePlaceIcon + ","+ Player.p.x +","+ Player.p.y +",{}");
      propsMap[Player.p.x][Player.p.y] = editModePlaceIcon;
      editModeGhostProps.add(new Prop(1,editModePlaceIcon,Player.p.x,Player.p.y));
    }
  }

  public static void editModeChangeIcon(int delta) {
    
        editModePlaceIconIndex += delta;
        if (editModePlaceIconIndex >= editModeTiles.length) {
          editModePlaceIconIndex = 0;
        }
        if (editModePlaceIconIndex < 0) {
          editModePlaceIconIndex = editModeTiles.length-1;
        }
        editModePlaceIcon = editModeTiles[editModePlaceIconIndex];
  }




  // DRAW FUNCTIONS

  void drawGrid()
  {
    for (int y = 0; y < GRID_HEIGHT; y++) {
      for (int x = 0; x < GRID_WIDTH; x++) {
        if ((editMode) || (Fog.fogOfWar[x][y] == 1)) {
          GFStamp s = textures[grid[x][y]];
          s.moveTo(edgeBuffer+x*TILE_SIZE, edgeBuffer+y*TILE_SIZE);
          s.stamp();
        }
      }
    }
  }

  // Draws all ghost props. 
  void drawGhostProps() {
    if (editMode) {
      for (int i = 0; i < editModeGhostProps.toArray().length; i++) {
          GFStamp s = textures[editModeGhostProps.get(i).getIcon()];
          s.moveTo(edgeBuffer+editModeGhostProps.get(i).getX()*TILE_SIZE, edgeBuffer+editModeGhostProps.get(i).getY()*TILE_SIZE);
          s.stamp();
          
      }
    }
  }


  void drawPlayer() {

    // determine the proper sprite for the player (animated or the currently selected block in edit mode)
    GFStamp player;
    if (editMode) {
      player = textures[editModePlaceIcon];
    } else {
      player = Player.p.playerImgs[Player.p.img];
    }
    
    player.moveTo(edgeBuffer+Player.p.x*TILE_SIZE, edgeBuffer+Player.p.y*TILE_SIZE);
    player.stamp();

    // in edit mode, there is an additional white outline for the cursor
    if (editMode) {
      player = textures[9];
      player.moveTo(edgeBuffer+Player.p.x*TILE_SIZE, edgeBuffer+Player.p.y*TILE_SIZE);
      player.stamp();
    }

  }


  // Draws all preexisting props. 
  public static void drawProps() {
    for (int i = 0; i < Prop.props.length; i++) {
      if ( (Prop.props[i].exists) && ( (editMode) || (Fog.fogOfWar[Prop.props[i].getX()][Prop.props[i].getY()] == 1)) ) {

        GFStamp s = textures[Prop.props[i].getIcon()];
        s.moveTo(edgeBuffer+Prop.props[i].getX()*TILE_SIZE, edgeBuffer+Prop.props[i].getY()*TILE_SIZE);
        s.stamp();
        
      }
    }
  }

  
  static void drawText(){
    TextBox.dialogueBox.drawBox();
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
      if (frameCount%2 == 0) {
        TextBox.dialogueBox.displayOneCharacter();
      }
    }

  }


  public static void errorScreen() {
    beRightBack = true;
  }

}