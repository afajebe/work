package model;


import core.InteractiveLargeScaleModel;
import model.people.DeployablePopulation;
import util.ImageUtility;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import movie.MovieUtility;
import movie.Movie;


/**
 * A GlobalModelRecord is a wrapper for storing all of the output data from a
 * model. Essentially 4 2 dimensional integer arrays.
 */
public class GlobalModelRecord {

	DeployablePopulation pop;

	ModelPlaceRecord[] records;

	int[][] sus;

	int[][] contagious;

	int[][] nonContagious;

	int[][] symtomatic;

	int[][] nonSymptomatic;

	int[][] rec;


	/** Creates a new instance of GlobalModelRecord */
	public GlobalModelRecord(int numZips, DeployablePopulation pop) {
		this.pop = pop;
		records = new ModelPlaceRecord[numZips];
		sus = new int[numZips][];
		this.contagious = new int[numZips][];
		this.nonContagious = new int[numZips][];
		this.symtomatic = new int[numZips][];
		this.nonSymptomatic = new int[numZips][];
		rec = new int[numZips][];
	}


	/**
	 * Add additional data to this record (the entire data set does not
	 * arrive simultaneously.
	 */
	public void addData(ModelPlaceRecord[] importedRecords) {

		int tempZip;
		for (int i = 0; i < importedRecords.length; i++) {
			tempZip = importedRecords[i].placeIndex();

			this.records[tempZip] = importedRecords[i];

			sus[tempZip] = importedRecords[i].getSusHistory();
			contagious[tempZip] = importedRecords[i].getContagiousHistory();
			nonContagious[tempZip] = importedRecords[i].getNonContagiousHistory();
			symtomatic[tempZip] = importedRecords[i].getSymptomaticHistory();
			nonSymptomatic[tempZip] = importedRecords[i].getNonSymptomaticHistory();
			rec[tempZip] = importedRecords[i].getRecHistory();


			//error check
			for (int j = 0; j < sus[tempZip].length; j++) {
				if (sus[tempZip][j] < 0) {
					throw new IllegalArgumentException(
							"Negative data in sus[" + tempZip + "][" + j + "] :: " + sus[tempZip][j]);
				}
			}
			for (int j = 0; j < contagious[tempZip].length; j++) {
				if (contagious[tempZip][j] < 0) {
					throw new IllegalArgumentException(
							"Negative data in contagious[" + tempZip + "][" + j + "] :: " + contagious[tempZip][j]);
				}
			}
			for (int j = 0; j < nonContagious[tempZip].length; j++) {
				if (nonContagious[tempZip][j] < 0) {
					throw new IllegalArgumentException(
							"Negative data in nonContagious[" + tempZip + "][" + j + "] :: " + nonContagious[tempZip][j]);
				}
			}
			for (int j = 0; j < symtomatic[tempZip].length; j++) {
				if (symtomatic[tempZip][j] < 0) {
					throw new IllegalArgumentException(
							"Negative data in symtomatic[" + tempZip + "][" + j + "] :: " + symtomatic[tempZip][j]);
				}
			}
			for (int j = 0; j < nonSymptomatic[tempZip].length; j++) {
				if (nonSymptomatic[tempZip][j] < 0) {
					throw new IllegalArgumentException(
							"Negative data in nonSymptomatic[" + tempZip + "][" + j + "] :: " + nonSymptomatic[tempZip][j]);
				}
			}

			for (int j = 0; j < rec[tempZip].length; j++) {
				if (rec[tempZip][j] < 0) {
					throw new IllegalArgumentException(
							"Negative data in rec[" + tempZip + "][" + j + "] :: " + rec[tempZip][j]);
				}
			}
		}
	}


	/** Write the 4 arrays to 4 files. */
	public void writeDataFiles() {
		writeTxt("susceptibles.txt", "susceptibles", sus);
		writeTxt("contagious.txt", "recovered", contagious);
		writeTxt("nonContagious.txt", "recovered", nonContagious);
		writeTxt("symptomatic.txt", "recovered", symtomatic);
		writeTxt("nonSymptomatic.txt", "recovered", nonSymptomatic);
		writeTxt("recovered.txt", "recovered", rec);

//		writeHospitalFile(inf);

		int numLogEntries = sus[0].length;

		Movie mov = null;
		BufferedImage frame = null;
		for (int i = 0; i < numLogEntries; i++) {
			frame = createFrame(i);
			if (i == 0) {
				mov = MovieUtility.
						initMovie(frame, InteractiveLargeScaleModel.OUTPUT_DIRECTORY + "Movie.avi", 2, numLogEntries);
			}
			MovieUtility.addFrame(mov, frame);
		}
		MovieUtility.endMovie(mov);
	}


