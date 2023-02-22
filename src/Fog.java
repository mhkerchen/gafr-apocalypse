
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

public class Fog {

    public static int fogOfWar[][] = new int[Game.GRID_WIDTH][Game.GRID_HEIGHT];
    public static int[][][] fowHistory = new int[Game.totalMaps][Game.GRID_WIDTH][Game.GRID_HEIGHT];
    static HashMap<String, Integer> mapIndexConnections = new HashMap<String, Integer>();


	public static void initFog() {
		
		Fog.mapIndexConnections.put("assets/maps/dungeon_map", 0);
		Fog.mapIndexConnections.put("assets/maps/dungeon_map_2", 1);
		Fog.mapIndexConnections.put("assets/maps/newmap", 2);
		Fog.mapIndexConnections.put("assets/maps/dungeon_map_3", 3);
	}

	public static void loadFog(String filename) {


		
      fowHistory[mapIndexConnections.get(Game.currentMapFilename)] = fogOfWar;

      // set the fog of war for the destination
      if (mapIndexConnections.containsKey(filename)) {
        fogOfWar = fowHistory[mapIndexConnections.get(filename)]; 
      } else {
        fogOfWar = new int[Game.GRID_WIDTH][Game.GRID_HEIGHT];
        System.out.println("warning: setup a map index connection for "+filename+" in the fog of war hashmap");
      }
      
	}

	public static void clearFog() {
		clearFog(Player.p.x, Player.p.y);
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



	// clears the fog from a tile
	private static boolean showTile(int x, int y) {
		if (Game.isValidTile(x,y)) {
		Fog.fogOfWar[x][y] = 1;
		return true;
		}
		return false;
	}

	  // Fog type A tiles are those in the immediate 8 squares around the player
  private static void revealFogA(int charX, int charY, int x, int y) {
    showTile(x,y); 
  }

  // Fog type B tiles are those in the "thick plus" shape around the player
 
  private static void revealFogB(int charX, int charY, int x, int y) {
    if (  (Math.abs(charY - y) <= 1)  &&  (!(Math.abs(charX-x) <= 1))) {


      // the tile is along the horizontal axis relative to the player
      if ( (charX - x) < 0) {
        // the tile is on the RIGHT of the player
        // thus the tile to the LEFT of the tile (x-1) must be checked
        if ( Game.isPassableXY(x-1,y) && isRevealedXY(x-1,y)) {
          showTile(x,y);
        }

      } else {

        // the tile is to the LEFT, thus reverse it all
        if ( Game.isPassableXY(x+1,y) && isRevealedXY(x+1,y)) {
          showTile(x,y);
        }
      }


    } else {


      // the tile is on the vertical axis
      if ( (charY - y) < 0) {
        // the tile is BELOW the player
        // thus the tile ABOVE the tile (y-1) must be checked
        if ( Game.isPassableXY(x,y-1) && isRevealedXY(x,y-1)) {
          showTile(x,y);
        }
      } else {
        // the tile is ABOVE the player
        if ( Game.isPassableXY(x,y+1) && isRevealedXY(x,y+1)) {
          showTile(x,y);
        }

      }


    }
  }


  private static void revealFogC(int charX, int charY, int x, int y) {
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
      if ( Game.isPassableXY(x,critY) || Game.isPassableXY(critX,y) || (!Game.isPassableXY(x,y)) ) {
        showTile(x,y);
      }
    }
  }

  // Determines whether a tile at x,y is revealed.
  // Returns false if it is hidden, or if it is invalid.
  public static boolean isRevealedXY(int x, int y) {
    if (Game.isValidTile(x,y)) {
      if (fogOfWar[x][y] == 0) { return false; }
      else {return true; }
    }
    return false;
  }

  

  
  // Given the character coordinates and the coordinates of a tile,
  // determines which type of fog to clear and then clears it
	private static void clearFogFromTile(int charX, int charY, int x, int y) {
    if ( ( Math.abs(charY - y) <= 1 ) && ( Math.abs(charX - x) <= 1 ) ) {
      revealFogA(charX,charY,x,y);
    } else if ( ( Math.abs(charY - y) <= 1 ) || ( Math.abs(charX - x) <= 1 ) ) {
      revealFogB(charX,charY,x,y);
    } else {
      revealFogC(charX,charY,x,y);
    }
  }

}