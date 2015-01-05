package output;

import util.Utility;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.StringTokenizer;


/**
 * The US Model produces output that is indexed by "ModelPlace index".  ModelPlace indices are
 * not related to any real world piece of info.  
 * 
 * 
 * This class produces a file "StateKey.txt" which can be used to conver data from a 
 * "by ModelPlaceIndex" format to a "by state" format.  The "StateKey.txt" file contains three
 * columns: Original Zipcode, State, and ModelPlaceIndex.  Some of the entries in the 
 * ModelPlaceIndex will be "-1" --- which indicates that this zipcode was not included in the 
 * modeled population because its population was too small.
 * 
 * 
 * In order to produce the "StateKey" this class:
 *	- Loads "US_48.txt", the file that lists all zipcodes
 *	- Loads "ZIP_CODES.txt", the file that lists the state of each zipcode
 *	- Loads "ZipToCoordMapping.txt", the file that lists each zipcodes the "i,j" coordinate
 *	- Loads "CoordToIndexMapping.txt", the file that lists each "i,j" coordinates "ModelPlaceIndex"
 */
public class ModelPlaceToStateKey {
	
	static class CompleteEntry {
		String realZip;
		String state;
		String xyCoord;
		int modelPlaceIndex;
		
		CompleteEntry(String realZip) {
			this.realZip = realZip;
			this.modelPlaceIndex = -1;
		}
		
		public String toString() {
			return realZip + "\t" + state + "\t" + modelPlaceIndex;
		}
	}


	public static void main(String[] args) {
				
		LinkedList<CompleteEntry> dataList = new LinkedList<CompleteEntry>();
		
		//read int "US_48.txt"
		System.out.println("Getting a list of valid zipcodes");
		try {
			BufferedReader br = new BufferedReader(new FileReader("US_48.txt"));

			StringTokenizer st;
			String line;
			
			br.readLine();	//discard numEntries line
			br.readLine();  //discard line that labels data columns

			line = br.readLine();  //read 1st line			

			//a line consists of the following tokens
			//ZIPCODE_ID POPULATION_OF_ZIPCODE LATITUDE LONGITUDE
			while (line != null) {
				st = new StringTokenizer(line);

				String trueZip = st.nextToken();

				dataList.addLast(new CompleteEntry(trueZip));
				line = br.readLine();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
		
		CompleteEntry[] dataArray = dataList.toArray(new CompleteEntry[0]);
		

		//read "ZIP_CODES.txt" a file that maps zipcodes to states
		System.out.println("Assign each zipcode a state");
		try {
			BufferedReader br = new BufferedReader(new FileReader("ZIP_CODES.txt"));

			StringTokenizer st;
			String line;

			line = br.readLine();  //read 1st line

			//a line consists of the following tokens
			//ZIPCODE STATE
			while (line != null) {
				st = new StringTokenizer(line);

				String trueZip = st.nextToken();
				String state = st.nextToken();
				
				for (int i = 0; i < dataArray.length; i++) {
					if(dataArray[i].realZip.equals(trueZip)) {
						dataArray[i].state = state;
					}
				}

				line = br.readLine();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
	
		
		//read "ZipToCoordMapping.txt" a file that maps zipcodes to i,j coordinates
		System.out.println("Assign each zipcode a Coordinate");
		try {
			BufferedReader br = new BufferedReader(new FileReader("ZipToCoordMapping.txt"));

			StringTokenizer st;
			String line;

			line = br.readLine();  //read 1st line

			//a line consists of the following tab delimited tokens
			//ZIPCODE (i,j)
			while (line != null) {
				st = new StringTokenizer(line, "\t");

				int trueZip = Integer.parseInt(st.nextToken());
				String coord = st.nextToken();

				for (int i = 0; i < dataArray.length; i++) {
					if (Integer.parseInt(dataArray[i].realZip) == trueZip) {
						dataArray[i].xyCoord = coord;
					}
				}

				line = br.readLine();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
		
		//read "CoordToIndexMapping.txt" a file that maps zipcodes to i,j coordinates
		System.out.println("Assign each Coordinate a ModelPlace Index");
		try {
			BufferedReader br = new BufferedReader(new FileReader("CoordToIndexMapping.txt"));

			StringTokenizer st;
			String line;

			line = br.readLine();  //read 1st line

			//a line consists of the following tab delimited tokens
			//(i,j) ModelPlaceIndex
			while (line != null) {
				st = new StringTokenizer(line, "\t");

				String coord = st.nextToken();
				int placeIndex = Integer.parseInt(st.nextToken());

				for (int i = 0; i < dataArray.length; i++) {
					if (dataArray[i].xyCoord.equals(coord)) {
						dataArray[i].modelPlaceIndex = placeIndex;
					}
				}

				line = br.readLine();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}

		
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < dataArray.length; i++) {
			sb.append(dataArray[i].toString() + "\n");
		}
		
		try {
			Utility.writeToNewFile("StateKey.txt", sb.toString());
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
