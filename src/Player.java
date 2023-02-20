/*import GaFr.GFTexture;

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


	// Right now, it's just the player that is a Player. 
	// Rover might become a Player later as well. 

	// statics
	public static Player currentPlayer;
	// public static HashMap<Integer,String> animation_key = makeAnimationKey("assets/image_indexes/animation_key.txt");


	public int x;
	public int y;
	public HashMap<String, GFStamp> imgs = new HashMap<String, GFStamp>();

	public Player(String texture_filename) {
		createImgs(new GFTexture(texture_filename));
	}

	public void createImgs(GFTexture texture) {

		/* split up the textures 
		
		match each texture with its component 
		
	}

	public static HashMap<Integer,String> makeAnimationKey(String filename) {
		String[] arr = GFU.loadTextFile(filename).split("\n");
		String[] split;
		for (int i = 0; i < arr.length; i++) {
			split = arr[i].split(",");
			//animation_key.put(Integer.parseToInt(split[0].trim()), arr[1].trim())
		}
	}

	
}*/