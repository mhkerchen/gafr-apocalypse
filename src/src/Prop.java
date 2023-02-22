import java.util.*;
import GaFr.GFU;

public class Prop { 

  int id;
  int icon;
  int x;
  int y;

  HashMap<String, String> metadata;
  boolean hasMetadata;
  
  boolean exists;
  boolean isPassable;

  Animation animation;
  boolean isAnimated;

  public static ArrayList<Integer> impassableProps = new ArrayList<Integer>();
  public static ArrayList<String> signals = new ArrayList<String>();
  public static Prop[] props;

  public static String[][] defaultProps; // n long, 3 wide

  // A prop has 4 arguments: the id, the icon, and the location in x and y coordinates.
  // Metadata, if any, is put in separately with the setMetadata argument.
   


   /*

   DM3 props

   
WALL_GLYPH,12,1,{none:none}
WALL_GLYPH,10,4,{none:none}
WALL_GLYPH,9,11,{none:none}
WALL_GLYPH,17,14,{none:none}

*/
    public Prop(int inid, int inicon, int inx, int iny) {
        id = inid;
        icon = inicon;
        x = inx;
        y = iny;
        metadata = new HashMap<String, String>();
        hasMetadata = false;
        exists = true;
    }

    public static String[] getDefaultProp(String name) {
      System.out.println(defaultProps.length);
      for (int i = 0; i < defaultProps.length; i++) {
        System.out.println(defaultProps[i][0]);
        if (defaultProps[i][0].equals(name)) {
        System.out.println("arg: "+defaultProps[i][1]);
        System.out.println("arg: "+defaultProps[i][2]);
          return defaultProps[i];
        }
        
      }
      return null;
    }

    public int getX() {
      return this.x;
    }

    public int getY() {
      return this.y;
    }

    public int getIcon() {
      if (isAnimated) {
        return animation.getImg();
      }
      return this.icon;
    }

    public String getString() {
      return "x: "+this.x+" y: "+this.y+" icon: "+this.icon+" id: "+this.id;
    }

    public boolean canPass() {
      return this.isPassable;
    }

    public void setMetadata(String metadata_raw) {
      // example metadata: {teleport:1,2; lock:blue_key}
      metadata_raw = metadata_raw.substring(1, metadata_raw.length() - 1); // cut off the {} and remove any whitespace
      if (metadata_raw.equals("")) {
        if (metadata.size() == 0) {
          this.metadata.put("none", "none");
          this.hasMetadata = false;
        }

      } else {
        String[] metadataArray = metadata_raw.split(";"); // example data now: ["teleport:1,2", "lock:blue_key"]
        String[] pairArray;
        for (int i = 0; i < metadataArray.length; i++) {
          pairArray = metadataArray[i].split(":");
          this.metadata.put(pairArray[0].trim(), pairArray[1].trim());
          //System.out.println("New piece of metadata added: "+pairArray[0].trim()+":"+pairArray[1].trim());
          // example data now: "teleport":"1,2", "lock":"blue_key"
          this.hasMetadata = true;
        }
        
        if (this.metadata.containsKey("animation")) {
          isAnimated = true;
          animation = Animation.loadAnimation(metadata.get("animation"));
        } else {
          isAnimated = false;
        }
      }
    }

    public void addMetadataKey(String key, String value) {
      this.metadata.put(key, value);
    }

    public static void initializeProps() {
      // must populate defaultProps;
      String[] propLines = Readers.splitFileNewline(GFU.loadTextFile("assets/defaultprops.txt"));
      defaultProps = new String[propLines.length][3];

      for (int i = 0; i < propLines.length; i++) {
        if (Readers.lineValid(propLines[i])) {
          System.out.println(propLines[i]);
          defaultProps[i] = Readers.splitLineStr(propLines[i]);
        } else {
          defaultProps[i] = new String[3];
        }
      }
    }

