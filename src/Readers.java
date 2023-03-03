/**
An attempt to organize things and standardize things.

All files should have the format:

	* Comma separated values. The reader separates all the values.
	* Ignores comments (start with /) and newlines. These should not error things.
	* Bracketed items should be processed separately.



Types of files existing so far, and their formats:

MAP = tile,tile,tile...tile,tile
	int,int,int or str,str,str
	Variable length, both width and height.

PROPS = icon, x, y, {metadata}
	int,int,int,{str}
	or str,int,int,{str}
		Metadata will always be in curly braces, and should be returned as a full string.
		First entry can be int OR str.
		Stretch goal: comma between y and {} is optional

DEFAULT_PROPS = name, icon, {metadata}
	str,int, {str}
	or str, str, {str}


 */

import java.util.*;

public class Readers{


	/* Splits the file by newlines, and returns the result.
	   Takes a string, as spit out by the GaFr to text function.
	   Trims lines and removes comments and blank spaces!
	*/
	public static String[] splitFileNewline(String text) {
		String[] textSplit = text.split("\n");
		ArrayList<String> manipulate = new ArrayList<String>();

		for (int i = 0; i < textSplit.length; i++) {
			if (lineValid(textSplit[i])) {
				manipulate.add(textSplit[i].trim());
				// System.out.println(textSplit[i].trim());
			}
		}

		textSplit = new String[manipulate.size()];
		return manipulate.toArray(textSplit);
	}

	

	// Returns True if the line is not a comment or whitespace.
	public static boolean lineValid(String line) {
		if (line.trim().equals("")) {
			return false;
		} else if (line.substring(0,2).equals("//")) {
			return false;
		}
		return true;
	}
	
	/*  Splits the line into a String[], comma separated.
		Does not care about whitespace, but trims each argument. 
		If there is metadata at the end of the string,
		it will be returned as a single entry with {}.
		Removes any ending commas.
		
	*/
	public static String[] splitLineStr(String text) {
		String meta = null;

		if (text.indexOf("{") != -1) { // if metadata exists, extract it
			
			meta = extractMetadata(text).trim();
			text = subtractMetadata(text); // save to put on the end
		}

		ArrayList<String> argmts = new ArrayList<String>();
		String[] arr = text.split(",");

		for (int i = 0; i < arr.length; i++) { // trim and append nonempty entries
			if (!arr[i].trim().equals("")) { // this statement is load bearing for some reason.
				argmts.add(arr[i].trim());
			}
		}

		if (meta != null) { // add meta back in
			argmts.add(meta);
		}

		// parse ArrayList to String array
		arr = new String[argmts.size()];
		return argmts.toArray(arr);
	}

	/*  Parses an array of Strings to integers, and returns that array.
		Optionally takes a starting position.

		3/2/2023: I DO NOT TRUST YOU
	*/
	public static int[] strToInt(String[] arr, int starti) {
		
		int[] arrint = new int[arr.length];
		for (int i = starti; i < arrint.length; i++) {
			try {
				arrint[i] = Integer.parseInt(arr[i]);
			} catch (Exception e) {
				System.out.println("Cannot parse \""+arr[i]+"\" to int");
			}
		}
		return arrint;

	}

	public static int[] strToInt(String[] arr) {
		return strToInt(arr, 0);
	}

	/* Returns the metadata, including brackets.*/
	public static String extractMetadata(String text) {
		int start = text.indexOf("{");
		int end = text.indexOf("}")+1;
		return text.substring(start, end);
	}

	/* Removes the metadata, including brackets.*/
	public static String subtractMetadata(String text) {
		int start = text.indexOf("{");
		int end = text.indexOf("}")+1;
		String meta = text.substring(start, end);
		return text.replace(meta, "");
	}

}