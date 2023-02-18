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
    static GFFont alienFont;
    static GFFont englishFont;

    // text
    static TextBox dialogueBox;

    // constants
    static final int GRID_HEIGHT = 15;
    static final int GRID_WIDTH = 24;
    static final int TILE_SIZE = 32;
    static final int TEXTURES_QTY = 64;
    static final int INVENTORY_SIZE = 6;

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

    public static int totalMaps = 10;
    public static int edgeBuffer = 16;

    public static ArrayList<String> playerInventory = new ArrayList<String>();

    // static variables and grids
    public static int grid[][] = new int[GRID_WIDTH][GRID_HEIGHT];
    public static int fogOfWar[][] = new int[GRID_WIDTH][GRID_HEIGHT];
    public static Prop[] props;
    public static int[][] propsMap; // used for checking collisions. Allows accessing ID by position.
    public static int[][][] fowHistory = new int[totalMaps][GRID_WIDTH][GRID_HEIGHT];
    
    // for fog of war saving and loading
    public static HashMap<String, Integer> mapIndexConnections = new HashMap<String, Integer>();
    public static String currentMapFilename = "assets/maps/dungeon_map";

    // error screen
    static boolean beRightBack = false;
    static GFStamp cryBabyScreen = new GFStamp("assets/images/technical_difficulties.png");


    // Load graphics

    GFStamp[][] tileImages = new GFTexture("assets/images/tileset_large.png").splitIntoTilesBySize2D(32,32);
    GFStamp[] textures;// = new GFStamp[TEXTURES_QTY];

    GFStamp[] playerImgs;// = new GFStamp[16];

  // Initialization
  {
    for (GFStamp s: new GFU.Iter2D<GFStamp>(tileImages)) {
      s.centerPin();
    }

    // i hate this code. get rid of it. burn it with fire
    mapIndexConnections.put("assets/maps/dungeon_map", 0);
    mapIndexConnections.put("assets/maps/dungeon_map_2", 1);
    mapIndexConnections.put("assets/maps/newmap", 2);

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

    

  // Loads the level.
  try {
    loadLevel("assets/maps/dungeon_map");
  } catch (Exception e) {
    System.out.println("Level load failed.");
    errorScreen();
  }

  initFonts();
    
  dialogueBox = new TextBox(0, 0, 800, 100, englishFont);
  dialogueBox.setTextSlowDisplay("You look at the engraving. You can't figure out what it says. There's a button beneath it. Press it? ^");

  }

  static void initFonts() {
    // initialize the alien text
      GFStamp[] glyphs = (new GFTexture("assets/fonts/alientext_2x.png", 0xff000000, 0xffffffff)).splitIntoTilesBySize(20,24);
      alienFont = new GFFont(glyphs,
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ.!" );
      alienFont.collapseCase();
      glyphs = new GFTexture("assets/fonts/bittext_2x.png", 0xff000000, 0xffffffff).splitIntoTilesBySize(12,22);
      englishFont = new GFFont(glyphs,
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz?!.,-:;()1234567890$#*'%^ " );
  }

  static void drawText(){

    //alienFont.draw(128+16,20, "WARNING!DO\nNOTGOBEYONDTHISPOINTIFYOUVALUEYOURLIFE");
    //englishFont.draw(200,50, "Hello World! This is a TEST FONT and a TEST!");
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
    String[] tilesArray = tilesText.split("\n");
    String currentLine;
    String[] currentLineSplit;

    GFStamp[] texturesArray = new GFStamp[destArraySize];
    for (int i =0; i < tilesArray.length; i++) {
      currentLine = tilesArray[i].trim();
      // skip blank lines or lines starting with a // (comments)
      
      // todo: the semicolons make me angry
      if (currentLine.isEmpty()) {
        ;
      } else if  (currentLine.charAt(0)=='/') {
        // reason for this: java will FREAK if i try to get charAt(0) of an empty string
        ;
      } else {
        currentLineSplit = currentLine.split(",");
        currentLineSplit[0] = currentLineSplit[0].trim();
        currentLineSplit[1] = currentLineSplit[1].trim();
        currentLineSplit[2] = currentLineSplit[2].trim();
        currentLineSplit[3] = currentLineSplit[3].trim();

        // roughly equivalent to textures[0] = tileImages[1][2];
        texturesArray[Integer.parseInt(currentLineSplit[1])] = imageMatrix[Integer.parseInt(currentLineSplit[2])][Integer.parseInt(currentLineSplit[3])];
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
    else if  ( (tile == 2) || (tile == 12))  {  // the list right now is very short.
      return false;
    }
    return true;
  }

  // Given a text file string formatted properly (see props_example.txt), will parse it into the Props list.
  // Props are any tile which can be interacted with.
  static Prop[] readProps(String propsString) {
    String[] propsArray = propsString.split("\n");
    String[] lineParser;
    Prop[] newProps = new Prop[propsArray.length];


    for (int i = 0; i < propsArray.length; i++) {
      lineParser = propsArray[i].split(",", 4);

      // if there are enough arguments, create a new prop
      if (lineParser.length > 3) {
        newProps[i] = new Prop(
          i,
          Integer.parseInt(lineParser[0].trim()),
          Integer.parseInt(lineParser[1].trim()),
          Integer.parseInt(lineParser[2].trim())
        );

        // parses and stores the metadata
        newProps[i].setMetadata(lineParser[3].trim());

        // stores the prop's ID in the props map
        propsMap[newProps[i].getX()][newProps[i].getY()] = i;
      }

    }

    return newProps;

  }

  // Loads a new level.
  public static void loadLevel(String filename) throws FileNotFoundException {
      
      loadLevelInternal(filename);

      // find the Spawn entry (usually but not always the first object) and spawn there
      Prop currentProp = Game.getProp(0);
      int propIndex = 0;
      while (!currentProp.metadata.containsKey("spawn")) {
        propIndex++;
        currentProp = Game.getProp(propIndex);
      }
      Game.setCharX(currentProp.getX());
      Game.setCharY(currentProp.getY());
      clearFog(charX, charY); // reveal the immediate area, for ~vibes~

  }
  public static void loadLevel(String filename, int spawnX, int spawnY) throws FileNotFoundException {

    loadLevelInternal(filename);

    Game.setCharX(spawnX);
    Game.setCharY(spawnY);
    clearFog(charX, charY);

  }

  public static void loadLevelInternal(String filename) throws FileNotFoundException {
      // clear maps before refilling them
      
      // save the current fog of war in the history
      fowHistory[mapIndexConnections.get(currentMapFilename)] = fogOfWar;

      // set the fog of war for the destination
      if (mapIndexConnections.containsKey(filename)) {
        fogOfWar = fowHistory[mapIndexConnections.get(filename)]; 
      } else {
        fogOfWar = new int[GRID_WIDTH][GRID_HEIGHT];
        System.out.println("warning: setup a map index connection for "+filename+" in the fog of war hashmap");
      }
      
    
      propsMap = new int[GRID_WIDTH][GRID_HEIGHT];
      
      // update the current filename
      currentMapFilename = filename;

      // load in maps
      System.out.println("Load ingame map");
      grid = readInGameMap(GFU.loadTextFile(filename+".txt"));
      System.out.println("Load prop map");
      props = readProps(GFU.loadTextFile(filename+"_props.txt"));
  }

  public static void errorScreen() {
    beRightBack = true;
  }


  public static void setCharX(int val) {
    charX = val;
  }

  public static void setCharY(int val) {
    charY = val;
  }

  public static Prop getProp(int index) {
    return props[index];
  }


  // Check if the player can move into a free space.
  // TODO: restrict the player from moving off the map
  boolean canMove(int dx, int dy) {
    if (((charX+dx) >= GRID_WIDTH) || ((charY+dy) >= GRID_HEIGHT) || ((charX+dx) < 0) || ((charY+dy) < 0)) {
      return false;
    } else if (canPass(grid[charX+dx][charY+dy])) {
      return true;
    }
    return false;
  }

  // Attempt to move into a free space.
  boolean tryMove(int dx, int dy) {
    if (canMove(dx, dy)) {
      charX += dx;
      charY += dy;
      clearFog(charX, charY);
      return true;
    }
    return false;
  }

  // Attempt to perform an (automatic) action.
  boolean tryAction(int x, int y) {
    if (propsMap[x][y] == 0) {
      return false;
    }
    return props[propsMap[x][y]].tryOverlapAction();
  }

  /*
  Attempt to perform a manual action
  When the player hits the touch action button (default Space and/or Enter) check for and execute any
  touch actions. 
  It only checks the prop in the direction where 
  */
  public void touchAction() {
    // above:
    if ( (charDir==0) && (propsMap[charX][charY-1] != 0)) {
      props[propsMap[charX][charY-1]].tryTouchAction();
    } 
    // below:
    else if ( (charDir==2) && (propsMap[charX][charY+1] != 0)) {
      props[propsMap[charX][charY+1]].tryTouchAction();
    } 
    // right:
    else if ( (charDir==1) && (propsMap[charX+1][charY] != 0)) {
      props[propsMap[charX+1][charY]].tryTouchAction();
    } 
    // left:
    else if ( (charDir==3) && (propsMap[charX-1][charY] != 0)) {
      props[propsMap[charX-1][charY]].tryTouchAction();
    } 
  }


  // FOG OF WAR FUNCTIONS


  // Clear the fog in a small square around your character.
  public static void clearFog() {
    clearFog(charX, charY);
  }



  // clears the fog from a tile
  public static boolean showTile(int x, int y) {
    if (isValidTile(x,y)) {
      fogOfWar[x][y] = 1;
      return true;
    }
    return false;
  }

  // Determines whether a tile at x,y is passable.
  // Returns false if it is not passable, or if it is invalid.
  public static boolean isPassableXY(int x, int y) {
    if (isValidTile(x,y)) {
      return canPass(grid[x][y]);
    }
    return false;
  }

  // Determines whether a tile at x,y is revealed.
  // Returns false if it is hidden, or if it is invalid.
  public static boolean isRevealedXY(int x, int y) {
    if (isValidTile(x,y)) {
      if (fogOfWar[x][y] == 0) { return false; }
      else {return true; }
    }
    return false;
  }

  // Fog type A tiles are those in the immediate 8 squares around the player
  public static void revealFogA(int charX, int charY, int x, int y) {
    showTile(x,y); 
  }

  // Fog type B tiles are those in the "thick plus" shape around the player
 
  public static void revealFogB(int charX, int charY, int x, int y) {
    if (  (Math.abs(charY - y) <= 1)  &&  (!(Math.abs(charX-x) <= 1))) {


      // the tile is along the horizontal axis relative to the player
      if ( (charX - x) < 0) {
        // the tile is on the RIGHT of the player
        // thus the tile to the LEFT of the tile (x-1) must be checked
        if ( isPassableXY(x-1,y) && isRevealedXY(x-1,y)) {
          showTile(x,y);
        }

      } else {

        // the tile is to the LEFT, thus reverse it all
        if ( isPassableXY(x+1,y) && isRevealedXY(x+1,y)) {
          showTile(x,y);
        }
      }


    } else {


      // the tile is on the vertical axis
      if ( (charY - y) < 0) {
        // the tile is BELOW the player
        // thus the tile ABOVE the tile (y-1) must be checked
        if ( isPassableXY(x,y-1) && isRevealedXY(x,y-1)) {
          showTile(x,y);
        }
      } else {
        // the tile is ABOVE the player
        if ( isPassableXY(x,y+1) && isRevealedXY(x,y+1)) {
          showTile(x,y);
        }

      }


    }
  }


  public static void revealFogC(int charX, int charY, int x, int y) {
    // Since both critical blocks share an edge with the active tile,
    // and they will not be opposite each other,
    // one block will share an X with the tile, the other will share a Y.
    int critX;
    int critY;
    if ( (charY - y) < 0) {
      // the tile is below the player, the critical block must be the one above this tile
      
      critY = y-1;

    } else {
      
      critY = y+1;
    }

    if ( (charX - x) < 0) {
      critX = x-1;
    } else {
      critX = x + 1;
    }

    // needs some work. but it'll do!
    // would like to have corners be auto revealed 
    // when they're a c type
    // but that's a stretch goal
    if ( isRevealedXY(x,critY) && isRevealedXY(critX,y) ) {
      if ( isPassableXY(x,critY) || isPassableXY(critX,y) || (!isPassableXY(x,y)) ) {
        showTile(x,y);
      }
    }
  }


  public static void clearFog(int charX, int charY) {
    int x;
    int y;
    // clear the first inner ring
    // it's all fog A, so give the game a break
    for ( x = charX-1; x <= charX+1; x++) {
      for ( y = charY-1; y <= charY+1; y++) {
        revealFogA(charX, charY, x, y);
      }
    }
    // clear the middle ring
    for ( x = charX-2; x <= charX+2; x++) {
      for ( y = charY-2; y <= charY+2; y++) {
        clearFogFromTile(charX, charY, x, y);
      }
    }
    // clear the outermost ring
    for ( x = charX-3; x <= charX+3; x++) {
      for ( y = charY-3; y <= charY+3; y++) {
        clearFogFromTile(charX, charY, x, y);
      }
    }
  }

  // Given the character coordinates and the coordinates of a tile,
  // determines which type of fog to clear and then clears it
  public static void clearFogFromTile(int charX, int charY, int x, int y) {
    if ( ( Math.abs(charY - y) <= 1 ) && ( Math.abs(charX - x) <= 1 ) ) {
      revealFogA(charX,charY,x,y);
    } else if ( ( Math.abs(charY - y) <= 1 ) || ( Math.abs(charX - x) <= 1 ) ) {
      revealFogB(charX,charY,x,y);
    } else {
      revealFogC(charX,charY,x,y);
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
    for (int y=0; y < GRID_HEIGHT; y++) {
      for (int x=0; x < GRID_WIDTH; x++) {
        tilesString = tilesString + grid[x][y] + ",";
      }
      tilesString = tilesString + "\n";
    }
    return tilesString;
  }


  void drawGrid()
  {
    for (int y = 0; y < GRID_HEIGHT; y++) {
      for (int x = 0; x < GRID_WIDTH; x++) {
        if ((editMode) || (fogOfWar[x][y] == 1)) {
          GFStamp s = textures[grid[x][y]];
          s.moveTo(edgeBuffer+x*TILE_SIZE, edgeBuffer+y*TILE_SIZE);
          s.stamp();
        }
      }
    }
  }

  // Draws all preexisting props. 
  void drawProps() {
    for (int i = 0; i < props.length; i++) {
      if ( (props[i].exists) && ( (editMode) || (fogOfWar[props[i].getX()][props[i].getY()] == 1)) ) {
        GFStamp s = textures[props[i].getIcon()];
        s.moveTo(edgeBuffer+props[i].getX()*TILE_SIZE, edgeBuffer+props[i].getY()*TILE_SIZE);
        s.stamp();
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

  public void faceChar(String dir) {
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

  

  public void onKeyDown(String key, int code, int flags) {

    switch(code) {
      case GFKey.A:
      case GFKey.ArrowLeft: {
        faceChar("left");
        if (tryMove(-1,0)) {
          tryAction(charX, charY);
        }
        
        break;
      }

      case GFKey.D:
      case GFKey.ArrowRight: {
        faceChar("right");
        if (tryMove(1,0)) {
          tryAction(charX, charY);
        }
        break;
      }  
      
      case GFKey.W:
      case GFKey.ArrowUp: {
        faceChar("up");
        if (tryMove(0,-1)) {
          tryAction(charX, charY);
        }
        break;
      }

      case GFKey.S:
      case GFKey.ArrowDown: {
        faceChar("down");
        if (tryMove(0,1)) {
          tryAction(charX, charY);
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
            /// WHY NOT WORK????
            editModeProps = editModeProps + "\n"+ editModePlaceIcon + ","+ charX +","+ charY +",{}";
            System.out.println(editModePlaceIcon + ","+ charX +","+ charY +",{}");
            propsMap[charX][charY] = editModePlaceIcon;
            editModeGhostProps.add(new Prop(1,editModePlaceIcon,charX,charY));
          }
        } else {
          // call a touch action
          touchAction();
        }
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

  public static boolean addToInventory(String item) {
    if (playerInventory.size() < INVENTORY_SIZE) {
      playerInventory.add(item);
      return true;
    } else {
      System.out.println("You can't pick it up. Not enough room.");
      return false;
    }
  }

  public static boolean inventoryTake(String item) {
    if (playerInventory.contains(item)) {
      playerInventory.remove(item);
      return true;
    }
    else{
      System.out.println("You don't have what you need.");
      return false;
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
      if (frameCount%4 == 0) {
        dialogueBox.displayOneCharacter();
      }
    }

  }

}
