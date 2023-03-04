import GaFr.GFSound;
import java.util.*;

public class Sfx {

    public static int stepNum = 0;
    public static int bgmDuration = 5*60 + 6; // 5 minutes 6 seconds

    public static GFSound STEP = new GFSound("assets/sounds/step.mp3");
    public static GFSound DOOR_OPEN = new GFSound("assets/sounds/doornoise.mp3");
    public static GFSound LEVER_ON = new GFSound("assets/sounds/lever.mp3");
    public static GFSound LEVER_OFF = new GFSound("assets/sounds/lever_off.mp3");
    public static GFSound DIALOGUE_START = new GFSound("assets/sounds/dialogue_go.mp3");
    public static GFSound BGM = new GFSound("assets/sounds/letting_go_coag.mp3");
    public static GFSound PICKUP = new GFSound("assets/sounds/pickup2.mp3");
    public static GFSound KEYCARD_FAILURE = new GFSound("assets/sounds/keycard_no.wav");
    public static GFSound KEYCARD_SUCCESS = new GFSound("assets/sounds/keycard_yes.wav");
    public static GFSound NEXT_LEVEL = new GFSound("assets/sounds/change_level.mp3");

    public static void playStep() {
        // eventually it'll cycle through step sfx, but this is good
        STEP.play();
    }
}