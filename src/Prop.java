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

  
  public Prop(int inid, int inicon, int inx, int iny, String metadata_raw) {
      id = inid; 
      icon = inicon;
      x = inx;
      y = iny;
      hasMetadata = false;
      exists = true;

      metadata = new HashMap<String, String>();
      this.setMetadata(metadata_raw);
  }

  // GETTERS/SETTERS

  


  public void addMetadataKey(String key, String value) {
    this.metadata.put(key, value);
  }
  
  public String[] getAttribute(String key) {
    String[] temp = this.metadata.get(key).split(",");
    for (int i = 0; i < temp.length; i++) {
      temp[i] = temp[i].trim();
    }
    return temp;
  }

  public int getX() {
    return this.x;
  }

  public int getY() {
    return this.y;
  }

  public String getString() {
    return "x: "+this.x+" y: "+this.y+" icon: "+this.icon+" id: "+this.id;
  }

  // Retrieves the index of the Stamp that should be used. 
  public int getImg() {
    if (isAnimated) {
      return animation.getImg();
    }
    return this.icon;
  }

  public boolean canPass() {
    return this.isPassable;
  }

  public static Prop getProp(int index) {
    return props[index];
  }


  // Retrieves the 3-wide array of a default prop [NAME, IMAGE, METADATA]
  public static String[] getDefaultProp(String name) {
    
    for (int i = 0; i < defaultProps.length; i++) {
      if (defaultProps[i][0].equals(name)) {
        return defaultProps[i];
      }
    }
    return null;
  }


    // METADATA SETTERS


    // Processes a piece of metadata. 
    public void setMetadata(String metadata_raw) {
      
      // cut off the {} and remove any whitespace
      if (metadata_raw.trim().substring(0,1).equals("{")) {
        metadata_raw = metadata_raw.substring(1, metadata_raw.length() - 1);
      } else {
        metadata_raw = metadata_raw.trim();
      }
      // no metadata to be added
      if (metadata_raw == null || metadata_raw.equals("")) {
        if (metadata.size() == 0) {
          addMetadataKey("none", "true");
          this.hasMetadata = false;
        }

      // add metadata
      } else {
        String[] metadataArray = metadata_raw.split(";");
        String[] pairArray;

        for (int i = 0; i < metadataArray.length; i++) {
          pairArray = metadataArray[i].split(":");
          addMetadataKey(pairArray[0].trim(), pairArray[1].trim());
        }
        
        this.hasMetadata = true;
      }

      this.checkMetadataTraits();
    }

    public void checkMetadataTraits() {

      // check for animation
      if (this.metadata.containsKey("animation")) {
        isAnimated = true;
        animation = Animation.loadAnimation(metadata.get("animation"));
      } else {
        isAnimated = false;
      }

      // determine whether this is an impassable block
      this.isPassable = !(impassableProps.contains(this.getImg()));

      // check for override meta
      if (this.metadata.containsKey("canpass")) {
        this.isPassable = true;
      } else if (this.metadata.containsKey("cannotpass")) {
        this.isPassable = false;
      }

    }




  // LOAD FUNCTIONS


    
  // Given a text file string formatted properly (see props_example.txt), will parse it into the Props list.
  // Props are any tile which can be interacted with.
  public static void readProps(String filename) {
    String propsString = "";
    try {
      propsString = GFU.loadTextFile(filename);
    } catch (Exception e) {
      System.out.println("Could not read props from "+filename);
    }
    
    String[] propsArray = Reader.splitFileNewline(propsString);
    String[] args;
    Prop[] newProps = new Prop[propsArray.length];
    //clearPropsList();

    for (int i = 0; i < propsArray.length; i++) {
      args = Reader.splitLineStr(propsArray[i]);

      if (args.length > 3) {
        
        try {
          newProps[i] = new Prop(
            i,
            Game.translate(Integer.parseInt(args[0])),
            Integer.parseInt(args[1]),
            Integer.parseInt(args[2]),
            args[3]
            );
            //newProps[i].setMetadata(args[3]);
        } catch (Exception e) {
          if (Game.tileDict.containsKey(args[0])) { 

            // standard id given
            newProps[i] = new Prop(
              i,
              Game.translate(args[0]),
              Integer.parseInt(args[1]),
              Integer.parseInt(args[2]),
              args[3]
              );
              //newProps[i].setMetadata(args[3]);

          } else {
            // preset id given
              newProps[i] = customProp(i, args);

          }

        }

        // stores the prop's ID in the props map
        
      } else {
        System.out.println("Warning: Line \""+propsArray[i]+"\" has too few arguments.");
      }
    }

    System.out.println("Props loaded.");
    props = newProps;
  }
  // Creates a prop with preset metadata.
  // Check defaultprops.txt for full list.
  public static Prop customProp(int i, String[] lineParser) {
    Prop custom;
    String key = lineParser[0].trim();
    String[] args;
    
    // Prop is properly defined
    if (getDefaultProp(key) != null) {
    
      args = getDefaultProp(key);

      // Name of the icon is a valid name
      if (Game.tileDict.containsKey(args[1])) {
        custom = new Prop(
          i,
          Game.tileDict.get(args[1]),
          Integer.parseInt(lineParser[1].trim()),
          Integer.parseInt(lineParser[2].trim()),
          args[2]
          );
      } else { 
        // Name of the icon is not valid. Defaults to nothing.
        // Can be intentional behaviour. 
        custom = new Prop(
          i,
          Game.translate("NOTHING"),
          Integer.parseInt(lineParser[1].trim()),
          Integer.parseInt(lineParser[2].trim()),
          args[2]
          );
        System.out.println("Warning: No icon given for prop \""+key+"\".");
      }

      //custom.setMetadata(lineParser[3].trim());
      //custom.setMetadata(args[2]);

    } else {
      System.out.println("No prop with name '"+key+"' found.");
      custom = null;
    }
    return custom;

  }

  // Determines which props are impassable. 
  public static void readImpassableProps() {
    String[] lines = GFU.loadTextFile("assets/data/impassable_props.txt").split("\n");
    for (int i = 0; i < lines.length-1; i++) {
      impassableProps.add(Game.translate(lines[i].trim()));
    }
  }

  // Populates defaultProps.
  public static void initializeProps() {
    
    readImpassableProps();
    String[] propLines = Reader.splitFileNewline(GFU.loadTextFile("assets/data/defaultprops.txt"));
    defaultProps = new String[propLines.length][3];

    for (int i = 0; i < propLines.length; i++) {
      if (Reader.lineValid(propLines[i])) {
        defaultProps[i] = Reader.splitLineStr(propLines[i]);
      } else {
        defaultProps[i] = new String[3]; // empty placeholder. Should be benign.
      }
    }
  }




  // INTERACTIONS




  // Called every time the player shares space with the object.
  public void tryOverlapAction() {
    //System.out.println("Overlapping "+this.id);
    if ( Game.editMode || (!this.exists) ) {
      return;
    }

    if (this.metadata.containsKey("teleport")) {
      this.doTeleport();
    } 
    if (this.metadata.containsKey("destination")) {
      this.doDestination();
    } 
    if (this.metadata.containsKey("pickup_item")) {
      if (Inventory.addToInventory(metadata.get("pickup_item"))) {
        // successful add, hide this item
        Sfx.PICKUP.play();
        this.exists = false;
      } 
    }

    return;
  }

  // Called everytime the player hits Enter while facing an interactible.
  public void tryTouchAction() {
    if ( Game.editMode || (!this.exists) ) {
      return;
    }
    if (this.metadata.containsKey("locked") ) {
      this.doUnlock();
    }
    if (this.metadata.containsKey("examine") ) {
      this.doExamine();
    }
    /*if (this.metadata.containsKey("inventory") ) {
      System.out.println(this.metadata.get("inventory"));
    }*/
    if (this.metadata.containsKey("signal")) {
      this.doSignal();
    }

    return;
  }

  // Teleports the player to another location on the same level.
  public boolean doTeleport() {
    String[] args = getAttribute("teleport");
    Player.cur.setCharX(Integer.parseInt(args[0]));
    Player.cur.setCharY(Integer.parseInt(args[1]));
    Fog.clearFog();
    return true;
  }

  public boolean doUnlock() {
      if (!(metadata.get("locked").equals("none")) && !Inventory.inventoryTake(metadata.get("locked")) ) {
        // you don't have it, so no door for you
        Sfx.KEYCARD_FAILURE.play();
        TextBox.dialogueBox.addMultipleLines("locked.");
      } else {
        this.metadata.remove("locked");
        this.metadata.put("unlocked", "its_open");
        this.isPassable = true;
        Sfx.KEYCARD_SUCCESS.play();
        this.icon = this.icon - 1; // The standard for switching to the "unlocked" version.
      }
      return true;

  }

  public boolean doExamine() {
    TextBox.dialogueBox.addMultipleLines(this.metadata.get("examine"));
    return true;

  }


  public void doSignal() {
    // metadata format: signalcolor, offimage, onimage
    String[] args = getAttribute("signal");
    
    // it's on, turn it off
    if (signals.contains(args[0])) {
      signals.remove(args[0]);
      signals.add("NOT_"+args[0]);

      this.icon = Game.translate(args[1]);
      Sfx.LEVER_OFF.play();

    // it's off, turn it on
    } else {
      signals.add(args[0]);
      signals.remove("NOT_"+args[0]);

      this.icon = Game.translate(args[2]);
      Sfx.LEVER_ON.play();

    }
    allSignalsUpdate();
  }
  
  public static void allSignalsUpdate() {
    // if it contains gate_control or another signal dependent meta,
    // check that it's at the proper state

    checkCombinedSignals();
    for (int i = 0; i < props.length; i++) {
      if (props[i].metadata.containsKey("gate_control")) {
        props[i].updateGate();
      }
    }
  }


  // Combines any signals as required. 
  public static void checkCombinedSignals() {
    if (signals.contains("BLUE") && signals.contains("RED")) { // should contain blue_red
      signals.add("RED_BLUE");
      if (signals.contains("NOT_RED_BLUE")) {signals.remove("NOT_RED_BLUE");}
    } else {
      signals.add("NOT_RED_BLUE");
      if (signals.contains("RED_BLUE")) {signals.remove("RED_BLUE");}
    }
    
    if (signals.contains("BLUE") && signals.contains("GREEN")) {
      signals.add("BLUE_GREEN");
      if (signals.contains("NOT_BLUE_GREEN")) {signals.remove("NOT_BLUE_GREEN");}
    } else {
      signals.add("NOT_BLUE_GREEN");
      if (signals.contains("BLUE_GREEN")) {signals.remove("BLUE_GREEN");}
    }

    
    if (signals.contains("RED") && signals.contains("GREEN")) {
      signals.add("RED_GREEN");
      if (signals.contains("NOT_RED_GREEN")) {signals.remove("NOT_RED_GREEN");}
    } else {
      signals.add("NOT_RED_GREEN");
      if (signals.contains("RED_GREEN")) {signals.remove("RED_GREEN");}
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
      this.icon = Game.translate(metastr[2].trim());
      this.isPassable = true;

    } else {
      // signal is OFF, switch to CLOSED_IMG and set isPassable = false
      this.icon = Game.translate(metastr[1].trim());
      this.isPassable = false;
    }

  }


  // Go to the destination pointed to by this prop
  public boolean doDestination() {
      if (!this.metadata.containsKey("locked")) {
        String[] args = getAttribute("destination");
        try {
          if (args.length == 1) { // no x,y dest given (will search for spawn)
            Game.loadLevel("assets/maps/"+args[0]);
            allSignalsUpdate();
            return true;
          } else if (args.length == 3) { // x,y dest given
            Game.loadLevel("assets/maps/"+args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            allSignalsUpdate();
            return true;
          }
        }
        catch (Exception e) {
          System.out.println("Failure to load new map at "+"assets/maps/"+args[0]+".");
          e.printStackTrace();
          Game.errorScreen();
        }
        return false;
      }
      return true;
      
  }



  // Determine if there is a prop at position x,y.
  public static boolean isValid(int x, int y) {
    for (int prop = 0; prop < props.length; prop++) {
      if (props[prop]==null) {
        return false; 
      } else if (props[prop].x == x && props[prop].y == y){
        return true;
      }
    }
    return false;
  }

  // Returns the first prop at a specific location. 
  public static Prop propAt(int x, int y) {
    for (int prop = 0; prop < props.length; prop++) {
      if (props[prop]==null) {
        System.out.println("Error: No prop found at "+x+", "+y+" (case 1)");
        return null;
      } else if (props[prop].x == x && props[prop].y == y){
        return props[prop];
      }
    }
    System.out.println("Error: No prop found at "+x+", "+y+" (case 1)");
    return null;

  }

}