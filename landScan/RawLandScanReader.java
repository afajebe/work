package landScan;


import gridBasedData.GridPlace;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.StringTokenizer;


/**
 * This class can read the raw landscan file.
 * 
 * This class provides a single static method that provides access to the data in the LandScan 
 * dataset -- loadGrid(int blockSize).  This method 
 */
public class RawLandScanReader {

	/** The original ascii raster file generated from ArcCatalog. (About 3.92 GB)*/
	private static String filePath = "C:\\MyFiles\\JPARKER\\extraction\\asciiraster.txt";
	
	/** The file that will recieve the list of biggest 'grid squares'. */
	private static String outFile = "LandScanExtraction.txt";

	/** The number of rows in the ORIGINAL dataset. */
	private static int numRows = 20880;

	/** The number of col in the ORIGINAL dataset. */
	private static int numColumns = 43200;
	

	/**
	 * This method will provide a copy of the landscan population grid -- at the desired 
	 * resolution.  This method will search for a pre-computed answer in a serialized file.
	 * It such a file does not exist it will manualy create the answer and serialize the 
	 * results of that computation for future use.
	 * 
	 * @param blockSize - LandScan data comes in 1 x 1 km square grid, enlarge this grid by 
	 * this factor.  AKA blockSize = 5 will build a 5x5 km square grid
	 *  
	 * @return The deseralized (or built) land scan population grid.  Chances are good the
	 * result of this method will be sent to a PopulatedGrid contructor
	 */
	static int[][] loadGrid(int blockSize) {

		String fileName = getFileName(blockSize);
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
			System.out.println("Cannot find a serialized LandScane grid file at resolution = :: " + blockSize);
			int[][] outputGrid = buildGrid(blockSize);
			LandScanUtility.serialize(outputGrid, fileName);
			return outputGrid;
		}
	}


	/**
	 * Build the land scan population Grid.
	 * 
	 * @param blockSize - LandScan data comes in 1 x 1 km square grid, enlarge this grid by 
	 * this factor.  AKA blockSize = 5 will build a 5x5 km square grid
	 * 
	 * @return The grid
	 */
	private static int[][] buildGrid(int blockSize) {
		
		System.out.println(
			"Building a grid with squares of size " + 
			blockSize + "km x " + blockSize + " km");		
		
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



			int[][] outGrid = new int[numColumns / blockSize][numRows / blockSize];
			//int[][] outGrid = new int[numRows / blockSize][numColumns / blockSize];

			for (int i = 0; i < numRows; i++) {
				StringTokenizer tokenizer = new StringTokenizer(br.readLine());

				int j = 0;
				while (tokenizer.hasMoreTokens()) {
					int val = Integer.parseInt(tokenizer.nextToken());

					if (val != -9999) {
						outGrid[j / blockSize][i / blockSize] += val;
						//outGrid[i / blockSize][j / blockSize] += val;
					}
					j++;
				}

				if (i % 250 == 0) {
					System.out.println("Completed Row " + i);
				}
			}

			System.out.println("done building grid of size " + blockSize);
			return outGrid;

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
		throw new IllegalStateException("Should never get here");
	}

	
	/** Return a correct file name for a seralized grid file. */
	private static String getFileName(int blockSize) {
		return "gridFile_" + blockSize + ".ser";
	}


	
	/**
	 * This method is meant to test the LandScan Extraction methods.  It will produce a picture 
	 * of the complete dataset (with blockSize = 20).  It will also produce a picture of the 
	 * top 25,000 20km x 20km squares in the LandScan data (and a txt file listing this places)
	 * 
	 * @param args - Ignored
	 * @throws java.io.IOException
	 */
	public static void main(String[] args) throws IOException {
		
		int numPixels = 25000;
		
		
		int[][] grid = loadGrid(20);		
		
		LandScanUtility.drawGrid(grid, 0 , "FullLandScan.bmp");

		System.out.println("Getting top " + numPixels + " squares");

		LinkedList<GridPlace> gridSizes = new LinkedList<GridPlace>();
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[i].length; j++) {

				if (grid[i][j] > 0) {
					gridSizes.addLast(new GridPlace(
						j,		//the column forms the x coor
						i,		//the row forms the y coor
						grid[i][j])
						);
				}
			}
		}
		Collections.sort(gridSizes);
		//trim the list to the correct size
		while(gridSizes.size() > numPixels) {
			gridSizes.removeFirst();
		}
		
		LandScanUtility.drawGrid(grid, gridSizes.getFirst().pop, "LandScan_" + gridSizes.getFirst().pop + ".bmp");

		//write the list of biggest grid squares to a file
		FileOutputStream fos = new FileOutputStream(outFile);
		PrintWriter dout = new PrintWriter(fos);

		while (gridSizes.size() > 0) {
			GridPlace entry = gridSizes.removeFirst();

			dout.write(entry.toString() + "\n");
		}

		dout.close();
		fos.close();
	}
		
		
		
}
