import java.util.HashMap;

public class Prop { 

  int id;
  int icon;
  int x;
  int y;
  HashMap<String, String> metadata;
  boolean hasMetadata;
  boolean exists;
  
  // A prop has 4 arguments: the id, the icon, and the location in x and y coordinates.
  // Metadata, if any, is put in separately with the setMetadata argument.
   
    public Prop(int inid, int inicon, int inx, int iny) {
        id = inid;
        icon = inicon;
        x = inx;
        y = iny;
        metadata = new HashMap<String, String>();
        hasMetadata = false;
        exists = true;
    }

    public int getX() {
      return this.x;
    }

    public int getY() {
      return this.y;
    }

    public int getIcon() {
      return this.icon;
    }

    public String getString() {
      return "x: "+this.x+" y: "+this.y+" icon: "+this.icon+" id: "+this.id;
    }

    public void setMetadata(String metadata_raw) {
      // example metadata: {teleport:1,2; lock:blue_key}
      metadata_raw = metadata_raw.substring(1, metadata_raw.length() - 1); // cut off the {} and remove any whitespace
      if (metadata_raw.equals("")) {
        this.metadata.put("none", "none");
        this.hasMetadata = false;
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
      }


    }

    public void addMetadataKey(String key, String value) {
      this.metadata.put(key, value);
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
        Game.setCharX(teleportDestX);
        Game.setCharY(teleportDestY);
        Game.clearFog();
        return true;
      } 
      else if (this.metadata.containsKey("destination")) {
        return this.doDestination();
      }else if (this.metadata.containsKey("pickup_item")) {
        if (Game.addToInventory(metadata.get("pickup_item").trim()) == true) {
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
      return false;
    }

    public boolean doUnlock() {
        if ( !Game.inventoryTake(metadata.get("locked")) ) {
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
              return true;
            } else if (destinationMetadata.length == 3) {
              Game.loadLevel("assets/maps/"+destinationMetadata[0], Integer.parseInt(destinationMetadata[1]), Integer.parseInt(destinationMetadata[2]));
              return true;
            }
          }
          catch (Exception e) {
            System.out.println("Failure to load new map.");
            Game.errorScreen();
          }
          return false;
        }
        return false;
        
    }
}

