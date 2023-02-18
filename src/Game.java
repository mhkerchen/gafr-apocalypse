
import GaFr.GFGame;
import GaFr.GFStamp;
import GaFr.GFFont;
import GaFr.Gfx;
import GaFr.GFU;
import GaFr.GFTexture;
import GaFr.GFKey;
import GaFr.GFSound;

import java.util.*;
import java.io.*;
import java.lang.*;
import java.lang.Integer;
import java.lang.Math;

public class Game extends GFGame
{
  {
    Gfx.clearColor(0xffcfcfff);
  }

  // TODO
  /**/
  // Constants
    static final int GRID_HEIGHT = 12;
    static final int GRID_WIDTH = 12;
    static final int TILE_SIZE = 32;
    static final int TEXTURES_QTY = 32;
    static final int EDGE_BUFFER = 0;
    static final int FPS = 60;

    // Scoring
    static int score = 0;
    static int ghostScore = 0;
    static int combo = 0;

    // Music
    static int bgmLength = 0; 
    // fun fact! defining the length here gives you the heavy metal cover
    public static HashMap<String, GFSound> soundfx = new HashMap<String, GFSound>(16);
    static boolean mute = false;

    // Display
    static GFStamp[][] tileImages = new GFTexture("assets/images/swaptiles_potions_32.png").splitIntoTilesBySize2D(TILE_SIZE,TILE_SIZE);
    static GFStamp[] textures = new GFStamp[TEXTURES_QTY];
    // A dictionary from the string name to the index in textures
    public static HashMap<String, Integer> tileDict = new HashMap<String, Integer>(TEXTURES_QTY);
    // An "inventory" of all potions (currently unused!)
    public static HashMap<Integer, Integer> potionsQty = new HashMap<Integer, Integer>(16);

    // Fonts
    static GFFont font = new GFFont("gafr/fonts/spleen/spleen-32x64.ffont.json");
    static GFFont fontLittle = new GFFont("gafr/fonts/spleen/spleen-12x24.ffont.json");

    // Cursor location
    static int startSelectX = -1;
    static int startSelectY = -1;
    static int endSelectX = -1;
    static int endSelectY = -1;

