
import GaFr.GFStamp;
import GaFr.GFFont;
import GaFr.GFTexture;


import java.util.*;

public class TextBox {


	public GFFont font;
	public int fontwidth;
	public int fontheight;

	public int x;
	public int y;
	public int width;
	public int height;

	public String textBuffer;
	private int textBufferIndex = 0;

	public String displayText;
	public boolean show;

	private ArrayList<String> allText = new ArrayList<String>();

	private static GFStamp background = new GFStamp(new GFTexture ("assets/images/textbox.png"));


	public static boolean slowText = true;

	public int ctc_timer = 0;
	public static int ctc_timeout = 5;
	
	public TextBox (int newx, int newy, int newwidth, int newheight, GFFont newfont) {

		x = newx;
		y = newy;

		width = newwidth;
		if (newwidth > 800 ) {
			width = 800;
		}
		height = newheight;
		font = newfont;

		fontwidth = 12;//(int)newfont.glyphMap[0].width;
		fontheight = 22;//(int)newfont.glyphMap[0].height;

		show = false;

	}

	public void clearText() {
		textBuffer = "";
		displayText = "";
		textBufferIndex = 0;
	}

	public void setTextSlowDisplay(String newtext) {
		textBuffer = splitByLine(newtext);
		displayText = "";
		textBufferIndex = 0;
	}

	public void setTextFastDisplay(String newtext) {
		displayText = splitByLine(newtext);
		textBuffer = "";
		textBufferIndex = 0;
	}

	public void setText(String newtext) {
		if (slowText) {
			setTextSlowDisplay(newtext);
		} else {
			setTextFastDisplay(newtext);
		}
	}

	public void displayOneCharacter() {
		if ((show) && (textBufferIndex < textBuffer.length())) {
			displayText = displayText+ textBuffer.substring(textBufferIndex,textBufferIndex+1);
			textBufferIndex++;
			ctc_timer = 0;
		} else {
			ctc_timer += 1;
		}
	}

	private String splitByLine(String instring) {
		String[] splitString = instring.split(" ");
		int maxChars = width / fontwidth;
		int numLines = 1;

		String outstring = splitString[0];

		for (int i = 1; i < splitString.length; i++) {
			if ( (outstring + " " + splitString[i]).length() <  maxChars*numLines)
				outstring = outstring + " " + splitString[i];
			else {
				outstring = outstring.trim() + "\n"+splitString[i];
				numLines++;
			}
		}

		return outstring;

	}

	public void drawBox() {
		if (show) {
			background.moveTo(x,y);
			background.stamp();
			if (ctc_timer>ctc_timeout) {
				font.draw(x+8,y+8, displayText+" ^");
			} else {
				font.draw(x+8,y+8, displayText);
			}
			
		}
		
	}

	public void show() {
		show = true;
	}

	public void hide() {
		show=false;
	}

	public void onInteract() {
		if (slowText) {

			if ((ctc_timer>ctc_timeout)) { // timeout has passed
				nextLine();
			} else if ((textBufferIndex < textBuffer.length())) { // finish the line instantly
				setTextFastDisplay(textBuffer);
				ctc_timer = 0;
			}

		} else { // automatically ctcs
			nextLine();
		}
		if (show) {
			Game.isDialogue = true;
		} else {
			Game.isDialogue = false;
		}
	
	}

	public void nextLine() {
		if (allText.size() == 0) { // if there's no more text
		
			hide();
			clearText();
			Game.isDialogue = false;

		} else {

			Game.isDialogue = true;
			show();
			setText(allText.get(0));
			Game.SOUND_DIALOGUE_START.play();
			allText.remove(0);

		}
	}

	// "Primes" the dialogue box.
	// Note that nothing will show at first. 
	public void addMultipleLines(String lines) {

		String[] allLines = lines.split("/");
		for (int i = 0; i < allLines.length; i++) {
			allText.add(allLines[i]);
		}

	}

	public void addMultipleLinesShow(String lines) {
		addMultipleLines(lines);
		nextLine();
	}



}