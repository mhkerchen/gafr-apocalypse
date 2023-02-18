// Made this for the mute button, but realized this code could be useful to 
// recycle later. 

import GaFr.GFStamp;

public class Button {



	// per-button variables

	private int icon;

	public int x;
	public int y;

	private int width;
	private int height;


	public Button(int newicon, int newx1, int newy1, int newwidth, int newheight) {

		icon = newicon;
		x = newx1;
		y = newy1;
		width = newwidth;
		height = newheight;

	}

	// Checks whether a click at x, y 
	public boolean isClick(int mousex, int mousey) {
		return ((mousex > x) && (mousex < x+width) && (mousey > y) && (mousey < y+height));

	}

	public int getImgIndex() {
		return this.icon;
	}

	public void setImgIndex(int newimg) {
		this.icon = newimg;
	}

	public void move(int newx, int newy) {

		x = newx;
		y = newy;

	}
}