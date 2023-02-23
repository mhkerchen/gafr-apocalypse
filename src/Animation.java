import GaFr.GFStamp;
import GaFr.GFU;
import java.util.*;
import java.io.*;
import java.lang.Integer;
import java.lang.Math;

public class Animation{

	// A simple animation.
	// If poll() is run 60x a second, img will switch between
	// all the images in the images list.
	
	// How it works: the img field replaces the icon field of a prop.
	// The code in the animation changes img, so prop will read img. 

	public int[] images;  // all indexes of images
	public int wait;  // duration between image changes
	public int timeout;  // the current countdown to the next change
	public int img_index = 0; // images[img_index] = img
	public int img;  // currently displayed image
	public boolean repeat = true; // does this animation play just once, or multiple times?
	public boolean go = true;  // whether it's currently playing or paused

	// All currently playing animations.
	public static ArrayList<Animation> animations = new ArrayList<Animation>(); 
	// A list of all "default" animations -- i.e. animations stored in animations.txt
	public static HashMap<String, Animation> defaultAnimations = new HashMap<String, Animation>(); 

	public Animation(String[] imglist, int framesSleep, boolean isRepeat) {

		// convert all strings into integers
		images = new int[imglist.length];
		int thing;
		for (int i = 0; i < imglist.length; i++) {
			thing = Game.translate(imglist[i]);
			images[i] = thing;
		}
		
		// initialize the other variables
		repeat = isRepeat;
		wait = framesSleep;
		timeout = framesSleep;
		img = images[0];

		animations.add(this); // add self to polled list -- this ensures that animations "animate"
	}

	// Returns the current image. 
	public int getImg() {
		return this.img;
	}

	// Polls all animations. 
	public static void pollAnimations() {
		for (int i = 0; i < animations.size(); i++) {
			animations.get(i).poll();
		}
	}

	// Populates the defaultAnimations map, from animations.txt. 
	public static void initializeAnimations() throws FileNotFoundException {
		String[] animationLines = Readers.splitFileNewline(GFU.loadTextFile("assets/image_indexes/animations.txt"));
		String[] args;

		for (int i = 0; i < animationLines.length; i++) {

			if (Readers.lineValid(animationLines[i])) {
				args = Readers.splitLineStr(animationLines[i]);
				// Format: NAME, NUMBER, PERIOD_FRAMES, REPEAT, {IMAGES}

				defaultAnimations.put(args[0], new Animation(metaToArr(args[4]), Integer.parseInt(args[2]), Boolean.parseBoolean(args[3])));
			}
		}

	}

	// Turns the metadata into a comma separated array.
	// NOT the same as the metadata reader for Props!!!
	public static String[] metaToArr(String meta) {
		return Readers.splitLineStr(meta.trim().substring(1, meta.length()-1));

	}

	// Given a name in animations.txt (e.g. GREEN_LIGHT_BLINK), get the right animation
	public static Animation loadAnimation(String name) {
		if (defaultAnimations.containsKey(name) ) {
			return defaultAnimations.get(name);
		} else {
			System.out.println("Error: no animation \""+name+"\" found in default animations");
			return null;
		}
		
	}
	
	// Run once per frame: changes the image as necessary, or changes timeout.
	private void poll() {
		if (go) {  // skip not running animations
			timeout--;
			if (timeout == 0) {
				img_index++;
				if (img_index == images.length) {
					if (repeat) {
						img_index = 0;
					} else {
						endAnimation();
					}
				}
				img = images[img_index];
				timeout = wait;
			}
		}
		
	}

	// stops (more accurately, freezes) an animation
	public void endAnimation() { 
		go = false;
	}

}