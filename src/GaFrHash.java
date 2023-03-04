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
			// replace the preexisting value
			int index = keys.indexOf(key);
			values.set(index, value);

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

	// Get the length.
	public int size() {
		return keys.size();
	}

	// Given a value, get the key.
	public K getKey(V value) {
		
	if (values.indexOf(value) != -1) {
		return keys.get(values.indexOf(value));
	} else {
		System.out.println("Error: value \""+value+"\" not found in GaFrHash");
		return null;
	}
	}

	// Returns an array of keys
	public ArrayList<K> getKeyArray() {
		return this.keys;
	}

	public void print() {
		for (int i = 0; i < this.size(); i++) {
			System.out.println(keys.get(i)+" : "+values.get(i));
		}
	}
}