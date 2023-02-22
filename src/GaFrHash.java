// GaFr doesn't play nice with HashMaps?
// Fine, I'll do it myself.
/*

Cons of GaFr Hash:
	* Haven't tested it, but it's definitely slower than a HashMap
	* O(n) retrieval time
	* Doesn't have all hashmap features

Benefits of GaFr Hash:
	* It works
	* Same syntax as a hashmap


*/

import java.util.*;


public class GaFrHash<K,V> {

	public ArrayList<K> keys;
	public ArrayList<V> values;

	public GaFrHash () {
		keys = new ArrayList<K>();
		values = new ArrayList<V>();
	}
	

	public void put (K key, V value) {
		keys.add(key);
		values.add(value);

	}

	public V get (K key) {
		return values.get(keys.indexOf(key));
	}

	public boolean containsKey(K key) {
		return keys.contains(key);
	}
}