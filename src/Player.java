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

  public static int imageIndex = 300;

  
  public static int keyTimeout = -1;

  public static String isPressed = "none";

  public static int INITIAL_TIMEOUT = 18;
  public static int DEFAULT_TIMEOUT = 10;

  // static variables
  public int x = 2;
  public int y = 2;
  public GFStamp img;
  public String prefix;
  public String imgname;
  public int dir = 0;

  public Animation animation;

  public boolean show = false;


	public Player(String type, String new_prefix) {
		//createImgs(new GFTexture(texture_filename));
		
    
    if (type.equals("player")) {
      show = true;
    }
    
    this.prefix = new_prefix;
    this.loadTextures("assets/image_indexes/characters/"+type+".txt");

    this.setImg("FACE_DOWN");

    //this.animation = new Animation();
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
      textureLines = Readers.splitFileNewline(GFU.loadTextFile(filename));
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
      args = Readers.splitLineStr(textureLines[i]);
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
    
    if (this.img == null) {
      System.out.println("Character image is null");
    }
    if (show && (this.img != null)) {
      return this.img;
    }
    return Game.textures.get(Game.translate("NOTHING"));
  }

  public void setImg(String imgnamen) {
    this.imgname = this.prefix +"_"+imgnamen;
    if (Game.tileDict.containsKey(imgname)) {
      this.img = Game.textures.get(Game.translate(imgname));
    } else {
      System.out.println("WARNING: no image \""+imgname+"\" found.");
      this.img = null;
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
	    //Sfx.SOUND_STEP.play();
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
  When the player hits the touch action button (default Space and/or Enter) check for and execute any
  touch actions. 
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
    String key;
    int timeout;
    if ( !isPressed.equals("none")) { // provided some key is pressed down

      if (keyTimeout < 0) { // this is a "fresh press", so move and start the countdown

        this.goDir(isPressed);
        keyTimeout = DEFAULT_TIMEOUT;

      } else { // decrement the timer
        keyTimeout--;
      }

    } else if (keyTimeout > 0 ) { 
      // reset the timer, to wait for another key press
      keyTimeout = -1;
    }


  }

  // Move in a direction, as if by keypress, and then check for action.
  public void goDir(String dir) {
    if (!TextBox.isDialogue) {

      if (dir.equals("left") || dir.equals("keyLeft")) {
        faceChar("left");
        if (tryMove(-1,0)) {
          tryAction(x, y); // run an overlap action after moving
        } else {
          ;
          //tryTouchAction(x,y); // run a touch action if you don't move
          // undecided
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