    // Called every time the player directly touches the object.
    public boolean tryOverlapAction() {
      if ( Game.editMode || !(this.exists)) {
        // actions are disabled in edit mode
        // nonexistent items are no longer interacted with
        return false;
      }

      if (this.metadata.containsKey("teleport")) {
        int teleportDestX = Integer.parseInt(metadata.get("teleport").trim().split(",")[0]);
        int teleportDestY = Integer.parseInt(metadata.get("teleport").trim().split(",")[1]);
        Player.p.setCharX(teleportDestX);
        Player.p.setCharY(teleportDestY);
        Fog.clearFog();
        return true;
      } 
      else if (this.metadata.containsKey("destination")) {
        return this.doDestination();
      }else if (this.metadata.containsKey("pickup_item")) {
        if (Inventory.addToInventory(metadata.get("pickup_item").trim()) == true) {
          // successful add, hide this item
          this.exists = false;
        } 

      }

      return false;
    }

    public boolean tryTouchAction() {
      if (this.metadata.containsKey("locked") ) {
        doUnlock();
      }
      if (this.metadata.containsKey("examine") ) {
        doExamine();
      }
      if (this.metadata.containsKey("inventory") ) {
        System.out.println(this.metadata.get("inventory"));
      }
      if (this.metadata.containsKey("signal")) {
        doSignal();
      }
      return false;
    }

    public boolean doUnlock() {
        if ( !Inventory.inventoryTake(metadata.get("locked")) ) {
          // you don't have it, so no door for you
          System.out.println("You don't have the key.");
          return false;
        }
        System.out.println("You unlocked it.");
        this.metadata.remove("locked");
        this.metadata.put("unlocked", "its_open");
        this.icon = this.icon - 1; // The standard for switching to the "unlocked" version.
        return true;

    }

    public boolean doExamine() {
      if (this.metadata.containsKey("examine")) {
        Game.dialogueBox.addMultipleLines(this.metadata.get("examine"));
        return true;
      } else {
        System.out.println("No examine");
        return false;
      }

    }

    public void doSignal() {
      // metadata format: signalcolor, offimage, onimage
      String[] metastr = metadata.get("signal").split(",");
      metastr[0] = metastr[0].trim();
      metastr[1] = metastr[1].trim();
      metastr[2] = metastr[2].trim();
      if (signals.contains(metastr[0])) {
        // it's on, turn it off
        signals.remove(metastr[0]);
        signals.add("NOT_"+metastr[0]);
        this.icon = Game.tileDict.get(metastr[1]);
        Game.SOUND_LEVER_PULL.play();
      } else {
        // it's off, turn it on
        signals.add(metastr[0]);
        signals.remove("NOT_"+metastr[0]);
        this.icon = Game.tileDict.get(metastr[2]);
        Game.SOUND_LEVER_PULL.play();

      }
      allSignalsUpdate();
    }
    
    public static void allSignalsUpdate() {
      // if it contains gate_control or another signal dependent meta,
      // check that it's at the proper state
      for (int pint = 0; pint < props.length; pint++) {
        if (props[pint].metadata.containsKey("gate_control")) {
          props[pint].updateGate();
        }
      }
    }

    public void updateGate() {
      //signals.contains("found")
      // {gate_control:GREEN,GATE_HOR,GATE_HOR_OPEN}
      String[] metastr = this.metadata.get("gate_control").split(",");
        // metastr = [SIG_COLOR, CLOSED_IMG, OPEN_IMG]
      if (signals.contains(metastr[0].trim())) {
        // signal is ON, switch to OPEN_IMG and set isPassable = true
        // TODO: add support for integer version also
        this.icon = Game.tileDict.get(metastr[2].trim());
        this.isPassable = true;

      } else {
        // signal is OFF, switch to CLOSED_IMG and set isPassable = false
        this.icon = Game.tileDict.get(metastr[1].trim());
        this.isPassable = false;
      }

    }


