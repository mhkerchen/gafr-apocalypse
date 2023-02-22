//https://rhea.sockette.net:5050/~kerch22m/WWW/proj1/

import GaFr.GFGame;
import GaFr.GFStamp;
import GaFr.GFFont;
import GaFr.Gfx;
import GaFr.GFU;
import GaFr.GFTexture;
import GaFr.GFKey;

import java.util.*;
import java.io.*;
import java.lang.Integer;
import java.lang.Math;

public class Game extends GFGame
{
  {
    Gfx.clearColor(Gfx.Color.BLACK);
  }

  /**
  16-size tile:
  gridHeight = 28
  Gridwidth = 50
  GFStamp[][] tileImages = new GFTexture("assets/tileset.png").splitIntoTilesBySize2D(16,16);
  
  32-size tile:
  gridHeight = 15
  gridWidth = 24
  GFStamp[][] tileImages = new GFTexture("assets/tileset_large.png").splitIntoTilesBySize2D(32,32);
   */

  // variables
    // fonts
    static GFFont englishFont;

    // text
    static TextBox dialogueBox;

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

    // static variables
    public static int charX = 1;
    public static int charY = 1;
    public static int charImg = 0;
    public static int charDir = 0;

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

    static boolean isDialogue = false;


    // Load graphics

    static GFStamp[][] tileImages = new GFTexture("assets/images/tileset.png").splitIntoTilesBySize2D(32,32);
    static GFStamp[] textures;// = new GFStamp[TEXTURES_QTY];

    static GFStamp[] playerImgs;// = new GFStamp[16];

    static GaFrHash<String, Integer> tileDict = new GaFrHash<String, Integer>();

  // Initialization
  {
    for (GFStamp s: new GFU.Iter2D<GFStamp>(tileImages)) {
      s.centerPin();
    }
    
    
    Fog.initFog();

    try {
      textures = indexTextures("assets/image_indexes/tiles", tileImages, TEXTURES_QTY);
    } catch (Exception e ) {
      System.out.println("Failure to load tile textures :(");
      errorScreen();
    }
    try {
      playerImgs = indexTextures("assets/image_indexes/character", tileImages, 16);
    } catch (Exception e ) {
      System.out.println("Failure to load player textures :(");
      errorScreen();
    }

    editModeTiles = getValidTiles(textures);

    Prop.readImpassableProps();

  // Loads the level.
  try {
    loadLevel("assets/maps/dungeon_map");
  } catch (Exception e) {
    System.out.println("Level load failed.");
    errorScreen();
  }

  initFonts();
    
  dialogueBox = new TextBox(0, 400, 800-16, 100-16, englishFont);
    
  //alienDialogueBox = new TextBox(0, 400, 800-16, 100-16, englishFont);
  //dialogueBox.addMultipleLines("You look at the engraving. You can't figure out what it says.");


  }

  static void initFonts() {
    // initialize the alien text
      GFStamp[] glyphs;/* = (new GFTexture("assets/fonts/alientext_2x.png", 0xff000000, 0xffffffff)).splitIntoTilesBySize(20,24);
      alienFont = new GFFont(glyphs,
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ.!" );
      alienFont.collapseCase();*/
      glyphs = new GFTexture("assets/fonts/bittext.png", 0xff000000, 0xffffffff).splitIntoTilesBySize(12,22);
      englishFont = new GFFont(glyphs,
        "abcdefghijklmnopqrstuvwxyz                          ?!.,-:()1234567890#*'^% --ABCDEFGHIJKLMNOPQRSTUVWXYZ" );
  }