    // Game structure
    public static int grid[][] = new int[GRID_WIDTH][GRID_HEIGHT];
    public static int[] colorsPool = {1,2,3,4,5,6,7,8,9,10}; 
    public static int[] validPotions = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};

    // Matching logic
    static int timeTicker = 0;
    static int matchesPhase = -1;

    // Buttons
    public static HashMap<String, Button> buttonsMap = new HashMap<String, Button>();


    // test

    static boolean isInit = false;

  // Initialization
  {
    initialize();
  }

  static void initialize() {


    // Initialize sounds
    initSound();
    
    // Initialize textures
    for (GFStamp s: new GFU.Iter2D<GFStamp>(tileImages)) {
      s.movePinTo(0.0f, 0.0f);
    }
    
    try {
      indexTextures("assets/tile_key.txt", tileImages);
      System.out.println("Textures loaded successfully");
    } catch (Exception e ) {
      System.out.println("Failure to load tile textures :(");
    }

    // Initialize buttons
    buttonsMap.put( "MuteButton", new Button( tileDict.get("MUTED"), 768, 0, 32, 32 ) );
    System.out.println("Buttons loaded");

    // Initialize inventory
    for (int i = 1; i <= 10; i++) {
      potionsQty.put(i, 0);
    }
    System.out.println("Inventory initialized");
    // Create new map
    newMap();
    System.out.println("New map created");

    isInit = true;
  }

  // Initializes all sounds.
  static void initSound() {
    

    // special setup for background music
    soundfx.put("bgm", new GFSound("assets/adventure_short.mp3"));
    soundfx.get("bgm").volume(0.5f);
    soundfx.get("bgm").play();

    // load other sound effects
    soundfx.put("swap", new GFSound("assets/failedswap.mp3")); // courtesy of Pixabay
    soundfx.put("match", new GFSound("assets/bloop.mp3")); // courtesy of freesound.org, jamesabels 


  }


  // Reads texture information from the file into the textures array.
  static void indexTextures(String filename, GFStamp[][] imageMatrix) throws FileNotFoundException {

    String[] tilesArray = GFU.loadTextFile(filename).split("\n"); // an array of each line in filename
    String currentLine;
    String[] currentLineSplit;

    for (int i =0; i < tilesArray.length; i++) {
      currentLine = tilesArray[i].trim();
      
      // skip empty lines, and those which are comments
      if (currentLine.isEmpty()) {
        ;
      } else if  (currentLine.charAt(0)=='/') {
        
        ;
      } else {
        // split the current line into its 4 parts
        currentLineSplit = currentLine.split(",");
        currentLineSplit[0] = currentLineSplit[0].trim();
        currentLineSplit[1] = currentLineSplit[1].trim();
        currentLineSplit[2] = currentLineSplit[2].trim();
        currentLineSplit[3] = currentLineSplit[3].trim();

        // add the tile image into the textures array
        textures[Integer.parseInt(currentLineSplit[1])] = imageMatrix[Integer.parseInt(currentLineSplit[2])][Integer.parseInt(currentLineSplit[3])];

        // add a tilename, index key value pair to tileDict
        tileDict.put(currentLineSplit[0], Integer.parseInt(currentLineSplit[1]));

        // and potions inventory!
        // only do if it's a valid potion (10 is magic unfortunately)
        if (Integer.parseInt(currentLineSplit[1]) <= 10) {
          potionsQty.put(Integer.parseInt(currentLineSplit[1]), 0);
        }
        
      }
    }

  }




  // DRAWING FUNCTIONS

  // Draws the grid, plus icons
  static void drawGrid() {
    
    GFStamp s;

    for (int y = 0; y < GRID_HEIGHT; y++) {
      for (int x = 0; x < GRID_WIDTH; x++) {

        // draw the tile background
        s = textures[tileDict.get("BACKGROUND")];
        s.moveTo(EDGE_BUFFER+x*TILE_SIZE, EDGE_BUFFER+y*TILE_SIZE);
        s.stamp();

        // draw the icon
        s = textures[grid[x][y]];
        s.moveTo(EDGE_BUFFER+x*TILE_SIZE, EDGE_BUFFER+y*TILE_SIZE);
        s.stamp();
        
      }
    }
  }

  // draws the score counter and mute button
  static void drawUI() {
    
    // draw the score counter
    font.color = Gfx.Color.BLACK;
    font.draw(TILE_SIZE*GRID_WIDTH+20,20, ""+(score*100));

    // draw the combo counter
    if (combo > 1) {
      font.draw(TILE_SIZE*GRID_WIDTH+20,80, ""+combo+"x");
    }

    // draw the mute button
    GFStamp s;
    
    if (mute) {
      s = textures[tileDict.get("MUTED")];
    } else {
      s = textures[tileDict.get("UNMUTED")];
    }

    s.moveTo(buttonsMap.get("MuteButton").x, buttonsMap.get("MuteButton").y);
    s.stamp();

    // draw the inventory 
    
  }

  static void drawInventory() {
    GFStamp s;
    
    for (int i = 1; i <= 10; i++) { // 10 is magic

      // draw inventory icon
      s = textures[i];
      s.moveTo(EDGE_BUFFER+(TILE_SIZE*i), 2*EDGE_BUFFER+(TILE_SIZE*GRID_HEIGHT));
      s.stamp();

      // draw quantity counter
      fontLittle.color = Gfx.Color.BLACK;
      fontLittle.draw(EDGE_BUFFER+(TILE_SIZE*i), 12+2*EDGE_BUFFER+(TILE_SIZE*GRID_HEIGHT), ""+(potionsQty.get(i)));
      
    }
  }

  // draws selectors
  static void drawDetails() {

    GFStamp s;

    if ((startSelectX!=-1) && (startSelectY!=-1) ) { // don't draw when the player isn't dragging
      s = textures[tileDict.get("SELECTOR")];

      s.moveTo( EDGE_BUFFER + startSelectX*TILE_SIZE, EDGE_BUFFER + startSelectY*TILE_SIZE );
      s.stamp();
      
      if ((endSelectX!=-1) && (endSelectY!=-1) ) { // ensure that there is also a valid end position

        s.moveTo( EDGE_BUFFER + endSelectX*TILE_SIZE, EDGE_BUFFER + endSelectY*TILE_SIZE );
        s.stamp();

      }
      
    }


  }



  // MAP AND MATCH FUNCTIONS


  // Creates a new map and removes all matches
  static void newMap() {

    for (int y = 0; y < GRID_HEIGHT; y++) {
      for (int x = 0; x < GRID_WIDTH; x++) {
        grid[x][y] = genTile(); 
      }
    }
    System.out.println("Tiles placed");
    
    removeMatchesNoScore();

  }

 // Remove all matches on the map, instantly and without scoring.
  static void removeMatchesNoScore() {
    
    deleteMatches(); // turns all matches into Xes, and increments ghost score
    System.out.println("Delete");

    // "bubble" the X'es up to the top
    while (areEmptyTiles()) {
      dropTilesOnce(true);
    }
    System.out.println("Bubble");
    
    // run again if new matches occur
    if (areMatches()) {
    System.out.println("Go again");
      removeMatchesNoScore();
    }
  }

  // replaces all matches with space
  static void deleteMatches() {
    // replace matches with X'es
    for (int y = 0; y < GRID_HEIGHT; y++) {
      for (int x = 0; x < GRID_WIDTH; x++) {
        checkXY(x,y, grid[x][y]);
      }
    }
  }
  
  // checks to see if tile x,y is part of any matches: if so, "match" them
  static void checkXY(int x, int y, int type) {
    // the checked block is always the upper-est and rightmost-est block
    // case 1: horizontal match:
    if (safeCheckXY(x+1,y,type) && safeCheckXY(x+2,y,type)) {
      
      if (safeCheckXY(x+3,y,type)) { // 4 long
      
        if (safeCheckXY(x+4,y,type)) { // 5 long
          clearTile(x+4,y);
          ghostScore+= 3;
          // FUN NEW BUG DROPPED: uncomment all these lines and then match 4
          //potionsQty.put(type, (potionsQty.get(type)+1));
        }
        clearTile(x+3,y);
        ghostScore+= 3;
        //potionsQty.put(type, (potionsQty.get(type)+1));
      }
      clearTile(x,y);
      clearTile(x+1,y);
      clearTile(x+2,y);
      
      ghostScore+= 3;
      //potionsQty.put(type, (potionsQty.get(type)+1));

    // case 2: vertical match
    }
    if (safeCheckXY(x,y+1,type) && safeCheckXY(x,y+2,type)) { // 3 long
      if (safeCheckXY(x,y+3,type)) { // 4 long
        if (safeCheckXY(x,y+4,type)) { // 5 long
          clearTile(x,y+4);
          ghostScore+= 3;
          //potionsQty.put(type, (potionsQty.get(type)+1));

        }
        clearTile(x,y+3);
        ghostScore+= 3;
        //potionsQty.put(type, (potionsQty.get(type)+1));
      }
      clearTile(x,y);
      clearTile(x,y+1);
      clearTile(x,y+2);

      ghostScore+= 3;
      //potionsQty.put(type, (potionsQty.get(type)+1));

    // case 3: diagonal '/' match
    }
    if (safeCheckXY(x-1,y+1,type) && safeCheckXY(x-2,y+2,type)) {
      if (safeCheckXY(x-3,y+3,type)) { // 4 long
      
        if (safeCheckXY(x-4,y+4,type)) { // 4 long
          clearTile(x-4,y+4);
          ghostScore+= 3;
          //potionsQty.put(type, (potionsQty.get(type)+1));
        }
        clearTile(x-3,y+3);
        ghostScore+= 3;
        //potionsQty.put(type, (potionsQty.get(type)+1));
      }
      clearTile(x,y);
      clearTile(x-1,y+1);
      clearTile(x-2,y+2);
      
      ghostScore+= 3;
      //potionsQty.put(type, (potionsQty.get(type)+1));
    // case 4: diagonal '\' match
    }
    if (safeCheckXY(x+1,y+1,type) && safeCheckXY(x+2,y+2,type)) {
      
      if (safeCheckXY(x+3,y+3,type)) { // 4 long
        if (safeCheckXY(x+4,y+4,type)) { // 5 long
          clearTile(x+4,y+4);
          ghostScore+= 4;
          //potionsQty.put(type, (potionsQty.get(type)+1));
        }
        clearTile(x+3,y+3);
        ghostScore+= 3;
        //potionsQty.put(type, (potionsQty.get(type)+1));
      }
      clearTile(x,y);
      clearTile(x+1,y+1);
      clearTile(x+2,y+2);
      
      ghostScore+= 3;
      //potionsQty.put(type, (potionsQty.get(type)+1));
    }
  }

  // Drops every tile by one space, and generates a new layer of tiles
  static void dropTilesOnce(boolean doRefill) {
    // Note the loop is unusual.
    // this loop checks from the bottom left up.

    for (int x = 0; x < GRID_WIDTH; x++) {
      
      for (int y = GRID_HEIGHT-1; y > 0; y--) {
        if (grid[x][y] == tileDict.get("X")) {

          // "bubble" by swapping the air tile with the tile above it
          grid[x][y] = grid[x][y-1];
          grid[x][y-1] = tileDict.get("X");
          
        }
      }

      // if there's an air space at the top of the column, fill it with a new tile
      if (doRefill) {
        if ((grid[x][0] == tileDict.get("X"))) {
          grid[x][0] = genTile();
        }
      }


    }
  }




  // POSSIBLE MATCH FUNCTIONS
  // Checks to see if there are any possible matches left on the map.
  static boolean areMatchesPossible() {

    // basically, if any match ever could come back true, then true
    // otherwise false (and generally the grid will be reshuffled)
    for (int y = -1; y < GRID_HEIGHT-1; y++) {
      for (int x = -1; x < GRID_WIDTH-1; x++) {
        if (possibleMatch(x,y) ) {
          return true;
        }
      }
    }
    return false;


  }

  // Checks to see if there is a possible match, starting from the anchor point of x,y.
  static boolean possibleMatch(int x, int y) {
    if (possibleMatchVertical(x,y) || possibleMatchHorizontal(x,y) || possibleMatchDiagonalDown(x,y) || possibleMatchDiagonalUp(x,y)) {
      //System.out.println(x+", "+y);
      return true;
    }
    return false;
  }

  static boolean possibleMatchVertical(int x, int y) {
    int type;
    if (safeGetType(x+1,y+1) == safeGetType(x+1,y+2)) {
        type = safeGetType(x+1,y+1);
      if (safeCheckXY(x,y+3,type) || safeCheckXY(x+1,y+4,type) || safeCheckXY(x+2,y+3,type)) {
        return true;
      }
    }
    if (safeGetType(x+1,y+1) == safeGetType(x+1,y+3)) {
        type = safeGetType(x+1,y+1);
      if (safeCheckXY(x,y+2,type) || safeCheckXY(x+2,y+2,type)) {
        return true;
      }

    }
    if (safeGetType(x+1,y+2) == safeGetType(x+1,y+3)) {
        type = safeGetType(x+1,y+2);
      if (safeCheckXY(x,y+1,type) || safeCheckXY(x+1,y,type) || safeCheckXY(x+2,y+1,type)) {
        return true;
      }

    }
    return false;
  }

  static boolean possibleMatchHorizontal(int x, int y) {
    int type;
    if (safeGetType(x+1,y+1) == safeGetType(x+2,y+1)) {
        type = safeGetType(x+1,y+1);
      if (safeCheckXY(x+3,y,type) || safeCheckXY(x+4,y+1,type) || safeCheckXY(x+3,y+2,type)) {
        return true;
      }
    }
    if (safeGetType(x+1,y+1) == safeGetType(x+3,y+1)) {
        type = safeGetType(x+1,y+1);
      if (safeCheckXY(x+2,y,type) || safeCheckXY(x+2,y+2,type)) {
        return true;
      }

    }
    if (safeGetType(x+2,y+1) == safeGetType(x+3,y+1)) {
        type = safeGetType(x+2,y+1);
      if (safeCheckXY(x+1,y,type) || safeCheckXY(x,y+1,type) || safeCheckXY(x+1,y+2,type)) {
        return true;
      }

    }
    return false;
  }

  static boolean possibleMatchDiagonalDown(int x, int y) {
    int type;
    if (safeGetType(x+1,y+1) == safeGetType(x+2,y+2)) {
      type = safeGetType(x+1,y+1);
      if (safeCheckXY(x+3,y+4,type) || safeCheckXY(x+4,y+3,type) || safeCheckXY(x+2,y+3,type) || safeCheckXY(x+3,y+2,type)) {
        return true;
      }
    }
    if (safeGetType(x+1,y+1) == safeGetType(x+3,y+3)) {
      type = safeGetType(x+1,y+1);
      if (safeCheckXY(x+3,y+2,type) || safeCheckXY(x+2,y+3,type) || safeCheckXY(x+2,y+1,type) || safeCheckXY(x+1,y+2,type)) {
        return true;
      }
    }
    if (safeGetType(x+2,y+2) == safeGetType(x+3,y+3)) {
      type = safeGetType(x+2,y+2);
      if (safeCheckXY(x,y+1,type) || safeCheckXY(x+1,y,type) || safeCheckXY(x+2,y+1,type) || safeCheckXY(x+1,y+2,type)) {
        return true;
      }
    }
    return false;
  }

  static boolean possibleMatchDiagonalUp(int x, int y) {
    
    int type;
    if (safeGetType(x+1,y+3) == safeGetType(x+2,y+2)) {
      type = safeGetType(x+2,y+2);
      if (safeCheckXY(x,y+3,type) || safeCheckXY(x+1,y+4,type) || safeCheckXY(x+1,y+2,type) || safeCheckXY(x+2,y+3,type)) {
        return true;
      }
    }

    if (safeGetType(x+1,y+3) == safeGetType(x+3,y+1)) {
      type = safeGetType(x+1,y+3);
      if (safeCheckXY(x+2,y+1,type) || safeCheckXY(x+3,y+2,type) || safeCheckXY(x+1,y+2,type) || safeCheckXY(x+2,y+3,type)) {
        return true;
      }
    }

    if (safeGetType(x+2,y+2) == safeGetType(x+3,y+1)) {
      type = safeGetType(x+3,y+1);
      if (safeCheckXY(x+2,y+1,type) || safeCheckXY(x+3,y+2,type) || safeCheckXY(x+3,y,type) || safeCheckXY(x+4,y+1,type)) {
        return true;
      }
    }

    return false;
  }




  static void runMatchesAsync(boolean scoreCounts) {


    if (timeTicker%15 == 0) {
      
      if (matchesPhase == -1 ){ // neutral
        combo = 0;
      }

      else if (matchesPhase == 0) {
        phaseCheckMatches(scoreCounts);
      }

      else if (matchesPhase==1) {
        phaseDropTiles();

      }

      else if (matchesPhase == 2) {
        phaseTestPlayable();
        
      }
      else if (matchesPhase == 5) {
        phaseEmptyBoard();

      }
      else if (matchesPhase == 6) {
        phaseFillBoard();
        
      }
    }

    
  }

  static void phaseCheckMatches(boolean scoreCounts) {

      combo += 1;
      ghostScore = 0;
      deleteMatches();

      if (scoreCounts) {

        score += ghostScore*combo;
        if (ghostScore != 0) {
          soundfx.get("match").play();
        }

      }

      matchesPhase=1;
  }

  static void phaseDropTiles() {

    if (areEmptyTiles()) {
      dropTilesOnce(true);
    } else {

      if (areMatches()) {
        matchesPhase = 0; // rematch!
      } else {
        matchesPhase = 2; // done with matches! check for a bad board + reset combo counter
      }

    }

  }

  static void phaseTestPlayable() {
    if (areMatchesPossible() ) {
      matchesPhase = -1; // back to default
    } else {
      System.out.println("New map!");
      matchesPhase = 5;
    }
  }

  static void phaseEmptyBoard() {
    for (int x = 0; x < GRID_WIDTH; x++) {
      grid[x][GRID_HEIGHT-1] = tileDict.get("X");
    }
    dropTilesOnce(false);
    if (grid[0][GRID_HEIGHT-1] == tileDict.get("X")) {
      matchesPhase = 6;
    }
  }

  static void phaseFillBoard() {

    dropTilesOnce(false);
    if (grid[0][GRID_HEIGHT-1] != tileDict.get("X")) {
      // there will probably be some matches
      if (areMatches()) {
        matchesPhase = 0;
      } else {
        matchesPhase = -1;
      }
      
    }
  }

  // TILE FUNCTIONS

  // Swaps two tiles, automatically switching them back if there is no match
  static void swapTiles(int x1, int y1, int x2, int y2) {
    if ((startSelectX==endSelectX) && (startSelectY==endSelectY)) {
      //dont();
      return;
    }
    int temp;
    temp = grid[x1][y1];
    grid[x1][y1] = grid[x2][y2];
    grid[x2][y2] = temp;

    // if this doesn't cause a match, it's NOT ALLOWED

    if (!areMatches()) {
      temp = grid[x1][y1];
      grid[x1][y1] = grid[x2][y2];
      grid[x2][y2] = temp;
      soundfx.get("swap").play();
    }

  }

  // map checks

  // returns True if there are empty tiles on the map.
  static boolean areEmptyTiles() {
    for (int y = 0; y < GRID_HEIGHT; y++) {
      for (int x = 0; x < GRID_WIDTH; x++) {
        if (grid[x][y] == tileDict.get("X")) {
          return true;
        }
      }
    }

    return false;

  }

  // returns True if there are matches on the map
  static boolean areMatches() {
    
    for (int y = 0; y < GRID_HEIGHT; y++) {
      for (int x = 0; x < GRID_WIDTH; x++) {
        if (checkXYDry(x,y, grid[x][y])) {
          return true;
        }
      }
    }
    return false;
  }

  // single tile functions

  // Returns a random new tile from the pool of acceptable tiles
  static int genTile() {
    return colorsPool[GFU.randint(1, colorsPool.length-1)];
  }
  
  // Sets the tile to the cleared value (safe)
  static void clearTile(int x, int y) {
    if (existXY(x,y)) {
      grid[x][y] = tileDict.get("X");
    }
  }

  // checks whether a single block is part of a match (used for areMatches)
  static boolean checkXYDry(int x, int y, int type) {
    // the checked block is always the upper-est and rightmost-est block
    // in case 3, x,y indicates the top block
    // not necessary to check the longer matches.
    // case 1: horizontal match:
    if (safeCheckXY(x+1,y,type) && safeCheckXY(x+2,y,type)) {
      return true;
    // case 2: vertical match
    }
    else if (safeCheckXY(x,y+1,type) && safeCheckXY(x,y+2,type)) {
      return true;
    // case 3: diagonal '/' match
    }
    else if (safeCheckXY(x-1,y+1,type) && safeCheckXY(x-2,y+2,type)) {
      return true;
    // case 3: diagonal '\' match
    }
    else if (safeCheckXY(x+1,y+1,type) && safeCheckXY(x+2,y+2,type)) {
      return true;
    }
    return false;
  }

  // checks that an x,y coordinate is a valid coord on the grid
  static boolean existXY(int x, int y) {
    if ( (x >= GRID_WIDTH) || (y >= GRID_HEIGHT) || (x < 0) || (y < 0)) {
      return false;
    }
    return true;
  }

  // safely (ie without exceptions) checks whether the block x,y is of type type.
  // if the block does not exist, or is of the wrong type, it returns false.
  static boolean safeCheckXY(int x, int y, int type) {
    if (!existXY(x,y)) {
      return false;
    } else if (grid[x][y] == type) {
      return true;
    }
    return false;
  }

  // returns the type of a block safely
  static int safeGetType(int x, int y) {
    if (!existXY(x,y)) {
      return -1;
    } else {
      return grid[x][y];
    }
  }


  // I/O
  
  // Keyboard

  public void onKeyDown(String key, int code, int flags) {

    switch(code) {
      case GFKey.M:
      {
        toggleMute();
        break;
      }
    }
  }

  static void toggleMute() {
    if (mute) {
      // it's muted
      // thus, unmute!

      soundfx.get("bgm").volume(0.5f);
      soundfx.get("swap").volume(1.0f);
      soundfx.get("match").volume(1.0f);
    } else {
      // mute it!

      soundfx.get("bgm").volume(0.0f);
      soundfx.get("swap").volume(0.0f);
      soundfx.get("match").volume(0.0f);
    }
    mute = !mute;
  }

  // Mousedown


  public void onMouseDown(int x, int y, int buttons, int flags, int button) {

    setStartSelector(x,y); // sets the swap start location, if applicable


    if ( (buttonsMap.get("MuteButton")).isClick(x, y)) {
      toggleMute();
    }

  }

  public void setStartSelector(int x, int y) {

    if ( (matchesPhase==-1) && (x > 0) && (x < (GRID_WIDTH*TILE_SIZE)) && (y > 0) && (y < (GRID_HEIGHT*TILE_SIZE)))
    // within valid bounds
    {
      startSelectX = x / TILE_SIZE;
      startSelectY = y / TILE_SIZE;
    }

  }



  public void onMouseMove(int x, int y, int flags, int button) {
    setEndSelector(x,y);
  }

  public void setEndSelector(int x, int y) {
    
    // catches situations where the end point is outside of the play area
    if (
        (matchesPhase==-1) &&   // proper phase
        (startSelectX != -1) && (startSelectY != -1) // CYA check
        ) 
      {

        int cursor_dx = (x/TILE_SIZE) - startSelectX; // note: full tiles, not pixels

        int cursor_dy = (y/TILE_SIZE) - startSelectY;

        // determine whether the cursor is farther away horizontally or vertically
        boolean favorHorizontal = (Math.abs(cursor_dx) > Math.abs(cursor_dy));

        // constrain the deltas to within 1 block, for later math
        // Original code wasn't quite so sleek. Thanks https://www.geeksforgeeks.org/integer-signum-method-in-java/ !
        cursor_dx = Integer.signum(cursor_dx);
        cursor_dy = Integer.signum(cursor_dy);
        
        if (favorHorizontal) {
          endSelectX = startSelectX + cursor_dx;
          endSelectY = startSelectY;
        } else {
          endSelectX = startSelectX;
          endSelectY = startSelectY + cursor_dy;
        }

        // if the end selection isn't a valid block, don't select it!
        if (!existXY(endSelectX,endSelectY)) {
          endSelectX = startSelectX;
          endSelectY = startSelectY;
        }

      } else {
        endSelectX = -1;
        endSelectY = -1;
      }
  }


  public void onMouseUp(int x, int y, int buttons, int flags, int button) {
    setEndSelector(x,y); // testing only!

    // perform a match between start and end grid items
    // as long as they aren't the same thing
    if ( (matchesPhase==-1) && !((startSelectX==endSelectX) && (startSelectY==endSelectY)) && (endSelectX!=-1) && (endSelectY!=-1) )
    {
      swapTiles(startSelectX,startSelectY,endSelectX,endSelectY);
      
      // if there are matches, run the matching code
      if (areMatches()) {
        matchesPhase = 0;
      }
      
    }

    startSelectX = -1;
    startSelectY = -1;
    endSelectX = -1;
    endSelectY = -1;

  }

  public void onDraw (int frameCount)
  {
    if (!isInit) {
      //initialize();
      ;
    }

      drawGrid();

      drawDetails();

      drawUI();

      //drawInventory();

  }

  public void onUpdate() {
    timeTicker += 1;
    runMatchesAsync(true);
    
    if (timeTicker % ( bgmLength * 60) == 0) {
      bgmLength = (int)Math.ceil(soundfx.get("bgm").getDuration()); 
      soundfx.get("bgm").play();
    }
    
  }

}














