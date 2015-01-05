package core;


import core.ThreadManager;
import core.SimulationProperties;
import model.events.AgentEvent;
import model.gui.GSAMGui;
import java.awt.image.BufferedImage;


public class InteractiveLargeScaleModel {

	/** The directory where all "readily usable" output should be placed. */
	public static final String OUTPUT_DIRECTORY = "output\\";

	public static InteractiveLargeScaleModel instance;

	/** A GUI that can display current information from the GSAM. */
	private GSAMGui gui;

	/** The Properties that govern this model. */
	private final SimulationProperties simProps;

	/** The object that manages the threads (and maintains the core ExecutorService). */
	private final ThreadManager threadManager;

//	/** The population being simulated (as indicated in the properties file) */
//	private final Population population;
	/** This "runs" unique number. */
	private final int runNumber;


	public InteractiveLargeScaleModel(SimulationProperties simProps) {

		if (InteractiveLargeScaleModel.instance == null) {
			InteractiveLargeScaleModel.instance = this;
		} else {
			throw new IllegalStateException("Only one InteractiveLargeScaleModel can exist at a time");
		}
		this.simProps = simProps;


		this.threadManager = new ThreadManager(this);
		this.runNumber = getRunNumber();
	}


	/** @return - The SimulationProperties that govern this model. */
	public SimulationProperties simProps() {
		return this.simProps;
	}


	/** @return - The current run number. */
	public int getRunNumber() {
		return simProps.runNumber();
	}


	/** Prompt the Thread Manager to begin processing. */
	public void beginModel() {
		this.threadManager.begin();
	}


	public void pause() {
		this.threadManager.pause();
	}


	public void unpause() {
		this.threadManager.unpause();
	}


	public void setSocialDistance(double reductionFraction) {
		AgentEvent.setSocialDistance(reductionFraction);
	}


	/** Connect a GUI to the GSAM. */
	public void addGui(GSAMGui gui) {
		this.gui = gui;
	}


	/** @return - This GSAM's GUI (if it exists). */
	GSAMGui getGUI() {
		return this.gui;
	}


	public void updateGUI() {
		this.updateNumSick(this.threadManager.getNumSick());
		this.updateTotalSick(this.threadManager.getTotalInfections());
		this.updateMap(this.threadManager.getCurrentImage());
	}


	void updateNumSick(int numSick) {
		if (this.gui != null) {
			gui.updateNumSick(numSick);
		}
	}


	void updateTotalSick(int newlySick) {
		if (this.gui != null) {
			gui.updateTotalSick(newlySick);
		}
	}


	void updateMap(BufferedImage newMap) {
		if (this.gui != null) {
			gui.updateMap(newMap);
		}
	}
}
