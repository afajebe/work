package core;


import model.events.ReconcileEvent;
import model.people.DeployablePopulation;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedList;
import model.gui.DefaultPlaceColorer;
import model.gui.PlaceColorer;
import util.Parallelizer;


/**
 * The ThreadManager class replaces the old "Node" class.  This new class reflects the 
 * fact a Deployable GSAM should always have exactly one Node.  The code was dramatically simplified 
 * as a results.
 */
public class ThreadManager {

	/** The Model that is using this Manager. */
	private InteractiveLargeScaleModel gsam;

	/** The Properties that govern this model. */
	private final SimulationProperties props;

	/** The population this Model will simulate. */
	public final DeployablePopulation population;

	/** The threads used to execute the model. */
	private final WorkerThread[] threads;

	/** Save the number of threads to increase code readability. */
	private final int numThreads;

	/** The current Reconcile Number. */
	private int recNum;

	private boolean paused;


	public ThreadManager(InteractiveLargeScaleModel gsam) {
		this.gsam = gsam;
		this.props = gsam.simProps();

		this.population = DeployablePopulation.loadPopulation(props);
		this.numThreads = props.numThreads();
				
		this.threads = createWorkerThreads();

		buildPlacesInParallel();
		this.paused = false;
	}


	private WorkerThread[] createWorkerThreads() {
		
		WorkerThread[] newThreads = new WorkerThread[numThreads];
		
		for (int i = 0; i < newThreads.length; i++) {
			newThreads[i] = new WorkerThread(this, i);
		}
		return newThreads;
	}


	/** Prompt each thread to build the ModelPlaces it is responsible for. */
	private void buildPlacesInParallel() {
		
		class PlaceBuiler implements Runnable {
			
			WorkerThread thread;

			PlaceBuiler(WorkerThread thread) {
				this.thread = thread;
			}

			@Override
			public void run() {
				thread.buildThisThreadsPlaces();
			}			
		}		
		
		Runnable[] jobs = new Runnable[threads.length];
		for (int i = 0; i < jobs.length; i++) {
			jobs[i] = new PlaceBuiler(threads[i]);
		}
		
		logInstantly("Building ModelPlaces in parallel...please wait");
		Parallelizer para = new Parallelizer(this.props.numThreads());
		para.doWorkInParallel(jobs);
	}


	/** @return the number of WorkerThreads used to execute the simulation. */
	public int numThreads() {
		return numThreads;
	}


	/** @return The core model. */
	public InteractiveLargeScaleModel getModel() {
		return this.gsam;
	}


	/** @return - One of the DeployableThreadObjects running this model. */
	WorkerThread getThread(int threadIndex) {
		return this.threads[threadIndex];
	}


	/**
	 * @return - The total number of Intra-Node events that must be distributed. If this is
	 * 0 it is appropriate to reset the ReconcileEvents and to notify the ModelManager that
	 * processing is done.
	 */
	private long getNumberOfQueuedOTCEWaiting() {

		long sum = 0;
		for (int i = 0; i < threads.length; i++) {
			sum += threads[i].getNumQueueOTCE();
		}
		return sum;
	}


	/** Write a String to the command line AND to a file. */
	private void logInstantly(String s) {
		//write to command line
		System.out.println(s);
		
		//push to a text file???
	}


	/** Prompt the Thread Manager to begin processing. */
	public void begin() {

		System.out.println("Do all Initialization Here");

		//initalize some sick agents
		seedSickAgents();

		runModel();
	}


	private void runModel() {
		while (!isFinished()) {
			if (!paused) {
				simulateUntilReconcileEvent();

				if (getNumberOfQueuedOTCEWaiting() == 0) {
					advanceReconcileNum();
				} else {
					transmitOTCE();
				}
			}
		}

		cleanUpAndClose();

	}


	/** Process each working thread until that thread reaches a ReconcileEvent. */
	private void simulateUntilReconcileEvent() {

		Parallelizer para = new Parallelizer(this.numThreads);
		para.doWorkInParallel(threads);
	}


	/**
	 * @return - The total number of OffThreadContactEvents that must be distributed. If this is
	 * 0 it is appropriate to reset all ReconcileEvents.
	 */
	private void transmitOTCE() {
		
		class GetOffThreadContactEvents implements Runnable {
			
			private final WorkerThread thread;
			
			GetOffThreadContactEvents(WorkerThread thread) {
				this.thread = thread;
			}

			@Override
			public void run() {
				thread.retrieveOffThreadContactEventsFromOtherThreads();
			}
		}

		Runnable[] jobs = new Runnable[this.numThreads];
		for (int i = 0; i < jobs.length; i++) {
			jobs[i] = new GetOffThreadContactEvents(threads[i]);
		}
				
		Parallelizer para = new Parallelizer(props.numThreads());
		para.doWorkInParallel(jobs);

		//add a Reconcilation Event, then unpause the threads
		for (int i = 0; i < threads.length; i++) {
			threads[i].addReconcileEvent(threads[i].getCurrentTime());
		}
	}


