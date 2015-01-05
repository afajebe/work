package gridBasedData;

import util.Utility;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.StringTokenizer;


/**
 * This utility is meant to supplement the current "US_48.txt" zipcode data with state data.
 */
public class ZipcodeUtility {

	private static String ZIPCODE_FILE = "US_48.txt";

	/** The map width when graphing the US_ZIPCODE data. */
	private static int standardWidth = 360;

	/** The map height when graphing the US_ZIPCODE data. */
	private static int standardHeight = 180;

	/** The US map is 57.56 units across. */
	private static double actualXDistance = 57.56;

	/** The US map is 24.33 units tall. */
	private static double actualYDistance = 24.33;


	/**
	 * Read the raw zipcode file
	 * 
	 * @return - Return a list of the parsed information packets
	 */
	private static LinkedList<ZipcodeEntry> readInputFile() {

		System.out.println("Reading US zipcode data flat file");

		LinkedList<ZipcodeEntry> zipcodeList = new LinkedList<ZipcodeEntry>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(ZIPCODE_FILE));

			StringTokenizer st;
			String line;

			br.readLine();	//discard numEntries line
			br.readLine();  //discard line that labels data columns

			line = br.readLine();  //read 1st line


			//a line consists of the following tokens
			//ZIPCODE_ID POPULATION_OF_ZIPCODE LATITUDE LONGITUDE
			while (line != null) {
				st = new StringTokenizer(line);

				String trueZip = st.nextToken();			//extract zipcode
				int population = Integer.parseInt(st.nextToken());	//save individual populations
				double latitude = Double.parseDouble(st.nextToken());
				double longitude = Double.parseDouble(st.nextToken());

				//save record of data
//				zipcodeList.addLast(new ZipcodeEntry(
//					population,
//					latitude,
//					longitude));
				zipcodeList.addLast(new ZipcodeEntry(
					trueZip,
					population,
					longitude,
					latitude
					));
				
				line = br.readLine();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}	
		
		return zipcodeList;
	}
	
	


	/** A simple wrapper for the data in the US zipcode text file. */
	private static class ZipcodeEntry {

		String realWorldZip;

		int population;		

		double x;

		double y;

		int xCoor;

		int yCoor;
		
		String state;

		ZipcodeEntry(String trueZip, int population, double x, double y) {
			this.realWorldZip = trueZip;
			this.population = population;
			this.x = x;
			this.y = y;
		}
	}


	public static void main(String[] args) {

		/** A Utility class. */
		class ZipStatePair {
			String zip;
			String state;
			
			ZipStatePair(String zip, String state) {
				this.state = state;
				this.zip = zip;
			}
		}
		

		//create a list of mappings from zipcode to state
		LinkedList<ZipStatePair> pairs = new LinkedList<ZipStatePair>();		

		//read the ZIP to STATE map
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
				
				pairs.addLast(new ZipStatePair(trueZip, state));

				line = br.readLine();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
		
		
		//get the list of zipcodes
		LinkedList<ZipcodeEntry> zipcodes = readInputFile();
		
		//give each zipcode a state
		ZipStatePair[] array = pairs.toArray(new ZipStatePair[0]);		
		for (ZipcodeEntry zip : zipcodes) {
			for (int i = 0; i < array.length; i++) {
				if(array[i].zip.equals(zip.realWorldZip)) {
					zip.state = array[i].state;
				}
			}
		}
		
		
		//write out the results
		StringBuffer output = new StringBuffer();
		for (ZipcodeEntry zip : zipcodes) {
			output.append(
				zip.realWorldZip + "\t" + 
				zip.population + "\t" +
				zip.y + "\t" +
				zip.x + "\t" +
				zip.state + "\n");
		}

		try {
			Utility.writeToNewFile("withZipData.txt", output.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
