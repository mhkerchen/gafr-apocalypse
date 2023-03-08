
import java.util.*;


public class Inventory {
	
    static final int INVENTORY_SIZE = 6;

    public static ArrayList<String> playerInventory = new ArrayList<String>();

	
  public static boolean addToInventory(String item) {
    if (playerInventory.size() < INVENTORY_SIZE) {
      playerInventory.add(item);
      return true;
    } else {
      System.out.println("You can't pick it up. Not enough room.");
      return false;
    }
  }

  public static boolean inventoryTake(String item) {
    if (playerInventory.contains(item)) {
      playerInventory.remove(item);
      return true;
    }
    else{
      System.out.println("You don't have what you need.");
      return false;
    }
  }

  public static boolean contains(String item) {
    return (playerInventory.contains(item));
  }

}