	/**  Determine which agents are sick and make them that way. */
	private void seedSickAgents() {
		//put 1 sick people in the place with the biggest population

		int biggestIndex = 0;

		for (int i = 0; i < this.population.numPlaces; i++) {
			if (population.placePopulations[i] > population.placePopulations[biggestIndex]) {
				biggestIndex = i;
			}
		}

		System.out.println("Biggest Index :: " + biggestIndex);

		int numInitialCase = props.numInitialCases();

		for (int i = 0; i < numInitialCase; i++) {
			ModelPlace biggestPlace = threads[population.threadOwners[biggestIndex]].getPlace(biggestIndex);
			biggestPlace.locals().exposeRandomAdult();
		}
	}


	private void advanceReconcileNum() {

		//add a Reconcilation Event
		this.recNum++;
		System.out.println("\nNow at Reconcile Num :: " + recNum);
		for (int i = 0; i < threads.length; i++) {
			threads[i].addReconcileEvent(
					threads[i].getCurrentTime().add(ReconcileEvent.getFrequency()));
		}

		gsam.updateGUI();
	}


	/** Examine the threads and determine if all processing is complete. */
	private boolean isFinished() {

		int numSick = getNumSick();
		
//		System.out.println("Num Sick :: " + numSick);
//		System.out.println("Num new Infections :: " + numNewInfections);

		return (numSick == 0);
	}


	/** @return - The number of sick agents. */
	public int getNumSick() {
		int numSick = 0;
		for (int i = 0; i < threads.length; i++) {
			numSick += threads[i].getNumSick();
		}

		return numSick;
	}

	public int getTotalInfections() {

		int totalNumSick = 0;
		for (int i = 0; i < threads.length; i++) {
			totalNumSick += threads[i].getTotalInfections();
		}

		return totalNumSick;
	}


	/** @return - The number of newly infected agents (since the last time this method was called). */
	public int getNumNewInfections() {
		int numNewInfections = 0;
		for (int i = 0; i < threads.length; i++) {
			numNewInfections += threads[i].getNumNewInfections();
		}

		return numNewInfections;
	}

	public void pause() {
		System.out.println("pause");
		this.paused = true;
	}


	public void unpause() {
		System.out.println("Unpause");
		this.paused = false;
	}


	public BufferedImage getCurrentImage() {
		
		PlaceColorer coloringStrategy = new DefaultPlaceColorer();

		//define a simple class that enables us to compile all required info
		class PixelInfo {

			int x;

			int y;

			int color;


			PixelInfo(int x, int y, int color) {
				this.x = x;
				this.y = y;
				this.color = color;
			}
		}

		//create a list of pixel information
		LinkedList<PixelInfo> pixels = new LinkedList<>();

		//for each place: compute distance and retain x,y coordinate
		//also compute max and min distance		
		int minX = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;

		for (int i = 0; i < threads.length; i++) {
			WorkerThread curThread = threads[i];

			Iterator<ModelPlace> iter = curThread.iterator();
			while (iter.hasNext()) {
				
				ModelPlace place = iter.next();
				pixels.add(new PixelInfo(
						place.getX(),
						place.getY(),
						coloringStrategy.getColor(place)));

				minX = Math.min(minX, place.getX());
				maxX = Math.max(maxX, place.getX());
				minY = Math.min(minY, place.getY());
				maxY = Math.max(maxY, place.getY());
			}
		}

//		System.out.println("Max X :: " + maxX);
//		System.out.println("Min X :: " + minX);
//		System.out.println("Max Y :: " + maxY);
//		System.out.println("Min Y :: " + minY);

		//create the image
		int width = maxX + 1;
		int height = maxY + 1;
		BufferedImage image = new BufferedImage(
				width,
				height,
				BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = image.getGraphics();

		//make the background WHITE
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);

		for (PixelInfo pixelInfo : pixels) {
			image.setRGB(
					pixelInfo.x,
					pixelInfo.y,
					pixelInfo.color);
		}

		return image;
	}


	/** Report final output, close logs and ExecutorService */
	private void cleanUpAndClose() {
		//@todo --
		System.out.println("Clean Up And Close");
	}
}
