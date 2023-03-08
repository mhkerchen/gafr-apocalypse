import GaFr.GFTexture;

import GaFr.GFGame;
import GaFr.Gfx;
import GaFr.GFU;
import GaFr.GFTexture;
import GaFr.GFStamp;
import GaFr.GFKey;

import java.util.*;
import java.io.*;
import java.lang.Integer;
import java.lang.Math;


public class Player {
	// statics
	public static Player human;
	public static Player robot;
	public static Player cur; // either human or rover, depending

  public static int imageIndex = 300; // used in texture indexing
  public static boolean robot_unlocked = false;
  public static boolean can_swap = false;

  public static final int INITIAL_TIMEOUT = 12;
  public static final int DEFAULT_TIMEOUT = 10;
  public static int keyTimeout = -5;
  public static String isPressed = "none";

  // non static variables
  public int x = 0;
  public int y = 0;
  public GFStamp img;
  public String prefix;
  public String imgname;
  public int dir = 0;
  public boolean show = false;

  //public Animation animation;


	public Player(String type, String new_prefix) {
    
    if (type.equals("player")) {
      show = true;
    }
    
    this.prefix = new_prefix;
    this.loadTextures("assets/data/characters/"+type+".txt");
    this.setImg("FACE_DOWN");
	}

  public static void initPlayers() {
    
    human = new Player("player", "HUMAN");
    robot = new Player("robot", "ROBOT");

    cur = human;

  }


  // Loads textures from the given file into the main textures dict.
  // They are tagged with the prefix (either HUMAN_ or ROBOT_).
  void loadTextures (String filename) {

    
    // split data into lines
    String[] textureLines;
    try {
      textureLines = Reader.splitFileNewline(GFU.loadTextFile(filename));
    } catch (Exception e) {
      System.out.println("Error opening file \""+filename+"\".");
      textureLines = null;
    }

    // split and prepare images
    GFStamp[][] images = new GFTexture("assets/images/characters.png").splitIntoTilesBySize2D(32,32);
    for (GFStamp s: new GFU.Iter2D<GFStamp>(images)) {
      s.centerPin();
    }

    String[] args;
    int tempx;
    int tempy;

    for (int i = 0; i < textureLines.length; i++) {
      args = Reader.splitLineStr(textureLines[i]);
      tempx = Integer.parseInt(args[1]);
      tempy = Integer.parseInt(args[2]);
      
      Game.addTile(imageIndex, this.prefix+"_"+args[0], images[tempx][tempy]);
      imageIndex++;

    }

  }

  // Set the current player. 
  public static void setPlayer(String name) {
    if (name.equals("human")) {
      cur = human;
    } else {
      cur = robot;
    }
  }

	public void setCharX(int val) {
		x = val;
	}

	public void setCharY(int val) {
		y = val;
	}

  public GFStamp getImg() {
    
    if (show && (this.img != null)) {
      return this.img;
    }
    else if (this.img == null) {
      System.out.println("Character image is null");
    }
    return Game.textures.get(Game.translate("NOTHING"));
  }

  public void setImg(String new_imgname) {
    this.imgname = this.prefix +"_"+new_imgname;
    if (Game.tileDict.containsKey(imgname)) {
      this.img = Game.textures.get(Game.translate(imgname));
    } else {
      System.out.println("WARNING: no image \""+imgname+"\" found.");
      this.img = Game.textures.get(Game.translate("NOT_FOUND"));
    }
  }


  // Check if the player can move into a free space.
  boolean canMove(int dx, int dy) { 
    if (((x+dx) >= Game.GRID_WIDTH) || ((y+dy) >= Game.GRID_HEIGHT) || ((x+dx) < 0) || ((y+dy) < 0)) {
      return false;
    } else if (Game.isPassableXY(x+dx, y+dy)) {//(canPass(grid[charX+dx][charY+dy])) {
      return true;
    }
    return false;
  }

  // Attempt to move into a free space.
  boolean tryMove(int dx, int dy) {
    if (canMove(dx, dy)) {
      movePlayer(dx, dy);
      return true;
    }
    return false;
  }

  // Force moves the player.
  void movePlayer(int dx, int dy) {
      x += dx;
      y += dy;
	    Sfx.playStep();
      Fog.clearFog(x, y);

  }

  // Changes the direction the player is moving. 
  public void faceChar(String ndir) {
    if (ndir.equals("up")) {
      dir = 0;
      setImg("FACE_UP");
    } else if (ndir.equals("right")) {
      dir = 1;
      setImg("FACE_RIGHT");
    } else if (ndir.equals("down")) {
      dir = 2;
      setImg("FACE_DOWN");
    } else {
      dir = 3;
      setImg("FACE_LEFT");
    }
  }

  /*
  Attempt to perform a manual action
  When the player hits the touch action button (default Space or Enter) 
  check for and execute any touch actions. 
  */
  public void touchAction() {
    // on tile:
    if (Prop.isValid(x,y)) {
      Prop.propAt(x,y).tryTouchAction();
    } 
    // above:
    else if ( (dir==0) && (Prop.isValid(x,y-1))) {
      Prop.propAt(x,y-1).tryTouchAction();
    } 
    // below:
    else if ( (dir==2) && (Prop.isValid(x,y+1))) {
      Prop.propAt(x,y+1).tryTouchAction();
    } 
    // right:
    else if ( (dir==1) && (Prop.isValid(x+1,y))) {
      Prop.propAt(x+1,y).tryTouchAction();
    } 
    // left:
    else if ( (dir==3) && (Prop.isValid(x-1,y))) {
      Prop.propAt(x-1,y).tryTouchAction();
    } 
  }

  public void pollMove() {
    if ( !isPressed.equals("none")) { // provided some key is pressed down

      if (this.keyTimeout < 0) { // time to move in any event

        this.goDir(isPressed);
        if (this.keyTimeout==-5) {
          // this is a "fresh press", so move and start the countdown
          this.keyTimeout = INITIAL_TIMEOUT;
        } else {
          // a repeat press
          this.keyTimeout = DEFAULT_TIMEOUT;
        }
        

      } else { // decrement the timer
        this.keyTimeout--;
      }

    } else if (keyTimeout > 0 ) { 
      // reset the timer, to wait for another key press
      keyTimeout = -5;
    }


  }

  // Move in a direction, as if by keypress, and then check for action.
  public void goDir(String dir) {
    if (!TextBox.isDialogue) {

      if (dir.equals("left") || dir.equals("keyLeft")) {
        faceChar("left");
        if (tryMove(-1,0)) {
          tryAction(x, y); // run an overlap action after moving
        }
      }
      if (dir.equals("right") || dir.equals("keyRight")) {
        faceChar("right");
        if (tryMove(1,0)) {
          tryAction(x, y);
        }
      }
      if (dir.equals("up") || dir.equals("keyUp")) {
        faceChar("up");
        if (tryMove(0,-1)) {
          tryAction(x, y);
        }
      }
      if (dir.equals("down") || dir.equals("keyDown")) {
        faceChar("down");
        if (tryMove(0,1)) {
          tryAction(x, y);
        }
      }

    }
		
  }

	
  // Attempt to perform an (automatic) action.
  void tryAction(int x, int y) {
    if (Prop.isValid(x,y)) {
      Prop.propAt(x,y).tryOverlapAction();
    }
  }

}