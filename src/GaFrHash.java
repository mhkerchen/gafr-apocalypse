/* Emulates a HashMap, since GaFr doesn't play nice.*/

/*
Cons of GaFr Hash:
	* Haven't tested it, but it's definitely slower than a HashMap
	* O(n) retrieval time
	* Doesn't have all hashmap features
	* I don't trust it because I wrote it

Benefits of GaFr Hash:
	* It works
	* Same syntax as a hashmap
*/

import java.util.*;

public class GaFrHash<K,V> {

	public ArrayList<K> keys;
	public ArrayList<V> values;

	// Create a new "hashmap".
	public GaFrHash () {
		keys = new ArrayList<K>();
		values = new ArrayList<V>();
	}
	
	// Place a new key, value pair on the "hashmap".
	public void put (K key, V value) {
		if (this.containsKey(key)) {
			System.out.println("warning; duplicate key detected in hashmap");
		}
		keys.add(key);
		values.add(value);

	}

	// Gets the value stored by K key. Returns null if nothing is found.
	public V get (K key) {
		if (this.containsKey(key)) {
			return values.get(keys.indexOf(key));
		} else {
			System.out.println("Error: key \""+key+"\" not found in GaFrHash");
			return null;
		}
		
	}

	// Checks whether the "hashmap" contains said key. 
	public boolean containsKey(K key) {
		return keys.contains(key);
	}
}