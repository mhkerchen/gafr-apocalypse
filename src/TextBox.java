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


	public String displayText; // currently shown text
	public String textBuffer; // a buffer of text that is revealed
	private int textBufferIndex = 0;
	public int ctc_timer = 0;
	public boolean show = false;

	public boolean continueFlash = false;

    static TextBox dialogueBox;
    static boolean isDialogue = false; // freezes input/output

	// a "queue" containing the text to display
	private ArrayList<String> allText = new ArrayList<String>();

	static GFStamp background = new GFStamp(new GFTexture ("assets/images/textbox.png"));
	static boolean slowText = true;
	static int ctc_timeout = 5;
    static GFFont englishFont;

	
	public TextBox (int newx, int newy, int newwidth, int newheight, GFFont newfont) {

		x = newx;
		y = newy;

		width = newwidth;
		if (newwidth > 800 ) {
			width = 800;
		}

		height = newheight;
		font = newfont;
		fontwidth = 12;
		fontheight = 22;

	}

	// Clears the displayed text. 
	public void clearText() {
		textBuffer = "";
		displayText = "";
		textBufferIndex = 0;
	}

	/* Primes a line of text to be displayed
	   by putting it into the buffer.*/
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

	/*	Loads one character from the text buffer. 
		If all text is shown, increment the click-to-continue timer. 
		Polled once per frame. */
	public void displayOneCharacter() {
		if ((show) && (textBufferIndex < textBuffer.length())) {
			displayText = displayText + textBuffer.substring(textBufferIndex,textBufferIndex+1);
			textBufferIndex++;
			ctc_timer = 0;
		} else {
			ctc_timer += 1;
		}
	}

	/*Splits a string into lines, not breaking up words.
	  Note: Not height safe. Don't push your luck.*/
	private String splitByLine(String instring) {
		String[] splitString = instring.split(" ");

		int numLines = 1;
		String outstring = splitString[0];
		for (int i = 1; i < splitString.length; i++) {
			// if the current string plus the next word is shorter than 
			// the width of the textbox, add it
			if ( (outstring + " " + splitString[i]).length() <  (width / fontwidth)*numLines)
				outstring += " " + splitString[i];
			else {
				// otherwise, add a new line
				outstring += "\n"+splitString[i];
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
			
			if (ctc_timer > ctc_timeout) { 
				// timeout has passed, show ctc prompt
				//updateCount
				if (Game.updateCount%60 == 0) {
					continueFlash = !continueFlash;
				}

				if (continueFlash) {
					font.draw(x+8,y+8, displayText+" ^");
				} else {
					font.draw(x+8,y+8, displayText+" ~");
				}
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

	/*Whenever the player hits the Interact key, this runs.
	  Determines whether the next line will be shown or
	  the text will finish fast-typing.
	*/
	public void onInteract() {
		isDialogue = show;

		if (slowText) {

			// timer hasn't timed out and the text buffer 
			// has not been fully displayed
			if ((ctc_timer<ctc_timeout) && (textBufferIndex < textBuffer.length())) { 
				setTextFastDisplay(textBuffer);
				ctc_timer = 0;
			} else {
				// enough time has passed that you can click to continue
				nextLine();
			}

		} else { // click to continue
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

	/* "Primes" the dialogue box.
	   Note that nothing will show at first. 
	   nextLine() must be called to show dialogue. 
	*/
	public void addMultipleLines(String lines) {

		String[] allLines = lines.split("/");
		for (int i = 0; i < allLines.length; i++) {
			allText.add(allLines[i].trim());
		}

	}



	// Initialize the fonts (currently just the English font)
	// and create the textbox
  	static void initText() {
		GFStamp[] glyphs;
		glyphs = new GFTexture("assets/fonts/bittext.png", 0xff000000, 0xffffffff).splitIntoTilesBySize(12,22);
		englishFont = new GFFont(glyphs,
			"abcdefghijklmnopqrstuvwxyz                          ?!.,-:()1234567890#*'^% ~-ABCDEFGHIJKLMNOPQRSTUVWXYZ" );
		dialogueBox = new TextBox(0, 400, 800-16, 100-16, englishFont);
  	}

}