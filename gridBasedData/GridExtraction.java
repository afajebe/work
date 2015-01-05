package gridBasedData;

import model.ModelPlaceSeed;
import util.ImageUtility;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Arrays;
import javax.imageio.ImageIO;
import landScan.LandScanExtractor;



/**
 * This class repackages the data inside a PopulatedGrid while also supplementing that data with
 * distance information.
 * 
 * Since most entries in a PopulatedGrid will be small (or zero) this class lists only the grid 
 * entries which warrant a row (and column) in a final travel matrix.
 * 
 * After the appropriate scale has been assigned to the grid (i.e. is a square in the grid 1 km by
 * 1 km or 100 km by 100 km), this class is capable of computing a travel matrix.
 */
public class GridExtraction implements Serializable {
	
	
	/**
	 * This exponent controls how widely distribute peoples travel patterns are.
	 * The "exp" in the following equation -- Distance = (sqrt(x^2 + y^2)) ^ exp
	 */
	private static double DISTANCE_EXPONENT = 2.0;
	
	/** A list of the GridValues (it is assumed that this is the population you intend to model) */
	public final GridPlace[] gridPlaces;
	
	/** The number of places that will be modeled. */
	public final int numZips;
		
	/** 
	 * How "far apart" should two GridValues be assuming they have equal y coordinates, and thier
	 * x coordinates only differ by one.
	 */
	double xDistance;
	
	/** 
	 * How "far apart" should two GridValues be assuming they have equal y coordinates, and thier
	 * x coordinates only differ by one.
	 */
	double yDistance;
	
	
	/** 
	 * If you want all maps generated from the same "parent grid" to have the same size you 
	 * need to save the width of the inital grid
	 */
	public final int mapWidth;
	
	/**
	 * If you want all maps generated from the same "parent grid" to have the same size you
	 * need to save the height of the inital grid.
	 */
	public final int mapHeight;
	
