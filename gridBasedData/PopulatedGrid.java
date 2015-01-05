package gridBasedData;

import util.ImageUtility;
import util.Utility;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import landScan.RawLandScanReader;

/**
 * A PopulatedGrid is a simple wrapper class that stores a int[][] (where each entry stores a 
 * population)
 */
public class PopulatedGrid implements Serializable {
	
	/** An array that stores the number of people in grid square i,j. */
	public final int[][] grid;
	
	
	/** 
	 * Wrap a preexisting grid of population data to provide access to the other utility 
	 * methods of this class.
	 * 
	 * @param data - An array that stores the number of people in grid square i,j.
	 */
	public PopulatedGrid(int[][] data) {
		int rowLength = data[0].length;
		for (int i = 1; i < data.length; i++) {
			if(data[i].length != rowLength) {
				throw new IllegalArgumentException(
					"All rows must have the same length" +
					" -- IE grid must be rectangular");
			}
		}
		
		this.grid = data;
	}
	
		
	/**
	 * If the provided data has too high a resolution create a copy of this dataset that has 
	 * a lower resolution, but still contains the same population.  (AKA combine multiple grid
	 * squares into larger squares)
	 * 
	 * For instance, if the grid is 1280 X 1024 -- Produce a copy that is 320 x 256 by calling
	 * getLowerResolutionCopy(4)
	 * 
	 * @param blockSize - The reduction factor
	 * @return A copy of the original dataset that has lower resolution.
	 */
	public int[][] getLowerResolutionCopy(int blockSize) {
		
		if(grid.length % blockSize != 0) {
			throw new IllegalArgumentException(
				"grid.length (" + grid.length + ") must be divisible by " +
				"blockSize ( " + blockSize + ")");		
		}
		if(grid[0].length % blockSize != 0) {
			throw new IllegalArgumentException(
				"grid[0].length (" + grid[0].length + ") must be divisible by " +
				"blockSize ( " + blockSize + ")");
		}
		
		int numRows = grid.length;
		int numColumns = grid[0].length;

		int[][] outGrid = new int[numRows / blockSize][numColumns / blockSize];

		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numColumns; j++) {
				outGrid[i / blockSize][j/blockSize] += grid[i][j];
			}
		}		

		return outGrid;
	}
	
	
	/** 
	 * Get a copy of a rectangular region of this PopulationGrid
	 * 
	 * @param startI - The inital i (included)
	 * @param startJ - The inital j (included)
	 * @param endI - The final i (excluded)
	 * @param endJ - The final j (excluded)
	 * 
	 * @return an int[][] that has size (endI - startI) x (endJ - startJ)
	 */
	public int[][] extract(int startI, int startJ, int endI, int endJ) {
		int i_size = endI - startI;
		int j_size = endJ - startJ;
		int[][] outGrid = new int[i_size][j_size];
		
		for (int i = 0; i < outGrid.length; i++) {
			for (int j = 0; j < outGrid[i].length; j++) {
				outGrid[i][j] = grid[startI + i][startJ + j];
			}
		}
		return outGrid;
	}
	
	
	/**
	 * Extract (and wrap) the largest squares in this grid.  This extraction can the be used
	 * to generate a travel matrix.
	 * 
	 * @param numberToExtract - The maximum number of grid sqares to include.
	 * @param scale - Scale the population in the GridExtraction by this fraction.  For
	 * instance, if scale = .5, this method will extract the biggest grid sqaures and then
	 * reduce each of those populations by one half.
	 * @param wraps - True if the distance measurement in the X direction should wrap around the map
	 * 
	 * @return A GridExtraction object that wraps the "big value" data
	 */
	public GridExtraction extractLargest(int numberToExtract, double scale, boolean wraps) {
		return new GridExtraction(this, numberToExtract, grid.length, grid[0].length, scale, wraps);
	}
	

	/**
	 * This method creates an array that contains the largest squares in the grid
	 * 
	 * @param numberToExtract - The length or the returned array.
	 * @param scale - Scale the population in the GridExtraction by this fraction.  For
	 * instance, if scale = .5, this method will extract the biggest grid sqaures and then
	 * reduce each of those populations by one half.
	 * 
	 * @return - An array of maximum length "numberToExtract".  The largest grid square will 
	 * be the last entry in this array.
	 */
	public GridPlace[] getLargestSquares(int numberToExtract, double scale) {

		LinkedList<GridPlace> gridSizes = new LinkedList<GridPlace>();
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[i].length; j++) {

				if (grid[i][j] > 0) {
					gridSizes.addLast(new GridPlace(
						i, //the column forms the x coor
						j, //the row forms the y coor
						(int)(grid[i][j] * scale)));
				}
			}
		}
		Collections.sort(gridSizes);
		//trim the list to the correct size
		while (gridSizes.size() > numberToExtract) {
			gridSizes.removeFirst();
		}
		
		drawGrid(grid, gridSizes.getFirst().pop, 
			"extraction_" + gridSizes.getFirst().pop +".bmp");
		
		GridPlace[] output = gridSizes.toArray(new GridPlace[0]);

		//print a record of the mapping from (i,j) coordinate to ModelPlace Index
		printMapping(output);

		return output;
	}


	/**
	 * Print the mapping of coordinates to ModelPlace Indexes
	 * 
	 * @param places - An array of GridPlaces
	 */
	private static void printMapping(GridPlace[] places) {

		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < places.length; i++) {
			buffer.append(
				"(" + places[i].x + "," + places[i].y + ")\t" +
				i + "\n");
		}

		try {
			Utility.writeToNewFile("CoordToIndexMapping.txt", buffer.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	/**
	 * Draw a Map of the Grid, just for reference.
	 * 
	 * @param includeWhenBiggerThanThis - Only map grid squares with populations bigger than this num.
	 */
	public static void drawGrid(int[][] grid, int includeWhenBiggerThanThis, String fileName) {

		int width = grid.length;		//aka numCol in grid
		int height = grid[0].length;		//aka numRows in grid

		//create the image
		BufferedImage image = new BufferedImage(
			width,
			height,
			BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = image.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (grid[i][j] > includeWhenBiggerThanThis) {
					image.setRGB(i, j, ImageUtility.getRGB(0, 0, 0));
				}
			}
		}
		try {
			ImageIO.write(image, "bmp", new File(fileName));
		} catch (IOException ex) {
			Logger.getLogger(RawLandScanReader.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
