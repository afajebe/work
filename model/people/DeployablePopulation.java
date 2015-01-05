package model.people;


import util.Utility;
import gridBasedData.GridExtraction;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import core.SimulationProperties;
import probMass.ProbMassFunction;
import probMass.ProbMassFunctionFactory;
import util.Parallelizer;


public class DeployablePopulation implements Serializable {

	/** The directory where thread balancing computations are found. */
	private final static String POPULATION_PATH = "pops\\";

	/** The name of the dataset being loaded. */
	private final String dataset;

	/** The number of ModelPlaces you want to load. */
	private final int approxNumPlaces;

	/** The fraction of each ModelPlace's population that is instantiated. */
	private final double scale;

	/** The number of threads that "share" this population. */
	private final int numThreads;

	/** A GridBased dataset that is responsible for computing the interactionMatrix. */
	public final GridExtraction extraction;

	/** The matrix which controls interaction between places. */
	public double[][] interactionMatrix;

	/** The object speeds up selecting a random number from a given distribution. */
	public ProbMassFunction[] pdfs;

	public final long totalPopulation;

	/** The actual number of place in this population. */
	public int numPlaces;

	/** placePopulations[i] = the population of ModelPlace i. */
	public int[] placePopulations;

	/** threadOwners[i] = The index of the thread that owns ModelPlace i. */
	public int[] threadOwners;

	/** The width of the grid (used when drawing maps). */
	public final int gridWidth;

	/** The height of the grid (used when drawing maps). */
	public final int gridHeight;


	/** Load (or create) the population requested in the properties file. */
	public static DeployablePopulation loadPopulation(SimulationProperties props) {

		String fileName = getFileName(props);
		File file = new File(fileName);

		if (file.exists()) {
			try {
				System.out.println("A previously constructed population was found\nLoading...");
				DeployablePopulation dp = (DeployablePopulation) Utility.deserialize(file);
				return dp;
			} catch (IOException ioe) {
				ioe.printStackTrace();
				System.exit(0);
			}

			throw new IllegalStateException("Should never reach this statement");

		} else {
			file = new File(DeployablePopulation.POPULATION_PATH);
			if (!file.exists()) {
				file.mkdir();
			}

			return new DeployablePopulation(props);
		}
	}


	/**
	 * Generate the file name for the Population requested in the supplied properties file
	 *
	 * @return - A string like "USZIP_10000_0.5_2"
	 */
	private static String getFileName(SimulationProperties props) {

		String dataset = props.dataSource();
		int approxNumPlaces = props.approxNumPlaces();
		double scale = props.scale();
		int numThreads = props.numThreads();

		return POPULATION_PATH
				+ dataset + "_"
				+ approxNumPlaces + "_"
				+ scale + "_"
				+ numThreads + ".ser";
	}


	private DeployablePopulation(SimulationProperties props) {
		System.out.println("Constructing a Population");

		//find basic population data in properties file
		this.dataset = props.dataSource();
		this.scale = props.scale();
		this.approxNumPlaces = props.approxNumPlaces();
		this.numThreads = props.numThreads();

		//get the correct data from the data provider
		this.extraction = GridExtraction.loadData(dataset, approxNumPlaces, scale);
		this.gridWidth = extraction.mapWidth;
		this.gridHeight = extraction.mapHeight;
		this.numPlaces = extraction.numZips;

		this.placePopulations = new int[numPlaces];
		long sum = 0;
		for (int i = 0; i < numPlaces; i++) {
			placePopulations[i] = extraction.gridPlaces[i].pop;
			sum += placePopulations[i];
		}
		this.totalPopulation = sum;

		System.out.println("numPlaces = " + numPlaces);
		System.out.println("population = " + totalPopulation);

		this.interactionMatrix = extraction.computeInteractionMatrix();

		threadOwners = determineThreadOwners();

		//save a picture of the distribution
//		drawModelPic("ModelDistribution.bmp");


		//report amount of offThread communication
		double[][] nodeComm = new double[this.numThreads][this.numThreads];
		for (int i = 0; i < numPlaces; i++) {
			for (int j = 0; j < numPlaces; j++) {
				nodeComm[threadOwners[i]][threadOwners[j]] += interactionMatrix[i][j];
			}
		}
		double[] nodeRowSums = new double[this.numThreads];
		for (int i = 0; i < this.numThreads; i++) {
			for (int j = 0; j < this.numThreads; j++) {
				nodeRowSums[i] += nodeComm[i][j];
			}
		}
		System.out.println("\nBelow you will find the intra-node communication matrix\n");
		DecimalFormat df = new DecimalFormat("#.###");
		for (int i = 0; i < this.numThreads; i++) {
			for (int j = 0; j < this.numThreads; j++) {
				System.out.print(df.format(nodeComm[i][j] / nodeRowSums[i]) + "\t");
			}
			System.out.println();
		}

		this.pdfs = computePDFsInParallel();

		try {
			System.out.println("Serializing Population for future use...");
			Utility.serialize(this, DeployablePopulation.getFileName(props));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	/**
	 * Given a persons resident zipcode, determine the home zipcode of a random contact.
	 *
	 * @param placeIndex - The index of a person's home.
	 * @param randomDraw - A random number 0 - 1
	 */
	public int getRandomPlaceIndex(int placeIndex, double randomDraw) {
		return pdfs[placeIndex].getSample(randomDraw);
	}


	/** @return - An array in which output[i] = the index of the thread that owners ModelPlace i. */
	private int[] determineThreadOwners() {

		//@todo -- optimize this

		//speckled - round robin dist
		int[] array = new int[numPlaces];
		for (int i = 0; i < numPlaces; i++) {
			array[i] = i % this.numThreads;
		}
		return array;
	}


	private ProbMassFunction[] computePDFsInParallel() {

		//recast interaction matrix as a series of pdfs (1 per zip)
		System.out.println("\ncalculating pdfs (This will take a while)...");

		//Define a simple Task object -- each task computes the PDF for a single place
		class ComputeDistributionTask implements Runnable {

			int index;

			//the input
			double[] rowOfInteractionMatrix;

			//the output
			ProbMassFunction output;


			ComputeDistributionTask(int index, double[] rowOfInteractionMatrix) {
				this.index = index;
				this.rowOfInteractionMatrix = rowOfInteractionMatrix;
			}


			@Override
			public void run() {
				//this.outputSet[index] = new BinarySearchDistribution(interactionMatrix[i]);
				this.output = ProbMassFunctionFactory.mediumSpeedMediumMemoryPMF(rowOfInteractionMatrix);
			}
		}

		ComputeDistributionTask[] jobs = new ComputeDistributionTask[numPlaces];
		for (int i = 0; i < jobs.length; i++) {
			jobs[i] = new ComputeDistributionTask(i, interactionMatrix[i]);
		}

		Parallelizer para = new Parallelizer(SimulationProperties.getDefaultProperties().
				numThreads());
		para.doWorkInParallel(jobs);

		System.out.println("Done Building Complete Population");

		//destory the interactionMatrix because it is no longer needed
		interactionMatrix = null;

		//extract the distributions that were built..
		ProbMassFunction[] distributions = new ProbMassFunction[numPlaces];
		for (int i = 0; i < distributions.length; i++) {
			distributions[i] = jobs[i].output;
		}

		//return them...
		return distributions;
	}
}
