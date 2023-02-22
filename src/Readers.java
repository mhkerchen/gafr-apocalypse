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
	   Does NOT trim lines. 
	*/
	public static String[] splitFileNewline(String text) {
		return text.split("\n");
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
	/* Splits the file into a comma separated String[].
		Does not care about whitespace, but trims each argument. 
		If there is metadata at the end of the string, it will be removed.
		(Only one {} of metadata can be processed!)
		Empty strings will be removed.
	*/
	
	public static String[] splitLineStr(String text) {
		ArrayList<String> argmts = new ArrayList<String>();
		String meta = null;
		if (text.indexOf("{") != -1) {
			meta = extractMetadata(text);
			System.out.println("Metadata: "+meta);
			text = subtractMetadata(text);
			System.out.println("Remaining text: "+text);
		}
		String[] arr = text.split(",");
		for (int i = 0; i < arr.length; i++) {
			if (!arr[i].trim().equals("")) {
				argmts.add(arr[i].trim());
			}
		}
		if (meta != null) {
			argmts.add(meta);
		}
		arr = new String[argmts.size()];

		return argmts.toArray(arr);

	}

	public static int[] strToInt(String[] arr, int starti) {
		int[] arrint = new int[arr.length];
		for (int i = starti; i < arrint.length; i++) {
			try {
				arrint[i] = Integer.parseInt(arr[i]);
			} catch (Exception e) {
				System.out.println("Cannot parse "+arr[i]+" to string");
			}
		}
		return arrint;

	}

	public static String extractMetadata(String text) {
		int start = text.indexOf("{");
		int end = text.indexOf("}")+1;
		return text.substring(start, end);
	}


	public static String subtractMetadata(String text) {
		int start = text.indexOf("{");
		int end = text.indexOf("}")+1;
		String meta = text.substring(start, end);
		return text.replace(meta, "");
	}



}