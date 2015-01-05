package landScan;

import gridBasedData.GridExtraction;
import gridBasedData.PopulatedGrid;


/**
 * This class is meant to provide easy access to the North American Extraction of
 * Land Scan data.
 * 
 * It provides access to the high resolution grid that only includes North American data.  This 
 * way, you can easily reduce resolution as required (using the PopulatedGrid class) and extract
 * the number of pixels you want (also using the PopulatedGrid class)
 * 
 * 
 * This class is meant to extract the North American data from the LandScan grid
 */
public class NorthAmericanLandScan {

	/** The file that will recieve the list of biggest 'grid squares'. */
	private static String OUTPUT_FILE = "NorthAmericaExtraction.txt";
	
	private static final int lowX = 300;
	private static final int highX = 725;
	private static final int lowY = 200;
	private static final int highY = 425;
	
	private static final int caribeanX_1 = 560;
	private static final int caribeanY_1 = 360;
		
	private static final int caribeanX_2 = 602;
	private static final int caribeanY_2 = 337;
	
	private static final int caribeanX_3 = 682;
	private static final int caribeanY_3 = 300;
	

//	/**
//	 * Compute, and return a hi-res extraction of the North American Land Scan Data
//	 * 
//	 * @return - A wrapped Grid of data.
//	 */
//	public static PopulatedGrid getHiResNorthAmericanData() {
//	
//		//compute the sub grid -- that only includes North America
//		int[][] fullGrid = RawLandScanReader.grid;
//		int[][] subGrid = new int[highY - lowY][highX - lowX];
//		for (int i = 0; i < subGrid.length; i++) {
//			for (int j = 0; j < subGrid[i].length; j++) {
//				subGrid[i][j] = fullGrid[i + lowY][j + lowX];
//			}
//		}
//		//remove the caribean -- part 1
//		for (int i = caribeanY_1 - lowY; i < subGrid.length; i++) {
//			for (int j = caribeanX_1 - lowX; j < subGrid[i].length; j++) {
//				subGrid[i][j] = 0;
//			}
//		}
//		//remove the caribean -- part 2
//		for (int i = caribeanY_2 - lowY; i < subGrid.length; i++) {
//			for (int j = caribeanX_2 - lowX; j < subGrid[i].length; j++) {
//				subGrid[i][j] = 0;
//			}
//		}
//		//remove the caribean -- part 2
//		for (int i = caribeanY_3 - lowY; i < subGrid.length; i++) {
//			for (int j = caribeanX_3 - lowX; j < subGrid[i].length; j++) {
//				subGrid[i][j] = 0;
//			}
//		}
//		
//		//return subGrid;
//		return new PopulatedGrid(subGrid);
//	}
	
	
	/**
	 * Compute, and return a low-res extraction of the North American Land Scan Data
	 * 
	 * @return - A wrapped Grid of data.
	 */
	public static PopulatedGrid getLowResNortherAmericanData() {
		//compute the sub grid -- that only includes North America
		int[][] fullGrid = RawLandScanReader.loadGrid(20);
		int[][] subGrid = new int[highY - lowY][highX - lowX];
		for (int i = 0; i < subGrid.length; i++) {
			for (int j = 0; j < subGrid[i].length; j++) {
				subGrid[i][j] = fullGrid[i + lowY][j + lowX];
			}
		}
		//remove the caribean -- part 1
		for (int i = caribeanY_1 - lowY; i < subGrid.length; i++) {
			for (int j = caribeanX_1 - lowX; j < subGrid[i].length; j++) {
				subGrid[i][j] = 0;
			}
		}
		//remove the caribean -- part 2
		for (int i = caribeanY_2 - lowY; i < subGrid.length; i++) {
			for (int j = caribeanX_2 - lowX; j < subGrid[i].length; j++) {
				subGrid[i][j] = 0;
			}
		}
		//remove the caribean -- part 2
		for (int i = caribeanY_3 - lowY; i < subGrid.length; i++) {
			for (int j = caribeanX_3 - lowX; j < subGrid[i].length; j++) {
				subGrid[i][j] = 0;
			}
		}
		
		//return subGrid;
		return new PopulatedGrid(subGrid);
	}



	/**
	 * Extract the NorthAmerican Land Scan data
	 * 
	 * @param args - Ignored
	 */
	public static void main(String[] args) {
		GridExtraction northAmerica = getLowResNortherAmericanData().extractLargest(25000, 1.0, false);
		northAmerica.printToFile(OUTPUT_FILE);		
	}

}