	/** Create one frame of the output movie. */
	private BufferedImage createFrame(int entryNum) {

		//create the image
		BufferedImage image = new BufferedImage(
				pop.gridWidth,
				pop.gridHeight,
				BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = image.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, pop.gridWidth, pop.gridHeight);

		//iterate across the zips
		int x;
		int y;
		int red;
		int green;
		int blue;
		int infections;
		int recoveries;

		double redMult = 100.0;
		double blueMult = 2.0;

		for (int i = 0; i < pop.numPlaces; i++) {

			x = pop.extraction.gridPlaces[i].x;
			y = pop.extraction.gridPlaces[i].y;


			infections = contagious[i][entryNum];
			recoveries = rec[i][entryNum];

			if (infections < 0) {
				throw new IllegalStateException("infections in zipcode :: " + i + " are negative :: " + infections);
			}
			if (recoveries < 0) {
				throw new IllegalStateException("recoveries in zipcode :: " + i + " are negative :: " + recoveries);
			}


			if (x >= pop.gridWidth) {
				throw new IllegalStateException("x :: " + x);
			}
			if (y >= pop.gridHeight) {
				throw new IllegalStateException("y :: " + y);
			}


			int zipPop = pop.placePopulations[i];

			//calculate the "redness" of a pixel (tied to infectives)
			double fraction = ((double) infections) / ((double) zipPop) * redMult;
//			double fraction = Math.pow(((double)infections) / ((double)zipPop) , 2.0) * redMult;			
			fraction = Math.min(1.0, fraction);
			red = (int) (255.0 * fraction);

			//calculate
			green = 0;

			//calculate the "blueness" of a pixel (tied to recovered)
			fraction = ((double) recoveries) / ((double) zipPop) * blueMult;
			fraction = Math.min(1.0, fraction);
			blue = (int) (255.0 * fraction);

			//paint this zip white			
			image.setRGB(x, y, ImageUtility.getRGB(red, green, blue));

			/*
			 //if anyone is recovered here
			 if(rec[i][entryNum] > 0 ) {
			 //calculate the fraction that is rec
			 int zipPop = pop.zipcodePopulation[i];
			 double fraction = ((double)rec[i][entryNum]) / ((double)zipPop);
			 int color = (int)(255.0 * fraction);
				
			 //make this pixel a shade of red
			 image.setRGB(x , y , getRGB(255 , 255 - color , 255 - color));
			 }
			 */
		}
		return image;
	}


	/** Wrap the txt output process incase I have to change it. */
	private void writeTxt(String fileName, String label, int[][] data) {
		try {
			File f = new File(InteractiveLargeScaleModel.OUTPUT_DIRECTORY + fileName);
			FileOutputStream fos = new FileOutputStream(f);
			PrintWriter pw = new PrintWriter(fos);

			pw.
					write("This file contains the history of the number of " + label + " people in the ModelPlace\n\n");
			pw.
					write("Each ModelPlace has it's history listed in a column that is topped with its ModelPlace index\n");

			//writeTxt the zipcode ids
			for (int i = 0; i < data.length; i++) {
				pw.write("\t" + i);
			}

			for (int time = 0; time < data[0].length; time++) {
				pw.write(time + "\t");	//label this row

				//writeTxt each zip's number
				for (int zip = 0; zip < data.length; zip++) {
					pw.write(data[zip][time] + "\t");

				}
				pw.write("\n");
			}

			pw.flush();
			pw.close();

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
	}
//	private void writeHospitalFile(int[][] infData) {
//		try {
//			File f = new File("surgeData.txt");
//			FileOutputStream fos = new FileOutputStream(f);
//			PrintWriter pw = new PrintWriter(fos);
//			
//			pw.write("Time\tTotalCases\tHandledCases\tOverflow\n");
//			
//			int totalTime = infData[0].length;
//			int numZips = infData.length;
//			for (int time = 0 ; time < totalTime ; time++) {
//				int handledCases = 0;
//				int overflowCases = 0;
//				int totalCases = 0;
//				for (int zip = 0 ; zip < numZips ; zip++) {
//					int localCases = infData[zip][time];
//					totalCases += localCases;
//					
//					if(localCases > pop.places[zip].totalBeds) {
//						//over capacity
//						handledCases += pop.places[zip].totalBeds;
//						overflowCases += localCases - pop.places[zip].totalBeds;
//					} else {
//						//at or below capacity
//						handledCases += localCases;
//						overflowCases += 0;
//					}					
//				}
//				
//				pw.write(time + "\t" + totalCases + "\t" +  handledCases + "\t" + overflowCases + "\n");
//			}
//			
////			pw.write("This file contains the history of the number of " + label + " people in the zipcodes\n\n");
////			pw.write("Each zipcode has it's history listed in a column that is topped with its zipcode index\n");
////			
////			//writeTxt the zipcode ids
////			for(int i = 0 ; i < data.length ; i++) {
////				pw.write("\t" + i);
////			}
////			
////			for(int time = 0 ; time < data[0].length ; time++) {
////				pw.write(time + "\t");	//label this row
////				
////				//writeTxt each zip's number
////				for(int zip = 0 ; zip < data.length ; zip++) {
////					pw.write(data[zip][time] + "\t");
////					
////				}
////				pw.write("\n");
////			}
////			
////			pw.write("\n");
//			pw.flush();
//			pw.close();
//			
//		} catch (FileNotFoundException ex) {
//			ex.printStackTrace();
//		}
//		
//	}
}