/** 

Currently unused: 


  public void replaceX() {
    for (int y = 0; y < GRID_HEIGHT; y++) {
      for (int x = 0; x < GRID_WIDTH; x++) { 
        if (grid[x][y] == 11) {
          grid[x][y] = genTile();
        }
        
      }
    }
  }



  
  static void checkDifficultyIncrease() {
    // must init colorsPool to {1,2,3,4,5,6,7,8,9,10,1,3,5,7,9};
    if ((score > 50)) {
      colorsPool[10] = 11;
    }
    if ((score > 100)) {
      colorsPool[11] = 12;
    }
    if ((score > 150)) {
      colorsPool[12] = 13;
    }
    if ((score > 200)) {
      colorsPool[13] = 14;
    }
    if ((score > 250)) {
      colorsPool[14] = 15;
    }
  }


  // preserved for posterity: the original monstrosity of a setEndSelector function

  
    if ( (matchesPhase==-1) && (x > 0) && (x < (GRID_WIDTH*TILE_SIZE)) && (y > 0) && (y < (GRID_HEIGHT*TILE_SIZE)))
    // within valid bounds
    // and no matches falling
    {
      if (startSelectX != -1) {

        if (((x/TILE_SIZE)==startSelectX) && ((y/TILE_SIZE)==startSelectY)) {
          // you're mousing over the block.
          endSelectX = startSelectX;
          endSelectY = startSelectY;
        }else if (Math.abs((x/TILE_SIZE)-startSelectX) > Math.abs((y/TILE_SIZE)-startSelectY)) {
          // you need to go either on the left or right of the startSelect button
          
          endSelectY = startSelectY;

          if ( (x/TILE_SIZE > startSelectX) && existXY(startSelectX + 1,startSelectY)) {
            // the end location is on the right of the start button

            endSelectX = startSelectX + 1;

          } else if ( (x/TILE_SIZE < startSelectX) && existXY(startSelectX - 1,startSelectY)) {

            endSelectX = startSelectX - 1;

          }

        } else {
            // you need to go above or below
          
          endSelectX = startSelectX;

          if ( ((y/TILE_SIZE) > startSelectY) && existXY(startSelectX,startSelectY + 1)) {
            // the end location is on the right of the start button

            endSelectY = startSelectY + 1;

          } else if ( ((y/TILE_SIZE) < startSelectY) && existXY(startSelectX,startSelectY-1)) {

            endSelectY = startSelectY - 1;

          }
        }
      }
    }

  */