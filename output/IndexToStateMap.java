package output;


import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.TreeMap;


/**
 * This class provides a "stateFinder" treeMap that can quickly convert ModelPlaceIndex values
 * to the proper state
 */
public class IndexToStateMap {

	/**  A Map that can receive ModelPlaceIndex values and quickly return a state. */
	public static TreeMap<Integer, String> stateFinder;
	


	static {
		stateFinder = createMapping();
	}


	/** Build the mapping from ModelPlaceIndex to States */
	private static TreeMap<Integer, String> createMapping() {
		ZipStateIndexTriple[] triples = readStateKey();

		TreeMap<Integer, String> map = new TreeMap<Integer, String>();
		for (int i = 0; i < triples.length; i++) {
			map.put(triples[i].modelPlaceIndex, triples[i].state);
		}
		return map;
	}


	
	private static class ZipStateIndexTriple implements Comparable {

		public final String state;

		public final int modelPlaceIndex;

		public final String realZip;


		/**
		 * Create a new triple from a line in the StateKey file
		 * 
		 * @param fileLine - A String like "realZip state modelPlaceIndex"
		 */
		ZipStateIndexTriple(String fileLine) {
			StringTokenizer st = new StringTokenizer(fileLine);

			this.realZip = st.nextToken();
			this.state = st.nextToken();
			this.modelPlaceIndex = Integer.parseInt(st.nextToken());
		}


		/** Sort by ZipStateIndexTriple objects by modelPlaceIndex. */
		public int compareTo(Object o) {
			ZipStateIndexTriple other = (ZipStateIndexTriple) o;
			return this.modelPlaceIndex - other.modelPlaceIndex;
		}
	}


	/** Read the "StateKey.txt" file and return the data inside it. */
	private static ZipStateIndexTriple[] readStateKey() {

		LinkedList<ZipStateIndexTriple> triples = new LinkedList<ZipStateIndexTriple>();

		try {
			BufferedReader br = new BufferedReader(new FileReader("StateKey.txt"));

			StringTokenizer st;
			String line;

			line = br.readLine();  //read 1st line

			while (line != null) {
				triples.addLast(new ZipStateIndexTriple(line));
				line = br.readLine();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}

		return triples.toArray(new ZipStateIndexTriple[0]);
	}
}
