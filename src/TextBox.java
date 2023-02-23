
import GaFr.GFStamp;
import GaFr.GFFont;
import GaFr.GFTexture;


import java.util.*;

public class TextBox {


	public GFFont font;
	public int fontwidth = 12;
	public int fontheight = 22;

	public int x;
	public int y;
	public int width;
	public int height;

	public String textBuffer; // a buffer of text that is revealed
	private int textBufferIndex = 0;

	public String displayText; // currently shown text
	public boolean show = false;
	public int ctc_timer = 0;

	public static boolean slowText = true;
	public static int ctc_timeout = 5;

    static TextBox dialogueBox;
    static boolean isDialogue = false;

	// a queue containing the text to display
	private ArrayList<String> allText = new ArrayList<String>();

	private static GFStamp background = new GFStamp(new GFTexture ("assets/images/textbox.png"));
	
	public TextBox (int newx, int newy, int newwidth, int newheight, GFFont newfont) {

		x = newx;
		y = newy;

		width = newwidth;
		if (newwidth > 800 ) {
			width = 800;
		}
		height = newheight;
		font = newfont;

	}

	// Clears the displayed text. 
	public void clearText() {
		textBuffer = "";
		displayText = "";
		textBufferIndex = 0;
	}

	// Primes a line of text to be displayed.
	public void setTextSlowDisplay(String newtext) {
		clearText();
		textBuffer = splitByLine(newtext);
	}

	// Instantly displays a line of text. 
	public void setTextFastDisplay(String newtext) {
		clearText();
		displayText = splitByLine(newtext);
	}

	// Displays a line of text.
	public void setText(String newtext) {
		if (slowText) {
			setTextSlowDisplay(newtext);
		} else {
			setTextFastDisplay(newtext);
		}
	}

	// Loads one character from the text buffer. 
	// If all text is shown, increment the click-to-continue timer. 
	public void displayOneCharacter() {
		if ((show) && (textBufferIndex < textBuffer.length())) {
			displayText = displayText+ textBuffer.substring(textBufferIndex,textBufferIndex+1);
			textBufferIndex++;
			ctc_timer = 0;
		} else {
			ctc_timer += 1;
		}
	}

	// Splits a string into lines, not breaking up words.
	// Note: Not height safe. Don't push your luck.
	private String splitByLine(String instring) {
		String[] splitString = instring.split(" ");
		int numLines = 1;
		String outstring = splitString[0];

		for (int i = 1; i < splitString.length; i++) {
			// if the current string plus the next word is shorter than 
			// the width of the textbox, add it
			if ( (outstring + " " + splitString[i]).length() <  (width / fontwidth)*numLines)
				outstring = outstring + " " + splitString[i];
			else {
				// otherwise, add a new line
				outstring = outstring.trim() + "\n"+splitString[i];
				numLines++;
			}
		}
		return outstring;
	}

	// Draws the textbox and text.
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
		show = false;
	}

	// Whenever the player hits the Interact key
	public void onInteract() {
		isDialogue = show;

		if (slowText) {
			// enough time has passed that you can click to continue
			if ((ctc_timer>ctc_timeout)) { 
				nextLine();

			// the line is still going, but you can fast-finish it
			} else if ((textBufferIndex < textBuffer.length())) { 
				setTextFastDisplay(textBuffer);
				ctc_timer = 0;
			}

		} else { // automatically ctcs
			nextLine();
		}
	}

	// Shows the next screen worth of text. 
	// Lines are split with a / symbol. 
	public void nextLine() {
		if (allText.size() == 0) { // if there's no more text
		
			hide();
			clearText();
			isDialogue = false;

		} else {

			show();
			setText(allText.get(0));
			isDialogue = true;
			Sfx.DIALOGUE_START.play();
			allText.remove(0); // remove line from queue

		}
	}

	// "Primes" the dialogue box.
	// Note that nothing will show at first. 
	public void addMultipleLines(String lines) {

		String[] allLines = lines.split("/");
		for (int i = 0; i < allLines.length; i++) {
			allText.add(allLines[i].trim());
		}

	}
}