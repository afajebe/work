package landScan;

import gridBasedData.GridExtraction;
import gridBasedData.PopulatedGrid;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.util.StringTokenizer;


/**
 * This class is meant to extract high resolution rectangles from the original LandScanData
 */
public class LandScanExtractor {

	/** The original ascii raster file generated from ArcCatalog. (About 3.92 GB)*/
	private static String filePath = "C:\\MyFiles\\JPARKER\\extraction\\asciiraster.txt";

	/** The number of rows in the ORIGINAL dataset. */
	private static int numRows = 20880;

	/** The number of col in the ORIGINAL dataset. */
	private static int numColumns = 43200;
	
	/** The US map is 57.56 units across. */
	private static double actualXDistance = 360.0;
	
	/** The US map is 24.33 units tall. */
	private static double actualYDistance = 180.0;
	
	/** 
	 * LandScan data comes in 1 x 1 km square grid, enlarge this grid by this factor.  
	 * AKA blockSize = 5 will build a 5x5 km square grid
	 */
	private static final int standardBlockSize = 60;
	
	
	/**
	 * The only (in theory) programatic way to access the LandScan Data
	 * 
	 * @param dataset - An indentifing String that contains "LANDSCAN".  Currently only 
	 * "FULL_LANDSCAN" is supported. 
	 * @param numSquares - The number of pixels you want from the above dataset
	 * @param scale - The fractional size each of the above pixels should be.
	 * 
	 * @return - A GridExtraction that contains the correct format
	 */
	public static GridExtraction loadData(String dataset, int numSquares, double scale) {
		if(dataset.equals("FULL_LANDSCAN")) {
			int[][] rawGrid = RawLandScanReader.loadGrid(standardBlockSize);
			PopulatedGrid pGrid = new PopulatedGrid(rawGrid);
			GridExtraction extraction = pGrid.extractLargest(numSquares, scale, true);			
			
			extraction.setXStepSize(actualXDistance / (double) extraction.mapWidth);
			extraction.setYStepSize(actualYDistance / (double) extraction.mapHeight);
			
			return extraction;
		}
		
		throw new IllegalArgumentException("Unknown dataset :: " + dataset);
	}
	
	
	private static String getFilePrefix(int startI, int startJ, int width, int height) {
		//first look for the file before making the extraction
		String fileName = "HighRes_LS_Extract_" + startI + "_" + startJ + "_" + width + "_" + height;
		return fileName;
	}
	

	public static int[][] getHighResLandScanExtraction(int startI, int startJ, int width, int height) {
		//first look for the file before making the extraction
		String fileName = getFilePrefix(startI, startJ, width, height) + ".ser";

		File f = new File(fileName);
		if (f.exists()) {
			System.out.println("deserializing grid file");
			int[][] outputGrid = null;

			try {
				FileInputStream fis = new FileInputStream(f);
				ObjectInputStream ois = new ObjectInputStream(fis);
				outputGrid = (int[][]) ois.readObject();

				ois.close();
				fis.close();

				if (outputGrid == null) {
					throw new IllegalStateException("outputGrid == null");
				}

			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(0);
			}

			return outputGrid;
		} else {
			System.out.println("Cannot find a serialized grid file");
			int[][] outputGrid = buildGrid(startI, startJ, width, height);
			LandScanUtility.serialize(outputGrid, fileName);
			return outputGrid;
		}
	}	
	

	/**
	 * 
	 * 
	 * @param startX
	 * @param startY
	 * @param width
	 * @param height
	 * @return
	 */
	private static int[][] buildGrid(int startX, int startY, int width, int height) {
				
		//read an asci file
		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(filePath));
			br.readLine();	//remove: ncols         43200
			br.readLine();	//remove: nrows         20880
			br.readLine();	//remove: xllcorner     -180
			br.readLine();	//remove: yllcorner     -90
			br.readLine();	//remove: cellsize      0.0083333333333333
			br.readLine();	//remove: NODATA_value  -9999

			int[][] outGrid = new int[height][width];

			for (int i = 0; i < numRows; i++) {
				
				StringTokenizer tokenizer = new StringTokenizer(br.readLine());
				
				if(startY <= i && i < (startY + height)) {
			
					int j = 0;
					while (tokenizer.hasMoreTokens()) {
						int val = Integer.parseInt(tokenizer.nextToken());

						if (startX <= j && j < (startX + width)) {
							if (val != -9999) {
								outGrid[i - startY][j - startX] = val;
							} 
						}
						j++;
						
						if( j > (startX + width)) {
							break;
						}
					}
				}
				
				if( i > (startY + height)) {
					break;
				}

				if (i % 250 == 0) {
					System.out.println("Completed Row " + i);
				}
			}

			return outGrid;

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
		throw new IllegalStateException("Should never get here");
	}
	
	
	public static void main(String[] args) {
//		int[][] northEast = getHighResLandScanExtraction(12200,4900,1100,600);
//		
//		LandScanUtility.drawGrid(northEast, 0, "FullNorthEast.bmp");
//		
//		PopulatedGrid northEastPop = new PopulatedGrid(northEast);
//		northEastPop.extractLargest(25000, 1.0);
//		
//		PopulatedGrid northEastPop2 = new PopulatedGrid(northEastPop.getLowerResolutionCopy(2));
//		northEastPop2.extractLargest(25000, 1.0);
		
	}
}