	/** True if the distance calculation should wrap in the X direction. */
	private final boolean distanceWraps;
	
	
	public static GridExtraction loadData(String dataset, int numSquares, double scale) {
		
		if(dataset.equals("US_ZIPCODE")) {
			return USZipCodeData.load(numSquares, scale);
		}
		if(dataset.contains("LANDSCAN")) {
			return LandScanExtractor.loadData(dataset, numSquares, scale);
		}
		
		throw new IllegalArgumentException("Unknown dataset :: " + dataset);
	}
		
	
	/**
	 * Create a wrapper for "place" data that can be supplement with distance data and then used
	 * to compute travel matricies.
	 * 
	 * @param originalGrid - The raw grid data
	 * @param sizeOfExtraction - The number of squares from the originalGrid to extract
	 * @param mapWidth - The width of the original grid
	 * @param mapHeight - The height of the original grid
	 * @param scale - The fraction to scale the population of the original grid data
	 * @param wraps - True if the distance measurement in the X direction should wrap around the map
	 */
	GridExtraction(PopulatedGrid originalGrid, int sizeOfExtraction, int mapWidth, int mapHeight, double scale, boolean wraps) {
		
		this.gridPlaces = originalGrid.getLargestSquares(sizeOfExtraction, scale);
		this.numZips = gridPlaces.length;
		
		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;
		this.distanceWraps = wraps;
	}

	
	/**
	 * Return a ModelPlaceSeed for this place.
	 * 
	 * @param placeNum - The entry
	 * 
	 * @return A ModelPlaceSeed that contains all the information requried to build this place
	 */
	public ModelPlaceSeed getSeed(int placeNum) {
		
		return new ModelPlaceSeed(
			gridPlaces[placeNum].pop ,
			gridPlaces[placeNum].x ,
			gridPlaces[placeNum].y);
	}
	
	
	/** 
	 * Set the step size distance in the x direction. 
	 * 
	 * @param xDist - The distance between two adjacent columns
	 */ 
	public void setXStepSize(double xDist) {
		this.xDistance = xDist;
	}
	
	
	/** 
	 * Set the step size distance in the y direction. 
	 *  
	 * @param yDist - The distance between two adjacent rows
	 */
	public void setYStepSize(double yDist) {
		this.yDistance = yDist;
	}
	
	
	/**
	 * Print a sorted list of the places in this GridExtraction.
	 * 
	 * @param fileName - The name of the file where the text will appear.
	 */
	public void printToFile(String fileName) {
		Arrays.sort(gridPlaces);
		//write the list of biggest grid squares to a file
		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			PrintWriter dout = new PrintWriter(fos);

			for (int i = 0; i < gridPlaces.length; i++) {
				dout.write(gridPlaces[i].toString() + "\n");
			}

			dout.close();
			fos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	/** Return a distance matrix for this population */
	private double[][] calculateDistanceMatrix() {
		
		if(xDistance == 0.0 || yDistance == 0.0) {
			throw new IllegalStateException(
				"Both xDistance and yDistance must be set prior to computing a distance matrix");
		}

		double[][] distMatrix = new double[numZips][numZips];
		double distance1;
		double distance2;
		double xDist;
		double yDist;
		for (int i = 0; i < numZips; i++) {
			for (int j = 0; j < numZips; j++) {
				
				if (distanceWraps) {
					distance1 = Math.abs(Math.abs(gridPlaces[i].x - gridPlaces[j].x) * xDistance);
					distance2 = Math.abs(Math.abs(gridPlaces[i].x + mapWidth - gridPlaces[j].x) * xDistance);
					xDist = Math.min(distance1, distance2);
				} else {
					xDist = Math.abs(Math.abs(gridPlaces[i].x - gridPlaces[j].x) * xDistance);
				}

				yDist = Math.abs(gridPlaces[i].y - gridPlaces[j].y) * yDistance;
				distMatrix[i][j] = Math.pow(Math.sqrt((xDist * xDist + yDist * yDist)), DISTANCE_EXPONENT);

				//zero distance isn't allowed - treat zips as same place (distance = 1.0)
				if (distMatrix[i][j] == 0.0) {
					distMatrix[i][j] = 1.0;
				}
			}
		}

		//System.out.println("Error checking calculate distance matrix");
		for (int i = 0; i < distMatrix.length; i++) {
			for (int j = 0; j < distMatrix.length; j++) {
				if (distMatrix[i][j] <= 0.0) {
					throw new IllegalStateException(
						"distMatrix[" + i + "][" + j + "] :: " +
						distMatrix[i][j]);
				}
			}
		}
		//System.out.println("Calculate Distance Matrix has all postive values");

		return distMatrix;
	}


	/**  Return a matrix with entries equal to Population_i * Population_j / f(distance_ij). */
	private double[][] calculatePopOverDist(double[][] distMatrix) {
		
		double[][] popOverDist = new double[distMatrix.length][distMatrix.length];	//this will be a symetric matrix
		for(int i = 0 ; i < distMatrix.length ; i++) {
			for(int j = i ; j < numZips ; j++) {
				//do 2 seperate cast so that you don't cast the result of an integer overflow
				popOverDist[i][j] = ((double)gridPlaces[i].pop) * ((double)gridPlaces[j].pop) / distMatrix[i][j];
				popOverDist[j][i] = popOverDist[i][j];
			}
		}
		
		//System.out.println("Error checking Population Over Distance matrix");
		for (int i = 0 ; i < popOverDist.length ; i++) {
			for (int j = 0 ; j < popOverDist.length ; j++) {
				if(popOverDist[i][j] <= 0.0) {
					throw new IllegalStateException(
						"popOverDist[" + i + "][" + j + "] :: " +
						popOverDist[i][j] + "\n\n" +
						"places[" + i + "].pop :: " + gridPlaces[i].pop + "\n\n" +
						"places[" + j + "].pop :: " + gridPlaces[j].pop + "\n\n" +
						"distMatrix[" + i + "][" + j + "] :: " + distMatrix[i][j]
						);
				}
			}
		}
		//System.out.println("Population Over Distance Matrix has all postive values");
		
		return popOverDist;
	}
	
	
	/**  Return a matrix with entries equal to prob of ij interaction. */
	private double[][] calculateInteractionMatrix(double[][] popOverDist) {
		
		double[][] interactionMatrix = new double[popOverDist.length][popOverDist.length];
		//we now have to rescale the popOverDist matrix into a probability.
		//Thus creating the interactionMatrix.
		//prob that a NYCer travels to rural NY is not equal to prob
		//rural NYer travels to NYC.  (interactionMatrix shouldn't be symetric)
		double rowSum;
		for(int i = 0 ; i < popOverDist.length ; i++) {
			//calculate a row sum
			rowSum = 0;
			for(int j = 0 ; j < popOverDist.length ; j++) {
				rowSum += popOverDist[i][j];
			}
			
			//for each entry in that row enter the fraction of the rowSum that was
			for(int k = 0 ; k < popOverDist.length ; k++) {
				interactionMatrix[i][k] = popOverDist[i][k]/rowSum;
			}
		}
		return interactionMatrix;
	}
	
	
	/**  Return a matrix with entries equal to prob of ij interaction. */
	public double[][] computeInteractionMatrix() {
		System.out.println("\ncalculating distMatix");
		double[][] distMatrix = this.calculateDistanceMatrix();
		
		System.out.println("calculating popOverDist");
		double[][] popOverDist = calculatePopOverDist(distMatrix);
		System.out.println("Reclaiming distMatrix");
		distMatrix = null;				//reclaim mem
		
		System.out.println("calculating interactionMatrix");
		double[][] interactionMatrix = calculateInteractionMatrix(popOverDist);		
		System.out.println("Reclaiming popOverDist");
		popOverDist = null;				//reclaim mem
		
		return interactionMatrix;
	}	
	
	
	/** 
	 * Draw a map of this Extraction.
	 * 
	 * @param fileName - The name of the file (a ".bmp" will be appended to this string)
	 */
	public void drawMap(String fileName) {
	
		//create the image
		BufferedImage image = new BufferedImage(
			this.mapWidth,
			this.mapHeight,
			BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = image.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.mapWidth, this.mapHeight);
		
		for (int i = 0; i < gridPlaces.length; i++) {
			image.setRGB(gridPlaces[i].x ,gridPlaces[i].y, ImageUtility.getRGB(0, 0, 0));
		}
	
		try {
			ImageIO.write(image, "bmp", new File(fileName + ".bmp"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
