import GaFr.GFTexture;

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


public class Player {
	// statics
	public static Player human;
	public static Player rover;
	public static Player p;
	public static Player cur; // either human or rover, depending
  // static variables
  public int x = 1;
  public int y = 1;
  public int img = 0;
  //public GFStamp img;
  // public String imgname;
  public int dir = 0;

  public boolean show = false;


  GFStamp[] playerImgs;// = new GFStamp[16];

  public static int keyTimeout = -1;

  public static String isPressed = "none";

  public static int INITIAL_TIMEOUT = 18;
  public static int DEFAULT_TIMEOUT = 10;

  public static GaFrHash<String, GFStamp> playerTextures = new GaFrHash<String, GFStamp>();

	public Player(String type) {
		//createImgs(new GFTexture(texture_filename));
		
    if (type.equals("player")) {
      show = true;
    }
		x=2;
		y=2;
	}

	public void setCharX(int val) {
		x = val;
	}

	public void setCharY(int val) {
		y = val;
	}

  public int getImg() {
    if (show) {
      return this.img;
    }
    return 0;
  }

  public void getImgStamp() {

  }
/*
	public static HashMap<Integer,String> makeAnimationKey(String filename) {
		String[] arr = GFU.loadTextFile(filename).split("\n");
		String[] split;
		for (int i = 0; i < arr.length; i++) {
			split = arr[i].split(",");
			//animation_key.put(Integer.parseToInt(split[0].trim()), arr[1].trim())
		}
	}
*/

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

  public void faceChar(String ndir) {
    if (ndir.equals("up")) {
      dir = 0;
      img = 4;
    } else if (ndir.equals("right")) {
      dir = 1;
      img = 8;
    } else if (ndir.equals("down")) {
      dir = 2;
      img = 0;
    } else {
      dir = 3;
      img = 12;
    }
  }

  /*
  Attempt to perform a manual action
  When the player hits the touch action button (default Space and/or Enter) check for and execute any
  touch actions. 
  It only checks the prop in the direction where you're facing. 
  */
  public void touchAction() {
    // on tile:
    if ((Game.propsMap[x][y] != 0)) {
      Prop.props[Game.propsMap[x][y]].tryTouchAction();
    } 
    // above:
    else if ( (dir==0) && (Game.propsMap[x][y-1] != 0)) {
      Prop.props[Game.propsMap[x][y-1]].tryTouchAction();
    } 
    // below:
    else if ( (dir==2) && (Game.propsMap[x][y+1] != 0)) {
      Prop.props[Game.propsMap[x][y+1]].tryTouchAction();
    } 
    // right:
    else if ( (dir==1) && (Game.propsMap[x+1][y] != 0)) {
      Prop.props[Game.propsMap[x+1][y]].tryTouchAction();
    } 
    // left:
    else if ( (dir==3) && (Game.propsMap[x-1][y] != 0)) {
      Prop.props[Game.propsMap[x-1][y]].tryTouchAction();
    } 
  }

  public void pollMove() {
    String key;
    int timeout;
    //System.out.println(isPressed.get("keyDown"));
    if ( !isPressed.equals("none")) { 
      // if a key is currently held down
      // isPressed is the currently pressed key
      

      if (keyTimeout < 0) { // reset the timer
        this.goDir(isPressed);
        keyTimeout = DEFAULT_TIMEOUT;
      } else { // decrement the timer
        keyTimeout--;
      }
    } else if (keyTimeout > 0 ) {
      keyTimeout = -1;
    }


  }

  public void goDir(String dir) {
    if (!TextBox.isDialogue) {
      if (dir.equals("left") || dir.equals("keyLeft")) {
        faceChar("left");
        if (tryMove(-1,0)) {
          tryAction(x, y);
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
    System.out.println("Try action WHEE: "+Game.propsMap[x][y]);
    if (Game.propsMap[x][y] == 0) {
      return;
    }
    Prop.props[Game.propsMap[x][y]].tryOverlapAction();
  }

}