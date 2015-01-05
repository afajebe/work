package gridBasedData;

import util.Utility;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.StringTokenizer;


/**
 * This class can provide a PopulatedGrid based on US zipcode data.
 */
public class USZipCodeData {
	
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
	 * Read the US_ZIPCODE data and return a grid extraction
	 * 
	 * @param numPixels - The number of pixels to model
	 * @param scale - The scale of each pixels (0 to 1.0)
	 * 
	 * @return A modelable GridExtraction
	 */
	public static GridExtraction load(int numPixels, double scale) {
		PopulatedGrid grid = buildZipcodeGrid(standardWidth, standardHeight);
		GridExtraction extraction = grid.extractLargest(numPixels, scale, false);
		extraction.setXStepSize(actualXDistance / (double) standardWidth);
		extraction.setYStepSize(actualYDistance / (double) standardHeight);

		return extraction;
	}


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

				int trueZip = Integer.parseInt(st.nextToken());		//extract zipcode
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
	
	
	/**
	 * Build a grid of zipcode data
	 * 
	 * @param gridWidth - The number of pixels in the x direction 
	 * @param gridHeight - The number of pixels in the y direction
	 * 
	 * @return - A PopulatedGrid built from US Zipcode data
	 */
	private static PopulatedGrid buildZipcodeGrid(int gridWidth, int gridHeight) {
		
		System.out.println("Reading a grid of width :: " + gridWidth + " and height :: " + gridHeight);

		LinkedList<ZipcodeEntry> zipcodeList = readInputFile();
		
		
		//determine mins and maxs - add population
		double maxY = Double.NEGATIVE_INFINITY;
		double minY = Double.MAX_VALUE;
		double maxX = Double.NEGATIVE_INFINITY;
		double minX = Double.MAX_VALUE;	
		
		for (ZipcodeEntry elem : zipcodeList) {			
			maxY = Math.max(maxY, elem.y);
			minY = Math.min(minY, elem.y);
			maxX = Math.max(maxX, elem.x);
			minX = Math.min(minX, elem.x);
		}			
		
		//save total distances
		double width = Math.abs(maxX - minX);
		double height = Math.abs(maxY - minY);
		
		//System.out.println("width :: " + width);
		//System.out.println("height :: " + height);
				
		//////////////////////
		//  rasterize data  //
		//////////////////////		
		int[][] grid = new int[gridWidth][gridHeight];
		
		for (ZipcodeEntry elem : zipcodeList) {	
			
			int xIndex = (int) ((maxX - elem.x - .0001) / width * gridWidth);
			int yIndex = (int) ((maxY - elem.y - .0001) / height * gridHeight);
			
			elem.xCoor = xIndex;
			elem.yCoor = yIndex;

			grid[xIndex][yIndex] += elem.population;
		}
		
		printCoordinateMapping(zipcodeList);

		return new PopulatedGrid(grid);
	}
	
	
	/**
	 * Print the mapping of realworld zipcodes to (i,j) coordinates
	 * 
	 * @param zipcodeList - A list of ZipcodeEntry
	 */
	private static void printCoordinateMapping(LinkedList<ZipcodeEntry> zipcodeList) {

		StringBuffer buffer = new StringBuffer();
		DecimalFormat df = new DecimalFormat("#####");
		for (ZipcodeEntry entry : zipcodeList) {
			buffer.append(
				df.format(entry.realWorldZip) + "\t" +
				"(" + entry.xCoor + "," + entry.yCoor + ")\n");
		}
		
		try {
			Utility.writeToNewFile("ZipToCoordMapping.txt" , buffer.toString());
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}


	/** A simple wrapper for the data in the US zipcode text file. */
	private static class ZipcodeEntry {

		int realWorldZip;

		int population;

		double x;

		double y;

		int xCoor;

		int yCoor;

		ZipcodeEntry(int trueZip, int population, double x, double y) {
			this.realWorldZip = trueZip;
			this.population = population;
			this.x = x;
			this.y = y;
		}
	}
}