    // Go to the destination pointed to by this prop
    public boolean doDestination() {
        if (this.metadata.containsKey("locked")) {
          doUnlock();

        }
        if (!this.metadata.containsKey("locked")) {
          String[] destinationMetadata = metadata.get("destination").trim().split(",");
          try {
            if (destinationMetadata.length == 1) {
              Game.loadLevel("assets/maps/"+destinationMetadata[0]);
              allSignalsUpdate();
              return true;
            } else if (destinationMetadata.length == 3) {
              Game.loadLevel("assets/maps/"+destinationMetadata[0], Integer.parseInt(destinationMetadata[1]), Integer.parseInt(destinationMetadata[2]));
              allSignalsUpdate();
              return true;
            }
          }
          catch (Exception e) {
            System.out.println("Failure to load new map.");
            e.printStackTrace();
            Game.errorScreen();
          }
          return false;
        }
        return false;
        
    }




    // LOAD FUNCTIONS


    
  // Given a text file string formatted properly (see props_example.txt), will parse it into the Props list.
  // Props are any tile which can be interacted with.
  static Prop[] readProps(String propsString) {
    String[] propsArray = propsString.split("\n");
    String[] lineParser;
    Prop[] newProps = new Prop[propsArray.length];


    for (int i = 0; i < propsArray.length; i++) {
      lineParser = propsArray[i].split(",", 4);

      // if there are enough arguments, create a new prop
      if (lineParser.length > 3) {
        try {
          newProps[i] = new Prop(
            i,
            Integer.parseInt(lineParser[0].trim()),
            Integer.parseInt(lineParser[1].trim()),
            Integer.parseInt(lineParser[2].trim())
            );
            newProps[i].setMetadata(lineParser[3].trim());
        } catch (Exception e ){
          if (Game.tileDict.containsKey(lineParser[0].trim())) {

            newProps[i] = new Prop(
              i,
              Game.tileDict.get(lineParser[0].trim()),
              Integer.parseInt(lineParser[1].trim()),
              Integer.parseInt(lineParser[2].trim())
              );
            newProps[i].setMetadata(lineParser[3].trim());
          } else {
            // this is a custom prop
            System.out.println("Custom prop");
            newProps[i] = customProp(i, lineParser);
          }
        }
        System.out.println("done?");


        newProps[i].isPassable = !(impassableProps.contains(newProps[i].getIcon()));

        if (newProps[i].metadata.containsKey("canpass")) {
          newProps[i].isPassable = true;
        } else if (newProps[i].metadata.containsKey("cannotpass")) {
          newProps[i].isPassable = false;
        }


        // parses and stores the metadata

        // stores the prop's ID in the props map
        Game.propsMap[newProps[i].getX()][newProps[i].getY()] = i;
        
      }

    }

    return newProps;

  }


  public static Prop customProp(int i, String[] lineParser) {
    Prop custom;
    String key = lineParser[0].trim();
    String[] args;
    System.out.println("custom");
    if (getDefaultProp(key) != null) { // it has the prop
      System.out.println("go");
      args = getDefaultProp(key);
      System.out.println("CURRENTLY DEBUGGING");
      try {
        System.out.println(args[0]);
      } catch (Exception e) {
        System.out.println(key); // expected: WALL_GLYPH
          
      }
      System.out.println(lineParser[1].trim());
      System.out.println(lineParser[2].trim());
      System.out.println(args[2]); // metadata
      System.out.println("abt to make new prop");
      custom = new Prop(
        i,
        Game.tileDict.get(args[1]),
        Integer.parseInt(lineParser[1].trim()),
        Integer.parseInt(lineParser[2].trim())
        );
      //custom.setMetadata(lineParser[3].trim());
      System.out.println("3");
      custom.setMetadata(args[2]);
      System.out.println("4");

    } else {
      System.out.println("No prop with name '"+key+"' found.");
      custom = null;
    }
    System.out.println("stop");
    return custom;

  }

  public static void readImpassableProps() {
    String[] lines = GFU.loadTextFile("assets/image_indexes/impassible_props.txt").split("\n");
    for (int i = 0; i < lines.length-1; i++) {
      impassableProps.add(Game.tileDict.get(lines[i].trim()));
    }
  }



  public static Prop getProp(int index) {
    return props[index];
  }
}