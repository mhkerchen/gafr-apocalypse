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

  public static int IMAGE_INDEX = 300;

  
  public static int keyTimeout = -1;

  public static String isPressed = "none";

  public static int INITIAL_TIMEOUT = 18;
  public static int DEFAULT_TIMEOUT = 10;

  // static variables
  public int x = 1;
  public int y = 1;
  public GFStamp img;
  public String prefix;
  public String imgname;
  public int dir = 0;

  public Animation animation;

  public boolean show = false;

  public GaFrHash<String, GFStamp> characterTextures = new GaFrHash<String, GFStamp>();
  // VERY IMPORTANT: this is NOT static!
  // The robot and the player both have their own copy of this.

	public Player(String type, String new_prefix) {
		//createImgs(new GFTexture(texture_filename));
		
    
    if (type.equals("player")) {
      show = true;
    }
		x=2;
		y=2;

    prefix = new_prefix;

    //this.animation = new Animation();
	}

  public static void initPlayers() {
    
    human = new Player("player", "HUMAN");
    robot = new Player("robot", "ROBOT");
    cur = human;


    // load textures in
    human.loadTextures("assets/image_indexes/characters/player.txt");
    robot.loadTextures("assets/image_indexes/characters/robot.txt");

    human.setImg("FACE_DOWN");
    robot.setImg("FACE_DOWN");
  }


  void loadTextures (String filename) {
    System.out.println("Start");
    String[] textureLines;
    try {
      textureLines = Readers.splitFileNewline(GFU.loadTextFile(filename));
    } catch (Exception e) {
      System.out.println("Error opening file \""+filename+"\".");
      textureLines = null;
    }
    GFStamp[][] images = new GFTexture("assets/images/characters.png").splitIntoTilesBySize2D(32,32);
    
    for (GFStamp s: new GFU.Iter2D<GFStamp>(images)) {
      s.centerPin();
    }

    String[] args;
    int tempx;
    int tempy;

    for (int i = 0; i < textureLines.length; i++) {
      System.out.println(i);
      System.out.println(textureLines[i]);
      System.out.println(textureLines.length);
      args = Readers.splitLineStr(textureLines[i]);
      tempx = Integer.parseInt(args[1]);
      tempy = Integer.parseInt(args[2]);

      characterTextures.put(args[0], images[tempx][tempy]);

      // and then put this stuff into the main textures 
      // for animation compatibility

      // player textures start with 300
      Game.textures.put(IMAGE_INDEX, images[tempx][tempy]);

      Game.tileDict.put(this.prefix+"_"+args[0], IMAGE_INDEX);
      IMAGE_INDEX++;
    }
    System.out.println("done");
    characterTextures.print();

  }

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
    return characterTextures.get("NOTHING");
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
  It only checks the prop in the direction where you're facing. 
  */
  public void touchAction() {
    // on tile:
    System.out.println("Try touch action: "+Game.propsMap[x][y]);
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
    if (Game.propsMap[x][y] == 0) {
      return;
    }
    System.out.println("Try action: "+Game.propsMap[x][y]);
    Prop.props[Game.propsMap[x][y]].tryOverlapAction();
  }

}