
import GaFr.GFStamp;
import GaFr.GFFont;
import GaFr.GFTexture;

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

	public void displayOneCharacter() {
		if (textBufferIndex < textBuffer.length()) {
			displayText = displayText+ textBuffer.substring(textBufferIndex,textBufferIndex+1);
			textBufferIndex++;
		}
	}

	private String splitByLine(String instring) {
		String[] splitString = instring.split(" ");
		int maxChars = width / fontwidth;
		int numLines = 1;

		String outstring = "";

		for (int i = 0; i < splitString.length; i++) {
			if ( (outstring + " " + splitString[i]).length() <  maxChars*numLines)
				outstring = outstring + " " + splitString[i];
			else {
				outstring = outstring + "\n"+splitString[i];
				numLines++;
			}
		}

		return outstring;

	}

	public void setDisplayText(String newtext) {
		displayText = splitByLine(newtext);
		textBuffer = "";
		textBufferIndex = 0;
	}

	public void drawBox() {
		font.draw(x,y, displayText);
	}



}