  static void drawText(){

    dialogueBox.drawBox();
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

    String tilesText = GFU.loadTextFile(tile_filename+".txt");
    String[] tilesArray = Readers.splitFileNewline(tilesText);
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

        tileDict.put(currentLineSplit[0], arr[1]);
      }
    }

    return texturesArray;
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
      while (!currentProp.metadata.containsKey("spawn")) {
        propIndex++;
        currentProp = Prop.getProp(propIndex);
      }
      Game.setCharX(currentProp.getX());
      Game.setCharY(currentProp.getY());
      Fog.clearFog(charX, charY); // reveal the immediate area, for ~vibes~

  }

  public static void loadLevel(String filename, int spawnX, int spawnY) throws FileNotFoundException {

    loadLevelInternal(filename);

    Game.setCharX(spawnX);
    Game.setCharY(spawnY);
    Fog.clearFog(charX, charY);

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

  public static void setCharX(int val) {
    charX = val;
  }

  public static void setCharY(int val) {
    charY = val;
  }


  // Check if the player can move into a free space.
  static boolean canMove(int dx, int dy) { 
    if (((charX+dx) >= GRID_WIDTH) || ((charY+dy) >= GRID_HEIGHT) || ((charX+dx) < 0) || ((charY+dy) < 0)) {
      return false;
    } else if (isPassableXY(charX+dx, charY+dy)) {//(canPass(grid[charX+dx][charY+dy])) {
      return true;
      // TODO: check for an impassible prop
    }
    return false;
  }

  // Attempt to move into a free space.
  static boolean tryMove(int dx, int dy) {
    if (canMove(dx, dy)) {
      movePlayer(dx, dy);
      return true;
    }
    return false;
  }

  // Force moves the player.
  static void movePlayer(int dx, int dy) {
      charX += dx;
      charY += dy;
      Fog.clearFog(charX, charY);

  }

  // Attempt to perform an (automatic) action.
  static boolean tryAction(int x, int y) {
    if (propsMap[x][y] == 0) {
      return false;
    }
    return Prop.props[propsMap[x][y]].tryOverlapAction();
  }

  /*
  Attempt to perform a manual action
  When the player hits the touch action button (default Space and/or Enter) check for and execute any
  touch actions. 
  It only checks the prop in the direction where you're facing. 
  */
  public static void touchAction() {
    // on tile:
    if ((propsMap[charX][charY] != 0)) {
      Prop.props[propsMap[charX][charY]].tryTouchAction();
    } 
    // above:
    else if ( (charDir==0) && (propsMap[charX][charY-1] != 0)) {
      Prop.props[propsMap[charX][charY-1]].tryTouchAction();
    } 
    // below:
    else if ( (charDir==2) && (propsMap[charX][charY+1] != 0)) {
      Prop.props[propsMap[charX][charY+1]].tryTouchAction();
    } 
    // right:
    else if ( (charDir==1) && (propsMap[charX+1][charY] != 0)) {
      Prop.props[propsMap[charX+1][charY]].tryTouchAction();
    } 
    // left:
    else if ( (charDir==3) && (propsMap[charX-1][charY] != 0)) {
      Prop.props[propsMap[charX-1][charY]].tryTouchAction();
    } 
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


  public static void faceChar(String dir) {
    if (dir.equals("up")) {
      charDir = 0;
      charImg = 4;
    } else if (dir.equals("right")) {
      charDir = 1;
      charImg = 8;
    } else if (dir.equals("down")) {
      charDir = 2;
      charImg = 0;
    } else {
      charDir = 3;
      charImg = 12;
    }
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
        if (!isDialogue) {
          faceChar("left");
          if (tryMove(-1,0)) {
            tryAction(charX, charY);
          }
        }
        break;
      }

      case GFKey.D:
      case GFKey.ArrowRight: {
        if (!isDialogue) {
          faceChar("right");
          if (tryMove(1,0)) {
            tryAction(charX, charY);
          }
        }
        break;
      }  
      
      case GFKey.W:
      case GFKey.ArrowUp: {
        if (!isDialogue) {
          faceChar("up");
          if (tryMove(0,-1)) {
            tryAction(charX, charY);
          }
        }
        break;
      }

      case GFKey.S:
      case GFKey.ArrowDown: {
        if (!isDialogue) {
          faceChar("down");
          if (tryMove(0,1)) {
            tryAction(charX, charY);
          }
        }
        break;
      }  

      case GFKey.P: {
        // P is for Print! This prints off some relevant information about your current position.
        System.out.println("current position: "+charX+","+charY);
        break;
      }  

      case GFKey.E: {
        // E is for Edit! 
        /**
          Edit Mode 
          Upon entering edit mode,
        
         */
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
            grid[charX][charY] = editModePlaceIcon;
          } else {
            editModeProps = editModeProps + "\n"+ editModePlaceIcon + ","+ charX +","+ charY +",{}";
            System.out.println(editModePlaceIcon + ","+ charX +","+ charY +",{}");
            propsMap[charX][charY] = editModePlaceIcon;
            editModeGhostProps.add(new Prop(1,editModePlaceIcon,charX,charY));
          }
        } else {
          // call a touch action
          if (!isDialogue) {
            touchAction();
          } 
          
        }

        dialogueBox.onInteract(); 
        

        // placement is important. if this statement is placed
        // before the freeze check, examine quotes become impassible.
        break;
      }

      case GFKey.BracketRight: {
        editModePlaceIconIndex++;
        if (editModePlaceIconIndex >= editModeTiles.length) {
          editModePlaceIconIndex = 0;
        }
        System.out.println(editModePlaceIconIndex);
        editModePlaceIcon = editModeTiles[editModePlaceIconIndex];
        break;
      }

      case GFKey.BracketLeft: {
        editModePlaceIconIndex--;
        if (editModePlaceIconIndex < 0) {
          editModePlaceIconIndex = editModeTiles.length-1;
        }
        System.out.println(editModePlaceIconIndex);
        editModePlaceIcon = editModeTiles[editModePlaceIconIndex];
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
      grid[charX][charY] = editModePlaceIcon;
    } else {
      editModeProps = editModeProps + "\n"+ editModePlaceIcon + ","+ charX +","+ charY +",{}";
      System.out.println(editModePlaceIcon + ","+ charX +","+ charY +",{}");
      propsMap[charX][charY] = editModePlaceIcon;
      editModeGhostProps.add(new Prop(1,editModePlaceIcon,charX,charY));
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
      player = playerImgs[charImg];
    }
    
    player.moveTo(edgeBuffer+charX*TILE_SIZE, edgeBuffer+charY*TILE_SIZE);
    player.stamp();

    // in edit mode, there is an additional white outline for the cursor
    if (editMode) {
      player = textures[9];
      player.moveTo(edgeBuffer+charX*TILE_SIZE, edgeBuffer+charY*TILE_SIZE);
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

  @Override
  public void onDraw (int frameCount)
  {
    if (beRightBack) {
      cryBabyScreen.moveTo(0,0);
      cryBabyScreen.stamp();
    } else {
      drawGrid();
      if (editMode) {
        drawGhostProps();
      } 
      drawProps();
      drawPlayer();
      drawText();
      if (frameCount%2 == 0) {
        dialogueBox.displayOneCharacter();
      }
    }

  }


  public static void errorScreen() {
    beRightBack = true;
  }

}