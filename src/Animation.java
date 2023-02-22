import GaFr.GFStamp;

public class Animation{


	// A simple animation.
	// If poll() is run 60x a second, img will switch between
	// all the images in the images list.
	// Can be treated as a GFStamp in the moveTo and stamp() actions

	public GFStamp[] images;
	public int wait; 

	public int img_index = 0;
	public GFStamp img;

	public int timeout;

	public Animation(String[] imglist, int framesSleep) {
		images = new GFStamp[imglist.length];
		for (int i = 0; i < imglist.length; i++) {
			images[i] = Game.textures[Game.tileDict.get(imglist[i])];
		}

		wait = framesSleep;
		timeout = framesSleep;
		img = images[0];
	}

	public void poll() {
		timeout--;
		if (timeout == 0) {
			img_index++;
			if (img_index == images.length) {
				img_index = 0;
			}
			img = images[img_index];
			timeout = wait;
		}
	}

	public void moveTo(int x, int y) {
		
        img.moveTo(x,y);
        img.stamp();
	}

}