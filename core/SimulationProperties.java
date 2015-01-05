package core;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import time.LengthOfTime;
import time.ModelTimeUnit;


/**
 * A SimulationProperties object contains the properties that are required to configure an LSAM run.
 */
public class SimulationProperties {

	private static final String PROPERTIES_FILE = "interactiveLSAMproperties.prop";

	/** The properties that were found in the properties text file. */
	private static final Properties textFileProperties;


	static {
		textFileProperties = loadTextFileProperties();
	}


	private static Properties loadTextFileProperties() {
		Properties p = new Properties();
		try {
			System.out.println("Loading Properties file: " + PROPERTIES_FILE);
			p.load(new FileInputStream(PROPERTIES_FILE));
		} catch (IOException ioe) {
			System.out.println("Could not find properties file");
			System.exit(0);
		}
		return p;
	}


	public static SimulationProperties getDefaultProperties() {
		SimulationProperties simProps = new SimulationProperties();
		System.out.println("detected :: " + simProps.numThreads + " cores");
		
		return simProps;
	}


	private int runNumber = Integer.parseInt(textFileProperties.getProperty("runNumber"));

	//You could load the property or detect it automatically
	//Integer.parseInt(textFileProperties.getProperty("numThreads"));
	private int numThreads = Runtime.getRuntime().availableProcessors();

	private int numInitialCases = Integer.parseInt(textFileProperties.getProperty("numInitialCases"));

	private String dataSource = "US_ZIPCODE"; // "FULL_LANDSCAN"

	/** Use this parameter to scale down (or up) the population if desired. */
	private double scale = Double.parseDouble(textFileProperties.getProperty("scale"));

	private int approxNumPlaces = Integer.parseInt(textFileProperties.getProperty("approxNumZips"));

	private double randomInteractionCoef = 0.5;

	private double familyInteractionCoef = 2.0;

	private LengthOfTime logUpdateEventFrequency = new LengthOfTime(1.0, ModelTimeUnit.DAYS);

	private LengthOfTime reconcileEventFrequency = new LengthOfTime(1.0, ModelTimeUnit.DAYS);


	public int runNumber() {
		return this.runNumber;
	}


	public int numThreads() {
		return this.numThreads;
	}


	public int numInitialCases() {
		return this.numInitialCases;
	}


	public String dataSource() {
		return this.dataSource;
	}


	public double scale() {
		return this.scale;
	}


	public int approxNumPlaces() {
		return this.approxNumPlaces;
	}


	public double randomInteractionCoef() {
		return this.randomInteractionCoef;
	}


	public double familyInteractionCoef() {
		return this.familyInteractionCoef;
	}


	public LengthOfTime logUpdateFreq() {
		return logUpdateEventFrequency;
	}


	public LengthOfTime reconcileEventFreq() {
		return reconcileEventFrequency;
	}
}
